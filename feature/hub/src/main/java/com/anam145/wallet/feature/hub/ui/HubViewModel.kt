package com.anam145.wallet.feature.hub.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor (
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
                }
            }
        }
    }

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
}