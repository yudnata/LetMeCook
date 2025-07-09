package com.example.letmecook.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
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


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


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

        binding.addIngredientButton.setOnClickListener {
            addIngredientView()
        }

        binding.addStepButton.setOnClickListener {
            addStepView()
        }

        addIngredientView()
        addStepView()

        binding.addRecipeBtn.setOnClickListener {
            handleCreateRecipe()
        }
    }

    private fun addIngredientView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val ingredientView = inflater.inflate(R.layout.item_ingredient_input, null)

        val deleteButton = ingredientView.findViewById<View>(R.id.deleteIngredientButton)
        deleteButton.setOnClickListener {
            binding.ingredientsContainer.removeView(ingredientView)
        }

        binding.ingredientsContainer.addView(ingredientView)
    }

    private fun addStepView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stepView = inflater.inflate(R.layout.item_step_input, null)

        val deleteButton = stepView.findViewById<View>(R.id.deleteStepButton)
        deleteButton.setOnClickListener {
            binding.stepsContainer.removeView(stepView)
        }

        binding.stepsContainer.addView(stepView)
    }

    private fun getIngredientsAsString(): String {
        return binding.ingredientsContainer.children
            .map { it.findViewById<EditText>(R.id.editIngredientText).text.toString().trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
    }

    private fun getStepsAsString(): String {
        return binding.stepsContainer.children
            .map { it.findViewById<EditText>(R.id.editStepText).text.toString().trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
    }

    private fun handleCreateRecipe() {
        loader.show()
        if (imageUri != null) {
            recipeViewModel.uploadRecipeImage(this, imageUri!!) { imageUrl ->
                Log.d("AddRecipeActivity", "Image URL: $imageUrl")
                createRecipe(imageUrl ?: "")
            }
        } else {
            createRecipe("")
        }
    }

    private fun createRecipe(imageUrl: String) {
        val title = binding.recipeTitle.text.toString().trim()
        val description = binding.recipeDesc.text.toString().trim()
        val ingredients = getIngredientsAsString()
        val process = getStepsAsString()
        val duration = binding.recipeDuration.text.toString().trim()
        val carbs = binding.recipeCarbs.text.toString().trim()
        val proteins = binding.recipeProteins.text.toString().trim()
        val fats = binding.recipeFats.text.toString().trim()
        val category = binding.selectCategory.selectedItem.toString()
        val cuisine = binding.selectCuisine.selectedItem.toString()
        val halalStatus = binding.selectHalalStatus.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty() || ingredients.isEmpty() || process.isEmpty() || duration.isEmpty() || carbs.isEmpty() || proteins.isEmpty() || fats.isEmpty() || category.isEmpty() || cuisine.isEmpty() || halalStatus.isEmpty()) {
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
            ingredients = ingredients,
            process = process,
            duration = duration,
            carbs = carbs,
            proteins = proteins,
            fats = fats,
            category = category,
            cuisine = cuisine,
            halalStatus = halalStatus,
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