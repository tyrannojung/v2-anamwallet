package com.anam145.wallet.feature.hub.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anam145.wallet.core.common.model.MiniApp
import kotlinx.coroutines.flow.Flow

@Dao
interface MiniAppDao {
    @Query("SELECT * FROM mini_app")
    fun getMiniApps(): Flow<List<MiniApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiniApps(miniApp: List<MiniApp>)

    @Update
    suspend fun updateMiniApp(miniApp: MiniApp)

    @Delete
    suspend fun deleteMiniApp(miniApp: MiniApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiniApp(miniApp: MiniApp)
}
