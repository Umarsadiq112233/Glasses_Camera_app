package com.oudmon.ble.base.scan;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.oudmon.ble.base.bluetooth.DeviceManager;
import com.oudmon.qc_utils.bluetooth.BluetoothUtils;
import androidx.core.os.HandlerCompat;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author gs 20200813
 */
public class BleScannerHelper{
    private static final String TAG = "glasses_sdk";
    private static final String HANDLER_TOKEN = "stop_token";
    private static BleScannerHelper bleScannerHelper;
    private Handler handler= new Handler(Looper.getMainLooper());
    private int timeOut=12000;
    private BleScannerHelper() {

    }


    public static BleScannerHelper getInstance() {
        if (bleScannerHelper == null) {
            synchronized (BleScannerHelper.class) {
                if (bleScannerHelper == null) {
                    bleScannerHelper = new BleScannerHelper();
                }
            }
        }
        return bleScannerHelper;
    }

    public void reSetCallback(){
        bleScannerHelper=null;
    }



    /**
     * 扫描设备
     *
     * @param scanCallBack
     */
    public void scanDevice(final Context context, UUID mUuid, final ScanWrapperCallback scanCallBack) {
        handler.removeCallbacksAndMessages(HANDLER_TOKEN);
        if(!BluetoothUtils.isEnabledBluetooth(context)){
            BleScannerCompat.getScanner(context).scanning=false;
            return;
        }
        if(BleScannerCompat.getScanner(context).isScanning()){
            Log.i(TAG,"isScanning:true");
            stopScan(context);
        }
        HandlerCompat.postDelayed(handler, new Runnable() {
                @Override
                public void run() {
                    stopScan(context);
                    if(scanCallBack!=null){
                        scanCallBack.onScanFailed(0);
                    }
                }
            }, HANDLER_TOKEN, 12000);
        BleScannerCompat.getScanner(context).startScan(scanCallBack);
    }



    public void stopScan(Context context) {
        handler.removeCallbacksAndMessages(HANDLER_TOKEN);
        if(!BluetoothUtils.isEnabledBluetooth(context)){
            BleScannerCompat.getScanner(context).scanning=false;
            return;
        }
        BleScannerCompat.getScanner(context).stopScan();
    }


    /**
     * 扫描指定设备
     *
     * @param macAddress
     * @param scanResult
     */
    public boolean scanTheDevice(final Context context, final String macAddress, final OnTheScanResult scanResult) {
        handler.removeCallbacksAndMessages(HANDLER_TOKEN);
        if(!BluetoothUtils.isEnabledBluetooth(context)){
            return false;
        }
        HandlerCompat.postDelayed(handler, new Runnable() {
            @Override
            public void run() {
                stopScan(context);
                if(scanResult!=null){
                    scanResult.onScanFailed(0);
                }
            }
        }, HANDLER_TOKEN, timeOut);

        BleScannerCompat.getScanner(context).startScan(new ScanWrapperCallback() {
            @Override
            public void onStart() {
                 Log.i(TAG,"start");
            }

            @Override
            public void onStop() {
                 Log.i(TAG,"stop");
                try {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
                    if (devices1.size()>0) {
                        for (BluetoothDevice bluetoothDevice : devices1) {
                            if (bluetoothDevice == null || bluetoothDevice.getName() == null || bluetoothDevice.getAddress() == null) {
                                continue;
                            }
                            if(bluetoothDevice.getAddress().equalsIgnoreCase(macAddress)){
                                scanResult.onResult(bluetoothDevice);
                                 Log.i(TAG,"系统绑定了手环");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(device.getAddress().equalsIgnoreCase(macAddress)){
                    scanResult.onResult(device);
                     Log.i(TAG,device.getAddress());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                scanResult.onScanFailed(errorCode);
               Log.i(TAG,"------------"+errorCode);
            }

            @Override
            public void onParsedData(BluetoothDevice device, ScanRecord scanRecord) {

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult sr:results) {
                    BluetoothDevice device=sr.getDevice();
                    if(device.getAddress().equalsIgnoreCase(macAddress)){
                        scanResult.onResult(device);
                    }
                }
            }
        });
        return true;
    }




    public void removeSystemBle(String mac){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
        if (devices1.size()>0) {
            for(Iterator<BluetoothDevice> iterator = devices1.iterator(); iterator.hasNext();){
                BluetoothDevice bluetoothDevice=(BluetoothDevice)iterator.next();
                 Log.i(TAG,bluetoothDevice.getName());
                 Log.i(TAG,bluetoothDevice.getAddress());
                if(bluetoothDevice==null || bluetoothDevice.getName()==null || bluetoothDevice.getAddress()==null){
                    continue;
                }
                Log.i(TAG,"移除"+mac);
                if(!TextUtils.isEmpty(mac)){
                    if(mac.equalsIgnoreCase(bluetoothDevice.getAddress())){
                        removeBondDevice(adapter,bluetoothDevice.getAddress());
                    }
                }
            }
        }
    }

    public void removeSystemBle(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
        if (devices1.size()>0) {
            for(Iterator<BluetoothDevice> iterator = devices1.iterator(); iterator.hasNext();){
                BluetoothDevice bluetoothDevice=(BluetoothDevice)iterator.next();
                if(bluetoothDevice==null || bluetoothDevice.getName()==null || bluetoothDevice.getAddress()==null){
                    continue;
                }
                String mac= DeviceManager.getInstance().getDeviceAddress();
                if(!TextUtils.isEmpty(mac)){
                    if(mac.equalsIgnoreCase(bluetoothDevice.getAddress())){
                        removeBondDevice(adapter,bluetoothDevice.getAddress());
                    }
                }
            }
        }
    }



    public void removeMacSystemBond(String address){
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
                        removeBondDevice(adapter,bluetoothDevice.getAddress());
                    }
                }
            }
        }
    }


    public boolean isMacSystemBond(String address){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices1 = adapter.getBondedDevices();
        if (devices1.size()>0) {
            for(Iterator<BluetoothDevice> iterator = devices1.iterator(); iterator.hasNext();){
                BluetoothDevice bluetoothDevice=(BluetoothDevice)iterator.next();
                if(bluetoothDevice==null || bluetoothDevice.getAddress()==null){
                    continue;
                }
                if(!TextUtils.isEmpty(address)){
                    if(address.equalsIgnoreCase(bluetoothDevice.getAddress())){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void removeBondDevice(BluetoothAdapter adapter,String address){
        BluetoothDevice device = adapter.getRemoteDevice(address);
        Class btDeviceCls = BluetoothDevice.class;
        Method removeBond = null;
        try {
            removeBond = btDeviceCls.getMethod("removeBond");
            removeBond.setAccessible(true);
            removeBond.invoke(device);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
}
