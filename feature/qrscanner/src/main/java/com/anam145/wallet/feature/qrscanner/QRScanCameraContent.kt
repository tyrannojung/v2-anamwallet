package com.anam145.wallet.feature.qrscanner

import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

/**
 * QR 스캔을 위한 카메라 통합 컴포넌트
 *
 * CameraX 설정, 카메라 프리뷰, ML Kit 바코드 스캔,
 * 그리고 스캔 오버레이 UI를 모두 통합한 컴포넌트
 */
@Composable
fun QRScanCameraContent(
    viewModel: QRScanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 카메라 상태
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    // 카메라 실행자 (백그라운드 스레드)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 카메라 초기화
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        // Preview UseCase 설정
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        // ImageAnalysis UseCase 설정 (QR 스캔용)
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                    // QR 코드 스캔 결과를 ViewModel에 전달
                    viewModel.onQRCodeScanned(qrCode)
                })
            }

        // 카메라 설정
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // 기존 바인딩 해제
            cameraProvider.unbindAll()

            // 새로운 UseCase들 바인딩
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

        } catch (exception: Exception) {
            viewModel.onError("카메라 초기화 실패: ${exception.message}")
        }

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()



            }, ContextCompat.getMainExecutor(context))
        } catch (exception: Exception) {
            viewModel.onError("카메라 초기화 실패: ${exception.message}")
        }
    }

    // 컴포넌트 해제 시 정리
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        // 1. 카메라 프리뷰 (배경)
        preview?.let { previewUseCase ->
            CameraPreviewComposable(
                preview = previewUseCase,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. 스캔 오버레이 UI (전경)
        QRScanOverlay(
            isScanning = true,                      // 또는 viewModel 상태
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ML Kit을 사용한 QR 코드 분석기
 *
 * ImageAnalysis.Analyzer를 구현하여 카메라 프레임을
 * 실시간으로 분석하고 QR 코드를 감지합니다.
 */
private class QRCodeAnalyzer(
    private val onQRCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            // ML Kit InputImage 생성
            val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            // 바코드 스캔 수행
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // 첫 번째 QR 코드만 처리
                    barcodes.firstOrNull()?.rawValue?.let { qrCode ->
                        onQRCodeDetected(qrCode)
                    }
                }
                .addOnFailureListener { exception ->
                    // 스캔 실패 처리 (일반적으로 무시)
                }
                .addOnCompleteListener {
                    // ImageProxy 정리 (메모리 누수 방지)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}