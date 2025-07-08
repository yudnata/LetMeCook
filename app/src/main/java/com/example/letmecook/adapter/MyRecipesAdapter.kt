package com.example.letmecook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letmecook.R
import com.example.letmecook.databinding.ItemMyRecipeBinding
import com.example.letmecook.model.Recipe
import com.squareup.picasso.Picasso

class MyRecipesAdapter(
    private var recipeList: MutableList<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit,
    private val onEditClicked: (Recipe) -> Unit,
    private val onDeleteClicked: (Recipe) -> Unit
) : RecyclerView.Adapter<MyRecipesAdapter.MyRecipesViewHolder>() {

    private var allRecipes: List<Recipe> = listOf()

    inner class MyRecipesViewHolder(val binding: ItemMyRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecipesViewHolder {
        val binding =
            ItemMyRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyRecipesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyRecipesViewHolder, position: Int) {
        val recipe = recipeList[position]
        with(holder.binding) {
            if (recipe.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_image) // Ganti placeholder
                    .into(imgRecipe)
            } else {
                imgRecipe.setImageResource(R.drawable.placeholder_image) // Ganti placeholder
            }

            recipeName.text = recipe.title
            recipeCategory.text = recipe.category
            recipeProtein.text = recipe.proteins
            recipeCarbs.text = recipe.carbs
            recipeDuration.text = recipe.duration

            btnEdit.setOnClickListener { onEditClicked(recipe) }
            btnDelete.setOnClickListener { onDeleteClicked(recipe) }
            holder.itemView.setOnClickListener { onRecipeClick(recipe) }
        }
    }

    override fun getItemCount(): Int = recipeList.size

    fun setAllRecipes(recipes: List<Recipe>) {
        allRecipes = recipes
        setData(recipes)
    }

    fun getAllRecipes(): List<Recipe> = allRecipes

    fun setData(newList: List<Recipe>) {
        recipeList.clear()
        recipeList.addAll(newList)
        notifyDataSetChanged()
    }
}