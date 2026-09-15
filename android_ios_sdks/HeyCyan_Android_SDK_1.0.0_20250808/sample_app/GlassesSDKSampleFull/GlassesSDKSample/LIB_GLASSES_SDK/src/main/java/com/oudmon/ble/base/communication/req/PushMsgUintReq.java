package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class PushMsgUintReq extends BaseReqCmd {

    public static final byte TYPE_PHONE_RING = 0x00;//：来电提醒
    public static final byte TYPE_SMS = 0x01;//：短信提醒
    public static final byte TYPE_QQ = 0x02;//：QQ提醒
    public static final byte TYPE_WECHAT = 0x03;//：微信提醒
    public static final byte TYPE_PHONE_ACTION = 0x04;//：来电接听或挂电话
    public static final byte TYPE_FACEBOOK = 0x05;//：Facebook消息提醒
    public static final byte TYPE_WHATSAPP = 0x06;//：WhatsApp消息提醒
    public static final byte TYPE_TWITTER = 0x07;//：Twitter消息提醒
    public static final byte TYPE_SKYPE = 0x08;//：Skype消息提醒
    public static final byte TYPE_Line = 0x09;//：Line消息提醒
    private byte[] subData;

    public PushMsgUintReq(byte type, int total, int index, byte[] msgData) {
        super(Constants.CMD_PUSH_MSG);
        this.subData = new byte[msgData.length + 3];
        subData[0] = type;
        subData[1] = (byte) total;
        subData[2] = (byte) index;
        System.arraycopy(msgData, 0, this.subData, 3, msgData.length);

    }

    @Override
    protected byte[] getSubData() {
        return subData;
    }
}
