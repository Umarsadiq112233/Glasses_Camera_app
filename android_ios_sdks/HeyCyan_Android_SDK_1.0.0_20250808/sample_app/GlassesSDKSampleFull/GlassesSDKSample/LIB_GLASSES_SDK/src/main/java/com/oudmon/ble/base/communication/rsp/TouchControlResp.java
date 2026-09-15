package com.oudmon.ble.base.communication.rsp;

public class TouchControlResp extends BaseRspCmd{
    private int appType;
    private int strength;

    private boolean touch;

    @Override
    public boolean acceptData(byte[] data) {
//     Log.i(TAG, ByteUtil.byteArrayToString(data));
        touch= data[1] ==0;
        appType = data[2];
        strength = data[3];
        return false;
    }

    public int getAppType() {
        return appType;
    }

    public int getStrength() {
        return strength;
    }

    public boolean isTouch() {
        return touch;
    }
}
