package com.anam145.wallet.feature.main

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
 * 메인 화면
 * 
 * ANAM Wallet의 홈 대시보드 화면입니다.
 * 학생증, 서비스 카드, 최근 사용 미니앱 등을 표시합니다.
 */
@Composable
fun MainScreen(
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
                text = strings.mainScreenTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}