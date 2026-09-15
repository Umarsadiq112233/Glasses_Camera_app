package com.oudmon.ble.base.communication.file;


import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.bluetooth.queue.BleDataBean;
import com.oudmon.ble.base.bluetooth.queue.BleThreadManager;
import com.oudmon.ble.base.communication.JPackageManager;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.oudmon.ble.base.communication.CompressUtils.compress;

import android.util.Log;

/**
 * Created by Jxr35 on 2018/6/26
 */

public class FileHandle {

    private static final String TAG = "FileHandle";
    public static final int TypeMarketWatchFace = 1;
    public static final int TypeDiyWatchFace = 2;
    public static final int TypeDismissFile = 3;
    public static final int TypeOtaFile = 4;
    /**
     * 获取连续体温数据
     */
    private static final byte ACTION_SERIES = 0x25;
    /**
     * 获取单次的体温数据
     */
    private static final byte ACTION_ONCE = 0x26;
    private static final byte ACTION_A_GPS = 0x54;

    private static final byte ACTION_PLATE = 0x35;

    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");

    private EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_NOTIFY, new EnableNotifyRequest.ListenerCallback() {
        @Override
        public void enable(boolean result) {

        }
    });
    private CopyOnWriteArraySet<ICallback> mCallbackArray = new CopyOnWriteArraySet<>();
    private int currFileType = 0;

    public int getCurrFileType() {
        return currFileType;
    }

    public void setCurrFileType(int currFileType) {
        this.currFileType = currFileType;
    }

    private String notDataString = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private int noDataCount;

    private ICallback mCallback = new ICallback() {

        @Override
        public void onRequestAGPS() {
            for (ICallback callback : mCallbackArray) {
                callback.onRequestAGPS();
            }
        }

        @Override
        public void onUpdatePlate(List<PlateEntity> array) {
            for (ICallback callback : mCallbackArray) {
                callback.onUpdatePlate(array);
            }
        }

        @Override
        public void onUpdatePlateError(int code) {
            for (ICallback callback : mCallbackArray) {
                callback.onUpdatePlateError(code);
            }
        }

        @Override
        public void onDeletePlate() {
            for (ICallback callback : mCallbackArray) {
                callback.onDeletePlate();
            }
        }

        @Override
        public void onDeletePlateError(int code) {
            for (ICallback callback : mCallbackArray) {
                callback.onDeletePlateError(code);
            }
        }

        @Override
        public void onUpdateTemperature(TemperatureEntity data) {
            for (ICallback callback : mCallbackArray) {
                callback.onUpdateTemperature(data);
            }
        }

        @Override
        public void onUpdateTemperatureList(List<TemperatureOnceEntity> array) {
            for (ICallback callback : mCallbackArray) {
                callback.onUpdateTemperatureList(array);
            }
        }

        @Override
        public void onFileNames(ArrayList<String> fileNames) {
            for (ICallback callback : mCallbackArray) {
                callback.onFileNames(fileNames);
            }
        }

        @Override
        public void onProgress(int percent) {
            for (ICallback callback : mCallbackArray) {
                callback.onProgress(percent);
            }
        }

        @Override
        public void onComplete() {
            for (ICallback callback : mCallbackArray) {
                callback.onComplete();
            }
        }

        @Override
        public void onActionResult(int type, int errCode) {
            for (ICallback callback : mCallbackArray) {
                callback.onActionResult(type, errCode);
            }
        }
    };

    private byte[] mReceivedData;
    private byte[] mFileSend;
    private short mPocketIndex = 0;

    private boolean mPlateReceivedFinished = true;    //默认没有新数据
    private boolean mTemperatureReceivedFinished = true;    //默认没有新数据
    private boolean mTemperatureOnceReceivedFinished = true;    //默认没有新数据
    private boolean mReceiving;
    private int mTotalCount = 0;
    private int mReceivedCount = 0;
    private int mPackageLength;

    private List<String> mFileNames = new ArrayList<>();

    private static FileHandle mInstance;

    public static FileHandle getInstance() {
        if (mInstance == null) {
            synchronized (FileHandle.class) {
                if (mInstance == null) {
                    mInstance = new FileHandle();
                }
            }
        }
        return mInstance;
    }

    private FileHandle() {
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
                if ((data[0] & 0xff) == 0xbc && data[1] == ACTION_PLATE) {
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    if (mTotalCount == 0) {
                        return;
                    }
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mPlateReceivedFinished = mReceivedCount >= mTotalCount;
                     Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mPlateReceivedFinished: " + mPlateReceivedFinished);
                    if (mPlateReceivedFinished) {
                         Log.i(TAG, "onReceiver All Temperature data: " + DataTransferUtils.getHexString(mReceivedData));
                        if (mReceivedData.length > 2) {
                            mCallback.onUpdatePlate(DataHelper.parsePlate(mReceivedData));
                        } else {
                            List<PlateEntity> mPlateArray = new ArrayList<>();
                            mCallback.onUpdatePlate(mPlateArray);
                        }
                    }
                } else if (!mPlateReceivedFinished) {
                    try {
                        System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                        mReceivedCount += data.length;
                        mPlateReceivedFinished = mReceivedCount >= mTotalCount;
                        if (mPlateReceivedFinished) {
                            if (mReceivedData.length > 2) {
                                mCallback.onUpdatePlate(DataHelper.parsePlate(mReceivedData));
                            } else {
                                List<PlateEntity> mPlateArray = new ArrayList<>();
                                mCallback.onUpdatePlate(mPlateArray);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == ACTION_A_GPS) {  //A_GPS相关协议
                    if (data[2] == 0x00) {
                        mCallback.onRequestAGPS();
                    } else {
                        if (sendNextBigPocket()) {
                            int percent = mPocketIndex * 1024 * 100 / mFileSend.length;
                             Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex);
                        } else {
                             Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                            cmdCheck();
                        }
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == ACTION_SERIES) {  //体温相关
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    if (mTotalCount == 0) {
                        return;
                    }
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mTemperatureReceivedFinished = mReceivedCount >= mTotalCount;
                     Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mTemperatureReceivedFinished: " + mTemperatureReceivedFinished);
                    if (mTemperatureReceivedFinished) {
                         Log.i(TAG, "onReceiver All Temperature data: " + DataTransferUtils.getHexString(mReceivedData));
                        if (mReceivedData.length > 2) {
                            mCallback.onUpdateTemperature(DataHelper.parseTemperature(mReceivedData));
                        }else {
                            mCallback.onUpdateTemperature(null);
                        }
                    }
                } else if (!mTemperatureReceivedFinished) {
                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mTemperatureReceivedFinished = mReceivedCount >= mTotalCount;
                     Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mTemperatureReceivedFinished: " + mTemperatureReceivedFinished);
                    if (mTemperatureReceivedFinished) {
                         Log.i(TAG, "onReceiver All Temperature data: " + DataTransferUtils.getHexString(mReceivedData));
                        if (mReceivedData.length > 2) {
                             Log.i(TAG, "mCallback: " + mCallback + ", class: " + mCallback.getClass());
                            mCallback.onUpdateTemperature(DataHelper.parseTemperature(mReceivedData));
                        }
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == ACTION_ONCE) {  //体温相关
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    if (mTotalCount == 0) {
                        return;
                    }
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mTemperatureOnceReceivedFinished = mReceivedCount >= mTotalCount;
                     Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mTemperatureOnceReceivedFinished: " + mTemperatureOnceReceivedFinished);
                    if (mTemperatureOnceReceivedFinished) {
                         Log.i(TAG, "onReceiver All Temperature once data: " + DataTransferUtils.getHexString(mReceivedData));
                        if (mReceivedData.length > 2) {
                             Log.i(TAG, "mCallback: " + mCallback + ", class: " + mCallback.getClass());
                            mCallback.onUpdateTemperatureList(DataHelper.parseTemperatureOnce(mReceivedData));
                        }else {
                            mCallback.onUpdateTemperatureList(null);
                        }
                    }
                } else if (!mTemperatureOnceReceivedFinished) {
                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mTemperatureOnceReceivedFinished = mReceivedCount >= mTotalCount;
                     Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mTemperatureOnceReceivedFinished: " + mTemperatureOnceReceivedFinished);
                    if (mTemperatureOnceReceivedFinished) {
                         Log.i(TAG, "onReceiver All Temperature once data: " + DataTransferUtils.getHexString(mReceivedData));
                        if (mReceivedData.length > 2) {
                             Log.i(TAG, "mCallback: " + mCallback + ", class: " + mCallback.getClass());
                            mCallback.onUpdateTemperatureList(DataHelper.parseTemperatureOnce(mReceivedData));
                        }
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x30) {
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mReceiving = mReceivedCount < mTotalCount;
                     Log.i(TAG, "文件：-> 1mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
                         Log.i(TAG, "文件：->2->" + DataTransferUtils.getHexString(mReceivedData));
                        parseFileInfo(mReceivedData);
                        mCallback.onFileNames((ArrayList<String>) mFileNames);
                    }
                } else if (mReceiving) {
                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mReceiving = mReceivedCount < mTotalCount;
                     Log.i(TAG, "文件：->3 mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
                         Log.i(TAG, "文件：->4 ->" + DataTransferUtils.getHexString(mReceivedData));
                        parseFileInfo(mReceivedData);
                        mCallback.onFileNames((ArrayList<String>) mFileNames);
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x31) {
                     Log.i(TAG, "初始化完成，开始向手环发送实际文件");
                    cmdSendPacket();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x32) {
                    if (readNextBigPocket()) {
                        int percent = mPocketIndex * 1024 * 100 / mFileSend.length;
                         Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex);
                        mCallback.onProgress(Math.min(percent, 100));
                    } else {
                         Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                        cmdCheck();
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x33) {
                     Log.i(TAG, "===============回调 onComplete");
                    mCallback.onComplete();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x36) {
                     Log.i(TAG, "初始化完成，开始向手环发送实际文件");
                    //cmdSendPacket();
                    if (data.length > 6 && data[6] > 0) {
                        mCallback.onUpdatePlateError(data[6]);
                        return;
                    }
                    executeFileSend(0x37);
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x37) {
                    if (executeNextSend(0x37)) {
                        int percent = mPocketIndex * 1024 * 100 / mFileSend.length;
                         Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex);
                        mCallback.onProgress(Math.min(percent, 100));
                    } else {
                         Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                        executeFileFinished(0x38);
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x38) {
                    if(data[6]>0){
                        mCallback.onUpdatePlateError(666);
                    }else {
                        mCallback.onComplete();
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x39) {
                    if (data.length > 6 && data[6] > 0) {
                        mCallback.onDeletePlateError(data[6]);
                        return;
                    }
                    mCallback.onDeletePlate();
                }
            }
        }
    };

    private void parseFileInfo(byte[] data) {
        mFileNames.clear();
        try {
            int fileCount = data[0];
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void registerCallback(ICallback callback) {
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
     * 连续体温
     */
    public void startObtainTemperatureSeries(int index) {
         Log.i(TAG, "startObtainSeries... ");
        byte[] data = {(byte) index};
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_SERIES, data), mPackageLength));
    }

    /***
     * 单次体温
     * @param index
     */
    public void startObtainTemperatureOnce(int index) {
         Log.i(TAG, "startObtainOnce... ");
        byte[] data = {(byte) index};
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_ONCE, data), mPackageLength));
    }

    /**
     * 获取手环上的表盘市场数据
     */
    public void startObtainPlate() {
         Log.i(TAG, "startObtainPlate... ");
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_PLATE, null), mPackageLength));
    }

    /**
     * 向手环发送命令，询问手环缺少哪些文件
     */
    public void start() {
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x30, null), mPackageLength));
    }


    /**
     * 向手环发送54指令，测试相关流程
     */
    @Deprecated
    public void testSend() {
         Log.i(TAG, "testSend... ");
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x54, null)));
    }

    /**
     * 该方法会获取 mFileSend
     *
     * @param filePath 路径
     * @return 是否成功
     */
    public boolean checkFile(String filePath) {
         Log.i(TAG, "准备发送的文件路径：" + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
             Log.i(TAG, "准备发送的文件不存在！");
            return false;
        }
        RandomAccessFile accessFile = null;
        try {
            mFileSend = fileToByteStr(filePath);
             Log.i(TAG, "准备发送的文件.. dataSize=" + mFileSend.length + "  readSize=" + mFileSend.length);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 该方法会获取 mFileSend
     *
     * @param data 文件bytes
     * @return 是否成功
     */
    public boolean checkData(byte[] data) {
         Log.i(TAG, "checkData... dataSize: " + data.length);
        mFileSend = data;
        return true;
    }

    public static byte[] fileToByteStr(String path) {
        byte[] data = null;
        try {
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                return data;
            }
            InputStream in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 表盘文件发送的准备工作，在调用init之前必须调用一次prepare
     *
     * @param path 路径
     * @return 是否成功
     */
    public boolean executeFilePrepare(String path) {
        noDataCount=0;
        File file = new File(path);
        if (!file.exists()) {
             Log.i(TAG, "准备发送的文件不存在！");
            return false;
        }
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(path, "r");
            mFileSend = new byte[(int) accessFile.length()];
            int size = accessFile.read(mFileSend, 0, mFileSend.length);
           Log.i(TAG, "准备发送的文件.. dataSize = " + mFileSend.length + ", readSize = " + size);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * 发送表盘文件的初始化文件
     *
     * @param fileName 文件名称
     * @param cmd      指令
     */
    public void executeFileInit(String fileName, int cmd) {
        try {
            setDeviceOperateManagerCallback();
             Log.i(TAG, "executeFileInit.. 开始");
            byte[] fileNames = fileName.getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[fileNames.length + 10];
            data[0] = 0x01;
            System.arraycopy(DataTransferUtils.intToBytes(mFileSend.length), 0, data, 1, 4);      //data[1,2,3,4]为数据长度
            data[9] = (byte) fileNames.length;
            System.arraycopy(fileNames, 0, data, 10, fileNames.length);

            sendPocketToBle(addHeader(cmd, data));
             Log.i(TAG, "executeFileInit.. 完成");
        } catch (Exception e) {
             Log.i(TAG, "executeFileInit.. Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 向手环发送真正的文件数据，
     * 由于一次只能发送1024+个字节，这里会分成多个序号进行发送
     */
    private void executeFileSend(int cmd) {
        setDeviceOperateManagerCallback();
        mPocketIndex = 0;
         Log.i(TAG, "executeFileSend.. 开始发送数据，数据长度: " + mFileSend.length);
//          Log.i(TAG, "executeFileSend.. 开始发送数据，数据内容: " + DataTransferUtils.getHexString(mFileSend));
        executeNextSend(cmd);
    }

    /**
     * 真正执行发送文件的操作
     *
     * @param cmd 指令
     * @return 是否发送完成
     */
    private boolean executeNextSend(int cmd) {
        try {
            setDeviceOperateManagerCallback();
            if (mPocketIndex * 1024 < mFileSend.length) {//已读取的小于文件中长度
                byte[] bufferSend = new byte[Math.min(1024, mFileSend.length - mPocketIndex * 1024)];   //需要发送的文件内容
                System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩
                byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndex + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                sendPocketToBle(addHeader(cmd, bufferNext));
                mPocketIndex++;
                if(notDataString.equalsIgnoreCase(ByteUtil.byteArrayToString(bufferSend)) && cmd ==0x37){
                    noDataCount++;
                    if(noDataCount>30){
                         Log.i(TAG,"表盘下发异常，全为0的数据");
                        return false;
                    }
                }else {
                    noDataCount=0;
                }
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
     * 发送完毕指令，告诉手环一个文件发送完毕
     */
    private void executeFileFinished(int cmd) {
        setDeviceOperateManagerCallback();
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(cmd, null), mPackageLength));
    }

    public void executeFileDelete(String name) {
        setDeviceOperateManagerCallback();
         Log.i(TAG, "executeFileDelete.. name: " + name);
        byte[] fileNames = name.getBytes(StandardCharsets.UTF_8);
         Log.i(TAG, "executeFileDelete.. fileNames: " + DataTransferUtils.getHexString(fileNames));
        byte[] data = new byte[fileNames.length + 1];
        data[0] = 0x01;
        System.arraycopy(fileNames, 0, data, 1, fileNames.length);      //data[1,2,3,4]为数据长度
         Log.i(TAG, "executeFileDelete.. data: " + DataTransferUtils.getHexString(data));
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x39, data), mPackageLength));
    }

    public void executeMusicSend(boolean playing, int progress, int volume, String name) {
         Log.i(TAG, "executeMusicSend.. playing: " + playing + ", progress: " + progress + ", volume: " + volume + ", name: " + name);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
         Log.i(TAG, "executeMusicSend.. nameBytes: " + DataTransferUtils.getHexString(nameBytes));
        byte[] data = new byte[nameBytes.length + 3];
        data[0] = (byte) (playing ? 0 : 1);
        data[1] = (byte) progress;
        data[2] = (byte) volume;
        System.arraycopy(nameBytes, 0, data, 3, nameBytes.length);      //data[1,2,3,4]为数据长度
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(0x6, data), mPackageLength));
    }


    /**
     * 向手环发送文件初始化命令
     * 注意，在执行本命令前必须要进行checkFile操作，以读取到文件的实际内容
     *
     * @param fileName 文件名
     */
    public void cmdFileInit(String fileName) {
        try {
            setDeviceOperateManagerCallback();
            byte[] fileNames = fileName.getBytes("UTF-8");
            byte[] data = new byte[fileNames.length + 10];
            data[0] = 0x01;
            System.arraycopy(DataTransferUtils.intToBytes(mFileSend.length), 0, data, 1, 4);      //data[1,2,3,4]为数据长度
            data[9] = (byte) fileNames.length;
            System.arraycopy(fileNames, 0, data, 10, fileNames.length);

            sendPocketToBle(addHeader(0x31, data));
            //mBleManager.execute(getWriteRequest(addHeader(0x31, data)), localEventCallback);
             Log.i(TAG, "cmdFileInit.. 完成");
        } catch (Exception e) {
             Log.i(TAG, "cmdFileInit.. Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startGpsOnline() {
        mPocketIndex = 0;
        sendNextBigPocket();
    }

    /**
     * 发送文件到手环，
     * 拆分成包序[2] + 内容[1024] 进行发送
     *
     * @return 是否仍有文件未发送完成
     */
    private boolean sendNextBigPocket() {
        try {
            setDeviceOperateManagerCallback();
            if (mPocketIndex * 1024 < mFileSend.length) {//已读取的小于文件中长度
                byte[] bufferSend = new byte[Math.min(1024, mFileSend.length - mPocketIndex * 1024)];   //需要发送的文件内容
                System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

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
        setDeviceOperateManagerCallback();
        mPocketIndex = 0;
        if (mFileSend == null) {
            return;
        }
         Log.i(TAG, "cmdSendPacket.. 开始发送数据，数据长度: " + mFileSend.length);
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
            if (mPocketIndex * 1024 < mFileSend.length) {//已读取的小于文件中长度
                byte[] bufferSend = new byte[Math.min(1024, mFileSend.length - mPocketIndex * 1024)];   //需要发送的文件内容
                System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

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

    public WriteRequest getWriteRequest(byte[] data) {
         Log.i(TAG, "getWriteRequest: data=" + DataTransferUtils.getHexString(data));
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_WRITE);
        noRspInstance.setValue(data);
        return noRspInstance;
    }

    public static String getMD5Three(String path) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }
}
