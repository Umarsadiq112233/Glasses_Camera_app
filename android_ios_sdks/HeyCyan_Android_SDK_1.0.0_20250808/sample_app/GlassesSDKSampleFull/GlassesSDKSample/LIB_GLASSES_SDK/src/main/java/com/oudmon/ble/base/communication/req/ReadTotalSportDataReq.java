package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * AA = 0表示当天，AA = 10表示10天前
 */

public class ReadTotalSportDataReq extends BaseReqCmd {
    private int theDayOffset = 0;

    public ReadTotalSportDataReq(int theDayOffset) {
        super(Constants.CMD_GET_STEP_TOTAL_SOMEDAY);
        this.theDayOffset = theDayOffset;
    }

    @Override
    protected byte[] getSubData() {
        return new byte[] {(byte) (theDayOffset & 0xff)};
    }
}
