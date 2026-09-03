package com.hdtchat.wifip2p

import android.net.wifi.p2p.WifiP2pDevice
import kotlinx.coroutines.flow.MutableStateFlow

data class Peers (
    var device: WifiP2pDevice? = null,
    var instanceName: String? = null,
    var serviceRegistrationType: String? = null,
)
