package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/5/14
 * 手环显示时长设置
 */

public class DisplayTimeReq extends MixtureReq {

    private DisplayTimeReq() {
        super(Constants.CMD_DISPLAY_TIME);
    }

    public static DisplayTimeReq getReadInstance() {
        return new DisplayTimeReq() {{
            subData = new byte[] {0x01};
        }};
    }

    public static DisplayTimeReq getWriteInstance(final int displayTime, final int displayType, final int alpha,int total,int curr) {
        return new DisplayTimeReq() {{
            subData = new byte[] {0x02, (byte) displayTime, (byte) displayType, (byte) alpha,0, (byte) total, (byte) curr};
        }};
    }

    public static DisplayTimeReq getWriteInstanceNew(final int displayTime, final int displayType, final int alpha,int total,int curr,boolean open) {
        return new DisplayTimeReq() {{
            subData = new byte[] {0x02, (byte) displayTime, (byte) displayType, (byte) alpha,0, (byte) total, (byte) curr,5,30,5, (byte) (open?2:1)};
        }};
    }

    public static DisplayTimeReq getDeleteInstance() {
        return new DisplayTimeReq() {{
            subData = new byte[] {0x03};
        }};
    }

}
