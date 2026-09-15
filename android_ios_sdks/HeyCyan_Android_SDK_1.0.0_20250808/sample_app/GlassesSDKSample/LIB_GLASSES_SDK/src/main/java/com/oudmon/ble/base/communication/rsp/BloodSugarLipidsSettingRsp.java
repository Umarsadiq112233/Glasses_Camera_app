package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BloodSugarLipidsSettingRsp extends BaseRspCmd {
    private boolean read;
    private byte type;

    private boolean isEnable;

    private int value;


    public boolean isEnable() {
        return isEnable;
    }

    public int getValue() {
        return value;
    }

    public byte getType() {
        return type;
    }

    @Override
    public boolean acceptData(byte[] data) {
        //00 01 00 4800000000000000000000
        read = data[1] == 0x01;
        if (data[1] == 0x01) {
            type = data[0];
            isEnable = data[2] == 1;
            value = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 3, 5));
        }
        return false;
    }
}
