package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class QueryDataDistributionRsp extends BaseRspCmd {

    private int distribution;

    @Override
    public boolean acceptData(byte[] data) {
        distribution = BLEDataFormatUtils.bytes2Int(new byte[] {data[1 - 1], data[2 - 1], data[3 - 1], data[4 - 1]});
        return false;
    }

    public boolean isTheDayHasData(int dayOffset) {
        int tmp = distribution >> dayOffset;
        return (tmp & 0x01) != 0;
    }
}
