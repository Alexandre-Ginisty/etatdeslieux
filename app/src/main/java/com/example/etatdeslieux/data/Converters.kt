package com.example.etatdeslieux.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLongSetToString(value: Set<Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromStringToLongSet(value: String): Set<Long> {
        val type = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(value, type)
    }
}
