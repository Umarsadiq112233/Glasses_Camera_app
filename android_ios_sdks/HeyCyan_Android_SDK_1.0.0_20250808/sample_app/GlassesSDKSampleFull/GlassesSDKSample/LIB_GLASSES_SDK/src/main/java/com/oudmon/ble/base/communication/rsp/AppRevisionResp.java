package com.oudmon.ble.base.communication.rsp;

import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;

public class AppRevisionResp extends BaseRspCmd {

    private int dataType;
    private int result;

    @Override
    public boolean acceptData(byte[] data) {
         Log.i(TAG, ByteUtil.byteArrayToString(data));
        dataType = data[0];
        result= data[9];
        return false;
    }

    public int getDataType() {
        return dataType;
    }

    public int getResult() {
        return result;
    }
}
