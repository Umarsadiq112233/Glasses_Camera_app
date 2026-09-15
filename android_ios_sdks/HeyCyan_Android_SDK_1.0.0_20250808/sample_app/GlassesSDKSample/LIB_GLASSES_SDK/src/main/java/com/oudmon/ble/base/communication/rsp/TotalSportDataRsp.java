package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.entity.BleStepTotal;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;


/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.7 读取某天总运动信息
 */

public class TotalSportDataRsp extends BaseRspCmd {
    private int pocketCount = 2;
    private int curIndex = 0;

    private BleStepTotal bleStepTotal;

    @Override
    public boolean acceptData(byte[] data) {
        int pocketIndex = data[0];
        if (pocketIndex != curIndex || pocketIndex >= pocketCount) {
            Log.e(TAG, "acceptData: index 错误 need=" + curIndex + " received=" + pocketIndex);
            bleStepTotal = null;
            return false;
        }
        if (data[2] == 0 && data[3] == 0 && data[4] == 0) {
            Log.d(TAG, "没有存储数据");
            bleStepTotal = null;
            return false;
        }
        if (curIndex == 0) {
            bleStepTotal = new BleStepTotal();
            bleStepTotal.setDaysAgo(BLEDataFormatUtils.BCDToDecimal(data[1]));
            bleStepTotal.setYear(BLEDataFormatUtils.BCDToDecimal(data[2]) + 2000);
            bleStepTotal.setMonth(BLEDataFormatUtils.BCDToDecimal(data[3]));
            bleStepTotal.setDay(BLEDataFormatUtils.BCDToDecimal(data[4]));
            bleStepTotal.setTotalSteps(BLEDataFormatUtils.bytes2Int(new byte[] {data[5], data[6], data[7]}));
            bleStepTotal.setRunningSteps(BLEDataFormatUtils.bytes2Int(new byte[] {data[8], data[9], data[10]}));
            bleStepTotal.setCalorie(BLEDataFormatUtils.bytes2Int(new byte[] {data[11], data[12], data[13]})); // 得到单位为卡的数值
        } else if (curIndex == 1) {
            int daysAgo = BLEDataFormatUtils.BCDToDecimal(data[1]);
            int year = BLEDataFormatUtils.BCDToDecimal(data[2]) + 2000;
            int month = BLEDataFormatUtils.BCDToDecimal(data[3]);
            int day = BLEDataFormatUtils.BCDToDecimal(data[4]);
            if ((bleStepTotal != null) && (bleStepTotal.getDaysAgo() == daysAgo) && (bleStepTotal.getYear() == year)
                    && (bleStepTotal.getMonth() == month) && (bleStepTotal.getDay() == day)) {
                bleStepTotal.setWalkDistance(BLEDataFormatUtils.bytes2Int(new byte[] {data[5], data[6], data[7]}));
                bleStepTotal.setSportDuration(BLEDataFormatUtils.bytes2Int(new byte[] {data[8], data[9]}) * 60); // 单位为秒
                bleStepTotal.setSleepDuration(BLEDataFormatUtils.bytes2Int(new byte[] {data[10], data[11]}) * 60); // 单位为秒
            }
        }
        curIndex++;
        return curIndex != pocketCount;
    }

    public BleStepTotal getBleStepTotal() {
        return bleStepTotal;
    }
}
