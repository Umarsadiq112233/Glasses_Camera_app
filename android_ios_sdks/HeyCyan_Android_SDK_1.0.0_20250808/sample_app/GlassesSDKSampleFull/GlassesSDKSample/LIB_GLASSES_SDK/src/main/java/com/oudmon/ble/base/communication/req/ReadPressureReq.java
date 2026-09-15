package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.DataParseUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadPressureReq extends BaseReqCmd {

    private byte[] data;

    public ReadPressureReq(long time) {
        super(Constants.CMD_GET_BAND_PRESSURE);
        byte[] timeArray = DataParseUtils.intToByteArray((int) (time));
        data = new byte[timeArray.length + 2];

        System.arraycopy(timeArray, 0, this.data, 0, timeArray.length);
        data[4] = 0x00;
        data[5] = (byte) Constants.BAND_PRESSURE_COUNT;
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }
}
