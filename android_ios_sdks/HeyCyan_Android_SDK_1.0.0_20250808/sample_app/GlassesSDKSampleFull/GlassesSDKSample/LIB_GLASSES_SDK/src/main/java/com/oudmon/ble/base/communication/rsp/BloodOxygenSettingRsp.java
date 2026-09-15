package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BloodOxygenSettingRsp extends MixtureRsp {

    private boolean isEnable;

    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
    }

    public boolean isEnable() {
        return isEnable;
    }
}
