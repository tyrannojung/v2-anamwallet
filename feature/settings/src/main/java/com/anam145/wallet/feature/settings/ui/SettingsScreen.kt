package com.anam145.wallet.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.anam145.wallet.core.ui.language.LocalStrings
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.feature.settings.ui.components.*
import kotlinx.coroutines.flow.collectLatest

/**
 * 설정 화면
 * 
 * 앱 설정, 테마, 언어 등을 관리하는 화면
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToFAQ: () -> Unit = {},
    onNavigateToAppInfo: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    
    // 부수효과 처리
    // key = 이 작업을 다시 실행할 조건
    LaunchedEffect(key1 = viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SettingsContract.SettingsEffect.NavigateToHelp -> {
                    onNavigateToHelp()
                }
                SettingsContract.SettingsEffect.NavigateToFAQ -> {
                    onNavigateToFAQ()
                }
                SettingsContract.SettingsEffect.NavigateToAppInfo -> {
                    onNavigateToAppInfo()
                }
                SettingsContract.SettingsEffect.NavigateToLicense -> {
                    onNavigateToLicense()
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // 언어 섹션
        Spacer(modifier = Modifier.height(20.dp))
        LanguageSection(
            currentLanguage = uiState.language,
            onLanguageChange = { language ->
                viewModel.handleIntent(SettingsContract.SettingsIntent.ChangeLanguage(language))
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 스킨 섹션
        SkinSection(
            currentSkin = uiState.skin, // TODO: uiState에 skin 추가 필요
            onSkinChange = { skin ->
                viewModel.handleIntent(SettingsContract.SettingsIntent.ChangeSkin(skin))
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 지원 섹션
        SettingsSection(title = strings.settingsSupportSection) {
            SettingsListItem(
                title = strings.settingsHelp,
                subtitle = strings.settingsHelpDescription,
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                onClick = {
                    viewModel.handleIntent(SettingsContract.SettingsIntent.ClickHelp)
                }
            )
            
            SettingsListItem(
                title = strings.settingsFaq,
                subtitle = strings.settingsFaqDescription,
                icon = Icons.Outlined.QuestionAnswer,
                onClick = {
                    viewModel.handleIntent(SettingsContract.SettingsIntent.ClickFAQ)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 정보 섹션
        SettingsSection(title = strings.settingsAboutSection) {
            SettingsListItem(
                title = strings.settingsAppInfo,
                subtitle = strings.settingsAppInfoDescription,
                icon = Icons.Outlined.Info,
                onClick = {
                    viewModel.handleIntent(SettingsContract.SettingsIntent.ClickAppInfo)
                }
            )
            
            SettingsListItem(
                title = strings.settingsLicense,
                subtitle = strings.settingsLicenseDescription,
                icon = Icons.Outlined.Description,
                onClick = {
                    viewModel.handleIntent(SettingsContract.SettingsIntent.ClickLicense)
                }
            )
        }
        
        // 하단 여백
        Spacer(modifier = Modifier.height(32.dp))
    }
}