package com.example.letmecook.model

data class CommentModel(
    var id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var comment: String = "",
    val rating: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    var edited: Boolean = false,
    var updateTimestamp: Long? = null,
    val parentId: String? = null,
    var parentUserName: String? = null // Menyimpan nama user yang dibalas
)