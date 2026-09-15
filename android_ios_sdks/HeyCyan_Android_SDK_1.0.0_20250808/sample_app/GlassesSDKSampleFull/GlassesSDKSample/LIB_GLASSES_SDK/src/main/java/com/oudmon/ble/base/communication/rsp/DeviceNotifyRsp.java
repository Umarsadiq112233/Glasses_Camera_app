package com.oudmon.ble.base.communication.rsp;

public class DeviceNotifyRsp extends BaseRspCmd {

    private int dataType;
    private byte [] loadData;

    @Override
    public boolean acceptData(byte[] data) {
        dataType = data[0];
        loadData = data;
        return false;
    }

    public int getDataType() {
        return dataType;
    }

    public byte[] getLoadData() {
        return loadData;
    }
}
