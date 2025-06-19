package com.example.recipely.ui.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recipely.R
import com.example.recipely.databinding.ActivityAddRecipeBinding
import com.example.recipely.databinding.ActivityEditProfileBinding
import com.example.recipely.databinding.ActivityEditRecipeBinding
import com.example.recipely.repository.RecipeRepositoryImpl
import com.example.recipely.utils.ImageUtils
import com.example.recipely.utils.LoadingUtils
import com.example.recipely.viewmodel.RecipeViewModel
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
        val category = intent.getStringExtra("EVENT_CATEGORY") ?: ""
        currentImageUrl = intent.getStringExtra("RECIPE_IMAGE_URL") ?: ""

        binding.recipeTitle.setText(title)
        binding.recipeDesc.setText(description)
        binding.recipeProcess.setText(process)
        binding.recipeDuration.setText(duration)
        binding.recipeCarbs.setText(carbs)
        binding.recipeProteins.setText(proteins)
        binding.recipeFats.setText(fats)

        if (!currentImageUrl.isNullOrEmpty()) {
            Picasso.get().load(currentImageUrl).into(binding.imageBrowse)
        }

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.recipe_categories,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.selectCategory.adapter = spinnerAdapter
        val categories = resources.getStringArray(R.array.recipe_categories)
        val index = categories.indexOf(category)
        if (index >= 0) binding.selectCategory.setSelection(index)

        // Image picking
        imageUtils.registerActivity { uri ->
            uri?.let {
                imageUri = it
                Picasso.get().load(it).into(binding.imageBrowse)
            }
        }
        binding.imageBrowse.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        binding.editRecipeBtn.setOnClickListener {
            handleUpdateRecipe()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleUpdateRecipe() {
        loader.show()
        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "Recipe ID missing. Cannot update.", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // If user picked a new image, upload it
            recipeViewModel.uploadRecipeImage(this, imageUri!!) { imageUrl ->
                Log.d("UpdateRecipeActivity", "New Image URL: $imageUrl")
                updateRecipeInFirebase(imageUrl ?: currentImageUrl.orEmpty())
            }
        } else {
            // No new image selected; use existing image URL
            updateRecipeInFirebase(currentImageUrl.orEmpty())
        }
    }

    private fun updateRecipeInFirebase(imageUrl: String) {
        val newTitle = binding.recipeTitle.text.toString().trim()
        val newDescription = binding.recipeDesc.text.toString().trim()
        val newProcess = binding.recipeProcess.text.toString().trim()
        val newDuration = binding.recipeDuration.text.toString().trim()
        val newCarbs = binding.recipeCarbs.text.toString().trim()
        val newProteins = binding.recipeProteins.text.toString().trim()
        val newFats = binding.recipeFats.text.toString().trim()
        val newCategory = binding.selectCategory.selectedItem.toString()

        if (newTitle.isEmpty() || newDescription.isEmpty() || newProcess.isEmpty() || newDuration.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        // Build a data map for partial update
        val data = mutableMapOf<String, Any>()
        data["title"] = newTitle
        data["description"] = newDescription
        data["process"] = newProcess
        data["duration"] = newDuration
        data["carbs"] = newCarbs
        data["proteins"] = newProteins
        data["fats"] = newFats
        data["category"] = newCategory
        data["imageUrl"] = imageUrl

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