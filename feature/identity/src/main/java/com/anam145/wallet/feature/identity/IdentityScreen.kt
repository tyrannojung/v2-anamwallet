package com.anam145.wallet.feature.identity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.theme.*

/**
 * 신원 화면
 * 
 * 사용자 신원 관리 화면입니다.
 * DID, VC, 학생증 정보 등을 관리합니다.
 */
@Composable
fun IdentityScreen(
    modifier: Modifier = Modifier,
    onNavigateToStudentCard: () -> Unit = {},
    onNavigateToDriverLicense: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        MobileDriverLicenseCard(
            title = strings.identityMobileDriverLicense,
            eligibilityLabel = strings.identityEligibility,
            eligibilityValue = strings.identityDriverEligibility,
            requirementsLabel = strings.identityRequirements,
            requirementsValue = strings.identityNoRequirements,
            buttonText = strings.identityInquire,
            onClick = onNavigateToDriverLicense  // TODO: 임시 - 조회하기 버튼으로 상세 화면 이동
        )
        
        MobileStudentIDCard(
            title = strings.identityMobileStudentId,
            eligibilityLabel = strings.identityEligibility,
            eligibilityValue = strings.identityStudentEligibility,
            requirementsLabel = strings.identityRequirements,
            requirementsValue = strings.identityNoRequirements,
            buttonText = strings.identityInquire,
            onClick = onNavigateToStudentCard  // TODO: 임시 - 조회하기 버튼으로 상세 화면 이동
        )
    }
}

@Composable
private fun MobileDriverLicenseCard(
    title: String,
    eligibilityLabel: String,
    eligibilityValue: String,
    requirementsLabel: String,
    requirementsValue: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 정보 섹션
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = eligibilityLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = eligibilityValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = requirementsLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = requirementsValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AnamSea
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                ),
                shape = ShapeNormal
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun MobileStudentIDCard(
    title: String,
    eligibilityLabel: String,
    eligibilityValue: String,
    requirementsLabel: String,
    requirementsValue: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 정보 섹션
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = eligibilityLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = eligibilityValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = requirementsLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = requirementsValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AnamSea
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                ),
                shape = ShapeNormal
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}