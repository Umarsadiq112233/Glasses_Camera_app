package com.oudmon.ble.base.communication.bigData.bean;

public class ClassicBluetooth extends BaseBean{
    private String  deviceMac;
    private String deviceName;

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
