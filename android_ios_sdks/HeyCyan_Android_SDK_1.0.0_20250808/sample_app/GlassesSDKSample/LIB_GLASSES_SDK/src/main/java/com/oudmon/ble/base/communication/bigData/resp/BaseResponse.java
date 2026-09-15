package com.oudmon.ble.base.communication.bigData.resp;

public abstract class BaseResponse {
    public static final String TAG="GLASSES_LOG";
    protected int cmdType;
    public abstract boolean acceptData(byte[] data);

    public int getCmdType() {
        return cmdType;
    }
    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }


}
