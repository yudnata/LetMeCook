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
    private var commentList: List<CommentModel>,
    private val currentUserId: String?,
    private val onEditClick: (CommentModel) -> Unit,
    private val onDeleteClick: (CommentModel) -> Unit
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
            ratingBar.rating = comment.rating
            commentText.text = comment.comment

            // --- UBAH BAGIAN INI ---
            if (comment.userAvatar.isNotEmpty()) {
                Picasso.get()
                    .load(comment.userAvatar)
                    .placeholder(R.drawable.placeholder_image) // Gunakan placeholder baru
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.placeholder_image) // Gunakan placeholder baru
            }
            // --- AKHIR PERUBAHAN ---

            val displayTimestamp = comment.updateTimestamp ?: comment.timestamp
            val timeAgo = DateUtils.getRelativeTimeSpanString(displayTimestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            commentDate.text = timeAgo

            editedLabel.visibility = if (comment.edited) View.VISIBLE else View.GONE

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
        commentList = newComments
        notifyDataSetChanged()
    }
}