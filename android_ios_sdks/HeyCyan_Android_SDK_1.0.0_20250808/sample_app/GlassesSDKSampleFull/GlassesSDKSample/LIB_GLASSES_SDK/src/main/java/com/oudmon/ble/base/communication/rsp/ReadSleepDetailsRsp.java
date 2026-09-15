package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.entity.BleSleepDetails;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

import java.util.ArrayList;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadSleepDetailsRsp extends BaseRspCmd {

    private ArrayList<BleSleepDetails> bleSleepDetailses = new ArrayList<>();
    private int index = 0;

    @Override
    public boolean acceptData(byte[] data) {
//        Log.i(TAG, "acceptData: data=" + DataTransferUtils.getHexString(data));
        byte flag = data[0];
        if (index == 0 && (flag & 0xff) == 0xff) {
            bleSleepDetailses.clear();
            return false;
        }
        if (index == 0 && (flag & 0xff) == 0xF0) {//报告数据大小
            Log.i(TAG, "acceptData: init data list");
            bleSleepDetailses.clear();
            index++;
        } else {//数据
            BleSleepDetails sleepDetail = new BleSleepDetails();
            sleepDetail.setYear(BLEDataFormatUtils.BCDToDecimal(data[1 - 1]) + 2000);
            sleepDetail.setMonth(BLEDataFormatUtils.BCDToDecimal(data[2 - 1]));
            sleepDetail.setDay(BLEDataFormatUtils.BCDToDecimal(data[3 - 1]));
            sleepDetail.setTimeIndex(data[4 - 1]);
            int[] sleepQualities = new int[8];
            for (int i = 1; i < sleepQualities.length; i++) {
                sleepQualities[i] = data[6 - 1 + i] & 0xFF;
            }
            sleepDetail.setSleepQualities(sleepQualities);
            bleSleepDetailses.add(sleepDetail);
            index++;
            if (data[5 - 1] == data[6 - 1] - 1) {//最后一条
                return false;
            }
        }
        return true;
    }

    public ArrayList<BleSleepDetails> getBleSleepDetailses() {
        return bleSleepDetailses;
    }

    @Override
    public String toString() {
        return "ReadSleepDetailsRsp{" +
                "status=" + status +
                ", bleSleepDetailses=" + bleSleepDetailses +
                ", index=" + index +
                '}';
    }
}
