package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BlackListReq extends BaseReqCmd {

    public BlackListReq() {
        super(Constants.CMD_BlackList_LOCATION);
    }

    @Override
    protected byte[] getSubData() {
        return new byte[]{0x01};
    }
}
