package com.anam145.wallet.feature.hub.di

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import com.anam145.wallet.feature.hub.domain.dao.MiniAppManifestDao
import com.anam145.wallet.feature.hub.domain.db.MiniAppDB
import com.anam145.wallet.feature.hub.domain.db.MiniAppManifestDB
import com.anam145.wallet.feature.hub.domain.repository.MiniAppManifestRepositoryImpl
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepositoryImpl
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class HubModule {

    @Binds
    abstract fun bindMiniAppRepository(
        miniAppRepositoryImpl: MiniAppRepositoryImpl
    ): MiniAppRepository

    @Binds
    abstract fun bindMiniAppManifestRepository(
        miniAppManifestRepositoryImpl: MiniAppManifestRepositoryImpl
    ): MiniAppManifestRepositoryImpl


    companion object {
        @Provides
        @Singleton
        fun provideMiniAppDatabase(
            @ApplicationContext context: Context
        ): MiniAppDB {
            Log.d(">>>",  "DB 생성됨")
            return Room.databaseBuilder(
                context,
                MiniAppDB::class.java,
                "mini_app_db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideMiniAppDao(
            database: MiniAppDB
        ): MiniAppDao {
            return database.miniAppDao()
        }

        @Provides
        @Singleton
        fun provideMiniAppManifestDatabase(
            @ApplicationContext context: Context
        ): MiniAppManifestDB {
            Log.d(">>>",  "DB 생성됨")
            return Room.databaseBuilder(
                context,
                MiniAppManifestDB::class.java,
                "mini_app_manifest_db"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        @Singleton
        fun provideMiniAppManifestDao(
            database: MiniAppManifestDB
        ): MiniAppManifestDao {
            return database.miniAppManifestDao()
        }

        @Provides
        @Singleton
        fun provideHubApi(): HubServerApi {
            val BASE_URL = "http://10.0.2.2:8080"

            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(HubServerApi::class.java)
        }
    }
}

