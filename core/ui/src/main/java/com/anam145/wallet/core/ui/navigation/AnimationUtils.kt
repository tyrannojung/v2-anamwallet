package com.anam145.wallet.core.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NamedNavArgument

/**
 * 일관된 애니메이션이 적용된 composable 네비게이션 빌더
 * 
 * 모든 화면 전환에 동일한 150ms 슬라이드 애니메이션을 적용합니다.
 * - 진입: 화면 너비만큼 오른쪽에서 슬라이드
 * - 뒤로가기: 살짝 겹치는 효과로 자연스러운 전환
 * 
 * 사용 예시:
 * ```
 * animatedComposable("home") { 
 *     HomeScreen()
 * }
 * ```
 */
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = { 
            slideInHorizontally(
                initialOffsetX = { it },  // 화면 너비 기반
                animationSpec = tween(150)
            ) + fadeIn(tween(100))
        },
        exitTransition = { 
            fadeOut(tween(50))  // 빠른 페이드아웃으로 잔상 최소화
        },
        popEnterTransition = { 
            slideInHorizontally(
                initialOffsetX = { -it },  // 왼쪽에서 진입
                animationSpec = tween(150)
            ) + fadeIn(tween(100))
        },
        popExitTransition = { 
            fadeOut(tween(50))
        },
        content = content
    )
}