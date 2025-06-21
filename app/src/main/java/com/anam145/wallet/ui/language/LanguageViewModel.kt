package com.anam145.wallet.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import com.anam145.wallet.core.common.model.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 앱 전체 언어 상태를 관리하는 ViewModel
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    languageRepository: LanguageRepository
) : ViewModel() {
    
    /**
     * 현재 언어 설정
     */
    val language: StateFlow<Language> = languageRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Language.KOREAN
        )
}