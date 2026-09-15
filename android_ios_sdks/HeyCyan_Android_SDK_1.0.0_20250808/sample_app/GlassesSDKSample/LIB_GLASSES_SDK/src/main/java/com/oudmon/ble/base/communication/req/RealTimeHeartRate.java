package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * @author gs
 * @CreateDate: /8/5 17:16
 * <p>
 * "佛主保佑,
 * 永无bug"
 */
public class RealTimeHeartRate extends BaseReqCmd{
    private int type;
    private byte[] mData = new byte[1];

    public RealTimeHeartRate(int type) {
        super(Constants.CMD_REAL_TIME_HEART_RATE);
        this.type = type;
        mData[0]= (byte) type;
    }

    @Override
    protected byte[] getSubData() {
        return mData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
