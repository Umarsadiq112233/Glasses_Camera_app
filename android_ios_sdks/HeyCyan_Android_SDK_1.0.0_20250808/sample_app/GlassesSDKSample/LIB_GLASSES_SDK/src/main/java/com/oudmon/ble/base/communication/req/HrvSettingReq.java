package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
/**
 * Created by jxr202 on 2017/12/13
 */

public class HrvSettingReq extends MixtureReq {
    /**
     * 用于hrv的开关状态
     */
    private HrvSettingReq() {
        super(Constants.CMD_HRV_ENABLE);
        subData = new byte[]{0x01};
    }

    /**
     * 用于写入心率数据
     * @param isEnable 开启或关闭该功能
     */
    public HrvSettingReq(boolean isEnable) {
            super(Constants.CMD_HRV_ENABLE);
            subData = new byte[]{0x02, (byte) (isEnable ? 0x01 : 0x00)};
    }

    /**
     * 读取开关状态数据
     * @return req
     */
    public static HrvSettingReq getReadInstance() {
        return new HrvSettingReq();
    }
}
