package com.anam145.wallet.feature.identity.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * 중복 클릭을 방지하는 Clickable Modifier
 * 
 * 지정된 시간 동안 추가 클릭을 무시합니다.
 * 
 * @param debounceTime 클릭 간 최소 간격 (밀리초)
 * @param onClick 클릭 이벤트 핸들러
 */
@Composable
fun Modifier.debouncedClickable(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): Modifier {
    var isClickable by remember { mutableStateOf(true) }
    
    LaunchedEffect(isClickable) {
        if (!isClickable) {
            delay(debounceTime)
            isClickable = true
        }
    }
    
    return this.clickable(enabled = isClickable) {
        if (isClickable) {
            isClickable = false
            onClick()
        }
    }
}