package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 북마크를 토글(추가/삭제)하는 UseCase
 */
class ToggleBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val addBookmarkUseCase: AddBookmarkUseCase
) {
    suspend operator fun invoke(title: String, url: String) {
        val isBookmarked = bookmarkRepository.isBookmarked(url).first()
        
        if (isBookmarked) {
            bookmarkRepository.deleteBookmarkByUrl(url)
        } else {
            addBookmarkUseCase(title, url)
        }
    }
}