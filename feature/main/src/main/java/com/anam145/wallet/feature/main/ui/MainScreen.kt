package com.anam145.wallet.feature.main.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.anam145.wallet.core.ui.theme.ShapeCard
import com.anam145.wallet.core.ui.theme.ShapeNormal
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.language.Strings
import com.anam145.wallet.core.common.constants.SectionOrder
import com.anam145.wallet.feature.main.ui.components.ThemeIllustration
import com.anam145.wallet.feature.main.ui.skins.BusanScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 메인 화면
 * 
 * ANAM Wallet의 홈 대시보드 화면.
 * 블록체인 모듈과 앱 모듈을 표시.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToMiniApp: (String) -> Unit = {},
    onLaunchBlockchain: (String) -> Unit = {},
    onNavigateToHub: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    
    // Hub에서 돌아올 때만 새로고침하도록 제거
    // 캐시가 이미 install/uninstall 시 clearCache()로 처리됨

    // key = 이 작업을 다시 실행할 조건
    LaunchedEffect(key1 = viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MainContract.MainEffect.LaunchWebAppActivity -> onNavigateToMiniApp(effect.appId)
                is MainContract.MainEffect.LaunchBlockchainActivity -> onLaunchBlockchain(effect.blockchainId)
                is MainContract.MainEffect.ShowError -> {
                    // TODO: Show error message (e.g., using SnackBar)
                    android.util.Log.e("MainScreen", "Error: ${effect.message}")
                }
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.isSyncing -> {
                // 초기화 동기화 중 표시
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.syncingApps,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                // 스킨별 커스텀 화면 분기
                when (uiState.currentSkin) {
                    Skin.BUSAN -> {
                        BusanScreen(
                            blockchainApps = uiState.blockchainApps,
                            regularApps = uiState.regularApps,
                            activeBlockchainId = uiState.activeBlockchainId,
                            onBlockchainClick = { miniApp ->
                                viewModel.handleIntent(
                                    MainContract.MainIntent.SwitchBlockchain(miniApp)
                                )
                            },
                            onRegularAppClick = { miniApp ->
                                viewModel.handleIntent(
                                    MainContract.MainIntent.ClickRegularApp(miniApp)
                                )
                            },
                            onViewBlockchainDetail = { miniApp ->
                                viewModel.handleIntent(
                                    MainContract.MainIntent.ClickBlockchainApp(miniApp)
                                )
                            },
                            onNavigateToHub = onNavigateToHub
                        )
                    }
                    // 추후 다른 커스텀 스킨 추가 시
                    // Skin.SEOUL -> SeoulScreen(...)
                    // Skin.JEJU -> JejuScreen(...)
                    
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 두 리스트가 모두 비어있을 때 전체 빈 상태 표시
                            if (uiState.blockchainApps.isEmpty() && uiState.regularApps.isEmpty()) {
                                EmptyStateView(
                                    strings = strings,
                                    currentSkin = uiState.currentSkin,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                MiniAppList(
                                    blockchainApps = uiState.blockchainApps,
                                    regularApps = uiState.regularApps,
                                    activeBlockchainId = uiState.activeBlockchainId,
                                    sectionOrder = uiState.sectionOrder,
                                    onBlockchainClick = { viewModel.handleIntent(MainContract.MainIntent.ClickBlockchainApp(it)) },
                                    onAppClick = { viewModel.handleIntent(MainContract.MainIntent.ClickRegularApp(it)) }
                                )
                            }
                            
                            // 테마별 일러스트레이션 표시
                            ThemeIllustration(
                                skin = uiState.currentSkin,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniAppList(
    blockchainApps: List<MiniApp>,
    regularApps: List<MiniApp>,
    activeBlockchainId: String?,
    sectionOrder: SectionOrder,
    onBlockchainClick: (MiniApp) -> Unit,
    onAppClick: (MiniApp) -> Unit
) {
    val strings = LocalStrings.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Space for bottom navigation
    ) {
        when (sectionOrder) {
            SectionOrder.BLOCKCHAIN_FIRST -> {
                // 블록체인이 먼저 (기본)
                BlockchainSection(
                    blockchainApps = blockchainApps,
                    activeBlockchainId = activeBlockchainId,
                    onBlockchainClick = onBlockchainClick,
                    strings = strings
                )
                
                if (blockchainApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                AppsSection(
                    regularApps = regularApps,
                    onAppClick = onAppClick,
                    strings = strings
                )
            }
            SectionOrder.APPS_FIRST -> {
                // 앱이 먼저 (부산)
                AppsSection(
                    regularApps = regularApps,
                    onAppClick = onAppClick,
                    strings = strings
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                BlockchainSection(
                    blockchainApps = blockchainApps,
                    activeBlockchainId = activeBlockchainId,
                    onBlockchainClick = onBlockchainClick,
                    strings = strings
                )
            }
        }
    }
}

@Composable
private fun BlockchainSection(
    blockchainApps: List<MiniApp>,
    activeBlockchainId: String?,
    onBlockchainClick: (MiniApp) -> Unit,
    strings: Strings
) {
    Text(
        text = strings.mainSectionBlockchain,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
    
    if (blockchainApps.isEmpty()) {
        // 블록체인이 없을 때 메시지 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),  // BlockchainCard와 동일한 높이
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.mainNoBlockchainsInstalled,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(blockchainApps) { app ->
                BlockchainCard(
                    miniApp = app,
                    isActive = app.appId == activeBlockchainId,
                    onClick = { onBlockchainClick(app) }
                )
            }
        }
    }
}

@Composable
private fun AppsSection(
    regularApps: List<MiniApp>,
    onAppClick: (MiniApp) -> Unit,
    strings: Strings
) {
    Text(
        text = strings.mainSectionApps,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
    
    if (regularApps.isEmpty()) {
        // 앱이 없을 때 메시지 표시 - 이미지 위치를 고려한 높이
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),  // 이미지가 표시되는 영역의 중간 높이
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.mainNoAppsInstalled,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            regularApps.chunked(3).forEach { rowApps ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowApps.forEach { app ->
                        AppCard(
                            miniApp = app,
                            onClick = { onAppClick(app) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Fill empty spaces in the row
                    repeat(3 - rowApps.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring()
    )
    
    // 그라데이션 브러시 (대각선)
    val gradientBrush = if (isActive) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else null
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .scale(scale)
            .then(
                if (isActive && gradientBrush != null) {
                    Modifier.border(
                        width = 2.dp,
                        brush = gradientBrush,
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = ShapeCard,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Active 뱃지 (우측 상단)
            if (isActive) {
                val strings = LocalStrings.current
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                        Text(
                            text = strings.active,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 아이콘과 이름 (좌측 상단)
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                // 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = ShapeNormal
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MiniAppIcon(
                        miniApp = miniApp,
                        size = 48.dp,
                        iconSize = 32.dp,
                        showBackground = false
                    )
                }
                
                // 이름 (아이콘 바로 아래)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = miniApp.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AppCard(
    miniApp: MiniApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring()
    )
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = ShapeCard,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MiniAppIcon(
                miniApp = miniApp,
                size = 48.dp,
                iconSize = 32.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = miniApp.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniAppIcon(
    miniApp: MiniApp,
    size: Dp,
    iconSize: Dp,
    showBackground: Boolean = true
) {
    var iconBitmap by remember { mutableStateOf<BitmapPainter?>(null) }
    
    LaunchedEffect(miniApp.iconPath) {
        miniApp.iconPath?.let { path ->
            withContext(Dispatchers.IO) {
                try {
                    val file = File(path)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(path)
                        iconBitmap = BitmapPainter(bitmap.asImageBitmap())
                    }
                } catch (e: Exception) {
                    // Ignore icon loading errors
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = ShapeNormal
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        iconBitmap?.let { painter ->
            Image(
                painter = painter,
                contentDescription = miniApp.name,
                modifier = Modifier.size(iconSize)
            )
        } ?: Icon(
            imageVector = when (miniApp.type) {
                MiniAppType.BLOCKCHAIN -> Icons.Default.Link
                MiniAppType.APP -> Icons.Default.Dashboard
            },
            contentDescription = miniApp.name,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateView(
    strings: Strings,
    currentSkin: Skin,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 지갑 아이콘
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 제목 (스킨에 따라 다른 메시지)
        Text(
            text = when (currentSkin) {
                Skin.BUSAN -> strings.mainEmptyStateTitleBusan
                else -> strings.mainEmptyStateTitle
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 설명
        Text(
            text = strings.mainEmptyStateDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        // 하단 여백 (bottom navigation 고려)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

