package com.hdtchat.wifip2p.chat

import android.net.wifi.p2p.WifiP2pInfo
import android.os.Handler
import android.os.Message
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException


class CommsApi(
    private var comms: Comms
): Handler.Callback {

    private var p2pInfo: WifiP2pInfo? = null
    private var handler: Handler = Handler(this)
    suspend fun serverClient(wifiP2pInfo: WifiP2pInfo): Flow<String> =
        callbackFlow {
            Log.d(TAG, "Beginning of serverClient, content of wifiP2pInfo: $wifiP2pInfo")
            val myThread: Thread
            p2pInfo = wifiP2pInfo
            if (p2pInfo!!.isGroupOwner) {
                Log.d(TAG, "Connected as group owner")
                try {
                    myThread = ServerSocket(handler,comms)
                    myThread.start()
                    // Open the chat window
                    Log.d(TAG, "Chat started now")
                    trySend("Chat started now")
                } catch (e: IOException) {

                    channel.close()
                    awaitClose {
                        Log.d(TAG, "Failed to create a server thread - " + e.message)
                    }
                }
            } else {
                Log.d(TAG, "Connected as peer")
                myThread = ClientSocket(handler, p2pInfo!!.groupOwnerAddress,comms)
                myThread.start()
                trySend("Chat started now")
            }

            awaitClose { Log.d(TAG, "ServerClient callback flow closed") }
        }

    override fun handleMessage(msg: Message): Boolean {
        Log.d(TAG, "handleMessage()")
            when (msg.what) {
                MESSAGE_READ -> {
                    Log.i(TAG, "handleMessage() > MESSAGE_READ")
                    val readBuf: ByteArray = msg.obj as ByteArray
                    val fullMessage = String(readBuf, 0, msg.arg1)
                    comms.message = fullMessage

                }
            }
            return true
        }
    companion object {
        const val TAG = "CommsApi"
        const val MESSAGE_READ: Int = 0x400 + 1

    }
}