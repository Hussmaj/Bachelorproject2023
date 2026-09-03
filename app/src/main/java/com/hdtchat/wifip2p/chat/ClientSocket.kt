package com.hdtchat.wifip2p.chat

import android.os.Handler
import android.util.Log
import com.hdtchat.wifip2p.WMActivity
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Sets up a client socket and attempts to connect to the servers socket with information provided from wifip2pmanager services.
 * After successful connection it runs a instance of the communicationmanager
 */
class ClientSocket(private val handler: Handler, private val mAddress: InetAddress, private var comms: Comms) :
    Thread() {
    var chat: CommunicationManagement? = null
        private set

    override fun run() {
        val socket = Socket()
        try {
            socket.bind(null)
            socket.connect(
                InetSocketAddress(
                    mAddress.hostAddress,
                    WMActivity.SERVER_PORT
                ), 5000
            )
            Log.d(TAG, "Launching the I/O handler")
            chat = CommunicationManagement(socket, handler,comms)
            Thread(chat).start()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                socket.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            return
        }
    }

    companion object {
        private const val TAG = "ClientSocketHandler"
    }
}