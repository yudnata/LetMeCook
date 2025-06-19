package com.example.recipely.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipely.R
import com.example.recipely.databinding.ItemMyRecipeBinding
import com.example.recipely.model.Recipe
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
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgRecipe)
            } else {
                imgRecipe.setImageResource(R.drawable.ic_launcher_foreground)
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