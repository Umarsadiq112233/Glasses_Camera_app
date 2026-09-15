package com.oudmon.ble.base.bluetooth;
import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.oudmon.ble.base.communication.CommandHandle;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.responseImpl.DeviceNotifyListener;
import com.oudmon.ble.base.communication.responseImpl.DeviceSportNotifyListener;
import com.oudmon.ble.base.communication.responseImpl.InnerCameraNotifyListener;
import com.oudmon.ble.base.communication.responseImpl.MusicCommandListener;
import com.oudmon.ble.base.communication.responseImpl.PackageLengthListener;
import com.oudmon.ble.base.request.BaseRequest;
import com.oudmon.ble.base.request.EnableNotifyRequest;
import com.oudmon.ble.base.request.LocalWriteRequest;
import com.oudmon.qc_utils.bluetooth.BluetoothUtils;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 * Refactor by hzy on 202012
 */
public class BleOperateManager extends HandlerThread implements IBleListener {
    private static final String TAG = "DeviceOperateManager";
    private static BleOperateManager DeviceOperateManager = null;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Handler workThreadHandler;
    private final Object mLock = new Object();
    private boolean mRequestCompleted = false;
    private final Context mContext;
    private ConcurrentHashMap<Integer, LocalWriteRequest> localWriteRequestConcurrentHashMap = new ConcurrentHashMap<>();
    private SparseArray<ICommandResponse> notifySparseArray = new SparseArray<>();
    private InnerCameraNotifyListener innerCameraNotifyListener;
    private OnGattEventCallback callback;
    private String reConnectMac;
    private DeviceNotifyListener deviceNotifyListener = new DeviceNotifyListener();
    private Application application;
    private FirmwareRunnable timeoutFirmwareRunnable=new FirmwareRunnable();
    private DeviceSportNotifyListener deviceSportNotifyListener = new DeviceSportNotifyListener();

    private boolean ready;



    public static BleOperateManager getInstance(Application application) {
        if (DeviceOperateManager == null) {
            synchronized (BleOperateManager.class) {
                if (DeviceOperateManager == null) {
                    DeviceOperateManager = new BleOperateManager(application);
                }
            }
        }
        return DeviceOperateManager;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getReConnectMac() {
        return reConnectMac;
    }

    public void setReConnectMac(String reConnectMac) {
        this.reConnectMac = reConnectMac;
    }

    public void setCallback(OnGattEventCallback callback) {
        this.callback = callback;
    }

    public static BleOperateManager getInstance() {
        return DeviceOperateManager;
    }

    private BleOperateManager(Context context) {
        super("DeviceOperateManager");
        this.mContext = context;
        BleBaseControl.getInstance(context).setListener(this);
//        innerCameraNotifyListener = new InnerCameraNotifyListener(context);
//        notifySparseArray.put(Constants.CMD_MUSIC_COMMAND, new MusicCommandListener(context));
//        notifySparseArray.put(Constants.CMD_TAKING_PICTURE, innerCameraNotifyListener);
        notifySparseArray.put(Constants.CMD_PACKAGE_LENGTH, new PackageLengthListener());
//        notifySparseArray.put(Constants.CMD_DEVICE_NOTIFY, deviceNotifyListener);
//        notifySparseArray.put(Constants.CMD_PHONE_SPORT_N0TIFY, deviceSportNotifyListener);

        this.start();
        workThreadHandler = new Handler(getLooper());
    }

    public ConcurrentHashMap<Integer, LocalWriteRequest> getLocalWriteRequestConcurrentHashMap() {
        return localWriteRequestConcurrentHashMap;
    }


    public void setBluetoothTurnOff(boolean onOrOff) {
        BleBaseControl.getInstance().setBluetoothTurnOff(onOrOff);
    }

    public void addOutCameraListener(ICommandResponse outRspIOdmOpResponse) {
        innerCameraNotifyListener.setOutRspIOdmOpResponse(outRspIOdmOpResponse);
    }

    public void removeOutCameraListener() {
        innerCameraNotifyListener.setOutRspIOdmOpResponse(null);
    }


    public void addSportDeviceListener(int type, ICommandResponse outRspIOdmOpResponse) {
        deviceSportNotifyListener.setOutRspIOdmOpResponse(type, outRspIOdmOpResponse);
    }

    public void removeSportDeviceListener(int key) {
        deviceSportNotifyListener.removeCallback(key);
    }

    public void addOutDeviceListener(int type, ICommandResponse outRspIOdmOpResponse) {
        deviceNotifyListener.setOutRspIOdmOpResponse(type, outRspIOdmOpResponse);
    }

    public void removeOutDeviceListener(int key) {
        deviceNotifyListener.removeCallback(key);
    }

    public void removeOthersListener() {
        deviceNotifyListener.removeOtherCallbacks();
    }

    public boolean addNotifyListener(int key, ICommandResponse iOpResponse) {
        if (iOpResponse == null) return false;
        notifySparseArray.put(key, iOpResponse);
        return true;
    }

    public void removeNotifyListener(int key) {
        notifySparseArray.delete(key);
    }


    public SparseArray<ICommandResponse> getNotifySparseArray() {
        return notifySparseArray;
    }

    /***
     * 只有解除绑定的时候，将这些信息部清除
     */
    public void unBindDevice() {
        setNeedConnect(false);
        disconnect();
        setMacNull();
    }

    /***
     * 将手环的逻辑地址置空
     */
    public void setMacNull() {
        BleBaseControl.getInstance().setmDeviceAddress(null);
    }

    /***
     * 直接连接
     * @param macAddress
     */
    public void connectDirectly(String macAddress) {
        BleBaseControl.getInstance().connect(macAddress);
    }

    public void connectWithScan(String macAddress) {
        if (TextUtils.isEmpty(macAddress)) {
            return;
        }
        if (TextUtils.isEmpty(reConnectMac)) {
            return;
        }
        BleBaseControl.getInstance().setNeedReconnect(true);
        BleBaseControl.getInstance().setmDeviceAddress(macAddress);
        BleBaseControl.getInstance().reconnectOpeningUp();
    }

    public void disconnect() {
        ready=false;
        Log.e(TAG, "disconnect...");
        BleBaseControl.getInstance().disconnectDevice(BleBaseControl.getInstance().getmDeviceAddress());
    }

    /***
     * 设置是否需要重连，只有用户手动解除绑定的时候不能重连，其它情况都要进行重连
     * @param needConnect
     */
    public void setNeedConnect(boolean needConnect) {
        BleBaseControl.getInstance().setNeedReconnect(needConnect);
    }


    @Override
    public boolean execute(final BaseRequest baseCharAction) {
        if (!BluetoothUtils.isEnabledBluetooth(mContext)) {
            Log.e(TAG, "connectDirectly: 蓝牙未打开");
            return false;
        }
        if (!BleBaseControl.getInstance().ismIsConnected()) {
            Log.e(TAG, "ismIsConnected: false");
            return false;
        }
        if(baseCharAction.writeRequest){
            if(!ready){
                try {
//                    BluetoothGattCharacteristic charCharacteristic = BleBaseControl.getInstance().findTheGattCharacteristic(baseCharAction.getServiceUuid(), baseCharAction.getCharUuid());
//                     XLog.i( ByteUtil.byteArrayToString(charCharacteristic.getValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        workThreadHandler.postDelayed(runnable, 5 * 1000);
        workThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    try {
                        if (baseCharAction.needInitCharacteristic()) {
                            BluetoothGattCharacteristic charCharacteristic = BleBaseControl.getInstance().findTheGattCharacteristic(baseCharAction.getServiceUuid(), baseCharAction.getCharUuid());
                            if (charCharacteristic == null) {
                                return;
                            }
                            boolean result = baseCharAction.execute(BleBaseControl.getInstance().getGatt(BleBaseControl.getInstance().getmDeviceAddress()), charCharacteristic);
                            if (!result) {
                                notifyLock();
                            }
                        } else {
                            boolean result = baseCharAction.execute(BleBaseControl.getInstance().getGatt(BleBaseControl.getInstance().getmDeviceAddress()), null);
                            Log.e(TAG,  result+"");
                            if (!result) {
                                notifyLock();
                            }
                        }
                        mRequestCompleted = false;
                        if (BleBaseControl.getInstance().ismIsConnected()) {
                            waitUntilActionResponse();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        return true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,  "---lock timeout---");
            mRequestCompleted = true;
            notifyLock();
        }
    };


    private void waitUntilActionResponse() {
        try {
//             XLog.i(mRequestCompleted);
            while (!mRequestCompleted) {
                mLock.wait();
            }
        } catch (Exception e) {
            Log.e(TAG, "Sleeping interrupted", e);
        }
    }

    protected void notifyLock() {
        workThreadHandler.removeCallbacks(runnable);
        synchronized (mLock) {
            mRequestCompleted = true;
            mLock.notifyAll();
        }
    }


    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        notifyLock();
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        notifyLock();
    }

    @Override
    public boolean isConnected() {
        boolean connect=BleBaseControl.getInstance().ismIsConnected();
        if(!connect){
            ready=false;
        }
        return connect;
    }

    public boolean isReady() {
        return ready;
    }


    public void setReady(boolean ready) {
        mainThreadHandler.removeCallbacks(timeoutFirmwareRunnable);
        if(!this.ready){
            mainThreadHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(BleAction.BLE_SERVICE_DISCOVERED);
                    intent.putExtra(BleAction.EXTRA_ADDR, BleBaseControl.getInstance().getmDeviceAddress());
                    mySendBroadcast(intent);
                }
            }, 2500);
            this.ready = ready;
        }else {
            Log.e(TAG, "已经更新过版本号");
        }

    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        notifyLock();
    }

    @Override
    public void startConnect() {
        Intent intent = new Intent(BleAction.BLE_START_CONNECT);
        mySendBroadcast(intent);
    }

    @Override
    public void bleGattConnected(BluetoothDevice device) {
        Intent intent = new Intent(BleAction.BLE_GATT_CONNECTED);
        intent.putExtra(BleAction.EXTRA_DEVICE, device);
        intent.putExtra(BleAction.EXTRA_ADDR, device.getAddress());
        mySendBroadcast(intent);
    }

    @Override
    public void bleGattDisconnect(BluetoothDevice device) {
        Intent intent = new Intent(BleAction.BLE_GATT_DISCONNECTED);
        intent.putExtra(BleAction.EXTRA_DEVICE, device);
        intent.putExtra(BleAction.EXTRA_ADDR, device.getAddress());
        mySendBroadcast(intent);
        synchronized (mLock) {
            localWriteRequestConcurrentHashMap.clear();
            notifyLock();
        }
    }

    @Override
    public void bleServiceDiscovered(int state, final String address) {
        Log.e(TAG, "---------bleServiceDiscovered address");
        enableUUID();
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runCommonCmd();
            }
        }, 500);

        mainThreadHandler.postDelayed(timeoutFirmwareRunnable, 1000);
        mainThreadHandler.postDelayed(timeoutFirmwareRunnable, 1500);
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BleAction.BLE_SERVICE_DISCOVERED);
                intent.putExtra(BleAction.EXTRA_ADDR, address);
                mySendBroadcast(intent);
            }
        }, 1000);
    }

    public class FirmwareRunnable implements Runnable{

        @Override
        public void run() {
            runCommonCmd();
        }
    }



    @Override
    public void bleStatus(int status, int newState) {
        Intent intent = new Intent(BleAction.BLE_STATUS);
        intent.putExtra(BleAction.EXTRA_BLE_STATUS, status);
        intent.putExtra(BleAction.EXTRA_BLE_NEW_STATE, newState);
        mySendBroadcast(intent);
    }

    @Override
    public void bleCharacteristicRead(String address, String uuid, int status, byte[] value) {
        Intent intent = new Intent(BleAction.BLE_CHARACTERISTIC_READ);
        intent.putExtra(BleAction.EXTRA_ADDR, address);
        intent.putExtra(BleAction.EXTRA_CHARACTER_UUID, uuid);
        intent.putExtra(BleAction.EXTRA_STATUS, status);
        intent.putExtra(BleAction.EXTRA_VALUE, value);
        mySendBroadcast(intent);
        notifyLock();
    }

    @Override
    public void bleCharacteristicNotification() {
        Intent intent = new Intent(BleAction.BLE_CHARACTERISTIC_DISCOVERED);
        mySendBroadcast(intent);
        notifyLock();
    }

    @Override
    public void bleCharacteristicWrite(String address, String uuid, int status, byte[] data) {
        Intent intent = new Intent(BleAction.BLE_CHARACTERISTIC_WRITE);
        intent.putExtra(BleAction.EXTRA_ADDR, address);
        intent.putExtra(BleAction.EXTRA_CHARACTER_UUID, uuid);
        intent.putExtra(BleAction.EXTRA_STATUS, status);
        intent.putExtra(BleAction.EXTRA_DATA, data);
        mySendBroadcast(intent);
        notifyLock();
    }

    @Override
    public void bleCharacteristicChanged(String address, String uuid, byte[] value) {
        Intent intent = new Intent(BleAction.BLE_CHARACTERISTIC_CHANGED);
        intent.putExtra(BleAction.EXTRA_ADDR, address);
        intent.putExtra(BleAction.EXTRA_CHARACTER_UUID, uuid);
        intent.putExtra(BleAction.EXTRA_VALUE, value);
        mySendBroadcast(intent);
        if (callback != null) {
            callback.onReceivedData(uuid, value);
        }
    }

    @Override
    public void bleNoCallback() {
        Intent intent = new Intent(BleAction.BLE_NO_CALLBACK);
        mySendBroadcast(intent);
    }

    private void mySendBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void enableUUID() {
        mainThreadHandler.removeCallbacks(runnableEnable);
        mainThreadHandler.postDelayed(runnableEnable, 4 * 1000);
        EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(Constants.UUID_SERVICE, Constants.UUID_READ, new EnableNotifyRequest.ListenerCallback() {
            @Override
            public void enable(boolean result) {
                Log.e(TAG, "enableUUID:"+result);
                mainThreadHandler.removeCallbacks(runnableEnable);
            }
        });
        enableNotifyRequest.setEnable(true);
        DeviceOperateManager.getInstance().execute(enableNotifyRequest);
    }

    Runnable runnableEnable = new Runnable() {
        @Override
        public void run() {
            mRequestCompleted = true;
            notifyLock();
            EnableNotifyRequest enableNotifyRequest = new EnableNotifyRequest(Constants.UUID_SERVICE, Constants.UUID_READ, new EnableNotifyRequest.ListenerCallback() {
                @Override
                public void enable(boolean result) {
                    Log.e(TAG, "enableUUID:"+result);
                    mainThreadHandler.removeCallbacks(runnableEnable);
                }
            });
            enableNotifyRequest.setEnable(true);
            DeviceOperateManager.getInstance().execute(enableNotifyRequest);
        }
    };

    public void init() {
        IntentFilter intentFilter = BleAction.getIntentFilter();
        QCBluetoothCallbackReceiver bluetoothDataParseReceiver = new QCBluetoothCallbackReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(bluetoothDataParseReceiver, intentFilter);

        QCBluetoothCallbackCloneReceiver cloneReceiver = new QCBluetoothCallbackCloneReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(cloneReceiver, intentFilter);

        QCBluetoothCallbackBigDataCloneReceiver bigDataClone = new QCBluetoothCallbackBigDataCloneReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(bigDataClone, intentFilter);
    }

    private void runCommonCmd() {
        //硬件信息
        CommandHandle.getInstance().execReadCmd(CommandHandle.getInstance().getReadHwRequest());
        //固件信息
        CommandHandle.getInstance().execReadCmd(CommandHandle.getInstance().getReadFmRequest());
    }

    public void classicBluetoothStartScan() {
        BleBaseControl.getInstance().classicBluetoothScan();
    }

    public void classicBluetoothStopScan() {
        BleBaseControl.getInstance().cancelScanBluetooth();
    }

    public void createBondBlueTooth(BluetoothDevice device) {
        BleBaseControl.getInstance().createBondBlueTooth(device);
    }

    public void createBondBluetoothJieLi(BluetoothDevice device){
        BleBaseControl.getInstance().createBond(device,1);
    }


    public void setRtkBindTag(Boolean rtkBindTag) {
        BleBaseControl.getInstance().setRtkBindTag(rtkBindTag);
    }

    public BluetoothDevice getMacSystemBond(String address){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
        if (devices1.size()>0) {
            for(Iterator<BluetoothDevice> iterator = devices1.iterator(); iterator.hasNext();){
                BluetoothDevice bluetoothDevice=(BluetoothDevice)iterator.next();
                if(bluetoothDevice==null || bluetoothDevice.getName()==null || bluetoothDevice.getAddress()==null){
                    continue;
                }
                if(!TextUtils.isEmpty(address)){
                    if(address.equalsIgnoreCase(bluetoothDevice.getAddress())){
                        return bluetoothDevice;
                    }
                }
            }
        }
        return null;
    }

    public void bleCreateBond(){
        BleBaseControl.getInstance().bleCreateBond();
    }

    @SuppressLint("MissingPermission")
    public void createBondBluetoothJieLiFailRepeat(BluetoothDevice device){
        try {
            device.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
