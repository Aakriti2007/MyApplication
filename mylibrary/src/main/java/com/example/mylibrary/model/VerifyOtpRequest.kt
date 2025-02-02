package com.example.mylibrary.model

data class VerifyOtpRequest(
    val txnId: String,
    val otp: String
)
