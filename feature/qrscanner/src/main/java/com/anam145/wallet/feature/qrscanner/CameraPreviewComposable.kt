package com.anam145.wallet.feature.qrscanner

import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

/**
 * CameraX PreviewView를 Compose에서 사용하기 위한 컴포넌트
 *
 * AndroidView를 사용하여 기존 View 시스템의 PreviewView를
 * Compose UI에 통합
 */
@Composable
fun CameraPreviewComposable(
    preview: Preview,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                // PreviewView 설정
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // 스케일 타입 설정 - 화면에 꽉 채우기
                scaleType = PreviewView.ScaleType.FILL_CENTER

                // Preview UseCase 연결
                preview.setSurfaceProvider(surfaceProvider)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { previewView ->
            // Preview가 변경될 때마다 업데이트
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    )
}