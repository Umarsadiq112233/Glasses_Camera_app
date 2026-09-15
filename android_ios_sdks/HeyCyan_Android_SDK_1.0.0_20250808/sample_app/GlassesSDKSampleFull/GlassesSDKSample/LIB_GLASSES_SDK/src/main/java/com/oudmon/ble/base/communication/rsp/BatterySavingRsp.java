package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/29
 */

public class BatterySavingRsp extends BaseRspCmd {

    private boolean open;

    @Override
    public boolean acceptData(byte[] data) {
        open=data[1]==1;
        return false;
    }

    public boolean isOpen() {
        return open;
    }
}
