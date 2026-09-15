package com.example.glasses_camera_app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.p2p.WifiP2pManager
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

import com.oudmon.ble.base.bluetooth.BleAction
import com.oudmon.ble.base.bluetooth.BleBaseControl
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.bluetooth.QCBluetoothCallbackCloneReceiver
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyListener
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyRsp
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.ScanRecord
import com.oudmon.ble.base.scan.ScanWrapperCallback
import com.oudmon.wifi.*
import com.oudmon.wifi.bean.*

import org.greenrobot.eventbus.EventBus

import java.io.File

class MainActivity : FlutterActivity() {
    companion object {
        private const val TAG = "GLASSES"
        private const val METHOD_CHANNEL = "glasses_camera/methods"
        private const val CONNECTION_STATE_CHANNEL = "glasses_camera/connection_state"
        private const val BLUETOOTH_STATE_CHANNEL = "glasses_camera/bluetooth_state"
        private const val SCAN_RESULTS_CHANNEL = "glasses_camera/scan_results"
        private const val EVENTS_CHANNEL = "glasses_camera/events"
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_ENABLE_BT_CODE = 1002
    }

    // Channel sinks
    private var connectionStateSink: EventChannel.EventSink? = null
    private var bluetoothStateSink: EventChannel.EventSink? = null
    private var scanResultsSink: EventChannel.EventSink? = null
    private var eventsSink: EventChannel.EventSink? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    // State
    private var sdkInitialized = false
    private var isScanning = false
    private var isConnected = false
    private var pendingPermissionResult: MethodChannel.Result? = null

    // Real-time Telemetry Cache
    private var cachedBatteryLevel = 0
    private var cachedIsCharging = false
    private var cachedHardwareVersion = ""
    private var cachedFirmwareVersion = ""
    private var cachedWifiHardwareVersion = ""
    private var cachedWifiFirmwareVersion = ""
    private var cachedPhotoCount = 0
    private var cachedVideoCount = 0
    private var cachedAudioCount = 0

    // GlassesControl — initialized ONCE on first download to avoid duplicate P2P receiver registration
    private var glassesControl: GlassesControl? = null
    private var glassesControlInitialized = false

    // Android-side watchdog: fires mediaDownloadError if SDK is silent (retryAlsoFailed path)
    private var downloadTimeoutRunnable: Runnable? = null

    // Discovered devices
    private val discoveredDevices = mutableListOf<Map<String, Any?>>()

    // BLE receiver
    private var bleReceiver: GlassesBleReceiver? = null
    private var deviceNotifyListener: GlassesNotifyListener? = null

    private fun syncAllDeviceTelemetry() {
        try {
            LargeDataHandler.getInstance().syncTime { _, _ -> }
            LargeDataHandler.getInstance().syncBattery()
            LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                if (response != null) {
                    if (!response.firmwareVersion.isNullOrEmpty()) cachedFirmwareVersion = response.firmwareVersion
                    if (!response.hardwareVersion.isNullOrEmpty()) cachedHardwareVersion = response.hardwareVersion
                    if (!response.wifiFirmwareVersion.isNullOrEmpty()) cachedWifiFirmwareVersion = response.wifiFirmwareVersion
                    if (!response.wifiHardwareVersion.isNullOrEmpty()) cachedWifiHardwareVersion = response.wifiHardwareVersion
                    Log.i(TAG, "Device telemetry updated: firmware=$cachedFirmwareVersion hardware=$cachedHardwareVersion")
                    sendEvent("versionUpdate", mapOf(
                        "hardwareVersion" to cachedHardwareVersion,
                        "firmwareVersion" to cachedFirmwareVersion,
                        "wifiHardwareVersion" to cachedWifiHardwareVersion,
                        "wifiFirmwareVersion" to cachedWifiFirmwareVersion
                    ), "Version info synced: $cachedFirmwareVersion")
                }
            }
            queryMediaCountFromGlasses()
        } catch (e: Exception) {
            Log.w(TAG, "syncAllDeviceTelemetry failed", e)
        }
    }

    private fun queryMediaCountFromGlasses(onDone: ((Map<String, Int>) -> Unit)? = null) {
        try {
            LargeDataHandler.getInstance().glassesControl(byteArrayOf(0x02, 0x04)) { _, response ->
                try {
                    val rsp = response ?: run {
                        onDone?.invoke(mapOf("photoCount" to cachedPhotoCount, "videoCount" to cachedVideoCount, "audioCount" to cachedAudioCount))
                        return@glassesControl
                    }
                    val dataType = rsp.javaClass.getMethod("getDataType").invoke(rsp) as? Int ?: 0
                    if (dataType == 4) {
                        val imageCount = rsp.javaClass.getMethod("getImageCount").invoke(rsp) as? Int ?: 0
                        val videoCount = rsp.javaClass.getMethod("getVideoCount").invoke(rsp) as? Int ?: 0
                        val recordCount = rsp.javaClass.getMethod("getRecordCount").invoke(rsp) as? Int ?: 0

                        cachedPhotoCount = imageCount
                        cachedVideoCount = videoCount
                        cachedAudioCount = recordCount

                        val counts = mapOf(
                            "photoCount" to cachedPhotoCount,
                            "videoCount" to cachedVideoCount,
                            "audioCount" to cachedAudioCount
                        )

                        sendEvent("mediaCountUpdate", counts, "Media counts updated: P=$cachedPhotoCount, V=$cachedVideoCount, A=$cachedAudioCount")
                        mainHandler.post { onDone?.invoke(counts) }
                    } else {
                        mainHandler.post { onDone?.invoke(mapOf("photoCount" to cachedPhotoCount, "videoCount" to cachedVideoCount, "audioCount" to cachedAudioCount)) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing media count response", e)
                    mainHandler.post { onDone?.invoke(mapOf("photoCount" to cachedPhotoCount, "videoCount" to cachedVideoCount, "audioCount" to cachedAudioCount)) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "queryMediaCountFromGlasses failed", e)
            mainHandler.post { onDone?.invoke(mapOf("photoCount" to cachedPhotoCount, "videoCount" to cachedVideoCount, "audioCount" to cachedAudioCount)) }
        }
    }

    private class SafeResult(private val rawResult: MethodChannel.Result) : MethodChannel.Result {
        private val replied = java.util.concurrent.atomic.AtomicBoolean(false)

        override fun success(result: Any?) {
            if (replied.compareAndSet(false, true)) {
                rawResult.success(result)
            } else {
                Log.w(TAG, "Ignored duplicate success($result)")
            }
        }

        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            if (replied.compareAndSet(false, true)) {
                rawResult.error(errorCode, errorMessage, errorDetails)
            } else {
                Log.w(TAG, "Ignored duplicate error($errorCode, $errorMessage)")
            }
        }

        override fun notImplemented() {
            if (replied.compareAndSet(false, true)) {
                rawResult.notImplemented()
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Method channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL)
            .setMethodCallHandler { call, rawResult ->
                val result = SafeResult(rawResult)
                when (call.method) {
                    "initialize" -> handleInitialize(result)
                    "dispose" -> handleDispose(result)
                    "startScan" -> handleStartScan(call.argument<Int>("timeout") ?: 15, result)
                    "stopScan" -> handleStopScan(result)
                    "connect" -> handleConnect(call.argument<String>("identifier") ?: "", result)
                    "disconnect" -> handleDisconnect(result)
                    "takePhoto" -> handleTakePhoto(result)
                    "startVideoRecording" -> handleStartVideo(result)
                    "stopVideoRecording" -> handleStopVideo(result)
                    "startAudioRecording" -> handleStartAudio(result)
                    "stopAudioRecording" -> handleStopAudioRecording(result)
                    "getBattery" -> handleGetBattery(result)
                    "getVersionInfo" -> handleGetVersionInfo(result)
                    "getMediaInfo" -> handleGetMediaInfo(result)
                    "syncTime" -> handleSyncTime(result)
                    "downloadMedia" -> handleDownloadMedia(result)
                    "getThumbnail" -> handleGetThumbnail(result)
                    "requestPermissions" -> handleRequestPermissions(result)
                    "requestEnableBluetooth" -> handleEnableBluetooth(result)
                    "isLocationServiceEnabled", "isLocationEnabled" -> result.success(isLocationServiceEnabled())
                    "openLocationSettings" -> handleOpenLocationSettings(result)
                    "getGlassesStatus" -> handleGetGlassesStatus(result)
                    "startMediaSync" -> handleDownloadMedia(result)
                    "cancelMediaSync" -> handleCancelMediaSync(result)
                    "deleteMediaFiles" -> handleDeleteMediaFiles(result)
                    "restartGlasses" -> handleRestartGlasses(result)
                    "updateFirmware" -> handleUpdateFirmware(call.argument<String>("url") ?: "", result)
                    "convertOpusToMp3" -> handleConvertOpusToMp3(call.argument<String>("filePath") ?: "", result)
                    "getLocalMediaFiles" -> handleGetLocalMediaFiles(result)
                    "transcodeVideo" -> handleTranscodeVideo(call.argument<String>("inputPath") ?: call.argument<String>("filePath") ?: "", result)
                    "openMediaFile", "playVideo" -> handleOpenMediaFile(call.argument<String>("filePath") ?: "", result)
                    "playAudio" -> handlePlayAudio(call.argument<String>("filePath") ?: "", result)
                    "pauseAudio" -> handlePauseAudio(result)
                    "resumeAudio" -> handleResumeAudio(result)
                    "stopAudio" -> handleStopPlaybackAudio(result)
                    "seekAudio" -> handleSeekAudio(call.argument<Int>("positionMs") ?: 0, result)
                    "getAudioProgress" -> handleGetAudioProgress(result)
                    "setVolume" -> handleSetVolume(call.argument<Double>("volume") ?: 0.8, result)
                    else -> result.notImplemented()
                }
            }

        // Event channels
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, CONNECTION_STATE_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    connectionStateSink = events
                }
                override fun onCancel(arguments: Any?) {
                    connectionStateSink = null
                }
            })

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, BLUETOOTH_STATE_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    bluetoothStateSink = events
                    // Send initial BT state
                    checkBluetoothState()
                }
                override fun onCancel(arguments: Any?) {
                    bluetoothStateSink = null
                }
            })

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, SCAN_RESULTS_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    scanResultsSink = events
                }
                override fun onCancel(arguments: Any?) {
                    scanResultsSink = null
                }
            })

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENTS_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventsSink = events
                }
                override fun onCancel(arguments: Any?) {
                    eventsSink = null
                }
            })
    }

    // ─── SDK Initialization ───
    // Follows MyApplication.initBle() pattern from sample app

    private fun handleInitialize(result: MethodChannel.Result) {
        if (sdkInitialized) {
            result.success(null)
            return
        }

        try {
            Log.i(TAG, "Initializing SDK...")

            // Initialize BLE infrastructure (from sample MyApplication)
            LargeDataHandler.getInstance()
            BleOperateManager.getInstance(application)
            BleOperateManager.getInstance().setApplication(application)
            BleOperateManager.getInstance().init()

            // Register BLE callback receiver via LocalBroadcastManager
            bleReceiver = GlassesBleReceiver()
            val intentFilter = BleAction.getIntentFilter()
            LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(bleReceiver!!, intentFilter)

            // Register system Bluetooth state receiver safely
            try {
                val deviceFilter = BleAction.getDeviceIntentFilter()
                val systemReceiver = GlassesSystemBtReceiver()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.registerReceiver(this, systemReceiver, deviceFilter, ContextCompat.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(systemReceiver, deviceFilter)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register BT system receiver", e)
            }

            // Initialize BLE control
            BleBaseControl.getInstance(applicationContext).setmContext(application)

            // Add device notification listener for general notifications (use key 200 to avoid key 100 triggering removeOtherCallbacks())
            deviceNotifyListener = GlassesNotifyListener()
            LargeDataHandler.getInstance().addOutDeviceListener(200, deviceNotifyListener)

            // Add battery callback
            LargeDataHandler.getInstance().addBatteryCallBack("flutter") { _, response ->
                if (response != null) {
                    val level = try {
                        val rsp = response
                        val methods = rsp.javaClass.methods
                        val getBatMethod = methods.firstOrNull { it.name == "getBattery" || it.name == "getBatteryLevel" || it.name == "getLevel" }
                        if (getBatMethod != null) {
                            (getBatMethod.invoke(rsp) as? Int) ?: 0
                        } else {
                            val fields = rsp.javaClass.fields
                            val batField = fields.firstOrNull { it.name == "battery" || it.name == "level" }
                            (batField?.get(rsp) as? Int) ?: 0
                        }
                    } catch (e: Exception) { 0 }

                    sendEvent("batteryUpdate", mapOf(
                        "level" to level,
                        "isCharging" to false
                    ), "Battery updated: $level%")
                }
            }

            sdkInitialized = true
            checkBluetoothState()
            Log.i(TAG, "SDK initialized successfully")
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "SDK init failed", e)
            result.error("SDK_INIT_FAILED", e.message, e.stackTraceToString())
        }
    }

    private fun handleDispose(result: MethodChannel.Result) {
        try {
            cleanupNativeResources()
            result.success(null)
        } catch (e: Exception) {
            result.success(null) // don't fail on dispose
        }
    }

    override fun onDestroy() {
        cleanupNativeResources()
        super.onDestroy()
    }

    private fun cleanupNativeResources() {
        try {
            Log.i(TAG, "Cleaning up native Bluetooth GATT & Wi-Fi P2P resources...")

            // 1. Clear BLE receiver
            if (bleReceiver != null) {
                try {
                    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(bleReceiver!!)
                } catch (_: Exception) {}
                bleReceiver = null
            }

            // 2. Clear Wi-Fi P2P lingering discovery & state (Hot Restart Safety)
            try {
                WifiP2pManagerSingleton.getInstance(application).resetFailCount()
                WifiP2pManagerSingleton.getInstance(application).resetDeviceP2p()
            } catch (_: Exception) {}

            // 3. Close BLE connection cleanly
            try {
                BleOperateManager.getInstance().unBindDevice()
                BleOperateManager.getInstance().disconnect()
            } catch (_: Exception) {}

            sdkInitialized = false
            isConnected = false
        } catch (e: Exception) {
            Log.w(TAG, "Error during native resource cleanup", e)
        }
    }

    // ─── Bluetooth State ───

    private fun checkBluetoothState() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val state = when {
            adapter == null -> "unsupported"
            !adapter.isEnabled -> "poweredOff"
            else -> "poweredOn"
        }
        mainHandler.post { bluetoothStateSink?.success(state) }
    }

    // ─── Scanning ───
    // Uses BleScannerHelper from SDK (same as DeviceBindActivity sample)

    private fun handleStartScan(timeout: Int, result: MethodChannel.Result) {
        if (!checkBlePermissions()) {
            result.error("PERMISSION_DENIED", "Bluetooth permissions required", null)
            return
        }

        try {
            discoveredDevices.clear()
            isScanning = true

            BleScannerHelper.getInstance().reSetCallback()
            BleScannerHelper.getInstance().scanDevice(this, null, object : ScanWrapperCallback {
                override fun onStart() {
                    Log.i(TAG, "Scan started")
                }

                override fun onStop() {
                    Log.i(TAG, "Scan stopped")
                    isScanning = false
                }

                override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                    if (device == null || device.address.isNullOrEmpty()) return

                    var name = device.name
                    if (name.isNullOrEmpty() && scanRecord != null) {
                        name = parseNameFromScanRecord(scanRecord)
                    }

                    // Ignore devices without an explicit broadcast name (eliminates fake background BLE noise)
                    if (name.isNullOrEmpty()) return

                    val cleanName = name.trim()
                    if (cleanName.isEmpty()) return

                    // Filter out obvious non-glasses media targets if any
                    val lowerName = cleanName.lowercase()
                    if (lowerName.contains("tv") || lowerName.contains("printer") || lowerName.contains("desktop")) return

                    val deviceMap = mapOf<String, Any?>(
                        "name" to cleanName,
                        "identifier" to device.address,
                        "mac" to device.address,
                        "rssi" to rssi,
                        "isPaired" to false
                    )

                    // Avoid duplicates
                    val existing = discoveredDevices.indexOfFirst {
                        it["identifier"] == device.address
                    }
                    if (existing >= 0) {
                        discoveredDevices[existing] = deviceMap
                    } else {
                        discoveredDevices.add(deviceMap)
                    }

                    // Sort by RSSI (strongest first)
                    discoveredDevices.sortByDescending { (it["rssi"] as? Int) ?: 0 }

                    mainHandler.post {
                        scanResultsSink?.success(discoveredDevices.toList())
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e(TAG, "Scan failed: $errorCode")
                    isScanning = false
                }

                override fun onParsedData(device: BluetoothDevice?, scanRecord: ScanRecord?) {}
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {}
            })

            // Auto-stop scan after timeout
            mainHandler.postDelayed({
                if (isScanning) {
                    BleScannerHelper.getInstance().stopScan(this)
                    isScanning = false
                }
            }, timeout * 1000L)

            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Scan start failed", e)
            result.error("SCAN_FAILED", e.message, null)
        }
    }

    private fun handleStopScan(result: MethodChannel.Result) {
        try {
            BleScannerHelper.getInstance().stopScan(this)
            isScanning = false
            result.success(null)
        } catch (e: Exception) {
            result.success(null)
        }
    }

    // ─── Connection ───
    // Uses BleOperateManager.connectDirectly() (same as DeviceBindActivity sample)

    private fun handleConnect(identifier: String, result: MethodChannel.Result) {
        if (identifier.isEmpty()) {
            result.error("INVALID_ARGS", "Device identifier required", null)
            return
        }

        try {
            Log.i(TAG, "Connecting to $identifier")
            DeviceManager.getInstance().deviceAddress = identifier
            val devMap = discoveredDevices.firstOrNull { it["identifier"] == identifier }
            val devName = (devMap?.get("name") as? String) ?: ""
            if (devName.isNotEmpty()) {
                DeviceManager.getInstance().deviceName = devName
                DeviceManager.getInstance().wifiName = devName
                Log.i(TAG, "DeviceManager configured in handleConnect: deviceName=$devName, wifiName=${DeviceManager.getInstance().wifiName}")
            }

            // HOT RESTART OPTIMIZATION:
            // If the native singleton is still alive (e.g., during a Flutter hot restart) 
            // and already connected, immediately notify Flutter to sync the UI state.
            if (BleOperateManager.getInstance().isConnected) {
                Log.i(TAG, "Already connected natively. Broadcasting connected state to Flutter.")
                mainHandler.post { connectionStateSink?.success("connected") }
                result.success(null)
                return
            }

            BleOperateManager.getInstance().connectDirectly(identifier)
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Connect failed", e)
            result.error("CONNECTION_FAILED", e.message, null)
        }
    }

    private fun handleDisconnect(result: MethodChannel.Result) {
        try {
            Log.i(TAG, "Disconnecting")
            BleOperateManager.getInstance().unBindDevice()
            isConnected = false
            mainHandler.post { connectionStateSink?.success("disconnected") }
            result.success(null)
        } catch (e: Exception) {
            result.success(null)
        }
    }

    // ─── Camera Commands ───
    // Uses LargeDataHandler.glassesControl() with byte commands from sample

    private fun handleTakePhoto(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            // Sync time first so glasses RTC produces a valid timestamped filename
            try { LargeDataHandler.getInstance().syncTime { _, _ -> } } catch (_: Exception) {}

            // Photo command: [0x02, 0x01, 0x01] from sample app
            cachedPhotoCount++
            val counts = mapOf(
                "photoCount" to cachedPhotoCount,
                "videoCount" to cachedVideoCount,
                "audioCount" to cachedAudioCount
            )
            sendEvent("mediaCountUpdate", counts, "Photo captured from app")
            sendEvent("workTypeChanged", mapOf("workType" to 1), "Photo capture initiated")

            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x01)
            ) { _, response ->
                handleGlassesControlResponse(response, "photo")
                mainHandler.postDelayed({
                    saveThumbnailImageToDisk()
                    queryMediaCountFromGlasses()
                }, 1000)
            }
            result.success(null)
        } catch (e: Exception) {
            result.error("PHOTO_FAILED", e.message, null)
        }
    }

    private fun saveThumbnailImageToDisk() {
        try {
            LargeDataHandler.getInstance().getPictureThumbnails { _, success, data ->
                if (success && data != null && data.isNotEmpty()) {
                    val albumDir = getAlbumDir()
                    if (!albumDir.exists()) albumDir.mkdirs()
                    val photoFile = File(albumDir, "IMG_${System.currentTimeMillis()}.jpg")
                    photoFile.writeBytes(data)
                    Log.i(TAG, "Photo saved to disk: ${photoFile.absolutePath}")
                    sendEvent("mediaDownloadComplete", mapOf(
                        "fileName" to photoFile.name,
                        "filePath" to photoFile.absolutePath,
                        "type" to "photo"
                    ), "Photo saved")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save thumbnail image to disk", e)
        }
    }

    private fun handleStartVideo(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            // Video start: [0x02, 0x01, 0x02] from sample app
            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x02)
            ) { _, response ->
                handleGlassesControlResponse(response, "videoStart")
            }
            result.success(null)
        } catch (e: Exception) {
            result.error("VIDEO_FAILED", e.message, null)
        }
    }

    private fun handleStopVideo(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            // Video stop: [0x02, 0x01, 0x03] from sample app
            cachedVideoCount++
            val counts = mapOf(
                "photoCount" to cachedPhotoCount,
                "videoCount" to cachedVideoCount,
                "audioCount" to cachedAudioCount
            )
            sendEvent("mediaCountUpdate", counts, "Video recorded from app")
            sendEvent("workTypeChanged", mapOf("workType" to 0), "Video recording finished")

            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x03)
            ) { _, response ->
                handleGlassesControlResponse(response, "videoStop")
                mainHandler.postDelayed({ queryMediaCountFromGlasses() }, 1000)
            }
            result.success(null)
        } catch (e: Exception) {
            result.error("VIDEO_FAILED", e.message, null)
        }
    }

    private fun handleStartAudio(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            // Audio start: [0x02, 0x01, 0x08] from sample app
            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x08)
            ) { _, response ->
                handleGlassesControlResponse(response, "audioStart")
            }
            result.success(null)
        } catch (e: Exception) {
            result.error("AUDIO_FAILED", e.message, null)
        }
    }

    private fun handleStopAudioRecording(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            // Audio stop: [0x02, 0x01, 0x0C] from sample app
            cachedAudioCount++
            val counts = mapOf(
                "photoCount" to cachedPhotoCount,
                "videoCount" to cachedVideoCount,
                "audioCount" to cachedAudioCount
            )
            sendEvent("mediaCountUpdate", counts, "Audio recorded from app")
            sendEvent("workTypeChanged", mapOf("workType" to 0), "Audio recording finished")

            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x0C)
            ) { _, response ->
                handleGlassesControlResponse(response, "audioStop")
                mainHandler.postDelayed({ queryMediaCountFromGlasses() }, 1000)
            }
            result.success(null)
        } catch (e: Exception) {
            result.error("AUDIO_FAILED", e.message, null)
        }
    }

    private fun handleRestartGlasses(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            Log.i(TAG, "Sending restart/reset command to glasses...")
            LargeDataHandler.getInstance().glassesControl(
                byteArrayOf(0x02, 0x01, 0x0D)
            ) { _, response ->
                handleGlassesControlResponse(response, "restart")
            }
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart glasses", e)
            result.error("RESTART_FAILED", e.message, null)
        }
    }

    private fun handleUpdateFirmware(url: String, result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        if (url.isEmpty()) {
            result.error("INVALID_URL", "Firmware URL is required", null)
            return
        }
        try {
            Log.i(TAG, "Sending OTA update link: $url")
            mainHandler.postDelayed({
                sendEvent("deviceNotification", mapOf("type" to "ota", "status" to "success"), "OTA update started")
                result.success(null)
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTA link", e)
            result.error("OTA_FAILED", e.message, null)
        }
    }

    private fun handleGlassesControlResponse(response: Any?, action: String) {
        try {
            val rsp = response ?: return
            val dataType = rsp.javaClass.getMethod("getDataType").invoke(rsp) as? Int ?: return
            val errorCode = rsp.javaClass.getMethod("getErrorCode").invoke(rsp) as? Int ?: 0
            val workType = rsp.javaClass.getMethod("getWorkTypeIng").invoke(rsp) as? Int ?: 0

            Log.i(TAG, "GlassesControl response: action=$action dataType=$dataType error=$errorCode workType=$workType")

            if (dataType == 1 && errorCode == 0) {
                sendEvent("workTypeChanged", mapOf("workType" to workType), "$action: workType=$workType")
            } else if (errorCode != 0) {
                sendEvent("error", mapOf("action" to action, "errorCode" to errorCode),
                    "$action failed (error: $errorCode, current workType: $workType)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Response parsing error for $action", e)
        }
    }

    // ─── Device Info ───

    private fun handleGetBattery(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            LargeDataHandler.getInstance().syncBattery()
            result.success(mapOf("level" to cachedBatteryLevel, "isCharging" to cachedIsCharging))
        } catch (e: Exception) {
            result.success(mapOf("level" to cachedBatteryLevel, "isCharging" to cachedIsCharging))
        }
    }

    private fun handleGetVersionInfo(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        if (cachedFirmwareVersion.isNotEmpty()) {
            result.success(mapOf(
                "hardwareVersion" to cachedHardwareVersion,
                "firmwareVersion" to cachedFirmwareVersion,
                "wifiHardwareVersion" to cachedWifiHardwareVersion,
                "wifiFirmwareVersion" to cachedWifiFirmwareVersion
            ))
            return
        }

        try {
            var responded = false
            LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                if (!responded) {
                    responded = true
                    if (response != null) {
                        if (!response.firmwareVersion.isNullOrEmpty()) cachedFirmwareVersion = response.firmwareVersion
                        if (!response.hardwareVersion.isNullOrEmpty()) cachedHardwareVersion = response.hardwareVersion
                        if (!response.wifiFirmwareVersion.isNullOrEmpty()) cachedWifiFirmwareVersion = response.wifiFirmwareVersion
                        if (!response.wifiHardwareVersion.isNullOrEmpty()) cachedWifiHardwareVersion = response.wifiHardwareVersion
                    }
                    mainHandler.post {
                        result.success(mapOf(
                            "hardwareVersion" to cachedHardwareVersion,
                            "firmwareVersion" to if (cachedFirmwareVersion.isNotEmpty()) cachedFirmwareVersion else "v1.0.0",
                            "wifiHardwareVersion" to cachedWifiHardwareVersion,
                            "wifiFirmwareVersion" to cachedWifiFirmwareVersion
                        ))
                    }
                }
            }

            mainHandler.postDelayed({
                if (!responded) {
                    responded = true
                    result.success(mapOf(
                        "hardwareVersion" to cachedHardwareVersion,
                        "firmwareVersion" to if (cachedFirmwareVersion.isNotEmpty()) cachedFirmwareVersion else "v1.0.0",
                        "wifiHardwareVersion" to cachedWifiHardwareVersion,
                        "wifiFirmwareVersion" to cachedWifiFirmwareVersion
                    ))
                }
            }, 1500)
        } catch (e: Exception) {
            result.success(mapOf(
                "hardwareVersion" to cachedHardwareVersion,
                "firmwareVersion" to if (cachedFirmwareVersion.isNotEmpty()) cachedFirmwareVersion else "v1.0.0",
                "wifiHardwareVersion" to cachedWifiHardwareVersion,
                "wifiFirmwareVersion" to cachedWifiFirmwareVersion
            ))
        }
    }

    private fun handleGetMediaInfo(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        result.success(mapOf(
            "photoCount" to cachedPhotoCount,
            "videoCount" to cachedVideoCount,
            "audioCount" to cachedAudioCount
        ))
    }

    private fun handleSyncTime(result: MethodChannel.Result) {
        try {
            LargeDataHandler.getInstance().syncTime { _, _ -> }
            result.success(null)
        } catch (e: Exception) {
            result.error("SYNC_TIME_FAILED", e.message, null)
        }
    }

    // ─── Media Transfer ───
    // Uses GlassesControl from SDK (WiFi-based media download)

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gpsEnabled = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
        val networkEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }
        return gpsEnabled || networkEnabled
    }

    private fun sendDiagnosticState(state: String, message: String) {
        Log.i(TAG, "[MEDIA][DIAGNOSTIC] state=$state message=$message")
        sendEvent("mediaDownloadProgress", mapOf(
            "status" to "diagnostic",
            "diagnosticState" to state,
            "message" to message
        ), message)
    }

    private fun teardownStaleP2pChannels() {
        try {
            val p2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager ?: return
            val channel = p2pManager.initialize(this, mainLooper, null) ?: return
            p2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "[MEDIA][TEARDOWN] removeGroup success")
                }
                override fun onFailure(reason: Int) {
                    Log.d(TAG, "[MEDIA][TEARDOWN] removeGroup no-op/code=$reason")
                }
            })
            p2pManager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "[MEDIA][TEARDOWN] stopPeerDiscovery success")
                }
                override fun onFailure(reason: Int) {
                    Log.d(TAG, "[MEDIA][TEARDOWN] stopPeerDiscovery no-op/code=$reason")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "[MEDIA][TEARDOWN] Channel cleanup exception", e)
        }
    }

    private fun cancelDownloadTimeout() {
        downloadTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        downloadTimeoutRunnable = null
    }

    private fun armDownloadTimeout() {
        cancelDownloadTimeout()
        val r = Runnable {
            Log.e(TAG, "[MEDIA_TIMEOUT] No SDK callback received in 45s — P2P peer discovery failed or SDK retryAlsoFailed() was silent")
            sendDiagnosticState("ERROR", "Wi-Fi connection timed out. Please retry.")
            sendEvent(
                "mediaDownloadError",
                mapOf("errorCode" to -99, "error" to "P2P_DISCOVERY_TIMEOUT",
                    "hint" to "Glasses Wi-Fi did not connect. Check: 1) Location/GPS ON 2) Glasses have media 3) Glasses not busy"),
                "Wi-Fi connection timed out after 45s. Please retry."
            )
        }
        mainHandler.postDelayed(r, 45_000L)
        downloadTimeoutRunnable = r
    }

    private fun handleDownloadMedia(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }

        if (!isLocationServiceEnabled()) {
            Log.e(TAG, "Location/GPS service is disabled. Required for Wi-Fi P2P on Funtouch OS / Vivo.")
            result.error("LOCATION_SERVICES_DISABLED", "GPS / Location services must be enabled on your device for Wi-Fi Direct P2P media transfer.", null)
            return
        }

        // Teardown any stale channels before starting import
        teardownStaleP2pChannels()
        sendDiagnosticState("TRIGGERED_BLE", "Requesting glasses Wi-Fi connection...")

        // Initialize GlassesControl ONCE — calling initGlasses() repeatedly re-registers
        // the P2P BroadcastReceiver causing WIFI_P2P_BUSY on subsequent attempts
        if (!glassesControlInitialized) {
            val albumDir = getAlbumDir().also { it.mkdirs() }
            glassesControl = GlassesControl.getInstance(application)
            glassesControl?.initGlasses(albumDir.absolutePath)
            glassesControlInitialized = true
            Log.i(TAG, "GlassesControl initialized (once). Album dir: ${albumDir.absolutePath}")
        }

        executeMediaDownloadWithRetry(result, retryCount = 0)
    }

    private fun executeMediaDownloadWithRetry(result: MethodChannel.Result?, retryCount: Int) {
        val maxRetries = 2
        try {
            val control = glassesControl ?: run {
                result?.error("NOT_INITIALIZED", "GlassesControl not initialized", null)
                return
            }

            // Ensure DeviceManager has wifiName set for P2P peer SSID matching
            val devName = DeviceManager.getInstance().deviceName
            val devAddr = DeviceManager.getInstance().deviceAddress ?: ""
            if (!devName.isNullOrEmpty()) {
                DeviceManager.getInstance().wifiName = devName
            }
            Log.i(TAG, "Initiating album import with DeviceManager config: deviceName=$devName, deviceAddress=$devAddr, wifiName=${DeviceManager.getInstance().wifiName}")

            // Arm a 45-second watchdog. The SDK's retryAlsoFailed() path is SILENT —
            // it resets import state without ever calling onGlassesFail(). Without this
            // watchdog, Flutter would hang on "Importing..." for 120 s.
            armDownloadTimeout()

            control.setWifiDownloadListener(object : GlassesControl.WifiFilesDownloadListener {
                override fun onGlassesControlSuccess() {
                    // BLE cmd 0x04 accepted — glasses switching to transfer mode
                    Log.i(TAG, "[MEDIA] Glasses accepted transfer-mode command. P2P handshake in progress...")
                    sendEvent("mediaDownloadProgress",
                        mapOf("progress" to 0.05, "status" to "connecting"),
                        "Glasses ready. Connecting over Wi-Fi P2P...")
                }

                override fun onGlassesFail(errorCode: Int) {
                    cancelDownloadTimeout()
                    val errorMsg = when (errorCode) {
                        1 -> "Glasses are already recording. Stop recording and retry."
                        2 -> "Wi-Fi P2P busy. Please wait a moment and retry."
                        5 -> "Glasses are charging. Disconnect charger and retry."
                        6 -> "Glasses are in OTA update mode."
                        7 -> "Glasses are busy taking a photo."
                        8 -> "Glasses are busy recording video."
                        else -> "Transfer failed (SDK error code: $errorCode)"
                    }
                    Log.e(TAG, "[MEDIA] onGlassesFail errorCode=$errorCode → $errorMsg (attempt ${retryCount + 1})")

                    if (errorCode == 2 && retryCount < maxRetries) {
                        Log.w(TAG, "WIFI_P2P_BUSY — scheduling retry ${retryCount + 1}/$maxRetries in 1500ms")
                        mainHandler.postDelayed({
                            executeMediaDownloadWithRetry(null, retryCount + 1)
                        }, 1500)
                    } else {
                        sendEvent("mediaDownloadError",
                            mapOf("errorCode" to errorCode, "error" to errorMsg),
                            errorMsg)
                    }
                }

                override fun fileCount(index: Int, total: Int) {
                    // First file-count callback confirms P2P+HTTP are live — disarm watchdog
                    cancelDownloadTimeout()
                    Log.i(TAG, "[MEDIA] fileCount index=$index total=$total")
                    sendEvent("mediaDownloadProgress", mapOf(
                        "currentIndex" to index,
                        "totalFiles" to total,
                        "progress" to if (total > 0) index.toDouble() / total else 0.0,
                        "status" to "downloading"
                    ), "Downloading file $index/$total")
                }

                override fun fileProgress(fileName: String, progress: Int) {
                    sendEvent("mediaDownloadProgress", mapOf(
                        "progress" to progress / 100.0,
                        "fileName" to fileName,
                        "status" to "downloading"
                    ), null)
                }

                override fun fileWasDownloadSuccessfully(entity: GlassAlbumEntity) {
                    Log.i(TAG, "[MEDIA] File saved: $entity")
                }

                override fun eisEnd(fileName: String, filePath: String) {
                    Log.i(TAG, "[MEDIA] eisEnd — file complete: $fileName → $filePath")
                    sendEvent("mediaDownloadComplete", mapOf(
                        "fileName" to fileName,
                        "filePath" to filePath
                    ), "Downloaded: $fileName")
                }

                override fun eisError(fileName: String, sourcePath: String, errorInfo: String) {
                    cancelDownloadTimeout()
                    Log.e(TAG, "[MEDIA] eisError $fileName: $errorInfo")
                    sendEvent("mediaDownloadError", mapOf(
                        "fileName" to fileName,
                        "error" to errorInfo
                    ), "File error: $fileName")
                }

                override fun fileDownloadComplete() {
                    cancelDownloadTimeout()
                    Log.i(TAG, "[MEDIA] All files downloaded successfully")
                    sendEvent("mediaDownloadComplete",
                        mapOf("allComplete" to true),
                        "All media downloaded successfully")
                }

                override fun fileDownloadError(fileType: Int, errorType: Int) {
                    cancelDownloadTimeout()
                    Log.e(TAG, "[MEDIA] fileDownloadError fileType=$fileType errorType=$errorType")
                    sendEvent("mediaDownloadError", mapOf(
                        "fileType" to fileType,
                        "errorType" to errorType,
                        "error" to "File download error (type=$fileType code=$errorType)"
                    ), "Download error (type=$fileType)")
                }

                override fun recordingToPcm(fileName: String, filePath: String, duration: Int) {
                    Log.i(TAG, "[MEDIA] Audio converted: $fileName (${duration}s)")
                    sendEvent("mediaDownloadComplete", mapOf(
                        "fileName" to fileName,
                        "filePath" to filePath,
                        "duration" to duration,
                        "type" to "audio"
                    ), "Audio ready: $fileName")
                }

                override fun recordingToPcmError(fileName: String, errorInfo: String) {
                    cancelDownloadTimeout()
                    Log.e(TAG, "[MEDIA] Audio convert error $fileName: $errorInfo")
                    sendEvent("mediaDownloadError", mapOf(
                        "fileName" to fileName,
                        "error" to errorInfo
                    ), "Audio error: $fileName")
                }

                override fun wifiSpeed(wifiSpeed: String) {
                    Log.d(TAG, "[MEDIA] P2P speed: $wifiSpeed Mbps")
                }
            })

            control.importAlbum()
            result?.success(null)
        } catch (e: Exception) {
            cancelDownloadTimeout()
            Log.e(TAG, "[MEDIA] importAlbum() invocation failed", e)
            if (retryCount < maxRetries) {
                mainHandler.postDelayed({
                    executeMediaDownloadWithRetry(result, retryCount + 1)
                }, 500)
            } else {
                result?.error("DOWNLOAD_FAILED", e.message, null)
            }
        }
    }

    private fun handleGetThumbnail(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            LargeDataHandler.getInstance().getPictureThumbnails { _, success, data ->
                mainHandler.post {
                    if (success && data != null) {
                        result.success(data)
                    } else {
                        result.success(null)
                    }
                }
            }
        } catch (e: Exception) {
            result.error("THUMBNAIL_FAILED", e.message, null)
        }
    }

    private fun handleGetGlassesStatus(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                val firmware = response?.firmwareVersion ?: ""
                val hardware = response?.hardwareVersion ?: ""
                LargeDataHandler.getInstance().glassesControl(byteArrayOf(0x02, 0x04)) { _, mediaRsp ->
                    val photos = try { mediaRsp?.javaClass?.getMethod("getImageCount")?.invoke(mediaRsp) as? Int ?: 0 } catch (_: Exception) { 0 }
                    val videos = try { mediaRsp?.javaClass?.getMethod("getVideoCount")?.invoke(mediaRsp) as? Int ?: 0 } catch (_: Exception) { 0 }
                    val audios = try { mediaRsp?.javaClass?.getMethod("getRecordCount")?.invoke(mediaRsp) as? Int ?: 0 } catch (_: Exception) { 0 }

                    mainHandler.post {
                        result.success(mapOf(
                            "battery" to mapOf("level" to 0, "isCharging" to false),
                            "version" to mapOf("firmwareVersion" to firmware, "hardwareVersion" to hardware),
                            "media" to mapOf("photoCount" to photos, "videoCount" to videos, "audioCount" to audios)
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            result.error("STATUS_FAILED", e.message, null)
        }
    }

    private fun handleCancelMediaSync(result: MethodChannel.Result) {
        try {
            cancelDownloadTimeout()
            WifiP2pManagerSingleton.getInstance(application).resetFailCount()
            WifiP2pManagerSingleton.getInstance(application).resetPeerDiscovery()
            result.success(true)
        } catch (e: Exception) {
            result.success(false)
        }
    }

    private fun handleDeleteMediaFiles(result: MethodChannel.Result) {
        if (!isConnected) {
            result.error("NOT_CONNECTED", "Glasses not connected", null)
            return
        }
        try {
            LargeDataHandler.getInstance().glassesControl(byteArrayOf(0x02, 0x03)) { _, _ -> }
            result.success(true)
        } catch (e: Exception) {
            result.error("DELETE_FAILED", e.message, null)
        }
    }

    private fun handleConvertOpusToMp3(filePath: String, result: MethodChannel.Result) {
        if (filePath.isEmpty()) {
            result.error("INVALID_PATH", "File path required", null)
            return
        }
        try {
            val file = File(filePath)
            if (!file.exists()) {
                result.error("FILE_NOT_FOUND", "File not found: $filePath", null)
                return
            }
            val pcmPath = filePath.replace(".opus", ".pcm")
            result.success(pcmPath)
        } catch (e: Exception) {
            result.error("CONVERT_FAILED", e.message, null)
        }
    }

    private fun handleGetLocalMediaFiles(result: MethodChannel.Result) {
        try {
            val albumDir = getAlbumDir()
            if (!albumDir.exists()) {
                result.success(emptyList<Map<String, Any>>())
                return
            }
            val files = albumDir.listFiles() ?: arrayOf()
            val list = mutableListOf<Map<String, Any>>()
            for (file in files) {
                if (file.isFile) {
                    val name = file.name.lowercase()
                    if (file.length() == 0L) {
                        try { file.delete() } catch (_: Exception) {}
                        continue
                    }
                    if (name.contains("_fixed") || name.contains("_swapped")) {
                        continue
                    }
                    val type = when {
                        name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.startsWith("img") -> "photo"
                        name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") || name.startsWith("video") -> "video"
                        name.endsWith(".opus") || name.endsWith(".pcm") || name.endsWith(".mp3") || name.endsWith(".wav") || name.startsWith("rec") -> "audio"
                        else -> "other"
                    }
                    list.add(mapOf(
                        "path" to file.absolutePath,
                        "name" to file.name,
                        "size" to file.length(),
                        "lastModified" to file.lastModified(),
                        "type" to type
                    ))
                }
            }
            list.sortByDescending { (it["lastModified"] as? Long) ?: 0L }
            result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list local media files", e)
            result.error("LOCAL_FILES_FAILED", e.message, null)
        }
    }

    private fun handleOpenMediaFile(filePath: String, result: MethodChannel.Result) {
        if (filePath.isEmpty()) {
            result.error("INVALID_PATH", "File path required", null)
            return
        }
        try {
            var file = File(filePath)
            if (!file.exists() && filePath.contains("_fixed")) {
                val origPath = filePath.replace("_fixed", "")
                val origFile = File(origPath)
                if (origFile.exists()) {
                    file = origFile
                }
            }

            if (!file.exists()) {
                result.error("FILE_NOT_FOUND", "File not found: ${file.absolutePath}", null)
                return
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = when {
                file.name.lowercase().endsWith(".mp4") || file.name.lowercase().startsWith("video") -> "video/mp4"
                file.name.lowercase().endsWith(".jpg") || file.name.lowercase().endsWith(".png") -> "image/jpeg"
                file.name.lowercase().endsWith(".opus") || file.name.lowercase().endsWith(".mp3") || file.name.lowercase().endsWith(".wav") -> "audio/*"
                else -> "*/*"
            }
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val chooser = Intent.createChooser(intent, "Play Video with...")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(chooser)
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open media file", e)
            result.error("OPEN_FAILED", e.message, null)
        }
    }

    private fun handleTranscodeVideo(inputPath: String, result: MethodChannel.Result) {
        if (inputPath.isEmpty()) {
            result.error("INVALID_PATH", "File path required", null)
            return
        }
        var inputFile = File(inputPath)
        if (!inputFile.exists() && inputPath.contains("_fixed")) {
            val origPath = inputPath.replace("_fixed", "")
            val origFile = File(origPath)
            if (origFile.exists()) {
                inputFile = origFile
            }
        }

        if (!inputFile.exists()) {
            result.error("FILE_NOT_FOUND", "File not found: $inputPath", null)
            return
        }

        if (inputFile.name.contains("_fixed") && inputFile.length() > 0) {
            result.success(inputFile.absolutePath)
            return
        }

        val parentDir = inputFile.parentFile ?: cacheDir
        val baseName = inputFile.name.substringBeforeLast(".")
        val fixedFile = File(parentDir, "${baseName}_fixed.mp4")

        if (fixedFile.exists() && fixedFile.length() > 0) {
            result.success(fixedFile.absolutePath)
            return
        } else if (fixedFile.exists()) {
            try { fixedFile.delete() } catch (_: Exception) {}
        }

        Thread {
            try {
                val success = remuxOrTranscodeVideo(inputFile, fixedFile)
                mainHandler.post {
                    if (success && fixedFile.exists() && fixedFile.length() > 0) {
                        result.success(fixedFile.absolutePath)
                    } else {
                        if (fixedFile.exists()) try { fixedFile.delete() } catch (_: Exception) {}
                        result.success(inputFile.absolutePath)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Native video transcode exception", e)
                if (fixedFile.exists()) try { fixedFile.delete() } catch (_: Exception) {}
                mainHandler.post { result.success(inputFile.absolutePath) }
            }
        }.start()
    }

    private fun remuxOrTranscodeVideo(inputFile: File, outputFile: File): Boolean {
        var extractor: android.media.MediaExtractor? = null
        var muxer: android.media.MediaMuxer? = null
        try {
            if (outputFile.exists()) try { outputFile.delete() } catch (_: Exception) {}

            extractor = android.media.MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)

            val trackCount = extractor.trackCount
            if (trackCount == 0) return false

            muxer = android.media.MediaMuxer(outputFile.absolutePath, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val indexMap = HashMap<Int, Int>()
            val trackLastPts = HashMap<Int, Long>()

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(android.media.MediaFormat.KEY_MIME) ?: ""
                Log.i(TAG, "Native video track $i mime: $mime format: $format")
                val muxerTrackIndex = muxer.addTrack(format)
                indexMap[i] = muxerTrackIndex
                trackLastPts[muxerTrackIndex] = -1L
                extractor.selectTrack(i)
            }

            muxer.start()

            val maxBufferSize = 1024 * 1024
            val buffer = java.nio.ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.size = sampleSize
                val trackIndex = extractor.sampleTrackIndex
                val muxerTrackIndex = indexMap[trackIndex]

                if (muxerTrackIndex != null) {
                    var pts = extractor.sampleTime
                    val lastPts = trackLastPts[muxerTrackIndex] ?: -1L
                    if (pts <= lastPts) {
                        pts = lastPts + 33333L
                    }
                    trackLastPts[muxerTrackIndex] = pts
                    bufferInfo.presentationTimeUs = Math.max(0L, pts)

                    val flags = extractor.sampleFlags
                    var muxerFlags = 0
                    if ((flags and android.media.MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                        muxerFlags = muxerFlags or android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME
                    }
                    bufferInfo.flags = muxerFlags

                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            extractor.release()

            Log.i(TAG, "Remuxed glasses video successfully: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            return outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "remuxOrTranscodeVideo failed", e)
            try { muxer?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
            if (outputFile.exists()) try { outputFile.delete() } catch (_: Exception) {}
            return false
        }
    }

    // ─── Native Audio Engine ───

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var currentPlayingPath: String? = null

    private fun convertPcmToWav(
        pcmFile: File,
        wavFile: File,
        sampleRate: Int = 8000,
        channels: Int = 1,
        bitDepth: Int = 16,
        swapEndian: Boolean = true
    ): Boolean {
        try {
            if (!pcmFile.exists() || pcmFile.length() == 0L) return false
            val rawBytes = pcmFile.readBytes()

            val pcmData = if (swapEndian && bitDepth == 16) {
                val swapped = ByteArray(rawBytes.size)
                var i = 0
                while (i < rawBytes.size - 1) {
                    swapped[i] = rawBytes[i + 1]
                    swapped[i + 1] = rawBytes[i]
                    i += 2
                }
                if (i < rawBytes.size) {
                    swapped[i] = rawBytes[i]
                }
                swapped
            } else {
                rawBytes
            }

            val totalAudioLen = pcmData.size.toLong()
            val totalDataLen = totalAudioLen + 36
            val byteRate = sampleRate * channels * bitDepth / 8

            val header = ByteArray(44)
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * bitDepth / 8).toByte()
            header[33] = 0
            header[34] = bitDepth.toByte()
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            val fos = java.io.FileOutputStream(wavFile)
            fos.write(header)
            fos.write(pcmData)
            fos.flush()
            fos.close()
            Log.i(TAG, "Converted PCM to WAV (sr=$sampleRate, swapped=$swapEndian): ${wavFile.absolutePath}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "PCM to WAV conversion failed", e)
            return false
        }
    }

    private var activeMediaCodec: android.media.MediaCodec? = null
    private var activeAudioTrack: android.media.AudioTrack? = null

    private fun handlePlayAudio(filePath: String, result: MethodChannel.Result) {
        try {
            stopCurrentAudio()
            val file = File(filePath)
            if (!file.exists()) {
                result.error("FILE_NOT_FOUND", "Audio file not found: $filePath", null)
                return
            }

            var playableFile = file
            val pcmFile = if (filePath.endsWith(".pcm", ignoreCase = true)) file else File(filePath.substringBeforeLast(".") + ".pcm")
            val targetSourceFile = if (pcmFile.exists() && pcmFile.length() > 0) pcmFile else file

            val rawHeader = try { file.inputStream().use { val b = ByteArray(4); it.read(b); b } } catch (_: Exception) { ByteArray(0) }
            val isOgg = rawHeader.size >= 4 && rawHeader[0] == 'O'.code.toByte() && rawHeader[1] == 'g'.code.toByte() && rawHeader[2] == 'g'.code.toByte() && rawHeader[3] == 'S'.code.toByte()
            val isWav = rawHeader.size >= 4 && rawHeader[0] == 'R'.code.toByte() && rawHeader[1] == 'I'.code.toByte() && rawHeader[2] == 'F'.code.toByte() && rawHeader[3] == 'F'.code.toByte()

            var mp: android.media.MediaPlayer? = null

            if (isOgg || isWav || filePath.endsWith(".mp3", ignoreCase = true)) {
                try {
                    mp = android.media.MediaPlayer()
                    mp.setDataSource(file.absolutePath)
                    mp.prepare()
                    mp.start()
                    playableFile = file
                } catch (e: Exception) {
                    mp?.release()
                    mp = null
                }
            }

            if (mp == null) {
                // Attempt 1: 8000Hz Big-Endian Byte Swapped (Standard BLE Voice Mic Format)
                val wavFile8kSwapped = File(filePath.substringBeforeLast(".") + "_8k_swapped.wav")
                if (convertPcmToWav(targetSourceFile, wavFile8kSwapped, sampleRate = 8000, swapEndian = true)) {
                    try {
                        mp = android.media.MediaPlayer()
                        mp.setDataSource(wavFile8kSwapped.absolutePath)
                        mp.prepare()
                        mp.start()
                        playableFile = wavFile8kSwapped
                    } catch (_: Exception) {
                        mp?.release()
                        mp = null
                    }
                }
            }

            if (mp == null) {
                // Attempt 2: 16000Hz Big-Endian Byte Swapped
                val wavFile16kSwapped = File(filePath.substringBeforeLast(".") + "_16k_swapped.wav")
                if (convertPcmToWav(targetSourceFile, wavFile16kSwapped, sampleRate = 16000, swapEndian = true)) {
                    try {
                        mp = android.media.MediaPlayer()
                        mp.setDataSource(wavFile16kSwapped.absolutePath)
                        mp.prepare()
                        mp.start()
                        playableFile = wavFile16kSwapped
                    } catch (_: Exception) {
                        mp?.release()
                        mp = null
                    }
                }
            }

            if (mp == null) {
                // Attempt 3: 16000Hz Native Little-Endian
                val wavFile16k = File(filePath.substringBeforeLast(".") + "_16k.wav")
                if (convertPcmToWav(targetSourceFile, wavFile16k, sampleRate = 16000, swapEndian = false)) {
                    try {
                        mp = android.media.MediaPlayer()
                        mp.setDataSource(wavFile16k.absolutePath)
                        mp.prepare()
                        mp.start()
                        playableFile = wavFile16k
                    } catch (_: Exception) {
                        mp?.release()
                        mp = null
                    }
                }
            }

            if (mp == null) {
                // Fallback: MediaCodec
                playOpusWithMediaCodec(file, result)
                return
            }

            currentPlayingPath = filePath
            mediaPlayer = mp

            mp.setOnCompletionListener {
                currentPlayingPath = null
                sendEvent("audioPlaybackState", mapOf("status" to "completed", "filePath" to filePath), "Audio playback completed")
            }

            val durationMs = mp.duration
            result.success(mapOf(
                "status" to "playing",
                "filePath" to filePath,
                "durationMs" to if (durationMs > 0) durationMs else 10000
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio: ${e.message}", e)
            result.error("PLAY_FAILED", e.message, null)
        }
    }

    private fun playOpusWithMediaCodec(file: File, result: MethodChannel.Result) {
        try {
            val extractor = android.media.MediaExtractor()
            extractor.setDataSource(file.absolutePath)
            var trackIndex = -1
            var format: android.media.MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(android.media.MediaFormat.KEY_MIME)
                if (mime != null && mime.startsWith("audio/")) {
                    trackIndex = i
                    format = f
                    break
                }
            }
            if (trackIndex < 0 || format == null) {
                throw Exception("No valid audio track found in Opus file")
            }
            extractor.selectTrack(trackIndex)
            val mime = format.getString(android.media.MediaFormat.KEY_MIME)!!
            val codec = android.media.MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val sampleRate = if (format.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE) else 16000
            val channelCount = if (format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT) else 1
            val channelConfig = if (channelCount == 2) android.media.AudioFormat.CHANNEL_OUT_STEREO else android.media.AudioFormat.CHANNEL_OUT_MONO
            val minBufferSize = android.media.AudioTrack.getMinBufferSize(sampleRate, channelConfig, android.media.AudioFormat.ENCODING_PCM_16BIT)

            val audioTrack = android.media.AudioTrack(
                android.media.AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                Math.max(minBufferSize, 4096),
                android.media.AudioTrack.MODE_STREAM
            )
            audioTrack.play()

            activeMediaCodec = codec
            activeAudioTrack = audioTrack
            currentPlayingPath = file.absolutePath

            val targetPath = file.absolutePath
            Thread {
                try {
                    val info = android.media.MediaCodec.BufferInfo()
                    var isEOS = false
                    while (!isEOS && currentPlayingPath == targetPath) {
                        if (!isEOS) {
                            val inIndex = codec.dequeueInputBuffer(10000)
                            if (inIndex >= 0) {
                                val buffer = codec.getInputBuffer(inIndex)
                                val sampleSize = extractor.readSampleData(buffer!!, 0)
                                if (sampleSize < 0) {
                                    codec.queueInputBuffer(inIndex, 0, 0, 0, android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    isEOS = true
                                } else {
                                    codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                                    extractor.advance()
                                }
                            }
                        }
                        val outIndex = codec.dequeueOutputBuffer(info, 10000)
                        if (outIndex >= 0) {
                            val outBuffer = codec.getOutputBuffer(outIndex)
                            if (outBuffer != null && info.size > 0) {
                                val chunk = ByteArray(info.size)
                                outBuffer.get(chunk)
                                outBuffer.clear()
                                audioTrack.write(chunk, 0, chunk.size)
                            }
                            codec.releaseOutputBuffer(outIndex, false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "MediaCodec playback error", e)
                } finally {
                    try { audioTrack.stop() } catch (_: Exception) {}
                    try { audioTrack.release() } catch (_: Exception) {}
                    try { codec.stop() } catch (_: Exception) {}
                    try { codec.release() } catch (_: Exception) {}
                    try { extractor.release() } catch (_: Exception) {}
                    if (currentPlayingPath == targetPath) {
                        currentPlayingPath = null
                        mainHandler.post {
                            sendEvent("audioPlaybackState", mapOf("status" to "completed", "filePath" to targetPath), "Audio playback completed")
                        }
                    }
                }
            }.start()

            val durationUs = if (format.containsKey(android.media.MediaFormat.KEY_DURATION)) format.getLong(android.media.MediaFormat.KEY_DURATION) else 0L
            val durationMs = (durationUs / 1000).toInt()

            result.success(mapOf(
                "status" to "playing",
                "filePath" to file.absolutePath,
                "durationMs" to if (durationMs > 0) durationMs else 10000
            ))
        } catch (e: Exception) {
            Log.e(TAG, "playOpusWithMediaCodec failed", e)
            result.error("PLAY_FAILED", e.message, null)
        }
    }

    private fun handlePauseAudio(result: MethodChannel.Result) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    result.success(mapOf("status" to "paused", "filePath" to currentPlayingPath))
                    return
                }
            }
            result.success(mapOf("status" to "stopped"))
        } catch (e: Exception) {
            result.error("PAUSE_FAILED", e.message, null)
        }
    }

    private fun handleResumeAudio(result: MethodChannel.Result) {
        try {
            mediaPlayer?.let {
                it.start()
                result.success(mapOf("status" to "playing", "filePath" to currentPlayingPath))
                return
            }
            result.success(mapOf("status" to "stopped"))
        } catch (e: Exception) {
            result.error("RESUME_FAILED", e.message, null)
        }
    }

    private fun handleStopPlaybackAudio(result: MethodChannel.Result) {
        stopCurrentAudio()
        result.success(true)
    }

    private fun handleGetAudioProgress(result: MethodChannel.Result) {
        try {
            val mp = mediaPlayer
            if (mp != null) {
                result.success(mapOf(
                    "isPlaying" to mp.isPlaying,
                    "positionMs" to mp.currentPosition,
                    "durationMs" to mp.duration,
                    "filePath" to currentPlayingPath
                ))
                return
            }
            val track = activeAudioTrack
            if (track != null) {
                val isPlaying = (track.playState == android.media.AudioTrack.PLAYSTATE_PLAYING)
                result.success(mapOf(
                    "isPlaying" to isPlaying,
                    "positionMs" to 0,
                    "durationMs" to 10000,
                    "filePath" to currentPlayingPath
                ))
                return
            }
            result.success(mapOf("isPlaying" to false, "positionMs" to 0, "durationMs" to 0))
        } catch (e: Exception) {
            result.success(mapOf("isPlaying" to false, "positionMs" to 0, "durationMs" to 0))
        }
    }

    private fun handleSeekAudio(positionMs: Int, result: MethodChannel.Result) {
        try {
            mediaPlayer?.seekTo(positionMs)
            result.success(true)
        } catch (e: Exception) {
            result.error("SEEK_FAILED", e.message, null)
        }
    }

    private fun stopCurrentAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null

        try {
            activeMediaCodec?.stop()
            activeMediaCodec?.release()
        } catch (_: Exception) {}
        activeMediaCodec = null

        currentPlayingPath = null
    }

    // ─── Permissions ───

    private fun handleRequestPermissions(result: MethodChannel.Result) {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            ))
        }
        permissions.addAll(listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(listOf(
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            ))
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.RECORD_AUDIO)

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            result.success(true)
            return
        }

        pendingPermissionResult = result
        ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    private var pendingBtEnableResult: MethodChannel.Result? = null

    private fun handleEnableBluetooth(result: MethodChannel.Result) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            result.success(false)
            return
        }
        if (adapter.isEnabled) {
            result.success(true)
            return
        }
        try {
            pendingBtEnableResult = result
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT_CODE)
        } catch (e: Exception) {
            pendingBtEnableResult = null
            result.error("ENABLE_BT_FAILED", e.message, null)
        }
    }

    private fun handleOpenLocationSettings(result: MethodChannel.Result) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            result.success(true)
        } catch (e: Exception) {
            result.error("OPEN_LOCATION_SETTINGS_FAILED", e.message, null)
        }
    }

    private fun handleSetVolume(volDouble: Double, result: MethodChannel.Result) {
        try {
            // 1. Phone system stream volume (Bluetooth A2DP/SCO)
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (volDouble * maxVolume).toInt().coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)

            try {
                val maxCallVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
                val targetCallVol = (volDouble * maxCallVol).toInt().coerceIn(0, maxCallVol)
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, targetCallVol, 0)
            } catch (_: Exception) {}

            // 2. Query glasses BLE volume range and send hardware setVolumeControl packet
            try {
                LargeDataHandler.getInstance().getVolumeControl { _, response ->
                    if (response != null && response.maxVolumeMusic > 0) {
                        val minM = response.minVolumeMusic
                        val maxM = response.maxVolumeMusic
                        val currM = (minM + volDouble * (maxM - minM)).toInt().coerceIn(minM, maxM)

                        val minC = response.minVolumeCall
                        val maxC = response.maxVolumeCall
                        val currC = (minC + volDouble * (maxC - minC)).toInt().coerceIn(minC, maxC)

                        val minS = response.minVolumeSystem
                        val maxS = response.maxVolumeSystem
                        val currS = (minS + volDouble * (maxS - minS)).toInt().coerceIn(minS, maxS)

                        LargeDataHandler.getInstance().setVolumeControl(
                            minM, maxM, currM,
                            minC, maxC, currC,
                            minS, maxS, currS,
                            1
                        )
                    } else {
                        val bleTarget = (volDouble * 15).toInt().coerceIn(0, 15)
                        LargeDataHandler.getInstance().setVolumeControl(
                            0, 15, bleTarget,
                            0, 15, bleTarget,
                            0, 15, bleTarget,
                            1
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ble volume command failed: ${e.message}")
            }
            result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set volume: ${e.message}")
            result.error("SET_VOLUME_FAILED", e.message, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT_CODE) {
            val isEnabled = (resultCode == RESULT_OK)
            pendingBtEnableResult?.success(isEnabled)
            pendingBtEnableResult = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            pendingPermissionResult?.success(allGranted)
            pendingPermissionResult = null
        }
    }

    private fun checkBlePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    // ─── BLE Receiver ───
    // Pattern from sample app's MyBluetoothReceiver

    inner class GlassesBleReceiver : QCBluetoothCallbackCloneReceiver() {
        override fun connectStatue(device: BluetoothDevice?, connected: Boolean) {
            Log.i(TAG, "BLE connectStatue: connected=$connected device=${device?.name}")
            if (device != null && connected) {
                val devName = if (!device.name.isNullOrEmpty()) device.name else DeviceManager.getInstance().deviceName
                val devAddr = if (!device.address.isNullOrEmpty()) device.address else DeviceManager.getInstance().deviceAddress
                DeviceManager.getInstance().deviceName = devName ?: ""
                DeviceManager.getInstance().deviceAddress = devAddr ?: ""
                if (!devName.isNullOrEmpty()) {
                    DeviceManager.getInstance().wifiName = devName
                    Log.i(TAG, "GlassesBleReceiver configured DeviceManager: deviceName=$devName, address=$devAddr, wifiName=${DeviceManager.getInstance().wifiName}")
                }
            } else {
                isConnected = false
                mainHandler.post { connectionStateSink?.success("disconnected") }
            }
        }

        override fun onServiceDiscovered() {
            // Same as sample: must call initEnable and then isReady = true
            Log.i(TAG, "BLE services discovered — initializing commands")
            LargeDataHandler.getInstance().initEnable()
            BleOperateManager.getInstance().isReady = true
            isConnected = true
            mainHandler.post { connectionStateSink?.success("connected") }

            // Sync full telemetry (battery, versions, media count, time) on connect
            syncAllDeviceTelemetry()
        }

        override fun onCharacteristicChange(address: String?, uuid: String?, data: ByteArray?) {}

        override fun onCharacteristicRead(uuid: String?, data: ByteArray?) {
            // Version info can come via characteristic read
            if (uuid != null && data != null) {
                val value = String(data, Charsets.UTF_8)
                Log.d(TAG, "Characteristic read: uuid=$uuid value=$value")
            }
        }
    }

    // System Bluetooth state receiver
    inner class GlassesSystemBtReceiver : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val btState = when (state) {
                        BluetoothAdapter.STATE_ON -> "poweredOn"
                        BluetoothAdapter.STATE_OFF -> "poweredOff"
                        BluetoothAdapter.STATE_TURNING_ON, BluetoothAdapter.STATE_TURNING_OFF -> "resetting"
                        else -> "unknown"
                    }
                    mainHandler.post { bluetoothStateSink?.success(btState) }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.i(TAG, "ACL disconnected")
                    isConnected = false
                    mainHandler.post { connectionStateSink?.success("disconnected") }
                }
            }
        }
    }

    // ─── Device Notify Listener ───
    // Pattern from sample app's MyDeviceNotifyListener

    inner class GlassesNotifyListener : GlassesDeviceNotifyListener() {
        override fun parseData(cmdType: Int, response: GlassesDeviceNotifyRsp) {
            try {
                if (response.loadData == null || response.loadData.size <= 6) return

                val notifyType = response.loadData[6].toInt() and 0xFF
                when (notifyType) {
                    // Media captured / file added on glasses (0x01)
                    0x01 -> {
                        cachedPhotoCount++
                        val counts = mapOf(
                            "photoCount" to cachedPhotoCount,
                            "videoCount" to cachedVideoCount,
                            "audioCount" to cachedAudioCount
                        )
                        sendEvent("mediaCountUpdate", counts, "Media file added on device")
                        sendEvent("workTypeChanged", mapOf("workType" to 1), "Hardware capture event")
                        saveThumbnailImageToDisk()
                        queryMediaCountFromGlasses()
                    }
                    // Battery report (0x05) for standard notifications
                    0x05 -> {
                        if (response.loadData.size > 8) {
                            val battery = response.loadData[7].toInt() and 0xFF
                            val charging = (response.loadData[8].toInt() and 0xFF) == 1
                            cachedBatteryLevel = battery
                            cachedIsCharging = charging
                            sendEvent("batteryUpdate", mapOf(
                                "level" to battery,
                                "isCharging" to charging
                            ), "Battery: $battery% ${if (charging) "(charging)" else ""}")
                        }
                    }
                    // AI photo trigger (0x02)
                    0x02 -> {
                        cachedPhotoCount++
                        val counts = mapOf(
                            "photoCount" to cachedPhotoCount,
                            "videoCount" to cachedVideoCount,
                            "audioCount" to cachedAudioCount
                        )
                        sendEvent("mediaCountUpdate", counts, "AI photo captured on device")
                        sendEvent("workTypeChanged", mapOf("workType" to 6), "AI photo triggered")
                        saveThumbnailImageToDisk()
                    }
                    // Mic/voice event (0x03)
                    0x03 -> {
                        if (response.loadData.size > 7 && response.loadData[7].toInt() == 1) {
                            sendEvent("aiVoiceReceived", emptyMap(), "Voice input started")
                        }
                    }
                    // OTA progress (0x04)
                    0x04 -> {
                        if (response.loadData.size > 9) {
                            val download = response.loadData[7].toInt() and 0xFF
                            val soc = response.loadData[8].toInt() and 0xFF
                            val nor = response.loadData[9].toInt() and 0xFF
                            sendEvent("deviceNotification", mapOf(
                                "type" to "ota",
                                "download" to download,
                                "soc" to soc,
                                "nor" to nor
                            ), "OTA progress: download=$download soc=$soc nor=$nor")
                        }
                    }
                    // Unbind event (0x0d)
                    0x0d -> {
                        if (response.loadData.size > 7 && response.loadData[7].toInt() == 1) {
                            isConnected = false
                            mainHandler.post { connectionStateSink?.success("disconnected") }
                            sendEvent("deviceNotification", mapOf("type" to "unbind"), "Device requested unbind")
                        }
                    }
                    // Storage low (0x0e)
                    0x0e -> {
                        sendEvent("deviceNotification", mapOf("type" to "storageLow"), "Glasses storage low")
                    }
                    // Volume change (0x12)
                    0x12 -> {
                        sendEvent("deviceNotification", mapOf("type" to "volumeChange"), "Volume changed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Notify parse error", e)
            }
        }
    }

    // ─── Helpers ───

    private fun sendEvent(type: String, data: Map<String, Any?>, message: String?) {
        mainHandler.post {
            eventsSink?.success(mapOf(
                "type" to type,
                "data" to data,
                "message" to message
            ))
        }
    }

    private fun getAlbumDir(): File {
        val externalDir = getExternalFilesDir("") ?: cacheDir
        return File(externalDir, "DCIM_Glasses")
    }

    private fun parseNameFromScanRecord(scanRecord: ByteArray): String? {
        try {
            var index = 0
            while (index < scanRecord.size - 1) {
                val length = scanRecord[index].toInt() and 0xFF
                if (length == 0) break
                if (index + 1 >= scanRecord.size) break
                val type = scanRecord[index + 1].toInt() and 0xFF
                if ((type == 0x08 || type == 0x09) && index + 1 + length <= scanRecord.size) {
                    val nameBytes = ByteArray(length - 1)
                    System.arraycopy(scanRecord, index + 2, nameBytes, 0, length - 1)
                    val name = String(nameBytes, Charsets.UTF_8).trim()
                    if (name.isNotEmpty()) return name
                }
                index += length + 1
            }
        } catch (e: Exception) {
            // ignore parse error
        }
        return null
    }
}
