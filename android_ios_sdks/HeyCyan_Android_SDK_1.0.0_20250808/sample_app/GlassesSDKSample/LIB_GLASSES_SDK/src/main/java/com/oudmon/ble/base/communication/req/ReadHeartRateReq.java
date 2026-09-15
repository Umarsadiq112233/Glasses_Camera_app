package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.DataParseUtils;

/**
 * Created by jxr202 on 2017/12/12
 */

public class ReadHeartRateReq extends BaseReqCmd {

    private byte[] data;

    public ReadHeartRateReq(long time) {
        super(Constants.CMD_GET_HEART_RATE);
        data = DataParseUtils.intToByteArray((int) (time));
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }

}
