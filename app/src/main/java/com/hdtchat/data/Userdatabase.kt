package com.hdtchat.data

import android.content.Context
import android.provider.CalendarContract.Instances
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hdtchat.data.Dao
import com.hdtchat.data.User
import java.security.AccessControlContext

@Database(entities = [User::class], version = 2, exportSchema = false)

abstract class Userdatabase : RoomDatabase(){

    abstract fun dao(): Dao

    companion object {
        @Volatile
        private var INSTANCE: Userdatabase? = null

        fun getDatabase(context: Context): Userdatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Userdatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }


    }
}
