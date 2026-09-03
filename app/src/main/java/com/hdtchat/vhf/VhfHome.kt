package com.hdtchat.vhf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.hdtchat.R

class VhfHome : AppCompatActivity() {
    private lateinit var btn_lyd: Button
    private lateinit var btn_chat: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_slave)

        btn_lyd = findViewById(R.id.btn_lyd)
        btn_chat = findViewById(R.id.btn_chat)
        btn_lyd.setOnClickListener {
            val intent = Intent(this, vhfChat::class.java)
            startActivity(intent)
        }


        btn_chat.setOnClickListener {
            val intent = Intent(this, VHF::class.java)
            startActivity(intent)
        }
    }
}

