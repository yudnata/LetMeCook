package com.example.letmecook.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letmecook.R
import com.example.letmecook.adapter.CitiesAdapter
import com.example.letmecook.adapter.CountriesAdapter
import com.example.letmecook.databinding.ActivitySignupBinding
import com.example.letmecook.model.UserModel
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.utils.ImageUtils
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils

    private lateinit var imageUtils: ImageUtils
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countriesAdapter = CountriesAdapter(this)
        val citiesAdapter = CitiesAdapter(this)

        binding.selectCountry.adapter = countriesAdapter
        binding.autoCompleteCity.threshold = 1
        binding.autoCompleteCity.setAdapter(citiesAdapter)

        imageUtils = ImageUtils(this)
        loadingUtils = LoadingUtils(this)

        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)

        imageUtils.registerActivity { uri ->
            if (uri != null) {
                imageUri = uri
                Picasso.get().load(uri).into(binding.imageBrowse)
                Log.d("SignupActivity", "Image selected: $uri")
            } else {
                Log.e("SignupActivity", "No image selected")
            }
        }

        binding.imageBrowse.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        binding.signupBtn.setOnClickListener {
            val fullName = binding.editFullName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val genderId = binding.genderSelect.checkedRadioButtonId
            val gender = when (genderId) {
                R.id.maleRadio -> "Male"
                R.id.femaleRadio -> "Female"
                else -> "Unknown"
            }
            val country = binding.selectCountry.selectedItem.toString()
            val city = binding.autoCompleteCity.text.toString()
            val isTermsAccepted = binding.checkBox.isChecked

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() ||
                gender == "Unknown" || country.isEmpty() || city.isEmpty() || !isTermsAccepted
            ) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            uploadImage()
        }

        binding.login.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            loadingUtils.show()
            userViewModel.uploadImage(this, imageUri!!) { imageUrl ->
                Log.d("SignupActivity", "Image URL: $imageUrl")
                if (imageUrl != null) {
                    signupUser(imageUrl)
                } else {
                    Log.e("SignupActivity", "Failed to upload image to Cloudinary")

                    signupUser("")
                }
            }
        } else {
            Log.d("SignupActivity", "No image selected, proceeding without image")
            signupUser("")
        }
    }

    private fun signupUser(url: String) {
        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val genderId = binding.genderSelect.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.maleRadio -> "Male"
            R.id.femaleRadio -> "Female"
            else -> "Unknown"
        }
        val country = binding.selectCountry.selectedItem.toString()
        val city = binding.autoCompleteCity.text.toString()

        Log.d("SignupActivity", "Signing up with email: $email")

        userViewModel.signup(email, password) { success, message, userId ->
            if (success) {
                Log.d("SignupActivity", "Signup successful, userId: $userId")
                val userModel = UserModel(
                    userId,
                    fullName,
                    email,
                    gender,
                    country,
                    city,
                    url
                )
                addUserToDatabase(userModel)
            } else {
                loadingUtils.dismiss()
                Log.e("SignupActivity", "Signup failed: $message")
                Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserToDatabase(userModel: UserModel) {
        userViewModel.addUserToDatabase(userModel) { success, message ->
            loadingUtils.dismiss()
            if (success) {
                Log.d("SignupActivity", "User added to database: $message")
                Toast.makeText(this@SignupActivity, message, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            } else {
                Log.e("SignupActivity", "Failed to add user to database: $message")
            }
        }
    }
}