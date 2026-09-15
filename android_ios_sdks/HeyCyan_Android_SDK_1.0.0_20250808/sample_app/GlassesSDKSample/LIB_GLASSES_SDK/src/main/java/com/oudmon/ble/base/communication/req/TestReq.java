package com.oudmon.ble.base.communication.req;


import com.oudmon.ble.base.communication.Constants;

/**
 * @author gs ,
 * @date swatch_device_text8/29,
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class TestReq extends MixtureReq {
    public TestReq() {
        super(Constants.CMD_TEST_OPEN);
    }
}
