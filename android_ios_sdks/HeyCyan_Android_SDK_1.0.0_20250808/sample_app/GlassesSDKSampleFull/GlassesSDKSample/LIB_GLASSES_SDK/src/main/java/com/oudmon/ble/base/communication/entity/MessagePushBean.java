package com.oudmon.ble.base.communication.entity;

/**
 * @author gs ,
 * @date swatch_device_text9/12,
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class MessagePushBean {
    private String message;
    private long time;

    public MessagePushBean(String message, long time) {
        this.message = message;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
