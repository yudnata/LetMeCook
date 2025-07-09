package com.example.letmecook.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letmecook.R
import com.example.letmecook.databinding.ItemCommentBinding
import com.example.letmecook.model.CommentModel
import com.squareup.picasso.Picasso

class CommentAdapter(
    private var commentList: MutableList<CommentModel> = mutableListOf(),
    private val currentUserId: String?,
    private val onEditClick: (CommentModel) -> Unit,
    private val onDeleteClick: (CommentModel) -> Unit,
    private val onReplyClick: (CommentModel) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding =
            ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        with(holder.binding) {
            userName.text = comment.userName
            commentText.text = comment.comment

            if (comment.parentId != null || comment.rating == 0f) {
                ratingBar.visibility = View.GONE
            } else {
                ratingBar.visibility = View.VISIBLE
                ratingBar.rating = comment.rating
            }

            if (comment.parentId != null) {
                replyIndentSpace.visibility = View.VISIBLE
                replyingToText.visibility = View.VISIBLE
                replyingToText.text = "Replying to ${comment.parentUserName}"
            } else {
                replyIndentSpace.visibility = View.GONE
                replyingToText.visibility = View.GONE
            }

            commentDivider.visibility = View.GONE

            if (position < commentList.size - 1) {
                val nextComment = commentList[position + 1]

                if (nextComment.parentId == null) {
                    commentDivider.visibility = View.VISIBLE
                }
            }
            // --- AKHIR PERBAIKAN ---

            if (comment.userAvatar.isNotEmpty()) {
                Picasso.get()
                    .load(comment.userAvatar)
                    .placeholder(R.drawable.placeholder_image)
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.placeholder_image)
            }

            val displayTimestamp = comment.updateTimestamp ?: comment.timestamp
            val timeAgo = DateUtils.getRelativeTimeSpanString(displayTimestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            commentDate.text = timeAgo

            editedLabel.visibility = if (comment.edited) View.VISIBLE else View.GONE

            replyButton.setOnClickListener { onReplyClick(comment) }

            if (comment.userId == currentUserId) {
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
                editButton.setOnClickListener { onEditClick(comment) }
                deleteButton.setOnClickListener { onDeleteClick(comment) }
            } else {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = commentList.size

    fun updateComments(newComments: List<CommentModel>) {
        val processedList = mutableListOf<CommentModel>()
        val commentMap = newComments.associateBy { it.id }


        val topLevelComments = newComments.filter { it.parentId == null }.sortedByDescending { it.timestamp }


        topLevelComments.forEach { parent ->
            processedList.add(parent)
            addReplies(parent, newComments, commentMap, processedList)
        }

        commentList.clear()
        commentList.addAll(processedList)
        notifyDataSetChanged()
    }


    private fun addReplies(
        parent: CommentModel,
        allComments: List<CommentModel>,
        commentMap: Map<String, CommentModel>,
        processedList: MutableList<CommentModel>
    ) {
        val replies = allComments.filter { it.parentId == parent.id }.sortedBy { it.timestamp }
        replies.forEach { reply ->
            reply.parentUserName = commentMap[reply.parentId!!]?.userName
            processedList.add(reply)
            addReplies(reply, allComments, commentMap, processedList)
        }
    }
}