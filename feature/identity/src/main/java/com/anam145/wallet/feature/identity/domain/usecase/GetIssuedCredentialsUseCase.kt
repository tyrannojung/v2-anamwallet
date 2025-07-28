package com.anam145.wallet.feature.identity.domain.usecase

import com.anam145.wallet.feature.identity.domain.model.IssuedCredential
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 발급된 자격증명 목록 조회 UseCase
 */
class GetIssuedCredentialsUseCase @Inject constructor(
    private val didRepository: DIDRepository
) {
    operator fun invoke(): Flow<List<IssuedCredential>> {
        return didRepository.getIssuedCredentials()
    }
}