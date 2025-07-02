package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }
    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private val userViewModel: UserViewModel by lazy { UserViewModel(UserRepositoryImpl()) }
    private lateinit var recipeAdapter: RecipesAdapter
    private var currentFilter = "All"
    private var currentUserId: String? = null

    private var recipeList = listOf<Recipe>()
    private var bookmarkList = listOf<BookmarkModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        setupBanner()
        setupRecipesList()
        setupFilterChips()
        setupSearch() // Panggil fungsi setup untuk search
        setupViewModelObservers()
        loadInitialData()
    }

    // --- FUNGSI BARU UNTUK SETUP SEARCH ---
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateAndFilterRecipes() // Panggil fungsi filter setiap kali teks berubah
            }
        })
    }

    private fun setupBanner() {
        Glide.with(requireContext())
            .load("https://res.cloudinary.com/dnqet3vq1/image/upload/v1740833865/Card_lxxa6l.png")
            .into(binding.bannerImage)
        binding.bannerTitle.text = "Asian white noodle with extra seafood"
        binding.bannerDescription.text = "Exclusive recipe by Chef Juna!"
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
        currentUserId?.let {
            bookmarkViewModel.listenForUserBookmarks(it)
        }
    }

    private fun setupViewModelObservers() {
        recipeViewModel.recipeData.observe(viewLifecycleOwner, Observer { recipes ->
            this.recipeList = recipes
            updateAndFilterRecipes()
        })
        bookmarkViewModel.userBookmarks.observe(viewLifecycleOwner, Observer { bookmarks ->
            this.bookmarkList = bookmarks
            updateAndFilterRecipes()
        })
    }

    // --- FUNGSI FILTER DIPERBARUI UNTUK MENANGANI PENCARIAN ---
    private fun updateAndFilterRecipes() {
        if (view == null) return // Cek jika fragment view sudah dihancurkan

        // 1. Update status bookmark
        val bookmarkedRecipeIds = bookmarkList.map { it.recipeId }.toSet()
        val updatedRecipes = recipeList.map { recipe ->
            recipe.isBookmarked = bookmarkedRecipeIds.contains(recipe.id)
            recipe
        }

        // 2. Terapkan filter kategori
        val categorizedList = if (currentFilter == "All") {
            updatedRecipes
        } else {
            updatedRecipes.filter { it.category == currentFilter }
        }

        // 3. Terapkan filter pencarian
        val searchQuery = binding.searchEditText.text.toString()
        val finalList = if (searchQuery.isBlank()) {
            categorizedList
        } else {
            categorizedList.filter { recipe ->
                recipe.title.contains(searchQuery, ignoreCase = true)
            }
        }

        // 4. Update adapter dengan hasil akhir
        fetchAuthorsAndDisplay(finalList)
    }

    private fun fetchAuthorsAndDisplay(recipesToDisplay: List<Recipe>) {
        if (view == null) return

        if (recipesToDisplay.isEmpty()) {
            recipeAdapter.setFilteredRecipes(emptyList())
            binding.progressBar.visibility = View.GONE
            return
        }

        var authorFetchCount = 0
        recipesToDisplay.forEach { recipe ->
            userViewModel.getDataFromDatabase(recipe.creatorId) { user ->
                recipe.creatorName = user?.fullName ?: "Unknown"
                authorFetchCount++
                if(authorFetchCount == recipesToDisplay.size){
                    if (view != null) { // Cek lagi sebelum update UI
                        recipeAdapter.setFilteredRecipes(recipesToDisplay)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
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
            if (!success) {
                Toast.makeText(context, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Recipe Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}