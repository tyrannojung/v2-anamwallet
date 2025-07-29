package com.anam145.wallet.feature.identity.domain.usecase

import com.anam145.wallet.feature.identity.domain.model.VerifiableCredential
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import javax.inject.Inject

/**
 * 학생증 발급 UseCase
 */
class IssueStudentCardUseCase @Inject constructor(
    private val didRepository: DIDRepository
) {
    suspend operator fun invoke(): Result<VerifiableCredential> {
        return didRepository.issueStudentCard()
    }
}