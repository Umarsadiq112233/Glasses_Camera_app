package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;

public class MuslimTargetRsp extends BaseRspCmd {
    private int muslimTarget=0;

    @Override
    public boolean acceptData(byte[] data) {
        Log.i(TAG,ByteUtil.byteArrayToString(data));
         //0100 000000000000000000000000
        //总长度18字节
        try {
            muslimTarget=ByteUtil.bytesToInt(new byte[]{data[2],data[3],data[4],data[5]});
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getMuslimTarget() {
        return muslimTarget;
    }
}
