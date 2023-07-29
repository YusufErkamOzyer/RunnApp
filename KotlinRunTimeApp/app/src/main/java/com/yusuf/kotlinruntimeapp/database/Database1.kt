package com.yusuf.kotlinruntimeapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Records::class], version = 1)
abstract class Database1 : RoomDatabase(){
    abstract fun recordsDao(): RecordsDao

    }