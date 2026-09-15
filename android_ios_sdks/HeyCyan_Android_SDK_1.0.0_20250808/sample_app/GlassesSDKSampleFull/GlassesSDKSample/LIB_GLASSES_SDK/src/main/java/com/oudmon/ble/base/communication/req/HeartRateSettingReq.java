package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by jxr202 on 2017/12/13
 */

public class HeartRateSettingReq extends MixtureReq {

    /**
     * 用于读取心率的开关状态
     */
    private HeartRateSettingReq() {
        super(Constants.CMD_HR_TIMING_MONITOR_SWITCH);
        subData = new byte[]{0x01};
    }

    /**
     * 用于写入心率数据
     * @param isEnable 开启或关闭该功能
     */
    private HeartRateSettingReq(boolean isEnable,int interval) {
        super(Constants.CMD_HR_TIMING_MONITOR_SWITCH);
        subData = new byte[]{0x02, (byte) (isEnable ? 0x01 : 0x02), (byte) interval};
    }

    /**
     * 读取开关状态数据
     * @return req
     */
    public static HeartRateSettingReq getReadInstance() {
        return new HeartRateSettingReq();
    }

    /**
     * 写入数据，开启或关闭该功能
     * @param isEnable 开启或关闭该功能
     * @return req
     */
    public static HeartRateSettingReq getWriteInstance(boolean isEnable,int interval) {
        return new HeartRateSettingReq(isEnable,interval);
    }

}
