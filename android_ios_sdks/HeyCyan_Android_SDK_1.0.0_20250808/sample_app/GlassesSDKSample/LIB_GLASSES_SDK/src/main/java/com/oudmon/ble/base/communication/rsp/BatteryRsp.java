package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BatteryRsp extends BaseRspCmd {

    private int batteryValue;
    private boolean charging;

    @Override
    public boolean acceptData(byte[] data) {
        batteryValue = data[0];
        charging = data[1]==1;
        return false;
    }

    public int getBatteryValue() {
        return batteryValue;
    }

    public boolean isCharging() {
        return charging;
    }
}
