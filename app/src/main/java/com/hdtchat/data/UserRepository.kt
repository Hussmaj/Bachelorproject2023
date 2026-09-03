package com.hdtchat.data

import java.security.MessageDigest

import androidx.lifecycle.LiveData
import java.nio.charset.StandardCharsets

class UserRepository(private val userDao: Dao) {

    //Legge til bruker funksjon
    suspend fun addUser(user: User) {
        val hashedPassword = user.password
        userDao.addUser(user.copy(password = hashedPassword))
    }

    //Verifisere bruker
    fun verifyUser(username: String, password: String): Boolean {
        val hashedPassword = hash(password)
        val user = userDao.getUser(username, hashedPassword)
        return if (user != null) {
            user.password == hash(password)
        } else {
            false
        }
    }

    //Hash password
    private fun hash(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedWord = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return hashedWord.joinToString("") { "%02x".format(it) }
    }

}





