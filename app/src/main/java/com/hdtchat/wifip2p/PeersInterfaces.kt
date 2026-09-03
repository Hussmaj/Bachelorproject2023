package com.hdtchat.wifip2p

import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import kotlinx.coroutines.flow.Flow


interface PeersInterfaces {
    suspend fun findPeers(): Flow<String>
    suspend fun getPeers(peers: Peers): Flow<Peers>
    suspend fun connectToPeer(peer: Peers): Flow<String>
    suspend fun updatePeerList(peer: Peers): Flow<Peers>
    suspend fun disconnect(): Flow<String>
    fun stopP2p()
}
