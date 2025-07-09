package com.example.letmecook.ui.activity

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.adapter.CommentAdapter
import com.example.letmecook.databinding.ActivityRecipeDetailsBinding
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.model.CommentModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.BookmarkViewModel
import com.example.letmecook.viewmodel.CommentViewModel
import com.example.letmecook.viewmodel.RecipeViewModel
import com.example.letmecook.viewmodel.RecipeViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding

    private val recipeViewModel: RecipeViewModel by lazy {
        ViewModelProvider(this, RecipeViewModelFactory(RecipeRepositoryImpl())).get(RecipeViewModel::class.java)
    }

    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private val commentViewModel: CommentViewModel by lazy { CommentViewModel() }
    private lateinit var commentAdapter: CommentAdapter

    private var recipeId: String = ""
    private var currentRecipe: Recipe? = null
    private lateinit var loader: LoadingUtils
    private var currentUserId: String? = null
    private var isBookmarked = false
    private var userBookmarks: List<BookmarkModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        loader = LoadingUtils(this)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        recipeId = intent.getStringExtra("RECIPE_ID") ?: ""
        if (recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupRecyclerView()
        loadRecipeDetails()
        observeBookmarkChanges()
        observeComments()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.collapsingToolbar.title = " "
        binding.backButton.setOnClickListener { finish() }
        binding.shareButton.setOnClickListener { Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show() }

        binding.bookmarkIconButton.setOnClickListener {
            handleBookmark()
        }

        binding.bookmarkbutton.setOnClickListener {
            handleBookmark()
        }
        binding.submitCommentButton.setOnClickListener {
            handleCommentSubmission()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Overview
                        binding.overviewContent.visibility = View.VISIBLE
                        binding.reviewsContent.visibility = View.GONE
                    }
                    1 -> { // Reviews
                        binding.overviewContent.visibility = View.GONE
                        binding.reviewsContent.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(
            emptyList(),
            currentUserId,
            onEditClick = { comment -> handleCommentEditing(comment) },
            onDeleteClick = { comment -> handleCommentDeletion(comment) }
        )
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = commentAdapter
        }
    }

    private fun updateBookmarkState(isBookmarked: Boolean) {
        this.isBookmarked = isBookmarked
        if (isBookmarked) {
            binding.bookmarkbutton.text = "Recipe Saved"
            binding.bookmarkbutton.isEnabled = false
            // --- PERUBAHAN DI SINI ---
            binding.bookmarkIconButton.setImageResource(R.drawable.ic_favorite_red_24) // Gunakan ikon merah
        } else {
            binding.bookmarkbutton.text = "Save this recipe"
            binding.bookmarkbutton.isEnabled = true
            binding.bookmarkIconButton.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    private fun observeBookmarkChanges() {
        val userId = currentUserId
        if (userId != null) {
            bookmarkViewModel.listenForUserBookmarks(userId)
            bookmarkViewModel.userBookmarks.observe(this, Observer { bookmarks ->
                this.userBookmarks = bookmarks
                val bookmarked = bookmarks.any { it.recipeId == recipeId }
                updateBookmarkState(bookmarked)
            })
        }
    }

    private fun observeComments() {
        commentViewModel.getComments(recipeId)
        commentViewModel.comments.observe(this, Observer { comments ->
            commentAdapter.updateComments(comments)
            updateRating(comments)
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

    private fun loadRecipeDetails() {
        loader.show()
        recipeViewModel.getRecipe(recipeId) { recipe, success, message ->
            loader.dismiss()
            if (success && recipe != null) {
                currentRecipe = recipe
                displayRecipeDetails(recipe)
            } else {
                Toast.makeText(this, "Failed to load recipe: $message", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(binding.recipeImage)

        binding.recipeTitle.text = recipe.title
        binding.recipeCategory.text = recipe.category
        binding.recipeCuisine.text = recipe.cuisine
        binding.recipeDuration.text = recipe.duration
        binding.recipeProteins.text = recipe.proteins
        binding.recipeFats.text = recipe.fats
        binding.recipeCarbs.text = recipe.carbs

        binding.recipeDescription.text = recipe.description
        binding.recipeIngredients.text = recipe.ingredients

        val htmlSteps = recipe.process.split("\n").joinToString("") { step ->
            "<p style='margin-bottom: 15px;'>$step</p>"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.recipeProcess.text = Html.fromHtml(htmlSteps, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.recipeProcess.text = Html.fromHtml(htmlSteps)
        }

        binding.recipeHalalStatus.text = recipe.halalStatus
        if (recipe.halalStatus.equals("Halal", ignoreCase = true)) {
            binding.recipeHalalStatus.background = ContextCompat.getDrawable(this, R.drawable.badge_halal_bg)
        } else {
            binding.recipeHalalStatus.background = ContextCompat.getDrawable(this, R.drawable.badge_non_halal_bg)
        }

        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun updateRating(comments: List<CommentModel>) {
        if (comments.isNotEmpty()) {
            val totalRating = comments.sumOf { it.rating.toDouble() }.toFloat()
            val averageRating = totalRating / comments.size
            val ratingCount = comments.size

            val decimalFormat = DecimalFormat("#.#")
            binding.ratingBar.rating = averageRating
            binding.ratingCount.text = "(${decimalFormat.format(averageRating)} from $ratingCount ratings)"

            val updateData = mutableMapOf<String, Any>(
                "averageRating" to averageRating,
                "totalRatings" to ratingCount
            )
            recipeViewModel.updateRecipe(recipeId, updateData) { _, _ -> }

        } else {
            binding.ratingBar.rating = 0f
            binding.ratingCount.text = "(No ratings yet)"
        }
    }

    private fun handleBookmark() {
        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(this, "Please login to bookmark this recipe", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentRecipe == null) {
            Toast.makeText(this, "Recipe data is not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (isBookmarked) {
            val bookmarkToDelete = userBookmarks.find { it.recipeId == recipeId }

            if (bookmarkToDelete != null) {
                loader.show()
                bookmarkViewModel.deleteBookmark(bookmarkToDelete.id) { success, message ->
                    loader.dismiss()
                    if (success) {
                        Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to remove bookmark: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            loader.show()
            val newBookmark = BookmarkModel(recipeId = recipeId, userId = userId)
            bookmarkViewModel.createBookmark(newBookmark) { success, message, _ ->
                loader.dismiss()
                if (success) {
                    Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save recipe: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleCommentSubmission() {
        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(this, "Please login to leave a review", Toast.LENGTH_SHORT).show()
            return
        }

        val commentText = binding.commentEditText.text.toString().trim()
        val rating = binding.addRatingBar.rating

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        if (rating == 0.0f) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                binding.commentEditText.text?.clear()
                binding.addRatingBar.rating = 0f
            } else {
                Toast.makeText(this, "Failed to submit review: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCommentDeletion(comment: CommentModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                loader.show()
                commentViewModel.deleteComment(comment.id) { success, message ->
                    loader.dismiss()
                    if (success) {
                        Toast.makeText(this, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete comment: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun handleCommentEditing(comment: CommentModel) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_comment, null)
        val editText = dialogView.findViewById<EditText>(R.id.editCommentEditText)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.editCommentRatingBar)

        editText.setText(comment.comment)
        ratingBar.rating = comment.rating

        AlertDialog.Builder(this)
            .setTitle("Edit Review")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newCommentText = editText.text.toString().trim()
                val newRating = ratingBar.rating

                if (newCommentText.isNotEmpty() && newRating > 0) {
                    loader.show()
                    commentViewModel.updateComment(comment.id, newCommentText, newRating) { success, message ->
                        loader.dismiss()
                        if (success) {
                            Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update review: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please provide a rating and comment.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}