package com.oudmon.ble.base.communication.sport;
import android.util.Log;
import android.util.SparseIntArray;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.OnGattEventCallback;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;
import com.oudmon.qc_utils.date.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jxr35 on 2017/11/28
 */

public class SportPlusHandle {
    private static final String TAG = "Jxr35";
    private IOpResult iOpResult;
    private int mSportIndex = 0;

    private List<SportPlusEntity> mSportEntities = new ArrayList<>();
    private List<SportLocation> mLocations = new ArrayList<>();

    /**
     * 待接收的数据总数，这里面不包含各种指令及CRC
     */
    private int mTotalCount = 0;
    /**
     * 已接收的数据总数。
     */
    private int mReceivedCount = 0;
    /**
     * 是否正在接收概要
     */
    private boolean mSummaryReceiving = false;
    /**
     * 是否正在接收数据详情，比如经纬度、心率、速度等
     */
    private boolean mDetailsReceiving = false;
    /**
     * 概要数据
     */
    private byte[] mSummary;
    /**
     * 数据详情
     */
    private byte[] mDetails;

    /**
     * 总包长度，从1开始
     */
    private int mPackageCount = 0;
    /**
     * 当前的包序
     */
    private int mPackageIndex = 0;
    /**
     * 抽样间隔时间，多少秒抽取一个数据点
     */
    private int mSampleSecond = 0;
    /**
     * 一组数据的总字节长度
     */
    private int mDataLength = 0;
    /**
     * key -> 数据项数据，value -> 数据项长度
     */
    private SparseIntArray mDataTypeArray = new SparseIntArray();

    public OnGattEventCallback callback=new OnGattEventCallback(){
        @Override
        public void onReceivedData(String uuid, byte[] data){
            if (data != null) {
                try {
                    if ((data[0] & 0xff) == 0xbc && data[1] == 0x42) {
                        mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                        mReceivedCount = data.length - 6;
                        mSummary = new byte[mTotalCount];
                        System.arraycopy(data, 6, mSummary, 0, mReceivedCount);
                        mSummaryReceiving = mReceivedCount < mTotalCount;
//                         Log.i(TAG,"onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mSummaryReceiving: " + mSummaryReceiving);
                        if (!mSummaryReceiving) {
                             Log.i(TAG,"onReceiver All Summary data: " + DataTransferUtils.getHexString(mSummary));
                            parseSummary(mSummary);
                            iOpResult.onSummary(1,mSportEntities);
                            executeRequest();
                        }
                    } else if (mSummaryReceiving) {
                        System.arraycopy(data, 0, mSummary, mReceivedCount, data.length);
                        mReceivedCount += data.length;
                        mSummaryReceiving = mReceivedCount < mTotalCount;
                         Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mSummaryReceiving: " + mSummaryReceiving);
                        if (!mSummaryReceiving) {
                             Log.i(TAG,"onReceiver All Summary data: " + DataTransferUtils.getHexString(mSummary));
                            parseSummary(mSummary);
                            iOpResult.onSummary(1,mSportEntities);
                            executeRequest();
                        }
                    } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x44) {
                        int mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                        int mReceivedCount = data.length - 6;
                        byte[] request = new byte[mTotalCount];
                        System.arraycopy(data, 6, request, 0, mReceivedCount);
                        parseRequest(request);
                    } else if ((data[0] & 0xff) == 0xbc && data[1] == 0x45) {
                        mTotalCount = DataTransferUtils.bytesToShort(data, 2);
                        mReceivedCount = data.length - 6;
                        mDetails = new byte[mTotalCount];
                        System.arraycopy(data, 6, mDetails, 0, mReceivedCount);
                        mDetailsReceiving = mReceivedCount < mTotalCount;
                         Log.i(TAG, "onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mDetailsReceiving: " + mDetailsReceiving);
                        if (!mDetailsReceiving) {
//                             Log.i(TAG, "onReceiver All Details data: " + DataTransferUtils.getHexString(mDetails));
                            parseDetails(mDetails);
                        }
                    } else if (mDetailsReceiving) {
                        System.arraycopy(data, 0, mDetails, mReceivedCount, data.length);
                        mReceivedCount += data.length;
                        mDetailsReceiving = mReceivedCount < mTotalCount;
                         Log.i(TAG,"onReceivedData.. mTotalCount: " + mTotalCount + ", mReceivedCount: " + mReceivedCount + ", mDetailsReceiving: " + mDetailsReceiving);
                        if (!mDetailsReceiving) {
//                             Log.i(TAG,"onReceiver All Details data: " + DataTransferUtils.getHexString(mDetails));
                            parseDetails(mDetails);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    /**
     * Summary数据格式：                 <br/>
     * 运动类型数量[1] + ( 字段长度[1] + 运动类型索引[1] + ( 数据项长度[1] + 数据项类型[1] + 数据项数据[n] ) * n ) * n <br/>
     *  运动类型数量[1]：本条报文中包含的所有运动类型的数量                    <br/>
     *  字段长度[1]：每个字段中所有内容的字节长度，包括字段长度、索引和数据    <br/>
     *  运动类型索引[1]：上面表格中的索引号                                    <br/>
     *  数据项长度[1]：该数据项总长度，包括表示长度和类型的两个字节            <br/>
     *  数据项类型[1]：表明本数据项的格式                                       <br/>
     *  数据项数据[n]：数据内容                                                 <br/>
     *
     * @param data summary
     */
    private void parseSummary(byte[] data) {
         Log.i(TAG, "===========================解析Summary开始============================");
        mSportIndex = 0;
        mSportEntities.clear();
        try {
            int count = data[0];
            int start = 1;
            while (count > 0) {
                int length = data[start];   //一条运动数据占用字符长度
                byte[] tempArray = new byte[length];
                System.arraycopy(data, start, tempArray, 0, length);
                SportPlusEntity entity = new SportPlusEntity();
                entity.mSportType = ByteUtil.byteToInt(tempArray[1]);   //运动类型索引
                 Log.i(TAG, ByteUtil.byteArrayToString(tempArray));
                 Log.i(TAG, "tempArray: " + DataTransferUtils.getHexString(tempArray) + ", sportType: " + entity.mSportType);
//                Log.i(TAG, "tempLength: " + length + ", start: " + start + ", count: " + count);

                int flag = 0;
                int sumLength = 2;
                while (sumLength < length) {
                    int dataLength = tempArray[flag + 2];    //数据项长度
                    int dataType = tempArray[flag + 3];        //运动类型索引
                    byte[] dataValue = new byte[dataLength - 2];
                    System.arraycopy(tempArray, flag + 4, dataValue, 0, dataLength - 2);
//                    Log.i(TAG, "flag: " + flag + ", dataLength: " + dataLength + ", dataType: " + dataType + ", dataValue: " + DataTransferUtils.getHexString(dataValue) + ", dataInt: " + DataTransferUtils.arrays2Int(dataValue));
                    setKeyValues(entity, dataType, dataValue);
                    flag += dataLength;
                    sumLength += dataLength;
                }
//                Log.i(TAG, "SportPlusEntity: " + entity);
                mSportEntities.add(entity);
                start += length;
                count--;
            }
            Log.i(TAG, "===========================解析Summary结束============================");
        } catch (Exception e) {
            Log.i(TAG, "===========================解析Summary异常============================");
            e.printStackTrace();
        }
    }

    /**
     * 根据不同的键值，设置运动+的各项属性及属性值
     *
     * @param entity 运动数据
     * @param key    数据项类型
     * @param values 数据项数据
     */
    private void setKeyValues(SportPlusEntity entity, int key, byte[] values) {
        if (key == SportPlusData.start_time) {
            entity.mStartTime = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.duration) {
            entity.mDuration = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.distance) {
            entity.mDistance = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.calories) {
            entity.mCalories = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.speed_avg) {
            entity.mSpeedAvg = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.speed_max) {
            entity.mSpeedMax = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.rate_avg) {
            entity.mRateAvg = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.rate_min) {
            entity.mRateMin = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.rate_max) {
            entity.mRateMax = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.elevation) {
            entity.mElevation = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.uphill) {
            entity.mUphill = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.downhill) {
            entity.mDownhill = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.step_rate) {
            entity.mStepRate = DataTransferUtils.arrays2Int(values);
        } else if (key == SportPlusData.sport_count) {
            entity.mSportCount = DataTransferUtils.arrays2Int(values);
        }else if (key == SportPlusData.steps) {
            entity.steps = DataTransferUtils.arrays2Int(values);
        }
    }

    /**
     * 请求详细数据
     */
    private void executeRequest() {
        Log.i(TAG, "executeRequest.. mSportIndex: " + mSportIndex + ", totalSize: " + mSportEntities.size());
        if (mSportIndex < mSportEntities.size()) {
            SportPlusEntity entity = mSportEntities.get(mSportIndex);
             Log.i(TAG,new DateUtil(entity.mStartTime,true).getY_M_D_H_M_S()+"----"+DataTransferUtils.getHexString(DataTransferUtils.intToBytes(entity.mStartTime)));
            cmdRequest(entity.mSportType, entity.mStartTime);
//            Logger.e("---------------------executeRequest");
        } else {
             Log.i(TAG, "获取所有详细数据结束 mSportEntities: " + mSportEntities);
            iOpResult.onSummary(2,mSportEntities);
            Log.i(TAG, "==================================onDetails cost time: " + (System.currentTimeMillis() - mTime));
        }
    }

    /**
     * 结果[1] + 数据分包总数[2] + 抽样值[1] + (数据项长度[1] + 数据项格式[1]) * n <p/>
     * <p>
     * 结果[1]：非0即为错误，手环不会上传详细数据              <p/>
     * 数据分包总数[2]：手环将会上传多少长包的详细数据        <p/>
     * 抽样值[1]：数据太多会被抽样，表示几秒钟抽样一个点       <p/>
     * 数据项长度[1]：仅表示数据项长度                        <p/>
     * 数据项类型[1]：表明将要通过CMD_DATA_SEND上传的数据的格式列表   <p/>
     *
     * @param data data 如 { 0x00, 0x02, 0x00, 0x05, 0x01, 0x11 }
     */
    private void parseRequest(byte[] data) {
        mDataLength = 0;
        mLocations.clear();
        if (data[0] == 0) {
            mPackageCount = DataTransferUtils.byte2Int(data, 1);
            mSampleSecond = data[3];
             Log.i(TAG, "parseRequest.. mPackageCount: " + mPackageCount + ", mSampleSecond: " + mSampleSecond);
            if (data.length == 4 || mPackageCount == 0) {
                mDataTypeArray.clear();
                mSportEntities.get(mSportIndex).mLocations.clear();
                mSportIndex++;
                executeRequest();
            } else {
                int index = 4;
                while (index < data.length) {
                    mDataTypeArray.put(data[index + 1], data[index]);
                    mDataLength += data[index];
                    index += 2;
                }
                for (int i = 0; i < mDataTypeArray.size(); i++) {
                    int key = mDataTypeArray.keyAt(i);
                    int length = mDataTypeArray.get(key);
                    Log.i(TAG, "parseRequest.. key: " + key + ", value: " + length);
                }
            }
        } else {
            mSportIndex++;
            executeRequest();
        }
    }

    private void parseDetails(byte[] data) {
        try {
            int packageId = DataTransferUtils.byte2Int(data, 0);
             Log.i(TAG, "parseDetails.. packageId: " + packageId + ", mPackageCount: " + mPackageCount);
            int index = 2;
            while (index < data.length) {
                byte[] temp = new byte[mDataLength];
                System.arraycopy(data, index, temp, 0, mDataLength);
                int flag = 0;
                SportLocation location = new SportLocation();
                for (int i = 0; i < mDataTypeArray.size(); i++) {
                    int key = mDataTypeArray.keyAt(i);
                    int length = mDataTypeArray.get(key);
                    if (key == SportPlusData.longitude) {    // 实时经度 Float
//                        location.mLongitude = DataTransferUtils.bytes2Float(temp, flag);
                    } else if (key == SportPlusData.latitude) {  // 实时维度 Float
//                        location.mLatitude = DataTransferUtils.bytes2Float(temp, flag);
                    } else if (key == SportPlusData.rate_real) {         // 实时心率 UINT 8
                        location.mRateReal = temp[flag] & 0xff;
                    } else if (key == SportPlusData.speed_real) {         // 实时速度 UINT 16
//                        location.mSpeedReal = DataTransferUtils.byte2Int(temp, flag);
                    }
                    flag += length;
                }
//                 Log.i(TAG, "SportLocation: " + location);
//                if (!Float.isNaN(location.mLongitude) && !Float.isNaN(location.mLatitude)) {
//                    mLocations.add(location);
//                }
                mLocations.add(location);
                index += mDataLength;
            }

            if (packageId >= mPackageCount) {
                mDataTypeArray.clear();
                mSportEntities.get(mSportIndex).mLocations.addAll(mLocations);
//                 Log.i(TAG, "==========获取到一条完整的运动+数据.. mSportIndex: " + mSportIndex + ", locationLength: " + mLocations.size() + ", mLocationSize: " + mSportEntities.get(mSportIndex).mLocations.size() + ", Entity: " + mSportEntities.get(mSportIndex));
                mSportIndex++;
                executeRequest();
//               Log.i(TAG,.e("--------------------执行了");
            }
//            Log.i(TAG, "===========================解析Details结束============================");
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "===========================解析Details异常============================");
        }
    }

    private long mTime = 0;

    /**
     * 开始设置监听
     *
     * @param iOpResult 结果
     */
    public void init(IOpResult iOpResult) {
        Log.i(TAG, "init... ");
        mTime = System.currentTimeMillis();
        this.iOpResult = iOpResult;
        mLocations.clear();
        BleOperateManager.getInstance().setCallback(callback);
//        enableNotifyRequest.setEnable(true);
//        DeviceOperateManager.getInstance().execute(enableNotifyRequest);
    }

    public void cmdSummary(int time) {
        byte[] data = new byte[4];
        System.arraycopy(DataTransferUtils.intToBytes(time), 0, data, 0, 4);
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x41, data)));
    }

    public void cmdRequest(int sportType, int time) {
        byte[] data = new byte[5];
        data[0] = (byte) sportType;
        System.arraycopy(DataTransferUtils.intToBytes(time), 0, data, 1, 4);
        BleOperateManager.getInstance().execute(getWriteRequest(addHeader(0x43, data)));
    }


    /**
     * 将要传送的Data转换成一个可以发送的包
     *
     * @param cmdid 指令ID
     * @param data  要发送的数据data
     * @return 转换后的包
     */
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
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(Constants.SERIAL_PORT_SERVICE, Constants.SERIAL_PORT_CHARACTER_WRITE);
        noRspInstance.setValue(data);
        return noRspInstance;
    }


    public interface IOpResult {

        void onSummary(int type ,List<SportPlusEntity> sportPlusEntities);
    }


}
