package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.21 设置/读取手环横竖屏
 */

public class DisplayOrientationReq extends MixtureReq {

    private DisplayOrientationReq() {
        super(Constants.CMD_ORIENTATION);
        subData = new byte[]{0x01};
    }

    private DisplayOrientationReq(boolean isPortrait, boolean isLeft) {
        super(Constants.CMD_ORIENTATION);
        subData = new byte[]{0x02, (byte) (isPortrait ? 0x01 : 0x02), (byte) (isPortrait ? 0x00 : (isLeft ? 0x01 : 0x02))};
    }

    public static DisplayOrientationReq getReadInstance(){
        return new DisplayOrientationReq();
    }

    public static DisplayOrientationReq getWriteInstance(boolean isPortrait, boolean isLeft) {
        return new DisplayOrientationReq(isPortrait,isLeft);
    }
}
