package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5
 * 设置/读取手环首页显示
 */

public class DisplayStyleReq extends MixtureReq {

    private DisplayStyleReq() {
        super(Constants.CMD_DISPLAY_STYLE);
        subData = new byte[]{0x01};
    }

    private DisplayStyleReq(int index) {
        super(Constants.CMD_DISPLAY_STYLE);
        subData = new byte[]{0x02, (byte) index};
    }

    public static DisplayStyleReq getReadInstance() {
        return new DisplayStyleReq();
    }

    public static DisplayStyleReq getWriteInstance(int index) {
        return new DisplayStyleReq(index);
    }
}
