package com.anam145.wallet.feature.hub

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
 * 허브 화면
 * 
 * 미니앱 허브 화면입니다.
 * 사용 가능한 모든 미니앱 목록을 표시합니다.
 */
@Composable
fun HubScreen(
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
                text = strings.hubScreenTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}