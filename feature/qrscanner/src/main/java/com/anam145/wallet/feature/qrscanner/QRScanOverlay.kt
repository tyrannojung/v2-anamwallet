package com.anam145.wallet.feature.qrscanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QR 코드 스캔을 위한 오버레이 UI
 *
 * 카메라 프리뷰 위에 표시되는 스캔 가이드와
 * 사용자 인터페이스 요소들을 포함
 */
@Composable
fun QRScanOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. 반투명 배경 + 스캔 영역 투명 처리
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawScanningOverlay(
                canvasSize = size,
                density = density
            )
        }

        // 2. 상단 안내 텍스트
        TopInstructions(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        // 3. 스캔 가이드 (중앙)
        ScanGuideFrame(
            isScanning = isScanning,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * 스캔 오버레이 배경 그리기
 * 전체 화면을 반투명으로 덮고 중앙 스캔 영역만 투명하게 처리
 */
private fun DrawScope.drawScanningOverlay(
    canvasSize: Size,
    density: androidx.compose.ui.unit.Density
) {
    val scanAreaSize = with(density) { 250.dp.toPx() }
    val cornerRadius = with(density) { 16.dp.toPx() }

    // 스캔 영역 계산
    val scanAreaLeft = (canvasSize.width - scanAreaSize) / 2
    val scanAreaTop = (canvasSize.height - scanAreaSize) / 2

    // 전체 화면을 반투명 검은색으로 덮기
    drawRect(
        color = Color.Black.copy(alpha = 0.6f),
        size = canvasSize
    )

    // 스캔 영역을 투명하게 만들기 (BlendMode 사용)
    drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(scanAreaLeft, scanAreaTop),
        size = Size(scanAreaSize, scanAreaSize),
        cornerRadius = CornerRadius(cornerRadius),
        blendMode = BlendMode.Clear
    )
}

/**
 * 상단 안내 텍스트
 */
@Composable
private fun TopInstructions(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = "QR 코드를 스캔 영역에 맞춰주세요",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 중앙 스캔 가이드 프레임
 */
@Composable
private fun ScanGuideFrame(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // 스캔 프레임 테두리
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawScanFrame(
                canvasSize = size,
                isScanning = isScanning
            )
        }

        // 스캔 상태 표시
        if (isScanning) {
            ScanningAnimation()
        }
    }
}

/**
 * 스캔 프레임 그리기
 */
private fun DrawScope.drawScanFrame(
    canvasSize: Size,
    isScanning: Boolean
) {
    val frameColor = if (isScanning) Color.Green else Color.White
    val strokeWidth = 4.dp.toPx()
    val cornerLength = 30.dp.toPx()

    // 네 모서리에 L자 형태의 가이드 그리기
    val corners = listOf(
        // 좌상단
        Pair(Offset(0f, 0f), Offset(cornerLength, 0f)),
        Pair(Offset(0f, 0f), Offset(0f, cornerLength)),

        // 우상단
        Pair(Offset(canvasSize.width - cornerLength, 0f), Offset(canvasSize.width, 0f)),
        Pair(Offset(canvasSize.width, 0f), Offset(canvasSize.width, cornerLength)),

        // 좌하단
        Pair(Offset(0f, canvasSize.height - cornerLength), Offset(0f, canvasSize.height)),
        Pair(Offset(0f, canvasSize.height), Offset(cornerLength, canvasSize.height)),

        // 우하단
        Pair(Offset(canvasSize.width, canvasSize.height - cornerLength), Offset(canvasSize.width, canvasSize.height)),
        Pair(Offset(canvasSize.width - cornerLength, canvasSize.height), Offset(canvasSize.width, canvasSize.height))
    )

    corners.forEach { (start, end) ->
        drawLine(
            color = frameColor,
            start = start,
            end = end,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 스캔 중 애니메이션
 */
@Composable
private fun ScanningAnimation() {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            // 위에서 아래로 스캔 라인 이동 애니메이션
            androidx.compose.animation.core.animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(2000),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                )
            ) { value, _ ->
                animationProgress = value
            }
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val lineY = size.height * animationProgress
        drawLine(
            color = Color.Green,
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 3.dp.toPx()
        )
    }
}