package com.example.letmecook.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letmecook.adapter.CommentAdapter
import com.example.letmecook.databinding.DialogEditCommentBinding
import com.example.letmecook.databinding.FragmentRecipeReviewsBinding
import com.example.letmecook.model.CommentModel
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.CommentViewModel
import com.google.firebase.auth.FirebaseAuth

class RecipeReviewsFragment : Fragment() {

    private var _binding: FragmentRecipeReviewsBinding? = null
    private val binding get() = _binding!!
    private val commentViewModel: CommentViewModel by lazy {
        ViewModelProvider(this).get(CommentViewModel::class.java)
    }
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var loader: LoadingUtils
    private var currentUserId: String? = null
    private var recipeId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = LoadingUtils(requireActivity())
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        recipeId = requireActivity().intent.getStringExtra("RECIPE_ID") ?: ""

        setupRecyclerView()
        observeComments()
        setupUI()
    }

    private fun setupUI() {
        binding.submitCommentButton.setOnClickListener {
            handleCommentSubmission()
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(
            emptyList(),
            currentUserId,
            onEditClick = { comment -> handleCommentEditing(comment) },
            onDeleteClick = { comment -> handleCommentDeletion(comment) }
        )
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }
    }

    private fun observeComments() {
        commentViewModel.getComments(recipeId)
        commentViewModel.comments.observe(viewLifecycleOwner, Observer { comments ->
            commentAdapter.updateComments(comments)
            val userHasCommented = comments.any { it.userId == currentUserId }
            updateCommentSectionVisibility(userHasCommented)
        })
    }
    private fun updateCommentSectionVisibility(hasCommented: Boolean) {
        if (hasCommented) {
            binding.addCommentSection.visibility = View.GONE
        } else {
            binding.addCommentSection.visibility = View.VISIBLE
        }
    }


    private fun handleCommentSubmission() {
        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login to leave a review", Toast.LENGTH_SHORT).show()
            return
        }

        val commentText = binding.commentEditText.text.toString().trim()
        val rating = binding.addRatingBar.rating

        if (commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        if (rating == 0.0f) {
            Toast.makeText(requireContext(), "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        loader.show()
        val newComment = CommentModel(
            recipeId = recipeId,
            userId = userId,
            comment = commentText,
            rating = rating,
            timestamp = System.currentTimeMillis()
        )

        commentViewModel.addComment(newComment) { success, message ->
            loader.dismiss()
            if (success) {
                Toast.makeText(requireContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                binding.commentEditText.text?.clear()
                binding.addRatingBar.rating = 0f
            } else {
                Toast.makeText(requireContext(), "Failed to submit review: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCommentDeletion(comment: CommentModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                loader.show()
                commentViewModel.deleteComment(comment.id) { success, message ->
                    loader.dismiss()
                    if (success) {
                        Toast.makeText(requireContext(), "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete comment: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCommentEditing(comment: CommentModel) {
        val dialogBinding = DialogEditCommentBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBinding.editCommentEditText.setText(comment.comment)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Comment")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val newCommentText = dialogBinding.editCommentEditText.text.toString().trim()
                if (newCommentText.isNotEmpty()) {
                    loader.show()
                    commentViewModel.updateComment(comment.id, newCommentText) { success, message ->
                        loader.dismiss()
                        if (success) {
                            Toast.makeText(requireContext(), "Comment updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update comment: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}