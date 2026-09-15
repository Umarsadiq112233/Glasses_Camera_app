package com.oudmon.ble.base.communication.dfu_temperature;

/**
 * Created by Jxr35 on swatch_device_text4/30
 */
public class TemperatureOnceEntity {

    public long mTime;

    public float mValue;

    public void clear() {
        mTime = 0;
        mValue = 0;
    }


    @Override
    public String toString() {
        return "TemperatureOnceEntity{" +
                "mTime=" + mTime +
                ", mValue=" + mValue +
                '}';
    }
}
