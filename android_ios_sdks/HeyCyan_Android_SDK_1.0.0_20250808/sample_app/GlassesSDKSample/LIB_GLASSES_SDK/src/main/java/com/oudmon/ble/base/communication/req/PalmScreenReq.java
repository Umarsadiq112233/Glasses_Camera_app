package com.oudmon.ble.base.communication.req;


import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 翻腕
 */

public class PalmScreenReq extends MixtureReq {

    private PalmScreenReq() {
        super(Constants.CMD_FANWAN);
        subData = new byte[] {0x01};
    }

    private PalmScreenReq(boolean isEnable, boolean isLeft, boolean needPalm) {
        super(Constants.CMD_FANWAN);
        subData = new byte[] {0x02, (byte) (isEnable ? 0x01 : 0x02), (byte) ((isLeft ? 0x01 : 0x02) | (needPalm ? 0x04 : 0x00))};
    }

    public static PalmScreenReq getReadInstance() {
        return new PalmScreenReq();
    }

    public static PalmScreenReq getWriteInstance(boolean isEnable, boolean isLeft) {
        return new PalmScreenReq(isEnable, isLeft, true);
    }

}
