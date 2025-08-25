package com.anam145.wallet.feature.main.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 부산 월렛 커스텀 화면 V3
 * - 디자인 개선 버전
 * - LazyColumn과 LazyVerticalGrid 적용
 * - Material3 컴포넌트 활용
 * - 색상 토큰화
 */

// 부산 월렛 색상 토큰
@Stable
data class BusanTokens(
    val blue: Color = Color(0xFF0F539E),
    val black: Color = Color(0xFF0E0E0E),
    val gray: Color = Color(0xFF8F9295),
    val lightBlue: Color = Color(0xFFE6F4FF),
    val white: Color = Color(0xFFFAFCFF),
    val green: Color = Color(0xFF4CAF50)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BusanCustomScreenV3() {
    val listState = rememberLazyListState()
    val tokens = remember { BusanTokens() }
    
    // 애니메이션 트리거
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        tokens.white,
                        tokens.lightBlue.copy(alpha = 0.15f)
                    )
                )
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 활성 자산 카드
            item(key = "active_asset") {
                ActiveDigitalAssetCard(
                    tokens = tokens,
                    isVisible = isVisible
                )
            }
            
            // 블록체인 지갑 섹션
            item(key = "blockchain_header") {
                SectionHeader(
                    title = "블록체인 지갑",
                    subtitle = "탭하여 전환",
                    tokens = tokens
                )
            }
            
            // 비활성 블록체인 카드
            item(key = "blockchain_cards") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InactiveAssetCard(
                        modifier = Modifier.weight(1f),
                        assetName = "Bitcoin",
                        appId = "com.anam.bitcoin",
                        tokens = tokens
                    )
                    // 추가 블록체인 카드를 위한 빈 공간
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // 시민 서비스 섹션
            item(key = "service_header") {
                SectionHeader(
                    title = "부산 서비스",
                    tokens = tokens
                )
            }
            
            // 시민 서비스 그리드
            item(key = "service_grid") {
                CitizenServiceGrid(tokens = tokens)
            }
            
            // 브랜드 워터마크
            item(key = "watermark") {
                BrandWatermark(
                    text = "BUSAN WALLET",
                    tokens = tokens
                )
            }
        }
    }
}

@Composable
private fun ActiveDigitalAssetCard(
    tokens: BusanTokens,
    isVisible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp // 8dp에서 4dp로 감소
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            tokens.blue.copy(alpha = 0.08f),
                            tokens.blue.copy(alpha = 0.03f)
                        )
                    )
                )
        ) {
            // 배경 패턴
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopEnd)
            ) {
                drawCircle(
                    color = tokens.blue.copy(alpha = 0.05f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width - 50.dp.toPx(), 50.dp.toPx())
                )
                drawCircle(
                    color = tokens.blue.copy(alpha = 0.03f),
                    radius = 150.dp.toPx(),
                    center = Offset(size.width - 30.dp.toPx(), 80.dp.toPx())
                )
            }
            
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 상단: 자산 정보와 활성화 칩
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 이더리움 아이콘
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                tokens.blue.copy(alpha = 0.2f),
                                                tokens.blue.copy(alpha = 0.1f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ξ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.blue.copy(alpha = 0.7f)
                                )
                            }
                            
                            Column {
                                Text(
                                    "Ethereum",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.black
                                )
                                Text(
                                    "com.anam.ethereum",
                                    fontSize = 12.sp,
                                    color = tokens.gray,
                                    letterSpacing = 0.02.em
                                )
                            }
                        }
                    }
                    
                    // Material3 AssistChip으로 변경
                    AssistChip(
                        onClick = { /* 상세 화면으로 이동 */ },
                        label = { 
                            Text(
                                "Activated",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "활성화됨",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = tokens.green.copy(alpha = 0.08f),
                            labelColor = tokens.green,
                            leadingIconContentColor = tokens.green
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = tokens.green.copy(alpha = 0.3f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 잔액 정보
                Column {
                    Text(
                        "잔액",
                        fontSize = 12.sp,
                        color = tokens.gray,
                        letterSpacing = 0.04.em
                    )
                    Text(
                        "0.1234 ETH",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.black,
                        letterSpacing = 0.02.em
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 상세보기 버튼
                Button(
                    onClick = { /* 상세 화면으로 이동 */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.blue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "상세보기",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.04.em
                    )
                }
            }
        }
    }
}

@Composable
private fun InactiveAssetCard(
    modifier: Modifier = Modifier,
    assetName: String,
    appId: String,
    tokens: BusanTokens
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            tokens.gray.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "B",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.gray
                    )
                }
                
                Text(
                    assetName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.black
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tokens.gray.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CitizenServiceGrid(tokens: BusanTokens) {
    val services = remember {
        listOf(
            BusanCitizenService("부산일보", "🗞️"),
            BusanCitizenService("본미디어", "📺")
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(
            items = services,
            key = { it.name }
        ) { service ->
            CitizenServiceCard(
                service = service,
                tokens = tokens,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
private fun CitizenServiceCard(
    service: BusanCitizenService,
    tokens: BusanTokens,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "service_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { /* 서비스 실행 */ }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(1.dp, tokens.gray.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    service.emoji,
                    fontSize = 20.sp
                )
                Text(
                    service.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.black
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "${service.name} 열기",
                modifier = Modifier.size(16.dp),
                tint = tokens.gray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    tokens: BusanTokens
) {
    Column {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.black,
            letterSpacing = 0.02.em
        )
        if (subtitle != null) {
            Text(
                subtitle,
                fontSize = 12.sp,
                color = tokens.gray,
                letterSpacing = 0.04.em,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun BrandWatermark(
    text: String,
    tokens: BusanTokens
) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = tokens.blue.copy(alpha = 0.12f),
        letterSpacing = 0.18.em,
        textAlign = TextAlign.Center
    )
}

// 데이터 클래스
private data class BusanCitizenService(
    val name: String,
    val emoji: String
)