package com.oudmon.ble.base.communication.rsp;


public class DialIndexRsp extends BaseRspCmd {
    private int index = 0;


    @Override
    public boolean acceptData(byte[] data) {
        index=data[1];
        return false;
    }


    public int getIndex() {
        return index;
    }
}
