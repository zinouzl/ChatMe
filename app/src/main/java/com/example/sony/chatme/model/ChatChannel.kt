package com.example.sony.chatme.model

import java.util.*

data class ChatChannel(
    val userIds: MutableList<String>,
    val lastMessage: String,
    val activeTime: Date
) {
    constructor() : this(mutableListOf(), "", Calendar.getInstance().time)
}