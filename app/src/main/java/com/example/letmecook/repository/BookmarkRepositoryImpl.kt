package com.example.letmecook.repository

import com.example.letmecook.model.BookmarkModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookmarkRepositoryImpl : BookmarkRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("bookmarks")

    override fun createBookmark(
        bookmark: BookmarkModel,
        callback: (Boolean, String, String) -> Unit
    ) {
        val bookmarkId = reference.push().key ?: return
        bookmark.id = bookmarkId
        reference.child(bookmarkId).setValue(bookmark).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Bookmark created successfully", bookmarkId)
            } else {
                callback(false, it.exception?.message ?: "Failed to create bookmark", "")
            }
        }
    }

    override fun getBookmark(
        bookmarkId: String,
        callback: (BookmarkModel?, Boolean, String) -> Unit
    ) {
        reference.child(bookmarkId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmark = snapshot.getValue(BookmarkModel::class.java)
                if (bookmark != null) {
                    callback(bookmark, true, "Bookmark fetched successfully")
                } else {
                    callback(null, false, "Bookmark not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun updateBookmark(
        bookmarkId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(bookmarkId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Bookmark updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to update bookmark")
            }
        }
    }

    override fun deleteBookmark(bookmarkId: String, callback: (Boolean, String) -> Unit) {
        reference.child(bookmarkId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Bookmark deleted successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete bookmark")
            }
        }
    }

    override fun getUserBookmarks(
        userId: String,
        callback: (List<BookmarkModel>, Boolean, String) -> Unit
    ) {
        reference.orderByChild("userId").equalTo(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmarks =
                    snapshot.children.mapNotNull { it.getValue(BookmarkModel::class.java) }
                callback(bookmarks, true, "User bookmarks fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun getRecipeBookmarks(
        recipeId: String,
        callback: (List<BookmarkModel>, Boolean, String) -> Unit
    ) {
        reference.orderByChild("recipeId").equalTo(recipeId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmarks =
                    snapshot.children.mapNotNull { it.getValue(BookmarkModel::class.java) }
                callback(bookmarks, true, "Recipe bookmarks fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun findBookmarkByUserAndRecipe(userId: String, recipeId: String, callback: (BookmarkModel?) -> Unit) {
        reference.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundBookmark: BookmarkModel? = null
                    for (bookmarkSnapshot in snapshot.children) {
                        val bookmark = bookmarkSnapshot.getValue(BookmarkModel::class.java)
                        if (bookmark?.recipeId == recipeId) {
                            foundBookmark = bookmark
                            break
                        }
                    }
                    callback(foundBookmark)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
}