package com.anam145.wallet.feature.main.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 부산 월렛 커스텀 디자인 화면 V2
 * 
 * - 헤더 제거
 * - 디지털 자산 우선 표시
 * - 활성화된 자산만 잔고 표시
 * - 시민 서비스 통일된 색상
 * - 파도 애니메이션과 교통카드 제거
 */
@Composable
fun BusanCustomScreenV2(
    modifier: Modifier = Modifier
) {
    // 부산 브랜드 색상 (정확한 색상 적용)
    val busanBlue = Color(0xFF0F539E)      // 메인 파란색
    val busanBlack = Color(0xFF0E0E0E)     // 검정색
    val busanGray = Color(0xFF8F9295)      // 회색
    val busanLightBlue = Color(0xFFE6F4FF)
    val busanWhite = Color(0xFFFAFCFF)
    
    // 애니메이션 상태
    var isAnimating by remember { mutableStateOf(false) }
    
    // 시민 서비스 리스트 (확장 가능)
    val citizenServices = remember {
        listOf(
            CitizenService("부산일보", Icons.Default.Newspaper),
            CitizenService("본미디어", Icons.Default.PlayCircle),
            // 추가 서비스를 여기에 넣으면 자동으로 그리드에 표시됨
        )
    }
    
    LaunchedEffect(Unit) {
        delay(100)
        isAnimating = true
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        busanWhite,
                        busanLightBlue.copy(alpha = 0.15f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp) // 하단 네비게이션 공간
        ) {
            
            // 1. 활성 디지털 자산 섹션 (가장 중요)
            ActiveDigitalAssetCard(
                assetName = "Ethereum",
                assetSymbol = "ETH",
                balance = "2.4567",
                isAnimating = isAnimating,
                primaryColor = busanBlue,
                backgroundColor = busanLightBlue,
                busanBlack = busanBlack,
                busanGray = busanGray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 2. 다른 디지털 자산들 (컴팩트하게, 잔고 없음)
            Column {
                Text(
                    "블록체인 지갑",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = busanBlack,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    "탭하여 전환",
                    fontSize = 12.sp,
                    color = busanGray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 4.dp).padding(top = 2.dp, bottom = 8.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bitcoin (비활성 - 잔고 숨김)
                InactiveAssetCard(
                    modifier = Modifier.weight(1f),
                    name = "Bitcoin",
                    symbol = "BTC",
                    color = Color(0xFFF7931A),
                    busanBlack = busanBlack,
                    busanGray = busanGray
                )
                
                // 빈 공간 유지 (레이아웃 균형)
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // 3. 부산 시민 서비스 섹션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "부산 시민 서비스",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = busanBlack
                )
                
                // 서비스 개수 표시
                if (citizenServices.size > 4) {
                    Text(
                        "+${citizenServices.size - 4} 더보기",
                        fontSize = 12.sp,
                        color = busanBlue,
                        modifier = Modifier.clickable { /* 확장 액션 */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 시민 서비스 그리드 (확장 가능) - 통일된 색상
            CitizenServiceGrid(
                services = citizenServices,
                isAnimating = isAnimating,
                serviceColor = busanBlue,  // 모든 서비스 카드 동일한 색상
                busanBlack = busanBlack,
                busanGray = busanGray
            )
        }
        
        // 하단 부산 로고 (subtle)
        Text(
            "BUSAN WALLET",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = busanBlue.copy(alpha = 0.15f),
            letterSpacing = 4.sp
        )
    }
}

// 활성 디지털 자산 카드 (크고 중요하게)
@Composable
private fun ActiveDigitalAssetCard(
    assetName: String,
    assetSymbol: String,
    balance: String,
    isAnimating: Boolean,
    primaryColor: Color,
    backgroundColor: Color,
    busanBlack: Color = Color(0xFF0E0E0E),
    busanGray: Color = Color(0xFF8F9295)
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.08f),
                            primaryColor.copy(alpha = 0.03f)
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
                    color = primaryColor.copy(alpha = 0.05f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width - 50.dp.toPx(), 50.dp.toPx())
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.03f),
                    radius = 150.dp.toPx(),
                    center = Offset(size.width - 30.dp.toPx(), 80.dp.toPx())
                )
            }
            
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 상단: 자산 정보와 네트워크 상태
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
                            // 이더리움 아이콘 플레이스홀더
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = 0.2f),
                                                primaryColor.copy(alpha = 0.1f)
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
                                    color = primaryColor.copy(alpha = 0.7f)
                                )
                            }
                            
                            Column {
                                Text(
                                    assetName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = busanBlack
                                )
                                Text(
                                    "com.anam.ethereum",
                                    fontSize = 12.sp,
                                    color = busanGray
                                )
                            }
                        }
                    }
                    
                    // 활성화 상태
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Text(
                                "Activated",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 잔액 정보
                Column {
                    Text(
                        "잔액",
                        fontSize = 12.sp,
                        color = busanGray
                    )
                    Text(
                        "$balance ETH",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = busanBlack
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 상세보기 버튼
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "지갑 열기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// 비활성 자산 카드 (작고 심플하게, 잔고 없음)
@Composable
private fun InactiveAssetCard(
    modifier: Modifier = Modifier,
    name: String,
    symbol: String,
    color: Color,
    busanBlack: Color = Color(0xFF0E0E0E),
    busanGray: Color = Color(0xFF8F9295)
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
                            color.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        symbol.first().toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                Text(
                    name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = busanBlack
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = busanGray.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 시민 서비스 그리드 (확장 가능, 통일된 색상)
@Composable
private fun CitizenServiceGrid(
    services: List<CitizenService>,
    isAnimating: Boolean,
    serviceColor: Color,  // 통일된 색상
    busanBlack: Color = Color(0xFF0E0E0E),
    busanGray: Color = Color(0xFF8F9295)
) {
    // 2열 그리드로 표시, 서비스가 많아지면 자동으로 늘어남
    val columns = 2
    val rows = (services.size + columns - 1) / columns
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0 until minOf(rows, 2)) { // 최대 2줄만 표시 (4개)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < services.size) {
                        val service = services[index]
                        CitizenServiceCard(
                            modifier = Modifier.weight(1f),
                            service = service,
                            color = serviceColor,  // 통일된 색상 사용
                            isAnimating = isAnimating,
                            busanBlack = busanBlack,
                            busanGray = busanGray
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// 시민 서비스 카드
@Composable
private fun CitizenServiceCard(
    modifier: Modifier = Modifier,
    service: CitizenService,
    color: Color,
    isAnimating: Boolean,
    busanBlack: Color = Color(0xFF0E0E0E),
    busanGray: Color = Color(0xFF8F9295)
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .aspectRatio(1.8f)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = busanGray.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, busanGray.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                service.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = busanGray
            )
            
            Text(
                service.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = busanBlack
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = busanGray.copy(alpha = 0.5f)
            )
        }
    }
}

// 시민 서비스 데이터 클래스
data class CitizenService(
    val name: String,
    val icon: ImageVector
)