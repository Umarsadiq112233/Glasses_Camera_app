package com.oudmon.ble.base.communication.responseImpl;


import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.JPackageManager;
import com.oudmon.ble.base.communication.LargeDataHandler;
import com.oudmon.ble.base.communication.rsp.PackageLengthRsp;

/**
 * @author gs ,
 * @date /1/21
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class PackageLengthListener implements ICommandResponse<PackageLengthRsp> {

    @Override
    public void onDataResponse(PackageLengthRsp resultEntity) {
        JPackageManager.getInstance().setLength(Math.max(resultEntity.mData, 244));
        LargeDataHandler.getInstance().packageLength();
    }

}
