package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class DisplayOrientationRsp extends MixtureRsp {

    private boolean isPortrait;
    private boolean isLeft;

    @Override
    protected void readSubData(byte[] subData) {
        isPortrait = subData[1] == 0x01;
        isLeft = subData[2] == 0x01;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public void setLeft(boolean left) {
        isLeft = left;
    }
}
