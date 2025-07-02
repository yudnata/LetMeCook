package com.example.letmecook.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.letmecook.model.UserModel
import com.example.letmecook.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(private val repo: UserRepository) {
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }

    fun signup(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        repo.signup(email, password, callback)
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    fun addUserToDatabase(userModel: UserModel, callback: (Boolean, String) -> Unit) {
        repo.addUserToDatabase(userModel, callback)
    }

    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }

    private var _userData = MutableLiveData<UserModel?>()
    val userData
        get() = _userData

    // --- PERUBAHAN UTAMA DI SINI ---
    // Mengubah fungsi ini agar bisa menerima callback untuk mengambil data pengguna manapun
    fun getDataFromDatabase(userId: String, onResult: (UserModel?) -> Unit) {
        repo.getDataFromDatabase(userId) { userModel, success, _ ->
            if (success) {
                // Jika ini adalah pengguna yang sedang login, perbarui LiveData
                if (userId == repo.getCurrentUser()?.uid) {
                    _userData.postValue(userModel)
                }
                // Selalu panggil callback dengan hasilnya
                onResult(userModel)
            } else {
                onResult(null)
            }
        }
    }

    // Fungsi lama untuk menjaga kompatibilitas dengan halaman profil
    fun getDataFromDatabase(userId: String) {
        repo.getDataFromDatabase(userId) { userModel, success, _ ->
            if (success) {
                _userData.value = userModel
            } else {
                _userData.value = null
            }
        }
    }
    // --- AKHIR DARI PERUBAHAN ---

    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout(callback)
    }

    fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        repo.editProfile(userId, data, callback)
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadImage(context, imageUri, callback)
    }
}