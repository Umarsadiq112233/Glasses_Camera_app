package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5
 * 设置/读取手环首页显示
 */

public class DisplayClockReq extends MixtureReq {

    private DisplayClockReq() {
        super(Constants.CMD_DISPLAY_CLOCK);
        subData = new byte[]{0x01};
    }

    private DisplayClockReq(boolean enable) {
        super(Constants.CMD_DISPLAY_CLOCK);
        subData = new byte[]{0x02, (byte) (enable ? 0x01 : 0x02)};
    }

    public static DisplayClockReq getReadInstance() {
        return new DisplayClockReq();
    }

    public static DisplayClockReq getWriteInstance(boolean enable) {
        return new DisplayClockReq(enable);
    }
}
