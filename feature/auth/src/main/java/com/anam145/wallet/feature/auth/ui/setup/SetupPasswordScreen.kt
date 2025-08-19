package com.anam145.wallet.feature.auth.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.core.ui.theme.AnamSuccess
import com.anam145.wallet.core.ui.theme.AnamError
import com.anam145.wallet.core.ui.theme.AnamWarning
import com.anam145.wallet.core.ui.theme.Typography
import com.anam145.wallet.core.ui.theme.CocogooseFamily
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.anam145.wallet.feature.auth.R as AuthR
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.feature.auth.domain.model.AuthError

/**
 * AuthError를 문자열로 변환
 */
@Composable
private fun AuthError.toMessage(): String {
    val strings = LocalStrings.current
    return when (this) {
        AuthError.PasswordTooShort -> strings.authErrorPasswordTooShort
        AuthError.PasswordMismatch -> strings.authErrorPasswordMismatch
        AuthError.LoginFailed -> strings.authErrorLoginFailed
        AuthError.PasswordSetupFailed -> strings.authErrorPasswordSetupFailed
        // AuthError.DIDCreationFailed -> strings.authErrorDIDCreationFailed  // DID 기능 비활성화
        else -> strings.authErrorPasswordSetupFailed  // 기본 에러 메시지
    }
}

/**
 * 비밀번호 설정 화면
 */
@Composable
fun SetupPasswordScreen(
    onNavigateToMain: () -> Unit,
    viewModel: SetupPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SetupPasswordContract.Effect.NavigateToMain -> onNavigateToMain()
                is SetupPasswordContract.Effect.ShowToast -> {
                    // Toast 처리
                }
            }
        }
    }
    
    SetupPasswordContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupPasswordContent(
    uiState: SetupPasswordContract.State,
    onIntent: (SetupPasswordContract.Intent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val strings = LocalStrings.current
    
    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // 앱 로고
            Image(
                painter = painterResource(id = AuthR.drawable.logo),
                contentDescription = "ANAM Logo",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 타이틀
            Text(
                text = strings.setupPasswordTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 설명
            Text(
                text = strings.setupPasswordDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 비밀번호 입력
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onIntent(SetupPasswordContract.Intent.UpdatePassword(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                label = { Text(strings.setupPasswordLabel) },
                placeholder = { Text(strings.setupPasswordPlaceholder) },
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { onIntent(SetupPasswordContract.Intent.TogglePasswordVisibility) }
                    ) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (uiState.isPasswordVisible) {
                                strings.loginPasswordHide
                            } else {
                                strings.loginPasswordShow
                            }
                        )
                    }
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    AnimatedVisibility(
                        visible = uiState.passwordError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = uiState.passwordError?.toMessage() ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium
            )
            
            // 비밀번호 강도 표시
            if (uiState.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(
                    strength = uiState.passwordStrength,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 비밀번호 확인
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { onIntent(SetupPasswordContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.setupPasswordConfirmLabel) },
                placeholder = { Text(strings.setupPasswordConfirmPlaceholder) },
                visualTransformation = if (uiState.isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onIntent(SetupPasswordContract.Intent.SetupPassword)
                    }
                ),
                trailingIcon = {
                    Row {
                        // 일치 여부 표시
                        if (uiState.confirmPassword.isNotEmpty() && 
                            uiState.password == uiState.confirmPassword) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = strings.setupPasswordMatch,
                                tint = AnamSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { onIntent(SetupPasswordContract.Intent.ToggleConfirmPasswordVisibility) }
                        ) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (uiState.isConfirmPasswordVisible) {
                                    strings.loginPasswordHide
                                } else {
                                    strings.loginPasswordShow
                                }
                            )
                        }
                    }
                },
                isError = uiState.confirmPasswordError != null,
                supportingText = {
                    AnimatedVisibility(
                        visible = uiState.confirmPasswordError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = uiState.confirmPasswordError?.toMessage() ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium
            )
            
            // 에러 메시지
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error?.toMessage() ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 설정 버튼
            Button(
                onClick = { onIntent(SetupPasswordContract.Intent.SetupPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.password.isNotEmpty() && 
                         uiState.confirmPassword.isNotEmpty() && 
                         !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = strings.setupPasswordButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 주의사항
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = strings.setupPasswordWarningTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.setupPasswordWarningContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(
    strength: SetupPasswordContract.PasswordStrength,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when (strength) {
            SetupPasswordContract.PasswordStrength.NONE -> Color.Transparent
            SetupPasswordContract.PasswordStrength.WEAK -> AnamError
            SetupPasswordContract.PasswordStrength.MEDIUM -> AnamWarning
            SetupPasswordContract.PasswordStrength.STRONG -> AnamSuccess
        },
        label = "strength_color"
    )
    
    val strings = LocalStrings.current
    val text = when (strength) {
        SetupPasswordContract.PasswordStrength.NONE -> ""
        SetupPasswordContract.PasswordStrength.WEAK -> strings.setupPasswordStrengthWeak
        SetupPasswordContract.PasswordStrength.MEDIUM -> strings.setupPasswordStrengthMedium
        SetupPasswordContract.PasswordStrength.STRONG -> strings.setupPasswordStrengthStrong
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 강도 바
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .padding(horizontal = 2.dp)
            ) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (index < when (strength) {
                        SetupPasswordContract.PasswordStrength.NONE -> 0
                        SetupPasswordContract.PasswordStrength.WEAK -> 1
                        SetupPasswordContract.PasswordStrength.MEDIUM -> 2
                        SetupPasswordContract.PasswordStrength.STRONG -> 3
                    }) color else MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 강도 텍스트
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

