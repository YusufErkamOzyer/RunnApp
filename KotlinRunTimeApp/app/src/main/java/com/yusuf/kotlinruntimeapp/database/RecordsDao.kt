package com.yusuf.kotlinruntimeapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface RecordsDao {
    @Query("SELECT * FROM Records")
    fun getAll():Flowable<List<Records>>

    @Insert
    fun insert(record: Records) :Completable

    @Query("DELETE FROM Records")
    fun deleteAll():Completable

    @Query("DELETE FROM Records WHERE id=:id")
    fun deleteRecord(id:Int):Completable

}