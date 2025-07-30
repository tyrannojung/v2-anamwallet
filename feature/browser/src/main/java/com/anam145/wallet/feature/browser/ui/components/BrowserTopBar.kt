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
    onUrlInputChange: (String) -> Unit,
    onUrlSubmit: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onShowUrlBar: () -> Unit,
    onHideUrlBar: () -> Unit,
    onSuggestionClick: (String) -> Unit
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onHideUrlBar) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Cancel"
                                )
                            }
                            
                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = onUrlInputChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = { Text(com.anam145.wallet.core.ui.language.LocalStrings.current.browserUrlPlaceholder) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        val finalUrl = if (urlInput.startsWith("http://") || urlInput.startsWith("https://")) {
                                            urlInput
                                        } else if (urlInput.contains(".")) {
                                            "https://$urlInput"
                                        } else {
                                            "https://duckduckgo.com/?q=$urlInput"
                                        }
                                        onUrlSubmit(finalUrl)
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
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
                            Text(
                                text = suggestion,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            onSuggestionClick(suggestion)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}