package com.oudmon.ble.base.communication;
import com.oudmon.ble.base.communication.rsp.SleepNewProtoResp;

/**
 * @author gs ,
 * @date /5/13
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public interface ILargeDataSleepResponse {
    void sleepData(SleepNewProtoResp resp);
}
