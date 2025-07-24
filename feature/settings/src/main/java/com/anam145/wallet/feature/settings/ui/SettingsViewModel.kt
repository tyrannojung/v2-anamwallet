package com.anam145.wallet.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.anam145.wallet.core.common.model.Language
import com.anam145.wallet.core.common.model.Skin
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.data.datastore.SkinDataStore
import com.anam145.wallet.feature.settings.domain.usecase.GetLanguageUseCase
import com.anam145.wallet.feature.settings.domain.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면의 ViewModel
 * 
 * MVI 패턴을 사용하여 설정 화면의 상태를 관리합니다.
 * 
 * UseCase 설명:
 * - GetLanguageUseCase: 현재 설정된 언어를 가져옴
 * - SetLanguageUseCase: 언어를 변경하고 저장
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val skinDataStore: SkinDataStore
) : ViewModel() {

    /**
     * UI 상태
     * MutableStateFlow : 이 상태를 관찰 가능하게 만드는 도구
     * private - 내부에서만 수정 가능한 MutableStateFlow
     * public - 외부에서는 읽기만 가능한 StateFlow
     * _uiState: ViewModel 내부에서만 값을 변경
     * uiState: UI는 읽기만 가능 (캡슐화)
     * */
    private val _uiState = MutableStateFlow(SettingsContract.SettingsState())
    val uiState: StateFlow<SettingsContract.SettingsState> = _uiState.asStateFlow()
    
    /**
     * State: 지속적인 상태 (현재 테마는 다크모드)
     * Effect: 일회성 동작 (화면 이동하세요!)
     * */
    private val _effect = MutableSharedFlow<SettingsContract.SettingsEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<SettingsContract.SettingsEffect> = _effect.asSharedFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * 사용자 Intent 처리
     * MVI 방식 - 모든 액션을 Intent로 통합!
     */
    fun handleIntent(intent: SettingsContract.SettingsIntent) {
        when (intent) {
            /**
             * is는 Kotlin의 타입 체크 연산자
             * is로 타입 체크하면 자동으로 타입 캐스팅됨!
             * 여기서 intent는 자동으로 ChangeTheme 타입
             * data class = 여러 인스턴스 생성 가능
             * 각각 다른 인스턴스들
             * val intent1 = ChangeTheme(ThemeMode.DARK)
             * val intent2 = ChangeTheme(ThemeMode.LIGHT)
             * val intent3 = ChangeTheme(ThemeMode.SYSTEM)
             * 모두 ChangeTheme 타입이지만 다른 객체
             * data object = 단 하나의 싱글톤
             * 즉 여러 개의 인스턴스가 가능하기 때문에 타입 체크가 필요함
             * */
            is SettingsContract.SettingsIntent.ChangeLanguage -> changeLanguage(intent.language)
            is SettingsContract.SettingsIntent.ChangeSkin -> changeSkin(intent.skin)
            SettingsContract.SettingsIntent.ClickHelp -> navigateToHelp()
            SettingsContract.SettingsIntent.ClickFAQ -> navigateToFAQ()
            SettingsContract.SettingsIntent.ClickAppInfo -> navigateToAppInfo()
            SettingsContract.SettingsIntent.ClickLicense -> navigateToLicense()
        }
    }
    
    /**
     *  저장된 설정 불러오기
     *  collect는 무한 대기 상태!
     *  Flow가 새 값을 방출할 때마다 실행됨
     *  사용자가 라이트 모드 선택
     *  setThemeModeUseCase(LIGHT)
     *     ↓
     *  Repository 업데이트
     *     ↓
     *  getThemeModeUseCase()의 Flow가 새 값 방출!
     *     ↓
     *  collect가 감지하고 combine 다시 실행
     *     ↓
     *  State 자동 업데이트
     */
    private fun loadSettings() {
        // 1. 코루틴 스코프
        viewModelScope.launch {
            // 언어와 스킨을 동시에 관찰
            combine(
                getLanguageUseCase(),
                skinDataStore.selectedSkin
            ) { language, skin ->
                _uiState.update { currentState ->
                    currentState.copy(
                        language = language,
                        skin = skin
                    )
                }
            }.collect()
        }
    }
    
    /**
     * 언어 변경
     */
    private fun changeLanguage(language: Language) {
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }
    
    /**
     * 스킨 변경
     */
    private fun changeSkin(skin: Skin) {
        viewModelScope.launch {
            // SkinDataStore에 저장하면 Flow를 통해 자동으로 UI 업데이트됨
            skinDataStore.setSelectedSkin(skin)
        }
    }
    
    private fun navigateToHelp() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToHelp)
        }
    }
    
    private fun navigateToFAQ() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToFAQ)
        }
    }
    
    private fun navigateToAppInfo() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToAppInfo)
        }
    }
    
    private fun navigateToLicense() {
        viewModelScope.launch {
            _effect.emit(SettingsContract.SettingsEffect.NavigateToLicense)
        }
    }
}