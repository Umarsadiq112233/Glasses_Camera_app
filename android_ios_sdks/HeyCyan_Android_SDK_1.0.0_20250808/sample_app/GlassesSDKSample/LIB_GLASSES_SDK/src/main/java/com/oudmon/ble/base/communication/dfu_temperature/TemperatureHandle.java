package com.oudmon.ble.base.communication.dfu_temperature;
import android.util.Log;

import com.oudmon.ble.base.bluetooth.IBleListener;
import com.oudmon.ble.base.communication.CompressUtils;
import com.oudmon.ble.base.communication.JPackageManager;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jxr35 on swatch_device_text4/26
 */
public class TemperatureHandle {

    private static final String TAG = "TemperatureHandle";
    /**
     * 获取连续体温数据
     */
    private static final byte ACTION_SERIES = 0x25;
    /**
     * 获取单次的体温数据
     */
    private static final byte ACTION_ONCE = 0x26;
    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");

    private IBleListener mBleManager;
    private HandlerCallback mCallback;

    private byte[] mReceivedData;
    private byte[] mFileSend;
    private short mPocketIndex = 0;

    private boolean mReceiving;
    private int mTotalCount = 0;
    private int mReceivedCount = 0;
    private int mPackageLength;

    private List<String> mFileNames = new ArrayList<>();

    private TemperatureEntity mTempEntity = new TemperatureEntity();

    public TemperatureHandle(IBleListener iBleManagerSrv) {
        this.mBleManager = iBleManagerSrv;
        mPackageLength = JPackageManager.getInstance().getLength();
    }

//    private OnGattEventCallback localEventCallback = new OnGattEventCallback() {
//        @Override
//        public void onReceivedData(UUID char_uuid, byte[] data) {
//            Log.i(TAG, "onReceivedData, uuid: " + char_uuid + ", data: " + DataTransferUtils.getHexString(data));
//            if (data != null) {
//                //设备返回给APP，DATA区=1字节日期+1字节数据间隔+温度数据N字节
//                if ((data[0] & 0xff) == 0xbc && data[1] == ACTION_SERIES) {
//                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
//                    if (mTotalCount == 0) {
//                        mCallback.onComplete();
//                        return;
//                    }
//                    mReceivedCount = data.length - 6;
//                    mReceivedData = new byte[mTotalCount];
//                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
//                    mReceiving = mReceivedCount < mTotalCount;
//                    Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
//                    if (!mReceiving) {
//                        Log.i(TAG, "onReceiver All data: " + DataTransferUtils.getHexString(mReceivedData));
//                        if (mReceivedData.length > 2) {
//                            parseReceivedData(mReceivedData);
//                            mCallback.onNext(mTempEntity);
//                        }
//                    }
//                } else if (mReceiving) {
//                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
//                    mReceivedCount += data.length;
//                    mReceiving = mReceivedCount < mTotalCount;
//                    Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
//                    if (!mReceiving) {
//                        Log.i(TAG, "onReceiver All data: " + DataTransferUtils.getHexString(mReceivedData));
//                        if (mReceivedData.length > 2) {
//                            parseReceivedData(mReceivedData);
//                            mCallback.onNext(mTempEntity);
//                        }
//                    }
//                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x31) {
//                    Log.i(TAG, "初始化完成，开始向手环发送实际文件");
//                    cmdSendPacket();
//                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x32) {
//                    if (readNextBigPocket()) {
//                        int percent = mPocketIndex * 1024 * 100 / mFileSend.length;
//                        Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex);
//                        mCallback.onProgress(percent > 100 ? 100 : percent);
//                    } else {
//                        Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
//                        cmdCheck();
//                    }
//                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x33) {
//                    Log.i(TAG, "===============回调 onComplete");
//                    mCallback.onComplete();
//                }
//            }
//
//        }
//
//        @Override
//        public void onOpResult(BaseRequest theRequest, int errCode) {
//            if (errCode != OnGattEventCallback.ACTION_OK && theRequest instanceof WriteRequest) {
//                //TODO：这里的errCode会与蓝牙操作错误的冲突，暂时先不处理
//                mCallback.onActionResult(((WriteRequest) theRequest).getValue()[0], errCode);//回掉写失败
//            }
//        }
//    };

    private void parseReceivedData(byte[] data) {
        try {
            Log.i(TAG, "===========================ParseReceivedData开始============================");
            mTempEntity.clear();
            mTempEntity.mIndex = data[0];
            mTempEntity.mTimeSpan = data[1];
            mTempEntity.mValues = new float[data[1] == 0 ? 1 : 60 * 24 / mTempEntity.mTimeSpan];
            //TODO 解析协议
            int index = 2;
            int i = 0;
            while (index < data.length) {
                int value = data[index] & 0xff;
                if (value > 0x80) {
                    int length = value - 0x80;
                    int temp = 0;
                    while (temp < length) {
                        mTempEntity.mValues[i++] = 0;
                        temp ++;
                    }
                } else {
                    int temp = data[index];
                    float val = temp * 1F / 10;
                    mTempEntity.mValues[i++] = val + 32;
                    //mTempEntity.mValues[i++] = temp;
                }
                index++;
            }

            Log.i(TAG, "mTempEntity: " + mTempEntity);

            /*int fileCount = data[0];
            if (fileCount > 0) {
                int start = 0;
                while (start < data.length - 1) {
                    int length = data[start + 1];
                    byte[] byteName = new byte[length];
                    System.arraycopy(data, start + 2, byteName, 0, length);
                    start += length + 1;
                    String fileName = new String(byteName);
                    mFileNames.add(fileName);
                    Log.i(TAG, "fileLength: " + length + ", fileName: " + fileName);
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "===========================ParseReceivedData结束============================");
    }


    /**
     * 开始设置监听
     *
     * @param callback 结果
     */
    public void init(HandlerCallback callback) {
        mCallback = callback;
//        enableNotifyRequest.setEnable(true);
//        mBleManager.execute(enableNotifyRequest);
    }

    /**
     * 向手环发送命令，询问手环缺少哪些文件
     */
    public void startObtainSeries(int index) {
        Log.i(TAG, "startObtainSeries... ");
        byte[] data = {(byte) index};
        mBleManager.execute(getWriteRequest(addHeader(ACTION_SERIES, data)));
    }

    /**
     * 向手环发送54指令，测试相关流程
     */
    @Deprecated
    public void testSend() {
        Log.i(TAG, "testSend... ");
        mBleManager.execute(getWriteRequest(addHeader(0x54, null)));
    }

    /**
     * 发送文件到手环，
     * 拆分成包序[2] + 内容[1024] 进行发送
     *
     * @return 是否仍有文件未发送完成
     */
    private boolean sendNextBigPocket() {
        try {
            if (mPocketIndex * 1024 < mFileSend.length) {//已读取的小于文件中长度
                byte[] bufferSend = new byte[Math.min(1024, mFileSend.length - mPocketIndex * 1024)];   //需要发送的文件内容
                System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                byte[] compressBuffer = CompressUtils.compress(bufferSend);   //将需要发送的文件内容压缩

                byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndex + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                sendPocketToBle(addHeader(0x32, bufferNext));
                mPocketIndex++;
                return true;
            } else {
                Log.i(TAG, "文件发送完毕");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "文件发送异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 向手环发送真正的文件数据，
     * 由于一次只能发送1024+个字节，这里会分成多个序号进行发送
     */
    public void cmdSendPacket() {
        mPocketIndex = 0;
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
            if (mPocketIndex * 1024 < mFileSend.length) {//已读取的小于文件中长度
                byte[] bufferSend = new byte[Math.min(1024, mFileSend.length - mPocketIndex * 1024)];   //需要发送的文件内容
                System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                byte[] compressBuffer = CompressUtils.compress(bufferSend);   //将需要发送的文件内容压缩

                byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndex + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                sendPocketToBle(addHeader(0x32, bufferNext));
                mPocketIndex++;
                return true;
            } else {
                Log.i(TAG, "文件发送完毕");
                return false;
            }
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
        Log.i(TAG, "sendPocketToBle: bigPocket=" + DataTransferUtils.getHexString(bigPocket));
        int index = 0;
        while (index * mPackageLength < bigPocket.length) {
            mBleManager.execute(getWriteRequest(Arrays.copyOfRange(bigPocket, index * mPackageLength, index * mPackageLength + Math.min(mPackageLength, bigPocket.length - index * mPackageLength))));
            index++;
        }
    }

    /**
     * 发送完毕指令，告诉手环一个文件发送完毕
     */
    private void cmdCheck() {
        mBleManager.execute(getWriteRequest(addHeader(0x33, null)));
    }

    /**
     * 资源回收
     */
    public void endAndRelease() {
//        enableNotifyRequest.setEnable(false);
//        mBleManager.execute(enableNotifyRequest);
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

    private WriteRequest getWriteRequest(byte[] data) {
        //Log.i(TAG, "getWriteRequest: data=" + DataTransferUtils.getHexString(data));
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_WRITE);
        noRspInstance.setValue(data);
        return noRspInstance;
    }


    public interface HandlerCallback {

        void onProgress(int percent);

        void onComplete();

        void onNext(TemperatureEntity data);

        void onActionResult(int type, int errCode);
    }


}
