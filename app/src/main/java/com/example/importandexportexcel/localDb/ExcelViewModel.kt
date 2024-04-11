package com.example.importandexportexcel.localDb

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.importandexportexcel.data.AllDataItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExcelViewModel(application: Application): AndroidViewModel(application) {

    private var repository: ExcelRepo
    val getAllDataItem:LiveData<List<AllDataItemEntity>>
    init {
        val userDao = DataBaseHandler.getDatabase(application).allDataItem()
        repository = ExcelRepo(userDao)
        getAllDataItem = repository.getAllDataItem
    }

    fun insertAllDataItems(items: AllDataItemEntity){
        viewModelScope.launch(Dispatchers.Main) {
            repository.insertAllDataItems(items)
        }
    }

    fun fetBookDetailsByAccessNo(accessNo:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getBookDetailsByAccessNo(accessNo)
        }
    }

    fun deleteAllItem(){
        viewModelScope.launch {
            repository.deleteAllitem()
        }
    }




}