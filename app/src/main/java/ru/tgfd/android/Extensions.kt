package ru.tgfd.android

import java.util.*

fun Int.toTwoDigits() = if (this < 10) "0$this" else this.toString()

fun Long.toStringData(): String {
    val timeMillis = this
    val publicationCalendar = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }
    val dayOfMonth = publicationCalendar.get(Calendar.DAY_OF_MONTH).toTwoDigits()
    val monthNumber = (publicationCalendar.get(Calendar.MONTH) + 1).toTwoDigits()
    val hours = publicationCalendar.get(Calendar.HOUR).toTwoDigits()
    val minutes = publicationCalendar.get(Calendar.MINUTE).toTwoDigits()
    val year = publicationCalendar.get(Calendar.YEAR)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearString = if (year != currentYear) ".$year" else ""
    return "$dayOfMonth.$monthNumber$yearString в $hours:$minutes"
}
