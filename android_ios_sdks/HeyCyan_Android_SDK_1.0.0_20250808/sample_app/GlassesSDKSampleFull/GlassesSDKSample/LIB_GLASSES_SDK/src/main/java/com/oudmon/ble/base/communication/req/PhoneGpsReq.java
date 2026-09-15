package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.ByteUtil;

/**
 手机发起PGS运动
 */

public class PhoneGpsReq extends MixtureReq {

    private PhoneGpsReq() {
        super(Constants.CMD_PHONE_GPS);
    }

    public static PhoneGpsReq getGpsStatus(byte status) {
        return new PhoneGpsReq() {{
            subData = new byte[] {status,0x00};
        }};
    }


    public static PhoneGpsReq setPhoneDataReq(int distance,int calorie) {
        return new PhoneGpsReq() {{
            byte [] distanceArray=ByteUtil.intToByte(distance,4);
            byte [] calorieArray=ByteUtil.intToByte(calorie,4);
            byte []tempA = ByteUtil.concat(new byte[] {0x05,0x00},distanceArray);
            subData= ByteUtil.concat(tempA,calorieArray);
        }};
    }

}
