package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 注意，BaseRspCmd针对只读的数据元，
 * 而如果有读有写的最好是继承MixtureRsp
 */

public abstract class BaseRspCmd {

    protected static final String TAG = "Jxr35";

    public static final int RESULT_OK = 0;
    /**
     * 校验错误或执行Fail返回 最高位为1
     */
    protected int status;
    protected int cmdType;

    /**
     * @param data
     * @return true 要等下一个数据，false,不需要
     */
    public abstract boolean acceptData(byte[] data);

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }
}
