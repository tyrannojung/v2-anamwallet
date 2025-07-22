package com.anam145.wallet.feature.auth.domain.model

/**
 * Auth 관련 에러 타입
 */
sealed class AuthError : Exception() {
    object PasswordTooShort : AuthError()
    object PasswordMismatch : AuthError()
    object LoginFailed : AuthError()
    object PasswordSetupFailed : AuthError()
}