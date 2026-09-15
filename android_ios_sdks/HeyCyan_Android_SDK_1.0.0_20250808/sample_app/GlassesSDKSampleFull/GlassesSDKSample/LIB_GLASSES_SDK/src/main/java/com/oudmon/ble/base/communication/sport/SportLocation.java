package com.oudmon.ble.base.communication.sport;

/**
 * Created by Jxr35 on 2018/6/4
 */

public class SportLocation {

//    /**
//     * 实时经度 WGS84坐标格式
//     */
//    public float mLongitude;
//
//    /**
//     * 实时纬度 WGS84坐标格式
//     */
//    public float mLatitude;

    /**
     * 实时心率 次/分
     */
    public int mRateReal;

//    /**
//     * 实时速度 cm/s
//     */
//    public int mSpeedReal;


    @Override
    public String toString() {
        return "SportLocation{" +
//                "mLongitude=" + mLongitude +
//                ", mLatitude=" + mLatitude +
                ", mRateReal=" + mRateReal +
//                ", mSpeedReal=" + mSpeedReal +
                '}';
    }

}
