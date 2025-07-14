package com.example.snswithai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.snswithai.data.local.db.converter.UserDataConverter
import com.example.snswithai.data.local.db.dao.CharacterDao
import com.example.snswithai.data.local.db.dao.UserDao
import com.example.snswithai.data.local.db.dao.UserDataDao
import com.example.snswithai.data.local.db.entity.User
import com.example.snswithai.data.local.db.entity.UserDataEntity

@Database(entities = [User::class, UserDataEntity::class, Character::class], version = 2, exportSchema = false)
@TypeConverters(UserDataConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun userDataDao(): UserDataDao
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sns_with_ai_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
