package com.anam145.wallet.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.core.common.constants.SkinConstants
import com.anam145.wallet.core.ui.language.LocalStrings
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 스킨 설정 섹션 - 칩 스타일
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinSection(
    currentSkin: Skin,
    onSkinChange: (Skin) -> Unit
) {
    val strings = LocalStrings.current
    
    Column {
        // 섹션 타이틀
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.settingsSkinSection,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        
        // 칩 그룹
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Skin.entries.forEach { skin ->
                    val isEnabled = SkinConstants.isSkinEnabled(skin)
                    FilterChip(
                        selected = currentSkin == skin,
                        onClick = { if (isEnabled) onSkinChange(skin) },
                        enabled = isEnabled,
                        label = {
                            Text(
                                text = when (skin) {
                                    Skin.ANAM -> strings.skinAnam
                                    Skin.SEOUL -> strings.skinSeoul
                                    Skin.BUSAN -> strings.skinBusan
                                    Skin.LA -> strings.skinLA
                                }
                            )
                        },
                        leadingIcon = if (currentSkin == skin) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(8.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = isEnabled,
                            selected = currentSkin == skin,
                            borderColor = when {
                                currentSkin == skin -> MaterialTheme.colorScheme.primary
                                !isEnabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = if (currentSkin == skin) 2.dp else 1.dp,
                            selectedBorderWidth = 2.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    )
                }
            }
        }
    }
}