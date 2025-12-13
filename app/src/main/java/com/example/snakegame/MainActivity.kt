package com.example.snakegame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snakegame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Başlangıçta basit bir mesaj göster
        binding.textView.text = "Snake Game\n\nComing Soon!"
    }
}