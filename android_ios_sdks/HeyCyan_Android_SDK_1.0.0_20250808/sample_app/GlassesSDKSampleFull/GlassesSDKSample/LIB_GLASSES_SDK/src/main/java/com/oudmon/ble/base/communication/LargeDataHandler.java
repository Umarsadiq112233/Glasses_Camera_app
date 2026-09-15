package com.oudmon.ble.base.communication;
import static com.oudmon.ble.base.bluetooth.QCDataParser.TAG;

import android.util.Log;
import android.util.SparseArray;
import com.oudmon.ble.base.bluetooth.BleOperateManager;
import com.oudmon.ble.base.bluetooth.queue.BleDataBean;
import com.oudmon.ble.base.bluetooth.queue.BleThreadManager;
import com.oudmon.ble.base.communication.bigData.bean.GlassModelControl;
import com.oudmon.ble.base.communication.bigData.bean.SyncTime;
import com.oudmon.ble.base.communication.bigData.bean.WifiInfoReq;
import com.oudmon.ble.base.communication.bigData.resp.AiChatResponse;
import com.oudmon.ble.base.communication.bigData.resp.BaseResponse;
import com.oudmon.ble.base.communication.bigData.resp.BatteryResponse;
import com.oudmon.ble.base.communication.bigData.resp.ClassBluetoothResponse;
import com.oudmon.ble.base.communication.bigData.resp.DeviceInfoResponse;
import com.oudmon.ble.base.communication.bigData.resp.GlassModelControlResponse;
import com.oudmon.ble.base.communication.bigData.resp.GlassesAiVoicePlayStatusRsp;
import com.oudmon.ble.base.communication.bigData.resp.GlassesAiVoiceRsp;
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyListener;
import com.oudmon.ble.base.communication.bigData.resp.GlassesTouchSupportRsp;
import com.oudmon.ble.base.communication.bigData.resp.GlassesWearRsp;
import com.oudmon.ble.base.communication.bigData.resp.PictureThumbnailsResponse;
import com.oudmon.ble.base.communication.bigData.resp.SyncTimeResponse;
import com.oudmon.ble.base.communication.bigData.resp.VolumeControlResponse;
import com.oudmon.ble.base.communication.responseImpl.DeviceNotifyListener;
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.ble.base.communication.utils.CRC16;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.ble.base.request.WriteRequest;
import com.oudmon.qc_utils.bytes.DataTransferUtils;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gs ,
 * @date /5/12
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class LargeDataHandler {
    /***设备数据上报*/
    public static final byte ACTION_DEVICE_DATA_REPORTING = 0x73;

    public static final byte ACTION_SYNC_TIME = 0x40;
    /***眼镜控制*/
    public static final byte ACTION_GLASSES_CONTROL= 0x41;
    /***眼镜电量*/
    public static final byte ACTION_GLASSES_BATTERY= 0x42;
    /***眼镜设备信息*/
    public static final byte ACTION_DEVICE_INFO= 0x43;
    /***语音助手开关*/
    public static final byte ACTION_DEVICE_AI_VOICE= 0x44;
    /***心跳包*/
    public static final byte ACTION_DEVICE_HEART_BEAT= 0x45;
    /***佩戴检测开关*/
    public static final byte ACTION_DEVICE_WEAR= 0x46;
    /***佩戴支持*/
    public static final byte ACTION_DEVICE_WEAR_SUPPORT= 0x47;
    /***bt蓝牙地址*/
    public static final byte ACTION_BT_MAC_Protocol = 0x2e;
    /***WIFI soc ip*/
    public static final byte ACTION_OTA_SOC = (byte) 0xFC;
    /***GPT upload**/
    public static final byte ACTION_GPT_UPLOAD= 0x59;
    /***缩略图**/
    public static final byte ACTION_PICTURE_THUMBNAILS= (byte) 0xfd;
    /***ai 播报**/
    public static final byte ACTION_VOICE_STATUS= 0x48;
    /***BT 连接**/
    public static final byte ACTION_BT_CONNECT= 0x49;
    /***设置修改音量**/
    public static final byte ACTION_VOLUME_CONTROL= 0x51;

    /***杨声器从出**/
    public static final byte ACTION_SPEAK_SOUND_SWITCH= 0x52;

    private int mPackageLength;

    private GlassesDeviceNotifyListener deviceNotifyListener=new GlassesDeviceNotifyListener();
    private ConcurrentHashMap<String, ILargeDataResponse<BatteryResponse>> batterySparseArray = new ConcurrentHashMap<>();

    private static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_NOTIFY, new EnableNotifyRequest.ListenerCallback() {
        @Override
        public void enable(boolean result) {
            if (!result) {
                initEnable();
            }
        }
    });

    private ConcurrentHashMap<Integer, ILargeDataResponse> respMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ILargeDataResponse> noClearMap = new ConcurrentHashMap<>();

    private static LargeDataHandler mInstance;

    private LargeDataHandler() {
        mPackageLength = JPackageManager.getInstance().getLength();
        noClearMap.put(0x73, deviceNotifyListener);
    }

    public static   LargeDataHandler getInstance() {
        if (mInstance == null) {
            synchronized (LargeDataHandler.class) {
                if (mInstance == null) {
                    mInstance = new LargeDataHandler();
                }
            }
        }
        return mInstance;
    }

    public void initEnable() {
        enableNotifyRequest.setEnable(true);
        BleOperateManager.getInstance().execute(enableNotifyRequest);
        noClearMap.put(0x73, deviceNotifyListener);
    }


    public void disEnable() {
        enableNotifyRequest.setEnable(false);
        BleOperateManager.getInstance().execute(enableNotifyRequest);
    }

    public void packageLength() {
        mPackageLength = JPackageManager.getInstance().getLength();
    }

    public void initPackageNotify(ILargeDataResponse<AiChatResponse> listener){
        respMap.put(0x59,listener);
    }

    public void removeGptNotify(){
        respMap.remove(0x59);
    }

    public void addOutDeviceListener(int type, ILargeDataResponse outRspIOdmOpResponse) {
        deviceNotifyListener.setOutRspIOdmOpResponse(type, outRspIOdmOpResponse);
    }

    public void removeOutDeviceListener(int key) {
        deviceNotifyListener.removeCallback(key);
    }

    //眼镜写时间
    public void syncTime(ILargeDataResponse<SyncTimeResponse> response) {
        respMap.put((int) ACTION_SYNC_TIME,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_SYNC_TIME, new SyncTime(1).getSubData()), mPackageLength));
    }
    public void addBatteryCallBack(String key,ILargeDataResponse<BatteryResponse> listener){
        batterySparseArray.put(key,listener);
    }

    public void removeBatteryCallBack(String key){
        batterySparseArray.remove(key);
    }
    //眼镜读取电量
    public void syncBattery() {
        respMap.put((int) ACTION_GLASSES_BATTERY, new ILargeDataResponse<BatteryResponse>() {

            @Override
            public void parseData(int cmdType, BatteryResponse response) {
                for (ILargeDataResponse<BatteryResponse> outRspIOdmOpResponse : batterySparseArray.values()) {
                    outRspIOdmOpResponse.parseData(cmdType,response);
                }
            }
        });
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_GLASSES_BATTERY,new byte[0x02]), mPackageLength,200));
    }

    //开BT
    public void openBT() {
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_BT_CONNECT,new byte[]{0x02,0x01}), mPackageLength));
    }

    //设备音量控制
    public void setVolumeControl(int minMusic ,int maxMusic,int currMusic,int minCall ,int maxCall,int currCall,int systemMin ,int systemMax,int systemCurr,int currVolumeType) {
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_VOLUME_CONTROL,
                new byte[]{0x02,(byte) 0x01,(byte) minMusic, (byte) maxMusic, (byte) currMusic
                        , 0x02,(byte) minCall, (byte) maxCall, (byte) currCall
                        , 0x03,(byte) systemMin, (byte) systemMax, (byte) systemCurr, (byte) currVolumeType}), mPackageLength));
    }

    //设备音量控制
    public void getVolumeControl(ILargeDataResponse<VolumeControlResponse> response) {
        respMap.put((int) ACTION_VOLUME_CONTROL,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_VOLUME_CONTROL,new byte[]{0x01}), mPackageLength));
    }

    //给眼镜送IP
    public void writeIpToSoc(String url,ILargeDataResponse<BatteryResponse> response) {
        respMap.put((int) ACTION_OTA_SOC,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_OTA_SOC,new WifiInfoReq(url).getSubData()), mPackageLength));
    }

    //眼镜读取设备信息
    public void syncDeviceInfo(ILargeDataResponse<DeviceInfoResponse> response) {
        respMap.put((int) ACTION_DEVICE_INFO,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_INFO,new byte[0x02]), mPackageLength,200));
    }

    //语音助手开关
    public void aiVoiceWake(boolean write,boolean isOpen,ILargeDataResponse<GlassesAiVoiceRsp> response) {
        respMap.put((int) ACTION_DEVICE_AI_VOICE,response);
        if(write){
            byte[] data = new byte[2];
            data[0] =0x02;
            data[1] = (byte) (isOpen ? 0x01 : 0x00);
            BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_AI_VOICE,data), mPackageLength));
        }else {
            BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_AI_VOICE,new byte[]{0x01,0x00}), mPackageLength));
        }
    }

    //语音播报开关
    public void aiVoicePlay(int status, ILargeDataResponse<GlassesAiVoicePlayStatusRsp> response) {
        byte[] data = new byte[2];
        data[0] =0x02;
        data[1] = (byte) (status);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_VOICE_STATUS,data), mPackageLength));
    }

    public void wearCheck(boolean write,boolean isOpen,ILargeDataResponse<GlassesWearRsp> response) {
        respMap.put((int) ACTION_DEVICE_WEAR,response);
        if(write){
            byte[] data = new byte[2];
            data[0] =0x02;
            data[1] = (byte) (isOpen ? 0x01 : 0x00);
            BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_WEAR,data), mPackageLength));
        }else {
            BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_WEAR,new byte[]{0x01,0x00}), mPackageLength));
        }
    }

    public void wearFunctionSupport( ILargeDataResponse<GlassesTouchSupportRsp> response){
        respMap.put((int) ACTION_DEVICE_WEAR_SUPPORT,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_WEAR_SUPPORT,new byte[]{0x01,0x00}), mPackageLength));
    }

    //眼镜控制模式
    public void glassesControl(byte [] sendData,ILargeDataResponse<GlassModelControlResponse> response) {
        respMap.put((int) ACTION_GLASSES_CONTROL,response);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_GLASSES_CONTROL, new GlassModelControl(sendData).getSubData()), mPackageLength));
    }

    //眼镜控制模式
    public void speakSoundSwitch(boolean phone) {
        byte[] data = new byte[2];
        data[0] =0x02;
        if(phone){
            data[1] =0x02;
        }else {
            data[1] =0x01;
        }
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_SPEAK_SOUND_SWITCH, data), mPackageLength));
    }

    public void removeGlassesControlCallback(){
        respMap.remove((int)ACTION_GLASSES_CONTROL);
    }


    public void getPictureThumbnails(ILargeDataImageResponse listener) {
        respMap.put((int) ACTION_PICTURE_THUMBNAILS, new ILargeDataResponse<PictureThumbnailsResponse>() {
            @Override
            public void parseData(int cmdType, PictureThumbnailsResponse response) {
                 Log.i(TAG,ByteUtil.byteArrayToString(response.getSubData()));
                if ((cmdType &0xff) == (0xfd)) {
                    // Log.i(TAG,ByteUtil.byteArrayToString(data));
                    //bcfd 0004 2502 01 1600 0000 ffd8ffe000104a46494600010100000100010000ffdb00430006040506050406060506070706080a100a0a09090a140e0f0c1017141818171416161a1d251f1a1b231c1616202c20232627292a29191f2d302d283025282928ffdb0043010707070a080a130a0a13281a161a2828282828282828282828282828282828282828282828282828282828282828282828282828282828282828282828282828ffc00011080168028003012200
                    int total = (ByteUtil.bytesToInt(Arrays.copyOfRange(response.getSubData(), 7, 9)));
                    int currIndex = (ByteUtil.bytesToInt(Arrays.copyOfRange(response.getSubData(), 9, 11)));
                    Log.i(TAG,currIndex+"--"+total);
                    if(total<=0){
                        return;
                    }
                    if(currIndex+1 == total){
                        listener.parseData(cmdType,true,Arrays.copyOfRange(response.getSubData(), 11, response.getSubData().length));
                    }else {
                        syncPictureThumbnails(++currIndex);
                        listener.parseData(cmdType,false,Arrays.copyOfRange(response.getSubData(), 11, response.getSubData().length));
                    }
                }
            }
        });
        syncPictureThumbnails(0);
    }

    private void syncPictureThumbnails(int index){
        byte[] cmdData = new byte[3];
        cmdData[0]=0x01;
        cmdData[1]= (byte) ByteUtil.loword(index);
        cmdData[2]= (byte) ByteUtil.hiword(index);
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_PICTURE_THUMBNAILS, cmdData), mPackageLength));
    }

    public void syncHeartBeat(int type){
        byte[] cmdData = new byte[2];
        cmdData[0]= (byte) type;
        cmdData[1]=0x01;
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_DEVICE_HEART_BEAT, cmdData), mPackageLength));
    }

    //同步经典蓝牙
    public void syncClassicBluetooth(ILargeDataResponse<ClassBluetoothResponse> response) {
        respMap.put((int) ACTION_BT_MAC_Protocol,response);
        byte[] cmdData = new byte[1];
        BleThreadManager.getInstance().addData(new BleDataBean(addHeader(ACTION_BT_MAC_Protocol, cmdData), mPackageLength));
    }


    public ConcurrentHashMap<Integer, ILargeDataResponse> getRespMap() {
        return respMap;
    }

    public ConcurrentHashMap<Integer, ILargeDataResponse> getNoClearMap() {
        if(noClearMap.isEmpty()){
            noClearMap.put(0x73, deviceNotifyListener);
        }
        return noClearMap;
    }

    public void cleanMap() {
        respMap.clear();
    }

    /**
     * 将要传送的Data转换成一个可以发送的包
     *
     * @param cmdId 指令ID
     * @param data  要发送的数据data
     * @return 转换后的包
     */
    private byte[] addHeader(int cmdId, byte[] data) {
        byte[] pocket = new byte[(data == null ? 0 : data.length) + 6];
        pocket[0] = (byte) 0xbc;
        pocket[1] = (byte) cmdId;
        if (data != null && data.length > 0) {
            System.arraycopy(DataTransferUtils.shortToBytes((short) data.length), 0, pocket, 2, 2);     //pocket[2], pocket[3]为数据长度信息
            System.arraycopy(DataTransferUtils.shortToBytes((short) CRC16.calcCrc16(data)), 0, pocket, 4, 2);   //pocket[4], pocket[5]为CRC校验
            System.arraycopy(data, 0, pocket, 6, data.length);  //pocket[6]以后为数据data
        } else {
            pocket[4] = (byte) 0xff;
            pocket[5] = (byte) 0xff;
        }
        return pocket;
    }

    private WriteRequest getWriteRequest(byte[] data) {
         Log.i(TAG, "getWriteRequest: data=" + DataTransferUtils.getHexString(data));
        WriteRequest noRspInstance = WriteRequest.getNoRspInstance(SERIAL_PORT_SERVICE, SERIAL_PORT_CHARACTER_WRITE);
        noRspInstance.setValue(data);
        return noRspInstance;
    }



}
