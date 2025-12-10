package com.example.merhabadunya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.merhabadunya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DataBinding bağlamasını oluştur
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ViewModel'i oluştur
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // DataBinding'e ViewModel'i bağla
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        
        // Button click listener
        binding.button.setOnClickListener {
            viewModel.changeMessage()
        }
    }
}