package com.oudmon.ble.base.communication;
import android.util.Log;

import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.communication.req.BaseReqCmd;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.request.LocalWriteRequest;
import com.oudmon.ble.base.request.ReadRequest;

/**
 * @author gs ,
 * @date swatch_device_text12/24
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class CommandHandle {
    private static final String TAG = "CommandHandle";
    private static CommandHandle odmHandle;

    public static CommandHandle getInstance() {
        if (odmHandle == null) {
            synchronized (CommandHandle.class){
                if(odmHandle==null){
                    odmHandle = new CommandHandle();
                }
            }
        }
        return odmHandle;
    }

    private CommandHandle() {

    }


    private <T extends BaseRspCmd> LocalWriteRequest<T> getWriteRequest(byte[] data) {
        LocalWriteRequest<T> writeRequest = new LocalWriteRequest<T>(Constants.UUID_SERVICE, Constants.UUID_WRITE);
        writeRequest.setValue(data);
        return writeRequest;
    }


    public ReadRequest getReadHwRequest() {
        return new ReadRequest(Constants.SERVICE_DEVICE_INFO, Constants.CHAR_HW_REVISION);
    }


    public ReadRequest getReadFmRequest() {
        return new ReadRequest(Constants.SERVICE_DEVICE_INFO, Constants.CHAR_FIRMWARE_REVISION);
    }




    public void  executeReqCmd(BaseReqCmd reqCmd, ICommandResponse iOpResponse) {
        if(!BleOperateManager.getInstance().isConnected()){
            Log.i(TAG,"设备已经断开：" + ByteUtil.byteArrayToString(reqCmd.getData()));
            return;
        }
        LocalWriteRequest localWriteRequest = getWriteRequest(reqCmd.getData());
        int notifyKey = localWriteRequest.getValue()[0] & (~Constants.FLAG_MASK_ERROR);
        localWriteRequest.setiOpResponse(iOpResponse);
//         Log.i(TAG,DataTransferUtils.getHexString(reqCmd.getData()));
        if(iOpResponse!=null){
            BleOperateManager.getInstance().getLocalWriteRequestConcurrentHashMap().put(notifyKey,localWriteRequest);
        }
        BleOperateManager.getInstance().execute(localWriteRequest);
    }



    public  void executeReqCmdNoCallback(BaseReqCmd reqCmd) {
        if(!BleOperateManager.getInstance().isConnected()){
             Log.i(TAG,"设备已经断开");
            return;
        }
        LocalWriteRequest localWriteRequest = getWriteRequest(reqCmd.getData());
        BleOperateManager.getInstance().execute(localWriteRequest);
    }

    public void execReadCmd(ReadRequest reqCmd){
        BleOperateManager.getInstance().execute(reqCmd);
    }
}
