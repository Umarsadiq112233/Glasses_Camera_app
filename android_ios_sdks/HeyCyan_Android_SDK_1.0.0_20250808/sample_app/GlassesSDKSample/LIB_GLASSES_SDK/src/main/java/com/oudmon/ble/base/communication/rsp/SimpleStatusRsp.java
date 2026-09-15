package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class SimpleStatusRsp extends BaseRspCmd {
    @Override
    public boolean acceptData(byte[] data) {
        return false;
    }
}
