package com.example.anuraginitoassignment.viewModel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anuraginitoassignment.data.User

class MainViewModel : ViewModel() {

    // create a live data object to store the user information
    val user = MutableLiveData<User>()

    // create a shared preferences object to save and load the user information
    private lateinit var sharedPreferences: SharedPreferences

    // initialize the shared preferences object with the application context
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    // load the user information from the shared preferences and update the live data object
    fun loadUser() {
        val name = sharedPreferences.getString("name", "") ?: ""
        val email = sharedPreferences.getString("email", "") ?: ""
        user.value = User(name, email)
    }

    // save the user information to the shared preferences and update the live data object
    fun saveUser(name: String, email: String) {
        with(sharedPreferences.edit()) {
            putString("name", name)
            putString("email", email)
            apply()
        }
        user.value = User(name, email)
    }

    // validate the user input and return true if valid, false otherwise
    fun validateInput(name: String, email: String): Boolean {
        return name.isNotEmpty() && email.isNotEmpty()
    }
}