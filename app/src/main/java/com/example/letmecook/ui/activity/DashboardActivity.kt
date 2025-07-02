package com.example.letmecook.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.letmecook.R
import com.example.letmecook.databinding.ActivityDashboardBinding
import com.example.letmecook.ui.fragment.BookmarksFragment
import com.example.letmecook.ui.fragment.HomeFragment
import com.example.letmecook.ui.fragment.ProfileFragment
import com.example.letmecook.ui.fragment.RecipesFragment

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val fragmentManager: FragmentManager = supportFragmentManager

    private val homeFragment: Fragment = HomeFragment()
    private val bookmarksFragment: Fragment = BookmarksFragment()
    private val recipesFragment: Fragment = RecipesFragment()
    private val profileFragment: Fragment = ProfileFragment()

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengaktifkan mode Edge-to-Edge
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- INI ADALAH KODE PERBAIKANNYA ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Menerapkan padding atas untuk status bar
            view.setPadding(0, insets.top, 0, 0)

            // Menerapkan padding bawah sebagai margin untuk BottomNavigationView
            // Ini akan mendorong menu navigasi ke atas, tepat di atas 3-button navigation bar
            binding.bottomNav.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }

            // Mengembalikan insets yang tidak dipakai
            WindowInsetsCompat.CONSUMED
        }
        // --- AKHIR DARI KODE PERBAIKAN ---

        setupFragments()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> switchFragment(homeFragment)
                R.id.myBookmarksFragment -> switchFragment(bookmarksFragment)
                R.id.myRecipesFragment -> switchFragment(recipesFragment)
                R.id.profileFragment -> switchFragment(profileFragment)
            }
            true
        }
    }

    private fun setupFragments() {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.mainFrame, profileFragment, "4").hide(profileFragment)
        transaction.add(R.id.mainFrame, recipesFragment, "3").hide(recipesFragment)
        transaction.add(R.id.mainFrame, bookmarksFragment, "2").hide(bookmarksFragment)
        transaction.add(R.id.mainFrame, homeFragment, "1")
        transaction.commit()
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit()
            activeFragment = fragment
        }
    }

    fun navigateToBookmarks() {
        binding.bottomNav.selectedItemId = R.id.myBookmarksFragment
    }

    fun navigateToRecipes() {
        binding.bottomNav.selectedItemId = R.id.myRecipesFragment
    }
}