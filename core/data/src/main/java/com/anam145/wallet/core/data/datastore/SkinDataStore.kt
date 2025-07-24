package com.anam145.wallet.core.data.datastore

import android.util.Log

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.core.common.constants.SkinConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 스킨 관련 데이터를 관리하는 DataStore
 * 
 * - 현재 선택된 스킨
 * - 각 스킨별 미니앱 목록 (동적 관리)
 */
@Singleton
class SkinDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.skinDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "skin_preferences"
    )
    
    companion object {
        private val SELECTED_SKIN_KEY = stringPreferencesKey("selected_skin")
        private val SKIN_INITIALIZED_KEY = booleanPreferencesKey("skin_initialized")
        
        // 각 스킨별 앱 목록 키 생성
        private fun skinAppsKey(skin: Skin) = stringSetPreferencesKey("skin_apps_${skin.name}")
    }
    
    /**
     * 현재 선택된 스킨
     */
    val selectedSkin: Flow<Skin> = context.skinDataStore.data
        .map { preferences ->
            val skinName = preferences[SELECTED_SKIN_KEY] ?: SkinConstants.DEFAULT_SKIN.name
            Skin.valueOf(skinName)
        }
    
    /**
     * 스킨 변경
     */
    suspend fun setSelectedSkin(skin: Skin) {
        context.skinDataStore.edit { preferences ->
            preferences[SELECTED_SKIN_KEY] = skin.name
        }
    }
    
    /**
     * 특정 스킨의 앱 목록 가져오기
     * 
     * 최초 실행 시 SkinConstants의 기본값으로 초기화
     */
    suspend fun getAppsForSkin(skin: Skin): Set<String> {
        Log.d("SkinDataStore", "getAppsForSkin called for skin: $skin")
        
        val preferences = context.skinDataStore.data.first()
        val initialized = preferences[SKIN_INITIALIZED_KEY] ?: false
        Log.d("SkinDataStore", "Is initialized: $initialized")
        
        if (!initialized) {
            // 최초 실행: 정적 데이터로 초기화하고 기다림
            Log.d("SkinDataStore", "First run - initializing skin apps")
            Log.d("SkinDataStore", "Default skin apps from constants: ${SkinConstants.DEFAULT_SKIN_MINIAPPS}")
            
            initializeSkinApps()
            // 초기화 후 다시 읽기
            val updatedPreferences = context.skinDataStore.data.first()
            val result = updatedPreferences[skinAppsKey(skin)] ?: SkinConstants.DEFAULT_SKIN_MINIAPPS[skin]?.toSet() ?: emptySet()
            
            Log.d("SkinDataStore", "After initialization, apps for $skin: $result")
            return result
        }
        
        // 기존 사용자: DataStore에서 읽기
        val result = preferences[skinAppsKey(skin)] ?: emptySet()
        Log.d("SkinDataStore", "Existing user - apps for $skin from DataStore: $result")
        return result
    }
    
    /**
     * 모든 스킨의 앱 목록 가져오기 (삭제 시 참조 카운팅용)
     */
    suspend fun getAppsForAllSkins(): Map<Skin, Set<String>> {
        val preferences = context.skinDataStore.data.first()
        
        return Skin.values().associateWith { skin ->
            preferences[skinAppsKey(skin)] ?: emptySet()
        }
    }
    
    /**
     * 최초 실행 시 정적 데이터로 초기화
     */
    private suspend fun initializeSkinApps() {
        Log.d("SkinDataStore", "initializeSkinApps - Starting initialization")
        
        context.skinDataStore.edit { preferences ->
            SkinConstants.DEFAULT_SKIN_MINIAPPS.forEach { (skin, appIds) ->
                val key = skinAppsKey(skin)
                Log.d("SkinDataStore", "Setting apps for $skin: $appIds")
                preferences[key] = appIds.toSet()
            }
            preferences[SKIN_INITIALIZED_KEY] = true
        }
        
        Log.d("SkinDataStore", "initializeSkinApps - Initialization completed")
    }
    
    /**
     * 현재 스킨에 앱 추가
     */
    suspend fun addAppToCurrentSkin(appId: String) {
        val currentSkin = selectedSkin.first()
        addAppToSkin(appId, currentSkin)
    }
    
    /**
     * 특정 스킨에 앱 추가
     */
    suspend fun addAppToSkin(appId: String, skin: Skin) {
        context.skinDataStore.edit { preferences ->
            val key = skinAppsKey(skin)
            val currentApps = preferences[key] ?: emptySet()
            preferences[key] = currentApps + appId
        }
    }
    
    /**
     * 현재 스킨에서 앱 제거
     */
    suspend fun removeAppFromCurrentSkin(appId: String) {
        val currentSkin = selectedSkin.first()
        removeAppFromSkin(appId, currentSkin)
    }
    
    /**
     * 특정 스킨에서 앱 제거
     */
    suspend fun removeAppFromSkin(appId: String, skin: Skin) {
        context.skinDataStore.edit { preferences ->
            val key = skinAppsKey(skin)
            val currentApps = preferences[key] ?: emptySet()
            preferences[key] = currentApps - appId
        }
    }
    
    /**
     * 특정 앱이 어떤 스킨에서든 사용 중인지 확인
     * 
     * @param appId 확인할 앱 ID
     * @return 하나라도 사용 중이면 true
     */
    suspend fun isAppUsedByAnySkin(appId: String): Boolean {
        val allSkinApps = getAppsForAllSkins()
        return allSkinApps.values.any { apps -> apps.contains(appId) }
    }
    
    /**
     * DEBUG: DataStore 상태 확인 및 강제 재초기화
     */
    suspend fun debugResetDataStore() {
        Log.d("SkinDataStore", "DEBUG: Forcing DataStore reset")
        context.skinDataStore.edit { preferences ->
            preferences.clear()
        }
        initializeSkinApps()
        Log.d("SkinDataStore", "DEBUG: DataStore reset completed")
    }
    
    /**
     * 현재 스킨의 앱 목록 Flow
     * 
     * 스킨 변경 시 자동으로 업데이트
     */
    val currentSkinApps: Flow<Set<String>> = selectedSkin
        .map { skin -> getAppsForSkin(skin) }
}