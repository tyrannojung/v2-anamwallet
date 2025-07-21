package com.anam145.wallet.feature.qrscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * QR 스캐너 화면
 * 
 * QR 코드를 스캔하여 다양한 기능을 수행하는 화면입니다.
 * 지갑 주소, 트랜잭션 정보, DID 연결 등을 처리합니다.
 */
@Composable
fun QrScannerScreen(
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = strings.qrScannerScreenTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}