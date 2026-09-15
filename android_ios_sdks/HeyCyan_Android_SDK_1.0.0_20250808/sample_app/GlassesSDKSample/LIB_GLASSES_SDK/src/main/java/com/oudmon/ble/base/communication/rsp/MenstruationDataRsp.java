package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class MenstruationDataRsp extends BaseRspCmd {
    @Override
    public boolean acceptData(byte[] data) {
        Log.i(TAG, "acceptData.. data: " + DataTransferUtils.getHexString(data));

        return true;
    }
}
