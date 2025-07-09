package com.example.letmecook.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityEditRecipeBinding
import com.example.letmecook.model.Recipe // Pastikan model di-import
import com.example.letmecook.repository.RecipeRepositoryImpl
import com.example.letmecook.utils.ImageUtils
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.RecipeViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class EditRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var loader: LoadingUtils
    private lateinit var imageUtils: ImageUtils

    private var recipeId: String? = null
    private var currentImageUrl: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loader = LoadingUtils(this)
        imageUtils = ImageUtils(this)
        recipeViewModel = RecipeViewModel(RecipeRepositoryImpl())

        // Hanya ambil ID dari Intent
        recipeId = intent.getStringExtra("RECIPE_ID")

        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "Recipe ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        loadRecipeData()

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

        binding.editRecipeBtn.setOnClickListener {
            handleUpdateRecipe()
        }
    }


    private fun loadRecipeData() {
        loader.show()
        recipeViewModel.getRecipe(recipeId!!) { recipe, success, message ->
            loader.dismiss()
            if (success && recipe != null) {

                populateUi(recipe)
            } else {
                Toast.makeText(this, "Failed to load recipe: $message", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // --- FUNGSI BARU UNTUK MENGISI UI ---
    private fun populateUi(recipe: Recipe) {
        binding.recipeTitle.setText(recipe.title)
        binding.recipeDesc.setText(recipe.description)
        binding.recipeDuration.setText(recipe.duration)
        binding.recipeCarbs.setText(recipe.carbs)
        binding.recipeProteins.setText(recipe.proteins)
        binding.recipeFats.setText(recipe.fats)

        currentImageUrl = recipe.imageUrl
        if (!currentImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(currentImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.imageBrowse)
        } else {
            binding.imageBrowse.setImageResource(R.drawable.placeholder_image)
        }


        binding.ingredientsContainer.removeAllViews()
        val ingredientList = recipe.ingredients.split("\n").filter { it.isNotBlank() }
        if (ingredientList.isNotEmpty()) {
            for (ingredient in ingredientList) {
                addIngredientView(ingredient)
            }
        } else {
            addIngredientView() // Tambahkan satu jika kosong
        }


        binding.stepsContainer.removeAllViews()
        val steps = recipe.process.split("\n").filter { it.isNotBlank() }
        if (steps.isNotEmpty()) {
            for (step in steps) {
                addStepView(step)
            }
        } else {
            addStepView() // Tambahkan satu jika kosong
        }

        // Setup spinner
        val categories = resources.getStringArray(R.array.recipe_categories)
        binding.selectCategory.setSelection(categories.indexOf(recipe.category).coerceAtLeast(0))

        val cuisines = resources.getStringArray(R.array.recipe_cuisines)
        binding.selectCuisine.setSelection(cuisines.indexOf(recipe.cuisine).coerceAtLeast(0))

        val halalStatuses = resources.getStringArray(R.array.recipe_halal_status)
        binding.selectHalalStatus.setSelection(halalStatuses.indexOf(recipe.halalStatus).coerceAtLeast(0))
    }

    private fun addIngredientView(text: String? = null) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val ingredientView = inflater.inflate(R.layout.item_ingredient_input, null)

        val editText = ingredientView.findViewById<EditText>(R.id.editIngredientText)
        text?.let {
            editText.setText(it)
        }

        val deleteButton = ingredientView.findViewById<View>(R.id.deleteIngredientButton)
        deleteButton.setOnClickListener {
            binding.ingredientsContainer.removeView(ingredientView)
        }

        binding.ingredientsContainer.addView(ingredientView)
    }

    private fun addStepView(text: String? = null) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stepView = inflater.inflate(R.layout.item_step_input, null)

        val editText = stepView.findViewById<EditText>(R.id.editStepText)
        text?.let {
            editText.setText(it)
        }

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

    private fun handleUpdateRecipe() {
        loader.show()
        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "Recipe ID missing. Cannot update.", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        if (imageUri != null) {
            recipeViewModel.uploadRecipeImage(this, imageUri!!) { imageUrl ->
                Log.d("UpdateRecipeActivity", "New Image URL: $imageUrl")
                updateRecipeInFirebase(imageUrl ?: currentImageUrl.orEmpty())
            }
        } else {
            updateRecipeInFirebase(currentImageUrl.orEmpty())
        }
    }

    private fun updateRecipeInFirebase(imageUrl: String) {
        val newTitle = binding.recipeTitle.text.toString().trim()
        val newDescription = binding.recipeDesc.text.toString().trim()
        val newIngredients = getIngredientsAsString()
        val newProcess = getStepsAsString()
        val newDuration = binding.recipeDuration.text.toString().trim()
        val newCarbs = binding.recipeCarbs.text.toString().trim()
        val newProteins = binding.recipeProteins.text.toString().trim()
        val newFats = binding.recipeFats.text.toString().trim()
        val newCategory = binding.selectCategory.selectedItem.toString()
        val newCuisine = binding.selectCuisine.selectedItem.toString()
        val newHalalStatus = binding.selectHalalStatus.selectedItem.toString()

        if (newTitle.isEmpty() || newDescription.isEmpty() || newIngredients.isEmpty() || newProcess.isEmpty() || newDuration.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        val data = mutableMapOf<String, Any>(
            "title" to newTitle,
            "description" to newDescription,
            "ingredients" to newIngredients,
            "process" to newProcess,
            "duration" to newDuration,
            "carbs" to newCarbs,
            "proteins" to newProteins,
            "fats" to newFats,
            "category" to newCategory,
            "cuisine" to newCuisine,
            "halalStatus" to newHalalStatus,
            "imageUrl" to imageUrl
        )

        recipeViewModel.updateRecipe(recipeId!!, data) { success, message ->
            loader.dismiss()
            if (success) {
                Toast.makeText(this, "Recipe updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman detail
            } else {
                Snackbar.make(
                    binding.main,
                    "Failed to update recipe: $message",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}