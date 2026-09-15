package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class IntellRsp extends MixtureRsp {

    private boolean isEnable;
    private byte delaySecond;

    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
        delaySecond = subData[2];
    }

    public boolean isEnable() {
        return isEnable;
    }

    public byte getDelaySecond() {
        return delaySecond;
    }
}
