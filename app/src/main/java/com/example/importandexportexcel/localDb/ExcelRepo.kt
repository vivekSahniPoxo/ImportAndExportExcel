package com.example.importandexportexcel.localDb

import androidx.lifecycle.LiveData
import com.example.importandexportexcel.data.AllDataItemEntity


class ExcelRepo(private val allDataItemDao: AllDataItemDao)  {
    val getAllDataItem: LiveData<List<AllDataItemEntity>> = allDataItemDao.getAllDataItems()

    suspend fun insertAllDataItems(items: AllDataItemEntity){
        allDataItemDao.insertAllDataItems(items)
    }

     fun getBookDetailsByAccessNo(accessNo: String){
        allDataItemDao.getDatafromaccessno(accessNo)
    }

    suspend fun deleteAllitem(){
        allDataItemDao.deleteAllitem()
    }

}