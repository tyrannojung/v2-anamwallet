package com.anam145.wallet.feature.main.ui.skins.default

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.common.constants.SectionOrder
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.feature.main.ui.MainContract
import com.anam145.wallet.feature.main.ui.MainViewModel
import com.anam145.wallet.feature.main.ui.components.ThemeIllustration

/**
 * 기본 스킨 화면
 * 
 * ANAM, SEOUL, LA 등의 스킨에서 사용하는 기본 UI입니다.
 * 색상과 섹션 순서 등만 변경됩니다.
 */
@Composable
fun DefaultSkinScreen(
    uiState: MainContract.MainState,
    viewModel: MainViewModel,
    skin: Skin,
    onNavigateToHub: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 그라디언트 (스킨별로 색상 다름)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getBackgroundGradient(skin))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 상단 일러스트
            ThemeIllustration(
                skin = skin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp)
            )
            
            // 섹션 순서에 따라 표시
            when (uiState.sectionOrder) {
                SectionOrder.BLOCKCHAIN_FIRST -> {
                    BlockchainSection(
                        blockchainApps = uiState.blockchainApps,
                        activeBlockchainId = uiState.activeBlockchainId,
                        viewModel = viewModel,
                        strings = strings
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RegularAppSection(
                        regularApps = uiState.regularApps,
                        viewModel = viewModel,
                        strings = strings,
                        onNavigateToHub = onNavigateToHub
                    )
                }
                SectionOrder.APPS_FIRST -> {
                    RegularAppSection(
                        regularApps = uiState.regularApps,
                        viewModel = viewModel,
                        strings = strings,
                        onNavigateToHub = onNavigateToHub
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlockchainSection(
                        blockchainApps = uiState.blockchainApps,
                        activeBlockchainId = uiState.activeBlockchainId,
                        viewModel = viewModel,
                        strings = strings
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // 하단 여백
        }
    }
}

@Composable
private fun BlockchainSection(
    blockchainApps: List<MiniApp>,
    activeBlockchainId: String?,
    viewModel: MainViewModel,
    strings: com.anam145.wallet.core.ui.language.Strings
) {
    Column {
        Text(
            text = "Blockchain Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (blockchainApps.isEmpty()) {
            EmptyStateCard(
                message = "No blockchain modules"
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockchainApps) { app ->
                    BlockchainCard(
                        miniApp = app,
                        isActive = app.appId == activeBlockchainId,
                        onClick = {
                            viewModel.handleIntent(
                                MainContract.MainIntent.ClickBlockchainApp(app)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegularAppSection(
    regularApps: List<MiniApp>,
    viewModel: MainViewModel,
    strings: com.anam145.wallet.core.ui.language.Strings,
    onNavigateToHub: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mini Apps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNavigateToHub) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Mini App"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (regularApps.isEmpty()) {
            EmptyStateCard(
                message = "No mini apps"
            )
        } else {
            regularApps.forEach { app ->
                RegularAppCard(
                    miniApp = app,
                    onClick = {
                        viewModel.handleIntent(
                            MainContract.MainIntent.ClickRegularApp(app)
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BlockchainCard(
    miniApp: MiniApp,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Add icon loading
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = miniApp.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (isActive) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RegularAppCard(
    miniApp: MiniApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Add icon loading
            Box(
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = miniApp.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getBackgroundGradient(skin: Skin): androidx.compose.ui.graphics.Brush {
    return when (skin) {
        // Skin.SEOUL -> androidx.compose.ui.graphics.Brush.verticalGradient(
        //     colors = listOf(
        //         MaterialTheme.colorScheme.surface,
        //         MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        //     )
        // )
        // Skin.LA -> androidx.compose.ui.graphics.Brush.verticalGradient(
        //     colors = listOf(
        //         MaterialTheme.colorScheme.surface,
        //         MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        //     )
        // )
        else -> androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }
}