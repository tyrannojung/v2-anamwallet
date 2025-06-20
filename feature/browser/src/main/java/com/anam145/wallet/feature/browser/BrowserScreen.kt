package com.anam145.wallet.feature.browser

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
 * 브라우저 화면
 * 
 * ANAM Wallet 내장 브라우저 화면입니다.
 * 미니앱 및 웹 콘텐츠를 표시합니다.
 */
@Composable
fun BrowserScreen(
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
                text = strings.browserScreenTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}