package com.anam145.wallet.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.anam145.wallet.core.common.model.Skin
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Skin.entries.forEach { skin ->
                FilterChip(
                        selected = currentSkin == skin,
                        onClick = { onSkinChange(skin) },
                        enabled = true,
                        label = {
                            Text(
                                text = when (skin) {
                                    Skin.ANAM -> strings.skinAnam
                                    Skin.BUSAN -> strings.skinBusan
                                }
                            )
                        },
                        leadingIcon = null,
                        shape = RoundedCornerShape(8.dp),
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,  // 항상 흰색
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                )
            }
        }
    }
}