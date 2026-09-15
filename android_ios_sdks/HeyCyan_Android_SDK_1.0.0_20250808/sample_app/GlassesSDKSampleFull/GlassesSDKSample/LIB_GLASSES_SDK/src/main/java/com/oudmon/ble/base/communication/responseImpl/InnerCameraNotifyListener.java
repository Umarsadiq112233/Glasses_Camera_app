package com.oudmon.ble.base.communication.responseImpl;
import android.content.Context;
import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;
import com.oudmon.ble.base.communication.rsp.CameraNotifyRsp;
/**
 * @author gs ,
 * @date /1/21
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class InnerCameraNotifyListener implements ICommandResponse<CameraNotifyRsp> {

    private Context mContext;
    private ICommandResponse<CameraNotifyRsp> outRspIOdmOpResponse;


    public InnerCameraNotifyListener(Context context) {
        mContext = context;
    }

    public ICommandResponse<CameraNotifyRsp> getOutRspIOdmOpResponse() {
        return outRspIOdmOpResponse;
    }

    public void setOutRspIOdmOpResponse(ICommandResponse<CameraNotifyRsp> outRspIOdmOpResponse) {
        this.outRspIOdmOpResponse = outRspIOdmOpResponse;
    }

    @Override
    public void onDataResponse(CameraNotifyRsp resultEntity) {
        //优先交给外部处理
        if (outRspIOdmOpResponse != null) {
            outRspIOdmOpResponse.onDataResponse(resultEntity);
        } else {
            if (resultEntity.getStatus() == BaseRspCmd.RESULT_OK) {
                if (resultEntity.getAction() == CameraNotifyRsp.ACTION_INTO_CAMERA_UI) {

                }
            }
        }
    }

}