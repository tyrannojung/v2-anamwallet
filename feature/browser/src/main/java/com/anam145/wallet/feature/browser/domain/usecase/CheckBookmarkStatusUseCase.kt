package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * URL의 북마크 상태를 확인하는 UseCase
 */
class CheckBookmarkStatusUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(url: String): Flow<Boolean> = bookmarkRepository.isBookmarked(url)
}