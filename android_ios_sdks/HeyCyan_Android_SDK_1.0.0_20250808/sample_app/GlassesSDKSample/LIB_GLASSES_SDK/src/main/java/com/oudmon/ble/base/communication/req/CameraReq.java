package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class CameraReq extends BaseReqCmd {

    public static final byte ACTION_INTO_CAMARA_UI = 0x04;
    public static final byte ACTION_KEEP_SCREEN_ON = 0x05;
    public static final byte ACTION_FINISH = 0x06;

    private byte action = 0;
    public CameraReq(byte action) {
        super(Constants.CMD_TAKING_PICTURE);
        if (action>ACTION_FINISH||action< ACTION_INTO_CAMARA_UI)
            throw new IllegalArgumentException("action 范围出错");
        this.action = action;
    }

    @Override
    protected byte[] getSubData() {
        return new byte[]{action};
    }
}
