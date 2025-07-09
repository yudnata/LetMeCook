package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letmecook.R
import com.example.letmecook.adapter.MyRecipesAdapter
import com.example.letmecook.databinding.FragmentRecipesBinding
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.ui.activity.AddRecipeActivity
import com.example.letmecook.ui.activity.EditRecipeActivity
import com.example.letmecook.ui.activity.RecipeDetailsActivity
import com.example.letmecook.viewmodel.RecipeViewModel
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class RecipesFragment : Fragment() {
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private val recipeViewModel: RecipeViewModel by lazy {
        RecipeViewModel(RecipeRepositoryImpl())
    }
    private lateinit var myRecipesAdapter: MyRecipesAdapter
    private var currentFilter: String = "All"
    private var areAllCategoriesVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAdd.visibility = View.VISIBLE

        setupFab()
        setupRecyclerView()
        setupFilterChips()
        fetchUserRecipes()
        setupSeeAllButton()
    }

    private fun setupSeeAllButton() {
        binding.seeAllCategories.setOnClickListener {
            areAllCategoriesVisible = !areAllCategoriesVisible
            toggleCategoryVisibility()
        }
    }

    private fun toggleCategoryVisibility() {
        val visibility = if (areAllCategoriesVisible) View.VISIBLE else View.GONE
        binding.chipSnack.visibility = visibility
        binding.chipDrink.visibility = visibility
        binding.chipVegetarian.visibility = visibility
        binding.seeAllCategories.text = if (areAllCategoriesVisible) "See Less" else "See All"
    }

    private fun setupRecyclerView() {
        myRecipesAdapter = MyRecipesAdapter(
            recipeList = mutableListOf(),
            onEditClicked = { recipe -> launchUpdateRecipeActivity(recipe) },
            onDeleteClicked = { recipe -> deleteRecipe(recipe) },
            onRecipeClick = { event -> navigateToRecipeDetail(event) },
        )

        binding.recyclerViewMyRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myRecipesAdapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            Log.d("MyRecipesFragment", "FAB clicked! Opening AddRecipeActivity")
            startActivity(Intent(requireContext(), AddRecipeActivity::class.java))
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilter = when (checkedIds.first()) {
                    R.id.chipAll -> "All"
                    R.id.chipAppetizer -> "Appetizer"
                    R.id.chipMainCourse -> "Main Course"
                    R.id.chipDessert -> "Dessert"
                    R.id.chipSnack -> "Snack"
                    R.id.chipDrink -> "Drink"
                    R.id.chipVegetarian -> "Vegetarian"
                    else -> "All"
                }
            } else {
                currentFilter = "All"
                binding.chipAll.isChecked = true
            }
            filterRecipes()
        }
    }

    private fun filterRecipes() {
        val filteredRecipes = if (currentFilter == "All") {
            myRecipesAdapter.getAllRecipes()
        } else {
            myRecipesAdapter.getAllRecipes().filter { it.category == currentFilter }
        }
        myRecipesAdapter.setData(filteredRecipes)
        updateEmptyState(filteredRecipes.isEmpty())
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    private fun launchUpdateRecipeActivity(
        recipe: Recipe
    ) {
        val intent = Intent(requireContext(), EditRecipeActivity::class.java).apply {
            putExtra("RECIPE_ID", recipe.id)
            putExtra("RECIPE_TITLE", recipe.title)
            putExtra("RECIPE_DESCRIPTION", recipe.description)
            putExtra("RECIPE_PROCESS", recipe.process)
            putExtra("RECIPE_DURATION", recipe.duration)
            putExtra("RECIPE_CARBS", recipe.carbs)
            putExtra("RECIPE_PROTEINS", recipe.proteins)
            putExtra("RECIPE_FATS", recipe.fats)
            putExtra("RECIPE_CATEGORY", recipe.category)
            putExtra("RECIPE_CUISINE", recipe.cuisine)
            putExtra("RECIPE_HALAL_STATUS", recipe.halalStatus)
            putExtra("RECIPE_IMAGE_URL", recipe.imageUrl)
        }
        startActivity(intent)
    }

    private fun deleteRecipe(recipe: Recipe) {
        recipeViewModel.deleteRecipe(recipe.id) { success, message ->
            if (success) {
                Snackbar.make(binding.myRecipesRoot, "Recipe deleted", Snackbar.LENGTH_SHORT).show()
                fetchUserRecipes()
            } else {
                Snackbar.make(binding.myRecipesRoot, "Error: $message", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserRecipes()
    }

    private fun fetchUserRecipes() {
        val currentUser = userViewModel.getCurrentUser()
        if (currentUser == null) {
            Snackbar.make(binding.myRecipesRoot, "User not logged in", Snackbar.LENGTH_SHORT).show()
            return
        }

        recipeViewModel.getRecipesByUser(currentUser.uid) { recipes: List<Recipe>, success: Boolean, message: String ->
            if (success) {
                myRecipesAdapter.setAllRecipes(recipes)
                filterRecipes()
            } else {
                Snackbar.make(binding.myRecipesRoot, "Error: $message", Snackbar.LENGTH_SHORT)
                    .show()
                updateEmptyState(true)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewMyRecipes.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewMyRecipes.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}