package com.anam145.wallet.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import com.anam145.wallet.feature.settings.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.anam145.wallet.core.ui.theme.ThemeMode as UiThemeMode

/**
 * 앱 전체 테마 상태를 관리하는 ViewModel
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    themeRepository: ThemeRepository
) : ViewModel() {
    
    /**
     * 현재 테마 모드
     */
    val themeMode: StateFlow<UiThemeMode> = themeRepository.themeMode
        .map { featureThemeMode ->
            when (featureThemeMode) {
                ThemeMode.LIGHT -> UiThemeMode.LIGHT
                ThemeMode.DARK -> UiThemeMode.DARK
                ThemeMode.SYSTEM -> UiThemeMode.SYSTEM
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiThemeMode.SYSTEM
        )
}