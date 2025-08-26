package com.anam145.wallet.feature.main.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 부산 월렛 커스텀 화면 V6
 * - V5의 컨텐츠 기반
 * - 이미지 레퍼런스의 디자인 스타일 적용
 * - 부산 시그니처 색상 사용
 * - 그라데이션 헤더
 */

// 부산 월렛 색상 토큰 V6
@Stable
data class BusanTokensV6(
    // 부산 시그니처 색상
    val busanBlue: Color = Color(0xFF0F2A48),    // 진한 남색
    val busanSkyBlue: Color = Color(0xFF5AABDB), // 하늘색
    val busanLightBlue: Color = Color(0xFFACCDEC), // 연한 하늘색
    val busanPaleBlue: Color = Color(0xFFAECDEC), // 연한 민트색
    
    // 기본 색상
    val black: Color = Color(0xFF0E0E0E),
    val gray: Color = Color(0xFF8F9295),
    val lightGray: Color = Color(0xFFF3F4F6),
    val white: Color = Color(0xFFFFFFFF),
    val green: Color = Color(0xFF4CAF50),
    val bitcoin: Color = Color(0xFFF7931A),
    val ethereum: Color = Color(0xFF627EEA),
    val blue: Color = Color(0xFF0F539E)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusanCustomScreenV6() {
    val tokens = remember { BusanTokensV6() }
    var selectedBlockchain by remember { mutableStateOf("Bitcoin") }
    val blockchains = listOf("Bitcoin", "Ethereum", "Solana")
    var expanded by remember { mutableStateOf(false) }
    
    // 애니메이션 트리거
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.lightGray)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 그라데이션 헤더 섹션
            item {
                HeaderSection(
                    tokens = tokens,
                    selectedBlockchain = selectedBlockchain,
                    onBlockchainSelected = { selectedBlockchain = it },
                    blockchains = blockchains,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    isVisible = isVisible
                )
            }
            
            // 부산 서비스 섹션
            item {
                ServiceSection(tokens = tokens)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderSection(
    tokens: BusanTokensV6,
    selectedBlockchain: String,
    onBlockchainSelected: (String) -> Unit,
    blockchains: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isVisible: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tokens.busanSkyBlue,
                        tokens.busanLightBlue,
                        tokens.busanPaleBlue.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // 환영 메시지
            Text(
                "나에게 맞는",
                fontSize = 16.sp,
                color = tokens.white.copy(alpha = 0.9f),
                fontWeight = FontWeight.Normal
            )
            Text(
                "블록체인 자산을 선택하시오",
                fontSize = 22.sp,
                color = tokens.white,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 블록체인 선택기
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .menuAnchor(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onExpandedChange(!expanded) }
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 블록체인 아이콘
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = when (selectedBlockchain) {
                                            "Bitcoin" -> tokens.bitcoin.copy(alpha = 0.15f)
                                            "Ethereum" -> tokens.ethereum.copy(alpha = 0.15f)
                                            else -> tokens.blue.copy(alpha = 0.15f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (selectedBlockchain) {
                                        "Bitcoin" -> "₿"
                                        "Ethereum" -> "Ξ"
                                        else -> "S"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (selectedBlockchain) {
                                        "Bitcoin" -> tokens.bitcoin
                                        "Ethereum" -> tokens.ethereum
                                        else -> tokens.blue
                                    }
                                )
                            }
                            Text(
                                selectedBlockchain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = tokens.black
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "드롭다운",
                            tint = tokens.busanBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    blockchains.forEach { blockchain ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
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
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
            
            // 선택기 설명 텍스트
            Text(
                "현재 활성화된 블록체인 변경",
                fontSize = 12.sp,
                color = tokens.white.copy(alpha = 0.8f),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 활성 자산 카드
            ActiveAssetCard(
                selectedBlockchain = selectedBlockchain,
                tokens = tokens,
                isVisible = isVisible
            )
        }
    }
}

@Composable
private fun ActiveAssetCard(
    selectedBlockchain: String,
    tokens: BusanTokensV6,
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
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            tokens.busanSkyBlue.copy(alpha = 0.08f),
                            tokens.busanLightBlue.copy(alpha = 0.03f)
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
                    .height(200.dp)
                    .align(Alignment.TopEnd)
            ) {
                drawCircle(
                    color = tokens.busanSkyBlue.copy(alpha = 0.06f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width - 50.dp.toPx(), 50.dp.toPx())
                )
                drawCircle(
                    color = tokens.busanLightBlue.copy(alpha = 0.04f),
                    radius = 150.dp.toPx(),
                    center = Offset(size.width - 30.dp.toPx(), 80.dp.toPx())
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            when (selectedBlockchain) {
                                "Bitcoin" -> "Bitcoin"
                                "Ethereum" -> "Ethereum"
                                else -> "Solana"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.black
                        )
                        Text(
                            when (selectedBlockchain) {
                                "Bitcoin" -> "com.anam.bitcoin"
                                "Ethereum" -> "com.anam.ethereum"
                                else -> "com.anam.solana"
                            },
                            fontSize = 12.sp,
                            color = tokens.gray,
                            letterSpacing = 0.02.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    AssistChip(
                        onClick = { },
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
                        color = tokens.gray
                    )
                    Text(
                        when (selectedBlockchain) {
                            "Bitcoin" -> "0.0024 BTC"
                            "Ethereum" -> "0.1234 ETH"
                            else -> "45.67 SOL"
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.black
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 상세보기 버튼
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.busanBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "상세보기",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceSection(tokens: BusanTokensV6) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "부산 서비스",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.black
            )
            TextButton(onClick = { }) {
                Text(
                    "모두보기",
                    fontSize = 14.sp,
                    color = tokens.busanSkyBlue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ServiceGrid(tokens = tokens)
        
        // 더 많은 모듈 다운로드 버튼
        OutlinedButton(
            onClick = { },
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
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ServiceGrid(tokens: BusanTokensV6) {
    val services = remember {
        listOf(
            ServiceItem("부산일보", "🗞️"),
            ServiceItem("본미디어", "📺")
        )
    }
    
    val gridHeight = remember(services.size) {
        val rows = (services.size + 1) / 2
        (rows * 70 + (rows - 1) * 12).dp
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(services) { service ->
            ServiceCard(
                service = service,
                tokens = tokens
            )
        }
    }
}

@Composable
private fun ServiceCard(
    service: ServiceItem,
    tokens: BusanTokensV6
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "service_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
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

// 데이터 클래스
private data class ServiceItem(
    val name: String,
    val emoji: String
)