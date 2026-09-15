package com.oudmon.ble.base.bluetooth;
import android.util.Log;
import android.util.SparseArray;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;
import com.oudmon.ble.base.request.LocalWriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.util.Arrays;

/**
 * @author gs ,
 * @date swatch_device_text12/24
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class QCDataParser {
    public static final String TAG = "GLASSES_LOG";
    private static SparseArray<BaseRspCmd> tempRspDataSparseArray = new SparseArray<>();

    public static boolean checkCrc(byte[] data) {
        int crc = 0;
        for (int i = 0; i < data.length - 1; i++) {
            crc += data[i];
        }
        return data[data.length - 1] == (byte) (crc & 0xFF);
    }

    public static boolean parserAndDispatchReqData(byte[] data) {
        int notifyKey = data[0] & (~Constants.FLAG_MASK_ERROR);
        int status = data[0] & Constants.FLAG_MASK_ERROR;
        LocalWriteRequest localWriteRequest = BleOperateManager.getInstance().getLocalWriteRequestConcurrentHashMap().get(notifyKey);
        if(localWriteRequest!=null){
            ICommandResponse iOpResponse = localWriteRequest.getiOpResponse();
            if(iOpResponse!=null){
                BaseRspCmd tempBaseRspCmd = tempRspDataSparseArray.get(notifyKey);
                if (tempBaseRspCmd == null) {
//                    Type[] genType = iOpResponse.getClass().getGenericInterfaces();
//                    Type[] params = ((ParameterizedType) genType[0]).getActualTypeArguments();
//                    Class<? extends BaseRspCmd> rspClass = (Class<? extends BaseRspCmd>) params[0];
                    try {
//                        tempBaseRspCmd = rspClass.newInstance();
//                        tempBaseRspCmd.setStatus(status);
//                         Log.i(TAG, Integer.toHexString(notifyKey & 0xFF));
                        tempBaseRspCmd=BeanFactory.createBean(notifyKey,localWriteRequest.getType());
                    }catch (Exception e) {
                        e.printStackTrace();
                        BleOperateManager.getInstance().getLocalWriteRequestConcurrentHashMap().clear();
                    }
                }
                if(tempBaseRspCmd!=null){
                    tempBaseRspCmd.setCmdType(notifyKey);
                    boolean needNext = tempBaseRspCmd.acceptData(Arrays.copyOfRange(data, 1, data.length - 1));
                    if (needNext) {
                        tempRspDataSparseArray.put(notifyKey, tempBaseRspCmd);
                        return true;
                    }
                    iOpResponse.onDataResponse(tempBaseRspCmd);
                    tempRspDataSparseArray.delete(notifyKey);
                    return true;
                }
            }
        }
        return false;
    }




    public static boolean parserAndDispatchNotifyData(SparseArray<ICommandResponse> sparseArray, byte[] data) {
        int notifyKey = data[0] & (~Constants.FLAG_MASK_ERROR);
        int status = data[0] & Constants.FLAG_MASK_ERROR;
        ICommandResponse iOpResponse = sparseArray.get(notifyKey);
//         Log.i(TAG,notifyKey);
         Log.i(TAG, "notifyKey: " + DataTransferUtils.getHexString(DataTransferUtils.intToBytes(notifyKey)));
        if (iOpResponse != null) {
            BaseRspCmd tempBaseRspCmd = tempRspDataSparseArray.get(notifyKey);//先读取缓存 TODO: 这里应该需要加入一个超时，防止数据回到一半，没返回，下一个数据来的时候跟缓存的拼，导致错误
            if (tempBaseRspCmd == null) {
//                Type[] genType = iOpResponse.getClass().getGenericInterfaces();
//                Type[] params = ((ParameterizedType) genType[0]).getActualTypeArguments();
//                Class<? extends BaseRspCmd> rspClass = (Class<? extends BaseRspCmd>) params[0];
//                try {
//                    tempBaseRspCmd = rspClass.newInstance();
//                    tempBaseRspCmd.setStatus(status);
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                    sparseArray.delete(notifyKey);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                    sparseArray.delete(notifyKey);
//                }
                tempBaseRspCmd=BeanFactory.createBean(notifyKey,0);
            }
            if(tempBaseRspCmd!=null){
                boolean needNext = tempBaseRspCmd.acceptData(Arrays.copyOfRange(data, 1, data.length - 1));//去掉CRC和KEY
                if (needNext) {
                    tempRspDataSparseArray.put(notifyKey, tempBaseRspCmd);//缓存起来，等下一个包
                    return true;//需要下一个数据包，接着等待
                }
                iOpResponse.onDataResponse(tempBaseRspCmd);
                tempRspDataSparseArray.delete(notifyKey);
                return true;
            }
        }
        return false;

    }


}
