package com.example.letmecook.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.letmecook.R
import com.example.letmecook.databinding.FragmentProfileBinding
import com.example.letmecook.repository.UserRepositoryImpl
import com.example.letmecook.ui.activity.DashboardActivity
import com.example.letmecook.ui.activity.EditProfileActivity
import com.example.letmecook.ui.activity.LoginActivity
import com.example.letmecook.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = UserRepositoryImpl()
        userViewModel = UserViewModel(repo)

        val currentUser = userViewModel.getCurrentUser()
        currentUser?.uid?.let { uid ->
            userViewModel.getDataFromDatabase(uid)
        }

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.lblName.text = user?.fullName
            binding.lblEmail.text = user?.email
            binding.lblGender.text = user?.gender
            binding.lblAddress.text = "${user?.country}, ${user?.city}"

            if (!user?.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user?.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.userAvatar)
            } else {
                binding.userAvatar.setImageResource(R.drawable.placeholder)
            }
        }

        binding.bookmarksLayout.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToBookmarks()
        }

        binding.recipesLayout.setOnClickListener{
            (activity as? DashboardActivity)?.navigateToRecipes()
        }

        binding.logout.setOnClickListener {
            userViewModel.logout { success, message ->
                if (success) {
                    val sharedPreferences = requireContext().getSharedPreferences("userData", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.remove("email")
                    editor.remove("password")
                    editor.apply()

                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Logout failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.edit.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}