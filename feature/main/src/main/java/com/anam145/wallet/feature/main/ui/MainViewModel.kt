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
import com.anam145.wallet.feature.miniapp.domain.usecase.ActivateBlockchainUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getInstalledMiniAppsUseCase: GetInstalledMiniAppsUseCase,
    private val initializeMiniAppsUseCase: InitializeMiniAppsUseCase,
    private val checkInitializationStateUseCase: CheckInitializationStateUseCase,
    private val activateBlockchainUseCase: ActivateBlockchainUseCase,
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
    
    // 블록체인 서비스 참조
    private var blockchainService: com.anam145.wallet.feature.miniapp.IBlockchainService? = null
    
    init {
        // 블록체인 서비스 상태 관찰 먼저 시작
        observeBlockchainService()
        
        // 앱 초기화 및 로드
        initializeAndLoad()
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
                processIntent(MainContract.MainIntent.LoadMiniApps)
            }
        }
    }
    
    fun processIntent(intent: MainContract.MainIntent) {
        when (intent) {
            is MainContract.MainIntent.LoadMiniApps -> loadMiniApps()
            is MainContract.MainIntent.ClickBlockchainApp -> handleBlockchainClick(intent.miniApp)
            is MainContract.MainIntent.ClickRegularApp -> handleAppClick(intent.miniApp)
            is MainContract.MainIntent.ClickAddMore -> handleAddMoreClick()
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
                    
                    // 저장된 활성 블록체인 ID 또는 첫 번째 블록체인 활성화
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
                    
                    // 블록체인 활성화 (서비스 연결 후 처리됨)
                    activeId?.let { id ->
                        Log.d("MainViewModel", "Active blockchain ID set: $id")
                        // 서비스 연결은 observeBlockchainService에서 처리
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
    
    private fun handleBlockchainClick(miniApp: MiniApp) {
        viewModelScope.launch {
            // 이미 활성화된 블록체인이면 UI만 표시
            if (_uiState.value.activeBlockchainId == miniApp.appId) {
                _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
                return@launch
            }
            
            // 새로운 블록체인 활성화
            _uiState.update { it.copy(activeBlockchainId = miniApp.appId) }
            
            // 활성 블록체인 ID 저장
            blockchainDataStore.setActiveBlockchainId(miniApp.appId)
            
            // 블록체인 서비스에서 전환 (IO 스레드에서 실행)
            blockchainService?.let { service ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        // switchBlockchain은 이제 WebView만 재생성함 (서비스 유지)
                        service.switchBlockchain(miniApp.appId)
                    } catch (e: android.os.RemoteException) {
                        Log.e("MainViewModel", "Blockchain service disconnected", e)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error switching blockchain", e)
                    }
                }
            } ?: run {
                Log.w("MainViewModel", "Blockchain service not connected")
            }
            
            _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
        }
    }
    
    private fun handleAppClick(miniApp: MiniApp) {
        viewModelScope.launch {
            _effect.emit(MainContract.MainEffect.NavigateToMiniApp(miniApp.appId))
        }
    }
    
    private fun handleAddMoreClick() {
        viewModelScope.launch {
            _effect.emit(MainContract.MainEffect.NavigateToHub)
        }
    }
    
    /**
     * 블록체인 서비스 상태 관찰
     */
    private fun observeBlockchainService() {
        viewModelScope.launch {
            activateBlockchainUseCase.observeServiceState().collect { state ->
                _isBlockchainConnected.value = state.isConnected
                blockchainService = state.service
                
                // 서비스 연결 시 활성 블록체인 복원
                if (state.isConnected && state.service != null) {
                    // 이미 활성화된 블록체인이 있으면 전환
                    _uiState.value.activeBlockchainId?.let { activeId ->
                        viewModelScope.launch {
                            try {
                                // IO 스레드에서 AIDL 호출
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    activateBlockchainUseCase(activeId, state.service)
                                }
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Error activating blockchain on service connection", e)
                            }
                        }
                    }
                }
            }
        }
    }
}