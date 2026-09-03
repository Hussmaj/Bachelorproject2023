package com.hdtchat.wifip2p.chat

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Defines a server socket and awaits connections from clients. The server socket is set up by device designated as group owner by the
 * wifip2p connection negotiation.
 */
class ServerSocket(handler: Handler?, private var comms: Comms) : Thread() {
    var socket: ServerSocket? = null
    private val THREAD_COUNT = 10
    private var handler: Handler? = null

    private val pool = ThreadPoolExecutor(
        THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )

    init {
        try {
            socket = ServerSocket(4545)
            this.handler = handler
            Log.d("GroupOwnerSocketHandler", "Socket Started")
        } catch (e: IOException) {
            e.printStackTrace()
            pool.shutdownNow()
            throw e
        }
    }

    override fun run() {
        while (true) {
            try {
                pool.execute(CommunicationManagement(socket!!.accept(), handler!!,comms))
                Log.d(TAG, "Launching the I/O handler")
            } catch (e: IOException) {
                try {
                    if (socket != null && !socket!!.isClosed) socket!!.close()
                } catch (_: IOException) {
                }
                e.printStackTrace()
                pool.shutdownNow()
                break
            }
        }
    }

    companion object {
        private const val TAG = "GroupOwnerSocketHandler"
    }
}