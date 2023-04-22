package com.example.anuraginitoassignment.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.anuraginitoassignment.data.LoginRequest
import com.example.anuraginitoassignment.data.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Interface for the API service
interface ApiService {
    @POST("sign_in")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}

// Retrofit instance with base URL and converter factory
val retrofit: Retrofit? = Retrofit.Builder()
    .baseUrl("http://apistaging.inito.com/api/v1/auth/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// API service object from retrofit instance
val apiService = retrofit?.create(ApiService::class.java)

// Request body object with user email and password
val loginRequest = LoginRequest("amit_4@test.com", "12345678")

fun LoginCall (context:Context) {
    // Call the post method on the API service object with the request body object
    apiService?.login(loginRequest)?.enqueue(object : Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            // Handle the response
            if (response.isSuccessful) {
                // Get the auth token from the header of the response
                val authToken = response.headers().get("access-token")
                val uid = response.headers().get("uid")
                val client = response.headers().get("client")


                if (authToken != null) {
                    Log.d("token","${authToken},${uid},${client}")
                    Toast.makeText(context, "Auth token obtained successfully", Toast.LENGTH_SHORT).show()
                }

                Log.d("token","successfull response")

                // Do something with the auth token
            } else {
                Log.d("token","Response failed")
            }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            // Handle failure
        }
    })
}

