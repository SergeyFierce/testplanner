package com.sergeyfierce.testplanner.lib.types

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val DATE_PATTERN = "yyyy-MM-dd"
private const val TIME_PATTERN = "HH:mm"

/**
 * Parse an ISO date string into [LocalDate]. Returns null if parsing fails.
 */
fun String.toLocalDateOrNull(): LocalDate? = runCatching {
    LocalDate.parse(this)
}.getOrNull()

/**
 * Parse a 24h time string into [LocalTime]. Returns null if parsing fails.
 */
fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
    LocalTime.parse(this)
}.getOrNull()

fun LocalDate.formatIso(): String = toString()

fun LocalTime.format24h(): String = toString()

/**
 * Formats duration (in minutes) into human readable text (e.g. `1ч 20м`).
 */
fun Int.toReadableDuration(): String {
    val hours = this / 60
    val minutes = this % 60
    val segments = buildList {
        if (hours > 0) add("${hours}ч")
        if (minutes > 0 || hours == 0) add("${minutes}м")
    }
    return segments.joinToString(" ")
}

fun nowIsoString(): String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

fun todayIsoDate(): String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

fun LocalDateTime.toIsoString(): String = toString()

fun combineDateTime(date: LocalDate, time: LocalTime): LocalDateTime =
    LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, time.hour, time.minute)

fun LocalDateTime.toEpochMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    this.toInstant(timeZone).toEpochMilliseconds()

