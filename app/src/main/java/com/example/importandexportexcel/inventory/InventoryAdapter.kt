package com.example.importandexportexcel.inventory

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.importandexportexcel.R
import com.example.importandexportexcel.data.AllDataItemEntity
import com.example.importandexportexcel.databinding.InventoryLayoutBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



class InventoryAdapter(private val mList: MutableList<AllDataItemEntity>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHOlder>() {

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = InventoryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }





    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: InventoryLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: AllDataItemEntity) {
            itemBinding.apply {
                itemBinding.tvId.text = item.idd.toString()
                itemBinding.tvRFidNo.text = item.rfidNo.toString()
                itemBinding.tvAccessNo.text = item.accessNo.toDoubleOrNull()?.toInt().toString()
                itemBinding.tvAuthor.text = item.author.toString()
                itemBinding.tvTitle.text = item.title.toString()
                itemBinding.tvPlace.text = item.place.toString()
                itemBinding.tvYear.text = item.year.toDoubleOrNull()?.toInt().toString()
                itemBinding.tvSouce.text = item.source.toDoubleOrNull()?.toInt().toString()
                itemBinding.tvPages.text = item.pages.toDoubleOrNull()?.toInt().toString()

            }

            if (item.status=="Found"){
                itemBinding.ll.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.green2
                    )
                )
            } else{
                itemBinding.ll.setBackgroundColor(Color.TRANSPARENT)
            }


        }
    }






    fun clearAllData() {
        mList.clear()
        notifyDataSetChanged()
    }

    fun refreshAdapter() {
//        totalCountSize = temList.distinct().size
        handler.post {
            mList.sortByDescending { it.status == "Found" }
            notifyDataSetChanged()


        }
    }







    fun moveFoundItemsToTop(loader: ProgressBar, recyclerView: RecyclerView) {
        loader.isVisible = true

        GlobalScope.launch(Dispatchers.Default) {
            refreshAdapter()

        }
        loader.isVisible = false
    }





    fun dateFormatConverter(dateString: String?): String {
        if (dateString == null) {
            // Handle null case if needed
            return ""
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val date: Date = inputFormat.parse(dateString) ?: Date()

        val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return outputFormat.format(date)
    }












}




