package com.oudmon.ble.base.request;
import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;

import java.util.UUID;

/**
 * @author gs ,
 * @date swatch_device_text12/24
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class LocalWriteRequest<T extends BaseRspCmd> extends WriteRequest{
    private ICommandResponse<T> iOpResponse;
    private int type;
    public LocalWriteRequest(UUID serviceUuid, UUID charUuid) {
        super(serviceUuid, charUuid);
    }

    public ICommandResponse<T> getiOpResponse() {
        return iOpResponse;
    }

    public  void setiOpResponse(ICommandResponse<T> iOpResponse) {
        this.iOpResponse = iOpResponse;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
