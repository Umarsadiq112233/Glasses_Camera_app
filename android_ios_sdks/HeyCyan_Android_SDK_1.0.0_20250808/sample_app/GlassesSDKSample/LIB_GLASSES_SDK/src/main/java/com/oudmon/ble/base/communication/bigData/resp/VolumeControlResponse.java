package com.oudmon.ble.base.communication.bigData.resp;


public class VolumeControlResponse extends BaseResponse {
    private byte[] subData;
    private int currVolumeType;
    private int minVolumeMusic;
    private int maxVolumeMusic;
    private int currVolumeMusic;
    private int minVolumeSystem;
    private int maxVolumeSystem;
    private int currVolumeSystem;
    private int minVolumeCall;
    private int maxVolumeCall;
    private int currVolumeCall;


    @Override
    public boolean acceptData(byte[] data) {
        subData=data;
        minVolumeMusic=data[8];
        maxVolumeMusic=data[9];
        currVolumeMusic=data[10];

        minVolumeCall=data[12];
        maxVolumeCall=data[13];
        currVolumeCall=data[14];

        minVolumeSystem=data[16];
        maxVolumeSystem=data[17];
        currVolumeSystem=data[18];
        currVolumeType=data[19];
        return false;
    }

    public byte[] getSubData() {
        return subData;
    }

    public int getCurrVolumeType() {
        return currVolumeType;
    }

    public int getMinVolumeMusic() {
        return minVolumeMusic;
    }

    public int getMaxVolumeMusic() {
        return maxVolumeMusic;
    }

    public int getCurrVolumeMusic() {
        return currVolumeMusic;
    }

    public int getMinVolumeSystem() {
        return minVolumeSystem;
    }

    public int getMaxVolumeSystem() {
        return maxVolumeSystem;
    }

    public int getCurrVolumeSystem() {
        return currVolumeSystem;
    }

    public int getMinVolumeCall() {
        return minVolumeCall;
    }

    public int getMaxVolumeCall() {
        return maxVolumeCall;
    }

    public int getCurrVolumeCall() {
        return currVolumeCall;
    }
}
