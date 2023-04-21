package com.example.anuraginitoassignment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.anuraginitoassignment.databinding.ActivityMainBinding
import com.example.anuraginitoassignment.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize view model
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.init(applicationContext)

        // load user information from view model
        viewModel.loadUser()

        // observe user live data and update UI accordingly
        viewModel.user.observe(this) { user ->
            binding.nameEditText.setText(user.name)
            binding.emailEditText.setText(user.email)
        }

        // handle button click
        binding.takeTestButton.setOnClickListener {
            // get input values from UI
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()

            // validate input values using view model
            if (viewModel.validateInput(name, email)) {
                // save user information using view model
                viewModel.saveUser(name, email)

                // start quiz activity
                val intent = Intent(this, Screen2Activity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter your name and email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
