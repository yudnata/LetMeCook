package com.example.letmecook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipely.R
import com.example.recipely.databinding.ItemBookmarkBinding
import com.example.recipely.model.BookmarkModel
import com.example.recipely.model.Recipe

class BookmarksAdapter(
    private var bookmarkList: MutableList<BookmarkModel>,
    private val onRecipeClick: (Recipe) -> Unit,
    private val onFetchEvent: (String, (Recipe?) -> Unit) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding =
            ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = bookmarkList[position]
        with(holder.binding) {
            Glide.with(root.context)
                .load("")
                .placeholder(R.drawable.placeholder)
                .into(imgRecipe)

            onFetchEvent(bookmark.recipeId) { recipe ->
                recipe?.let {
                    recipeName.text = it.title
                    recipeCategory.text = it.category
                    recipeProtein.text = it.category
                    recipeCarbs.text = it.carbs
                    recipeDuration.text = it.duration
                    holder.itemView.setOnClickListener { onRecipeClick(recipe) }

                    Glide.with(root.context)
                        .load(it.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(imgRecipe)
                } ?: run {
                    recipeName.text = "Recipe not found"
                }
            }

            btnDelete.setOnClickListener {
                onDeleteClicked?.invoke(bookmark)
            }
        }
    }

    override fun getItemCount(): Int = bookmarkList.size

    fun setData(newList: List<BookmarkModel>) {
        bookmarkList.clear()
        bookmarkList.addAll(newList)
        notifyDataSetChanged()
    }

    var onDeleteClicked: ((BookmarkModel) -> Unit)? = null
}
