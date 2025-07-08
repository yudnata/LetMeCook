package com.example.letmecook.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityRecipeDetailsBinding
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.BookmarkViewModel
import com.example.letmecook.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding
    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }
    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private var recipeId: String = ""
    private var currentRecipe: Recipe? = null
    private lateinit var loader: LoadingUtils
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = LoadingUtils(this)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        recipeId = intent.getStringExtra("RECIPE_ID") ?: ""
        if (recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        loadRecipeDetails()
        observeBookmarkChanges()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }
        binding.shareButton.setOnClickListener { Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show() }
        binding.favoriteButton.setOnClickListener {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
        }
        binding.bookmarkbutton.setOnClickListener {
            handleBookmark()
        }
    }

    private fun setButtonToSavedState() {
        binding.bookmarkbutton.text = "Recipe Saved"
        binding.bookmarkbutton.isEnabled = false
    }

    private fun setButtonToNormalState() {
        binding.bookmarkbutton.text = "Save this recipe"
        binding.bookmarkbutton.isEnabled = true
    }

    private fun observeBookmarkChanges() {
        val userId = currentUserId
        if (userId != null) {
            bookmarkViewModel.listenForUserBookmarks(userId)
            bookmarkViewModel.userBookmarks.observe(this, Observer { bookmarks ->
                val isBookmarked = bookmarks.any { it.recipeId == recipeId }
                if (isBookmarked) {
                    setButtonToSavedState()
                } else {
                    setButtonToNormalState()
                }
            })
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
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        Glide.with(this).load(recipe.imageUrl).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(binding.recipeImage)
        binding.recipeTitle.text = recipe.title
        binding.recipeCategory.text = recipe.category
        binding.recipeCuisine.text = recipe.cuisine
        binding.recipeDuration.text = recipe.duration
        binding.recipeProteins.text = recipe.proteins
        binding.recipeFats.text = recipe.fats
        binding.recipeCarbs.text = recipe.carbs
        binding.recipeDescription.text = recipe.description
        binding.recipeProcess.text = recipe.process
        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun handleBookmark() {
        val userId = currentUserId
        val recipe = currentRecipe

        if (userId == null) {
            Toast.makeText(this, "Please login to bookmark this recipe", Toast.LENGTH_SHORT).show()
            return
        }

        if (recipe == null) {
            Toast.makeText(this, "Recipe data is not available", Toast.LENGTH_SHORT).show()
            return
        }

        binding.bookmarkbutton.isEnabled = false
        loader.show()

        val newBookmark = BookmarkModel(recipeId = recipe.id, userId = userId)
        bookmarkViewModel.createBookmark(newBookmark) { success, message, _ ->
            loader.dismiss()
            if (success) {
                Toast.makeText(this, "Bookmark successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}