package com.anam145.wallet.feature.identity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * NFT 상세 화면
 * BEXCO G-STAR 2024 입장권 NFT 상세 정보를 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFTDetailScreen(
    nftId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    
    // BEXCO G-STAR NFT 정보 (하드코딩)
    val nftInfo = NFTDetailInfo(
        id = "5",
        title = "입장권 : BEXCO G-STAR 2024 입장권",
        collection = "G-STAR 컬렉션",
        price = "300 BSN",
        owner = "0x1234...5678",
        issueDate = "2024.11.14",
        eventDate = "2024.11.14 - 2024.11.17",
        location = "부산 BEXCO 제1전시장",
        description = "국내 최대 게임 전시회 G-STAR 2024의 4일권 입장권입니다. " +
                     "이 NFT를 소지하고 있으면 행사 기간 중 언제든지 입장 가능하며, " +
                     "특별 이벤트 및 한정판 굿즈 구매 우선권이 제공됩니다.",
        benefits = listOf(
            "G-STAR 2024 전 일정 입장 가능",
            "VIP 라운지 이용권",
            "한정판 굿즈 구매 우선권",
            "특별 이벤트 참여 자격"
        ),
        imageResId = R.drawable.nft_sample_5
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFT 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // NFT 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = painterResource(id = nftInfo.imageResId),
                    contentDescription = nftInfo.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // G-STAR 배지
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "G-STAR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            
            // NFT 정보
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 타이틀과 가격
                Text(
                    text = nftInfo.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nftInfo.collection,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = nftInfo.price,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // QR 코드 섹션
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "입장 QR 코드",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // QR 코드 플레이스홀더
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(168.dp),
                                tint = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "행사장 입구에서 QR 코드를 스캔해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 상세 정보
                DetailSection(title = "행사 정보") {
                    DetailRow(label = "행사 기간", value = nftInfo.eventDate)
                    DetailRow(label = "장소", value = nftInfo.location)
                    DetailRow(label = "발행일", value = nftInfo.issueDate)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailSection(title = "설명") {
                    Text(
                        text = nftInfo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailSection(title = "혜택") {
                    nftInfo.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "•",
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailSection(title = "소유자 정보") {
                    DetailRow(label = "지갑 주소", value = nftInfo.owner)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// NFT 상세 정보 데이터 클래스
private data class NFTDetailInfo(
    val id: String,
    val title: String,
    val collection: String,
    val price: String,
    val owner: String,
    val issueDate: String,
    val eventDate: String,
    val location: String,
    val description: String,
    val benefits: List<String>,
    val imageResId: Int
)