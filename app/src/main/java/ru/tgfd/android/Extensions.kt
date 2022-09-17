package ru.tgfd.android

import java.text.DateFormat
import java.util.*

fun Long.toStringData(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    return formatter.format(calendar.time)
}
