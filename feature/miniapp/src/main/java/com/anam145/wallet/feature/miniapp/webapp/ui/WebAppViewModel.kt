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
import com.anam145.wallet.feature.miniapp.webapp.domain.usecase.GetCredentialsUseCase
import com.anam145.wallet.feature.miniapp.webapp.domain.usecase.CreateVPFromServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject
import javax.inject.Inject
import com.anam145.wallet.core.common.extension.resolveEntryPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialInfo

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
    private val getActiveBlockchainIdUseCase: GetActiveBlockchainIdFromWebAppServiceUseCase,
    private val getCredentialsUseCase: GetCredentialsUseCase,
    private val createVPUseCase: CreateVPFromServiceUseCase
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
                
                // 10초 타임아웃 설정
                startConnectionTimeout()
                
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
            is WebAppContract.Intent.RequestVP -> requestVP(intent.vpRequest)
            is WebAppContract.Intent.DismissVPBottomSheet -> dismissVPBottomSheet()
            is WebAppContract.Intent.SelectCredential -> selectCredential(intent.credentialId)
            is WebAppContract.Intent.ApproveTransaction -> approveTransaction()
            is WebAppContract.Intent.RejectTransaction -> rejectTransaction()
            is WebAppContract.Intent.DismissTransactionApproval -> dismissTransactionApproval()
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
                
                // 서비스가 새로 연결되었을 때
                if (!previousState && isConnected) {
                    // 타임아웃 플래그 초기화 (연결 성공했으므로)
                    _uiState.update { it.copy(connectionTimeout = false) }
                    
                    // 활성화된 블록체인 정보 가져오기
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
     * - 트랜잭션 승인 UI 표시
     * - 사용자 승인 후 활성 블록체인으로 전달
     * 
     * 동작:
     * 1. 서비스 연결 상태 확인
     * 2. 활성 블록체인 정보 조회
     * 3. 트랜잭션 승인 바텀시트 표시
     * 4. 사용자 승인/거절 처리
     */
    private fun requestTransaction(transactionData: JSONObject) {
        Log.d(TAG, "requestTransaction called with: $transactionData")
        viewModelScope.launch {
            // 서비스 연결 확인
            if (!_uiState.value.isServiceConnected) {
                Log.e(TAG, "Service not connected")
                _effect.emit(WebAppContract.Effect.ShowError("서비스가 연결되지 않았습니다"))
                return@launch
            }
            
            try {
                // 활성 블록체인 정보 가져오기
                val activeBlockchainId = when (val result = getActiveBlockchainIdUseCase()) {
                    is MiniAppResult.Success -> result.data
                    is MiniAppResult.Error -> {
                        Log.e(TAG, "No active blockchain")
                        _effect.emit(WebAppContract.Effect.ShowError("활성화된 블록체인이 없습니다"))
                        return@launch
                    }
                }
                
                Log.d(TAG, "Active blockchain: $activeBlockchainId, name: ${_uiState.value.activeBlockchainName}")
                
                // 트랜잭션 승인 바텀시트 표시
                Log.d(TAG, "Showing transaction approval bottom sheet")
                _uiState.update {
                    it.copy(
                        showTransactionApproval = true,
                        pendingTransactionJson = transactionData.toString()
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing transaction request", e)
                _effect.emit(WebAppContract.Effect.ShowError("트랜잭션 요청 처리 실패"))
            }
        }
    }
    
    /**
     * 트랜잭션을 승인합니다.
     */
    private fun approveTransaction() {
        viewModelScope.launch {
            val transactionJson = _uiState.value.pendingTransactionJson ?: return@launch
            
            try {
                val transactionData = JSONObject(transactionJson)
                
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
                    requestId = "req_${System.currentTimeMillis()}", // requestId는 항상 새로 생성
                    blockchainId = activeBlockchainId,
                    transactionData = transactionJson  // 원본 JSON 문자열 그대로
                )
                
                // 바텀시트 닫기
                _uiState.update {
                    it.copy(
                        showTransactionApproval = false,
                        pendingTransactionJson = null
                    )
                }
                
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
                        
                        // JavaScript로 에러 전달
                        _effect.emit(
                            WebAppContract.Effect.SendTransactionError(errorMessage)
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
     * 트랜잭션을 거절합니다.
     */
    private fun rejectTransaction() {
        viewModelScope.launch {
            // 바텀시트 닫기
            _uiState.update {
                it.copy(
                    showTransactionApproval = false,
                    pendingTransactionJson = null
                )
            }
            
            // JavaScript로 거절 이벤트 전송
            _effect.emit(WebAppContract.Effect.SendTransactionError("User rejected transaction"))
        }
    }
    
    /**
     * 트랜잭션 승인 바텀시트를 닫습니다.
     */
    private fun dismissTransactionApproval() {
        _uiState.update {
            it.copy(
                showTransactionApproval = false,
                pendingTransactionJson = null
            )
        }
        
        // 사용자가 취소한 경우 에러 이벤트 전송
        viewModelScope.launch {
            _effect.emit(WebAppContract.Effect.SendTransactionError("User cancelled"))
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
     * VP 요청을 처리합니다.
     * 
     * @param vpRequest VP 요청 JSON 객체
     */
    private fun requestVP(vpRequest: JSONObject) {
        viewModelScope.launch {
            try {
                val service = vpRequest.optString("service", "Unknown Service")
                val purpose = vpRequest.optString("purpose", "본인인증")
                val challenge = vpRequest.optString("challenge", "")
                val type = vpRequest.optString("type", "both")
                
                // 신분증 목록 조회
                when (val result = getCredentialsUseCase()) {
                    is Result<List<CredentialInfo>> -> {
                        if (result.isSuccess) {
                            val credentials = result.getOrNull() ?: emptyList()
                            
                            _uiState.update { 
                                it.copy(
                                    showVPBottomSheet = true,
                                    vpRequestData = WebAppContract.VPRequestData(
                                        service = service,
                                        purpose = purpose,
                                        challenge = challenge,
                                        type = type
                                    ),
                                    credentials = credentials
                                )
                            }
                        } else {
                            Log.e(TAG, "Failed to get credentials", result.exceptionOrNull())
                            _effect.emit(WebAppContract.Effect.ShowError("신분증 목록 조회 실패"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing VP request", e)
                _effect.emit(WebAppContract.Effect.ShowError("VP 요청 처리 실패"))
            }
        }
    }
    
    /**
     * VP 바텀시트를 닫습니다.
     */
    private fun dismissVPBottomSheet() {
        _uiState.update { 
            it.copy(
                showVPBottomSheet = false,
                vpRequestData = null
            )
        }
        
        // 사용자가 취소한 경우 에러 이벤트 전송
        viewModelScope.launch {
            _effect.emit(WebAppContract.Effect.SendVPError("User cancelled"))
        }
    }
    
    /**
     * 신분증을 선택하고 VP를 생성합니다.
     * 
     * @param credentialId 선택된 신분증 ID
     */
    private fun selectCredential(credentialId: String) {
        viewModelScope.launch {
            val vpRequestData = _uiState.value.vpRequestData ?: return@launch
            
            try {
                // AIDL을 통해 Main 프로세스에서 VP 생성 요청
                when (val result = createVPUseCase(credentialId, vpRequestData.challenge)) {
                    is Result<String> -> {
                        if (result.isSuccess) {
                            val vpJson = result.getOrNull() ?: ""
                            
                            // 바텀시트 닫기
                            _uiState.update { 
                                it.copy(
                                    showVPBottomSheet = false,
                                    vpRequestData = null,
                                    credentials = emptyList()
                                )
                            }
                            
                            // VP 응답 전송
                            _effect.emit(WebAppContract.Effect.SendVPResponse(vpJson))
                            
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "VP creation failed"
                            Log.e(TAG, "VP creation failed", result.exceptionOrNull())
                            
                            _effect.emit(WebAppContract.Effect.SendVPError(error))
                            
                            // 바텀시트 닫기
                            _uiState.update { 
                                it.copy(
                                    showVPBottomSheet = false,
                                    vpRequestData = null,
                                    credentials = emptyList()
                                )
                            }
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating VP", e)
                _effect.emit(WebAppContract.Effect.SendVPError("VP creation failed"))
                
                // 바텀시트 닫기
                _uiState.update { 
                    it.copy(
                        showVPBottomSheet = false,
                        vpRequestData = null,
                        credentials = emptyList()
                    )
                }
            }
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