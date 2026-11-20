package com.example.peertutoringapplication.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.peertutoringapplication.data.model.User

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
}