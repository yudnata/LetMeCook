package com.example.letmecook.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.adapter.RecipeDetailsViewPagerAdapter
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
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailsBinding

    private val recipeViewModel: RecipeViewModel by lazy {
        ViewModelProvider(this, RecipeViewModelFactory(RecipeRepositoryImpl())).get(RecipeViewModel::class.java)
    }

    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private val commentViewModel: CommentViewModel by lazy { CommentViewModel() }


    private var recipeId: String = ""
    private var currentRecipe: Recipe? = null
    private lateinit var loader: LoadingUtils
    private var currentUserId: String? = null

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
        loadRecipeDetails()
        observeBookmarkChanges()
        setupViewPager()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.collapsingToolbar.title = " " // Hapus judul dari toolbar
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

    private fun setupViewPager() {
        val adapter = RecipeDetailsViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Overview"
                1 -> "Reviews"
                else -> null
            }
        }.attach()
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
        // --- PERBAIKI BAGIAN INI ---
        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(binding.recipeImage)
        // --- AKHIR PERBAIKAN ---

        binding.recipeTitle.text = recipe.title
        binding.recipeCategory.text = recipe.category
        binding.recipeCuisine.text = recipe.cuisine
        binding.recipeDuration.text = recipe.duration
        binding.recipeProteins.text = recipe.proteins
        binding.recipeFats.text = recipe.fats
        binding.recipeCarbs.text = recipe.carbs


        binding.recipeHalalStatus.text = recipe.halalStatus
        if (recipe.halalStatus.equals("Halal", ignoreCase = true)) {
            binding.recipeHalalStatus.background = ContextCompat.getDrawable(this, R.drawable.badge_halal_bg)
        } else {
            binding.recipeHalalStatus.background = ContextCompat.getDrawable(this, R.drawable.badge_non_halal_bg)
        }
        commentViewModel.getComments(recipeId)
        commentViewModel.comments.observe(this, Observer { comments ->
            updateRating(comments)
        })

        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun updateRating(comments: List<CommentModel>) {
        if (comments.isNotEmpty()) {
            var totalRating = 0.0f
            comments.forEach { totalRating += it.rating }
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