package com.oudmon.ble.base.communication.entity;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public class AlarmEntity {

    private int alarmIndex;
    private int enable;
    private int hour;
    private int minute;
    private byte weekMask;

    /**
     * @param alarmIndex a
     * @param enable     e
     * @param hour       h
     * @param minute     m
     * @param weekMask   w
     */
    public AlarmEntity(int alarmIndex, int enable, int hour, int minute, byte weekMask) {
        this.alarmIndex = alarmIndex;
        this.enable = enable;
        this.hour = hour;
        this.minute = minute;
        this.weekMask = weekMask;
    }

    public int getAlarmIndex() {
        return alarmIndex;
    }

    public int getEnable() {
        return enable;
    }

    public boolean isEnable() {
        return enable != 0;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public byte getWeekMask() {
        return weekMask;
    }

    public void setEnable(boolean enable) {
        this.enable = enable ? 1 : 0;
    }

    public void setWeekMask(byte weekMask) {
        this.weekMask = weekMask;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    /**
     * 设置星期几的标志
     * @param index    0 星期天，1 星期一
     * @param isEnable true 打开
     */
    public void enableTheWeek(int index, boolean isEnable) {
        weekMask = (byte) ((~(0x01 << index)) & weekMask);//将该位清零
        if (isEnable) weekMask = (byte) ((0x01 << index) | weekMask);//将该位置1
    }

    public AlarmEntity cloneMyself() {
        return new AlarmEntity(alarmIndex, enable, hour, minute, weekMask);
    }
}
