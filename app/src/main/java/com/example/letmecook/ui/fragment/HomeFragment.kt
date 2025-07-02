package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
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
import com.example.letmecook.utils.LoadingUtils
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

    // Variabel untuk menyimpan daftar resep dan bookmark terkini
    private var recipeList = listOf<Recipe>()
    private var bookmarkList = listOf<BookmarkModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        setupBanner()
        setupRecipesList()
        setupFilterChips()
        setupViewModelObservers()
        loadInitialData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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
            onRecipeClick = { event -> navigateToRecipeDetail(event) },
            onBookMarkClick = { event -> handleBookmark(event) },
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
        recipeViewModel.getAllRecipes() // Memuat semua resep
        currentUserId?.let {
            bookmarkViewModel.listenForUserBookmarks(it) // Mulai mendengarkan bookmark
        }
    }

    private fun setupViewModelObservers() {
        // Observer untuk daftar resep
        recipeViewModel.recipeData.observe(viewLifecycleOwner, Observer { recipes ->
            this.recipeList = recipes
            updateAndFilterRecipes() // Gabungkan dengan data bookmark
        })

        // Observer untuk daftar bookmark
        bookmarkViewModel.userBookmarks.observe(viewLifecycleOwner, Observer { bookmarks ->
            this.bookmarkList = bookmarks
            updateAndFilterRecipes() // Gabungkan dengan data resep
        })
    }

    // --- FUNGSI BARU UNTUK MENGGABUNGKAN DATA ---
    private fun updateAndFilterRecipes() {
        if (recipeList.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            return
        }

        // Buat daftar ID resep yang sudah di-bookmark untuk pencarian cepat
        val bookmarkedRecipeIds = bookmarkList.map { it.recipeId }.toSet()

        // Perbarui status `isBookmarked` untuk setiap resep
        val updatedRecipes = recipeList.map { recipe ->
            recipe.isBookmarked = bookmarkedRecipeIds.contains(recipe.id)
            recipe
        }

        // Dapatkan nama penulis
        var authorFetchCount = 0
        updatedRecipes.forEach { recipe ->
            userViewModel.getDataFromDatabase(recipe.creatorId) { user ->
                recipe.creatorName = user?.fullName ?: "Unknown"
                authorFetchCount++
                if(authorFetchCount == updatedRecipes.size){
                    val filteredList = if (currentFilter == "All") {
                        updatedRecipes
                    } else {
                        updatedRecipes.filter { it.category == currentFilter }
                    }
                    recipeAdapter.setFilteredRecipes(filteredList)
                    binding.progressBar.visibility = View.GONE
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
        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(context, "Please login to save recipes", Toast.LENGTH_SHORT).show()
            return
        }

        // Nonaktifkan tombol di adapter secara sementara (feedback visual)
        recipe.isBookmarked = true
        recipeAdapter.notifyDataSetChanged()

        val bookmark = BookmarkModel(recipeId = recipe.id, userId = userId)
        bookmarkViewModel.createBookmark(bookmark) { success, message, _ ->
            if (success) {
                Toast.makeText(context, "Bookmark successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
                // Jika gagal, observer akan mengembalikan state tombol ke semula secara otomatis
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}