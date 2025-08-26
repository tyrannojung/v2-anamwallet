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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 * 부산 월렛 커스텀 화면 V5
 * - V4 기반 + 개선 사항
 * - 셀렉터 설명 추가 (활성 블록체인 전환)
 * - 부산 시그니처 색상 추가
 * - 일러스트 추가
 */

// 부산 월렛 색상 토큰 V5
@Stable
data class BusanTokensV5(
    // 부산 시그니처 색상
    val busanBlue: Color = Color(0xFF0F2A48),    // 진한 남색
    val busanSkyBlue: Color = Color(0xFF5AABDB), // 하늘색
    val busanLightBlue: Color = Color(0xFFACCDEC), // 연한 하늘색
    val busanPaleBlue: Color = Color(0xFFAECDEC), // 연한 민트색
    
    // 기존 색상
    val blue: Color = Color(0xFF0F539E),
    val black: Color = Color(0xFF0E0E0E),
    val gray: Color = Color(0xFF8F9295),
    val lightBlue: Color = Color(0xFFE6F4FF),
    val white: Color = Color(0xFFFAFCFF),
    val green: Color = Color(0xFF4CAF50),
    val bitcoin: Color = Color(0xFFF7931A),
    val ethereum: Color = Color(0xFF627EEA)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BusanCustomScreenV5() {
    val listState = rememberLazyListState()
    val tokens = remember { BusanTokensV5() }
    
    // 선택된 블록체인 상태
    var selectedBlockchain by remember { mutableStateOf("Bitcoin") }
    
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
                        tokens.busanPaleBlue.copy(alpha = 0.2f)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 블록체인 선택 셀렉터
            item(key = "blockchain_selector") {
                BlockchainSelector(
                    selectedBlockchain = selectedBlockchain,
                    onBlockchainSelected = { selectedBlockchain = it },
                    tokens = tokens,
                    isVisible = isVisible
                )
            }
            
            // 활성 자산 카드
            item(key = "active_asset") {
                ActiveDigitalAssetCard(
                    tokens = tokens,
                    isVisible = isVisible
                )
            }
            
            // 시민 서비스 섹션
            item(key = "service_header") {
                SectionHeader(
                    title = "부산 서비스",
                    tokens = tokens
                )
            }
            
            // 시민 서비스 그리드와 다운로드 버튼
            item(key = "service_section") {
                Column {
                    CitizenServiceGrid(tokens = tokens)
                    
                    // 더 많은 모듈 다운로드 버튼
                    OutlinedButton(
                        onClick = { /* Hub로 이동 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = tokens.busanBlue
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = tokens.busanLightBlue.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "더 많은 모듈 다운받기",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.02.em
                        )
                    }
                }
            }
        }
    }
}

// 블록체인 선택 셀렉터 (드롭다운)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockchainSelector(
    selectedBlockchain: String,
    onBlockchainSelected: (String) -> Unit,
    tokens: BusanTokensV5,
    isVisible: Boolean
) {
    val blockchains = listOf("Bitcoin", "Ethereum", "Solana")
    var expanded by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(),
        label = "selector_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        // 셀렉터 위에 설명 추가
        Text(
            "활성 블록체인 전환",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = tokens.busanBlue.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedBlockchain,
                onValueChange = { },
                readOnly = true,
                label = { Text("블록체인 선택", fontSize = 14.sp) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "드롭다운",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = tokens.busanSkyBlue,
                    unfocusedBorderColor = tokens.busanLightBlue.copy(alpha = 0.5f),
                    focusedLabelColor = tokens.busanBlue,
                    unfocusedLabelColor = tokens.gray
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                blockchains.forEach { blockchain ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 블록체인 아이콘
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = when (blockchain) {
                                                "Bitcoin" -> tokens.bitcoin.copy(alpha = 0.15f)
                                                "Ethereum" -> tokens.ethereum.copy(alpha = 0.15f)
                                                else -> tokens.blue.copy(alpha = 0.15f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (blockchain) {
                                            "Bitcoin" -> "₿"
                                            "Ethereum" -> "Ξ"
                                            else -> "S"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (blockchain) {
                                            "Bitcoin" -> tokens.bitcoin
                                            "Ethereum" -> tokens.ethereum
                                            else -> tokens.blue
                                        }
                                    )
                                }
                                Text(
                                    blockchain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {
                            onBlockchainSelected(blockchain)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // 셀렉터 아래에 보조 설명 추가
        Text(
            "현재 활성화된 블록체인을 변경합니다",
            fontSize = 11.sp,
            color = tokens.gray.copy(alpha = 0.7f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
        )
    }
}


@Composable
private fun ActiveDigitalAssetCard(
    tokens: BusanTokensV5,
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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            tokens.busanSkyBlue.copy(alpha = 0.12f),
                            tokens.busanLightBlue.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
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
                    color = tokens.busanSkyBlue.copy(alpha = 0.08f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width - 50.dp.toPx(), 50.dp.toPx())
                )
                drawCircle(
                    color = tokens.busanLightBlue.copy(alpha = 0.05f),
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
                            // 비트코인 아이콘
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                tokens.bitcoin.copy(alpha = 0.2f),
                                                tokens.bitcoin.copy(alpha = 0.1f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "₿",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.bitcoin.copy(alpha = 0.8f)
                                )
                            }
                            
                            Column {
                                Text(
                                    "Bitcoin",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.black
                                )
                                Text(
                                    "com.anam.bitcoin",
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
                                "활성화됨",
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
                        "0.0024 BTC",
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
                        containerColor = tokens.busanBlue
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
    tokens: BusanTokensV5
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
private fun CitizenServiceGrid(tokens: BusanTokensV5) {
    val services = remember {
        listOf(
            BusanCitizenServiceV5("부산일보", "🗞️"),
            BusanCitizenServiceV5("본미디어", "📺")
        )
    }
    
    // 서비스 개수에 따라 높이 동적 계산
    // 각 카드 높이(약 70dp) + spacing(12dp)
    val gridHeight = remember(services.size) {
        val rows = (services.size + 1) / 2  // 2열이므로 올림 처리
        (rows * 70 + (rows - 1) * 12).dp
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),  // 계산된 높이 사용
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
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun CitizenServiceCard(
    service: BusanCitizenServiceV5,
    tokens: BusanTokensV5,
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
        border = BorderStroke(1.dp, tokens.busanLightBlue.copy(alpha = 0.3f))
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
    tokens: BusanTokensV5
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

// 데이터 클래스
private data class BusanCitizenServiceV5(
    val name: String,
    val emoji: String
)