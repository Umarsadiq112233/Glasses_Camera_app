package com.oudmon.ble.base.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_NONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import com.oudmon.ble.base.bluetooth.queue.BleThreadManager;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.scan.BleScannerCompat;
import com.oudmon.ble.base.scan.BleScannerHelper;
import com.oudmon.ble.base.scan.OnTheScanResult;
import com.oudmon.ble.base.util.AppUtil;

import com.oudmon.qc_utils.bluetooth.BluetoothUtils;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 *
 * @author gs james 20200807
 */

public class BleBaseControl {
    private static final String TAG = "GLASSES_LOG";
    private static BleBaseControl bleBaseControl;
    /**
     * 关闭Gatt延迟时间
     **/
    private static final int GATT_CLOSE_DELAY_MILLIS = 500;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    /***锁对象***/
    private final Object mLock = new Object();
    private String mDeviceAddress;
    /***是否连接,不需要要其它状态****/
    private volatile boolean mIsConnected;
    /***是否正在连接**/
    private volatile boolean connecting;
    /**
     * gatt 对象缓存
     **/
    protected Map<String, BluetoothGatt> mBluetoothGatt = new HashMap<>();
    /**
     * 是否要主动重连，如果是用户解绑此处为false
     ***/
    private boolean isNeedReconnect = true;
    /***默认重连次数**/
    private int maxReconnect = 10;
    /***最大失败交数**/
    private int maxFail = 6;
    /**
     * 原子操作重连次数
     */
    private AtomicInteger count = new AtomicInteger(0);
    /**
     * 失败次数记数器
     **/
    private AtomicInteger failCount = new AtomicInteger(0);
    /**
     * 给外部监听的接口
     **/
    private IBleListener listener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 蓝牙开关
     **/
    private boolean bluetoothTurnOff = false;
    private HashMap<UUID, BluetoothGattCharacteristic> cacheGattCharacteristicHashMap = new HashMap<>();

    private SystemProxyTimeoutRunnable systemProxyTimeoutRunnable = new SystemProxyTimeoutRunnable();
    private Boolean rtkBindTag = false;

    BluetoothDevice bleConnectDevice;

    public void setBluetoothTurnOff(boolean bluetoothTurnOff) {
        this.bluetoothTurnOff = bluetoothTurnOff;
        if (!bluetoothTurnOff) {
            mIsConnected = false;
            connecting = false;
        }
    }

    public boolean ismIsConnected() {
        return mIsConnected;
    }

    public static BleBaseControl getInstance() {
        return bleBaseControl;
    }


    public static BleBaseControl getInstance(Context context) {
        if (bleBaseControl == null) {
            synchronized (BleBaseControl.class) {
                if (bleBaseControl == null) {
                    bleBaseControl = new BleBaseControl(context);
                }
            }
        }
        return bleBaseControl;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 传入的context最好是application的或者service的，不要传人activity，防止内存泄漏
     *
     * @param context c
     */
    private BleBaseControl(Context context) {
        this.mContext = context;
        initialize();
    }

    private void initialize() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            Log.e(TAG, "Unable to initialize BluetoothManager...");
        }
    }

    /***
     * 连接状态回调
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            Log.i(TAG,"onConnectionStateChange-->status = [" + status + "], newState = [" + newState + "]");
            if (listener != null) {
                listener.bleStatus(status, newState);
            }
            String address = gatt.getDevice().getAddress();
            bleConnectDevice = gatt.getDevice();
            mHandler.removeCallbacks(mDiscoverServiceTimeoutRunnable);
            mHandler.removeCallbacks(mTimeoutRunnable);
            BleThreadManager.getInstance().clean();
            if (status != BluetoothGatt.GATT_SUCCESS) {
                notifyMyAll();
                mIsConnected = false;
                if (listener != null) {
                    listener.bleGattDisconnect(gatt.getDevice());
                }
                disconnectDevice(address);
                failCount.incrementAndGet();
//                if (status == 133 || status == 257) {
//                    disconnectDevice(address);
//                    reconnectFromStateChangeNoAutoConnect();
//                     Log.i(TAG,notScanFailCount.get());
//                } else {
//                    boolean background = AppUtil.isBackground(mContext);
//                    boolean background1 = AppUtil.isApplicationBroughtToBackground(mContext);
//                    if(background || background1){
//                        disconnectDeviceNotClose(address);
//                        reconnectFromStateChange(address);
//                    }else {
//                        disconnectDevice(address);
//                        reconnectFromStateChangeNoAutoConnect();
//                         Log.i(TAG,notScanFailCount.get());
//                    }
//                }
                reconnectFromStateChangeNoAutoConnect();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                cacheGattCharacteristicHashMap.clear();
                mHandler.removeCallbacks(mReconnectRunnable);
                waitFor(500);
                boolean discoverServices = gatt.discoverServices();
                if (discoverServices) {
                    mHandler.removeCallbacks(mDiscoverServiceTimeoutRunnable);
                    mHandler.postDelayed(mDiscoverServiceTimeoutRunnable, 40000);
                } else {
                    waitFor(1000);
                    boolean discoverServices1 = gatt.discoverServices();
                    Log.i(TAG,"-------1---" + discoverServices1);
                    if (!discoverServices1) {
                        disconnectDevice(address);
                        return;
                    }
                }
                count.getAndSet(0);
                failCount.getAndSet(0);
                mIsConnected = true;
                bluetoothTurnOff = false;
                mHandler.removeCallbacks(mReconnectRunnable);
                if (listener != null) {
                    listener.bleGattConnected(gatt.getDevice());
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mIsConnected = false;
                notifyMyAll();
                disconnectDevice(address);
                reconnectDevice();
                if (listener != null) {
                    listener.bleGattDisconnect(gatt.getDevice());
                }
            }
        }

        /**
         * 断开连接
         */
        public void disconnectDeviceNotClose(String address) {
            try {
                connecting = false;
                mIsConnected = false;
                Log.i(TAG,address + "  gatt map size:" + mBluetoothGatt.size());
                if (mBluetoothGatt.containsKey(address)) {
                    final BluetoothGatt gatt = mBluetoothGatt.get(address);
                    if (gatt != null) {
                        Log.i(TAG,"gatt disconnect it");
                        gatt.disconnect();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshDeviceCache(gatt);
                            }
                        }, GATT_CLOSE_DELAY_MILLIS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /***
         * 如果是异常断开，这个时候，做下延时再重连
         */
        private void reconnectFromStateChangeNoAutoConnect() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reconnectDevice();
                }
            }, 2000);
        }


        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            String address = gatt.getDevice().getAddress();
            mHandler.removeCallbacks(mDiscoverServiceTimeoutRunnable);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectDevice(address);
                return;
            }
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService bluetoothGattService : services) {
                Log.i(TAG,"servicesUUID:" + bluetoothGattService.getUuid().toString());
            }
            connecting = false;
            if (listener != null) {
                listener.bleServiceDiscovered(status, address);
            }
        }

        // Other methods just pass the parameters through
        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG,"a->w：" + DataTransferUtils.getHexString(characteristic.getValue()));
            if (listener != null) {
                listener.bleCharacteristicWrite(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), status, characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (listener != null) {
                listener.bleCharacteristicRead(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), status, characteristic.getValue());
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG,"w->a：" + DataTransferUtils.getHexString(characteristic.getValue()));
            if (listener != null) {
                listener.bleCharacteristicChanged(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), characteristic.getValue());
            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (listener != null) {
                listener.onDescriptorWrite(gatt, descriptor, status);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                checkIsNotifyConfigAndRegisterCallback(descriptor, gatt);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (listener != null) {
                listener.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (listener != null) {
                listener.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG,mtu + "");
        }
    };


    private class SystemProxyTimeoutRunnable implements Runnable {
        @Override
        public void run() {
            doConnectClone();
        }
    }


    /***
     * 如果是异常断开，这个时候，做下延时再重连
     */
    private void reconnectFromStateChange(String address) {
        mHandler.removeCallbacks(systemProxyTimeoutRunnable);
        mHandler.postDelayed(systemProxyTimeoutRunnable, 3000);
    }


    private void doConnectClone() {
        boolean background = AppUtil.isBackground(mContext);
        boolean background1 = AppUtil.isApplicationBroughtToBackground(mContext);
        if (background || background1) {
            Log.i(TAG,"后台重连调用");
            doConnect();
//            if(bluetoothTurnOff || systemProxy){
//               Log.i(TAG,"后台重连调用");
//                doConnect();
//            }else {
//               Log.i(TAG,"系统代理调用");
//                final BluetoothGatt gatt = mBluetoothGattClone.get(mDeviceAddress);
//                try {
//                    if (gatt != null) {
//                        connecting = true;
//                        mIsConnected = false;
//                        boolean autoConnect = gatt.connect();
//                        if (!autoConnect) {
//                            reconnectDevice();
//                            mBluetoothGattClone.remove(mDeviceAddress);
//                           Log.i(TAG, "系统托管报错移除");
//                        }
//                        count.incrementAndGet();
//                        mHandler.removeCallbacks(systemProxyRunnable);
//                        mHandler.postDelayed(systemProxyRunnable,30*1000);
//                       Log.i(TAG, "系统托管");
//                    }else {
//                       Log.i(TAG,"系统托管报错，走正常gatt ==null");
//                        doConnect();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    connecting = false;
//                    mIsConnected = false;
//                    count.incrementAndGet();
//                    reconnectDevice();
//                    mBluetoothGattClone.remove(mDeviceAddress);
//                     Log.i(TAG, "系统托管异常走外部正常连接");
//                }
//            }
        } else {
            Log.i(TAG,"前台重连调用");
            BleScannerHelper.getInstance().scanTheDevice(mContext, mDeviceAddress, new OnTheScanResult() {
                @Override
                public void onResult(BluetoothDevice bluetoothDevice) {
                    if (count.get() >= maxReconnect) {
                        count.getAndSet(0);
                    }
                    if (bluetoothDevice != null) {
                        mHandler.postDelayed(mReconnectRunnable, 200);
                    } else {
                        connecting = false;
                        count.incrementAndGet();
                        doConnect();
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    connecting = false;
                    count.incrementAndGet();
                    doConnect();
                }
            });
        }
    }


    /***
     * 外部调用
     */
    public void reconnectOpeningUp() {
        if (count.get() >= maxReconnect) {
            count.getAndSet(0);
        }
        if (failCount.get() >= maxFail) {
            failCount.getAndSet(0);
        }
        isNeedReconnect = true;
        reconnectDevice();
    }

    /****
     * 重连操作
     */
    private void reconnectDevice() {
        synchronized (BleBaseControl.class) {
            /***用户主动断开的，不需要重连***/
            if (!isNeedReconnect) {
                connecting = false;
                return;
            }
            /***蓝牙没开,或者连接地址为空，不需要重连***/
            if (!BluetoothUtils.isEnabledBluetooth(mContext) || TextUtils.isEmpty(mDeviceAddress)) {
                mIsConnected = false;
                connecting = false;
                return;
            }
            if (failCount.get() >= maxFail) {
                mIsConnected = false;
                connecting = false;
                Log.i(TAG,"内部失败循环大于" + maxFail + "次直接返回");
                return;
            }
            if (isConnecting() || ismIsConnected()) {
                Log.i(TAG,"正在连接:" + isConnecting() + " 已经连上:" + ismIsConnected());
                return;
            }
            mHandler.removeCallbacks(mReconnectRunnable);
            if (BleScannerCompat.getScanner(mContext).isScanning()) {
                return;
            }
            doConnectClone();
        }
    }


    private void doConnect() {
        /***用户主动断开的，不需要重连***/
        if (!isNeedReconnect) {
            connecting = false;
            return;
        }
        if (count.get() % 3 == 0 && !bluetoothTurnOff) {
            mHandler.postDelayed(mReconnectRunnable, 1000);
            Log.i(TAG,"--直连");
        } else {
            Log.i(TAG,"--扫连");
            bluetoothTurnOff = false;
            BleScannerHelper.getInstance().scanTheDevice(mContext, mDeviceAddress, new OnTheScanResult() {
                @Override
                public void onResult(BluetoothDevice bluetoothDevice) {
                    if (count.get() >= maxReconnect) {
                        count.getAndSet(0);
                    }
                    if (bluetoothDevice != null) {
                        mHandler.postDelayed(mReconnectRunnable, 200);
                    } else {
                        connecting = false;
                        count.incrementAndGet();
                        doConnect();
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    connecting = false;
                    count.incrementAndGet();
                    doConnect();
                }
            });
        }
    }

    /***
     * 连接方法
     * @param address
     * @return
     */
    public boolean connect(final String address) {
        if (!BluetoothUtils.isEnabledBluetooth(mContext)) {
            connecting = false;
            isNeedReconnect = false;
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            Log.i(TAG,"address 空返回");
            mIsConnected = false;
            connecting = false;
            return false;
        }
        if (isConnecting() || ismIsConnected()) {
            Log.i(TAG,"再次检查的时候返回了");
            return false;
        }
        mHandler.removeCallbacks(mTimeoutRunnable);
        mHandler.postDelayed(mTimeoutRunnable, 40000);

        connecting = true;
        isNeedReconnect = true;
        BleScannerHelper.getInstance().stopScan(mContext);
        mDeviceAddress = address;
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        BluetoothGatt gatt;
        Log.i(TAG,"---------------【开始GATT连接】--------------");


        if (listener != null) {
            listener.startConnect();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            gatt = device.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            gatt = device.connectGatt(mContext, false, mGattCallback);
        }
        /***将gatt缓存**/
        if (gatt == null) {
            mBluetoothGatt.remove(address);
            return false;
        } else {
            mBluetoothGatt.put(address, gatt);
            return true;
        }
    }


    /****
     * 发现服务超时处理
     */
    private Runnable mDiscoverServiceTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            disconnectDevice(mDeviceAddress);
        }
    };


    /**
     * 断开连接
     */
    public void disconnectDevice(String address) {
        try {
            connecting = false;
            mIsConnected = false;
            Log.i(TAG,address + "  gatt map size:" + mBluetoothGatt.size());
            for (final BluetoothGatt gatt : mBluetoothGatt.values()
            ) {
                if (gatt != null) {
                    Log.i(TAG,"gatt disconnect it");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    gatt.disconnect();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshDeviceCache(gatt);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                            }
                            gatt.close();
                        }
                    }, GATT_CLOSE_DELAY_MILLIS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BluetoothGatt getGatt(String address) {
        if (mBluetoothGatt.containsKey(address)) {
            BluetoothGatt gatt = mBluetoothGatt.get(address);
            if (gatt == null) {
                return null;
            } else {
                return gatt;
            }
        } else {
            return null;
        }
    }


    private void notifyMyAll() {
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

    private void waitFor(long time) {
        synchronized (mLock) {
            try {
                mLock.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 没有收系统回调的超时任务
     ****/
    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mIsConnected = false;
            connecting = false;
            Log.i(TAG,"没有收到系统回调，直接断开");
            disconnectDevice(mDeviceAddress);
            if (listener != null) {
                listener.bleNoCallback();
            }
        }
    };


    private Runnable mReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (count.get() < maxReconnect) {
                count.incrementAndGet();
                Log.i(TAG,"正在重连,重连次数：" + count.get());
                connect(mDeviceAddress);
            } else {
                /**连接重试多次没有连接上，如果没有外部的重连唤醒，这时候就不用去再尝试连接，等待外部重连，将原子变量初始化**/
                connecting = false;
                Log.i(TAG,"超出了重连次数:" + count.get());
            }
        }
    };


    /**
     * Clears the device cache. After uploading new hello4 the DFU target will
     * have other services than before.
     */
    protected boolean refreshDeviceCache(BluetoothGatt gatt) {
        if (!BluetoothUtils.isEnabledBluetooth(mContext)) {
            return false;
        }
        /*
         * There is a refresh() method in BluetoothGatt class but for now it's
         * hidden. We will call it using reflections.
         */
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                Log.i(TAG,"Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.i(TAG,"An exception occured while refreshing device " + e.toString());
        }
        return false;
    }

    public void setNeedReconnect(boolean needReconnect) {
        isNeedReconnect = needReconnect;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

    public IBleListener getListener() {
        return listener;
    }

    public void setListener(IBleListener listener) {
        this.listener = listener;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void setRtkBindTag(Boolean rtkBindTag) {
        this.rtkBindTag = rtkBindTag;
    }

    public BluetoothGattCharacteristic findTheGattCharacteristic(UUID serviceUuid, UUID charUuid) {
        BluetoothGattCharacteristic charCharacteristic = cacheGattCharacteristicHashMap.get(charUuid);
        if (charCharacteristic == null) {
            charCharacteristic = initTheCharacteristic(serviceUuid, charUuid);
            if (charCharacteristic != null) {
                cacheGattCharacteristicHashMap.put(charUuid, charCharacteristic);
            }
            return charCharacteristic;
        }
        return charCharacteristic;
    }

    private BluetoothGattCharacteristic initTheCharacteristic(UUID serviceUuid, UUID charUuid) {
        if (!BleOperateManager.getInstance().isConnected()) {
            return null;
        }
        String address = BleBaseControl.getInstance().getmDeviceAddress();
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        BluetoothGatt gatt = BleBaseControl.getInstance().getGatt(address);
        if (gatt == null) {
            return null;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        if (service == null) {
            Log.e(TAG, "initTheCharacteristic: can't find service uuid=" + serviceUuid);
            return null;
        }
        BluetoothGattCharacteristic charCharacteristic = service.getCharacteristic(charUuid);
        return charCharacteristic;
    }


    private void checkIsNotifyConfigAndRegisterCallback(BluetoothGattDescriptor descriptor, BluetoothGatt gatt) {

        if (descriptor.getUuid().compareTo(Constants.GATT_NOTIFY_CONFIG) == 0) {//是 open notify的操作，

            byte[] value = descriptor.getValue();
            if (value != null && value.length == 2 && value[1] == 0x00) {
                if (value[0] == 0x01) {
                    //打开notify
                    if (listener != null) {
                        listener.bleCharacteristicNotification();
                    }
                } else {
                    //关闭notify

                }
            }
        }
    }


    /****
     * 移除系统绑定的手环
     * @param mac
     */
    public void unBondedDevice(String mac) {
        try {
            if (TextUtils.isEmpty(mac)) {
                return;
            }
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
            Class btDeviceCls = BluetoothDevice.class;
            Method removeBond = null;
            try {
                removeBond = btDeviceCls.getMethod("removeBond");
                removeBond.invoke(device);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            removeBond.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean classicBluetoothScan() {
        try {
            //当前是否在扫描，如果是就取消当前的扫描，重新扫描
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            //此方法是个异步操作，一般搜索12秒
            Log.i(TAG,"------扫描");
            return mBluetoothAdapter.startDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取消扫描蓝牙
     *
     * @return true 为取消成功
     */
    @SuppressLint({"MissingPermission"})
    public boolean cancelScanBluetooth() {
        try {
            return mBluetoothAdapter.cancelDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint({"MissingPermission"})
    public void createBondBlueTooth(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        try {
            //配对之前把扫描关闭
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            //判断设备是否配对，没有配对在配，配对了就不需要配了
            if (device.getBondState() == BOND_NONE) {
                try {
                    boolean returnValue = device.createBond();
                    Log.i(TAG,"是否配对成功：" + "" + returnValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG,"配对失败：");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"DiscouragedPrivateApi"})
    public boolean createBond(BluetoothDevice device, int transport) {
        if (null != device) {
            boolean bRet = false;

            try {
                Class<?> bluetoothDeviceClass = device.getClass();
                Method createBondMethod = bluetoothDeviceClass.getDeclaredMethod("createBond", Integer.TYPE);
                createBondMethod.setAccessible(true);
                Object object = createBondMethod.invoke(device, transport);
                if (!(object instanceof Boolean)) {
                    return false;
                }

                bRet = (Boolean) object;
            } catch (Exception var7) {
                var7.printStackTrace();
            }

            return bRet;
        } else {
            return false;
        }
    }

    @SuppressLint({"MissingPermission"})
    public boolean createBond(Context context, BluetoothDevice device) {
        if (device != null) {
            boolean bRet = false;
            if (Build.VERSION.SDK_INT >= 20) {
                bRet = device.createBond();
            } else {
                Class btClass = device.getClass();

                try {
                    Method createBondMethod = btClass.getMethod("createBond");
                    Object object = createBondMethod.invoke(device);
                    if (!(object instanceof Boolean)) {
                        return false;
                    }

                    bRet = (Boolean) object;
                } catch (Exception var6) {
                    var6.printStackTrace();
                    Log.i(TAG,"Invoke createBond : " + var6.getMessage());
                }
            }

            return bRet;
        } else {
            return false;
        }
    }


    public BluetoothDevice getMacSystemBond(String address) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
        if (devices1.size() > 0) {
            for (Iterator<BluetoothDevice> iterator = devices1.iterator(); iterator.hasNext(); ) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                if (bluetoothDevice == null || bluetoothDevice.getName() == null || bluetoothDevice.getAddress() == null) {
                    continue;
                }
                if (!TextUtils.isEmpty(address)) {
                    if (address.equalsIgnoreCase(bluetoothDevice.getAddress())) {
                        return bluetoothDevice;
                    }
                }
            }
        }
        return null;
    }

    public void bleCreateBond() {
        try {
            if (bleConnectDevice != null) {
                BluetoothDevice device = getMacSystemBond(bleConnectDevice.getAddress());
                Log.i(TAG,"-----配对状态:" + bleConnectDevice.getBondState());
                if (bleConnectDevice.getBondState() == BOND_NONE && device == null) {
                    bleConnectDevice.createBond();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
