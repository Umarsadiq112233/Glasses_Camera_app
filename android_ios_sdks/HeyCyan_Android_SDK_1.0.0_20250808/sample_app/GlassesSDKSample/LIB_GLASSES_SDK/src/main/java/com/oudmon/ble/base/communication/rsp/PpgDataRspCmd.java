package com.oudmon.ble.base.communication.rsp;

import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/10/19
 */
public class PpgDataRspCmd extends BaseRspCmd {

    public int mRate;

    public int mPpgValue;

    @Override
    public boolean acceptData(byte[] data) {
        mRate = data[0];
        mPpgValue = DataTransferUtils.bytesToInt(data, 1);
        //Log.i(TAG, "==========PpgDataRspCmd.. mRate: " + mRate + ", mPpgValue: " + mPpgValue);
        return false;
    }


}
