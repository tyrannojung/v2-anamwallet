package com.anam145.wallet.core.security.domain.usecase

import com.anam145.wallet.core.security.data.util.ScryptConstants
import com.anam145.wallet.core.security.domain.repository.SecurityRepository
import com.lambdaworks.crypto.SCrypt
import kotlinx.coroutines.flow.first
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * 앱 비밀번호 검증 UseCase
 * 입력된 비밀번호가 저장된 비밀번호와 일치하는지 확인합니다.
 */
class VerifyAppPasswordUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    
    /**
     * 앱 비밀번호 검증
     * 
     * @param inputPassword 입력된 비밀번호
     * @return 비밀번호가 맞으면 true
     */
    suspend operator fun invoke(inputPassword: String): Result<Boolean> = runCatching {
        // Repository에서 데이터 조회
        val storedHash = securityRepository.getPasswordHash() ?: return@runCatching false
        val salt = securityRepository.getSalt() ?: return@runCatching false
        val params = securityRepository.getScryptParams() ?: return@runCatching false
        
        // 입력된 비밀번호로 해시 재생성
        val inputHash = SCrypt.scrypt(
            inputPassword.toByteArray(StandardCharsets.UTF_8),
            salt,
            params.n,
            params.r,
            params.p,
            ScryptConstants.DKLEN
        )
        
        // 비교
        storedHash.contentEquals(inputHash)
    }
    
    /**
     * 저장된 비밀번호 존재 여부 확인
     */
    suspend fun hasPassword(): Boolean {
        return securityRepository.hasPassword().first()
    }
}