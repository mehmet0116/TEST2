package com.example.merhabadunya

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    
    private val _message = MutableLiveData<String>().apply {
        value = "Merhaba Dünya!"
    }
    
    val message: LiveData<String> get() = _message
    
    private val messages = listOf(
        "Merhaba Dünya!",
        "Hello World!",
        "Hola Mundo!",
        "Bonjour le Monde!",
        "Hallo Welt!"
    )
    
    private var currentIndex = 0
    
    fun changeMessage() {
        currentIndex = (currentIndex + 1) % messages.size
        _message.value = messages[currentIndex]
    }
}