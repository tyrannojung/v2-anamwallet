package com.anam145.wallet.feature.identity.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.theme.*
import com.anam145.wallet.feature.identity.R as IdentityR

/**
 * 운전면허증 상세 화면
 * 
 * 운전면허증 정보를 자세히 표시하는 화면
 */
@Composable
fun DriverLicenseDetailScreen(
    modifier: Modifier = Modifier,
    vcId: String,
    onBackClick: () -> Unit = {},
    viewModel: DriverLicenseDetailViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
    val driverLicense by viewModel.driverLicense.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 상단 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = strings.back,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = strings.identityMobileDriverLicense,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // 카드 컨테이너
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),  // padding 제거하여 카드를 위로 올림
            contentAlignment = Alignment.TopCenter  // Center → TopCenter로 변경
        ) {
            // 운전면허증 카드
            Card(
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight(0.95f)  // 화면 높이의 95% 사용
                    .padding(horizontal = 20.dp, vertical = 10.dp),  // 상하 패딩 줄임
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column {
                    // 카드 헤더
                    DriverCardDetailHeader()
                    
                    // 카드 바디
                    driverLicense?.let {
                        DriverCardDetailBody(driverLicense = it)
                    } ?: DriverCardDetailBodyLoading()
                }
            }
        }
    }
}

@Composable
private fun DriverCardDetailHeader() {
    val strings = LocalStrings.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D47A1),  // 진한 파랑
                        Color(0xFF1976D2)   // 파랑
                    )
                )
            )
            .padding(25.dp)
    ) {
        // 배경 태극기 패턴
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-30).dp)
                .rotate(-15f)
                .alpha(0.1f)
        ) {
            // 태극기 원 패턴
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .background(
                        color = Color.Red.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 25.dp, y = 25.dp)
                    .background(
                        color = Color.Blue.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
        
        // 새로고침 버튼 제거
        
        // 기관 정보
        Column {
            Text(
                text = strings.identityDriversLicense,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(5.dp))
            
            Text(
                text = strings.identityRepublicOfKorea,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DriverCardDetailBody(
    driverLicense: DriverLicenseDetailViewModel.DriverLicenseInfo
) {
    val strings = LocalStrings.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 20.dp),  // 상하 패딩 줄임 30dp → 20dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지
        Image(
            painter = painterResource(id = IdentityR.drawable.ic_photo),
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(width = 80.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(15.dp))  // 20dp → 15dp
        
        // 운전자 이름
        Text(
            text = driverLicense.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(5.dp))
        
        // 면허번호
        Text(
            text = driverLicense.licenseNumber,
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.height(15.dp))  // 20dp → 15dp
        
        // 면허 정보 섹션 (고정 높이)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)  // 고정 높이 증가 120dp → 140dp
                .padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly  // 균등 분배
        ) {
            Divider(color = Color(0xFFF0F0F0))
            
            // 면허 종류
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.identityLicenseType,
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = driverLicense.licenseType,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.identityIssueDate,
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = driverLicense.issueDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            // 적성검사 기간 (갱신 제거 - 한국 운전면허증에는 적성검사만 있음)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.identityAptitudeTest,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = driverLicense.expiryDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }
            
            Divider(color = Color(0xFFF0F0F0))
        }
        
        Spacer(modifier = Modifier.height(5.dp))  // 10dp → 5dp
        
        // QR 섹션 (고정 높이)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)  // 고정 높이 감소 80dp → 70dp
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // QR 코드
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Image(
                    painter = painterResource(id = IdentityR.drawable.ic_qr),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // QR 텍스트
            Text(
                text = strings.identityQRScan,
                fontSize = 12.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            )
        }
    }
}

@Composable
private fun DriverCardDetailBodyLoading() {
    val strings = LocalStrings.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로딩 표시
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = strings.loading,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}