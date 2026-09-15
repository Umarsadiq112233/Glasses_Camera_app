package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;

public class FindPhoneRsp extends BaseRspCmd{
    private int openOrClose;
    @Override
    public boolean acceptData(byte[] data) {
        openOrClose=data[0];
        Log.i(TAG, ByteUtil.byteArrayToString(data));
        return false;
    }

    public int getSwitchStatue(){
        return openOrClose;
    }
}
