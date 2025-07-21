package com.anam145.wallet.feature.qrscanner.utils

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/**
 * 카메라 권한 관련 유틸리티
 */
object PermissionUtils {
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
}

/**
 * 권한 상태를 확인하고 적절한 액션을 반환
 */
@OptIn(ExperimentalPermissionsApi::class)
fun getPermissionAction(permissionState: PermissionState): PermissionAction {
    return when {
        permissionState.status.isGranted -> PermissionAction.Granted
        permissionState.status.shouldShowRationale -> PermissionAction.ShowRationale
        else -> PermissionAction.RequestPermission
    }
}

/**
 * 권한 처리 액션 타입
 */
sealed class PermissionAction {
    object Granted : PermissionAction()
    object RequestPermission : PermissionAction()
    object ShowRationale : PermissionAction()
}

/**
 * 권한 거부 시 설정으로 이동 안내 다이얼로그
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("카메라 권한 필요")
        },
        text = {
            Text(
                "QR 코드를 스캔하려면 카메라 권한이 필요합니다.\n" +
                        "설정에서 권한을 허용해주세요."
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 권한 설명 다이얼로그 (Rationale)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("카메라 권한이 필요한 이유")
        },
        text = {
            Text(
                "QR 코드를 스캔하기 위해 카메라 접근 권한이 필요합니다.\n" +
                        "권한을 허용하시겠습니까?"
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("권한 허용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        }
    )
}