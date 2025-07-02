package com.example.letmecook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.repository.BookmarkRepositoryImpl

class BookmarkViewModel(
    private val repo: BookmarkRepositoryImpl = BookmarkRepositoryImpl()
) : ViewModel() {

    private val _userBookmarks = MutableLiveData<List<BookmarkModel>>()
    val userBookmarks: LiveData<List<BookmarkModel>> get() = _userBookmarks

    fun listenForUserBookmarks(userId: String) {
        repo.getUserBookmarks(userId) { bookmarks, success, _ ->
            if (success) {
                _userBookmarks.postValue(bookmarks)
            } else {
                _userBookmarks.postValue(emptyList())
            }
        }
    }

    fun createBookmark(bookmark: BookmarkModel, callback: (Boolean, String, String) -> Unit) {
        repo.createBookmark(bookmark, callback)
    }

    fun deleteBookmark(bookmarkId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteBookmark(bookmarkId, callback)
    }

    fun findBookmarkByUserAndRecipe(userId: String, recipeId: String, callback: (BookmarkModel?) -> Unit) {
        repo.findBookmarkByUserAndRecipe(userId, recipeId, callback)
    }
}