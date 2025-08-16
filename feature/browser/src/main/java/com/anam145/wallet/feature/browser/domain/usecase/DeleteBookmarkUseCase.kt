package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import javax.inject.Inject

/**
 * 북마크를 삭제하는 UseCase
 */
class DeleteBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String) {
        bookmarkRepository.deleteBookmark(bookmarkId)
    }
}