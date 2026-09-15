package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadSitLongRsp extends BaseRspCmd {

    private StartEndTimeEntity startEndTimeEntity;
    private byte weekMask;
    /**
     * 运动提醒周期，单位为分钟，取值为30、60、90。如90表示90分钟提醒一次
     */
    private int cycle;


    @Override
    public boolean acceptData(byte[] data) {
        startEndTimeEntity = new StartEndTimeEntity(BLEDataFormatUtils.BCDToDecimal(data[0]), BLEDataFormatUtils.BCDToDecimal(data[1]), BLEDataFormatUtils.BCDToDecimal(data[2]), BLEDataFormatUtils.BCDToDecimal(data[3]));
        weekMask = data[4];
        cycle = (int) data[5];
        return false;
    }

    public StartEndTimeEntity getStartEndTimeEntity() {
        return startEndTimeEntity;
    }

    public byte getWeekMask() {
        return weekMask;
    }

    public int getCycle() {
        return cycle;
    }

    /**
     * 周一到周天都关闭了，则为false，有一个打开则为true
     *
     * @return
     */
    public boolean isEnable() {
        return weekMask != 0;
    }

    /**
     * 统一打开或者关闭
     *
     * @param isEnable
     */
    public void setEnableAll(boolean isEnable) {
        weekMask = (byte) (isEnable ? 0x7f : 0x00);
    }

    public ReadSitLongRsp cloneMySelf() {
        ReadSitLongRsp copy = new ReadSitLongRsp();
        copy.weekMask = weekMask;
        copy.cycle = cycle;
        copy.startEndTimeEntity = new StartEndTimeEntity(startEndTimeEntity.getStartHour(), startEndTimeEntity.getStartMinute(), startEndTimeEntity.getEndHour(), startEndTimeEntity.getEndMinute());
        return copy;
    }

    /**
     * 设置星期几的标志
     *
     * @param index    0 星期天，1 星期一
     * @param isEnable true 打开
     */
    public void enableTheWeek(int index, boolean isEnable) {
        weekMask = (byte) ((~(0x01 << index)) & weekMask);//将该位清零
        if (isEnable) weekMask = (byte) ((0x01 << index) | weekMask);//将该位置1
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }
}
