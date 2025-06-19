package com.example.letmecook.viewmodel

import androidx.lifecycle.ViewModel
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.repository.BookmarkRepositoryImpl

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
