package com.anam145.wallet.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ANAM Wallet 공통 헤더 컴포넌트
 * 
 * anam-android와 동일한 디자인:
 * - 높이: 56dp + 상태바 패딩
 * - 좌우 패딩: 24dp
 * - 타이틀: 24sp, Bold
 * - 블록체인 상태 칩 (옵션)
 * 
 * @param title 표시할 타이틀
 * @param showBlockchainStatus 블록체인 상태 표시 여부
 * @param blockchainConnected 블록체인 연결 상태
 */
@Composable
fun Header(
    title: String,
    showBlockchainStatus: Boolean = false,
    blockchainConnected: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 타이틀
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 블록체인 상태 표시 (옵션)
            AnimatedVisibility(
                visible = showBlockchainStatus && blockchainConnected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BlockchainStatusChip()
            }
        }
    }
}

/**
 * 블록체인 연결 상태를 표시하는 칩
 */
@Composable
private fun BlockchainStatusChip() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 연결 상태 표시 점
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            Text(
                text = "Connected",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}