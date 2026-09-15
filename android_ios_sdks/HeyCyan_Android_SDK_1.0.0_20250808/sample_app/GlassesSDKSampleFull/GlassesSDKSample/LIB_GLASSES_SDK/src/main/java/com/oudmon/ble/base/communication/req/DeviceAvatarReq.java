package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

public class DeviceAvatarReq extends BaseReqCmd {

    public DeviceAvatarReq() {
        super(Constants.CMD_DEVICE_AVATAR);
    }

    @Override
    protected byte[] getSubData() {
        return new byte[0];
    }
}
