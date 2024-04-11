package com.example.importandexportexcel.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "all_data_items",indices = [Index(value = ["rfidNo"], unique = true)])

data class AllDataItemEntity(
    @PrimaryKey
    val idd: Long,
    val rfidNo            :String,
    val accessNo          :String,
    val author            :String,
    val title             :String,
    val volume            :String,
    val place             :String,
    val year              :String,
    val pages             :String,
    val source            :String,
    val cost              :String,
    val billNo            :String,
    val registrationDate  :String,
    val userName          :String,
    val issue             :String,
    val rackNo            :String,
    var status:String
)
