package com.anam145.wallet.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * Bottom Navigation Bar 아이템 정의
 * 
 * @property route 네비게이션 경로
 * @property selectedIcon 선택된 상태의 아이콘
 * @property unselectedIcon 선택되지 않은 상태의 아이콘
 * @property labelKey 표시될 라벨의 키
 */
data class BottomNavItem(
    val route: AnamNavRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelKey: String
)

/**
 * Bottom Navigation 아이템 목록을 NavigationConfig에서 가져옴
 * Single Source of Truth 원칙 적용
 * NavigationConfig는 볼 필요도 없이, 상수 개념처럼 5개 list 던져준다고 생각하면 됨
 */
private val bottomNavItems: List<BottomNavItem>
    get() = NavigationConfig.getBottomNavItems()

/**
 * ANAM Wallet Bottom Navigation Bar
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun AnamBottomNavigation(
    navController: NavController
) {
    // 현재 선택된 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val strings = LocalStrings.current
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(elevation = 8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route.route
                
                BottomNavItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route.route) {
                                // 시작 지점까지 스택을 비움
                                // 즉, 홈 → 설정 → 허브 → 브라우저 → [허브] 를 눌렀다면,
                                // → 설정/허브/브라우저 스택은 모두 제거되고
                                // → 허브로 이동함
                                popUpTo(navController.graph.startDestinationId) {
                                    /**
                                     * 이건 제거한 화면의 상태를 저장하겠다는 뜻
                                     * 나중에 restoreState = true가 설정되어 있으면,
                                     * 해당 화면에 다시 갔을 때 초기화되지 않고 그대로 복원.
                                     * ex:
                                     * 사용자가 Settings 화면에서 스크롤을 내린 상태였고
                                     * 다른 탭으로 이동했다가 다시 Settings를 눌렀을 때,
                                     * 스크롤 위치나 UI 상태가 복원 됨.*/
                                    saveState = true

                                }
                                // 동일한 화면이 여러 번 스택에 쌓이지 않도록 함
                                launchSingleTop = true
                                // 다시 이동 시 상태 복원, saveState = true 이거랑 세트
                                restoreState = true
                            }
                        }
                    },
                    icon = if (selected) item.selectedIcon else item.unselectedIcon,
                    label = when (item.labelKey) {
                        "main" -> strings.navMain
                        "hub" -> strings.navHub
                        "browser" -> strings.navBrowser
                        "identity" -> strings.navDid
                        "settings" -> strings.navSettings
                        else -> ""
                    }
                )
            }
        }
    }
}

/**
 * 개별 Bottom Navigation 아이템
 * 
 * @param selected 선택 상태
 * @param onClick 클릭 이벤트
 * @param icon 표시할 아이콘
 * @param label 표시할 라벨
 */
@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    // 애니메이션 값들
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(),
        label = "scale"
    )
    
    val color by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "color"
    )
    
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 리플 효과 제거
            ) { onClick() }
    ) {
        // 선택된 아이템 상단 인디케이터 - Box 최상단에 배치
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-1).dp)  // 위로 0.5dp 이동
                    .width(40.dp)
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        )
                    )
            )
        }
        
        // 아이콘과 라벨을 담은 Column
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 아이콘
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(26.dp)
                    .scale(scale),
                tint = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 라벨
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = color
            )
        }
    }
}