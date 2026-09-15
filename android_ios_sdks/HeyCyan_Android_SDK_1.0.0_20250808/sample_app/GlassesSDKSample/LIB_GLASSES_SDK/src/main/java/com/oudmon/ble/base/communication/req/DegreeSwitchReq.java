package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/28
 */

public class DegreeSwitchReq extends MixtureReq {

    private DegreeSwitchReq() {
        super(Constants.CMD_GET_DEGREE_SWITCH);
    }

    public static DegreeSwitchReq getReadInstance() {
        return new DegreeSwitchReq() {{
            subData = new byte[] {0x01};
        }};
    }

    public static DegreeSwitchReq getWriteInstance(final boolean enable, final boolean isCelsius) {
        return new DegreeSwitchReq() {{
            subData = new byte[] {0x02, (byte) (enable ? 0x01 : 0x02), (byte) (isCelsius ? 0x01 : 0x02)};
        }};
    }

}
