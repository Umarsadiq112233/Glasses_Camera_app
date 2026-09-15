package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class PressureSettingReq extends MixtureReq {

    private PressureSettingReq(boolean open) {
        super(Constants.CMD_PRESSURE_SETTING);
        subData=new byte[]{0x02,(byte) (open?0x01:0x00)};
    }

    private PressureSettingReq() {
        super(Constants.CMD_PRESSURE_SETTING);
        subData=new byte[]{0x01};
    }

    public static PressureSettingReq getWriteInstance(boolean open){
        return new PressureSettingReq(open);
    }

    public static PressureSettingReq getReadInstance(){
        return new PressureSettingReq();
    }
}
