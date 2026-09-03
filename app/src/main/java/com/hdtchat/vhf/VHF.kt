package com.hdtchat.vhf

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.hdtchat.databinding.ActivityVhfBinding
import com.hdtchat.vhf.player.VoicePlayer
import com.hdtchat.vhf.recorder.VoiceRecorder
import java.io.File
import java.lang.Thread.sleep


class VHF : ComponentActivity() {

    private lateinit var binding: ActivityVhfBinding

    private var isSpeakerOn: Boolean = false

    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack
    private lateinit var audioManager: AudioManager

    private lateinit var bufferSize: IntArray
    private lateinit var thread: Thread

    private var prevAudioSource = AudioManager.MODE_NORMAL

    var isListening = false


    private val recorder by lazy {
        VoiceRecorder(applicationContext)
    }

    private val player by lazy {
        VoicePlayer(applicationContext)
    }
    private var audioFile: File? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVhfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        /*
        binding.btnListen.setOnClickListener {
            toggleListen()
            bufferSize = initializeAudio(0)
            thread = recordNPlay(bufferSize, 0)
            thread.start()
        }
         */

        binding.btnListen.setOnClickListener {
            if (isListening) {
                // Button is currently on, turn it off
                stopListening()
                Toast.makeText(this@VHF, "Not Listening", Toast.LENGTH_SHORT).show()
            } else {
                // Button is currently off, turn it on
                startListening()
                Toast.makeText(this@VHF, "Listening", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnRecording.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                toggleSpeak()
                bufferSize = initializeAudio(1)
                Log.d(TAG, "BUTTON PUSHED TO RECORD")
                File(cacheDir, "audio.mp3").also {
                    recorder.startRecord(it)
                    audioFile = it
                }
            }
            else if (event.action == MotionEvent.ACTION_UP) {
                Log.d(TAG,"BUTTON RELEASED TO STOP RECORD")
                sleep(200)
                recorder.stopRecord()
                audioFile?.let { player.startPlayer(it) }
            }
            false
        }


    }

    private fun startListening() {
        // Actions to perform when starting to listen
        isListening = true
        toggleListen()
        bufferSize = initializeAudio(0)
        thread = recordNPlay(bufferSize, 0)
        thread.start()
    }

    private fun stopListening() {
        // Actions to perform when stopping listening
        isListening = false
        toggleListen()
        thread.interrupt() // Interrupt the recording and playing thread if necessary

    }

    private fun recordNPlay(buffers: IntArray, startStop: Int): Thread {
            val myThread = Thread {
                val buffer = ByteArray(buffers[0])
                while (startStop == 0) {
                    if (isSpeakerOn) {
                        audioRecord.startRecording()
                        audioTrack.play()
                        var bytesRead = audioRecord.read(buffer, 0, buffers[0])
                        while (bytesRead > 0) {
                            audioTrack.write(buffer, 0, bytesRead)
                            bytesRead = audioRecord.read(buffer, 0, buffers[0])
                        }
                    }
                }
                Log.d(TAG, "While loop stopped")
                audioTrack.stop()
                audioRecord.stop()
            }
        return myThread
    }


    private fun initializeAudio(mode: Int): IntArray {
        val buffers = IntArray(2)
        buffers[0] = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT_IN)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG_IN,
            AUDIO_FORMAT_IN,
            buffers[0]
        )

        buffers[1] = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT_OUT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_VOICE_CALL,
            SAMPLE_RATE,
            CHANNEL_CONFIG_OUT,
            AUDIO_FORMAT_OUT,
            buffers[1],
            AudioTrack.MODE_STREAM
        )
        return buffers
    }


    private fun toggleSpeak() {
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        for (device in outputDevices) {
            if (device.productName.contains("Sound Blaster G3")) {
                Log.d(TAG, "Device name contains Sound Blaster G3")
                prevAudioSource = audioManager.mode
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

                Toast.makeText(this@VHF, "Recording", Toast.LENGTH_SHORT).show()
                return
                }
        }
        audioManager.mode = prevAudioSource
    }

    private fun toggleListen() {
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

            if(isListening) {
                for (device in outputDevices) {
                    if (device.productName.contains("Sound Blaster G3")) {
                        Log.d(TAG, "Device name contains Sound Blaster G3")

                        audioManager.isSpeakerphoneOn = true
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

                        isSpeakerOn = true
                        //Toast.makeText(this@VHF, "Listening", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        else {
                audioManager.isSpeakerphoneOn = false
                isSpeakerOn = false
        }
    }

    companion object {

        const val TAG: String = "VHF"


        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT_IN = AudioFormat.ENCODING_PCM_16BIT
        const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT_OUT = AudioFormat.ENCODING_PCM_16BIT
    }
}

