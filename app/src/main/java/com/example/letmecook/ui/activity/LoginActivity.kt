package com.example.letmecook.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityLoginBinding
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.utils.LoadingUtils
import com.example.letmecook.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)
        loadingUtils = LoadingUtils(this@LoginActivity)

        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)

        binding.register.setOnClickListener {
            val intent= Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            loadingUtils.show()
            val email: String = binding.editEmail.text.toString()
            val password: String = binding.editPassword.text.toString()

            userViewModel.login(email, password) { success, message ->
                if (success) {
                    loadingUtils.dismiss()
                    Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()

                    val editor = sharedPreferences.edit()
                    editor.putString("email", email)
                    editor.putString("password", password)
                    editor.apply()

                    val intent= Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    loadingUtils.dismiss()
                    Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.forgotPassword.setOnClickListener {
            loadingUtils.show()
            val email: String = binding.editEmail.text.toString()

            userViewModel.forgetPassword(email) { success, message ->
                if (success) {
                    loadingUtils.dismiss()
                    Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()
                } else {
                    loadingUtils.dismiss()
                    Snackbar.make(binding.main, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // --- KODE YANG DIPERBAIKI ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Menyesuaikan padding bawah untuk memberi ruang bagi keyboard
            val targetPadding = if (ime.bottom > 0) {
                ime.bottom
            } else {
                systemBars.bottom
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, targetPadding)

            // Mengembalikan insets yang tidak dipakai (agar sistem bisa menanganinya)
            WindowInsetsCompat.Builder(insets).setInsets(
                WindowInsetsCompat.Type.ime(),
                androidx.core.graphics.Insets.of(0, 0, 0, 0)
            ).build()
        }
    }
}