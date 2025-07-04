package com.daza.guitarsheetcreator.activities.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.daza.guitarsheetcreator.activities.models.Song
import com.daza.guitarsheetcreator.activities.dataBase.SongDao

@Database(entities = [Song::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "guitar_notes_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}