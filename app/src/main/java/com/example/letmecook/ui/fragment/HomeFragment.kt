package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.adapter.RecipesAdapter
import com.example.letmecook.databinding.FragmentHomeBinding
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.ui.activity.RecipeDetailsActivity
import com.example.letmecook.viewmodel.BookmarkViewModel
import com.example.letmecook.viewmodel.RecipeViewModel
import com.example.letmecook.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModels and Adapter
    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }
    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private val userViewModel: UserViewModel by lazy { UserViewModel(UserRepositoryImpl()) }
    private lateinit var recipeAdapter: RecipesAdapter

    // Data and State
    private var allRecipesList = listOf<Recipe>()
    private var bookmarkList = listOf<BookmarkModel>()
    private var currentFilter = "All"
    private var currentUserId: String? = null

    // Banner Carousel
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var currentBannerIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        setupRecipesList()
        setupFilterChips()
        setupSearch()
        setupViewModelObservers()
        loadInitialData()
    }

    override fun onResume() {
        super.onResume()
        startBannerCarousel()
    }

    override fun onPause() {
        super.onPause()
        stopBannerCarousel()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateAndFilterRecipes()
            }
        })
    }

    private fun setupRecipesList() {
        recipeAdapter = RecipesAdapter(
            emptyList(),
            onRecipeClick = { recipe -> navigateToRecipeDetail(recipe) },
            onBookMarkClick = { recipe -> handleBookmark(recipe) },
            onGetAuthorName = { userId, callback ->
                userViewModel.getDataFromDatabase(userId) { user ->
                    callback(user?.fullName ?: "Unknown")
                }
            }
        )
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recipeAdapter
        }
    }

    private fun setupFilterChips() {
        binding.categoryChipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chipAll -> "All"
                R.id.chipVeg -> "Vegetarian"
                R.id.chipNonVeg -> "Non-Vegetarian"
                else -> "All"
            }
            updateAndFilterRecipes()
        }
    }

    private fun loadInitialData() {
        binding.progressBar.visibility = View.VISIBLE
        recipeViewModel.getAllRecipes()
        currentUserId?.let { bookmarkViewModel.listenForUserBookmarks(it) }
    }

    private fun setupViewModelObservers() {
        recipeViewModel.recipeData.observe(viewLifecycleOwner, Observer { recipes ->
            allRecipesList = recipes
            updateAndFilterRecipes()
            if (recipes.isNotEmpty() && bannerRunnable == null) {
                startBannerCarousel()
            }
        })
        bookmarkViewModel.userBookmarks.observe(viewLifecycleOwner, Observer { bookmarks ->
            bookmarkList = bookmarks
            updateAndFilterRecipes()
        })
    }

    private fun startBannerCarousel() {
        stopBannerCarousel()
        if (allRecipesList.isNotEmpty() && isAdded) {
            bannerRunnable = Runnable {
                updateBannerView()
                bannerHandler.postDelayed(bannerRunnable!!, 8000)
            }
            bannerHandler.post(bannerRunnable!!)
        }
    }

    private fun stopBannerCarousel() {
        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }
        bannerRunnable = null
    }

    private fun updateBannerView() {
        if (allRecipesList.isEmpty() || !isAdded || context == null) return

        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                if (!isAdded) return

                currentBannerIndex = (currentBannerIndex + 1) % allRecipesList.size
                val recipe = allRecipesList[currentBannerIndex]

                binding.bannerRecipeTitle.text = recipe.title
                userViewModel.getDataFromDatabase(recipe.creatorId) { user ->
                    if(isAdded) binding.bannerRecipeAuthor.text = "by ${user?.fullName ?: "Unknown"}"
                }
                Glide.with(this@HomeFragment)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.recipe_preview)
                    .into(binding.bannerRecipeImage)

                binding.bannerRecipeImage.startAnimation(fadeIn)
                binding.bannerRecipeTitle.startAnimation(fadeIn)
                binding.bannerRecipeAuthor.startAnimation(fadeIn)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        binding.bannerRecipeImage.startAnimation(fadeOut)
        binding.bannerRecipeTitle.startAnimation(fadeOut)
        binding.bannerRecipeAuthor.startAnimation(fadeOut)
    }

    private fun updateAndFilterRecipes() {
        if (!isAdded) return

        val bookmarkedRecipeIds = bookmarkList.map { it.recipeId }.toSet()
        val updatedRecipes = allRecipesList.map { recipe ->
            recipe.copy(isBookmarked = bookmarkedRecipeIds.contains(recipe.id))
        }

        val categorizedList = if (currentFilter == "All") updatedRecipes else updatedRecipes.filter { it.category == currentFilter }
        val searchQuery = binding.searchEditText.text.toString()
        val finalList = if (searchQuery.isBlank()) categorizedList else categorizedList.filter { it.title.contains(searchQuery, ignoreCase = true) }

        recipeAdapter.updateRecipes(finalList)
        binding.progressBar.visibility = View.GONE
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    private fun handleBookmark(recipe: Recipe) {
        if (currentUserId == null) {
            Toast.makeText(context, "Please login to save recipes", Toast.LENGTH_SHORT).show()
            return
        }
        recipe.isBookmarked = true
        recipeAdapter.notifyDataSetChanged()
        val bookmark = BookmarkModel(recipeId = recipe.id, userId = currentUserId!!)
        bookmarkViewModel.createBookmark(bookmark) { success, message, _ ->
            if (success) {
                Toast.makeText(context, "Recipe Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBannerCarousel()
        _binding = null
    }
}