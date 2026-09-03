package com.hdtchat.vhf.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class VoiceRecorder (private val context: Context): Recorder {

    private var mediaRecorder: MediaRecorder? = null

    override fun startRecord(lydFil: File) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(lydFil).fd)

            prepare()
            start()
        }
    }

    override fun stopRecord() {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
        }
}
