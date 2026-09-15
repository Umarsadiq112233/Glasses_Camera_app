package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/6/25
 * 健康目标设置
 */

public class TargetSettingRsp extends MixtureRsp {

    private int mStep, mCalorie, mDistance,mSport,mSleep;

    @Override
    protected void readSubData(byte[] subData) {
//         Log.i(TAG,ByteUtil.byteArrayToString(subData));
        mStep = ByteUtil.bytesToInt(Arrays.copyOfRange(subData, 1, 4));
        mCalorie = ByteUtil.bytesToInt(Arrays.copyOfRange(subData, 4, 7));
        mDistance =  ByteUtil.bytesToInt(Arrays.copyOfRange(subData, 7, 10));
        mSport =  ByteUtil.bytesToInt(Arrays.copyOfRange(subData, 10, 12));
        mSleep =  ByteUtil.bytesToInt(Arrays.copyOfRange(subData, 12, 14));
    }

    public int getStep() {
        return mStep;
    }

    public int getCalorie() {
        return mCalorie;
    }

    public int getDistance() {
        return mDistance;
    }

    public int getmSport() {
        return mSport;
    }

    public int getmSleep() {
        return mSleep;
    }
}
