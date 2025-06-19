package com.example.letmecook.repository


import android.content.Context
import android.net.Uri
import com.example.letmecook.model.Recipe

interface RecipeRepository {
    fun createRecipe(recipe: Recipe, callback: (Boolean, String, String) -> Unit)
    fun getRecipe(recipeId: String, callback: (Recipe?, Boolean, String) -> Unit)
    fun updateRecipe(
        recipeId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )
    fun deleteRecipe(recipeId: String, callback: (Boolean, String) -> Unit)
    fun getAllRecipes(callback: (List<Recipe>, Boolean, String) -> Unit)
    fun getRecipesByCategory(category: String, callback: (List<Recipe>, Boolean, String) -> Unit)
    fun getRecipesByUser(userId: String, callback: (List<Recipe>, Boolean, String) -> Unit)
    fun uploadRecipeImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
}

