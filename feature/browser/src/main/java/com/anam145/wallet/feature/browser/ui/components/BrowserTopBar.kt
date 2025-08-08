package com.anam145.wallet.feature.browser.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings

/**
 * 브라우저 상단 바
 * URL 입력 및 북마크 토글 기능
 */
@Composable
fun BrowserTopBar(
    url: String,
    pageTitle: String,
    isLoading: Boolean,
    isBookmarked: Boolean,
    showUrlBar: Boolean,
    urlInput: String,
    searchSuggestions: List<String>,
    showBookmarks: Boolean = false,  // 북마크 화면 여부
    onUrlInputChange: (String) -> Unit,
    onUrlSubmit: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onShowUrlBar: () -> Unit,
    onHideUrlBar: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onClearInput: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
            ) {
                AnimatedContent(
                    targetState = showUrlBar,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { isEditing ->
                    if (isEditing) {
                        // URL 입력 모드
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 검색 입력 필드
                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = onUrlInputChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .padding(start = 8.dp)
                                    .height(48.dp),  // 고정 높이로 텍스트 잘림 방지
                                placeholder = { 
                                    val strings = LocalStrings.current
                                    Text(
                                        text = strings.browserSearchPlaceholder,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (urlInput.isNotEmpty()) {
                                        IconButton(
                                            onClick = onClearInput,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        if (urlInput.isNotEmpty()) {
                                            val finalUrl = if (urlInput.startsWith("http://") || urlInput.startsWith("https://")) {
                                                urlInput
                                            } else if (urlInput.contains(".")) {
                                                "https://$urlInput"
                                            } else {
                                                "https://duckduckgo.com/?q=$urlInput"
                                            }
                                            onUrlSubmit(finalUrl)
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(24.dp),  // MetaMask처럼 둥근 모서리
                                textStyle = MaterialTheme.typography.bodyMedium  // 텍스트 스타일 명시
                            )
                            
                            // Cancel 버튼 (오른쪽)
                            TextButton(
                                onClick = onHideUrlBar,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                val strings = LocalStrings.current
                                Text(strings.cancel)
                            }
                        }
                        
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        // 일반 모드
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onShowUrlBar() }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showBookmarks) {
                                // 북마크 화면일 때: 검색 아이콘과 플레이스홀더만 표시
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val strings = LocalStrings.current
                                Text(
                                    text = strings.browserSearchPlaceholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                // 웹 페이지 볼 때: 기존 UI
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (url.startsWith("https://")) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (pageTitle.isNotEmpty()) {
                                        Text(
                                            text = pageTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                // 북마크 버튼 (웹 페이지에서만 표시)
                                IconButton(onClick = onBookmarkClick) {
                                    Icon(
                                        imageVector = if (isBookmarked) {
                                            Icons.Default.Bookmark
                                        } else {
                                            Icons.Outlined.BookmarkBorder
                                        },
                                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                                        tint = if (isBookmarked) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 검색 추천
        AnimatedVisibility(
            visible = showUrlBar && searchSuggestions.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                items(searchSuggestions) { suggestion ->
                    ListItem(
                        headlineContent = { 
                            val strings = LocalStrings.current
                            val displayText = when {
                                suggestion == "Search on DuckDuckGo" -> {
                                    if (urlInput.isEmpty()) {
                                        strings.browserSearchDuckDuckGo
                                    } else {
                                        "${strings.browserSearchDuckDuckGo}: \"$urlInput\""
                                    }
                                }
                                suggestion.startsWith("http") || suggestion.contains(".") -> {
                                    suggestion
                                }
                                else -> suggestion
                            }
                            Text(
                                text = displayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (suggestion == "Search on DuckDuckGo" || suggestion.contains("DuckDuckGo")) {
                                    Icons.Default.Search
                                } else {
                                    Icons.Default.Language
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSuggestionClick(suggestion)
                            }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}