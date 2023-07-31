package com.yusuf.kotlinruntimeapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Records (
        @ColumnInfo(name = "distanceColumn")
        var distance:Float?,
        @ColumnInfo(name = "timeColumn")
        var time:Long?,
        @ColumnInfo(name = "speedColumn")
        var speed:Double?,
        @ColumnInfo(name = "currentDateColumn")
        var currentDate:String?,
        @ColumnInfo(name = "currentTimeColumn")
        var currentTime:String?


        ) : Serializable {
        @PrimaryKey(autoGenerate = true)
        var id =0
}