package com.anam145.wallet.feature.miniapp.webapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialInfo
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialType

/**
 * VP(Verifiable Presentation) 요청 바텀시트
 * 
 * WebApp 프로세스에서 직접 표시되는 바텀시트입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VPBottomSheet(
    serviceName: String,
    purpose: String,
    credentialType: String, // "both", "driver", "student"
    credentials: List<CredentialInfo>,
    onCredentialSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    
    // 지원되는 신분증 필터링
    val supportedCredentials = remember(credentialType, credentials) {
        when (credentialType) {
            "student" -> credentials.filter { it.type == CredentialType.STUDENT_CARD }
            "driver" -> credentials.filter { it.type == CredentialType.DRIVER_LICENSE }
            else -> credentials // "both"
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // 드래그 핸들
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 아이콘
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 제목
            Text(
                text = "${serviceName} requests your ID",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 목적
            Text(
                text = "Purpose: $purpose",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 신분증 목록
            if (supportedCredentials.isNotEmpty()) {
                Text(
                    text = "Select an ID",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 신분증 카드들
                supportedCredentials.forEach { credential ->
                    CredentialCard(
                        credential = credential,
                        enabled = credential.isIssued,
                        onClick = { 
                            if (credential.isIssued) {
                                onCredentialSelected(credential.id) 
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // 지원되는 신분증이 없는 경우
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "No available IDs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 취소 버튼
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun CredentialCard(
    credential: CredentialInfo,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Icon(
                imageVector = when (credential.type) {
                    CredentialType.STUDENT_CARD -> Icons.Default.School
                    CredentialType.DRIVER_LICENSE -> Icons.Default.DirectionsCar
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = credential.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                if (!enabled) {
                    Text(
                        text = "Requires issuance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = credential.holderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
            
            // 화살표 (활성화된 경우만)
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}