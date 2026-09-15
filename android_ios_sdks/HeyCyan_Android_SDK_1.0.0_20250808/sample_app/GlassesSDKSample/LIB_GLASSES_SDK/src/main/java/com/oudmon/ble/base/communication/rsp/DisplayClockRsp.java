package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class DisplayClockRsp extends MixtureRsp {

    private boolean isClock;

    @Override
    protected void readSubData(byte[] subData) {
        isClock = subData[1] == 0x01;
    }

    public boolean isClock() {
        return isClock;
    }


    public void setClock(boolean clock) {
        isClock = clock;
    }
}
