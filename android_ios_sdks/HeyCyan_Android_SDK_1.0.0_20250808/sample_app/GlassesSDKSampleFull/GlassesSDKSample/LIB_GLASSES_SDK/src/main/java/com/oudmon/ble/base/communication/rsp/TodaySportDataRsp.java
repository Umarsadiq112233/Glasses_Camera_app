package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.entity.BleStepTotal;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

import java.util.Calendar;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.25 获取当前运动信息 (实时运动信息)
 */

public class TodaySportDataRsp extends BaseRspCmd {
    private BleStepTotal sportTotal;

    @Override
    public boolean acceptData(byte[] data) {
        sportTotal = new BleStepTotal();
        sportTotal.setTotalSteps(BLEDataFormatUtils.bytes2Int(new byte[] {data[1 - 1], data[2 - 1], data[3 - 1]}));
        sportTotal.setRunningSteps(BLEDataFormatUtils.bytes2Int(new byte[] {data[4 - 1], data[5 - 1], data[6 - 1]}));
        sportTotal.setCalorie(BLEDataFormatUtils.bytes2Int(new byte[] {data[7 - 1], data[8 - 1], data[9 - 1]}));
        sportTotal.setWalkDistance(BLEDataFormatUtils.bytes2Int(new byte[] {data[10 - 1], data[11 - 1], data[12 - 1]}));
        sportTotal.setSportDuration(BLEDataFormatUtils.bytes2Int(new byte[] {data[13 - 1], data[14 - 1]}) * 60);

        Calendar calendar = Calendar.getInstance();
        sportTotal.setDaysAgo(0);
        sportTotal.setYear(calendar.get(Calendar.YEAR));
        sportTotal.setMonth(calendar.get(Calendar.MONTH) + 1);
        sportTotal.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        return false;
    }

    public BleStepTotal getSportTotal() {
        return sportTotal;
    }
}
