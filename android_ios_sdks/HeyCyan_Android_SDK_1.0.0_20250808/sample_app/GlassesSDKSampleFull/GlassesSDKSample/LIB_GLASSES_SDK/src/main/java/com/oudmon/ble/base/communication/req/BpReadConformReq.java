package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.14 定时血压数据确认
 */

public class BpReadConformReq extends BaseReqCmd {

    private  boolean isSuccess;

    public BpReadConformReq(boolean isSuccess) {
        super(Constants.CMD_BP_TIMING_MONITOR_CONFIRM);
        this.isSuccess = isSuccess;
    }

    @Override
    protected byte[] getSubData() {
        return new byte[]{(byte) (isSuccess ? 0x00 : 0xff)};
    }
}
