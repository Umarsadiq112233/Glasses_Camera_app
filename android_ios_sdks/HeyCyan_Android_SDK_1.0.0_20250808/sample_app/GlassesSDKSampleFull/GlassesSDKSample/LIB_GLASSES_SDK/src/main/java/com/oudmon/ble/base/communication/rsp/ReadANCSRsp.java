package com.oudmon.ble.base.communication.rsp;

import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadANCSRsp extends BaseRspCmd {
    private short stateMask;

    @Override
    public boolean acceptData(byte[] data) {
        stateMask = DataTransferUtils.bytesToShort(data, 0);
        return false;
    }

    public short getStateMask() {
        return stateMask;
    }

    public void setStateMask(short stateMask) {
        this.stateMask = stateMask;
    }
}
