package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/5/14
 * AGPS配置
 */

public class AgpsReq extends MixtureReq {

    private AgpsReq() {
        super(Constants.CMD_AGPS_SWITCH);
    }

    public static AgpsReq getReadInstance() {
        return new AgpsReq() {{
            subData = new byte[] {0x01};
        }};
    }

    public static AgpsReq getWriteInstance(final boolean enable) {
        return new AgpsReq() {{
            subData = new byte[] {0x02, (byte) (enable ? 0x01 : 0x00)};
        }};
    }

}
