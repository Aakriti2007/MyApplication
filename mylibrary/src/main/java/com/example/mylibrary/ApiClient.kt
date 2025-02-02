package com.example.mylibrary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var retrofit: Retrofit? = null
    private var baseUrl: String? = null

    // Set the base URL dynamically
    fun setBaseUrl(url: String) {
        baseUrl = url
        retrofit = null // Clear existing Retrofit instance if base URL changes
    }

    fun getInstance(): Retrofit {
        if (retrofit == null) {
            if (baseUrl.isNullOrEmpty()) {
                throw IllegalStateException("Base URL is not set. Call setBaseUrl() first.")
            }

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl!!)
                //.baseUrl("https://34.102.220.112.nip.io/id-auth/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}