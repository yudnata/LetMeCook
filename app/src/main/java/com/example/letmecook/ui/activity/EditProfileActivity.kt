package com.example.letmecook.ui.activity

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.letmecook.R
import com.example.letmecook.adapter.CitiesAdapter
import com.example.letmecook.adapter.CountriesAdapter
import com.example.letmecook.adapter.countries
import com.example.letmecook.databinding.ActivityEditProfileBinding
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.utils.ImageUtils
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loader: LoadingUtils
    private lateinit var imageUtils: ImageUtils
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countriesAdapter = CountriesAdapter(this)
        val citiesAdapter = CitiesAdapter(this)

        binding.selectCountry.adapter = countriesAdapter
        binding.autoCompleteCity.threshold = 1
        binding.autoCompleteCity.setAdapter(citiesAdapter)

        loader = LoadingUtils(this)
        imageUtils = ImageUtils(this)
        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)

        imageUtils.registerActivity { uri ->
            uri?.let {
                imageUri = it
                Picasso.get().load(it).into(binding.imageBrowse)
            }
        }

        binding.imageBrowse.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        val currentUser = userViewModel.getCurrentUser()
        if (currentUser != null) {
            userViewModel.getDataFromDatabase(currentUser.uid)
            userViewModel.userData.observe(this) { user ->
                user?.let {
                    val countryIndex = countries.indexOf(it.country)
                    when (it.gender) {
                        "Male" -> binding.genderSelect.check(R.id.maleRadio)
                        "Female" -> binding.genderSelect.check(R.id.femaleRadio)
                    }
                    binding.editFullName.setText(it.fullName)
                    binding.editEmail.setText(it.email)
                    binding.selectCountry.setSelection(countryIndex)
                    binding.autoCompleteCity.setText(it.city)

                    // --- UBAH BAGIAN INI ---
                    if (!it.imageUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(it.imageUrl)
                            .placeholder(R.drawable.placeholder_image) // Gunakan placeholder baru
                            .into(binding.imageBrowse)
                    } else {
                        binding.imageBrowse.setImageResource(R.drawable.placeholder_image) // Gunakan placeholder baru
                    }
                    // --- AKHIR PERUBAHAN ---
                }
            }
        }

        binding.saveBtn.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        loader.show()

        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val genderId = binding.genderSelect.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.maleRadio -> "Male"
            R.id.femaleRadio -> "Female"
            else -> "Unknown"
        }
        val country = binding.selectCountry.selectedItem.toString()
        val city = binding.autoCompleteCity.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || gender.isEmpty() || country.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        val currentUser = userViewModel.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            return
        }

        val updateData = mutableMapOf<String, Any>(
            "fullName" to fullName,
            "email" to email,
            "gender" to gender,
            "country" to country,
            "city" to city
        )

        if (imageUri != null) {
            userViewModel.uploadImage(this, imageUri!!) { imageUrl ->
                if (imageUrl != null) {
                    updateData["imageUrl"] = imageUrl
                    performProfileUpdate(currentUser.uid, updateData)
                } else {
                    loader.dismiss()
                    Snackbar.make(binding.root, "Image upload failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            performProfileUpdate(currentUser.uid, updateData)
        }
    }

    private fun performProfileUpdate(userId: String, updateData: MutableMap<String, Any>) {
        userViewModel.editProfile(userId, updateData) { success, message ->
            loader.dismiss()
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Snackbar.make(
                    binding.root,
                    "Profile update failed: $message",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}