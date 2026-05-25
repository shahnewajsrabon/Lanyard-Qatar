package com.example

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return sdf.format(Date(epochMillis))
    }

    fun formatShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.US)
        return sdf.format(Date(epochMillis))
    }

    fun getDayOfWeek(epochMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.US)
        return sdf.format(Date(epochMillis))
    }
}
