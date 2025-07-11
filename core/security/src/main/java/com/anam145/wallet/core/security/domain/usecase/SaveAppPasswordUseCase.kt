package com.anam145.wallet.core.security.domain.usecase

import com.anam145.wallet.core.security.data.util.ScryptConstants
import com.anam145.wallet.core.security.domain.repository.SecurityRepository
import com.anam145.wallet.core.security.model.ScryptParams
import com.lambdaworks.crypto.SCrypt
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.inject.Inject

/**
 * 앱 비밀번호 저장 UseCase
 * 사용자의 앱 접근 비밀번호를 안전하게 해싱하여 저장합니다.
 */
class SaveAppPasswordUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    
    /**
     * 앱 비밀번호 저장
     * 
     * @param password 사용자 비밀번호
     * @return 저장 성공 여부
     */
    suspend operator fun invoke(password: String): Result<Unit> = runCatching {
        // Salt 생성
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        
        // SCrypt로 해시 생성
        val hash = SCrypt.scrypt(
            password.toByteArray(StandardCharsets.UTF_8),
            salt,
            ScryptConstants.N, 
            ScryptConstants.R, 
            ScryptConstants.P, 
            ScryptConstants.DKLEN
        )
        
        // Repository를 통해 저장
        val scryptParams = ScryptParams(ScryptConstants.N, ScryptConstants.R, ScryptConstants.P)
        
        securityRepository.savePasswordHash(
            passwordHash = hash,
            salt = salt,
            scryptParams = scryptParams
        ).getOrThrow()
    }
}