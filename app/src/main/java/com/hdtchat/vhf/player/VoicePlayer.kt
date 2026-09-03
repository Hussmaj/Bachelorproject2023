package com.hdtchat.vhf.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class VoicePlayer(private val context: Context): Player {
    private var mediaPlayer: MediaPlayer? = null

    override fun startPlayer(lydFil: File) {

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.fromFile(lydFil))
            setOnPreparedListener {
                start()
            }
            prepareAsync()
        }
    }

    override fun stopPlayer() {
        mediaPlayer?.apply {
            stop()
            reset()
        }
        mediaPlayer = null
    }
}

