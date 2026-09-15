package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;


public class SetMessagePushReq extends BaseReqCmd {

    public SetMessagePushReq() {
        super(Constants.CMD_GET_ANCS_ON_OFF);
    }

    @Override
    protected byte[] getSubData() {
       return null;
    }
}
