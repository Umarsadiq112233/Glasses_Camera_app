package com.oudmon.ble.base.communication.bigData.resp;


public class BatteryResponse extends BaseResponse {
    private byte[] subData;
    private int battery;
    private boolean charging;

    @Override
    public boolean acceptData(byte[] data) {
        try {
            subData=data;
            battery= data[6];
            charging= data[7]==1;
        } catch (Exception e) {
           e.printStackTrace();
        }
        return false;
    }

    public byte[] getSubData() {
        return subData;
    }

    public int getBattery() {
        return battery;
    }

    public boolean isCharging() {
        return charging;
    }
}
