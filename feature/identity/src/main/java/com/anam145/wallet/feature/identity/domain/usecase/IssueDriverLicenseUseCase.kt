package com.anam145.wallet.feature.identity.domain.usecase

import com.anam145.wallet.feature.identity.domain.model.VerifiableCredential
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import javax.inject.Inject

/**
 * 운전면허증 발급 UseCase
 */
class IssueDriverLicenseUseCase @Inject constructor(
    private val didRepository: DIDRepository
) {
    suspend operator fun invoke(): Result<VerifiableCredential> {
        return didRepository.issueDriverLicense()
    }
}