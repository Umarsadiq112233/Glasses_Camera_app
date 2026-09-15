package com.oudmon.ble.base.communication.rsp;


import com.oudmon.ble.base.communication.entity.AlarmEntity;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadAlarmRsp extends BaseRspCmd {

    private AlarmEntity alarmEntity;

    @Override
    public boolean acceptData(byte[] data) {
        byte weekMask = 0;
        for (int i = 0; i < 7; i++) {
            weekMask = (byte) (weekMask | (data[4 + i] << i));
        }
        alarmEntity = new AlarmEntity(data[0], data[1], BLEDataFormatUtils.BCDToDecimal(data[2]), BLEDataFormatUtils.BCDToDecimal(data[3]), weekMask);
        return false;
    }

    public AlarmEntity getAlarmEntity() {
        return alarmEntity;
    }
}
