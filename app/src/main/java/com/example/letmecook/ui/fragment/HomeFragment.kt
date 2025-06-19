package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.adapter.RecipesAdapter
import com.example.letmecook.databinding.FragmentHomeBinding
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.ui.activity.RecipeDetailsActivity
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.BookmarkViewModel
import com.example.letmecook.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }
    private val bookmarkViewModel: BookmarkViewModel by lazy { BookmarkViewModel() }
    private lateinit var recipeAdapter: RecipesAdapter
    private var currentFilter = "All"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner()
        setupRecipesList()
        setupFilterChips()
        loadRecipes()
        setupViewModelObservers()
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
            onBookMarkClick = { event -> handleBookmark(event) }
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
            filterRecipes()
        }
    }

    private fun loadRecipes() {
        binding.progressBar.visibility = View.VISIBLE
        recipeViewModel.getAllRecipes()
    }

    private fun setupViewModelObservers() {
        recipeViewModel.recipeData.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.updateRecipes(recipes)
            filterRecipes()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun filterRecipes() {
        val allRecipes = recipeAdapter.getAllRecipes()
        val filteredRecipes = if (currentFilter == "All") {
            allRecipes
        } else {
            allRecipes.filter { it.category == currentFilter }
        }
        recipeAdapter.setFilteredRecipes(filteredRecipes)
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    private fun handleBookmark(recipe: Recipe) {
        val loader = LoadingUtils(requireActivity())
        loader.show()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

        val bookmark = BookmarkModel(
            recipeId = recipe.id,
            userId = userId,
        )

        bookmarkViewModel.createBookmark(bookmark) { success, message, bookingId ->
            loader.dismiss()
            if (success) {
                Toast.makeText(context, "Bookmark successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Bookmark failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}