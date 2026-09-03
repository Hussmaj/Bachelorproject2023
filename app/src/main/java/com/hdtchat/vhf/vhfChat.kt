package com.hdtchat.vhf


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.*
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hdtchat.Home.Companion.TAG
import com.hdtchat.R
import com.hdtchat.vhf.MAdapter.Message
import kotlinx.coroutines.*

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


@Suppress("DEPRECATION")
class vhfChat : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageAdapter: MAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var audioManager: AudioManager
    private lateinit var audioRecord: AudioRecord
    private lateinit var soundPool: SoundPool
    private var loaded = false
    private var isRecording = false
    private val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 123
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE =123

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        messagesRecyclerView = findViewById(R.id.charRecyclerView)
        messageList = ArrayList()
        messageAdapter = MAdapter(this, messageList)
        messagesRecyclerView.adapter = messageAdapter
        var soundID: Int
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messagesRecyclerView.layoutManager = layoutManager


        // Add a message to the list when the user clicks send button
        val selectImageButton = findViewById<ImageButton>(R.id.button3)
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        }

        val sendButton = findViewById<ImageButton>(R.id.button1)
        val Recivebutton = findViewById<ImageButton>(R.id.button2)
        val messageEditText = findViewById<EditText>(R.id.messagebox)
        Recivebutton.setOnClickListener {
            isRecording = !isRecording // Toggle the recording state

            if (isRecording) {
                startRecording()
            } else {
                StopRecording()
            }
        }
        soundID = toggleSpeak()
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                val newMessage = Message(message, true,null)
                messageList.add(newMessage)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
                val encodedMessage = messageModification(message)
                //val encodedMessage = Base64.encode(message.toByteArray(),Base64.DEFAULT)
                if (loaded) {
                    Log.d(TAG,"Soundfile is loaded")
                    lifecycleScope.launch(Dispatchers.Main) {
                        playMessage(encodedMessage,soundID)
                    }
                }
                messageEditText.text.clear()
                messageAdapter.notifyItemInserted(messageList.size - 1)
            }
        }

    }
    private fun messageModification(message: String): IntArray {
        val encodedMessage = Base64.decode(message, Base64.DEFAULT)
        Log.d(TAG, "Raw message: $message")
        Log.d(TAG, "Encoded mes sage: ${encodedMessage.contentToString()}")
        Log.d(TAG, "Encoded Message retur ned: ${Base64.encodeToString(encodedMessage, Base64.DEFAULT)}")
        Log.d(TAG, "Binary representation of first index: ${Integer.toBinaryString(encodedMessage[0].toInt())}")
        val stringArray = mutableListOf<String>()
        for (byte in 0 until(encodedMessage.size)) {
            stringArray.add(Integer.toBinaryString(encodedMessage[byte].toInt()))
            while (stringArray[byte].length < 8*4) {
                stringArray[byte] = "0" + stringArray[byte]
            }
            if (stringArray[byte].length > 8) {
                stringArray[byte] = stringArray[byte].removeRange(0, 24)
                Log.d(TAG, "Removing unecesssary first 3 bytes ${stringArray[byte]}")
            }
        }
        Log.d(TAG,"StringArray: $stringArray")
        val intArray = IntArray(encodedMessage.size*8)
        var innerIndexValue = 0
        for (int in 0 until stringArray.size) {
            for (i in 0 until 8) {
                intArray.set(innerIndexValue, Character.getNumericValue(stringArray[int][i]))
                innerIndexValue += 1
            }
        }
        Log.d(TAG,"Returning intArray from messageModification")
        return intArray
    }
    @SuppressLint("Recycle")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            Log.d(TAG, "Encoded message: $imageUri")

            if (imageUri != null) {
                val message = Message("bilde", true, imageUri)

                messageList.add(message)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
                val imageView = findViewById<ImageView>(R.id.imageView)
                if (imageView != null) {
                    imageView.visibility = View.VISIBLE
                    try {
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (inputStream!!.read(buffer).also { len = it } != -1) {
                            byteArrayOutputStream.write(buffer, 0, len)
                        }
                        val imageBytes = byteArrayOutputStream.toByteArray()
                        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                        Log.d(TAG, "Encoded image: $encodedImage")
                        playImage(imageBytes)


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            val minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
            )

            // Start recording in a background thread
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    isRecording = true
                    audioRecord.startRecording()
                    while (isRecording) {
                        val buffer = ByteArray(minBufferSize)
                        val read = audioRecord.read(buffer, 0, minBufferSize)
                        if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                            throw IOException("AudioRecord error: $read")
                        }
                        val rmsThreshold = 2500.0

                        // Calculate short buffer and RMS
                        val shortBuffer = ShortArray(buffer.size/2)
                        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortBuffer)
                        val rms = calculateRMS(shortBuffer)

                        if (buffer.any { it.toInt() != 0 }) {
                            if (rms > rmsThreshold) {
                                val decodedMessage = String(buffer, Charsets.UTF_8)


                                runOnUiThread {
                                    val newMessage = Message(decodedMessage, isSent = false, null)
                                    messageList.add(newMessage)
                                    messageAdapter.notifyItemInserted(messageList.size - 1)
                                    messagesRecyclerView.scrollToPosition(messageList.size - 1)
                                }
                            }
                        }

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        }
    }
    fun StopRecording() {
        isRecording = false
    }


    private suspend fun playMessage(decodedMessage: IntArray, soundID: Int) {
        //  val testMessage = Base64.encode(decodedMessage, Base64.DEFAULT)
        //val encodedMessage = Base64.encode(decodedMessage,Base64.DEFAULT)
        val sampleRate = 44000
        val audioEncoding = AudioFormat.ENCODING_PCM_8BIT
        val playMode = AudioTrack.MODE_STREAM
        val actualVolume = audioManager
            .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val maxVolume = audioManager
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volume = actualVolume / maxVolume
        AudioFormat.Builder()
            .setEncoding(audioEncoding)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            audioEncoding
        )
/*
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            audioEncoding,
            bufferSize,
            playMode
        )

 */
        Log.d(TAG,"Size of bitArray: ${decodedMessage.size}")
        //val testTapSound = Base64.encode("oooo".toByteArray(), Base64.DEFAULT)
        //audioTrack.setPlaybackRate(10)
        //audioTrack.play()
        Log.d(TAG, "Audiotrack started")
        var startbit = false
        var endbit = false
        for (i in 0 until 3) {
            soundPool.play(soundID, volume,volume,1,0,1f)
            delay(200
            )
        }
        for (i in decodedMessage.indices) {
            Log.d(TAG,"${decodedMessage[i]}")
            if (decodedMessage[i] == 1) {
                soundPool.play(soundID, volume,volume,1,0,1f)
                //      audioTrack.write(testTapSound, 0, testTapSound.size)
            }
            delay(200)
        }
        for (i in 0 until 3) {
            soundPool.play(soundID, volume,volume,1,0,1f)
            delay(200)
        }
        //audioTrack.stop()
        //audioTrack.release()
        Log.d(TAG, "Audiotrack finished")
    }

    private fun toggleSpeak(): Int {
        var soundID: Int
        var prevAudioSource = AudioManager.MODE_NORMAL
        val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        for (device in outputDevices) {
            if (device.productName.contains("Sound Blaster G3")) {
                Log.d(VHF.TAG, "Device name contains Sound Blaster G3")
                prevAudioSource = audioManager.mode
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isSpeakerphoneOn = false
                audioManager.isBluetoothScoOn = false
                audioManager.isBluetoothA2dpOn = false
                audioManager.isWiredHeadsetOn = false
                audioManager.setBluetoothScoOn(false)
                audioManager.setBluetoothA2dpOn(false)
                audioManager.setSpeakerphoneOn(false)
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)

                Toast.makeText(this@vhfChat, "Listening", Toast.LENGTH_SHORT).show()
            }
        }
        if (!audioManager.isWiredHeadsetOn) {

            audioManager.mode = prevAudioSource
            audioManager.isSpeakerphoneOn = false
            audioManager.isBluetoothScoOn = false
            audioManager.isBluetoothA2dpOn = false
            audioManager.isWiredHeadsetOn = false
            audioManager.setBluetoothScoOn(false)
            audioManager.setBluetoothA2dpOn(false)
            audioManager.setSpeakerphoneOn(false)
            audioManager.setMode(prevAudioSource)

            /*
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
             */
            Toast.makeText(this@vhfChat, "Stopped Listening", Toast.LENGTH_SHORT).show()
        }
        soundPool = SoundPool(10,AudioManager.STREAM_MUSIC,0)
        soundID = soundPool.load(this@vhfChat,R.raw.beepsound,1)
        soundPool.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
            override fun onLoadComplete(
                soundPool: SoundPool?, sampleId: Int,
                status: Int
            ) {
                loaded = true
            }
        })
        return soundID
    }
    private fun playImage(encodedImage: ByteArray) {

        AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(44100)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val bufferSize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        audioTrack.play()
        audioTrack.write(encodedImage, 0, encodedImage.size)
        audioTrack.stop()
        audioTrack.release()
    }
    private fun calculateRMS(shortBuffer: ShortArray): Double {
        var sum = 0.0
        for (i in 0 until shortBuffer.size) {
            sum += shortBuffer[i] * shortBuffer[i]
        }
        val average = sum / shortBuffer.size
        return Math.sqrt(average)
    }


}




