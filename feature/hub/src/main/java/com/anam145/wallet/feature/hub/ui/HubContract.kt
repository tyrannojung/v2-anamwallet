package com.anam145.wallet.feature.hub.ui

import com.anam145.wallet.core.common.model.MiniApp

interface HubContract {
    /**
     * HUB 화면의 UI 상태
     *
     * State = "화면에 지속적으로 표시되는 데이터"
     * - 화면 회전 시에도 유지되어야 함
     * - 사용자가 현재 보고 있는 설정값들
     *
     * @property MiniApp(installed, from db) 현재 기기에 설치된 Miniapp
     * @property MiniApp(Not Installed, from remote) 다운로드 가능한 Miniapp
     */

    data class HubState(
        val installedMiniApp: List<MiniApp> = emptyList(),
        val unInstalledMiniApp: List<MiniApp> = emptyList(),
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
    sealed interface HubIntent {
        data class InstallMiniApp(val miniApp: MiniApp) : HubIntent
        data class UninstallMiniApp(val miniApp: MiniApp) : HubIntent
        data object RefreshUninstalledMiniApp : HubIntent
        data class ClickMiniApp(val miniApp: MiniApp) : HubIntent
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
    sealed interface HubEffect {
        data object NavigateToManifestView : HubEffect
    }



}