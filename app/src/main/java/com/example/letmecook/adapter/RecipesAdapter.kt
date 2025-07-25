package com.example.letmecook.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letmecook.R
import com.example.letmecook.model.Recipe
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat

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
        val cuisineBadge: TextView = view.findViewById(R.id.cuisineBadge)
        val recipeTitle: TextView = view.findViewById(R.id.recipeTitle)
        val authorName: TextView = view.findViewById(R.id.authorName)
        val recipeProtein: TextView = view.findViewById(R.id.recipeProtein)
        val recipeDuration: TextView = view.findViewById(R.id.recipeDuration)
        val recipeCarbs: TextView = view.findViewById(R.id.recipeCarbs)
        val recipeFats: TextView = view.findViewById(R.id.recipeFats)
        val bookmarkButton: MaterialButton = view.findViewById(R.id.bookmarkButton)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val ratingCountText: TextView = view.findViewById(R.id.ratingCountText)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = filteredRecipes[position]

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.recipeImage)

        holder.categoryBadge.text = recipe.category
        holder.cuisineBadge.text = recipe.cuisine
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

        if (recipe.isBookmarked) {
            holder.bookmarkButton.text = "Recipe Saved"
            holder.bookmarkButton.isEnabled = false
        } else {
            holder.bookmarkButton.text = "Save Recipe"
            holder.bookmarkButton.isEnabled = true
        }

        val decimalFormat = DecimalFormat("#.#")
        holder.ratingBar.rating = recipe.averageRating
        if (recipe.totalRatings > 0) {
            holder.ratingCountText.text = "(${decimalFormat.format(recipe.averageRating)} from ${recipe.totalRatings} users)"
        } else {
            holder.ratingCountText.text = "(No ratings yet)"
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