package com.oudmon.ble.base.communication;
import android.util.Log;


import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class DfuHandle {

    // 串口服务	0x12, 0xA2, 0x4D, 0x2E, 0xFE, 0x14, 0x48, 0x8e, 0x93, 0xD2, 0x17, 0x3C
    public static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");//0000ffea-0000-1000-8000-00805f9b34fb");
    // 串口服务:Watch->App  0xC7, 0x5D, 0x2A, 0x01, 0xE3, 0x65, 0x26, 0xAF,0x47, 0x4E, 0x11, 0xD7, 0x28, 0xF7, 0x5B, 0xDE
    public static final UUID SERIAL_PORT_CHAREACTER_NOTIRY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");//"0000ffeb-0000-1000-8000-00805f9b34fb");
    // 串口服务:App->Watch
    public static final UUID SERIAL_PORT_CHAREACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");//0000ffec-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "DfuHandle";
    public static final int RSP_OK = 0x0;//	成功
    public static final int RSP_DATA_SIZE = 0x1;//	数据大小错误
    public static final int RSP_DATA_CONTENT = 0x2;//	数据内容错误
    public static final int RSP_CMD_STATUS = 0x3;//	指令状态不符
    public static final int RSP_CMD_FORMAT = 0x4;//	指令格式错误
    public static final int RSP_INNER = 0x5;//	设备内部错误
    public static final int RSP_LOW_BATTERY = 0x6;//	设备电量过低

//    private EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(SERIAL_PORT_SERVICE, SERIAL_PORT_CHAREACTER_NOTIRY);
    private byte[] dfuData;
    private IOpResult iOpResult;
    private int mPackageLength;
    private static DfuHandle odmHandle;

    public static DfuHandle getInstance() {
        if (odmHandle == null) {
            synchronized (DfuHandle.class){
                if(odmHandle==null){
                    odmHandle = new DfuHandle();
                }
            }
        }
        return odmHandle;
    }


    public DfuHandle() {
        mPackageLength = JPackageManager.getInstance().getLength();
    }

    public void initCallback(){
        mPackageLength = JPackageManager.getInstance().getLength();
        BleOperateManager.getInstance().setCallback(localEventCallback);
    }

    public void setDeviceOperateManagerCallback(){
        BleOperateManager.getInstance().setCallback(localEventCallback);
    }

    private void openNotify() {
//        enableNotifyRequest.setEnable(true);
//        DeviceOperateManager.getInstance().execute(enableNotifyRequest);
    }

    private OnGattEventCallback localEventCallback = new OnGattEventCallback() {
        @Override
        public void onReceivedData(String uuid, byte[] data){
            if (data == null) {
                return;
            } else {
//                Log.i(TAG,"onReceivedData: data=" + DataTransferUtils.getHexString(data));
                if (checkTheData(data)) {
                    if (data[6] == RSP_OK) {
                        if (data[1] == 0x03) {//bigPocket 发送成功
                            if (!readNextBigPocket()) {//没有数据了
                                iOpResult.onProgress(100);
//                            iOpResult.onActionResult(data[0],data[1]);
                            } else {
                                iOpResult.onProgress(bigPocketIndex * 1024 * 100 / dfuData.length);
                                return;//先不回掉
                            }
                        }
                    }
                    if (iOpResult != null) {
                        if((data[0] & 0xff) == 0xbc && data[1] <= 0x05) {
                            iOpResult.onActionResult(data[1], data[6]);
                        }
                    }
                }
            }
        }

    };

    private boolean checkTheData(byte[] data) {
        Log.i(TAG, "checkTheData: data=" + DataTransferUtils.getHexString(data));
        if (data.length >= 6) {
            if ((data[0] & 0xff) == 0xbc && DataTransferUtils.bytesToShort(data, 2) == data.length - 6) {
                int crc = CRC16.calcCrc16(Arrays.copyOfRange(data, 6, data.length));
                if ((DataTransferUtils.bytesToShort(data, 4) & 0xffff) == crc) {
                    return true;
                } else {
                    Log.e(TAG, "checkTheData: CRC 校验失败");
                }
            } else {
                Log.e(TAG, "checkTheData: 数据长度不一致");
            }
        } else {
            Log.e(TAG, "checkTheData: 协议长度有问题");
        }
        return false;
    }

    private short dfuFileChecksum = 0;
    private short dfuFileCrc16 = 0;

    /**
     * 该方法会获取三个数据dfuData、dfuFileChecksum、dfuFileCrc16
     *
     * @param filePath 路径
     * @return
     */
    public boolean checkFile(String filePath) {
        Log.i(TAG,"OTA升级调试--"+ "选择升级文件：" + filePath);
        File dfuNrfFile = new File(filePath);

        if (!dfuNrfFile.exists()) {
            Log.i(TAG,"OTA升级调试--"+ "文件不存在！");
            return false;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            dfuFileChecksum = 0;
            dfuFileCrc16 = 0;
            randomAccessFile = new RandomAccessFile(filePath, "r");
            if (randomAccessFile.length() > 12000 * 1024) {
                 Log.i(TAG, "文件大小溢出！");
                return false;
            }
            dfuData = new byte[(int) randomAccessFile.length()];
            int size = randomAccessFile.read(dfuData, 0, dfuData.length);
             Log.i(TAG, "start cal check sum.. dataSize=" + dfuData.length + "  readSize=" + size);
            dfuFileChecksum = 0;
            for (int i = 0; i < dfuData.length; i++) {
                dfuFileChecksum += dfuData[i] & 0xff;
            }

            dfuFileChecksum = (short) (dfuFileChecksum & 0xffffffff);

            dfuFileCrc16 = (short) CRC16.calcCrc16(dfuData);

             Log.i(TAG, "dfuFileChecksum: " + dfuFileChecksum + ", dfuFileCrc16: " + dfuFileCrc16);

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void start(IOpResult iOpResult) {
        //Log.i(TAG, "0x01.. start..");
        openNotify();
        this.iOpResult = iOpResult;
        setDeviceOperateManagerCallback();
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x01, null)));
    }

    public void init() {
        //Log.i(TAG, "0x02.. init..");
        setDeviceOperateManagerCallback();
        byte[] data = new byte[9];
        data[0] = 0x01;
        System.arraycopy(DataTransferUtils.intToBytes(dfuData.length), 0, data, 1, 4);      //data[1,2,3,4]为数据长度
        System.arraycopy(DataTransferUtils.shortToBytes(dfuFileCrc16), 0, data, 5, 2);      //data[5,6]为CRC检验
        System.arraycopy(DataTransferUtils.shortToBytes(dfuFileChecksum), 0, data, 7, 2);   //data[7,8]为dfuFileChecksum
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x02, data)));
    }

    private short bigPocketIndex = 0;

    public void sendPacket() {
        bigPocketIndex = 0;
        readNextBigPocket();
    }

    private boolean readNextBigPocket() {
        //Log.i(TAG, "0x03.. readNextBigPocket..");
        setDeviceOperateManagerCallback();
        if (bigPocketIndex * 1024 < dfuData.length) {//已读取的小于文件中长度
            byte[] bufferNext = new byte[Math.min(1024, dfuData.length - bigPocketIndex * 1024) + 2];
            System.arraycopy(DataTransferUtils.shortToBytes((short) (bigPocketIndex + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
            System.arraycopy(dfuData, bigPocketIndex * 1024, bufferNext, 2, bufferNext.length - 2);
            sendPocketToBle(addHeader(0x03, bufferNext));
            bigPocketIndex++;
        } else {
            //数据发送完毕
            Log.i(TAG, "升级包发送完毕");
            return false;
        }
        return true;

    }

    private void sendPocketToBle(byte[] bigPocket) {
        Log.i(TAG, "sendPocketToBle: bigPocket=" + DataTransferUtils.getHexString(bigPocket));
        setDeviceOperateManagerCallback();
        int index = 0;
        while (index * mPackageLength < bigPocket.length) {
            BleOperateManager.getInstance().execute(getWriteRequest(Arrays.copyOfRange(bigPocket, index * mPackageLength, index * mPackageLength + Math.min(mPackageLength, bigPocket.length - index * mPackageLength))));
            index++;
        }
    }

    public void check() {
        //Log.i(TAG, "0x04.. check..");
        setDeviceOperateManagerCallback();
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x04, null)));
    }


    public void endAndRelease() {
        //Log.i(TAG, "0x05.. endAndRelease..");
        iOpResult = null;
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x05, null)));
//        enableNotifyRequest.setEnable(false);
//        DeviceOperateManager.getInstance().execute(enableNotifyRequest);
        BleOperateManager.getInstance().setCallback(null);
    }


    private byte[] addHeader(int cmdid, byte[] data) {
        byte[] pocket = new byte[(data == null ? 0 : data.length) + 6];
        pocket[0] = (byte) 0xbc;
        pocket[1] = (byte) cmdid;
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
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(SERIAL_PORT_SERVICE, SERIAL_PORT_CHAREACTER_WRITE);
        noRspInstance.setValue(data);
        return noRspInstance;
    }


    public interface IOpResult {
        void onActionResult(int type, int errCode);

        void onProgress(int percent);
    }
}
