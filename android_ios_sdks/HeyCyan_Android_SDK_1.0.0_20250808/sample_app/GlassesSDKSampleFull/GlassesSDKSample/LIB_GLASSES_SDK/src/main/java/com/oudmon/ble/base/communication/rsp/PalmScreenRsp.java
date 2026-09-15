package com.oudmon.ble.base.communication.rsp;

/**
 * 勿扰模式响应
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class PalmScreenRsp extends MixtureRsp {


    /**
     * 功能是否开启
     */
    private boolean isEnable;
    /**
     * 是否左手佩戴
     */
    private boolean isLeft;
    /**
     * 是否翻腕
     */
    private boolean needPalm;


    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
        isLeft = (subData[2] & 0x01) == 0x01;
        needPalm = (subData[2] & 0x04) == 0x04;
    }


    public boolean isEnable() {
        return isEnable;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isNeedPalm() {
        return needPalm;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void setLeft(boolean left) {
        isLeft = left;
    }
}
