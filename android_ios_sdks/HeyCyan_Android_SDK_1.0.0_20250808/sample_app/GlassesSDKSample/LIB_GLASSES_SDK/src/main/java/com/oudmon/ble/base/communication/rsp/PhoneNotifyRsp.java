package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class PhoneNotifyRsp extends BaseRspCmd {

    private int action = 0;

    @Override
    public boolean acceptData(byte[] data) {
        action = data[0] & 0xff;
        return false;
    }

    public int getAction() {
        return action;
    }

    public boolean isReject() {
        return action == 0x01;
    }
}
