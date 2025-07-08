package com.example.letmecook.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.letmecook.ui.fragment.RecipeOverviewFragment
import com.example.letmecook.ui.fragment.RecipeReviewsFragment

class RecipeDetailsViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecipeOverviewFragment()
            1 -> RecipeReviewsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}