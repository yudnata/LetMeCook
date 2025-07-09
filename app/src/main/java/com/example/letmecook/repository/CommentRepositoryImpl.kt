package com.example.letmecook.repository

import com.example.letmecook.model.CommentModel
import com.google.firebase.database.*

class CommentRepositoryImpl : CommentRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("comments")
    private val userReference: DatabaseReference = database.reference.child("users")

    override fun addComment(comment: CommentModel, callback: (Boolean, String) -> Unit) {
        val commentId = reference.push().key ?: return
        comment.id = commentId
        reference.child(commentId).setValue(comment).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Comment added successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to add comment")
            }
        }
    }

    override fun getComments(
        recipeId: String,
        callback: (List<CommentModel>, Boolean, String) -> Unit
    ) {
        reference.orderByChild("recipeId").equalTo(recipeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<CommentModel>()
                    val userIds = mutableListOf<String>()

                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(CommentModel::class.java)
                        if (comment != null) {
                            comments.add(comment)
                            if (!userIds.contains(comment.userId)) {
                                userIds.add(comment.userId)
                            }
                        }
                    }

                    if (userIds.isEmpty()) {
                        callback(comments.sortedByDescending { it.timestamp }, true, "Comments fetched")
                        return
                    }

                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            for (comment in comments) {
                                val user = userSnapshot.child(comment.userId).getValue(com.example.letmecook.model.UserModel::class.java)
                                comment.userName = user?.fullName ?: "Unknown User"
                                comment.userAvatar = user?.imageUrl ?: ""
                            }
                            callback(comments.sortedByDescending { it.timestamp }, true, "Comments fetched successfully")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(emptyList(), false, "Failed to fetch user data for comments")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList(), false, error.message)
                }
            })
    }

    override fun deleteComment(commentId: String, callback: (Boolean, String) -> Unit) {
        reference.child(commentId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Comment deleted successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to delete comment")
            }
        }
    }

    override fun updateComment(commentId: String, newCommentText: String, newRating: Float, callback: (Boolean, String) -> Unit) {
        val updateData = mapOf(
            "comment" to newCommentText,
            "rating" to newRating,
            "edited" to true,
            "updateTimestamp" to System.currentTimeMillis()
        )
        reference.child(commentId).updateChildren(updateData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Comment updated successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to update comment")
            }
        }
    }

    override fun addReply(reply: CommentModel, callback: (Boolean, String) -> Unit) {
        val replyId = reference.push().key ?: return
        reply.id = replyId
        reference.child(replyId).setValue(reply).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Reply added successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to add reply")
            }
        }
    }
}