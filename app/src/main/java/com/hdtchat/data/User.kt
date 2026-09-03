package com.hdtchat.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Denne klassen skal represnetere en tabell i databasen
@Entity(tableName = "Bruker_Tabell")
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "Brukernavn")
    val userName: String?,
    @ColumnInfo(name = "Epost")
    val email: String?,
    @ColumnInfo(name = "Passord")
    val password: String?
)



