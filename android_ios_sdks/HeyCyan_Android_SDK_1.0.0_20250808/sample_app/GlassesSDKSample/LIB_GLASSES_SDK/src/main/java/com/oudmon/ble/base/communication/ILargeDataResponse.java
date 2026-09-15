package com.oudmon.ble.base.communication;
import com.oudmon.ble.base.communication.bigData.resp.BaseResponse;

public interface ILargeDataResponse<T extends BaseResponse> {
    void parseData(int cmdType, T response);
}
