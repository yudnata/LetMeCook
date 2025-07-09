package com.example.letmecook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.letmecook.model.CommentModel
import com.example.letmecook.repository.CommentRepository
import com.example.letmecook.repository.CommentRepositoryImpl

class CommentViewModel(private val repository: CommentRepository = CommentRepositoryImpl()) : ViewModel() {

    private val _comments = MutableLiveData<List<CommentModel>>()
    val comments: LiveData<List<CommentModel>> get() = _comments

    fun addComment(comment: CommentModel, callback: (Boolean, String) -> Unit) {
        repository.addComment(comment, callback)
    }

    fun getComments(recipeId: String) {
        repository.getComments(recipeId) { commentList, success, message ->
            if (success) {
                _comments.postValue(commentList)
            } else {
            }
        }
    }

    fun deleteComment(commentId: String, callback: (Boolean, String) -> Unit) {
        repository.deleteComment(commentId, callback)
    }

    fun updateComment(commentId: String, newCommentText: String, newRating: Float, callback: (Boolean, String) -> Unit) {
        repository.updateComment(commentId, newCommentText, newRating, callback)
    }

    fun addReply(reply: CommentModel, callback: (Boolean, String) -> Unit) {
        repository.addReply(reply, callback)
    }
}