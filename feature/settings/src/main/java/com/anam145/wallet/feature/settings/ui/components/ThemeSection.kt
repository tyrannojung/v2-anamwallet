package com.anam145.wallet.feature.settings.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.anam145.wallet.core.ui.language.LocalStrings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam145.wallet.feature.settings.domain.model.ThemeMode

/**
 * 테마 설정 섹션
 */
@Composable
fun ThemeSection(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val strings = LocalStrings.current
    
    Column {
        Text(
            text = strings.settingsThemeSection,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            val isSystemMode = currentTheme == ThemeMode.SYSTEM
            val isDarkMode = when (currentTheme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Toggle between light and dark (skip system mode in direct toggle)
                        onThemeChange(
                            when (currentTheme) {
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.LIGHT
                                ThemeMode.SYSTEM -> if (isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
                            }
                        )
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Animated Icon Background
                    val scale by animateFloatAsState(
                        targetValue = if (isDarkMode) 1f else 0.9f,
                        animationSpec = spring(),
                        label = "iconScale"
                    )
                    
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isDarkMode) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        label = "backgroundColor"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDarkMode) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = if (isDarkMode) {
                                strings.settingsDarkMode
                            } else {
                                strings.settingsLightMode
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (isDarkMode) {
                                strings.settingsDarkModeDescription
                            } else {
                                strings.settingsLightModeDescription
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Custom Theme Switch
                ThemeSwitch(
                    isDarkMode = isDarkMode,
                    onToggle = {
                        onThemeChange(if (it) ThemeMode.DARK else ThemeMode.LIGHT)
                    }
                )
            }
        }
    }
}

/**
 * Custom animated theme switch
 */
@Composable
private fun ThemeSwitch(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (isDarkMode) 20f else 0f,
        animationSpec = spring(),
        label = "thumbOffset"
    )
    
    val trackColor by animateColorAsState(
        targetValue = if (isDarkMode) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "trackColor"
    )
    
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable { onToggle(!isDarkMode) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp, y = 0.dp)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}