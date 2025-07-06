package com.anam145.wallet.feature.main.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.IMainBridgeService
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetInstalledMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.InitializeMiniAppsUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.CheckInitializationStateUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.ObserveBlockchainServiceUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.SwitchBlockchainUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetActiveBlockchainUseCase
import com.anam145.wallet.feature.miniapp.common.domain.usecase.SetActiveBlockchainUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val getInstalledMiniAppsUseCase: GetInstalledMiniAppsUseCase,
    private val initializeMiniAppsUseCase: InitializeMiniAppsUseCase,
    private val checkInitializationStateUseCase: CheckInitializationStateUseCase,
    private val observeBlockchainServiceUseCase: ObserveBlockchainServiceUseCase,
    private val switchBlockchainUseCase: SwitchBlockchainUseCase,
    private val getActiveBlockchainUseCase: GetActiveBlockchainUseCase,
    private val setActiveBlockchainUseCase: SetActiveBlockchainUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainContract.MainState())
    val uiState: StateFlow<MainContract.MainState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MainContract.MainEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<MainContract.MainEffect> = _effect.asSharedFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        observeBlockchainService()
        initializeAndLoad()
        bindMainBridgeService() // ✅ 추가된 라인
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
            val isInitialized = checkInitializationStateUseCase()

            if (!isInitialized) {
                _uiState.update { it.copy(isSyncing = true) }

                when (val result = initializeMiniAppsUseCase()) {
                    is MiniAppResult.Success -> {
                        _uiState.update { it.copy(isSyncing = false, error = null) }
                    }
                    is MiniAppResult.Error -> {
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

            _isInitializing.value = false

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

                    val savedActiveId = getActiveBlockchainUseCase().first()
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

                    activeId?.let { id ->
                        Log.d("MainViewModel", "Active blockchain ID set to: $id (will be activated when service connects)")
                    }
                }
                is MiniAppResult.Error -> {
                    val errorMessage = when (result) {
                        is MiniAppResult.Error.NoAppsInstalled -> "설치된 앱이 없습니다"
                        is MiniAppResult.Error.ScanFailed -> "앱 스캔 실패: ${result.cause.message}"
                        is MiniAppResult.Error.InstallationFailed -> "앱 설치 실패: ${result.appId}"
                        is MiniAppResult.Error.ManifestNotFound -> "매니페스트를 찾을 수 없습니다: ${result.appId}"
                        is MiniAppResult.Error.InvalidManifest -> "유효하지 않은 매니페스트: ${result.appId}"
                        is MiniAppResult.Error.AppNotFound -> "앱을 찾을 수 없습니다: ${result.appId}"
                        is MiniAppResult.Error.Unknown -> "알 수 없는 오류: ${result.cause.message}"
                    }

                    _uiState.update {
                        it.copy(isLoading = false, error = errorMessage)
                    }
                }
            }
        }
    }

    private fun handleBlockchainClick(miniApp: MiniApp) {
        viewModelScope.launch {
            if (_uiState.value.activeBlockchainId == miniApp.appId) {
                _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
                return@launch
            }

            _uiState.update { it.copy(activeBlockchainId = miniApp.appId) }
            setActiveBlockchainUseCase(miniApp.appId)
            _effect.emit(MainContract.MainEffect.LaunchBlockchainActivity(miniApp.appId))
        }
    }

    private fun observeBlockchainService() {
        viewModelScope.launch {
            combine(
                observeBlockchainServiceUseCase.invoke(),
                _uiState.map { it.activeBlockchainId }.distinctUntilChanged()
            ) { serviceState, activeId ->
                serviceState to activeId
            }.collect { (serviceState, activeId) ->
                val service = serviceState.service

                if (serviceState.isConnected && service != null && activeId != null) {
                    when (val result = switchBlockchainUseCase(activeId, service)) {
                        is MiniAppResult.Success -> {
                            Log.d("MainViewModel", "Auto-activated blockchain: $activeId")
                        }
                        is MiniAppResult.Error.Unknown -> {
                            Log.e("MainViewModel", "Failed to auto-activate blockchain: $activeId - ${result.cause.message}")
                        }
                        is MiniAppResult.Error -> {
                            Log.e("MainViewModel", "Error auto-activating blockchain: $activeId - $result")
                        }
                    }
                }
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

    // 🔽🔽🔽 [⬇️ 추가된 필드] 🔽🔽🔽
    private var mainBridgeService: IMainBridgeService? = null
    private var isServiceBound: Boolean = false

    private fun bindMainBridgeService() {
        val context = getApplication<Application>()
        val intent = Intent().apply {
            component = ComponentName(
                "com.anam145.wallet",
                "com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService"
            )
        }

        context.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mainBridgeService = IMainBridgeService.Stub.asInterface(service)
                isServiceBound = true
                Log.d("MainViewModel", "MainBridgeService 바인딩 성공")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mainBridgeService = null
                isServiceBound = false
                Log.d("MainViewModel", "MainBridgeService 바인딩 해제")
            }
        }, Context.BIND_AUTO_CREATE)
    }

    fun sendPasswordToService(password: String) {
        viewModelScope.launch {
            if (isServiceBound && mainBridgeService != null) {
                try {
                    val result = mainBridgeService?.updatePassword(password)
                    Log.d("MainViewModel", "비밀번호 전달 결과: $result")
                } catch (e: RemoteException) {
                    Log.e("MainViewModel", "비밀번호 전달 실패", e)
                }
            } else {
                Log.e("MainViewModel", "서비스가 바인딩되지 않았습니다")
            }
        }
    }
}
