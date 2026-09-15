package com.oudmon.ble.base.communication.rsp;
import android.util.Log;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.entity.BlePressure;
import com.oudmon.ble.base.communication.utils.DataParseUtils;
import com.oudmon.qc_utils.bytes.DataTransferUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadBlePressureRsp extends BaseRspCmd {

    private int mCount = 0;
    private List<BlePressure> mValueList = new ArrayList<>();
    private Calendar mCalendar = Calendar.getInstance();

    @Override
    public boolean acceptData(byte[] data) {
        //总长度18字节
        Log.i(TAG, "ReadBlePressureRsp -> acceptData -> data: " + DataTransferUtils.getHexString(data));
        byte[] time = Arrays.copyOfRange(data, 0, 4);
        if ("ffffffff".equalsIgnoreCase(DataTransferUtils.getHexString(time))) {
            mCount = 0;
            return false;
        } else {
            mCount++;
            long timeStamp = DataParseUtils.byteArrayToInt(time);
//            long timeOffset = mCalendar.get(Calendar.ZONE_OFFSET) / 1000;
            int timeOffset= (int) (getTimeZone()*3600);
            timeStamp -= timeOffset;    //手环现在返回的是本地时间，而不是正确的UTC时间，导致手环测量的数据比正常数据多8个小时
            Log.i(TAG, "timeStamp: " + timeStamp + ", timeOffset: " + timeOffset);
            int dbp = data[4] & 0xff;
            int sbp = data[5] & 0xff;
            mValueList.add(new BlePressure(timeStamp, sbp, dbp));
        }
        if (mCount >= Constants.BAND_PRESSURE_COUNT) {
            mCount = 0;
            return false;
        }
        return true;
    }

    public List<BlePressure> getValueList() {
        return mValueList;
    }

    public static float getTimeZone() {
        return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (3600 * 1000f);
    }
}
