package com.oudmon.wifi
import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.oudmon.ble.base.communication.LargeDataHandler

class WifiP2pManagerSingleton private constructor(private val context: Context) {
    private var wifiP2pManager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

    private var wifiP2pChannel: WifiP2pManager.Channel =
        wifiP2pManager.initialize(
            context, context.mainLooper
        ) {
            Log.i(LogTag.TAG,"wifiP2pChannel disconnect")
        }

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }
    private var wifiP2pDevice: WifiP2pDevice? = null
    private var callback: WifiP2pCallback? = null
    private var connecting: Boolean = false
    private val handler = Handler(context.mainLooper)
    private val discoveryTimeOut = DiscoveryTimeOut(this)
    private val connectTimeOut = ConnectTimeOut(this)

    private var connectRetry = 0
    private var discoveryRetry = 0

    private var connected = false


    @SuppressLint("MissingPermission")
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    try {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            callback?.onWifiP2pEnabled()
                        } else {
                            callback?.onWifiP2pDisabled()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    wifiP2pManager.requestPeers(wifiP2pChannel) { peersList ->
                        callback?.onPeersChanged(peersList.deviceList)
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo =
                        intent.getParcelableExtra<android.net.NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    val wifiP2pInfo =
                        intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                    if (networkInfo != null && networkInfo.isConnected) {
                        connecting = false
                        callback?.onConnected(wifiP2pInfo)
                        handler.removeCallbacks(connectTimeOut)
                    } else {
//                        connecting = false
                        callback?.onDisconnected()
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device =
                        intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    callback?.onThisDeviceChanged(device)
                }
            }
        }
    }

    fun registerReceiver() {
        context.registerReceiver(receiver, intentFilter)
    }

    fun resetPeerDiscovery() {
        handler.removeCallbacks(discoveryTimeOut)
    }

    fun resetFailCount() {
        connectRetry = 0
        discoveryRetry = 0
        connecting = false
        setConnect(false)
        handler.removeCallbacks(discoveryTimeOut)
        handler.removeCallbacks(connectTimeOut)
    }

    @SuppressLint("MissingPermission")
    fun startPeerDiscovery() {
        handler.postDelayed(discoveryTimeOut, 16 * 1000)
        wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                callback?.onPeerDiscoveryStarted()
            }

            override fun onFailure(reason: Int) {
                handler.removeCallbacks(discoveryTimeOut)
                handler.postDelayed(discoveryTimeOut, 2 * 1000)
                callback?.onPeerDiscoveryFailed(reason)
            }
        })
    }

    fun setConnect(connected: Boolean) {
        this.connected = connected
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: WifiP2pDevice) {
        try {
            resetPeerDiscovery()
            if (connected) {
                 Log.i(LogTag.TAG,"P2P已经连接上了，直接返回")
                return
            }
            if (connecting) {
                callback?.connecting()
                 Log.i(LogTag.TAG,"P2P正在连接,不调用连接返回")
                return
            }
            handler.postDelayed(connectTimeOut, 15 * 1000)
            this.wifiP2pDevice = device
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
                wps.setup = WpsInfo.PBC
            }
            connecting = true
             Log.i(LogTag.TAG,"已经在连接设备:" + device.deviceName)
            wifiP2pManager.connect(wifiP2pChannel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    callback?.onConnectRequestSent()
                }

                override fun onFailure(reason: Int) {
                    connecting = false
                    callback?.onConnectRequestFailed(reason)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun cancelP2pConnection() {
        try {
            initP2P()
            wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    callback?.cancelConnect()
                }

                override fun onFailure(reason: Int) {
                    callback?.cancelConnectFail(reason)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addCallback(callback: WifiP2pCallback) {
        this.callback = callback
    }

    fun removeCallback() {
        instance = null
    }


    fun unregisterReceiver() {
        context.unregisterReceiver(receiver)
    }

    interface WifiP2pCallback {
        fun onWifiP2pEnabled()
        fun onWifiP2pDisabled()
        fun onPeersChanged(peers: Collection<WifiP2pDevice>)
        fun onConnected(info: WifiP2pInfo?)
        fun onDisconnected()
        fun onThisDeviceChanged(device: WifiP2pDevice?)
        fun onPeerDiscoveryStarted()
        fun onPeerDiscoveryFailed(reason: Int)
        fun onConnectRequestSent()
        fun onConnectRequestFailed(reason: Int)
        fun connecting()
        fun cancelConnect()
        fun cancelConnectFail(reason: Int)
        fun retryAlsoFailed()
    }

    private class DiscoveryTimeOut(private val outer: WifiP2pManagerSingleton) : Runnable {
        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun run() {
             Log.i(LogTag.TAG,"内部扫描重试连接:" + outer.discoveryRetry)
            if (outer.discoveryRetry < 1) {
                 Log.i(LogTag.TAG,"内部扫描重试连接一次")
                outer.resetDeviceP2p()
                outer.initP2P()
                outer.startPeerDiscovery()
                outer.discoveryRetry++
            }
        }
    }


    fun resetDeviceP2p() {
        LargeDataHandler.getInstance().glassesControl(
            byteArrayOf(0x02, 0x01, 0x0f)
        ) { _, it -> }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun initP2P() {
        wifiP2pChannel.close()
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        wifiP2pChannel = wifiP2pManager.initialize(context, Looper.getMainLooper()) {
             Log.i(LogTag.TAG,"wifiP2pChannel initP2P")
        }
    }

    private class ConnectTimeOut(private val outer: WifiP2pManagerSingleton) : Runnable {
        override fun run() {
            outer.connecting = false
            if (outer.connectRetry < 1) {
                outer.wifiP2pDevice?.let {
                     Log.i(LogTag.TAG,"内部连接重试连接一次")
                    outer.connectToDevice(it)
                }
                outer.connectRetry++
            } else {
                 Log.i(LogTag.TAG,"不重连，等外部超时")
                outer.callback?.retryAlsoFailed()
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WifiP2pManagerSingleton? = null

        fun getInstance(application: Application): WifiP2pManagerSingleton =
            instance ?: synchronized(this) {
                instance
                    ?: WifiP2pManagerSingleton(application).also {
                        instance = it
                    }
            }
    }
}