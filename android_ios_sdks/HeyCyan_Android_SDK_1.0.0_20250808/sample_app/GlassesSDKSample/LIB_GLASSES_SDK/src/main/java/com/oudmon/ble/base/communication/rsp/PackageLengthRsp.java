package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on swatch_device_text4/7
 */
public class PackageLengthRsp extends BaseRspCmd {

    public int mData = 0;

    @Override
    public boolean acceptData(byte[] data) {
        mData = data[0] & 0xff;
        return false;
    }

}
