package com.anam145.wallet.feature.miniapp.qrscanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * QR 코드 스캐너 Activity
 */
class QRScannerActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "QRScannerActivity"
    }
    
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true
    
    // 카메라 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 승인됨
            Log.d(TAG, "Camera permission granted")
        } else {
            // 권한 거부됨
            Log.e(TAG, "Camera permission denied")
            MainBridgeService.handleQRScanResult(false, "Camera permission is required")
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // 옵션 파싱
        val optionsJson = intent.getStringExtra("options") ?: "{}"
        val options = try {
            JSONObject(optionsJson)
        } catch (e: Exception) {
            JSONObject()
        }
        
        val title = options.optString("title", "QR 코드 스캔")
        val description = options.optString("description", "QR 코드를 스캔하세요")
        
        // 카메라 권한 확인
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 있음
                setContent {
                    AnamWalletTheme {
                        QRScannerScreen(
                            title = title,
                            description = description,
                            onQRCodeScanned = { qrData ->
                                if (isScanning) {
                                    isScanning = false
                                    Log.d(TAG, "QR code scanned: $qrData")
                                    MainBridgeService.handleQRScanResult(true, qrData)
                                    finish()
                                }
                            },
                            onCancel = {
                                MainBridgeService.handleQRScanResult(false, "QR scan cancelled by user")
                                finish()
                            }
                        )
                    }
                }
            }
            else -> {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    title: String,
    description: String,
    onQRCodeScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var camera: Camera? by remember { mutableStateOf(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // 카메라 프리뷰
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Preview
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Image Analysis
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                processImageProxy(imageProxy, onQRCodeScanned)
                            }
                        }
                    
                    // Select back camera
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll()
                        
                        // Bind use cases to camera
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                        
                    } catch (exc: Exception) {
                        Log.e("QRScanner", "Use case binding failed", exc)
                        Toast.makeText(
                            context,
                            "카메라를 시작할 수 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                }, ContextCompat.getMainExecutor(context))
            }
            
            // 오버레이 UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                
                // 스캔 영역 표시
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Transparent)
                ) {
                    // 스캔 영역 가이드라인
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 설명 텍스트
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 취소 버튼
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("취소")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        val scanner = BarcodeScanning.getClient()
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_TEXT,
                        Barcode.TYPE_URL,
                        Barcode.TYPE_UNKNOWN -> {
                            barcode.rawValue?.let { value ->
                                onQRCodeScanned(value)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QRScanner", "Barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}