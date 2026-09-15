package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/5/14
 * 手环时长显示
 */

public class DisplayTimeRsp extends MixtureRsp {

    private int mDisplayTime;

    private int mDisplayType;

    private int mAlpha;

    private boolean mIsCustom;

    private int total;

    private int type;
    private int min;
    private int max;
    private int step;
    private int alwaysOn;
    private int supportAlwaysOn;

    @Override
    protected void readSubData(byte[] subData) {
        mDisplayTime = subData[1];
        mDisplayType = subData[2];
        mAlpha = subData[3];
        mIsCustom = subData[4] != 0;
        total=subData[5];
        type=subData[6];
        min=subData[7];
        max=subData[8];
        step=subData[9];
        alwaysOn=subData[10];
        supportAlwaysOn=subData[11];
    }

    public int getDisplayTime() {
        return mDisplayTime;
    }

    public int getDisplayType() {
        return mDisplayType;
    }

    public int getAlpha() {
        return mAlpha;
    }

    public boolean isCustom() {
        return mIsCustom;
    }

    public int getTotal() {
        return total;
    }

    public int getType() {
        return type;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public int getAlwaysOn() {
        return alwaysOn;
    }

    public int getSupportAlwaysOn() {
        return supportAlwaysOn;
    }
}
