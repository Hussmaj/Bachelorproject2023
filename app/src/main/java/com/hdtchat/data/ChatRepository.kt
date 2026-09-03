/*
package com.hdtchat

import androidx.lifecycle.LiveData
import com.hdtchat.data.ChatMessage
import com.hdtchat.data.Dao
import com.hdtchat.data.Userdatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


//private val userDao: Dao
//private val chatDatabase: Userdatabase

class ChatRepository(private val userDao: Dao) {
    suspend fun insertMessage(message: ChatMessage) {
        //Here i can either change this to viewModelScope or lifeCycleScope.
        withContext(Dispatchers.IO) {
            userDao.insertMessage(message)
        }
    }

    fun getMessages(sender: String, receiver: String): LiveData<List<ChatMessage>> {
        return userDao.getMessagesForChat(sender, receiver)
    }
}

 */