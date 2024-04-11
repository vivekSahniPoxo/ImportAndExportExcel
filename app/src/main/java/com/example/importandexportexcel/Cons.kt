package com.example.importandexportexcel

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Cons {
    companion object {
        const val ID = "ID"
        const val RFIdNO = "RFIDNO"
        const val ACCESSNO = "AccessNo"
        const val AUTHOR = "Author"
        const val TITILE = "Title"
        const val PLACE = "Place"
        const val YEAR = "Year"
        const val Source = "AccessNo"
        const val PAGES = "pages"
        const val STATAUS = "status"
        const val TiME = "Timestamp"
        const val FOUND = "Found"
        const val NOTFOUND = "Not Found"


        @RequiresApi(Build.VERSION_CODES.O)
        fun getDatetime(text: String): String? {

            val time = LocalDateTime.now()
            val str_date = time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

            return (str_date)
        }


    }
}