package com.oudmon.ble.base.communication.bigData.bean;

public class BaseBean {
    private int type;

    public BaseBean() {
    }

    public BaseBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
