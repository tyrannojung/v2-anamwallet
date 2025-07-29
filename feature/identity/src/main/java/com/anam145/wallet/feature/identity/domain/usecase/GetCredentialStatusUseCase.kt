package com.anam145.wallet.feature.identity.domain.usecase

import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 자격증명 발급 상태 조회 UseCase
 */
class GetCredentialStatusUseCase @Inject constructor(
    private val didRepository: DIDRepository
) {
    data class CredentialStatus(
        val isStudentCardIssued: Boolean,
        val isDriverLicenseIssued: Boolean
    )
    
    operator fun invoke(): Flow<CredentialStatus> {
        return combine(
            didRepository.isStudentCardIssued(),
            didRepository.isDriverLicenseIssued()
        ) { isStudentCardIssued, isDriverLicenseIssued ->
            CredentialStatus(
                isStudentCardIssued = isStudentCardIssued,
                isDriverLicenseIssued = isDriverLicenseIssued
            )
        }
    }
}