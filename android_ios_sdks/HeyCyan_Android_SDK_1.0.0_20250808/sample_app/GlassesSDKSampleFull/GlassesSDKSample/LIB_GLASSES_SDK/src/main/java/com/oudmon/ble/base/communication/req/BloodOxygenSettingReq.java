package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by jxr202 on 2017/12/13
 */

public class BloodOxygenSettingReq extends MixtureReq {

    /**
     * 用于读取血氧的开关状态
     */
    private BloodOxygenSettingReq() {
        super(Constants.CMD_AUTO_BLOOD_OXYGEN);
        subData = new byte[]{0x01};
    }

    /**
     * 用于写入血氧数据
     * @param isEnable 开启或关闭该功能
     */
    private BloodOxygenSettingReq(boolean isEnable) {
        super(Constants.CMD_AUTO_BLOOD_OXYGEN);
        subData = new byte[]{0x02, (byte) (isEnable ? 0x01 : 0)};
    }

    /**
     * 读取开关状态数据
     * @return req
     */
    public static BloodOxygenSettingReq getReadInstance() {
        return new BloodOxygenSettingReq();
    }

    /**
     * 写入数据，开启或关闭该功能
     * @param isEnable 开启或关闭该功能
     * @return req
     */
    public static BloodOxygenSettingReq getWriteInstance(boolean isEnable) {
        return new BloodOxygenSettingReq(isEnable);
    }

}
