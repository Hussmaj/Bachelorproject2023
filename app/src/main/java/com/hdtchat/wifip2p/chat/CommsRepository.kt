package com.hdtchat.wifip2p.chat

import android.net.wifi.p2p.WifiP2pInfo
import android.util.Log
import com.hdtchat.wifip2p.chat.Comms
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class CommsRepository(
    private var comms: Comms
): CommsInterface {
    private var commsApi: CommsApi? = null

    init {
        commsApi = CommsApi(comms)
    }
    override suspend fun startCommunication(wifiP2pInfo: WifiP2pInfo): Flow<String> = callbackFlow {
        Log.d(TAG, "Beginning of startCommunication, content of wifiP2pInfo: $wifiP2pInfo")
        commsApi!!.serverClient(wifiP2pInfo).collect {
            trySend(it)
            //channel.close()
        }
        awaitClose { Log.d(TAG, "startCommunication callbackflow closed") }
    }
    companion object {
        private const val TAG = "PeersRepository"
    }
}
