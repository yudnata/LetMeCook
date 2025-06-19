package com.example.recipely.viewmodel

import androidx.lifecycle.ViewModel
import com.example.recipely.model.BookmarkModel
import com.example.recipely.repository.BookmarkRepositoryImpl

class BookmarkViewModel(
    private val repo: BookmarkRepositoryImpl = BookmarkRepositoryImpl()
) : ViewModel() {

    fun createBookmark(bookmark: BookmarkModel, callback: (Boolean, String, String) -> Unit) {
        repo.createBookmark(bookmark, callback)
    }

    fun deleteBookmark(bookmarkId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteBookmark(bookmarkId, callback)
    }

    fun getUserBookmarks(userId: String, callback: (List<BookmarkModel>, Boolean, String) -> Unit) {
        repo.getUserBookmarks(userId, callback)
    }
}
