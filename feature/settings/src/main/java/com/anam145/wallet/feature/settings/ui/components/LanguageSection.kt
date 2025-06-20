package com.anam145.wallet.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anam145.wallet.core.ui.language.LocalStrings
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anam145.wallet.core.ui.language.Language

/**
 * 언어 설정 섹션
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSection(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    
    Column {
        Text(
            text = strings.settingsLanguageSection,
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
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = when (currentLanguage) {
                        Language.KOREAN -> strings.settingsLanguageKorean
                        Language.ENGLISH -> strings.settingsLanguageEnglish
                    },
                    onValueChange = { },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Language.entries.forEach { language ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (language) {
                                            Language.KOREAN -> strings.settingsLanguageKorean
                                            Language.ENGLISH -> strings.settingsLanguageEnglish
                                        }
                                    )
                                    if (language == currentLanguage) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onLanguageChange(language)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}