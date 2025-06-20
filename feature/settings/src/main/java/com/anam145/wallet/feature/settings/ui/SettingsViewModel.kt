package com.anam145.wallet.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.anam145.wallet.core.ui.language.Language
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.settings.domain.model.ThemeMode
import com.anam145.wallet.feature.settings.domain.usecase.GetThemeModeUseCase
import com.anam145.wallet.feature.settings.domain.usecase.SetThemeModeUseCase
import com.anam145.wallet.feature.settings.domain.usecase.GetLanguageUseCase
import com.anam145.wallet.feature.settings.domain.usecase.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면의 ViewModel
 * 
 * MVI 패턴을 사용하여 설정 화면의 상태를 관리합니다.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {
    
    // UI 상태
    private val _uiState = MutableStateFlow(SettingsContract.SettingsState())
    val uiState: StateFlow<SettingsContract.SettingsState> = _uiState.asStateFlow()
    
    // 부수효과
    private val _effect = Channel<SettingsContract.SettingsEffect>()
    val effect = _effect.receiveAsFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * 사용자 Intent 처리
     */
    fun handleIntent(intent: SettingsContract.SettingsIntent) {
        when (intent) {
            is SettingsContract.SettingsIntent.ChangeTheme -> changeTheme(intent.themeMode)
            is SettingsContract.SettingsIntent.ChangeLanguage -> changeLanguage(intent.language)
            SettingsContract.SettingsIntent.ClickHelp -> navigateToHelp()
            SettingsContract.SettingsIntent.ClickFAQ -> navigateToFAQ()
            SettingsContract.SettingsIntent.ClickAppInfo -> navigateToAppInfo()
            SettingsContract.SettingsIntent.ClickLicense -> navigateToLicense()
        }
    }
    
    /**
     * 저장된 설정 불러오기
     */
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                getThemeModeUseCase(),
                getLanguageUseCase()
            ) { theme, language ->
                _uiState.update { currentState ->
                    currentState.copy(
                        themeMode = theme,
                        language = language,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }
    
    /**
     * 테마 변경
     */
    private fun changeTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(themeMode)
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
    
    private fun navigateToHelp() {
        viewModelScope.launch {
            _effect.send(SettingsContract.SettingsEffect.NavigateToHelp)
        }
    }
    
    private fun navigateToFAQ() {
        viewModelScope.launch {
            _effect.send(SettingsContract.SettingsEffect.NavigateToFAQ)
        }
    }
    
    private fun navigateToAppInfo() {
        viewModelScope.launch {
            _effect.send(SettingsContract.SettingsEffect.NavigateToAppInfo)
        }
    }
    
    private fun navigateToLicense() {
        viewModelScope.launch {
            _effect.send(SettingsContract.SettingsEffect.NavigateToLicense)
        }
    }
}