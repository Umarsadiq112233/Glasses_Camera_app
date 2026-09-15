package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.ByteUtil;

public class SugarLipidsSettingReq extends MixtureReq {

    private SugarLipidsSettingReq(byte type) {
        super(Constants.CMD_DEVICE_SUGAR_LIPIDS);
        subData = new byte[]{type,0x01};
    }


    private SugarLipidsSettingReq(byte type, boolean isEnable, int value) {
        super(Constants.CMD_DEVICE_SUGAR_LIPIDS);
        subData = new byte[]{type,0x02, (byte) (isEnable ? 0x01 : 0x00), (byte) ByteUtil.loword(value), (byte) ByteUtil.hiword(value)};

    }

    public static SugarLipidsSettingReq getReadInstance(byte type){
        return new SugarLipidsSettingReq(type);
    }

    public static SugarLipidsSettingReq getWriteInstance(byte type,boolean isEnable,int value) {
        return new SugarLipidsSettingReq(type,isEnable,value);
    }
}
