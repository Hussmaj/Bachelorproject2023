package com.hdtchat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "Sender")
    val sender: String,
    @ColumnInfo(name = "Motakker")
    val receiver: String,
    @ColumnInfo(name = "Melding")
    val message: String,
    @ColumnInfo(name = "Tid")
    val timestamp: Long = System.currentTimeMillis()
)





