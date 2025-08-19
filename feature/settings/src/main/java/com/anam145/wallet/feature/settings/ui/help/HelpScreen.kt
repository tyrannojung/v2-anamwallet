package com.anam145.wallet.feature.settings.ui.help

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * 도움말 화면
 * 
 * 앱 사용법과 주요 기능에 대한 설명을 제공합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackClick: () -> Unit
) {
    val strings = LocalStrings.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = strings.helpTitle,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 비밀번호 설정
            HelpItem(
                icon = Icons.Outlined.Lock,
                title = strings.helpPasswordTitle,
                content = strings.helpPasswordContent
            )
            
            // 미니앱 설치 및 관리
            HelpItem(
                icon = Icons.Outlined.Apps,
                title = strings.helpMiniAppTitle,
                content = strings.helpMiniAppContent
            )
            
            // 블록체인 지갑 사용
            HelpItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = strings.helpBlockchainTitle,
                content = strings.helpBlockchainContent
            )
            
            // 웹 브라우저
            HelpItem(
                icon = Icons.Outlined.Language,
                title = strings.helpBrowserTitle,
                content = strings.helpBrowserContent
            )
            
            // 언어 및 스킨 설정
            HelpItem(
                icon = Icons.Outlined.Settings,
                title = strings.helpLanguageTitle,
                content = strings.helpLanguageContent
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 도움말 항목 컴포넌트
 * 
 * 확장/축소 가능한 카드 형태로 도움말 내용을 표시합니다.
 */
@Composable
private fun HelpItem(
    icon: ImageVector,
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // ripple effect 제거
                ) { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 제목
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 확장/축소 아이콘
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 내용 (확장 시에만 표시)
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                    )
                }
            }
        }
    }
}