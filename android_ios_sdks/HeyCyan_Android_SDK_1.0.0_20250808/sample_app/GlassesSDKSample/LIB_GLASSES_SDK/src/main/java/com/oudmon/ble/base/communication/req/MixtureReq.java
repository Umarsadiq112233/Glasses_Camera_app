package com.oudmon.ble.base.communication.req;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 混合请求，表示这个命令 可以READ，也可以Write，
 * 比如 勿扰的设置与读取，翻腕的设置与读取
 * 如果只是读取数据，如读取血压、心率、电量等数据，
 * 最好是继承 BaseReqCmd
 */

public abstract class MixtureReq extends BaseReqCmd {

    protected byte[] subData;

    public MixtureReq(byte key) {
        super(key);
    }

    @Override
    protected byte[] getSubData() {
        return subData;
    }
}
