package com.oudmon.ble.base.communication.sport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jxr35 on 2018/6/1
 */

public class SportPlusEntity {

    /**
     * 运动类型索引
     */
    public int mSportType;
    /**
     * 起始时间 秒
     */
    public int mStartTime;
    /**
     * 运动时长 秒
     */
    public int mDuration;
    /**
     * 运动里程 米
     */
    public int mDistance;
    /**
     * 卡路里 小卡
     */
    public float mCalories;
    /**
     * 平均速度 cm/s
     */
    public int mSpeedAvg;
    /**
     * 最高速度 cm/s
     */
    public int mSpeedMax;
    /**
     * 平均心率 次/分
     */
    public int mRateAvg;
    /**
     * 最小心率 次/分
     */
    public int mRateMin;
    /**
     * 最大心率 次/分
     */
    public int mRateMax;
    /**
     * 平均海拔 cm
     */
    public int mElevation;
    /**
     * 累计爬坡 cm
     */
    public int mUphill;
    /**
     * 累计下坡 cm
     */
    public int mDownhill;
    /**
     * 平均步频 步/分
     */
    public int mStepRate;
    /**
     * 运动次数 次
     */
    public int mSportCount;

    public int steps;

    public List<SportLocation> mLocations = new ArrayList<>();

    @Override
    public String toString() {
        return "SportPlusEntity{" +
                "mSportType=" + mSportType +
                ", mStartTime=" + mStartTime +
                ", mDuration=" + mDuration +
                ", mDistance=" + mDistance +
                ", mCalories=" + mCalories +
                ", mSpeedAvg=" + mSpeedAvg +
                ", mSpeedMax=" + mSpeedMax +
                ", mRateAvg=" + mRateAvg +
                ", mRateMin=" + mRateMin +
                ", mRateMax=" + mRateMax +
                ", mElevation=" + mElevation +
                ", mUphill=" + mUphill +
                ", mDownhill=" + mDownhill +
                ", mStepRate=" + mStepRate +
                ", mSportCount=" + mSportCount +
                ", mLocations=" + mLocations +
                ", steps=" + steps +
                '}';
    }
}
