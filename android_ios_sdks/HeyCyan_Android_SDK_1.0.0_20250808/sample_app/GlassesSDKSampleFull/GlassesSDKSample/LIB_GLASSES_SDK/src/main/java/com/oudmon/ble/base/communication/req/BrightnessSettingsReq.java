package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/29
 */

public class BrightnessSettingsReq extends MixtureReq {

    private BrightnessSettingsReq() {
        super(Constants.CMD_GET_BRIGHTNESS);
    }

    /**
     * 读取当前亮度的实例
     * @return instance
     */
    public static BrightnessSettingsReq getReadInstance() {
        return new BrightnessSettingsReq() {{
            subData = new byte[] {0x01};
        }};
    }

    /**
     * 写入手环亮度，级别1 - 10.
     * @param level 1 - 10之间的数据
     * @return instance
     */
    public static BrightnessSettingsReq getWriteInstance(final int level) {
        return new BrightnessSettingsReq() {{
            subData = new byte[] {0x02, (byte) level};
        }};
    }

}
