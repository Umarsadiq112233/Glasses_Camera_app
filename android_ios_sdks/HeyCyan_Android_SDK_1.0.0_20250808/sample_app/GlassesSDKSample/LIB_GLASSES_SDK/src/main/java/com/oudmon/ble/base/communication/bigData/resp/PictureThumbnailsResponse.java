package com.oudmon.ble.base.communication.bigData.resp;

public class PictureThumbnailsResponse extends BaseResponse{
    private byte[] subData;
    @Override
    public boolean acceptData(byte[] data) {
        subData=data;
        return false;
    }

    public byte[] getSubData() {
        return subData;
    }
}
