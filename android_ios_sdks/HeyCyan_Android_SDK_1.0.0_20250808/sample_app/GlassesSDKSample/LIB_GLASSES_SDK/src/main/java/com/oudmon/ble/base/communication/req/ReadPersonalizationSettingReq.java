package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr202 on 2017/12/21
 */

public class ReadPersonalizationSettingReq extends BaseReqCmd {

    private byte[] data = new byte[] {0x01, 0x02, 0x03};

    private ReadPersonalizationSettingReq() {
        super(Constants.CMD_GET_PERSONALIZATION_SETTING);
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }

    public static ReadPersonalizationSettingReq getReadInstance() {
        return new ReadPersonalizationSettingReq();
    }

}
