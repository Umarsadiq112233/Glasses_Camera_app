package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/5/10
 */

public class MusicSwitchRsp extends BaseRspCmd {

    private int action;
    private boolean enable;

    @Override
    public boolean acceptData(byte[] data) {
        action = data[0];   //读还是写
        enable = data[1] == 0x01;
        return false;
    }

    public boolean isEnable() {
        return enable;
    }

    public int getAction() {
        return action;
    }

}
