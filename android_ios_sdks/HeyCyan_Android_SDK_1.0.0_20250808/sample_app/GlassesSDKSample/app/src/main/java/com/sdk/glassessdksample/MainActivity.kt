package com.sdk.glassessdksample

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyListener
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyRsp
import com.oudmon.wifi.GlassesControl
import com.oudmon.wifi.bean.GlassAlbumEntity
import com.sdk.glassessdksample.databinding.AcitivytMainBinding
import com.sdk.glassessdksample.ui.BluetoothUtils
import com.sdk.glassessdksample.ui.DeviceBindActivity
import com.sdk.glassessdksample.ui.MyApplication
import com.sdk.glassessdksample.ui.hasBluetooth
import com.sdk.glassessdksample.ui.requestAllPermission
import com.sdk.glassessdksample.ui.requestBluetoothPermission
import com.sdk.glassessdksample.ui.requestLocationPermission
import com.sdk.glassessdksample.ui.setOnClickListener
import com.sdk.glassessdksample.ui.startKtxActivity
import org.greenrobot.eventbus.EventBus
import java.io.File

class MainActivity : AppCompatActivity() {
    private val TAG="HeyCyanSDK"
    private lateinit var binding: AcitivytMainBinding
    private val deviceNotifyListener by lazy { MyDeviceNotifyListener() }



    private val requestedPermissions = buildList {
        add(Manifest.permission.INTERNET)
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        add(Manifest.permission.ACCESS_NETWORK_STATE)
        add(Manifest.permission.CHANGE_NETWORK_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()

    private val requestPermissionLaunch = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        if (it.all { it.value }) {
            GlassesControl.getInstance(MyApplication.getInstance())?.initGlasses(MyApplication.getInstance().getAlbumDirFile().absolutePath)
            GlassesControl.getInstance(MyApplication.getInstance())?.setWifiDownloadListener(object :GlassesControl.WifiFilesDownloadListener{
                override fun eisEnd(fileName: String, filePath: String) {
                    Log.i(TAG,"eisEnd fileName: $fileName filePath: $filePath")
                }

                override fun eisError(fileName: String, sourcePath: String, errorInfo: String) {
                    Log.i(TAG,"eisEnd fileName: $fileName filePath: $sourcePath errorInfo: $errorInfo")
                }

                override fun fileCount(index: Int, total: Int) {
                    Log.i(TAG,"fileCount index: $index total: $total")
                }

                override fun fileDownloadComplete() {
                    Log.i(TAG,"fileDownloadComplete")
                }

                override fun fileDownloadError(fileType: Int, errorType: Int) {
                    Log.i(TAG,"fileDownloadError fileType: $fileType errorType: $errorType")
                }

                override fun fileProgress(fileName: String, progress: Int) {
                    Log.i(TAG,"fileProgress fileName: $fileName progress: $progress")
                }

                override fun fileWasDownloadSuccessfully(entity: GlassAlbumEntity) {
                    Log.i(TAG,"fileWasDownloadSuccessfully entity: $entity")
                }

                override fun onGlassesControlSuccess() {
                    Log.i(TAG,"onGlassesControlSuccess")
                }

                override fun onGlassesFail(errorCode: Int) {
                    Log.i(TAG,"onGlassesFail errorCode: $errorCode")
                }

                override fun recordingToPcm(fileName: String, filePath: String, duration: Int) {
                    Log.i(TAG,"recordingToPcm fileName: $fileName filePath: $filePath duration: $duration")
                }

                override fun recordingToPcmError(fileName: String, errorInfo: String) {
                    Log.i(TAG,"recordingToPcmError fileName: $fileName errorInfo: $errorInfo")
                }

                override fun wifiSpeed(wifiSpeed: String) {
                   Log.i(TAG,"wifiSpeed wifiSpeed: $wifiSpeed")
                }

            })
            GlassesControl.getInstance(MyApplication.getInstance)?.importAlbum()
        } else {
            Log.i("sdk","拒绝了权限")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivytMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    inner class PermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            } else {
                startKtxActivity<DeviceBindActivity>()
            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if (never) {
                XXPermissions.startPermissionActivity(this@MainActivity, permissions);
            }
        }

    }


    override fun onResume() {
        super.onResume()
        try {
            if (!BluetoothUtils.isEnabledBluetooth(this)) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                }
                startActivityForResult(intent, 300)
            }
        } catch (e: Exception) {
        }
        if (!hasBluetooth(this)) {
            requestBluetoothPermission(this, BluetoothPermissionCallback())
        }

        requestAllPermission(this, OnPermissionCallback { permissions, all -> })
    }

    inner class BluetoothPermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if (never) {
                XXPermissions.startPermissionActivity(this@MainActivity, permissions)
            }
        }

    }

    private fun initView() {
        requestAllPermission(this, OnPermissionCallback { permissions, all -> })
        MyApplication.getInstance.createDirs( MyApplication.getInstance.getAlbumDirFile().absolutePath)
        setOnClickListener(
            binding.btnScan,
            binding.btnConnect,
            binding.btnDisconnect,
            binding.btnAddListener,
            binding.btnSetTime,
            binding.btnVersion,
            binding.btnCamera,
            binding.btnVideo,
            binding.btnRecord,
            binding.btnThumbnail,
            binding.btnBt,
            binding.btnBattery,
            binding.btnVolume,
            binding.btnMediaCount,
            binding.btnSyncPicture
        ) {
            when (this) {
                binding.btnScan -> {
                    requestLocationPermission(this@MainActivity, PermissionCallback())
                }

                binding.btnConnect -> {
                    BleOperateManager.getInstance()
                        .connectDirectly(DeviceManager.getInstance().deviceAddress)
                }

                binding.btnDisconnect -> {
                    BleOperateManager.getInstance().unBindDevice()
                }

                binding.btnAddListener -> {
                    LargeDataHandler.getInstance().addOutDeviceListener(100, deviceNotifyListener)
                }

                binding.btnSetTime -> {
                    Log.i("setTime", "setTime" + BleOperateManager.getInstance().isConnected)
                    LargeDataHandler.getInstance().syncTime { _, _ -> }
                }

                binding.btnVersion -> {
                    LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                        if (response != null) {
                            //wifi 固件版本
                            response.wifiFirmwareVersion
                            //wifi 产品版本
                            response.wifiHardwareVersion
                            //蓝牙产品版本
                            response.hardwareVersion
                            //蓝牙固件版本
                            response.firmwareVersion
                        }
                    }
                }

                binding.btnCamera -> {
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, 0x01)
                    ) { _, it ->
                        if (it.dataType == 1 && it.errorCode == 0) {
                            when (it.workTypeIng) {
                                2 -> {
                                    //眼镜正在录像
                                }

                                4 -> {
                                    //眼镜正在传输模式
                                }

                                5 -> {
                                    //眼镜正在OTA模式
                                }

                                1, 6 -> {
                                    //眼镜正在拍照模式
                                }

                                7 -> {
                                    //眼镜正在AI对话
                                }

                                8 -> {
                                    //眼镜正在录音模式
                                }
                            }
                        } else {
                            //执行开始和结束
                        }
                    }
                }

                binding.btnVideo -> {
                    //videoStart  true 开始录制   false 停止录制
                    val videoStart = true
                    val value = if (videoStart) 0x02 else 0x03
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, value.toByte())
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                    }

                                    4 -> {
                                        //眼镜正在传输模式
                                    }

                                    5 -> {
                                        //眼镜正在OTA模式
                                    }

                                    1, 6 -> {
                                        //眼镜正在拍照模式
                                    }

                                    7 -> {
                                        //眼镜正在AI对话
                                    }

                                    8 -> {
                                        //眼镜正在录音模式
                                    }
                                }
                            } else {
                                //执行开始和结束
                            }
                        }
                    }
                }

                binding.btnRecord -> {
                    //recordStart  true 开始录制   false 停止录制
                    val recordStart = true
                    val value = if (recordStart) 0x08 else 0x0c
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, value.toByte())
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                    }

                                    4 -> {
                                        //眼镜正在传输模式
                                    }

                                    5 -> {
                                        //眼镜正在OTA模式
                                    }

                                    1, 6 -> {
                                        //眼镜正在拍照模式
                                    }

                                    7 -> {
                                        //眼镜正在AI对话
                                    }

                                    8 -> {
                                        //眼镜正在录音模式
                                    }
                                }
                            } else {
                                //执行开始和结束
                            }
                        }
                    }
                }

                binding.btnThumbnail -> {
                    //thumbnailSize  0..6
                    val thumbnailSize = 0x02
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(
                            0x02,
                            0x01,
                            0x06,
                            thumbnailSize.toByte(),
                            thumbnailSize.toByte(),
                            0x02
                        )
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                    }

                                    4 -> {
                                        //眼镜正在传输模式
                                    }

                                    5 -> {
                                        //眼镜正在OTA模式
                                    }

                                    1, 6 -> {
                                        //眼镜正在拍照模式
                                    }

                                    7 -> {
                                        //眼镜正在AI对话
                                    }

                                    8 -> {
                                        //眼镜正在录音模式
                                    }
                                }
                            } else {
                                //触发AI拍照，上报缩略图会收到上报指令
                            }
                        }
                    }
                }

                binding.btnBt -> {
                    //BT扫描
                    LargeDataHandler.getInstance().openBT()
                    BleOperateManager.getInstance().classicBluetoothStartScan()

                }

                binding.btnBattery -> {
                    //添加电量监听
                    LargeDataHandler.getInstance().addBatteryCallBack("init") { _, response ->

                    }
                    //电量
                    LargeDataHandler.getInstance().syncBattery()
                }

                binding.btnVolume -> {
                    //读取音量控制
                    LargeDataHandler.getInstance().getVolumeControl { _, response ->
                        if (response != null) {
                            //眼镜音量 音乐最小值 最大值 当前值
                            response.minVolumeMusic
                            response.maxVolumeMusic
                            response.currVolumeMusic
                            //眼镜电话 电话最小值 最大值 当前值
                            response.minVolumeCall
                            response.maxVolumeCall
                            response.currVolumeCall
                            //眼镜系统 系统最小值 最大值 当前值
                            response.minVolumeSystem
                            response.maxVolumeSystem
                            response.currVolumeSystem
                            //眼镜当前的模式
                            response.currVolumeType
                        }
                    }
                }

                binding.btnMediaCount -> {
                    LargeDataHandler.getInstance()
                        .glassesControl(byteArrayOf(0x02, 0x04)) { _, it ->
                            if (it.dataType == 4) {
                                val mediaCount = it.imageCount + it.videoCount + it.recordCount
                                if (mediaCount > 0) {
                                    //眼镜有多少个媒体没有上传
                                } else {
                                    //无
                                }
                            }
                        }
                }

                binding.btnSyncPicture -> {
                    //眼镜的多媒体
                    //要先申请wifi权限
                    requestPermissionLaunch.launch(requestedPermissions)
                }
            }
        }
    }

    inner class MyDeviceNotifyListener : GlassesDeviceNotifyListener() {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun parseData(cmdType: Int, response: GlassesDeviceNotifyRsp) {
            when (response.loadData[6].toInt()) {
                //眼镜电量上报
                0x05 -> {
                    //当前电量
                    val battery = response.loadData[7].toInt()
                    //是否在充电
                    val changing = response.loadData[8].toInt()
                }
                //眼镜通过快捷识别
                0x02 -> {
                    if (response.loadData.size > 9 && response.loadData[9].toInt() == 0x02) {
                        //要设置识别意图：eg 请帮我看看眼前是什么，图片中的内容
                    }
                    //获取图片缩略图
                    LargeDataHandler.getInstance().getPictureThumbnails { cmdType, success, data ->
                        //请将data存入路径,jpg的图片
                    }
                }

                0x03 -> {
                    if (response.loadData[7].toInt() == 1) {
                        //眼镜启动麦克风开始说话
                    }
                }
                //ota 升级
                0x04 -> {
                    try {
                        val download = response.loadData[7].toInt()
                        val soc = response.loadData[8].toInt()
                        val nor = response.loadData[9].toInt()
                        //download 固件下载进度 soc 下载进度 nor 升级进度
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                0x0c -> {
                    //眼镜触发暂停事件，语音播报
                    if (response.loadData[7].toInt() == 1) {
                        //to do
                    }
                }

                0x0d -> {
                    //解除APP绑定事件
                    if (response.loadData[7].toInt() == 1) {
                        //to do
                    }
                }
                //眼镜内存不足事件
                0x0e -> {

                }
                //翻译暂停事件
                0x10 -> {

                }
                //眼镜音量变化事件
                0x12 -> {
                    //音乐音量
                    //最小音量
                    response.loadData[8].toInt()
                    //最大音量
                    response.loadData[9].toInt()
                    //当前音量
                    response.loadData[10].toInt()

                    //来电音量
                    //最小音量
                    response.loadData[12].toInt()
                    //最大音量
                    response.loadData[13].toInt()
                    //当前音量
                    response.loadData[14].toInt()

                    //眼镜系统音量
                    //最小音量
                    response.loadData[16].toInt()
                    //最大音量
                    response.loadData[17].toInt()
                    //当前音量
                    response.loadData[18].toInt()

                    //当前的音量模式
                    response.loadData[19].toInt()

                }
            }
        }
    }
}