package com.example.etatdeslieux.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SetConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Set<Long> {
        if (value == null) return emptySet()
        val type = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toString(value: Set<Long>): String {
        return gson.toJson(value)
    }
}
