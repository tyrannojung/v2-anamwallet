package com.anam145.wallet.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import com.anam145.wallet.core.common.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )
}