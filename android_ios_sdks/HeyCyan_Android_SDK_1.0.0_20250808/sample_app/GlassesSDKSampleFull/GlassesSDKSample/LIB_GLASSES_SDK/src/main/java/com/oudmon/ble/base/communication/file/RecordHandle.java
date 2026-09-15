package com.oudmon.ble.base.communication.file;


import android.util.Log;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.bluetooth.queue.BleDataBean;
import com.oudmon.ble.base.bluetooth.queue.BleThreadManager;
import com.oudmon.ble.base.communication.JPackageManager;
import com.oudmon.ble.base.communication.entity.RecordEntity;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Jxr35 on 2/7
 */

public class RecordHandle {

    private static final String TAG = "RecondHandle";
    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");

    private ArrayList<RecordEntity> fileNames = new ArrayList<>();

    private EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_NOTIFY, new EnableNotifyRequest.ListenerCallback() {
        @Override
        public void enable(boolean result) {

        }
    });
    private CopyOnWriteArraySet<IRecordCallback> mCallbackArray = new CopyOnWriteArraySet<>();
    private int currFileType = 0;

    private int mReceivedCount = 0;
    private int mTotalCount=0;
    private boolean mReceiving;
    private byte[] mDetails;
    private String fileName;

    private int mReceivedCountName = 0;
    private int mTotalCountName=0;
    private boolean mReceivingName;
    private byte[] mDetailsName;

    public int getCurrFileType() {
        return currFileType;
    }

    public void setCurrFileType(int currFileType) {
        this.currFileType = currFileType;
    }


    private IRecordCallback mCallback = new IRecordCallback() {

        @Override
        public void onFileNames(ArrayList<RecordEntity> fileNames) {
            for (IRecordCallback callback : mCallbackArray) {
                callback.onFileNames(fileNames);
            }
        }

        @Override
        public void onProgress(float percent) {
            for (IRecordCallback callback : mCallbackArray) {
                callback.onProgress(percent);
            }
        }

        @Override
        public void onComplete() {
            for (IRecordCallback callback : mCallbackArray) {
                callback.onComplete();
            }
        }

        @Override
        public void onReceiver(byte[] data) {
            for (IRecordCallback callback : mCallbackArray) {
                callback.onReceiver(data);
            }
        }


        @Override
        public void onActionResult(int errCode) {
            for (IRecordCallback callback : mCallbackArray) {
                callback.onActionResult(errCode);
            }
        }
    };


    private int mPackageLength;


    private static RecordHandle mInstance;

    public static RecordHandle getInstance() {
        if (mInstance == null) {
            synchronized (RecordHandle.class) {
                if (mInstance == null) {
                    mInstance = new RecordHandle();
                }
            }
        }
        return mInstance;
    }

    private RecordHandle() {
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
                 if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x82) {
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    mReceivedCount = data.length - 6;
                    mReceiving = mReceivedCount < mTotalCount;
                    mDetails = new byte[mTotalCount];
                    System.arraycopy(data, 6, mDetails, 0, mReceivedCount);
                    if (!mReceiving) {
                        parseRecord(mDetails);
                    }
                }else if(mReceiving) {
                    System.arraycopy(data, 0, mDetails, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mReceiving = mReceivedCount < mTotalCount;
                    if (!mReceiving) {
                        parseRecord(mDetails);
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x80) {
                    // bc80a0008ce4 03 04 00 00 000c0b00 22 3200330030003100310032005f003100370034003500300038002e0064006100740000801f00223200330030003100310032005f003100370034003500330034002e0064006100740000640300223200330030003200300032005f003100370034003300340037002e0064006100740000b40300223200330030003200300037005f003100340030003800300033002e00640061007400
                    mTotalCountName= DataTransferUtils.bytesToShort(data, 2);
                    mReceivedCountName = data.length - 6;
                    mReceivingName = mReceivedCountName < mTotalCountName;
                    mDetailsName = new byte[mTotalCountName];
                    System.arraycopy(data, 6, mDetailsName, 0, mReceivedCountName);
                    if (!mReceivingName) {
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 8, 9)));
                            parseRecordData(mDetailsName, 4, 9, offset, total, 0);
                        } else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                }else if(mReceivingName) {
                    System.arraycopy(data, 0, mDetailsName, mReceivedCountName, data.length);
                    mReceivedCountName += data.length;
                    mReceivingName = mReceivedCountName < mTotalCountName;
                    if (!mReceivingName) {
                         Log.i(TAG,ByteUtil.byteArrayToString(mDetailsName));
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mDetailsName, 8, 9)));
                            parseRecordData(mDetailsName, 4, 9, offset, total, 0);
                        } else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                }
            }
        }
    };

    private void parseRecord(byte[] data){
        int total=ByteUtil.bytesToInt(Arrays.copyOfRange(data, 0, 4));
        int curr=ByteUtil.bytesToInt(Arrays.copyOfRange(data, 4, 8));
         Log.i(TAG,total+"---"+curr);
        try {
            float percent = (curr)  * 1.0f * 100 / total;
            DecimalFormat df = new DecimalFormat("#.00",new DecimalFormatSymbols(Locale.US));
            percent = Float.parseFloat(df.format(percent));
            mCallback.onProgress(Math.min(percent, 100));
            if(curr < total){
                mCallback.onReceiver(Arrays.copyOfRange(data, 8, data.length));
                readRecordFile(curr+1,fileName);
            }else {
                mCallback.onComplete();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void parseRecordData(byte[] data, int lengthStart, int start, int offset, int total, int currIndex) {
        try {
            currIndex++;
            int fileLength = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, lengthStart, lengthStart + 4)));
            byte[] nameArrays = Arrays.copyOfRange(data, start, start + offset);
            String name = unicodeByteToStr(nameArrays);
            RecordEntity entity = new RecordEntity();
            entity.setFileName(name);
            entity.setLength(fileLength);
            fileNames.add(entity);
            if (currIndex < total) {
                parseRecordData(data, start + offset, start + 4 + offset + 1, data[start + 4 + offset], total, currIndex);
            } else {
                mCallback.onFileNames(fileNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String unicodeByteToStr(byte[] bBuf) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bBuf.length; i += 2) {
            int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(bBuf, i, i + 2)));
            sb.append((char) index);
        }
        return sb.toString();
    }


    public void registerCallback(IRecordCallback callback) {
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


    public void start(int packIndex) {
        byte[] data = new byte[]{0x03, (byte) packIndex};
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x80, data), mPackageLength));
    }

    public void readRecordFile(int packageIndex, String fileName) {
        resetPackageLength();
        this.fileName=fileName;
        byte[] indexArray = ByteUtil.intToByte(packageIndex, 4);
        byte[] deleteBytes = fileName.getBytes(Charset.forName("unicode"));
        if (!ByteUtil.byteArrayToString(deleteBytes).startsWith("fffe")) {
            for (int i = 0; i < deleteBytes.length; i += 2) {
                int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(deleteBytes, i, i + 2)));
                deleteBytes[i] = (byte) ByteUtil.hiword(index);
                deleteBytes[i + 1] = (byte) ByteUtil.loword(index);
            }
        }
        byte[] data = new byte[]{indexArray[0], indexArray[1], indexArray[2], indexArray[3], (byte) deleteBytes.length};
        byte[] b = ByteUtil.concat(data, deleteBytes);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x82, b), mPackageLength));
    }


    /**
     * 判断文本文件的字符集，文件开头三个字节表明编码格式。
     *
     * @param path
     * @return
     * @throws Exception
     * @throws Exception
     */
    public static String charset(String path) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bis.mark(0); // 读者注： bis.mark(0);修改为 bis.mark(100);我用过这段代码，需要修改上面标出的地方。
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                bis.close();
                return charset; // 文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; // 文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; // 文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; // 文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) { // 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("--文件-> [" + path + "] 采用的字符集为: [" + charset + "]");
        return charset;
    }


    private void resetPackageLength() {
        mPackageLength = JPackageManager.getInstance().getLength();
         Log.i(TAG, "resetPackageLength.. mPackageLength: " + mPackageLength);
    }

    /**
     * 发送完毕指令，告诉手环一个文件发送完毕
     */
    private void cmdCheck() {
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x33, null), mPackageLength));
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


    /**
     * 分隔数组 根据段数分段 <多出部分在最后一个数组>
     *
     * @param data     被分隔的数组
     * @param segments 需要分隔的段数
     * @return
     */
    public List<List<byte[]>> subListBySegment(List<byte[]> data, int segments) {

        List<List<byte[]>> result = new ArrayList<>();

        int size = data.size();// 数据长度

        if (size > 0 && segments > 0) {// segments == 0 ，不需要分隔

            int count = size / segments;// 每段数量

            List<byte[]> cutList = null;// 每段List

            for (int i = 0; i < segments; i++) {
                if (i == segments - 1) {
                    cutList = data.subList(count * i, size);
                } else {
                    cutList = data.subList(count * i, count * (i + 1));
                }
                result.add(cutList);
            }
        } else {
            result.add(data);
        }
        return result;
    }
}
