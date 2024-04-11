package com.example.importandexportexcel.localDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.importandexportexcel.data.AllDataItemEntity


@Database(entities =[AllDataItemEntity::class],version = 5, exportSchema = false)
abstract class DataBaseHandler : RoomDatabase() {

    abstract fun allDataItem(): AllDataItemDao


    companion object {
        @Volatile
        private var INSTANCE: DataBaseHandler? = null

        fun getDatabase(context: Context): DataBaseHandler {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBaseHandler::class.java,
                    "all_excel_item")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }


}