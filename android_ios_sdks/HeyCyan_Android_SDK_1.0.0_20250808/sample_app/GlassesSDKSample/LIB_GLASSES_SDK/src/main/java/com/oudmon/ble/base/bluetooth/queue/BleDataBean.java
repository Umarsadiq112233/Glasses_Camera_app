package com.oudmon.ble.base.bluetooth.queue;

/**
 * @author gs
 * @CreateDate: /6/23 15:57
 * <p>
 * "佛主保佑,
 * 永无bug"
 */
public class BleDataBean {
    private byte [] data;
    private int subLength;
    private int sleepTime;

    public BleDataBean(byte[] data, int subLength) {
        this.data = data;
        this.subLength = subLength;
    }

    public BleDataBean(byte[] data, int subLength, int sleepTime) {
        this.data = data;
        this.subLength = subLength;
        this.sleepTime = sleepTime;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSubLength() {
        return subLength;
    }

    public void setSubLength(int subLength) {
        this.subLength = subLength;
    }
}
