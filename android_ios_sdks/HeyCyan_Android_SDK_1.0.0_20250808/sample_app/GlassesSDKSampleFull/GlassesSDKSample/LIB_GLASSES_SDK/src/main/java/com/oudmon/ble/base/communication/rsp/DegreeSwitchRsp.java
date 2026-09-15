package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/28
 */

public class DegreeSwitchRsp extends MixtureRsp {

    /**
     * 天气预报开关状态
     */
    private boolean isEnable = false;
    /**
     * 温度显示是否是摄氏度
     */
    private boolean isCelsius = false;

    @Override
    protected void readSubData(byte[] subData) {

        isEnable = subData[1] == 0x01;

        isCelsius = subData[2] == 0x01;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isCelsius() {
        return isCelsius;
    }

    public void setCelsius(boolean celsius) {
        isCelsius = celsius;
    }


    @Override
    public String toString() {
        return "DegreeSwitchRsp{" +
                "isEnable=" + isEnable +
                ", isCelsius=" + isCelsius +
                '}';
    }
}
