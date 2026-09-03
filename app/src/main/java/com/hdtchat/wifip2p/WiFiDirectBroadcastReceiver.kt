
package com.hdtchat.wifip2p

import android.content.*
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.util.Log

class WiFiDirectBroadcastReceiver(private val manager: WifiP2pManager?, private val channel: WifiP2pManager.Channel?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        if ((WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action)) {
            if (manager == null) {
                return
            }
             val wifiP2pInfo: WifiP2pInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO)

            if (wifiP2pInfo!!.groupFormed) {
                Log.d(TAG, (wifiP2pInfo.toString()))

                Log.d(
                    TAG,
                    "Connected to p2p network. Requesting network details"
                )
                manager.requestConnectionInfo(channel, context as ConnectionInfoListener?)
            } else {
                Log.d(TAG, "Failed to request connection info")
            }
        } else if ((WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action)) {
            val device: WifiP2pDevice? = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
            Log.d(TAG, "Device status: " + device!!.status)
        }

    }
    companion object {
        private const val TAG = "WiFiDirectBroadcastReceiver"
    }
}
