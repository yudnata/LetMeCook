package com.example.letmecook.ui.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityAddRecipeBinding
import com.example.letmecook.model.Recipe
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.utils.ImageUtils
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.RecipeViewModel
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class AddRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var loader: LoadingUtils
    private lateinit var imageUtils: ImageUtils
    private var imageUri: Uri? = null
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = LoadingUtils(this)
        imageUtils = ImageUtils(this)
        val recipeRepository = RecipeRepositoryImpl()
        recipeViewModel = RecipeViewModel(recipeRepository)

        imageUtils.registerActivity { uri ->
            uri?.let {
                imageUri = it
                Picasso.get().load(it).into(binding.imageBrowse)
            }
        }

        binding.imageBrowse.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        binding.addRecipeBtn.setOnClickListener {
            handleCreateRecipe()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleCreateRecipe() {
        loader.show()
        if (imageUri != null) {
            recipeViewModel.uploadRecipeImage(this, imageUri!!) { imageUrl ->
                Log.d("AddEventActivity", "Image URL: $imageUrl")
                createRecipe(imageUrl ?: "")
            }
        } else {
            createRecipe("")
        }
    }

    private fun createRecipe(imageUrl: String) {
        val title = binding.recipeTitle.text.toString().trim()
        val description = binding.recipeDesc.text.toString().trim()
        val process = binding.recipeProcess.text.toString().trim()
        val duration = binding.recipeDuration.text.toString().trim()
        val carbs = binding.recipeCarbs.text.toString().trim()
        val proteins = binding.recipeProteins.text.toString().trim()
        val fats = binding.recipeFats.text.toString().trim()
        val category = binding.selectCategory.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty() || process.isEmpty() || duration.isEmpty() || carbs.isEmpty() || proteins.isEmpty() || fats.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)
        val currUser = userRepository.getCurrentUser()?.uid
        Log.d("hello", currUser.toString())

        val recipe = Recipe(
            title = title,
            description = description,
            process = process,
            duration = duration,
            carbs = carbs,
            proteins = proteins,
            fats = fats,
            category = category,
            creatorId = currUser.toString(),
            imageUrl = imageUrl,
        )

        recipeViewModel.createRecipe(recipe) { success, message, eventId ->
            loader.dismiss()
            if (success) {
                Toast.makeText(this, "Recipe created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Snackbar.make(
                    binding.main,
                    "Failed to create recipe: $message",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}