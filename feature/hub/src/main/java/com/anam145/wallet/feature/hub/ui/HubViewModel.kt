package com.anam145.wallet.feature.hub.ui

import androidx.lifecycle.ViewModel
import com.anam145.wallet.feature.hub.usecase.GetMiniAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor (
    private val getMiniAppsUseCase: GetMiniAppsUseCase
) : ViewModel() {
    /**
     * UI 상태
     * MutableStateFlow : 이 상태를 관찰 가능하게 만드는 도구
     * private - 내부에서만 수정 가능한 MutableStateFlow
     * public - 외부에서는 읽기만 가능한 StateFlow
     * _uiState: ViewModel 내부에서만 값을 변경
     * uiState: UI는 읽기만 가능 (캡슐화)
     * */

    private val _uiState = MutableStateFlow(HubContract.HubState())
    val uiState: StateFlow<HubContract.HubState> = _uiState.asStateFlow()


}