package com.anam145.wallet.feature.settings.ui

import com.anam145.wallet.core.common.model.Language

/**
 * Settings 화면의 MVI Contract
 * 
 * 설정 화면의 모든 상태, 의도(Intent), 부수효과를 정의.
 * 
 * Contract 패턴의 장점:
 * - 한 곳에서 화면의 모든 동작을 파악 가능
 * - State, Intent(Event), Effect를 명확히 구분
 * - 테스트와 유지보수가 용이
 *
 *  SettingsContract만 보면:
 *  "아, 이 화면은..."
 *  - 테마랑 언어를 보여주고 (State)
 *  - 테마/언어 변경, 도움말 클릭 가능하고 (Intent)
 *  - 다른 화면으로 이동할 수 있구나 (Effect)
 *
 *  신입: "Settings 화면이 뭐하는 곳이에요?"
 *  선임: "SettingsContract 봐!"
 *  신입: "아, 다 여기 있네요!"
 */
interface SettingsContract {
    
    /**
     * Settings 화면의 UI 상태
     * 
     * State = "화면에 지속적으로 표시되는 데이터"
     * - 화면 회전 시에도 유지되어야 함
     * - 사용자가 현재 보고 있는 설정값들
     * 
     * @property language 현재 선택된 언어 설정
     */
    data class SettingsState(
        val language: Language = Language.KOREAN
    )
    
    /**
     * 사용자의 의도(Intent)
     * 
     * Intent/Event = "사용자가 수행한 액션"
     * - 버튼 클릭, 스위치 토글 등의 사용자 입력
     * - ViewModel이 처리해야 할 명령
     * - 발생 즉시 처리되고 사라짐
     * 
     * sealed interface 사용 이유:
     * - 모든 가능한 액션을 제한적으로 정의
     * - when 문에서 else 브랜치 불필요
     */
    sealed interface SettingsIntent {
        data class ChangeLanguage(val language: Language) : SettingsIntent
        data object ClickHelp : SettingsIntent
        data object ClickFAQ : SettingsIntent
        data object ClickAppInfo : SettingsIntent
        data object ClickLicense : SettingsIntent
    }
    
    /**
     * 부수효과(Side Effect)
     * 
     * Effect = "한 번만 발생하는 시스템 동작"
     * - 화면 전환, 토스트, 다이얼로그 등
     * - 화면 회전 시 반복되면 안 됨
     * - Channel을 통해 전달되어 한 번만 소비됨
     * 
     * 이 화면에서는 주로 네비게이션 Effect만 있음
     * (설정 변경은 State로 처리되므로 Effect 불필요)
     */
    sealed interface SettingsEffect {
        data object NavigateToHelp : SettingsEffect
        data object NavigateToFAQ : SettingsEffect
        data object NavigateToAppInfo : SettingsEffect
        data object NavigateToLicense : SettingsEffect
    }
}

