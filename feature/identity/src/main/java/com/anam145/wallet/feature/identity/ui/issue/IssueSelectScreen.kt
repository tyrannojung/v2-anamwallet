package com.anam145.wallet.feature.identity.ui.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.theme.*
import com.anam145.wallet.feature.identity.ui.issue.IssueSelectContract
import com.anam145.wallet.feature.identity.ui.issue.IssueSelectViewModel
import com.anam145.wallet.feature.identity.ui.util.debouncedClickable

/**
 * 신분증 발급 선택 화면
 * 
 * 발급할 신분증 종류를 선택하는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueSelectScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: IssueSelectViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is IssueSelectContract.Effect.NavigateBack -> onBackClick()
                is IssueSelectContract.Effect.ShowToast -> {
                    // Toast will be handled by parent
                }
            }
        }
    }
    
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
                text = strings.identityIssueId,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // 선택 버튼들
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 학생증 발급 버튼
                    IssueButton(
                        modifier = Modifier.weight(1f),
                        title = strings.identityIssueStudentId,
                        subtitle = strings.identityStudentIdCard,
                        onClick = { viewModel.handleIntent(IssueSelectContract.Intent.IssueStudentCard) },
                        isIssued = uiState.isStudentCardIssued,
                        isIssuing = uiState.isIssuingStudentCard
                    )
                    
                    // 운전면허증 발급 버튼
                    IssueButton(
                        modifier = Modifier.weight(1f),
                        title = strings.identityIssueDriverLicense,
                        subtitle = strings.identityDriverLicenseCard,
                        onClick = { viewModel.handleIntent(IssueSelectContract.Intent.IssueDriverLicense) },
                        isIssued = uiState.isDriverLicenseIssued,
                        isIssuing = uiState.isIssuingDriverLicense
                    )
                }
                
                // 모두 발급된 상태 메시지
                if (uiState.isStudentCardIssued && uiState.isDriverLicenseIssued) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = strings.identityAllIssued,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // 에러 스낵바
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.handleIntent(IssueSelectContract.Intent.ClearError) }) {
                        Text(strings.dismiss)
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun IssueButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isIssued: Boolean = false,
    isIssuing: Boolean = false
) {
    val strings = LocalStrings.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .debouncedClickable { 
                if (!isIssued && !isIssuing) onClick() 
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isIssued) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        border = BorderStroke(1.dp, AnamLightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 아이콘 영역
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = if (isIssued) Color(0xFFE0E0E0) else AnamAqua.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isIssuing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = AnamPrimary
                    )
                } else if (isIssued) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF4CAF50)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = AnamPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 부제목
            Text(
                text = subtitle,
                color = AnamTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // 제목
            Text(
                text = if (isIssued) strings.identityIssued else title,
                color = if (isIssued) Color(0xFF9E9E9E) else AnamTextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}