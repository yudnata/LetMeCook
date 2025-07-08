package com.example.letmecook.repository

import com.example.letmecook.model.CommentModel

interface CommentRepository {
    fun addComment(comment: CommentModel, callback: (Boolean, String) -> Unit)
    fun getComments(recipeId: String, callback: (List<CommentModel>, Boolean, String) -> Unit)
    fun deleteComment(commentId: String, callback: (Boolean, String) -> Unit)
    fun updateComment(commentId: String, newCommentText: String, newRating: Float, callback: (Boolean, String) -> Unit)
}