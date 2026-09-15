package com.oudmon.ble.base.communication.bigData.resp;
import com.oudmon.ble.base.communication.ILargeDataResponse;
import java.util.concurrent.ConcurrentHashMap;

public class GlassesDeviceNotifyListener implements ILargeDataResponse<GlassesDeviceNotifyRsp> {
    private ConcurrentHashMap<Integer, ILargeDataResponse<GlassesDeviceNotifyRsp>> respList = new ConcurrentHashMap<>();

    public void setOutRspIOdmOpResponse(int key, ILargeDataResponse<GlassesDeviceNotifyRsp> outRspIOdmOpResponse) {
        respList.put(key, outRspIOdmOpResponse);
    }

    public void removeCallback(int key){
        respList.remove(key);
    }


    @Override
    public void parseData(int cmdType, GlassesDeviceNotifyRsp response) {
        for (ILargeDataResponse<GlassesDeviceNotifyRsp> outRspIOdmOpResponse : respList.values()) {
            outRspIOdmOpResponse.parseData(cmdType,response);
        }
    }
}
