package com.anam145.wallet.feature.miniapp.webapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.domain.model.PaymentRequest
import com.anam145.wallet.feature.miniapp.domain.usecase.ConnectToServiceUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.LoadMiniAppManifestUseCase
import com.anam145.wallet.feature.miniapp.domain.usecase.RequestPaymentUseCase
import com.anam145.wallet.feature.miniapp.webapp.domain.repository.WebAppServiceRepository
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
    private val requestPaymentUseCase: RequestPaymentUseCase,
    private val repository: WebAppServiceRepository
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
    
    fun processIntent(intent: WebAppContract.Intent) {
        when (intent) {
            is WebAppContract.Intent.LoadWebApp -> loadWebApp(intent.appId)
            is WebAppContract.Intent.RequestPayment -> requestPayment(intent.paymentData)
            is WebAppContract.Intent.RetryServiceConnection -> retryServiceConnection()
            is WebAppContract.Intent.DismissError -> dismissError()
            is WebAppContract.Intent.NavigateBack -> navigateBack()
            is WebAppContract.Intent.WebViewReady -> webViewReady()
        }
    }
    
    private fun loadWebApp(appId: String) {
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
                    
                    // 매니페스트 로드 후 URL 로드
                    if (_uiState.value.webViewReady) {
                        loadUrlInWebView()
                    }
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
    
    private fun requestPayment(paymentData: JSONObject) {
        viewModelScope.launch {
            // 서비스 연결 확인
            if (!_uiState.value.isServiceConnected) {
                _effect.emit(WebAppContract.Effect.ShowError("서비스가 연결되지 않았습니다"))
                return@launch
            }
            
            try {
                // 활성 블록체인 ID 가져오기
                val activeBlockchainId = when (val result = repository.getActiveBlockchainId()) {
                    is MiniAppResult.Success -> result.data
                    is MiniAppResult.Error -> {
                        _effect.emit(WebAppContract.Effect.ShowError("활성화된 블록체인이 없습니다"))
                        return@launch
                    }
                }
                
                // PaymentRequest 생성
                val request = PaymentRequest(
                    requestId = paymentData.optString("requestId", "req_${System.currentTimeMillis()}"),
                    blockchainId = activeBlockchainId,
                    amount = paymentData.getString("amount"),
                    description = paymentData.optString("description"),
                    metadata = paymentData.optJSONObject("metadata")?.let { metadata ->
                        metadata.keys().asSequence().associateWith { key -> metadata.get(key) }
                    } ?: emptyMap()
                )
                
                // 결제 요청
                when (val result = requestPaymentUseCase(request)) {
                    is MiniAppResult.Success -> {
                        val response = result.data
                        _effect.emit(
                            WebAppContract.Effect.SendPaymentResponse(response.toJson())
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
                            WebAppContract.Effect.SendPaymentResponse(errorResponse)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment request", e)
                _effect.emit(WebAppContract.Effect.ShowError("결제 요청 처리 중 오류 발생"))
            }
        }
    }
    
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
    
    private fun webViewReady() {
        _uiState.update { it.copy(webViewReady = true) }
        
        // WebView가 준비되고 manifest가 있으면 URL 로드
        if (_uiState.value.manifest != null) {
            loadUrlInWebView()
        }
    }
    
    private fun loadUrlInWebView() {
        viewModelScope.launch {
            _uiState.value.manifest?.let { manifest ->
                val appId = _uiState.value.appId
                val mainPage = manifest.resolveEntryPoint()
                val url = "https://$appId.miniapp.local/$mainPage"
                
                _effect.emit(WebAppContract.Effect.LoadUrl(url))
            }
        }
    }
    
    private fun checkActiveBlockchain() {
        viewModelScope.launch {
            when (val result = repository.getActiveBlockchainId()) {
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
    
    override fun onCleared() {
        super.onCleared()
        // 서비스 연결 해제
        viewModelScope.launch {
            connectToServiceUseCase.disconnect()
        }
    }
}