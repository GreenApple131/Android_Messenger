package com.example.dyplommessenger.models

class ChatMessage(val id: String, val text: String, val id_from: String, val id_to: String, val timestamp: Long){
    constructor(): this("", "", "", "", -1)
}