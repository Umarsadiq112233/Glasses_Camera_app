package com.oudmon.wifi
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.annotation.RequiresApi

interface DirectActionListener : WifiP2pManager.ChannelListener {
    val TAG: String get() = "GLASSES_LOG"

    fun wifiP2pEnabled(enabled: Boolean)

    fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo)

    fun onDisconnection()

    fun onSelfDeviceAvailable(device: WifiP2pDevice)

    fun onPeersAvailable(devices: List<WifiP2pDevice>)

}

class DirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,
    private val wifiP2pChannel: WifiP2pManager.Channel,
    private val directActionListener: DirectActionListener,
) : BroadcastReceiver() {

    companion object {

        fun getIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            return intentFilter
        }

    }

    @RequiresApi(35)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val enabled = intent.getIntExtra(
                    WifiP2pManager.EXTRA_WIFI_STATE,
                    -1
                ) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                directActionListener.wifiP2pEnabled(enabled)
                if (!enabled) {
                    directActionListener.onPeersAvailable(emptyList())
                }
                Log.i(LogTag.TAG,"WIFI_P2P_STATE_CHANGED_ACTION： $enabled")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                wifiP2pManager.requestPeers(wifiP2pChannel) { peers ->
                    directActionListener.onPeersAvailable(peers.deviceList.toList())
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo =
                    intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                Log.i(LogTag.TAG,"WIFI_P2P_CONNECTION_CHANGED_ACTION ： " + networkInfo?.isConnected)
                if (networkInfo != null && networkInfo.isConnected) {
                    wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { info ->
                        if (info != null) {
                            directActionListener.onConnectionInfoAvailable(info)
                        }
                    }
                    Log.i(LogTag.TAG,"已连接 P2P 设备")
                } else {
                    directActionListener.onDisconnection()
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val wifiP2pDevice =
                    intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                if (wifiP2pDevice != null) {
                    directActionListener.onSelfDeviceAvailable(wifiP2pDevice)
                }
            }
        }
    }

}