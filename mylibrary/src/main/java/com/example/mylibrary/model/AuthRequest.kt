package com.example.mylibrary.model

data class AuthRequest(
    val brand: String,
    val workflow: List<Workflow>
)

data class Workflow(
    val channel: String,
    val mobileNumberTo: String
)
