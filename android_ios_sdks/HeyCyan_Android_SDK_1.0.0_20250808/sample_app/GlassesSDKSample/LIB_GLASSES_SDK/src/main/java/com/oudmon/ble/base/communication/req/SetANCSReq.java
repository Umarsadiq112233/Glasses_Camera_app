package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class SetANCSReq extends BaseReqCmd {

    public SetANCSReq() {
        super(Constants.CMD_SET_ANCS_ON_OFF);
    }

    @Override
    protected byte[] getSubData() {
        byte[] src = new byte[2];
        src[0] = (byte) 0xff;
        src[1] = (byte) 0x9f;
        return src;
    }
}
