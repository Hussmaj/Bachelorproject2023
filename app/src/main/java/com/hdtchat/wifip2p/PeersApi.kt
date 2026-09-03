package com.hdtchat.wifip2p

import android.app.Notification.Action
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.ForeignKey
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import java.util.HashMap
import kotlin.coroutines.CoroutineContext
import com.hdtchat.wifip2p.WiFiDirectBroadcastReceiver


class PeersApi(
    private val manager: WifiP2pManager,
    private val channell: Channel
) {
    private val SERVICE_INSTANCE: String = "hdt chat"
    private val SERVICE_TYPE: String = "_presence._tcp"
    private val TXTRECORD_PROP_AVAILABLE: String = "available"
    private lateinit var serviceRequest: WifiP2pDnsSdServiceRequest
    private val record: MutableMap<String, String> = HashMap()



    suspend fun registrationAndDiscovery(): Flow<String> = flow {
       // var peers: Peers
        if (channell == null) {
            emit("channel er tom i PeersApi")
        }
        if (manager == null) {
            emit("manager er tom i PeersApi")
        }


        record[TXTRECORD_PROP_AVAILABLE] = "visible"
        // Gir informasjon om tjenesten som skal settes opp.
        val serviceInfo: WifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
            SERVICE_INSTANCE,
            SERVICE_TYPE, record
        )
        // Lar andre finne en via discover services
        getAddLocalService(serviceInfo).collect {
            emit(it)
        }
        // Etterspør å kunne bruke service discovery
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_INSTANCE,SERVICE_TYPE)
        getAddServiceRequest(serviceRequest).collect {
            emit(it)
        }
        //Initierer service discovery
        getDiscoverServices().collect {
            emit(it)
        }
        emit("Finished")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    internal fun connectP2p(peers: Peers): Flow<String> = callbackFlow {
        val config = WifiP2pConfig()
        config.deviceAddress = peers.device!!.deviceAddress
        config.wps.setup = WpsInfo.PBC
        Log.d(TAG, "Passphrase: ${config.passphrase}")
        manager.removeServiceRequest(channell, serviceRequest,
            object : ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Successfully removed Service Request")
                }
                override fun onFailure(arg0: Int) {
                    Log.d(TAG, "Failed to remove Service Request. Error Code: $arg0")
                }
            })

        manager.connect(channell, config, object : ActionListener {
            override fun onSuccess() {
               trySend("Connected to service")
                Log.d(TAG, "Connected to service")

            }

            override fun onFailure(error: Int) {
                trySend("Failed connecting to service $error")
                Log.d(TAG, "Failed connecting to service. Error code: $error")
            }
        })
        awaitClose {  }
    }

    fun disconnect(): Flow<String> {
        return callbackFlow {
            manager.requestGroupInfo(channell
            ) { p0 ->
                if (p0 != null) {
                    manager.removeGroup(channell, object : ActionListener {
                        override fun onSuccess() {
                            Log.d(TAG, "Remove Group Successful")
                            trySend("Remove Group Successful")
                            channel.close()
                        }

                        override fun onFailure(error: Int) {
                            Log.d(TAG, "Remove Group Failed: $error")
                            trySend("Remove Group Failed: $error")
                            channel.close()
                        }
                    }
                    )
                }
                else (
                        Log.d(TAG,"group returned null")
                )
            }
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Disconnect successful")
                    trySend("Disconnect successful")
                    channel.close()
                }

                override fun onFailure(error: Int) {
                    Log.d(TAG, "Disconnect failed. Reason: $error")
                    trySend("Disconnect failed. Reason $error")
                    channel.close()
                }
            }
            manager.cancelConnect(channell, actionListener)
            awaitClose { Log.d(TAG,"Disconnect callbackflow closed") }
        }
    }
    fun clearAllServices(): Flow<String> {
        return callbackFlow {

            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Disconnect successful")
                }

                override fun onFailure(error: Int) {
                    Log.d(TAG, "Disconnect failed. Reason: $error")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                manager.stopListening(channell, actionListener)
            }
            manager.clearServiceRequests(channell, actionListener)
            manager.clearLocalServices(channell, actionListener)
            awaitClose { }
        }
    }
    private fun getAddLocalService(serviceInfo: WifiP2pDnsSdServiceInfo): Flow<String>  {
        return callbackFlow {
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                  ///  trySendBlocking("Added Local Service")
                    Log.d(TAG, "Added a service.")
                    channel.close()
                }

                override fun onFailure(error: Int) {
                    ///trySendBlocking("Failed to add a service. Error code: $error")
                    Log.d(TAG, "Failed to add a service. Error code: $error")
                    channel.close()
                }
            }
            manager.addLocalService(channell, serviceInfo, actionListener)
            awaitClose {
                Log.d(TAG,"addLocalService callbackflow closed")
            }
        }
    }

    private suspend fun getAddServiceRequest(serviceRequest: WifiP2pDnsSdServiceRequest): Flow<String> {
        return callbackFlow {
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Added service discovery request")
                    channel.close()
                }

                override fun onFailure(error: Int) {
                    Log.d(TAG, "Failed adding service discovery request")
                    channel.close()
                }
            }
            manager.addServiceRequest(channell, serviceRequest, actionListener)
            awaitClose {
                Log.d(TAG,"addServiceRequest callbackflow closed")
            }
        }
    }
    private suspend fun getDiscoverServices(): Flow<String> {
        return callbackFlow {
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Service discovery initiated")
                    channel.close()
                }

                override fun onFailure(error: Int) {
                    Log.d(TAG, "Service discovery failed. Error code: $error")
                    channel.close()
                }
            }
            manager.discoverServices(channell, actionListener)
            awaitClose {
                Log.d(TAG,"DiscoverServices callbackflow closed")
            }
        }
    }
    internal suspend fun getSetDnsSdResponseListeners(peers: Peers): Flow<Peers> {
        return callbackFlow {

            val servListener =
                DnsSdServiceResponseListener { instanceName, registrationType, srcDevice ->
                    Log.d(TAG,srcDevice!!.deviceName + " form app " + instanceName + " with " + registrationType + " found")

                }
            val txtRecordListener =
                DnsSdTxtRecordListener { domainName, record, device ->
                    val delimiters = "."
                    val nameAndType = domainName.split(delimiters, limit = 2)
                    peers.device = device
                    peers.instanceName = nameAndType[0]
                    peers.serviceRegistrationType = nameAndType[1]
                    Log.d(
                        TAG,
                        device.deviceName + " is " + record[TXTRECORD_PROP_AVAILABLE] + "  " + nameAndType[0]
                    )
                    trySendBlocking(peers)
                }
            manager.setDnsSdResponseListeners(
                channell,
                servListener,
                txtRecordListener
            )

            awaitClose {
                Log.d(TAG, "dnsSdServiceResponseListener stopped")
            }
        }


    }


    internal fun newPeerList(peers: Peers): Flow<Peers> {
        return callbackFlow {
            val peerListener = PeerListListener { p0 ->
                for (i in p0.deviceList.indices) {
                    peers.device = p0.deviceList.elementAt(i)
                    trySendBlocking(peers)
                    Log.d(TAG, "Peers found ${peers.device!!.deviceName}")
                }
                channel.close()
            }
            manager.requestPeers(channell, peerListener)
            awaitClose {  }
        }

    }

    companion object {
        private const val TAG = "PeersApi"
    }

}

