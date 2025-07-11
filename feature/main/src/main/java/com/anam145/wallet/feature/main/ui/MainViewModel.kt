package com.anam145.wallet.feature.main.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetInstalledMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.InitializeMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.CheckInitializationStateUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.ObserveBlockchainServiceUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.SwitchBlockchainUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetActiveBlockchainUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.SetActiveBlockchainUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.ObserveAppChangesUseCase
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

/**
 * Main 화면의 ViewModel
 * 
 * UseCase 설명:
 * - GetInstalledMiniAppsUseCase: 설치된 모든 미니앱 목록을 가져옴
 * - InitializeMiniAppsUseCase: 미니앱 초기 설치 및 설정을 수행
 * - CheckInitializationStateUseCase: 미니앱 초기화 완료 여부를 확인
 * - ObserveBlockchainServiceUseCase: 블록체인 서비스 연결 상태를 실시간으로 관찰
 * - SwitchBlockchainUseCase: 활성 블록체인을 다른 블록체인으로 전환
 * - GetActiveBlockchainUseCase: 저장된 활성 블록체인 ID를 조회
 * - SetActiveBlockchainUseCase: 선택한 블록체인 ID를 영구 저장
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getInstalledMiniAppsUseCase: GetInstalledMiniAppsUseCase,
    private val initializeMiniAppsUseCase: InitializeMiniAppsUseCase,
    private val checkInitializationStateUseCase: CheckInitializationStateUseCase,
    private val observeBlockchainServiceUseCase: ObserveBlockchainServiceUseCase,
    private val switchBlockchainUseCase: SwitchBlockchainUseCase,
    private val getActiveBlockchainUseCase: GetActiveBlockchainUseCase,
    private val setActiveBlockchainUseCase: SetActiveBlockchainUseCase,
    private val observeAppChangesUseCase: ObserveAppChangesUseCase
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
    
    
    /**
     * ViewModel 초기화
     * 
     * 1. observeBlockchainService() - 서비스 상태 관찰 시작 (아직 앱이 없어도 서비스 연결 준비)
     * 2. initializeAndLoad() - 앱이 없으면 zip에서 설치, 있으면 로드
     * 
     * combine 연산자로 서비스 연결과 activeBlockchainId를 모두 관찰하므로,
     * 둘 다 준비되면 자동으로 블록체인이 활성화됨
     */
    init {
        // 블록체인 서비스 상태 관찰 먼저 시작
        observeBlockchainService()
        
        // 앱 초기화 및 로드
        initializeAndLoad()
        
        // 앱 변경 이벤트 구독 (설치/삭제 시 자동 새로고침)
        observeAppChanges()
    }

    fun handleIntent(intent: MainContract.MainIntent) {
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
                    val miniAppsMap = result.data
                    val miniAppsList = miniAppsMap.values.toList()
                    val blockchainApps = miniAppsList.filter { it.type == MiniAppType.BLOCKCHAIN }
                    val regularApps = miniAppsList.filter { it.type == MiniAppType.APP }
                    
                    // 저장된 활성 블록체인 ID 복원 또는 첫 번째 블록체인 선택
                    // 이 시점에서는 UI 상태만 설정하고, 실제 블록체인 활성화는
                    // observeBlockchainService()에서 서비스 연결 후 자동으로 처리됨
                    val savedActiveId = getActiveBlockchainUseCase().first()
                    val activeId = when {
                        savedActiveId != null && miniAppsMap.containsKey(savedActiveId) && miniAppsMap[savedActiveId]?.type == MiniAppType.BLOCKCHAIN -> savedActiveId
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
                        is MiniAppResult.Error.MiniAppNotFound ->
                            "미니앱을 찾을 수 없습니다: ${result.appId}"
                        is MiniAppResult.Error.UninstallFailed ->
                            "앱 삭제 실패: ${result.appId}"
                        is MiniAppResult.Error.UnknownError ->
                            "알 수 없는 오류: ${result.message}"
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
            setActiveBlockchainUseCase(miniApp.appId)
            
            // 3. observeBlockchainService()가 activeBlockchainId 변경을 감지하여
            //    자동으로 블록체인을 전환할 것임 (중복 호출 방지)
            //    서비스가 연결되지 않은 경우에도 observeBlockchainService()가
            //    연결 후 자동으로 활성화를 처리함
            
            // 4. 블록체인 UI 액티비티 실행
            _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
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
        // viewModelScope: ViewModel의 생명주기에 바인딩된 코루틴 스코프
        // ViewModel이 clear될 때 자동으로 취소됨
        viewModelScope.launch {
            // combine: 두 개의 Flow를 합쳐서 하나의 Flow로 만듦
            // 둘 중 하나라도 새 값을 방출하면 최신 값들로 조합하여 방출
            combine(
                // 첫 번째 Flow: 블록체인 서비스 연결 상태를 관찰
                observeBlockchainServiceUseCase(), // → Flow<ServiceState>
                
                // 두 번째 Flow: UI 상태에서 activeBlockchainId만 추출
                // map: Flow의 각 값을 변환 (State 전체 → activeBlockchainId만)
                // distinctUntilChanged: 같은 값이 연속으로 오면 무시 (중복 제거)
                _uiState.map { it.activeBlockchainId }.distinctUntilChanged()
            ) { serviceState, activeId ->
                // combine의 transform 람다: 두 Flow의 최신 값을 받아 결합
                // Pair로 묶어서 반환 (to는 infix 함수로 Pair 생성)
                serviceState to activeId
            }.collect { (serviceState, activeId) ->
                // collect: Flow를 구독하고 각 값에 대해 처리
                // 구조 분해 선언으로 Pair를 풀어서 받음
                
                // 서비스가 연결되고 activeBlockchainId가 있으면 자동 활성화
                // nullable 타입 체크를 위해 로컬 변수로 추출 (스마트 캐스트)
                val service = serviceState.service
                
                // 3가지 조건 모두 만족해야 블록체인 활성화:
                // 1. 서비스 연결됨 2. service 객체 존재 3. 활성화할 ID 존재
                if (serviceState.isConnected && service != null && activeId != null) {
                    // 블록체인 전환 시도 (결과값이 Unit이므로 성공/실패만 체크)
                    when (val result = switchBlockchainUseCase(activeId, service)) {
                        is MiniAppResult.Success -> {
                            Log.d("MainViewModel", "Auto-activated blockchain: $activeId")
                        }
                        is MiniAppResult.Error.Unknown -> {
                            // 이제 result.cause를 사용해서 구체적인 에러 정보 로깅
                            Log.e("MainViewModel", "Failed to auto-activate blockchain: $activeId - ${result.cause.message}")
                        }
                        is MiniAppResult.Error -> {
                            // 다른 에러 타입들 (현재는 Unknown만 사용중)
                            Log.e("MainViewModel", "Error auto-activating blockchain: $activeId - $result")
                        }
                    }
                }
            }
        }
    }

    /**
     * 미니앱 변경 이벤트를 구독하여 자동으로 새로고침
     * 
     * MiniAppScanner에서 clearCache()가 호출될 때마다
     * (앱 설치/삭제 시) 이벤트를 받아 앱 목록을 새로고침합니다.
     * 이를 통해 5분 캐시를 유지하면서도 변경사항은 즉시 반영됩니다.
     */
    private fun observeAppChanges() {
        viewModelScope.launch {
            observeAppChangesUseCase().collect {
                // 앱 변경 이벤트를 받으면 목록 새로고침
                loadMiniApps()
            }
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
}