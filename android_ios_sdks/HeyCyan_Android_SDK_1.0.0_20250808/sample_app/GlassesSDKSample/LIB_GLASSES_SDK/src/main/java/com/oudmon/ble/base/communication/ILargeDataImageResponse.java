package com.oudmon.ble.base.communication;
/**
 * @author gs ,
 * @date /5/13
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public interface ILargeDataImageResponse {
    void parseData(int cmdType,boolean success,byte[] data);
}
