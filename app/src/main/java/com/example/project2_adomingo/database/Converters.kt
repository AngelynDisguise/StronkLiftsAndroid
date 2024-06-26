package com.example.project2_adomingo.database

import androidx.room.TypeConverter
import java.time.Duration
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

//    @TypeConverter
//    fun fromDuration(value: Duration?): Long? {
//        return value?.toMillis()
//    }
//
//    @TypeConverter
//    fun toDuration(value: Long?): Duration? {
//        return value?.let { Duration.ofMillis(it) }
//    }
}

