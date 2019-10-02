package com.example.sony.chatme.model

data class ChatChannel(val userIds:MutableList<String>) {
    constructor():this(mutableListOf())
}