package com.oudmon.ble.base.bluetooth;

import android.text.TextUtils;

public class DeviceManager {

    private String mDeviceName="";
    private String mDeviceAddress="";
    private String mWifiName="";
    private String mWifiPassword="";
    private static DeviceManager mInstance;

    public static DeviceManager getInstance() {
        if (mInstance == null) {
            synchronized (DeviceManager.class) {
                if (mInstance == null) {
                    mInstance = new DeviceManager();
                }
            }
        }
        return mInstance;
    }

    public void reSet(){
        mInstance=null;
        mDeviceName="";
        mDeviceAddress=null;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String name) {
        if(TextUtils.isEmpty(name)){
            name="";
        }
        mDeviceName = name;
    }

    public void setWifiName(String mWifiName) {
        this.mWifiName = mWifiName;
    }

    public void setWifiPassword(String mWifiPassword) {
        this.mWifiPassword = mWifiPassword;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public void setDeviceAddress(String address) {
        mDeviceAddress = address;
    }

    public String getWifiName() {
        return mWifiName;
    }

    public String getWifiPassword() {
        return mWifiPassword;
    }
}
