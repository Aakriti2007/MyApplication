package com.example.mylibrary.api

import com.example.mylibrary.frameworks.common.Constants.KEY_AUTHENTICATION
import com.example.mylibrary.frameworks.common.Constants.TXN_ID
import com.example.mylibrary.model.AuthRequest
import com.example.mylibrary.model.VerifyOtpRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @POST("api/v1/id-auth/authenticate")
    fun authenticate(
        @Header(KEY_AUTHENTICATION) token: String,
        @Body request: AuthRequest
    ): Call<ResponseBody>


    @GET("api/v1/id-auth/status/{txnId}")
    fun getStatus(
        @Header(KEY_AUTHENTICATION) token: String,
        @Path("txnId") txnId: String
    ): Call<ResponseBody>

    @POST("api/v1/id-auth/verifyOtp")
    fun verifyOtp(
        @Header(KEY_AUTHENTICATION) token: String,
        @Body request: VerifyOtpRequest
    ): Call<ResponseBody>

    @POST("api/v1/id-auth/resendOtp")
    fun resendOtp(
        @Header(KEY_AUTHENTICATION) token: String,
        @Query(TXN_ID) txnId: String
    ): Call<ResponseBody>
}