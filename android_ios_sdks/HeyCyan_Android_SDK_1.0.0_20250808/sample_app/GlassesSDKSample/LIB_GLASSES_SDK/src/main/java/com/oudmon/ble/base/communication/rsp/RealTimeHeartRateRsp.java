package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on swatch_device_text5/9
 */
public class RealTimeHeartRateRsp extends BaseRspCmd {
    private int heart;

    @Override
    public boolean acceptData(byte[] data) {
        heart = data[0];
        return false;
    }


    public int getHeart() {
        return heart;
    }
}
