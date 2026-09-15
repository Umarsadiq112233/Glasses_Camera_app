package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.DataParseUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadBandSportReq extends BaseReqCmd {

    private byte[] data;

    public ReadBandSportReq(long time) {
        super(Constants.CMD_GET_SPORT);
        data = DataParseUtils.intToByteArray((int) (time));
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }
}
