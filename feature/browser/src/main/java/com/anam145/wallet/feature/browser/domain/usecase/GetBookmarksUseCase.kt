package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.model.Bookmark
import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 북마크 목록을 가져오는 UseCase
 */
class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(): Flow<List<Bookmark>> = bookmarkRepository.getBookmarks()
}