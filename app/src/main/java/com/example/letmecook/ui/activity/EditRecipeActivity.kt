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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityEditRecipeBinding
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


        recipeId = intent.getStringExtra("RECIPE_ID")
        val title = intent.getStringExtra("RECIPE_TITLE") ?: ""
        val description = intent.getStringExtra("RECIPE_DESCRIPTION") ?: ""
        val process = intent.getStringExtra("RECIPE_PROCESS") ?: ""
        val duration = intent.getStringExtra("RECIPE_DURATION") ?: ""
        val carbs = intent.getStringExtra("RECIPE_CARBS") ?: ""
        val proteins = intent.getStringExtra("RECIPE_PROTEINS") ?: ""
        val fats = intent.getStringExtra("RECIPE_FATS") ?: ""
        val category = intent.getStringExtra("RECIPE_CATEGORY") ?: ""
        val cuisine = intent.getStringExtra("RECIPE_CUISINE") ?: ""
        val halalStatus = intent.getStringExtra("RECIPE_HALAL_STATUS") ?: ""
        currentImageUrl = intent.getStringExtra("RECIPE_IMAGE_URL") ?: ""

        binding.recipeTitle.setText(title)
        binding.recipeDesc.setText(description)
        binding.recipeDuration.setText(duration)
        binding.recipeCarbs.setText(carbs)
        binding.recipeProteins.setText(proteins)
        binding.recipeFats.setText(fats)

        if (!currentImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(currentImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.imageBrowse)
        } else {
            binding.imageBrowse.setImageResource(R.drawable.placeholder_image)
        }

        val steps = process.split("\n").filter { it.isNotBlank() }
        if (steps.isNotEmpty()) {
            for (step in steps) {
                addStepView(step)
            }
        } else {
            addStepView()
        }

        val categorySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.recipe_categories, android.R.layout.simple_spinner_dropdown_item)
        binding.selectCategory.adapter = categorySpinnerAdapter
        val categories = resources.getStringArray(R.array.recipe_categories)
        val categoryIndex = categories.indexOf(category)
        if (categoryIndex >= 0) binding.selectCategory.setSelection(categoryIndex)

        val cuisineSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.recipe_cuisines, android.R.layout.simple_spinner_dropdown_item)
        binding.selectCuisine.adapter = cuisineSpinnerAdapter
        val cuisines = resources.getStringArray(R.array.recipe_cuisines)
        val cuisineIndex = cuisines.indexOf(cuisine)
        if (cuisineIndex >= 0) binding.selectCuisine.setSelection(cuisineIndex)

        val halalStatusSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.recipe_halal_status, android.R.layout.simple_spinner_dropdown_item)
        binding.selectHalalStatus.adapter = halalStatusSpinnerAdapter
        val halalStatuses = resources.getStringArray(R.array.recipe_halal_status)
        val halalStatusIndex = halalStatuses.indexOf(halalStatus)
        if (halalStatusIndex >= 0) binding.selectHalalStatus.setSelection(halalStatusIndex)

        imageUtils.registerActivity { uri ->
            uri?.let {
                imageUri = it
                Picasso.get().load(it).into(binding.imageBrowse)
            }
        }
        binding.imageBrowse.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        binding.addStepButton.setOnClickListener {
            addStepView()
        }

        binding.editRecipeBtn.setOnClickListener {
            handleUpdateRecipe()
        }

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
        val newProcess = getStepsAsString()
        val newDuration = binding.recipeDuration.text.toString().trim()
        val newCarbs = binding.recipeCarbs.text.toString().trim()
        val newProteins = binding.recipeProteins.text.toString().trim()
        val newFats = binding.recipeFats.text.toString().trim()
        val newCategory = binding.selectCategory.selectedItem.toString()
        val newCuisine = binding.selectCuisine.selectedItem.toString()
        val newHalalStatus = binding.selectHalalStatus.selectedItem.toString()

        if (newTitle.isEmpty() || newDescription.isEmpty() || newProcess.isEmpty() || newDuration.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        val data = mutableMapOf<String, Any>(
            "title" to newTitle,
            "description" to newDescription,
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
                finish()
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