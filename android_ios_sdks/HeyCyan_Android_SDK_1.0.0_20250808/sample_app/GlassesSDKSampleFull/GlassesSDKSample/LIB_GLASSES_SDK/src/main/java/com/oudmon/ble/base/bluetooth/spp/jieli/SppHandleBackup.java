package com.oudmon.ble.base.bluetooth.spp.jieli;

import static com.oudmon.ble.base.communication.CompressUtils.compress;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.bluetooth.spp.SerialListener;
import com.oudmon.ble.base.bluetooth.spp.SerialSocket;
import com.oudmon.ble.base.communication.file.ICallback;
import com.oudmon.ble.base.communication.file.IEbookCallback;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArraySet;


public class SppHandleBackup {
    private static final String TAG = "SppHandle";
    public static int FileTypeMuSic=0x03;

    private ArrayList<String> fileNames = new ArrayList<>();

    private CopyOnWriteArraySet<IEbookCallback> mCallbackArray = new CopyOnWriteArraySet<>();
    private int currFileType = 0;
    private SerialListener outSerialListener;
    private int mTotalCount = 0;
    private int mReceivedCount = 0;
    private byte[] mReceivedData;
    private boolean mReceiving;

    private byte[] mFileSend;
    private byte[] mFileSendA;
    private byte[] mFileSendB;
    private short mPocketIndex = 0;
    private short mPocketIndexA = 0;
    private short mPocketIndexB = 0;
    private int totalSize=1;
    private int sizeA=1;
    private int sizeB=1;

    private int mPackageLength=512;
    public static int PACKAGE_LENGTH=512;



    private static SppHandleBackup mInstance;



    public int getCurrFileType() {
        return currFileType;
    }

    public void setCurrFileType(int currFileType) {
        this.currFileType = currFileType;
    }

    public void setOutSerialListener(SerialListener outSerialListener) {
        this.outSerialListener = outSerialListener;
    }

    private SerialListener serialListener=new SerialListener() {
        @Override
        public void onSerialConnect() {
            if(outSerialListener!=null){
                outSerialListener.onSerialConnect();
            }
        }

        @Override
        public void onSerialConnectError(Exception e) {
            if(outSerialListener!=null){
                outSerialListener.onSerialConnectError(e);
            }
        }

        @Override
        public void onSerialRead(byte[] data) {
            if (data != null) {
                if ((data[0] & 0xff) == 0xbc && data[1] == 0x31) {
                     Log.i(TAG, "初始化完成，开始向手环发送实际文件");
                    cmdSendPacket();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x32) {
                    //bc810100bf4000
                     Log.i(TAG,ByteUtil.byteArrayToString(data));
                    int code = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                    if(code==0){
                        if (readNextBigPocket()) {
                            float percent = mPocketIndex * 1024 * 1.0f * 100 / totalSize;
                            DecimalFormat df = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
                            percent = Float.parseFloat(df.format(percent));
                             Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex+"总包:"+totalSize);
                            mCallback.onProgress(Math.min(percent, 100));
                        } else {
                             Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                            cmdCheck();
                        }
                    }else {
                        mCallback.onDeleteSuccess(code);
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x33) {
                     Log.i(TAG, "===============回调 onComplete");
                    mFileSend=new byte[]{};
                    mCallback.onComplete();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x80) {
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mReceiving = mReceivedCount < mTotalCount;
//                     Log.i(TAG, "文件：-> 1mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
//                         Log.i(TAG, "文件：->2->" + DataTransferUtils.getHexString(mReceivedData));
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 4, 5)));
                            parseEbookData(mReceivedData, 5, offset, total, 0);
                        }else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                } else if (mReceiving) {
                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mReceiving = mReceivedCount < mTotalCount;
//                     Log.i(TAG, "文件：->3 mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
//                         Log.i(TAG, "文件：->4 ->" + DataTransferUtils.getHexString(mReceivedData));
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 4, 5)));
                            parseEbookData(mReceivedData, 5, offset, total, 0);
                        }else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                }  else if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x81) {
                    //bc810100bf4000
                    int code = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                    mCallback.onDeleteSuccess(code);
                }
            }
        }

        @Override
        public void onSerialRead(ArrayDeque<byte[]> datas) {

        }

        @Override
        public void onSerialIoError(Exception e) {
            if(outSerialListener!=null){
                outSerialListener.onSerialIoError(e);
            }
        }
    };

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



    public static SppHandleBackup getInstance() {
        if (mInstance == null) {
            synchronized (SppHandleBackup.class) {
                if (mInstance == null) {
                    mInstance = new SppHandleBackup();
                }
            }
        }
        return mInstance;
    }

    private SppHandleBackup() {
        mPackageLength = PACKAGE_LENGTH;
         Log.i(TAG, "create FileHandle.. mPackageLength: " + mPackageLength);
    }

    public void initRegister() {
        registerMusicBleCallback();
        SerialSocket.getInstance().setListener(serialListener);
    }

    public void registerMusicBleCallback(){
        BleOperateManager.getInstance().setCallback(callback);
    }

    private OnGattEventCallback callback = new OnGattEventCallback() {
        @Override
        public void onReceivedData(String uuid, byte[] data) {
            if (data != null) {
//                 Log.i(TAG,ByteUtil.byteArrayToString(data));
                if ((data[0] & 0xff) == 0xbc && data[1] == 0x31) {
                     Log.i(TAG, "初始化完成，开始向手环发送实际文件");
                    cmdSendPacket();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x32) {
                    //bc810100bf4000
                    int code = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                    if(code==0){
                        if (readNextBigPocket()) {
                            float percent = mPocketIndex * 1024 * 1.0f * 100 / totalSize;
                            DecimalFormat df = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
                            percent = Float.parseFloat(df.format(percent));
                             Log.i(TAG, "向手环发送数据进度: " + percent + ", 包序: " + mPocketIndex+"总包:"+totalSize);
                            mCallback.onProgress(Math.min(percent, 100));
                        } else {
                             Log.i(TAG, "向手环发送数据完毕" + ", 包序: " + mPocketIndex);
                            cmdCheck();
                        }
                    }else {
                        mCallback.onDeleteSuccess(code);
                    }
                } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x33) {
                     Log.i(TAG, "===============回调 onComplete");
                    mFileSend=new byte[]{};
                    mCallback.onComplete();
                } else if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x80) {
                    mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                    mReceivedCount = data.length - 6;
                    mReceivedData = new byte[mTotalCount];
                    System.arraycopy(data, 6, mReceivedData, 0, mReceivedCount);
                    mReceiving = mReceivedCount < mTotalCount;
                     Log.i(TAG, "文件：-> 1mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
                         Log.i(TAG, "文件：->2->" + DataTransferUtils.getHexString(mReceivedData));
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 4, 5)));
                            parseEbookData(mReceivedData, 5, offset, total, 0);
                        }else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                } else if (mReceiving) {
                    System.arraycopy(data, 0, mReceivedData, mReceivedCount, data.length);
                    mReceivedCount += data.length;
                    mReceiving = mReceivedCount < mTotalCount;
                     Log.i(TAG, "文件：->3 mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mReceiving: " + mReceiving);
                    if (!mReceiving) {
                         Log.i(TAG, "文件：->4 ->" + DataTransferUtils.getHexString(mReceivedData));
                        int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 1, 2)));
                        int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 2, 3)));
                        if (currIndex == 0) {
                            fileNames = new ArrayList<>();
                        }
                        if (total > 0) {
                            int offset = (ByteUtil.bytesToInt(Arrays.copyOfRange(mReceivedData, 4, 5)));
                            parseEbookData(mReceivedData, 5, offset, total, 0);
                        }else {
                            mCallback.onFileNames(fileNames);
                        }
                    }
                }  else if ((data[0] & 0xff) == 0xbc && data[1] == (byte) 0x81) {
                    //bc810100bf4000
                    int code = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                    mCallback.onDeleteSuccess(code);
                }
            }
        }
    };

    public void connect(BluetoothDevice device){
        SerialSocket.getInstance().setDevice(device);
        SerialSocket.getInstance().connect(serialListener);
    }

    public void disconnect(){
        SerialSocket.getInstance().disconnect();
    }

    public boolean isConnected(){
       return SerialSocket.getInstance().isConnected();
    }


    private void parseEbookData(byte[] data, int start, int offset, int total, int currIndex) {
        try {
            currIndex++;
            byte[] nameArrays = Arrays.copyOfRange(data, start, start + offset);
            String name = unicodeByteToStr(nameArrays);
            fileNames.add(name);
            if (currIndex < total) {
                parseEbookData(data, start + offset + 1, data[start + offset], total, currIndex);
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


    public void start(int packIndex) {
        byte[] data = new byte[]{0x01, (byte) packIndex};
        SerialSocket.getInstance().write(addHeader(0x80, data));
    }

    public void deleteMusic(int position, String fileName) {
        try {
            byte[] deleteBytes = fileName.getBytes(Charset.forName("unicode"));
            if(!ByteUtil.byteArrayToString(deleteBytes).startsWith("fffe")){
                for (int i = 0; i < deleteBytes.length; i += 2) {
                    int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(deleteBytes, i, i + 2)));
                    deleteBytes[i] = (byte) ByteUtil.hiword(index);
                    deleteBytes[i + 1] = (byte) ByteUtil.loword(index);
                }
            }
            byte[] data = new byte[]{0x01, (byte) position, (byte) deleteBytes.length};
            byte[] contactBytes = ByteUtil.concat(data, deleteBytes);
            SerialSocket.getInstance().write(addHeader(0x81, contactBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * 表盘文件发送的准备工作，在调用init之前必须调用一次prepare
     *
     * @param path 路径
     * @return 是否成功
     */
    public boolean executeFilePrepare(String path) {
        if(currFileType==FileTypeMuSic){
            File file = new File(path);
            if (!file.exists()) {
                 Log.i(TAG, "准备发送的文件不存在！");
                return false;
            }
            String charSet = charset(path);
             Log.i(TAG, charSet);
            BufferedReader reader = null;
            StringBuffer sbf = new StringBuffer();
            try {
                FileInputStream inputStream = new FileInputStream(path);
                InputStreamReader reader1 = new InputStreamReader(inputStream, charSet);
                reader = new BufferedReader(reader1);
                String tempStr;
                while ((tempStr = reader.readLine()) != null) {
                    sbf.append(tempStr).append("\n");
                }
                reader.close();
                String book = sbf.toString();
                mPocketIndexA=0;
                mPocketIndexB=0;
                mPocketIndex=0;

                if(file.length()>10*1024*1024){
                    mFileSend=new byte[]{};
                    int length=book.length()/2;
                    String b1=book.substring(0,length);
                    String b2=book.substring(length);
                    mFileSendA = b1.getBytes(Charset.forName("unicode"));
                    mFileSendB = b2.getBytes(Charset.forName("unicode"));
//                mFileSend = book.getBytes(Charset.forName("unicode"));
                    sizeA=mFileSendA.length;
                    sizeB=mFileSendB.length;
                    if(!ByteUtil.byteArraySubToString(mFileSendA).startsWith("fffe")){
                        for (int i = 0; i < sizeA; i += 2) {
                            int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(mFileSendA, i, i + 2)));
                            mFileSendA[i] = (byte) ByteUtil.hiword(index);
                            mFileSendA[i + 1] = (byte) ByteUtil.loword(index);
                        }
                    }
                    if(!ByteUtil.byteArraySubToString(mFileSendB).startsWith("fffe")){
                        for (int i = 2; i < sizeB; i += 2) {
                            int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(mFileSendB, i, i + 2)));
                            mFileSendB[i-2] = (byte) ByteUtil.hiword(index);
                            mFileSendB[i-1] = (byte) ByteUtil.loword(index);
                        }
                    }

                    int a=1024-sizeA%1024;
                    byte[] tempA=new byte[a];
                    System.arraycopy(mFileSendB, 0, tempA, 0,a);

                    byte[] tempB=new byte[mFileSendB.length-a];
                    System.arraycopy(mFileSendB, 0, tempB, 0,mFileSendB.length-a);

                    mFileSendA=ByteUtil.concat(mFileSendA,tempA);
                    sizeA=mFileSendA.length;
                    mFileSendB=tempB;
                    sizeB=mFileSendB.length;
                     Log.i(TAG,sizeA+"----"+sizeB);
                    totalSize=sizeA+sizeB;
                }else {
//                    mFileSend = book.getBytes(Charset.forName("unicode"));
//                    totalSize=mFileSend.length;
//                    if(!ByteUtil.byteArraySubToString(mFileSend).startsWith("fffe")){
//                        for (int i = 0; i < totalSize; i += 2) {
//                            int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(mFileSend, i, i + 2)));
//                            mFileSend[i] = (byte) ByteUtil.hiword(index);
//                            mFileSend[i + 1] = (byte) ByteUtil.loword(index);
//                        }
//                    }

                    RandomAccessFile accessFile = null;
                    try {
                        accessFile = new RandomAccessFile(path, "r");
                        mFileSend = new byte[(int) accessFile.length()];
                        totalSize=mFileSend.length;
                        int size = accessFile.read(mFileSend, 0, mFileSend.length);
                       Log.i(TAG,"准备发送的文件.. dataSize = " + mFileSend.length + ", readSize = " + size);
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

                }

                if (totalSize > 0) {
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            return false;
        }
        return false;
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


    /**
     * 向手环发送文件初始化命令
     * 注意，在执行本命令前必须要进行checkFile操作，以读取到文件的实际内容
     *
     * @param fileName 文件名
     */
    public void cmdFileInit(String fileName) {
        try {
            byte[] fileNames = fileName.getBytes(Charset.forName("unicode"));
            if(!ByteUtil.byteArrayToString(fileNames).startsWith("fffe")){
                for (int i = 0; i < fileNames.length; i += 2) {
                    int index = (ByteUtil.bytesToInt(Arrays.copyOfRange(fileNames, i, i + 2)));
                    fileNames[i] = (byte) ByteUtil.hiword(index);
                    fileNames[i + 1] = (byte) ByteUtil.loword(index);
                }
            }
             Log.i(TAG, ByteUtil.byteArrayToString(fileNames));

            byte[] data = new byte[fileNames.length + 10];
            data[0] = (byte) currFileType;
            System.arraycopy(DataTransferUtils.intToBytes(totalSize), 0, data, 1, 4);      //data[1,2,3,4]为数据长度
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



    /**
     * 向手环发送真正的文件数据，
     * 由于一次只能发送1024+个字节，这里会分成多个序号进行发送
     */
    public void cmdSendPacket() {
        if (mFileSend == null) {
            return;
        }
         Log.i(TAG, "cmdSendPacket.. 开始发送数据，数据长度: " + totalSize);
//          Log.i(TAG, "cmdSendPacket.. 开始发送数据，数据内容: " + DataTransferUtils.getHexString(mFileSend));

        readNextBigPocket();
    }
    boolean mPocketIndexAFlag=false;
    /**
     * 发送文件到手环，
     * 拆分成包序[2] + 内容[1024] 进行发送
     *
     * @return 是否仍有文件未发送完成
     */
    private boolean readNextBigPocket() {
        try {
            if (mPocketIndex * 1024 < totalSize) {
                if(mFileSend.length>0){
                     Log.i(TAG,"next pkg");
                    //已读取的小于文件中长度
                    byte[] bufferSend = new byte[Math.min(1024, totalSize - mPocketIndex * 1024)];   //需要发送的文件内容
                    System.arraycopy(mFileSend, mPocketIndex * 1024, bufferSend, 0, bufferSend.length);
                    byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

                    byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                    System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndex + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                    System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                    sendPocketToBle(addHeader(0x32, bufferNext));
                    mPocketIndex++;
                    return true;
                }else {
                    if(mPocketIndexA * 1024<sizeA){
                        //已读取的小于文件中长度
                        int minLength=Math.min(1024, sizeA - mPocketIndexA * 1024);
                        byte[] bufferSend = new byte[minLength];   //需要发送的文件内容
                        System.arraycopy(mFileSendA, mPocketIndexA * 1024, bufferSend, 0, bufferSend.length);
                        byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

                        byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                        System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndexA + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                        System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                        sendPocketToBle(addHeader(0x32, bufferNext));
                        mPocketIndexA++;
                        mPocketIndex= (short) (mPocketIndexA+mPocketIndexB);
                        return true;
                    }else {
                        //已读取的小于文件中长度
                        byte[] bufferSend = new byte[Math.min(1024, sizeB- (mPocketIndexB) * 1024)];   //需要发送的文件内容
                        System.arraycopy(mFileSendB, (mPocketIndexB) * 1024, bufferSend, 0, bufferSend.length);
//                         Log.i(TAG,ByteUtil.byteArrayToString(bufferSend));
                        byte[] compressBuffer = compress(bufferSend);   //将需要发送的文件内容压缩

                        byte[] bufferNext = new byte[compressBuffer.length + 2];    //在发送的文件前面加一个包序
                        System.arraycopy(DataTransferUtils.shortToBytes((short) (mPocketIndexA+mPocketIndexB + 1)), 0, bufferNext, 0, 2);//填充index,包序从1开始
                        System.arraycopy(compressBuffer, 0, bufferNext, 2, compressBuffer.length);  //填充内容

                        sendPocketToBle(addHeader(0x32, bufferNext));
                        mPocketIndexB++;
                        mPocketIndex= (short) (mPocketIndexA+mPocketIndexB);
                        return true;
                    }
                }
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
        resetPackageLength();
//         Log.i(TAG, "sendPocketToBle: mPackageLength: " + mPackageLength + ", bigPocket=" + DataTransferUtils.getHexString(bigPocket));
        int index=0;
        while (index * mPackageLength < bigPocket.length) {
            SerialSocket.getInstance().write(Arrays.copyOfRange(bigPocket, index * mPackageLength, index * mPackageLength + Math.min(mPackageLength, bigPocket.length- index * mPackageLength)));
            index++;
        }
    }

    private void resetPackageLength() {
        mPackageLength = PACKAGE_LENGTH;
         Log.i(TAG, "resetPackageLength.. mPackageLength: " + mPackageLength);
    }

    /**
     * 发送完毕指令，告诉手环一个文件发送完毕
     */
    private void cmdCheck() {
        SerialSocket.getInstance().write(addHeader(0x33, null));
    }

    /**
     * 资源回收
     */
    public void endAndRelease() {

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
