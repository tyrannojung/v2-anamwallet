package com.anam145.wallet.feature.qrscanner

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.feature.qrscanner.utils.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * QR 스캐너 화면
 * 
 * QR 코드를 스캔하여 다양한 기능을 수행하는 화면.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    modifier: Modifier = Modifier
) {
    // 내부에서 NavController 가져오기
    val navController = rememberNavController()

    // 스캔 결과 처리 로직
    val handleQRScanned: (String) -> Unit = { code ->
        // TODO: 스캔된 QR 코드 처리 로직
        // 예: 송금 화면으로 이동하거나 결과를 상위로 전달
        println("QR 코드 스캔됨: $code")

        // 뒤로가기 구현 필요
    }

    QRScanScreenContent(
        onNavigateBack = { /* 뒤로가기 구현 필요 */ },
        onQRScanned = handleQRScanned,
        modifier = modifier
    )
}

/**
 * QR 스캔 화면 실제 내용
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QRScanScreenContent(
    onNavigateBack: () -> Unit,
    onQRScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QRScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 카메라 권한 상태
    val cameraPermissionState = rememberPermissionState(PermissionUtils.CAMERA_PERMISSION)

    // 권한 요청 결과 처리
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startScanning()
        } else {
            viewModel.showPermissionDialog()
        }
    }

    // 권한 상태에 따른 처리
    LaunchedEffect(cameraPermissionState.status) {
        when (getPermissionAction(cameraPermissionState)) {
            PermissionAction.Granted -> {
                viewModel.startScanning()
            }
            PermissionAction.RequestPermission -> {
                permissionLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
            }
            PermissionAction.ShowRationale -> {
                viewModel.showRationaleDialog()
            }
        }
    }

    // 스캔 결과 처리
    LaunchedEffect(uiState.scannedCode) {
        uiState.scannedCode?.let { code ->
            onQRScanned(code)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR 코드 스캔") }
                // 뒤로가기 버튼 제거 - 뒤로가기 구현 필요
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 권한이 허용되고 스캔 중일 때
                cameraPermissionState.status.isGranted && uiState.isScanning -> {
                    QRScanCameraContent(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 에러 발생 시
                uiState.errorMessage != null -> {
                    val errorMsg = uiState.errorMessage // 스마트 캐스트를 위한 지역 변수
                    ErrorContent(
                        message = errorMsg ?: "알 수 없는 오류가 발생했습니다",
                        onRetry = {
                            viewModel.clearError()
                            viewModel.startScanning()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 로딩 상태
                else -> {
                    LoadingContent(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // 권한 관련 다이얼로그들
    if (uiState.showRationaleDialog) {
        PermissionRationaleDialog(
            onDismiss = viewModel::dismissDialogs,
            onRequestPermission = {
                viewModel.dismissDialogs()
                permissionLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
            }
        )
    }

    if (uiState.showPermissionDialog) {
        PermissionDeniedDialog(
            onDismiss = {
                viewModel.dismissDialogs()
                // 뒤로가기 구현 필요
            },
            onGoToSettings = viewModel::openAppSettings
        )
    }
}

/**
 * 에러 상태 UI
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

/**
 * 로딩 상태 UI
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "카메라 준비 중...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}