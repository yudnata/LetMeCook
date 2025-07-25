package com.example.letmecook.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.letmecook.model.CommentModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepository

class RecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    private val _recipeData = MutableLiveData<List<Recipe>>()
    val recipeData: LiveData<List<Recipe>> get() = _recipeData

    private val _selectedRecipe = MutableLiveData<Recipe>()
    val selectedRecipe: LiveData<Recipe> get() = _selectedRecipe

    // --- TAMBAHKAN INI ---
    private val _comments = MutableLiveData<List<CommentModel>>()
    val comments: LiveData<List<CommentModel>> get() = _comments
    // --- AKHIR PENAMBAHAN ---

    fun createRecipe(recipe: Recipe, callback: (Boolean, String, String) -> Unit) {
        repo.createRecipe(recipe, callback)
    }

    fun getRecipe(recipeId: String, callback: (Recipe?, Boolean, String) -> Unit) {
        repo.getRecipe(recipeId) { recipe, success, message ->
            if (success && recipe != null) {
                _selectedRecipe.postValue(recipe)
            }
            callback(recipe, success, message)
        }
    }


    fun getAllRecipes() {
        repo.getAllRecipes { recipes, success, _ ->
            if (success) {
                _recipeData.value = recipes
            }
        }
    }

    fun updateRecipe(
        recipeId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateRecipe(recipeId, data, callback)
    }

    fun deleteRecipe(recipeId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteRecipe(recipeId, callback)
    }

    fun getRecipesByCategory(category: String) {
        repo.getRecipesByCategory(category) { recipes, success, _ ->
            if (success) {
                _recipeData.value = recipes
            }
        }
    }

    fun getRecipesByUser(userId: String, callback: (List<Recipe>, Boolean, String) -> Unit) {
        repo.getRecipesByUser(userId) { recipes, success, message ->
            if (success) {
                _recipeData.value = recipes
            }
            callback(recipes, success, message)
        }
    }

    fun uploadRecipeImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadRecipeImage(context, imageUri, callback)
    }
}