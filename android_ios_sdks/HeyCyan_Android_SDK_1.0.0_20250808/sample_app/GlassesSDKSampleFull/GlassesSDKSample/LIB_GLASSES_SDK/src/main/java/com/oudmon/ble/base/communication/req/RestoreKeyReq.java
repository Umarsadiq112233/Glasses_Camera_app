package com.oudmon.ble.base.communication.req;

/**
 * @author gs ,
 * @date /1/20
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public  class RestoreKeyReq extends SimpleKeyReq {

    public RestoreKeyReq(byte key) {
        super(key);
    }

    @Override
    protected byte[] getSubData() {
        byte[] sub = new byte[2];
        sub[0] = 0x66;
        sub[1] = 0x66;
        return sub;
    }
}

