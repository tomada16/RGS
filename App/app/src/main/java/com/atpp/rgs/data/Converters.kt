package com.atpp.rgs.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    //Change Long to Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date?{
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long?{
        return date?.time
    }
}