package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.entity.AlarmEntity;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class SetDrinkAlarmReq extends BaseReqCmd {
    /**
     * @param alarmIndex AA 闹钟编号（取值为0 to 4）,可设置5个闹钟。
     * @param enable BB 闹钟使能设置，0为Disable; 1为Enable睡觉闹钟，2为Enable其它闹钟。
     * @param hour CC 为闹钟时间的小时部分（24小时制），BCD码格式（如23点CC = 0x23）。
     * @param minute DD 为闹钟时间的分钟部分，BCD码格式（如59分CC = 0x59）。
     * @param weekMask byte bit0 星期天的使能信号,bit1 星期一
     */
    private byte[] data;

    public SetDrinkAlarmReq(AlarmEntity alarmEntity) {
        super(Constants.CMD_SET_DRINK_TIME);
        if (alarmEntity.getAlarmIndex() > 7) throw new IllegalArgumentException("闹钟索引只能0 到 7");
        if (alarmEntity.getEnable() > 2) throw new IllegalArgumentException("闹钟使能设置只能0 到 2");
        byte weekMask = alarmEntity.getWeekMask();
        data = new byte[] {(byte) alarmEntity.getAlarmIndex(), (byte) alarmEntity.getEnable(), BLEDataFormatUtils.decimalToBCD(alarmEntity.getHour()), BLEDataFormatUtils.decimalToBCD(alarmEntity.getMinute()),
                (byte) (weekMask >> 0 & (0x01)),
                (byte) (weekMask >> 1 & (0x01)),
                (byte) (weekMask >> 2 & (0x01)),
                (byte) (weekMask >> 3 & (0x01)),
                (byte) (weekMask >> 4 & (0x01)),
                (byte) (weekMask >> 5 & (0x01)),
                (byte) (weekMask >> 6 & (0x01)),
        };
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }
}
