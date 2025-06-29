package com.anam145.wallet.feature.hub.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import kotlinx.coroutines.flow.Flow

@Dao
interface MiniAppManifestDao {
    @Query("SELECT * FROM mini_app_manifest")
    fun getMiniAppManifest(): List<MiniAppManifest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiniAppManifests(miniAppManifest: List<MiniAppManifest>)

    @Update
    suspend fun updateMiniAppManifest(miniAppManifest: MiniAppManifest)

    @Delete
    suspend fun deleteMiniAppManifest(miniAppManifest: MiniAppManifest)
}
