package com.oudmon.ble.base.communication.rsp;
import android.util.Log;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.qc_utils.bytes.DataTransferUtils;
import com.oudmon.qc_utils.date.DateUtil;

import java.util.TimeZone;
/**
 * Created by jxr202 on 2017/12/12
 */

public class ReadHeartRateRsp extends BaseRspCmd {

    private int size = 0;
    private int index = 0;
    private int mUtcTime;
    private byte[] mHeartRateArray;
    private boolean endFlag = false;
    private int range=5;


    /**
     * 返回是否还有数据传递
     *
     * @param data 是否还有数据传递
     * @return y/f
     */
    @Override
    public boolean acceptData(byte[] data) {
        //总长度18字节
        try {
            byte flag = data[0];
            if ((flag & 0xff) == 0xff ||(new DateUtil(getmUtcTime(),true).isToday()&&flag ==0x17)) {
                endFlag = true;
                return false;
            }
            if ((flag & 0xff) == 0x00) {
                endFlag = false;
                size = data[1];
                range=data[2];
                mHeartRateArray = new byte[size * 13];
                Log.i(TAG, "0x00.. size: " + size);
            } else if ((flag & 0xff) == 0x01) {
                byte[] utcTime = new byte[4];
                utcTime[0] = data[1];
                utcTime[1] = data[2];
                utcTime[2] = data[3];
                utcTime[3] = data[4];
                mUtcTime = DataTransferUtils.bytesToInt(utcTime, 0);
                int time= (int) (getTimeZone()*3600);
                mUtcTime=mUtcTime-time;
                System.arraycopy(data, 5, mHeartRateArray, 0, data.length - 5);
                index += data.length - 5;
            } else {
                if(getCmdType()==0x15){
//                    Log.i(TAG,mHeartRateArray.length);
                    System.arraycopy(data, 1, mHeartRateArray, index, data.length - 1);
                    index += 13;
                    if (flag == size - 1) {
                        endFlag = true;
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mHeartRateArray = new byte[size * 13];
            Log.i(TAG, "0x00.. size: " + size);
            return false;
        }
    }


    public boolean isEndFlag() {
        return endFlag;
    }

    public byte[] getmHeartRateArray() {
        //一条的数据长度限制在288条，每5分钟一条，12*24=288条
        if (mHeartRateArray != null) {
            byte[] array = new byte[288];
            if (mHeartRateArray.length > 288) {
                System.arraycopy(mHeartRateArray, 0, array, 0, 288);
                return array;
            } else if (mHeartRateArray.length < 288) {
                System.arraycopy(mHeartRateArray, 0, array, 0, mHeartRateArray.length);
                return array;
            }

            DateUtil dateUtil=new DateUtil(mUtcTime,true);
            if(dateUtil.isToday()){
                int min=dateUtil.getTodayMin();
                int size=min/5;
                for (int i = 0; i <mHeartRateArray.length ; i++) {
                    if(i>size){
                        mHeartRateArray[i]=0;
                    }
                }
            }
        }
        return mHeartRateArray==null?new byte[]{}:mHeartRateArray;
    }


    public int getRange() {
        return range;
    }

    /**
     * 这个实际上获取的是本地时间而不是UTC时间
     * @return 本地时间戳
     */
    public int getmUtcTime() {
        return mUtcTime;
    }

    public static float getTimeZone() {
        return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (3600 * 1000f);
    }

}
