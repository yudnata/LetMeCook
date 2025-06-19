package com.example.letmecook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letmecook.adapter.BookmarksAdapter
import com.example.letmecook.databinding.FragmentBookmarksBinding
import com.example.letmecook.model.BookmarkModel
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.BookmarkRepositoryImpl
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.ui.activity.RecipeDetailsActivity
import com.example.letmecook.viewmodel.BookmarkViewModel
import com.example.letmecook.viewmodel.RecipeViewModel
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class BookmarksFragment : Fragment() {
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private val bookmarkViewModel: BookmarkViewModel by lazy {
        BookmarkViewModel(
            BookmarkRepositoryImpl()
        )
    }
    private val recipeViewModel: RecipeViewModel by lazy { RecipeViewModel(RecipeRepositoryImpl()) }

    private lateinit var bookmarksAdapter: BookmarksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchUserBookmarks()
    }

    private fun setupRecyclerView() {
        bookmarksAdapter = BookmarksAdapter(mutableListOf(), onRecipeClick = { event -> navigateToRecipeDetail(event) },) { recipeId, callback ->

            recipeViewModel.getRecipe(recipeId) { event, success, message ->
                callback(event)
            }
        }
        bookmarksAdapter.onDeleteClicked = { bookmark ->
            deleteBookmark(bookmark)
        }
        binding.recyclerViewBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarksAdapter
        }
    }

    private fun fetchUserBookmarks() {
        val currentUser = userViewModel.getCurrentUser()
        if (currentUser == null) {
            Snackbar.make(binding.myBookmarkRoot, "User not logged in", Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        bookmarkViewModel.getUserBookmarks(currentUser.uid) { bookmarks, success, message ->
            if (success) {
                bookmarksAdapter.setData(bookmarks)
                updateEmptyState(bookmarks.isEmpty())
            } else {
                Snackbar.make(binding.myBookmarkRoot, "Error: $message", Snackbar.LENGTH_SHORT)
                    .show()
                updateEmptyState(true)
            }
        }
    }

    private fun deleteBookmark(bookmark: BookmarkModel) {
        bookmarkViewModel.deleteBookmark(bookmark.id) { success, message ->
            if (success) {
                Snackbar.make(binding.myBookmarkRoot, "Bookmark deleted", Snackbar.LENGTH_SHORT)
                    .show()
                fetchUserBookmarks()
            } else {
                Snackbar.make(binding.myBookmarkRoot, "Error: $message", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewBookmarks.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewBookmarks.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}