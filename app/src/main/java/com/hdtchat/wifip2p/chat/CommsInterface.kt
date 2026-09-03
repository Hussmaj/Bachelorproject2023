package com.hdtchat.wifip2p.chat

import android.net.wifi.p2p.WifiP2pInfo
import android.os.Handler
import kotlinx.coroutines.flow.Flow


interface CommsInterface {
    suspend fun startCommunication(wifiP2pInfo: WifiP2pInfo): Flow<String>

}
