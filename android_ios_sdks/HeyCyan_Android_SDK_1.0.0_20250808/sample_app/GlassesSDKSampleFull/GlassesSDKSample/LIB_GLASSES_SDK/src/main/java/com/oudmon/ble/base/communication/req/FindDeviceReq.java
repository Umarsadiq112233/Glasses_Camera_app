package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class FindDeviceReq extends BaseReqCmd {

    public FindDeviceReq() {
        super(Constants.CMD_ANTI_LOST_RATE);
    }

    @Override
    protected byte[] getSubData() {
        return new byte[]{0x55, (byte) 0xAA};
    }
}
