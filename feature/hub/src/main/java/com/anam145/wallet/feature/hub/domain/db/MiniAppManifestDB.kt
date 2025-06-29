package com.anam145.wallet.feature.hub.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import com.anam145.wallet.feature.hub.domain.dao.MiniAppManifestDao

@Database(entities = [MiniAppManifest::class], version = 1)
@TypeConverters(MiniAppManifestConverter::class)
abstract class MiniAppManifestDB : RoomDatabase() {
    abstract fun miniAppManifestDao(): MiniAppManifestDao
}

class MiniAppManifestConverter {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
}