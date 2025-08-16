package com.anam145.wallet.feature.identity.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.theme.*
import com.anam145.wallet.feature.identity.domain.model.CredentialType
import com.anam145.wallet.feature.identity.domain.model.IssuedCredential
import com.anam145.wallet.feature.identity.R as IdentityR
import com.anam145.wallet.feature.identity.ui.main.IdentityContract
import com.anam145.wallet.feature.identity.ui.main.IdentityViewModel
import com.anam145.wallet.feature.identity.ui.util.debouncedClickable

/**
 * 디지털 신분증 화면
 * 
 * 디지털 신분증 관련 기능들을 표시하는 화면
 */
@Composable
fun IdentityScreen(
    modifier: Modifier = Modifier,
    onNavigateToStudentCard: (String) -> Unit = {},
    onNavigateToDriverLicense: (String) -> Unit = {},
    onNavigateToIssue: () -> Unit = {},
    viewModel: IdentityViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is IdentityContract.Effect.NavigateToStudentCardDetail -> 
                    onNavigateToStudentCard(effect.vcId)
                is IdentityContract.Effect.NavigateToDriverLicenseDetail -> 
                    onNavigateToDriverLicense(effect.vcId)
                IdentityContract.Effect.NavigateToIssue -> 
                    onNavigateToIssue()
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 상단 타이틀
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = strings.identityMyIds,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 카드 리스트 또는 빈 상태
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.issuedCredentials.isEmpty() -> {
                // 발급된 신분증이 없는 경우
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = strings.identityNoIssuedIds,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                // 발급된 신분증 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.issuedCredentials.forEach { credential ->
                        when (credential.type) {
                            CredentialType.STUDENT_CARD -> {
                                StudentCard(
                                    credential = credential,
                                    onClick = { 
                                        viewModel.handleIntent(
                                            IdentityContract.Intent.NavigateToDetail(credential)
                                        )
                                    }
                                )
                            }
                            CredentialType.DRIVER_LICENSE -> {
                                DriverLicenseCard(
                                    credential = credential,
                                    onClick = { 
                                        viewModel.handleIntent(
                                            IdentityContract.Intent.NavigateToDetail(credential)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 발급하기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { viewModel.handleIntent(IdentityContract.Intent.NavigateToIssue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.identityIssueNewId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StudentCard(
    credential: IssuedCredential,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column {
            // 카드 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8B1538),
                                Color(0xFFA91E3E)
                            )
                        )
                    )
                    .debouncedClickable { onClick() }
            ) {
                // 배경 로고 (투명도 적용)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp)
                        .alpha(0.1f)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                
                // 대학 정보
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = strings.identityKoreaUniversity,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = strings.identityGraduateSchool,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
                
                // 상세보기 버튼
                Text(
                    text = strings.identityViewDetail,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            // 카드 바디
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 사진
                Image(
                    painter = painterResource(id = IdentityR.drawable.ic_photo),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(width = 50.dp, height = 62.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(15.dp))
                
                // 학생 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = strings.identitySampleName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = credential.studentNumber ?: "2023572504",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "${credential.department ?: strings.identityFinancialSecurity} · ${strings.identityBlockchainMajor}",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
                
                // 유효 배지
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = strings.identityValid,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverLicenseCard(
    credential: IssuedCredential,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column {
            // 카드 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0D47A1),
                                Color(0xFF1976D2)
                            )
                        )
                    )
                    .debouncedClickable { onClick() }
            ) {
                // 배경 태극기 패턴 (투명도 적용)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 10.dp)
                        .alpha(0.1f)
                ) {
                    // 태극기 원 패턴
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                            .background(
                                color = Color.Red.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 12.dp, y = 12.dp)
                            .background(
                                color = Color.Blue.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
                
                // 기관 정보
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = strings.identityRepublicOfKorea,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = strings.identityDriversLicense,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
                
                // 상세보기 버튼
                Text(
                    text = strings.identityViewDetail,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            // 카드 바디
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 사진
                Image(
                    painter = painterResource(id = IdentityR.drawable.ic_photo),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(width = 50.dp, height = 62.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(15.dp))
                
                // 운전자 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = strings.identitySampleName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = credential.licenseNumber ?: "11-22-333333-44",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = strings.identityClass1Regular,
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
                
                // 유효 배지
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = strings.identityValid,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}