package com.anam145.wallet.feature.hub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.hub.usecase.GetMiniAppManifestsUseCase
import com.anam145.wallet.feature.hub.usecase.GetMiniAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor (
    private val getMiniAppsUseCase: GetMiniAppsUseCase,
    private val getMiniAppManifestsUseCase: GetMiniAppManifestsUseCase
) : ViewModel() {
    /**
     * UI 상태
     * MutableStateFlow : 이 상태를 관찰 가능하게 만드는 도구
     * private - 내부에서만 수정 가능한 MutableStateFlow
     * public - 외부에서는 읽기만 가능한 StateFlow
     * _uiState: ViewModel 내부에서만 값을 변경
     * uiState: UI는 읽기만 가능 (캡슐화)
     * */

    private val _uiState = MutableStateFlow(HubContract.HubState())
    val uiState: StateFlow<HubContract.HubState> = _uiState.asStateFlow()


    /**
     * State: 지속적인 상태 (현재 테마는 다크모드)
     * Effect: 일회성 동작 (화면 이동하세요!)
     * */
    private val _effect = MutableSharedFlow<HubContract.HubEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<HubContract.HubEffect> = _effect.asSharedFlow()

    init {
        loadSettings()
    }


    /**
     * 사용자 Intent 처리
     * MVI 방식 - 모든 액션을 Intent로 통합!
     */
    fun handleIntent(intent: HubContract.HubIntent) {
//        when (intent) {
//            is HubContract.HubIntent.ClickMiniApp -> navigateToManifest()
//            is HubContract.HubIntent.InstallMiniApp -> navigateToInstall()
//            is HubContract.HubIntent.UnInstallMiniApp -> navigateToUnInstall()
//        }
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
            // 2. 두 개의 Flow 합치기
            combine(
                getMiniAppsUseCase(),
                getMiniAppManifestsUseCase()
            ) { miniApp, miniAppManifest ->
                // 3. 두 값이 모두 도착하면 실행
                _uiState.update { currentState ->
                    currentState.copy(
                        installedMiniApp = miniApp,
                        installedMiniAppManifest = miniAppManifest
                    )
                }
            }.collect() // 4. Flow 구독 시작 , collect는 suspend 함수 코루틴 필요.
        }
    }

}