package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.model.Bookmark
import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import javax.inject.Inject

/**
 * 북마크를 추가하는 UseCase
 */
class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(title: String, url: String) {
        val bookmark = Bookmark(
            title = title,
            url = url
        )
        bookmarkRepository.addBookmark(bookmark)
    }
}