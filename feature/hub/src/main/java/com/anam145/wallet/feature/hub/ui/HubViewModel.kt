package com.anam145.wallet.feature.hub.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.usecase.GetMiniAppManifestsUseCase
import com.anam145.wallet.feature.hub.usecase.GetInstalledMiniAppsUseCase
import com.anam145.wallet.feature.hub.usecase.GetUnInstalledMiniAppsUseCase
import com.anam145.wallet.feature.hub.usecase.InstallMiniAppUseCase
import com.anam145.wallet.feature.hub.usecase.UninstallMiniAppUseCase
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
=======
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.hub.domain.usecase.GetHubMiniAppsUseCase
import com.anam145.wallet.feature.hub.domain.usecase.InstallMiniAppFromHubUseCase
import com.anam145.wallet.feature.hub.domain.usecase.UninstallMiniAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
>>>>>>> feature/blockchain-modular
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor (
<<<<<<< HEAD
    private val getInstalledMiniAppsUseCase: GetInstalledMiniAppsUseCase,
    private val getMiniAppManifestsUseCase: GetMiniAppManifestsUseCase,
    private val getUnInstalledMiniAppsUseCase: GetUnInstalledMiniAppsUseCase,
    private val installMiniAppUseCase: InstallMiniAppUseCase,
    private val uninstallMiniAppUseCase: UninstallMiniAppUseCase
) : ViewModel() {
    /**
     * UI 상태
     * MutableStateFlow : 이 상태를 관찰 가능하게 만드는 도구
     * private - 내부에서만 수정 가능한 MutableStateFlow     * public - 외부에서는 읽기만 가능한 StateFlow
     *
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
        when (intent) {
            is HubContract.HubIntent.ClickMiniApp -> { Log.d(">>>", "miniapp 클릭됨") }
            is HubContract.HubIntent.InstallMiniApp -> installMiniApp(intent.miniApp)
            is HubContract.HubIntent.UninstallMiniApp -> uninstallMiniApp(intent.miniApp)
            is HubContract.HubIntent.RefreshUninstalledMiniApp -> refreshUninstalledMiniApp()
        }
    }

    private fun installMiniApp(miniApp : MiniApp) {
        Log.d(">>>", "miniApp install 클릭됨")
        viewModelScope.launch {
            installMiniAppUseCase(miniApp)
        }
    }

    private fun uninstallMiniApp(miniApp : MiniApp) {
        Log.d(">>>", "miniapp unistall 클릭됨")
        viewModelScope.launch {
            uninstallMiniAppUseCase(miniApp)
        }
    }

    private fun refreshUninstalledMiniApp() {
        Log.d(">>>", "miniapp refreshUninstlledMiniApp 클릭됨")
        viewModelScope.launch {
            getUnInstalledMiniAppsUseCase().collect { uninstalledList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        unInstalledMiniApp = uninstalledList
                    )
=======
    private val getHubMiniAppsUseCase: GetHubMiniAppsUseCase,
    private val installMiniAppFromHubUseCase: InstallMiniAppFromHubUseCase,
    private val uninstallMiniAppUseCase: UninstallMiniAppUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HubContract.HubState())
    val uiState: StateFlow<HubContract.HubState> = _uiState.asStateFlow()
    
    // 동시성 제어를 위한 Mutex
    private val operationMutex = Mutex()

    init {
        loadHubApps()
    }

    fun handleIntent(intent: HubContract.HubIntent) {
        when (intent) {
            is HubContract.HubIntent.InstallMiniApp -> installMiniApp(intent.appId)
            is HubContract.HubIntent.UninstallMiniApp -> uninstallMiniApp(intent.appId)
            is HubContract.HubIntent.RefreshMiniApps -> loadHubApps()
        }
    }

    private fun installMiniApp(appId: String) {
        Log.d("HubViewModel", "Installing miniApp: $appId")
        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update { 
                    it.copy(loadingAppIds = it.loadingAppIds + appId)
                }
                
                when (val result = installMiniAppFromHubUseCase(appId)) {
                    is MiniAppResult.Success -> {
                        Log.d("HubViewModel", "Install success: $appId")
                        loadHubApps() // 목록 새로고침
                    }
                    is MiniAppResult.Error -> {
                        Log.e("HubViewModel", "Install failed: $result")
                        val errorMessage = when (result) {
                            is MiniAppResult.Error.UnknownError -> result.message
                            else -> "Failed to install app"
                        }
                        _uiState.update { 
                            it.copy(
                                loadingAppIds = it.loadingAppIds - appId,
                                error = errorMessage
                            )
                        }
                    }
>>>>>>> feature/blockchain-modular
                }
            }
        }
    }

<<<<<<< HEAD
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
                getInstalledMiniAppsUseCase(),
                getUnInstalledMiniAppsUseCase()
            ) { installedMiniApp, unInstalledMiniApp ->
                // 3. 두 값이 모두 도착하면 실행
                _uiState.update { currentState ->
                    currentState.copy(
                        installedMiniApp = installedMiniApp,
                        unInstalledMiniApp = unInstalledMiniApp
                    )
                }
            }.collect() // 4. Flow 구독 시작 , collect는 suspend 함수 코루틴 필요.
        }
    }

=======
    private fun uninstallMiniApp(appId: String) {
        Log.d("HubViewModel", "Uninstalling miniApp: $appId")
        viewModelScope.launch {
            operationMutex.withLock {
                _uiState.update { 
                    it.copy(loadingAppIds = it.loadingAppIds + appId)
                }
                
                when (val result = uninstallMiniAppUseCase(appId)) {
                    is MiniAppResult.Success -> {
                        Log.d("HubViewModel", "Uninstall success: $appId")
                        loadHubApps() // 목록 새로고침
                    }
                    is MiniAppResult.Error -> {
                        Log.e("HubViewModel", "Uninstall failed: $result")
                        _uiState.update { 
                            it.copy(
                                loadingAppIds = it.loadingAppIds - appId,
                                error = "Failed to uninstall app"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadHubApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getHubMiniAppsUseCase()) {
                is MiniAppResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            hubApps = result.data,
                            loadingAppIds = emptySet(),  // 로딩 완료 시 초기화
                            error = null
                        )
                    }
                }
                is MiniAppResult.Error -> {
                    Log.e("HubViewModel", "Failed to load hub apps: $result")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load apps"
                        )
                    }
                }
            }
        }
    }
>>>>>>> feature/blockchain-modular
}