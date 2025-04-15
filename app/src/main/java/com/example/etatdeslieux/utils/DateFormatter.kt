package com.example.etatdeslieux.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object DateFormatter {
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        val pattern = "dd/MM/yyyy HH:mm"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(date)
    }
}
