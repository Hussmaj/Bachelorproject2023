package com.hdtchat.wifip2p.chat

import android.os.Handler
import android.util.Log
import androidx.databinding.Observable
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import com.hdtchat.BR


class CommunicationManagement(socket: Socket?, handler: Handler, private var comms: Comms) : Runnable {
    var socket: Socket? = null
    private var handler: Handler
    private var oStream: OutputStream? = null
    init {
        this.socket = socket
        this.handler = handler
        //Initialising a callback listener for any changes to sendMessage defined in Comms.
        comms.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(
                sender: androidx.databinding.Observable?,
                propertyId: Int
            ) {
                // On property change, send to write function defined later in this class
                if (propertyId == BR.sendMessage) {
                    Log.d(TAG, "Melding motatt: " + comms.sendMessage)
                    write(comms.sendMessage)

                }
            }
        })
    }
    //Starts up whenever a new message arrives.
    override fun run() {
        try {
            val iStream = socket!!.getInputStream()
            oStream = socket!!.getOutputStream()
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer)
                    if (bytes == -1) {
                        break
                    }
                    Log.d(TAG, "Rec:" + String(buffer))
                    handler.obtainMessage(
                        CommsApi.MESSAGE_READ,
                        bytes,
                        -1,
                        buffer
                    ).sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    //Writes string message to a bytearray buffer and feeds it to socket.getoutputstream.write()
    fun write(msg: String) {
        val buffer = msg.toByteArray()
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    oStream!!.write(buffer)
                } catch (e: IOException) {
                    Log.e(TAG, "Exception during write", e)
                }
            }
        }
        thread.start()
    }

    companion object {
        private const val TAG = "CommunicationManager"
    }
}