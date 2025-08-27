package com.anam145.wallet.feature.main.ui.skins

import androidx.compose.runtime.Composable
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.feature.main.ui.MainContract
import com.anam145.wallet.feature.main.ui.MainViewModel
import com.anam145.wallet.feature.main.ui.skins.custom.busan.BusanSkinScreen
import com.anam145.wallet.feature.main.ui.skins.default.DefaultSkinScreen

/**
 * 스킨 프로바이더
 * 
 * 현재 선택된 스킨에 따라 적절한 UI를 표시합니다.
 * - 기본 스킨: 색상과 레이아웃만 약간 변경
 * - 커스텀 스킨: 완전히 새로운 UI 구조
 */
@Composable
fun ProvideSkin(
    skin: Skin,
    uiState: MainContract.MainState,
    viewModel: MainViewModel,
    onNavigateToHub: () -> Unit = {}
) {
    when (skin) {
        Skin.ANAM -> { // Skin.SEOUL, Skin.LA 추후 추가 예정
            // 기본 스킨 사용 (색상과 섹션 순서만 변경)
            DefaultSkinScreen(
                uiState = uiState,
                viewModel = viewModel,
                skin = skin,
                onNavigateToHub = onNavigateToHub
            )
        }
        
        Skin.BUSAN -> {
            // 부산 커스텀 스킨 (완전히 다른 UI)
            BusanSkinScreen(
                blockchainApps = uiState.blockchainApps,
                regularApps = uiState.regularApps,
                activeBlockchainId = uiState.activeBlockchainId,
                onBlockchainClick = { miniApp ->
                    viewModel.handleIntent(
                        MainContract.MainIntent.SwitchBlockchain(miniApp)
                    )
                },
                onRegularAppClick = { miniApp ->
                    viewModel.handleIntent(
                        MainContract.MainIntent.ClickRegularApp(miniApp)
                    )
                }
            )
        }
    }
}