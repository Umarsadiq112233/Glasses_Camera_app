package com.oudmon.ble.base.communication.bigData.bean;

public class ECardEntity {
    int type;
    String url;
    boolean support;
    int deviceError;
    int readOrWrite;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }

    public int getDeviceError() {
        return deviceError;
    }

    public void setDeviceError(int deviceError) {
        this.deviceError = deviceError;
    }

    public int getReadOrWrite() {
        return readOrWrite;
    }

    public void setReadOrWrite(int readOrWrite) {
        this.readOrWrite = readOrWrite;
    }
}
