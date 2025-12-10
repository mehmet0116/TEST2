package com.example.merhabadunya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.merhabadunya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DataBinding ile layout'u bağla
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        
        // Örnek: TextView'de bir metin ayarla
        binding.textView.text = "Merhaba Dünya!"
    }
}