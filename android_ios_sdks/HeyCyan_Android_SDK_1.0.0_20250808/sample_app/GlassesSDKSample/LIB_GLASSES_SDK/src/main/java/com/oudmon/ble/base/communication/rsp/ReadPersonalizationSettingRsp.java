package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr202 on 2017/12/21
 */

public class ReadPersonalizationSettingRsp extends BaseRspCmd {

    private int mClockSetting;

    private int mPowerOnSetting;

    private int mPowerOffSetting;

    @Override
    public boolean acceptData(byte[] data) {

        mClockSetting = data[0];
        mPowerOnSetting = data[1];
        mPowerOffSetting = data[2];

        return false;
    }


    public int getClockSetting() {
        return mClockSetting;
    }

    public int getPowerOnSetting() {
        return mPowerOnSetting;
    }

    public int getPowerOffSetting() {
        return mPowerOffSetting;
    }

    @Override
    public String toString() {
        return "ReadPersonalizationSettingRsp{" +
                "status=" + status +
                ", mClockSetting=" + mClockSetting +
                ", mPowerOnSetting=" + mPowerOnSetting +
                ", mPowerOffSetting=" + mPowerOffSetting +
                '}';
    }
}
