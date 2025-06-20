package com.anam145.wallet.feature.identity

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
 * 신원 화면
 * 
 * 사용자 신원 관리 화면입니다.
 * DID, VC, 학생증 정보 등을 관리합니다.
 */
@Composable
fun IdentityScreen(
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
                text = strings.identityScreenTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}