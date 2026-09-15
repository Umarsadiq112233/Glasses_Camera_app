package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/10/19
 */
public class HealthEcgRsp extends BaseRspCmd {

    public int mStatus;

    public int mEcgInterval;

    public int mPpgInterval;

    @Override
    public boolean acceptData(byte[] data) {
        if (data.length >= 3) {
            mStatus = data[0];
            mEcgInterval = data[1];
            mPpgInterval = data[2];
        }
        return false;
    }

}
