package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

/**
 手机发起运动
 */

public class PhoneSportReq extends MixtureReq {

    private PhoneSportReq() {
        super(Constants.CMD_PHONE_SPORT);
    }

    public static PhoneSportReq getSportStatus(byte status,byte sportType) {
        return new PhoneSportReq() {{
            subData = new byte[] {status, (byte) sportType};
        }};
    }

}
