package com.example.letmecook.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.model.Recipe
import com.google.android.material.button.MaterialButton

class RecipesAdapter(
    private var allRecipes: List<Recipe> = emptyList(),
    private val onRecipeClick: (Recipe) -> Unit,
    private val onBookMarkClick: (Recipe) -> Unit,
    private val onGetAuthorName: (String, (String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private var filteredRecipes: List<Recipe> = allRecipes

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
        val categoryBadge: TextView = view.findViewById(R.id.categoryBadge)
        val recipeTitle: TextView = view.findViewById(R.id.recipeTitle)
        val authorName: TextView = view.findViewById(R.id.authorName)
        val recipeProtein: TextView = view.findViewById(R.id.recipeProtein)
        val recipeDuration: TextView = view.findViewById(R.id.recipeDuration)
        val recipeCarbs: TextView = view.findViewById(R.id.recipeCarbs)
        val recipeFats: TextView = view.findViewById(R.id.recipeFats)
        val bookmarkButton: MaterialButton = view.findViewById(R.id.bookmarkButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = filteredRecipes[position]

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .into(holder.recipeImage)

        holder.categoryBadge.text = recipe.category
        holder.recipeTitle.text = recipe.title

        if (recipe.creatorId.isNotEmpty()) {
            onGetAuthorName(recipe.creatorId) { authorName ->
                holder.authorName.text = "by $authorName"
            }
        } else {
            holder.authorName.text = "by Unknown"
        }

        holder.recipeProtein.text = recipe.proteins
        holder.recipeDuration.text = recipe.duration
        holder.recipeCarbs.text = recipe.carbs
        holder.recipeFats.text = recipe.fats

        // --- LOGIKA UTAMA TOMBOL DI SINI ---
        if (recipe.isBookmarked) {
            holder.bookmarkButton.text = "Recipe Saved"
            holder.bookmarkButton.isEnabled = false
        } else {
            holder.bookmarkButton.text = "Save Recipe"
            holder.bookmarkButton.isEnabled = true
        }

        holder.itemView.setOnClickListener { onRecipeClick(recipe) }
        holder.bookmarkButton.setOnClickListener {
            if (holder.bookmarkButton.isEnabled) {
                onBookMarkClick(recipe)
            }
        }
    }

    override fun getItemCount() = filteredRecipes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateRecipes(newRecipes: List<Recipe>) {
        allRecipes = newRecipes
        filteredRecipes = newRecipes
        notifyDataSetChanged()
    }

    fun getAllRecipes(): List<Recipe> = allRecipes

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredRecipes(recipes: List<Recipe>) {
        filteredRecipes = recipes
        notifyDataSetChanged()
    }
}