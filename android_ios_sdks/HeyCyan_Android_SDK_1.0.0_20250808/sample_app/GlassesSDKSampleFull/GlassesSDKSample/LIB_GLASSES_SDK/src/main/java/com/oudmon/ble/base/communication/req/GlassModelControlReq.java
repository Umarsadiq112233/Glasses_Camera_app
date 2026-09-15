package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;


public class GlassModelControlReq extends MixtureReq {

    public GlassModelControlReq(int dataType,int glassWorkType) {
        super(Constants.CMD_DEVICE_GLASS_MODEL_CONTROL);
        subData = new byte[]{0x02,(byte) dataType,(byte) glassWorkType};
    }

}
