package com.example.merhabadunya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.merhabadunya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Merhaba Dünya mesajını göster
        binding.textView.text = "Merhaba Dünya!"
    }
}