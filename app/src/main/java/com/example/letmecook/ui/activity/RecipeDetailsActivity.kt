package com.example.recipely.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipely.R
import com.example.recipely.databinding.ActivityRecipeDetailsBinding
import com.example.recipely.model.BookmarkModel
import com.example.recipely.model.Recipe
import com.example.recipely.repository.RecipeRepositoryImpl
import com.example.recipely.utils.LoadingUtils
import com.example.recipely.viewmodel.BookmarkViewModel
import com.example.recipely.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding
    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }
    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private var recipeId: String = ""
    private var currentRecipe: Recipe? = null
    private lateinit var loader: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = LoadingUtils(this)

        recipeId = intent.getStringExtra("RECIPE_ID") ?: ""
        if (recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        loadRecipeDetails()
    }

    private fun setupUI() {
        // Set up back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Set up favorite button
        binding.favoriteButton.setOnClickListener {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
        }

        binding.bookmarkbutton.setOnClickListener {
            currentRecipe?.let { recipe ->
                handleBookmark(recipe)
            }
        }

        // Set up share button
        binding.shareButton.setOnClickListener {
            Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show()
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
        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(binding.recipeImage)

        binding.recipeTitle.text = recipe.title
        binding.recipeCategory.text = recipe.category
        binding.recipeDuration.text = recipe.duration
        binding.recipeProteins.text = recipe.proteins
        binding.recipeFats.text = recipe.fats
        binding.recipeCarbs.text = recipe.carbs
        binding.recipeDescription.text = recipe.description
        binding.recipeProcess.text = recipe.process
        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun handleBookmark(recipe: Recipe) {
        loader.show()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            loader.dismiss()
            Toast.makeText(this, "Please login to bookmark this recipe", Toast.LENGTH_SHORT).show()
            return
        }

        val bookmark = BookmarkModel(
            recipeId = recipe.id,
            userId = userId,
        )

        bookmarkViewModel.createBookmark(bookmark) { success, message, _ ->
            loader.dismiss()

            if (success) {
                Toast.makeText(this, "Bookmark successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}