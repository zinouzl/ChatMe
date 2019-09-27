package com.example.sony.chatme.model

data class User(
    val userName: String,
    val bio: String,
    val profilPicturPath: String?
) {
    constructor() : this("", "", null)
}