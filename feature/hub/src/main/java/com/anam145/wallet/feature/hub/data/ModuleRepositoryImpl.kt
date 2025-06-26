package com.anam145.wallet.feature.hub.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.feature.hub.domain.repository.ModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱 전체에서 인스턴스 하나만
class ModuleRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences> // DataStore 주입
) : ModuleRepository {}