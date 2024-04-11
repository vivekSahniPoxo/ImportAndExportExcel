package com.example.importandexportexcel


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.importandexportexcel.databinding.ExportLayoutBinding


class GetItemFromExcelAdapter(private val itemList: List<GetItemFromExcel>) :
    RecyclerView.Adapter<GetItemFromExcelAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ExportLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetItemFromExcel) {
            binding.apply {
                // Bind your data to the views using view binding
                itemNameTextView.text = item.Name
                itemDescriptionTextView.text = item.RfidNo
                // Bind other properties as needed
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ExportLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
