package com.example.importandexportexcel.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GetUtils {

    public companion object {
        fun getDatetime(text: String): String? {

            val time = LocalDateTime.now()
            val str_date = time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

            return (str_date)
        }
    }
}