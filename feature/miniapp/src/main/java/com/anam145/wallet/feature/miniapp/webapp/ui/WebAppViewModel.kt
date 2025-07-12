package com.anam145.wallet.feature.miniapp.webapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.model.TransactionRequest
import com.anam145.wallet.feature.miniapp.common.domain.usecase.ConnectToServiceUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.LoadMiniAppManifestUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.RequestTransactionUseCase
import com.anam145.wallet.feature.miniapp.webapp.domain.usecase.GetActiveBlockchainIdFromWebAppServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import com.anam145.wallet.core.common.extension.resolveEntryPoint

/**
 * WebApp 화면의 ViewModel
 * 
 * MVI 패턴에 따라 상태 관리 및 비즈니스 로직을 처리합니다.
 */
@HiltViewModel
class WebAppViewModel @Inject constructor(
    private val loadMiniAppManifestUseCase: LoadMiniAppManifestUseCase,
    private val connectToServiceUseCase: ConnectToServiceUseCase,
    private val requestTransactionUseCase: RequestTransactionUseCase,
    private val getActiveBlockchainIdUseCase: GetActiveBlockchainIdFromWebAppServiceUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "WebAppViewModel"
    }
    
    private val _uiState = MutableStateFlow(WebAppContract.State())
    val uiState: StateFlow<WebAppContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<WebAppContract.Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<WebAppContract.Effect> = _effect.asSharedFlow()
    
    init {
        // 서비스 연결 상태 관찰
        observeServiceConnection()
    }

    /**
     * WebApp 초기화
     * Screen에서 호출되며, appId를 받아 WebApp을 로드합니다.
     */
    fun initialize(appId: String) {
        if (_uiState.value.appId.isEmpty()) {
            viewModelScope.launch {
                _uiState.update { it.copy(appId = appId, isLoading = true) }
                
                // 매니페스트 로드
                when (val result = loadMiniAppManifestUseCase(appId)) {
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
                        Log.e(TAG, "Failed to load manifest for $appId: $result")
                    }
                }
                
                // 서비스 연결
                connectToService()
            }
        }
    }
    
    fun handleIntent(intent: WebAppContract.Intent) {
        when (intent) {
            is WebAppContract.Intent.RequestTransaction -> requestTransaction(intent.transactionData)
            is WebAppContract.Intent.RetryServiceConnection -> retryServiceConnection()
            is WebAppContract.Intent.DismissError -> dismissError()
            is WebAppContract.Intent.NavigateBack -> navigateBack()
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
     * WebView에 로드할 URL을 생성하고 State에 설정합니다.
     * 
     * 역할:
     * - manifest의 entryPoint를 기반으로 미니앱 URL 생성
     * - 생성된 URL을 State의 webUrl에 설정
     * - Screen의 LaunchedEffect가 이를 감지하여 WebView에 로드
     * 
     * URL 형식: https://appId.miniapp.local/entryPoint
     */
    private fun loadUrlInWebView() {
        _uiState.value.manifest?.let { manifest ->
            val appId = _uiState.value.appId
            val mainPage = manifest.resolveEntryPoint()
            // WebView는 도메인을 소문자로 변환하므로 일치시킴
            val url = "https://${appId.lowercase()}.miniapp.local/$mainPage"

            _uiState.update { it.copy(webUrl = url) }
        }
    }

    /**
     * 서비스 연결 상태를 지속적으로 관찰합니다.
     * 
     * 역할:
     * - WebAppService와의 연결 상태를 실시간 모니터링
     * - Service가 메모리 부족이나 시스템에 의해 종료될 수 있으므로,
     *   연결 상태를 추적하여 UI를 업데이트합니다.
     * 
     * 동작 방식:
     * 1. connectToServiceUseCase를 통해 서비스 연결 상태 Flow를 구독
     * 2. 연결 상태가 변경될 때마다 UI State 업데이트
     * 3. 서비스가 재연결된 경우(false → true), 활성 블록체인 정보 재확인
     * 4. UI에서는 isServiceConnected가 false일 때 ServiceConnectionCard를 표시
     */
    private fun observeServiceConnection() {
        viewModelScope.launch {
            connectToServiceUseCase.observeConnectionState().collect { isConnected ->
                val previousState = _uiState.value.isServiceConnected
                _uiState.update { it.copy(isServiceConnected = isConnected) }
                
                // 서비스가 새로 연결되었을 때 활성화된 블록체인 정보 가져오기
                if (!previousState && isConnected) {
                    checkActiveBlockchain()
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
    
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(WebAppContract.Effect.NavigateBack)
        }
    }
    
    /**
     * WebView가 생성되고 준비되었음을 기록합니다.
     * 
     * 호출 시점:
     * - WebAppScreen에서 WebAppWebView가 생성된 후
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
     * 웹앱 서비스에 연결(바인딩)합니다.
     * 
     * 역할:
     * - WebAppService에 바인딩하여 블록체인 서비스와 통신
     * - 연결 성공 시 현재 활성화된 블록체인 정보 확인
     * 
     * 주의:
     * - WebAppService는 Main 프로세스의 블록체인 서비스와 통신하는 중개자
     * - 결제 요청 시 활성 블록체인 정보가 필요하므로 연결 필수
     */
    private fun connectToService() {
        viewModelScope.launch {
            when (val result = connectToServiceUseCase.connect()) {
                is MiniAppResult.Success -> {
                    Log.d(TAG, "Service connected successfully")
                    // 서비스 연결 후 활성화된 블록체인 정보 확인
                    checkActiveBlockchain()
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
     * 트랜잭션 요청을 처리합니다.
     * 
     * 역할:
     * - WebView의 JavaScript에서 전달된 트랜잭션 요청 처리
     * - 활성 블록체인으로 트랜잭션 요청 전달
     * - 결과를 다시 JavaScript로 전달
     * 
     * 동작:
     * 1. 서비스 연결 상태 확인
     * 2. 활성 블록체인 ID 조회
     * 3. TransactionRequest 생성 및 전송
     * 4. 응답을 WebView로 전달
     */
    private fun requestTransaction(transactionData: JSONObject) {
        viewModelScope.launch {
            // 서비스 연결 확인
            if (!_uiState.value.isServiceConnected) {
                _effect.emit(WebAppContract.Effect.ShowError("서비스가 연결되지 않았습니다"))
                return@launch
            }
            
            try {
                // 활성 블록체인 ID 가져오기
                val activeBlockchainId = when (val result = getActiveBlockchainIdUseCase()) {
                    is MiniAppResult.Success -> result.data
                    is MiniAppResult.Error -> {
                        _effect.emit(WebAppContract.Effect.ShowError("활성화된 블록체인이 없습니다"))
                        return@launch
                    }
                }
                
                // TransactionRequest 생성 - 원본 데이터를 그대로 전달
                val request = TransactionRequest(
                    requestId = transactionData.optString("requestId", "req_${System.currentTimeMillis()}"),
                    blockchainId = activeBlockchainId,
                    transactionData = transactionData.toString()  // 원본 JSON 문자열 그대로
                )
                
                // 트랜잭션 요청
                when (val result = requestTransactionUseCase(request)) {
                    is MiniAppResult.Success -> {
                        val response = result.data
                        // responseData를 파싱하여 requestId와 status를 추가한 형태로 전달
                        val responseToWebApp = try {
                            val responseDataJson = JSONObject(response.responseData)
                            responseDataJson.put("requestId", response.requestId)
                            responseDataJson.put("status", response.status)
                            responseDataJson.toString()
                        } catch (e: Exception) {
                            // 파싱 실패 시 기본 형태로 전달
                            response.toJson()
                        }
                        _effect.emit(
                            WebAppContract.Effect.SendTransactionResponse(responseToWebApp)
                        )
                    }
                    is MiniAppResult.Error -> {
                        val errorMessage = when (result) {
                            is MiniAppResult.Error.Unknown -> result.cause.message ?: "결제 처리 실패"
                            else -> "결제 처리 실패"
                        }
                        
                        // Toast 메시지 표시
                        _effect.emit(WebAppContract.Effect.ShowError(errorMessage))
                        
                        // JavaScript로도 에러 전달
                        val errorResponse = """{"requestId": "${request.requestId}", "error": "$errorMessage"}"""
                        _effect.emit(
                            WebAppContract.Effect.SendTransactionResponse(errorResponse)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing transaction request", e)
                _effect.emit(WebAppContract.Effect.ShowError("트랜잭션 요청 처리 중 오류 발생"))
            }
        }
    }
    
    /**
     * manifest와 WebView가 모두 준비되었는지 확인하고 URL을 로드합니다.
     * 
     * 이 메서드는 두 가지 경우에 호출됩니다:
     * 1. manifest 로드 완료 시 (initialize에서)
     * 2. WebView 준비 완료 시 (webViewReady에서)
     * 
     * 두 조건이 모두 충족되었을 때만 실제 URL 로드가 실행됩니다.
     */
    private fun checkAndLoadUrl() {
        if (_uiState.value.manifest != null && _uiState.value.webViewReady) {
            loadUrlInWebView()
        }
    }
    
    /**
     * 현재 활성화된 블록체인 정보를 확인합니다.
     * 
     * 역할:
     * - 서비스에서 현재 활성 블록체인 ID 조회
     * - 블록체인 이름을 가져와 Header에 표시
     * 
     * 호출 시점:
     * - 서비스 연결 성공 후
     * - 서비스 재연결 시 (observeServiceConnection에서)
     * 
     * WebApp과 BlockchainViewModel의 차이점:
     * - WebApp: 활성 블록체인 정보를 표시만 함
     * - Blockchain: 자신이 활성화되었는지 확인
     */
    private fun checkActiveBlockchain() {
        viewModelScope.launch {
            when (val result = getActiveBlockchainIdUseCase()) {
                is MiniAppResult.Success -> {
                    val blockchainId = result.data
                    // 블록체인 ID로 매니페스트를 로드해서 이름 가져오기
                    when (val manifestResult = loadMiniAppManifestUseCase(blockchainId)) {
                        is MiniAppResult.Success -> {
                            _uiState.update { 
                                it.copy(
                                    activeBlockchainId = blockchainId,
                                    activeBlockchainName = manifestResult.data.name
                                )
                            }
                        }
                        is MiniAppResult.Error -> {
                            // 매니페스트 로드 실패 시 ID만 표시
                            _uiState.update { 
                                it.copy(
                                    activeBlockchainId = blockchainId,
                                    activeBlockchainName = blockchainId
                                )
                            }
                        }
                    }
                }
                is MiniAppResult.Error -> {
                    // 활성화된 블록체인이 없음
                    _uiState.update { 
                        it.copy(
                            activeBlockchainId = null,
                            activeBlockchainName = null
                        )
                    }
                }
            }
        }
    }
}