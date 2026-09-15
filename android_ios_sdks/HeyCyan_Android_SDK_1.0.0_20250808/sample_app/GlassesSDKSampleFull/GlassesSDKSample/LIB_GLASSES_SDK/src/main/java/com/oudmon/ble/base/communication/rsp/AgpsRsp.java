package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/5/14
 * 手环时长显示
 */

public class AgpsRsp extends MixtureRsp {

    private boolean mEnable;

    @Override
    protected void readSubData(byte[] subData) {
        mEnable = subData[1] == 0x01;
    }

    public boolean isEnable() {
        return mEnable;
    }

}
