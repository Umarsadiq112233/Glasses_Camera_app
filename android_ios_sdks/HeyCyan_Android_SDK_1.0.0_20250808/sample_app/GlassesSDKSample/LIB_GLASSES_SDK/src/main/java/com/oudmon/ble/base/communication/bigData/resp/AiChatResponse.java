package com.oudmon.ble.base.communication.bigData.resp;

public class AiChatResponse extends BaseResponse {
    private byte[] subData;

    @Override
    public boolean acceptData(byte[] data) {
//        Log.i(TAG,ByteUtil.byteArrayToString(data));
        subData=data;
        return false;
    }

    public byte[] getSubData() {
        return subData;
    }

}
