package com.oudmon.ble.base.bluetooth;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oudmon.ble.base.communication.Constants;

/**
 * @author gs ,
 * @date swatch_device_text12/23
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class QCBluetoothCallbackReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String uuid;
        byte[] data;
        switch (action){
            case BleAction.BLE_GATT_CONNECTED:
                 BluetoothDevice device=intent.getParcelableExtra(BleAction.EXTRA_DEVICE);
                 connectStatue(device,true);
                break;
            case BleAction.BLE_GATT_DISCONNECTED:
            case BleAction.BLE_NO_CALLBACK:
                connectStatue(null,false);
                break;
            case BleAction.BLE_CHARACTERISTIC_WRITE:
                data= intent.getByteArrayExtra(BleAction.EXTRA_DATA);
                onCommandSend(data);
                break;
            case BleAction.BLE_CHARACTERISTIC_CHANGED:
                String address = intent.getStringExtra(BleAction.EXTRA_ADDR);
                uuid=intent.getStringExtra(BleAction.EXTRA_CHARACTER_UUID);
                data = intent.getByteArrayExtra(BleAction.EXTRA_VALUE);
                onCharacteristicChange(address,uuid,data);
                break;
            case BleAction.BLE_SERVICE_DISCOVERED:
                onServiceDiscovered();
                break;

            default:
                break;
        }
    }

    public void connectStatue(BluetoothDevice device,boolean connected){
        if(device!=null){
            DeviceManager.getInstance().setDeviceName(device.getName());
            DeviceManager.getInstance().setDeviceAddress(device.getAddress());
            if(DeviceManager.getInstance().getDeviceName().contains("_")){
                String wifiName = DeviceManager.getInstance().getDeviceName().split("_")[0];
                String wifiMac = DeviceManager.getInstance().getDeviceAddress().replace(":", "");
                if(wifiName.length()>20){
                    wifiName= wifiName.substring(0,20);
                }
                DeviceManager.getInstance().setWifiName(wifiName + "_" + wifiMac);
                DeviceManager.getInstance().setWifiPassword("123456789");
            }else{
                String wifiName = DeviceManager.getInstance().getDeviceName();
                String wifiMac = DeviceManager.getInstance().getDeviceAddress().replace(":", "");
                DeviceManager.getInstance().setWifiName(wifiName + "_" + wifiMac);
                DeviceManager.getInstance().setWifiPassword("123456789");
            }
        }
    }

    public void onServiceDiscovered(){

    }


    public void  onCommandSend(byte[] data){
    }
    public void onCharacteristicChange(String address,String uuid,byte[] data){
//        if (data == null || data.length != Constants.CMD_DATA_LENGTH) {
//
//        } else {
//            if (!QCDataParser.checkCrc(data)) {
//                return;
//            }
//            if (!QCDataParser.parserAndDispatchReqData(data)) {
//                //手表主动发起的notify
//                QCDataParser.parserAndDispatchNotifyData(BleOperateManager.getInstance().getNotifySparseArray(), data);
//            }
//        }
    }




}
