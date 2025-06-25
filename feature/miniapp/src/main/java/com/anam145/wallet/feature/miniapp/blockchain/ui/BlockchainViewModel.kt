package com.anam145.wallet.feature.miniapp.blockchain.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.domain.usecase.LoadMiniAppManifestUseCase
import com.anam145.wallet.feature.miniapp.blockchain.domain.repository.BlockchainServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val repository: BlockchainServiceRepository
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
    
    /**
     * 블록체인 UI 초기화
     * Screen에서 호출되며, blockchainId를 받아 블록체인을 로드합니다.
     */
    fun initialize(blockchainId: String) {
        if (_uiState.value.blockchainId.isEmpty()) {
            loadBlockchain(blockchainId)
        }
    }
    
    fun handleIntent(intent: BlockchainContract.Intent) {
        when (intent) {
            is BlockchainContract.Intent.RetryServiceConnection -> retryServiceConnection()
            is BlockchainContract.Intent.DismissError -> dismissError()
            is BlockchainContract.Intent.NavigateBack -> navigateBack()
        }
    }
    
    /**
     * WebView가 준비되었을 때 호출
     * Intent가 아닌 직접 메서드로 처리
     */
    fun onWebViewReady() {
        webViewReady()
    }
    
    private fun loadBlockchain(blockchainId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(blockchainId = blockchainId, isLoading = true) }
            
            // 매니페스트 로드
            when (val result = loadMiniAppManifestUseCase(blockchainId)) {
                is MiniAppResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            manifest = result.data,
                            isLoading = false
                        )
                    }
                    
                    // WebView가 준비되면 URL 로드
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
                    Log.e(TAG, "Failed to load manifest for $blockchainId: $result")
                }
            }
            
            // 서비스 연결
            connectToService()
        }
    }
    
    private fun connectToService() {
        viewModelScope.launch {
            when (val result = repository.connectToService()) {
                is MiniAppResult.Success -> {
                    Log.d(TAG, "Service connected successfully")
                    // 서비스 연결 후 현재 활성화 상태만 확인
                    // 블록체인 전환은 MainViewModel에서 이미 처리됨
                    _uiState.value.blockchainId?.let { blockchainId ->
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
    
    // 블록체인 전환은 MainViewModel에서 처리하므로 제거
    // BlockchainViewModel은 이미 활성화된 블록체인의 UI만 담당
    
    private fun observeServiceConnection() {
        viewModelScope.launch {
            repository.observeServiceConnection().collect { isConnected ->
                val previousState = _uiState.value.isServiceConnected
                _uiState.update { it.copy(isServiceConnected = isConnected) }
                
                // 서비스가 새로 연결되었을 때 활성화 상태 재확인
                if (!previousState && isConnected) {
                    _uiState.value.blockchainId?.let { blockchainId ->
                        checkIfActivated(blockchainId)
                    }
                }
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
            _effect.emit(BlockchainContract.Effect.NavigateBack)
        }
    }
    
    private fun checkIfActivated(blockchainId: String) {
        viewModelScope.launch {
            when (val result = repository.getActiveBlockchainId()) {
                is MiniAppResult.Success<String> -> {
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
    
    private fun loadUrlInWebView() {
        _uiState.value.manifest?.let { manifest ->
            val blockchainId = _uiState.value.blockchainId
            val mainPage = manifest.resolveEntryPoint()
            val url = "https://$blockchainId.miniapp.local/$mainPage"
            
            Log.d(TAG, "loadUrlInWebView: mainPage=$mainPage, resolvedFrom=${manifest.mainPage ?: manifest.pages}, url=$url")
            
            _uiState.update { it.copy(webUrl = url) }
        }
    }
    
    private fun webViewReady() {
        _uiState.update { it.copy(webViewReady = true) }
        
        // WebView가 준비되고 manifest가 있으면 URL 로드
        if (_uiState.value.manifest != null) {
            loadUrlInWebView()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 서비스 연결 해제
        viewModelScope.launch {
            repository.disconnectFromService()
        }
    }
}