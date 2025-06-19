package com.example.letmecook.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityDashboardBinding
import com.example.letmecook.ui.fragment.BookmarksFragment
import com.example.letmecook.ui.fragment.HomeFragment
import com.example.letmecook.ui.fragment.ProfileFragment
import com.example.letmecook.ui.fragment.RecipesFragment

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.commit()
    }

    fun navigateToBookmarks() {
        binding.bottomNav.selectedItemId = R.id.myBookmarksFragment
    }

    fun navigateToRecipes() {
        binding.bottomNav.selectedItemId = R.id.myRecipesFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> replaceFragment(HomeFragment())
                R.id.myBookmarksFragment -> replaceFragment(BookmarksFragment())
                R.id.myRecipesFragment -> replaceFragment(RecipesFragment())
                R.id.profileFragment -> replaceFragment(ProfileFragment())
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}