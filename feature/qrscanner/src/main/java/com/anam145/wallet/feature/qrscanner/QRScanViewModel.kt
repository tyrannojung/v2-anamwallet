package com.anam145.wallet.feature.qrscanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QR 스캔 화면의 상태 관리
 */
data class QRScanUiState(
    val isScanning: Boolean = false,
    val scannedCode: String? = null,
    val errorMessage: String? = null,
    val showPermissionDialog: Boolean = false,
    val showRationaleDialog: Boolean = false
)

@HiltViewModel
class QRScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScanUiState())
    val uiState: StateFlow<QRScanUiState> = _uiState.asStateFlow()

    /**
     * 스캔 시작
     */
    fun startScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            errorMessage = null
        )
    }

    /**
     * 스캔 중지
     */
    fun stopScanning() {
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    /**
     * QR 코드 스캔 결과 처리
     */
    fun onQRCodeScanned(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scannedCode = code
            )
        }
    }

    /**
     * 에러 발생 처리
     */
    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            errorMessage = message
        )
    }

    /**
     * 권한 거부 다이얼로그 표시
     */
    fun showPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = true)
    }

    /**
     * 권한 설명 다이얼로그 표시
     */
    fun showRationaleDialog() {
        _uiState.value = _uiState.value.copy(showRationaleDialog = true)
    }

    /**
     * 다이얼로그 닫기
     */
    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = false,
            showRationaleDialog = false
        )
    }

    /**
     * 앱 설정 화면으로 이동
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        dismissDialogs()
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 스캔 결과 클리어 (다시 스캔하기)
     */
    fun clearScanResult() {
        _uiState.value = _uiState.value.copy(scannedCode = null)
    }
}