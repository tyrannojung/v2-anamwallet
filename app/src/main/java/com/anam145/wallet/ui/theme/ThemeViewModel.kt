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
 * @HiltViewModel: Hilt가 이 ViewModel을 관리하라고 표시
 * @Inject constructor: 생성자에 필요한 의존성을 Hilt가 주입
 * : ViewModel(): Android ViewModel 상속
 * ThemeViewModel은 앱 시작부터 끝까지 살아있음
 * 최대한 가볍게 유지
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    themeRepository: ThemeRepository
) : ViewModel() {
    
    /**
     * 현재 테마 모드
     * StateFlow(Hot StateFlow) : 항상 현재 상태 값을 가지고 있으며, 상태가 변경될 때 구독자들에게 이를 알리는 역할
     */
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode // Cold Flow
        .stateIn(
            scope = viewModelScope, // 1. scope: 구독을 관리할 코루틴 스코프
            started = SharingStarted.WhileSubscribed(5_000), // 2. started: 언제 구독을 시작/중지할지, 5_000, 5초 지연
            initialValue = ThemeMode.SYSTEM // 3. initialValue: 첫 값이 올 때까지 사용할 기본값
        ) // → Hot StateFlow로 변환!

    /**
     * 5초 구독이 필요한 이유
     * 1. 화면 회전 시 Repository 구독 유지
     * 2. 설정 화면 → 메인 화면 → 설정 화면 (2초 내)
     *    └─ 5초 버퍼로 계속 구독 유지 (효율적!)
     * */
}