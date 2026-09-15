package com.oudmon.ble.base.communication.bigData.resp;
public class GlassesDeviceNotifyRsp extends BaseResponse {

    private byte [] loadData;

    @Override
    public boolean acceptData(byte[] data) {
        loadData = data;
        return false;
    }

    public byte[] getLoadData() {
        return loadData;
    }
}
