package com.anam145.wallet.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * ANAM Wallet 공통 헤더 컴포넌트
 * 
 * anam-android와 동일한 디자인:
 * - 높이: 56dp + 상태바 패딩 + 블록체인 칩
 * - 좌우 패딩: 24dp
 * - 타이틀: 24sp, Bold
 * - 블록체인 상태 칩: 타이틀 아래 표시
 * 
 * @param title 표시할 타이틀
 * @param showBackButton 뒤로가기 버튼 표시 여부
 * @param onBackClick 뒤로가기 버튼 클릭 콜백
 * @param showBlockchainStatus 블록체인 상태 표시 여부
 * @param activeBlockchainName 활성 블록체인 이름
 * @param onBlockchainClick 블록체인 칩 클릭 콜백
 */
@Composable
fun Header(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    showBlockchainStatus: Boolean = false,
    activeBlockchainName: String? = null,
    onBlockchainClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            // 타이틀과 뒤로가기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // 뒤로가기 버튼
                if (showBackButton && onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
                
                // 타이틀
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = if (showBackButton) 48.dp else 0.dp)
                )
            }
            
            // 블록체인 상태 표시 (옵션)
            AnimatedVisibility(
                visible = showBlockchainStatus && !activeBlockchainName.isNullOrEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    BlockchainStatusChip(
                        blockchainName = activeBlockchainName ?: "",
                        onClick = onBlockchainClick
                    )
                }
            }
        }
    }
}

/**
 * 블록체인 활성화 상태를 표시하는 칩
 * 
 * @param blockchainName 활성 블록체인 이름
 * @param onClick 클릭 콜백
 */
@Composable
private fun BlockchainStatusChip(
    blockchainName: String,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .height(32.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val strings = LocalStrings.current
            Text(
                text = "🔗 $blockchainName ${strings.activated}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}