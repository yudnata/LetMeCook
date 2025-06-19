package com.example.letmecook.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.letmecook.model.Recipe
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class RecipeRepositoryImpl : RecipeRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("recipes")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dnqet3vq1",
            "api_key" to "241141791476868",
            "api_secret" to "vkvZYuioJ-8u-LZOWHyUwbz46HM"
        )
    )

    override fun createRecipe(recipe: Recipe, callback: (Boolean, String, String) -> Unit) {
        val recipeId = reference.push().key ?: return
        recipe.id = recipeId
        reference.child(recipeId).setValue(recipe).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Recipe created successfully", recipeId)
            } else {
                callback(false, it.exception?.message ?: "Failed to create recipe", "")
            }
        }
    }

    override fun getRecipe(recipeId: String, callback: (Recipe?, Boolean, String) -> Unit) {
        reference.child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipe = snapshot.getValue(Recipe::class.java)
                if (recipe != null) {
                    callback(recipe, true, "Recipe fetched successfully")
                } else {
                    callback(null, false, "Recipe not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun updateRecipe(
        recipeId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(recipeId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Recipe updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to update recipe")
            }
        }
    }

    override fun deleteRecipe(recipeId: String, callback: (Boolean, String) -> Unit) {
        reference.child(recipeId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Recipe deleted successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete recipe")
            }
        }
    }

    override fun getAllRecipes(callback: (List<Recipe>, Boolean, String) -> Unit) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipes = snapshot.children.mapNotNull { it.getValue(Recipe::class.java) }
                callback(recipes, true, "Events fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun getRecipesByCategory(
        category: String,
        callback: (List<Recipe>, Boolean, String) -> Unit
    ) {
        reference.orderByChild("category").equalTo(category).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipes = snapshot.children.mapNotNull { it.getValue(Recipe::class.java) }
                callback(recipes, true, "Recipes fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun getRecipesByUser(
        userId: String,
        callback: (List<Recipe>, Boolean, String) -> Unit
    ) {
        reference.orderByChild("creatorId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children.mapNotNull { it.getValue(Recipe::class.java) }
                callback(events, true, "Events fetched successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun uploadRecipeImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val fileName = "recipe_image_${System.currentTimeMillis()}"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }
}