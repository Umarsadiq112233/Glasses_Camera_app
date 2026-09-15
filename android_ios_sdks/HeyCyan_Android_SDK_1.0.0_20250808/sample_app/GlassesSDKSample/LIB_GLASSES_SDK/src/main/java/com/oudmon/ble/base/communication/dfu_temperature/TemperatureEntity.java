package com.oudmon.ble.base.communication.dfu_temperature;

import java.util.Arrays;

/**
 * Created by Jxr35 on swatch_device_text4/27
 */
public class TemperatureEntity {

    public int mIndex;

    public int mTimeSpan;

    public float[] mValues;

    public void clear() {
        mIndex = 0;
        mTimeSpan = 0;
        mValues = null;
    }


    @Override
    public String toString() {
        return "TemperatureEntity{" +
                "mIndex=" + mIndex +
                ", mTimeSpan=" + mTimeSpan +
                ", mValues=" + Arrays.toString(mValues) +
                '}';
    }
}
