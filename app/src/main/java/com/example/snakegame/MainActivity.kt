package com.example.snakegame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.snakegame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var gameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var highScoreTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var restartButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        gameView = binding.gameView
        scoreTextView = binding.scoreTextView
        highScoreTextView = binding.highScoreTextView
        startButton = binding.startButton
        pauseButton = binding.pauseButton
        restartButton = binding.restartButton
        
        // Oyun durumunu dinle
        gameView.setOnGameStateChangeListener(object : GameView.OnGameStateChangeListener {
            override fun onScoreChanged(score: Int) {
                scoreTextView.text = "Score: $score"
            }
            
            override fun onHighScoreChanged(highScore: Int) {
                highScoreTextView.text = "High Score: $highScore"
            }
            
            override fun onGameOver() {
                startButton.isEnabled = true
                pauseButton.isEnabled = false
                restartButton.isEnabled = true
            }
            
            override fun onGameStarted() {
                startButton.isEnabled = false
                pauseButton.isEnabled = true
                restartButton.isEnabled = true
            }
            
            override fun onGamePaused() {
                startButton.isEnabled = true
                pauseButton.isEnabled = false
            }
        })
    }
    
    private fun setupClickListeners() {
        startButton.setOnClickListener {
            gameView.startGame()
        }
        
        pauseButton.setOnClickListener {
            gameView.pauseGame()
        }
        
        restartButton.setOnClickListener {
            gameView.restartGame()
        }
    }
    
    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }
    
    override fun onResume() {
        super.onResume()
        // Oyun durumuna göre butonları güncelle
        if (gameView.isGameRunning()) {
            startButton.isEnabled = false
            pauseButton.isEnabled = true
        }
    }
}