package com.anam145.wallet.feature.main.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.domain.usecase.GetInstalledMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.InitializeMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.CheckInitializationStateUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.ObserveBlockchainServiceUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.SwitchBlockchainUseCase
import com.anam145.wallet.core.data.datastore.BlockchainDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getInstalledMiniAppsUseCase: GetInstalledMiniAppsUseCase,
    private val initializeMiniAppsUseCase: InitializeMiniAppsUseCase,
    private val checkInitializationStateUseCase: CheckInitializationStateUseCase,
    private val observeBlockchainServiceUseCase: ObserveBlockchainServiceUseCase,
    private val switchBlockchainUseCase: SwitchBlockchainUseCase,
    private val blockchainDataStore: BlockchainDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainContract.MainState())
    val uiState: StateFlow<MainContract.MainState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<MainContract.MainEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<MainContract.MainEffect> = _effect.asSharedFlow()
    
    // 스플래시 화면용 초기화 상태
    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()
    
    // 블록체인 서비스 연결 상태
    private val _isBlockchainConnected = MutableStateFlow(false)
    val isBlockchainConnected: StateFlow<Boolean> = _isBlockchainConnected.asStateFlow()
    
    // 블록체인 서비스 상태 (Clean Architecture를 위해 UseCase를 통해서만 접근)
    private var currentServiceState: ObserveBlockchainServiceUseCase.ServiceState? = null
    
    init {
        // 블록체인 서비스 상태 관찰 먼저 시작
        observeBlockchainService()
        
        // 앱 초기화 및 로드
        initializeAndLoad()
    }

    fun processIntent(intent: MainContract.MainIntent) {
        when (intent) {
            is MainContract.MainIntent.ClickBlockchainApp -> handleBlockchainClick(intent.miniApp)
            is MainContract.MainIntent.ClickRegularApp -> handleAppClick(intent.miniApp)
            is MainContract.MainIntent.ClickAddMore -> handleAddMoreClick()
        }
    }
    
    private fun initializeAndLoad() {
        viewModelScope.launch {
            // 초기화가 필요한지 확인
            val isInitialized = checkInitializationStateUseCase()
            
            if (!isInitialized) {
                // 초기화 진행 true면, 진행상태 ui에 표시 -> false로 바뀌어야 표시 x
                _uiState.update { it.copy(isSyncing = true) }
                
                when (val result = initializeMiniAppsUseCase()) {
                    is MiniAppResult.Success -> {
                        // 초기화 성공
                        _uiState.update { it.copy(isSyncing = false, error = null) }
                    }
                    is MiniAppResult.Error -> {
                        // 초기화 실패 - 에러 메시지 설정
                        val errorMessage = when (result) {
                            is MiniAppResult.Error.InstallationFailed ->
                                "앱 설치 실패: ${result.appId}"
                            else -> "초기화 중 오류가 발생했습니다"
                        }
                        _uiState.update { 
                            it.copy(isSyncing = false, error = errorMessage) 
                        }
                    }
                }
            }
            
            // 스플래시 종료
            _isInitializing.value = false
            
            // 에러가 없을 때만 앱 로드
            if (_uiState.value.error == null) {
                loadMiniApps()
            }
        }
    }
    
    private fun loadMiniApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = getInstalledMiniAppsUseCase()) {
                is MiniAppResult.Success -> {
                    val miniApps = result.data
                    val blockchainApps = miniApps.filter { it.type == MiniAppType.BLOCKCHAIN }
                    val regularApps = miniApps.filter { it.type == MiniAppType.APP }
                    
                    // 저장된 활성 블록체인 ID 복원 또는 첫 번째 블록체인 선택
                    // 이 시점에서는 UI 상태만 설정하고, 실제 블록체인 활성화는
                    // observeBlockchainService()에서 서비스 연결 후 자동으로 처리됨
                    val savedActiveId = blockchainDataStore.activeBlockchainId.first()
                    val activeId = when {
                        savedActiveId != null && blockchainApps.any { it.appId == savedActiveId } -> savedActiveId
                        blockchainApps.isNotEmpty() -> blockchainApps.first().appId
                        else -> null
                    }
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            blockchainApps = blockchainApps,
                            regularApps = regularApps,
                            activeBlockchainId = activeId,
                            error = null
                        )
                    }
                    
                    // 블록체인 활성화는 observeBlockchainService()에서 처리
                    // activeBlockchainId가 설정되고 서비스가 연결되면 자동으로 활성화됨
                    activeId?.let { id ->
                        Log.d("MainViewModel", "Active blockchain ID set to: $id (will be activated when service connects)")
                    }
                }
                is MiniAppResult.Error -> {
                    val errorMessage = when (result) {
                        is MiniAppResult.Error.NoAppsInstalled -> 
                            "설치된 앱이 없습니다"
                        is MiniAppResult.Error.ScanFailed -> 
                            "앱 스캔 실패: ${result.cause.message}"
                        is MiniAppResult.Error.InstallationFailed ->
                            "앱 설치 실패: ${result.appId}"
                        is MiniAppResult.Error.ManifestNotFound ->
                            "매니페스트를 찾을 수 없습니다: ${result.appId}"
                        is MiniAppResult.Error.InvalidManifest ->
                            "유효하지 않은 매니페스트: ${result.appId}"
                        is MiniAppResult.Error.AppNotFound ->
                            "앱을 찾을 수 없습니다: ${result.appId}"
                        is MiniAppResult.Error.Unknown ->
                            "알 수 없는 오류: ${result.cause.message}"
                    }
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 사용자가 블록체인 카드를 클릭했을 때 처리
     * 
     * 이 메서드는 사용자의 명시적인 블록체인 전환 요청을 처리합니다.
     * observeBlockchainService()와 달리 즉시 블록체인을 전환하고 UI를 표시합니다.
     */
    private fun handleBlockchainClick(miniApp: MiniApp) {
        viewModelScope.launch {
            // 이미 활성화된 블록체인이면 UI만 표시
            if (_uiState.value.activeBlockchainId == miniApp.appId) {
                _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
                return@launch
            }
            
            // 1. UI 상태 즉시 업데이트 (사용자 피드백)
            _uiState.update { it.copy(activeBlockchainId = miniApp.appId) }
            
            // 2. 영구 저장소에 활성 블록체인 ID 저장
            blockchainDataStore.setActiveBlockchainId(miniApp.appId)
            
            // 3. 블록체인 서비스에서 실제 전환 수행
            // observeBlockchainService()도 이 변경을 감지하지만,
            // 사용자 액션에 대한 즉각적인 반응을 위해 여기서도 직접 호출
            currentServiceState?.service?.let { service ->
                when (val result = switchBlockchainUseCase(miniApp.appId, service)) {
                    is MiniAppResult.Success -> {
                        Log.d("MainViewModel", "Blockchain switched to: ${miniApp.appId}")
                    }
                    is MiniAppResult.Error -> {
                        Log.e("MainViewModel", "Error switching blockchain: $result")
                        _effect.emit(MainContract.MainEffect.ShowError(
                            when (result) {
                                is MiniAppResult.Error.Unknown -> 
                                    result.cause.message ?: "블록체인 전환 실패"
                                else -> "블록체인 전환 실패"
                            }
                        ))
                    }
                }
            } ?: run {
                // 서비스가 아직 연결되지 않은 경우
                // observeBlockchainService()가 연결되면 자동으로 활성화할 것임
                Log.w("MainViewModel", "Blockchain service not connected yet, will activate when connected")
            }
            
            // 4. 블록체인 UI 액티비티 실행
            _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
        }
    }
    
    private fun handleAppClick(miniApp: MiniApp) {
        viewModelScope.launch {
            _effect.emit(MainContract.MainEffect.LaunchWebAppActivity(miniApp.appId))
        }
    }
    
    private fun handleAddMoreClick() {
        viewModelScope.launch {
            _effect.emit(MainContract.MainEffect.NavigateToHub)
        }
    }
    
    /**
     * 블록체인 서비스 상태 관찰 및 자동 활성화
     * 
     * 이 메서드가 블록체인 활성화의 중앙 제어점 역할을 합니다.
     * 서비스 연결 상태와 activeBlockchainId를 모두 관찰하여,
     * 두 조건이 모두 충족되면 자동으로 블록체인을 활성화합니다.
     */
    private fun observeBlockchainService() {
        viewModelScope.launch {
            // 서비스 상태와 activeBlockchainId를 함께 관찰
            combine(
                observeBlockchainServiceUseCase.invoke(),
                _uiState.map { it.activeBlockchainId }.distinctUntilChanged()
            ) { serviceState, activeId ->
                serviceState to activeId
            }.collect { (serviceState, activeId) ->
                // 서비스 연결 상태 업데이트
                _isBlockchainConnected.value = serviceState.isConnected
                currentServiceState = serviceState
                
                // 서비스가 연결되고 activeBlockchainId가 있으면 자동 활성화
                val service = serviceState.service
                if (serviceState.isConnected && service != null && activeId != null) {
                    when (val result = switchBlockchainUseCase(activeId, service)) {
                        is MiniAppResult.Success -> {
                            Log.d("MainViewModel", "Auto-activated blockchain: $activeId")
                        }
                        is MiniAppResult.Error -> {
                            Log.e("MainViewModel", "Error auto-activating blockchain: $activeId")
                            // 자동 활성화 실패는 조용히 처리 (사용자가 명시적으로 클릭한 것이 아니므로)
                        }
                    }
                }
            }
        }
    }
}