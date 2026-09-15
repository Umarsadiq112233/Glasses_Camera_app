package com.oudmon.ble.base.communication;

import com.oudmon.ble.base.communication.rsp.BaseRspCmd;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public interface ICommandResponse<T extends BaseRspCmd> {

    void onDataResponse(T resultEntity);
}
