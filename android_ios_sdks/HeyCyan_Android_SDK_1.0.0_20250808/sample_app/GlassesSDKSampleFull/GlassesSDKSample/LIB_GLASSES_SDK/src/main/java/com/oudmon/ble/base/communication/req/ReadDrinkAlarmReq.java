package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * RSP 用 ReadAlarmRsp
 */

public class ReadDrinkAlarmReq extends BaseReqCmd {

    private int alarmIndex;

    public ReadDrinkAlarmReq(int alarmIndex) {
        super(Constants.CMD_GET_DRINK_TIME);
        if (alarmIndex > 7) throw new IllegalArgumentException("闹钟索引只能0 到 7");
        this.alarmIndex = alarmIndex;
    }

    @Override
    protected byte[] getSubData() {
        return new byte[] {(byte) alarmIndex};
    }

}
