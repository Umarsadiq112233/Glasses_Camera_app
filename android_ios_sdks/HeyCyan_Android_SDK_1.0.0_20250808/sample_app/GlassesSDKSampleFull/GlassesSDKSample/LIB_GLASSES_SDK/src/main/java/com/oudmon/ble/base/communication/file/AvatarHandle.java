package com.oudmon.ble.base.communication.file;

import static com.oudmon.ble.base.communication.CompressUtils.compress;


import android.util.Log;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.bluetooth.queue.BleDataBean;
import com.oudmon.ble.base.bluetooth.queue.BleThreadManager;
import com.oudmon.ble.base.communication.JPackageManager;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;


public class AvatarHandle {

    private static final String TAG = "EbookHandle";

    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");

    private ArrayList<String> fileNames = new ArrayList<>();

    private EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_NOTIFY, new EnableNotifyRequest.ListenerCallback() {
        @Override
        public void enable(boolean result) {

        }
    });
    private CopyOnWriteArraySet<IEbookCallback> mCallbackArray = new CopyOnWriteArraySet<>();

    private int currFileType = 0;

    public int getCurrFileType() {
        return currFileType;
    }

    public void setCurrFileType(int currFileType) {
        this.currFileType = currFileType;
    }


    private IEbookCallback mCallback = new IEbookCallback() {

        @Override
        public void onFileNames(ArrayList<String> fileNames) {
            for (IEbookCallback callback : mCallbackArray) {
                callback.onFileNames(fileNames);
            }
        }

        @Override
        public void onProgress(float percent) {
            for (IEbookCallback callback : mCallbackArray) {
                callback.onProgress(percent);
            }
        }

        @Override
        public void onComplete() {
            for (IEbookCallback callback : mCallbackArray) {
                callback.onComplete();
            }
        }

        @Override
        public void onDeleteSuccess(int code) {
            for (IEbookCallback callback : mCallbackArray) {
                callback.onDeleteSuccess(code);
            }
        }

        @Override
        public void onActionResult(int errCode) {
            for (IEbookCallback callback : mCallbackArray) {
                callback.onActionResult(errCode);
            }
        }
    };

    private byte[] mFileSend;
    private short mPocketIndex = 0;
    private int totalCount = 1;
    private int totalSize = 1;

    private int mPackageLength;

    private static AvatarHandle mInstance;

    public static AvatarHandle getInstance() {
        if (mInstance == null) {
            synchronized (AvatarHandle.class) {
                if (mInstance == null) {
                    mInstance = new AvatarHandle();
                }
            }
        }
        return mInstance;
    }

    private AvatarHandle() {
        mPackageLength = JPackageManager.getInstance().getLength();
         Log.i(TAG, "create FileHandle.. mPackageLength: " + mPackageLength);
    }

    public void initRegister() {
        BleOperateManager.getInstance().setCallback(callback);
    }

    public void setDeviceOperateManagerCallback() {
        BleOperateManager.getInstance().setCallback(callback);
    }


    private OnGattEventCallback callback = new OnGattEventCallback() {
        @Override
        public void onReceivedData(String uuid, byte[] data) {
            if (data != null) {
                 Log.i(TAG,ByteUtil.byteArrayToString(data));
                if ((data[0] & 0xff) == 0xbc && data[1] == 0x4a) {
                    if (readNextBigPocket()) {
                        float percent = mPocketIndex * 1024 * 1.0f * 100 / totalSize;
                        DecimalFormat df = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
                        percent = Float.parseFloat(df.format(percent));
                         Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex + "总包:" + totalSize);
                        mCallback.onProgress(Math.min(percent, 100));
                    } else {
                        mCallback.onComplete();
                         Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                    }
                }
            }
        }
    };


    public String unicodeByteToStr(byte[] bBuf) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bBuf.length; i += 2) {
            int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(bBuf, i, i + 2)));
            sb.append((char) index);
        }
        return sb.toString();
    }


    public void registerCallback(IEbookCallback callback) {
        try {
            mCallbackArray.remove(callback);
            mCallbackArray.add(callback);
//            enableNotifyRequest.setEnable(true);
//            DeviceOperateManager.getInstance().execute(enableNotifyRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeCallback(ICallback callback) {
        try {
            mCallbackArray.remove(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCallback() {
        try {
            mCallbackArray.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 向手环发送真正的文件数据，
     * 由于一次只能发送1024+个字节，这里会分成多个序号进行发送
     */
    public void cmdSendPacket() {
        setDeviceOperateManagerCallback();
        mPocketIndex = 0;
        if (mFileSend == null) {
            return;
        }
         Log.i(TAG, "cmdSendPacket.. 开始发送数据，数据长度: " + totalSize);
//          Log.i(TAG, "cmdSendPacket.. 开始发送数据，数据内容: " + DataTransferUtils.getHexString(mFileSend));

        readNextBigPocket();
    }

    /**
     * 发送文件到手环，
     * 拆分成包序[2] + 内容[1024] 进行发送
     *
     * @return 是否仍有文件未发送完成
     */
    private boolean readNextBigPocket() {
        try {
            setDeviceOperateManagerCallback();
            if (mPocketIndex * 1024 < totalSize) {
                if (mFileSend.length > 0) {
                    //已读取的小于文件中长度
                    byte[] bufferSend = new byte[Math.min(1024, totalSize - mPocketIndex * 1024)];   //需要发送的文件内容
                    System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                    byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

                    byte[] bufferNext = new byte[compressBuffer.length + 3];    //在发送的文件前面加一个包序
                    byte[] buff = new byte[3];
                    buff[0] = (byte) totalCount;
                    buff[1] = (byte) (mPocketIndex + 1);
                    buff[2] = 0x01;
                    System.arraycopy(buff, 0, bufferNext, 0, 3);//填充index,包序从1开始
                    System.arraycopy(compressBuffer, 0, bufferNext, 3, compressBuffer.length);  //填充内容
                    sendPocketToBle(addHeader(0x4a, bufferNext));
                    mPocketIndex++;
                    return true;
                }
            } else {
                 Log.i(TAG, "文件发送完毕");
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
             Log.i(TAG, "文件发送异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 真正的发送，由BleManager去处理，这里面不需要去管
     *
     * @param bigPocket pocket
     */
    private void sendPocketToBle(byte[] bigPocket) {
        setDeviceOperateManagerCallback();
        resetPackageLength();
//         Log.i(TAG, "sendPocketToBle: mPackageLength: " + mPackageLength + ", bigPocket=" + DataTransferUtils.getHexString(bigPocket));
        BleThreadManager.getInstance().addData(new BleDataBean(bigPocket, mPackageLength));
    }

    private void resetPackageLength() {
        mPackageLength = JPackageManager.getInstance().getLength();
         Log.i(TAG, "resetPackageLength.. mPackageLength: " + mPackageLength);
    }

    /**
     * 该方法会获取 mFileSend
     *
     * @param data 文件bytes
     * @return 是否成功
     */
    public boolean checkData(byte[] data) {
        mFileSend = data;
        totalSize=data.length;
        int length = (mFileSend.length);
        totalCount = (length / 1024);
        if (length % 1024 != 0) {
            totalCount++;
        }
        setDeviceOperateManagerCallback();
         Log.i(TAG, "总包数: " + totalCount);
        return true;
    }


    /**
     * 资源回收
     */
    public void endAndRelease() {
        enableNotifyRequest.setEnable(false);
        BleOperateManager.getInstance().execute(enableNotifyRequest);
        BleOperateManager.getInstance().setCallback(null);
    }


    /**
     * 将要传送的Data转换成一个可以发送的包
     *
     * @param cmdId 指令ID
     * @param data  要发送的数据data
     * @return 转换后的包
     */
    private byte[] addHeader(int cmdId, byte[] data) {
        byte[] pocket = new byte[(data == null ? 0 : data.length) + 6];
        pocket[0] = (byte) 0xbc;
        pocket[1] = (byte) cmdId;
        if (data != null && data.length > 0) {
            System.arraycopy(DataTransferUtils.shortToBytes((short) data.length), 0, pocket, 2, 2);     //pocket[2], pocket[3]为数据长度信息
            System.arraycopy(DataTransferUtils.shortToBytes((short) CRC16.calcCrc16(data)), 0, pocket, 4, 2);   //pocket[4], pocket[5]为CRC校验
            System.arraycopy(data, 0, pocket, 6, data.length);  //pocket[6]以后为数据data
        } else {
            pocket[4] = (byte) 0xff;
            pocket[5] = (byte) 0xff;
        }
        return pocket;
    }
}
