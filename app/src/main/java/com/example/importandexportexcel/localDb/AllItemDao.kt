package com.example.importandexportexcel.localDb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.importandexportexcel.data.AllDataItemEntity


@Dao
interface AllDataItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDataItems(items: AllDataItemEntity)

    @Query("SELECT * FROM all_data_items")
     fun getAllDataItems(): LiveData<List<AllDataItemEntity>>


    @Query("SELECT * FROM all_data_items where source = :accessNo OR accessNo = :accessNo OR title = :accessNo")
    fun getDatafromaccessno(accessNo:String): AllDataItemEntity

    @Query("SELECT * FROM all_data_items where rfidNo = :rfidNo")
    fun getBookDataByRfidNuber(rfidNo:String): AllDataItemEntity

    @Query("DELETE FROM all_data_items")
    suspend fun deleteAllitem()
}