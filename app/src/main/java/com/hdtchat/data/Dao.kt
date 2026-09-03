package com.hdtchat.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Denne filen så inneholder alle metodene'ene/queries som skakl bli brukt.
@Dao
interface Dao {
    
    @Query("SELECT * FROM Bruker_Tabell WHERE Brukernavn = :userName AND Passord = :password")
    fun getUser(userName: String, password: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)





}


