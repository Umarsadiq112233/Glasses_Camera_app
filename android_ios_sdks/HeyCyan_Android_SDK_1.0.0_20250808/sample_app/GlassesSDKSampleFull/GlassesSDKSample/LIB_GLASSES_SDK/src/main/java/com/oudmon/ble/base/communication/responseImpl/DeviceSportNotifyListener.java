package com.oudmon.ble.base.communication.responseImpl;

import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceSportNotifyListener implements ICommandResponse<DeviceNotifyRsp> {
    private ConcurrentHashMap<Integer, ICommandResponse<DeviceNotifyRsp>> respList = new ConcurrentHashMap<>();

    public void setOutRspIOdmOpResponse(int key, ICommandResponse<DeviceNotifyRsp> outRspIOdmOpResponse) {
        respList.put(key, outRspIOdmOpResponse);
        if(respList.get(100)!=null){
            removeOtherCallbacks();
        }
    }

    public void removeCallback(int key){
        respList.remove(key);
    }

    public void removeOtherCallbacks(){
        //心率
        respList.remove(78);
    }

    @Override
    public void onDataResponse(DeviceNotifyRsp resultEntity) {
        for (ICommandResponse<DeviceNotifyRsp> outRspIOdmOpResponse : respList.values()) {
            outRspIOdmOpResponse.onDataResponse(resultEntity);
        }
    }
}
