package com.anam145.wallet.feature.auth.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.R
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
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
        else -> strings.authErrorLoginFailed  // 기본 에러 메시지
    }
}

/**
 * 로그인 화면
 */
@Composable
fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginContract.Effect.NavigateToMain -> onNavigateToMain()
                is LoginContract.Effect.ShowToast -> {
                    // Toast 처리
                }
            }
        }
    }
    
    LoginContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    uiState: LoginContract.State,
    onIntent: (LoginContract.Intent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val strings = LocalStrings.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // 앱 로고/아이콘
            Image(
                painter = painterResource(id = AuthR.drawable.logo),
                contentDescription = "ANAM Logo",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 타이틀
            Text(
                text = strings.headerTitleMain,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 서브타이틀
            Text(
                text = strings.loginTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 비밀번호 입력 필드
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onIntent(LoginContract.Intent.UpdatePassword(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(strings.loginPasswordLabel) },
                placeholder = { Text(strings.loginPasswordPlaceholder) },
                visualTransformation = if (uiState.isPasswordVisible) {
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
                        onIntent(LoginContract.Intent.Login)
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { onIntent(LoginContract.Intent.TogglePasswordVisibility) }
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
                isError = uiState.error != null,
                supportingText = {
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = uiState.error?.toMessage() ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 로그인 버튼
            Button(
                onClick = { onIntent(LoginContract.Intent.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.password.isNotEmpty() && !uiState.isLoading,
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
                        text = strings.loginUnlockButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 하단 정보
            Text(
                text = strings.loginForgotPassword,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

