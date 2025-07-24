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
import com.anam145.wallet.feature.main.ui.components.ThemeIllustration
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
    onLaunchBlockchain: (String) -> Unit = {}
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
                Box(modifier = Modifier.fillMaxSize()) {
                    MiniAppList(
                        blockchainApps = uiState.blockchainApps,
                        regularApps = uiState.regularApps,
                        activeBlockchainId = uiState.activeBlockchainId,
                        onBlockchainClick = { viewModel.handleIntent(MainContract.MainIntent.ClickBlockchainApp(it)) },
                        onAppClick = { viewModel.handleIntent(MainContract.MainIntent.ClickRegularApp(it)) }
                    )
                    
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

@Composable
private fun MiniAppList(
    blockchainApps: List<MiniApp>,
    regularApps: List<MiniApp>,
    activeBlockchainId: String?,
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
        // Blockchain Section
        if (blockchainApps.isNotEmpty()) {
            Text(
                text = strings.mainSectionBlockchain,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
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
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Apps Section
        Text(
            text = strings.mainSectionApps,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        if (regularApps.isEmpty()) {
            // 앱이 없을 때 메시지 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
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
    val borderColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .scale(scale)
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
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

