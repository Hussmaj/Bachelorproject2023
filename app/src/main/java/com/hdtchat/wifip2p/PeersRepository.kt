package com.hdtchat.wifip2p

import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*

class PeersRepository(
    private val manager: WifiP2pManager,
    private val channel: Channel,
): PeersInterfaces {

    private var peersApi: PeersApi? = null
    init {
        peersApi = PeersApi(manager,channel)
        Log.d(TAG, "peersApi initialisert")
    }

  override suspend fun findPeers(): Flow<String> = flow {
           peersApi!!.registrationAndDiscovery().collect {
               emit(it)
           }
    }
    override suspend fun updatePeerList(peer: Peers): Flow<Peers> = flow {
        peersApi!!.newPeerList(peer).collect {
            emit(it)
        }
    }
    override suspend fun getPeers(peers: Peers): Flow<Peers> = flow {
        peersApi!!.getSetDnsSdResponseListeners(peers).collect {
                emit(it)
        }
    }
    override suspend fun connectToPeer(peer: Peers): Flow<String> = callbackFlow {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            peersApi!!.connectP2p(peer).collect {
                trySendBlocking(it)
                channel.close()
            }
        }
        awaitClose {  }
    }
    override suspend fun disconnect(): Flow<String> = flow {
        peersApi!!.disconnect().collect {
            emit(it)
        }
    }
    override fun stopP2p() {
        peersApi!!.clearAllServices()
    }

    companion object {
        private const val TAG = "PeersRepository"
    }
}
