package com.anam145.wallet.feature.miniapp.blockchain.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.usecase.LoadMiniAppManifestUseCase
import com.anam145.wallet.feature.miniapp.blockchain.domain.usecase.ObserveBlockchainServiceConnectionUseCase
import com.anam145.wallet.feature.miniapp.blockchain.domain.usecase.ConnectToBlockchainServiceUseCase
import com.anam145.wallet.feature.miniapp.blockchain.domain.usecase.GetActiveBlockchainIdFromBlockchainServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import com.anam145.wallet.core.common.extension.resolveEntryPoint

/**
 * 블록체인 UI 화면의 ViewModel
 * 
 * MVI 패턴에 따라 상태 관리 및 비즈니스 로직을 처리합니다.
 */
@HiltViewModel
class BlockchainViewModel @Inject constructor(
    private val loadMiniAppManifestUseCase: LoadMiniAppManifestUseCase,
    private val observeBlockchainServiceConnectionUseCase: ObserveBlockchainServiceConnectionUseCase,
    private val connectToBlockchainServiceUseCase: ConnectToBlockchainServiceUseCase,
    private val getActiveBlockchainIdUseCase: GetActiveBlockchainIdFromBlockchainServiceUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "BlockchainViewModel"
    }
    
    private val _uiState = MutableStateFlow(BlockchainContract.State())
    val uiState: StateFlow<BlockchainContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<BlockchainContract.Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<BlockchainContract.Effect> = _effect.asSharedFlow()
    
    init {
        // 서비스 연결 상태 관찰
        observeServiceConnection()
    }

    fun handleIntent(intent: BlockchainContract.Intent) {
        when (intent) {
            is BlockchainContract.Intent.RetryServiceConnection -> retryServiceConnection()
            is BlockchainContract.Intent.DismissError -> dismissError()
            is BlockchainContract.Intent.NavigateBack -> navigateBack()
        }
    }
    
    /**
     * 블록체인 UI 초기화
     * Screen에서 호출되며, blockchainId를 받아 블록체인을 로드합니다.
     */
    fun initialize(blockchainId: String) {
        if (_uiState.value.blockchainId.isEmpty()) {
            viewModelScope.launch {
                _uiState.update { it.copy(blockchainId = blockchainId, isLoading = true) }
                
                // 10초 타임아웃 설정
                startConnectionTimeout()
                
                // 매니페스트 로드
                when (val result = loadMiniAppManifestUseCase(blockchainId)) {
                    is MiniAppResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                manifest = result.data,
                                isLoading = false
                            )
                        }
                        
                        // manifest와 WebView가 모두 준비되면 URL 로드
                        checkAndLoadUrl()
                    }
                    is MiniAppResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "매니페스트 로드 실패"
                            )
                        }
                        Log.e(TAG, "Failed to load manifest for $blockchainId: $result")
                    }
                }
                
                // 서비스 연결
                connectToService()
            }
        }
    }
    
    /**
     * WebView가 준비되었을 때 호출
     * Intent가 아닌 직접 메서드로 처리
     */
    fun onWebViewReady() {
        webViewReady()
    }


    /**
     * 서비스 연결 타임아웃을 시작합니다.
     * 10초 후에도 서비스가 연결되지 않으면 타임아웃 플래그를 설정합니다.
     */
    private fun startConnectionTimeout() {
        viewModelScope.launch {
            delay(10000) // 10초 대기
            
            // 10초 후에도 서비스가 연결되지 않았으면 타임아웃 설정
            if (!_uiState.value.isServiceConnected) {
                _uiState.update { it.copy(connectionTimeout = true) }
                Log.d(TAG, "Service connection timeout after 10 seconds")
            }
        }
    }

    /**
     * 블록체인 서비스 연결 상태를 지속적으로 관찰합니다.
     * 
     * 역할:
     * - BlockchainService와 BlockchainActivity는 같은 프로세스(:blockchain)에서 실행되지만,
     *   별개의 컴포넌트로서 각자의 생명주기를 가짐.
     * - Service가 메모리 부족이나 시스템에 의해 종료될 수 있으므로,
     *   연결 상태를 실시간으로 모니터링하여 UI를 업데이트함.
     * 
     * 동작 방식:
     * 1. repository.observeServiceConnection()을 통해 서비스 연결 상태 Flow를 구독
     * 2. 연결 상태가 변경될 때마다 UI State 업데이트
     * 3. 서비스가 재연결된 경우(false → true), 현재 블록체인의 활성화 상태를 재확인
     * 4. UI에서는 isServiceConnected가 false일 때 ServiceConnectionCard를 표시하여
     *    사용자가 수동으로 재연결을 시도할 수 있도록 함
     * 
     * 주요 사용 케이스:
     * - 헤더에 "활성화됨" 표시 여부 결정
     * - 서비스 연결이 끊어진 경우 재연결 UI 제공
     * - 서비스 재시작 후 블록체인 활성화 상태 동기화
     */
    private fun observeServiceConnection() {
        viewModelScope.launch {
            observeBlockchainServiceConnectionUseCase().collect { isConnected ->
                val previousState = _uiState.value.isServiceConnected
                _uiState.update { it.copy(isServiceConnected = isConnected) }
                
                // 서비스가 새로 연결되었을 때
                if (!previousState && isConnected) {
                    // 타임아웃 플래그 초기화 (연결 성공했으므로)
                    _uiState.update { it.copy(connectionTimeout = false) }
                    
                    // 활성화 상태 재확인
                    _uiState.value.blockchainId.let { blockchainId ->
                        checkIfActivated(blockchainId)
                    }
                }
            }
        }
    }


    /**
     * 서비스 재연결을 시도합니다.
     * 
     * 사용 시점:
     * - 사용자가 ServiceConnectionCard의 "재시도" 버튼 클릭 시
     * - RetryServiceConnection Intent를 통해 호출됨
     */
    private fun retryServiceConnection() {
        connectToService()
    }
    
    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * WebView가 생성되고 준비되었음을 기록합니다.
     * 
     * 호출 시점:
     * - BlockchainScreen에서 BlockchainWebView가 생성된 후
     * - onWebViewCreated 콜백을 통해 호출됨
     * 
     * 동작:
     * - webViewReady 플래그를 true로 설정
     * - manifest도 준비되었다면 URL 로드 시작
     */
    private fun webViewReady() {
        _uiState.update { it.copy(webViewReady = true) }
        
        // manifest와 WebView가 모두 준비되면 URL 로드
        checkAndLoadUrl()
    }

    /**
     * 블록체인 서비스에 연결(바인딩)합니다.
     * 
     * 역할:
     * - 이미 실행 중인 BlockchainService에 바인딩
     * - IBlockchainService 인터페이스 획득
     * - 연결 성공 시 현재 블록체인의 활성화 상태 확인
     * 
     * 주의:
     * - 서비스 시작은 MainViewModel에서 이미 완료됨
     * - 여기서는 연결만 수행 (bindService만 호출)
     * - 헷갈리면 안 되는 게, 메인과 연결한 bind와, activity와 연결한 bind는 다름
     * - 이거는 블록체인 activity 와 service bind임
     */
    private fun connectToService() {
        viewModelScope.launch {
            when (val result = connectToBlockchainServiceUseCase()) {
                is MiniAppResult.Success -> {
                    Log.d(TAG, "Service connected successfully")
                    // 서비스 연결 후 현재 활성화 상태만 확인
                    // 블록체인 전환은 MainViewModel에서 이미 처리됨
                    _uiState.value.blockchainId.let { blockchainId ->
                        checkIfActivated(blockchainId)
                    }
                }
                is MiniAppResult.Error -> {
                    Log.e(TAG, "Failed to connect to service: $result")
                    _uiState.update {
                        it.copy(error = "서비스 연결 실패")
                    }
                }
            }
        }
    }

    /**
     * 현재 블록체인이 활성화되어 있는지 확인합니다.
     * 
     * 역할:
     * - 서비스에 현재 활성화된 블록체인 ID 조회
     * - 현재 표시 중인 블록체인과 비교
     * - 일치하면 Header에 "활성화됨" 표시
     * 
     * 호출 시점:
     * - 서비스 연결 성공 후
     * - 서비스 재연결 시 (observeServiceConnection에서)
     * 
     * @param blockchainId 확인할 블록체인 ID
     */
    private fun checkIfActivated(blockchainId: String) {
        viewModelScope.launch {
            when (val result = getActiveBlockchainIdUseCase()) {
                is MiniAppResult.Success -> {
                    val isActivated = result.data == blockchainId
                    _uiState.update { it.copy(isActivated = isActivated) }
                }
                is MiniAppResult.Error -> {
                    // 활성화 상태 확인 실패 시 기본값 false
                    _uiState.update { it.copy(isActivated = false) }
                }
            }
        }
    }

    
    /**
     * manifest와 WebView가 모두 준비되었는지 확인하고 URL을 로드합니다.
     * 
     * 이 메서드는 두 가지 경우에 호출됩니다:
     * 1. manifest 로드 완료 시 (loadBlockchain에서)
     * 2. WebView 준비 완료 시 (webViewReady에서)
     * 두 공간에 있는 이유는, 두 조건이 만족해야 실행되므로, 둘다 실행되어도 안전한 비동기 패턴
     * 
     * 두 조건이 모두 충족되었을 때만 실제 URL 로드가 실행됩니다.
     */
    private fun checkAndLoadUrl() {
        if (_uiState.value.manifest != null && _uiState.value.webViewReady) {
            loadUrlInWebView()
        }
    }

    /**
     * WebView에 로드할 URL을 생성하고 State에 설정합니다.
     *
     * 역할:
     * - manifest의 entryPoint를 기반으로 미니앱 URL 생성
     * - 생성된 URL을 State의 webUrl에 설정
     * - Screen의 LaunchedEffect가 이를 감지하여 WebView에 로드
     *
     * URL 형식: https://blockchainId.miniapp.local/entryPoint
     */
    private fun loadUrlInWebView() {
        _uiState.value.manifest?.let { manifest ->
            val blockchainId = _uiState.value.blockchainId
            val mainPage = manifest.resolveEntryPoint()
            // WebView는 도메인을 소문자로 변환하므로 일치시킴
            val url = "https://${blockchainId.lowercase()}.miniapp.local/$mainPage"

            Log.d(TAG, "loadUrlInWebView: mainPage=$mainPage, resolvedFrom=${manifest.mainPage ?: manifest.pages}, url=$url")

            _uiState.update { it.copy(webUrl = url) }
        }
    }


    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(BlockchainContract.Effect.NavigateBack)
        }
    }
}