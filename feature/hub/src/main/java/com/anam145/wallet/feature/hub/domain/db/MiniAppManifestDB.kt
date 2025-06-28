package com.anam145.wallet.feature.hub.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import com.anam145.wallet.feature.hub.domain.dao.MiniAppManifestDao

@Database(entities = [MiniAppManifest::class], version = 1)
abstract class MiniAppManifestDB : RoomDatabase() {
    abstract fun miniAppManifestDao(): MiniAppManifestDao
}

