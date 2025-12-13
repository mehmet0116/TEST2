package com.example.snakegame

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var snakeGameView: SnakeGameView
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var tvGameOver: TextView
    private lateinit var btnPauseResume: Button
    private lateinit var btnRestart: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        setupGameCallbacks()
    }
    
    private fun initializeViews() {
        snakeGameView = findViewById(R.id.snakeGameView)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        tvGameOver = findViewById(R.id.tvGameOver)
        btnPauseResume = findViewById(R.id.btnPauseResume)
        btnRestart = findViewById(R.id.btnRestart)
        
        updateHighScoreDisplay()
    }
    
    private fun setupClickListeners() {
        btnPauseResume.setOnClickListener {
            if (snakeGameView.isGamePaused) {
                snakeGameView.resumeGame()
                btnPauseResume.text = getString(R.string.pause)
            } else {
                snakeGameView.pauseGame()
                btnPauseResume.text = getString(R.string.resume)
            }
        }
        
        btnRestart.setOnClickListener {
            snakeGameView.restartGame()
            tvGameOver.visibility = TextView.GONE
            btnPauseResume.text = getString(R.string.pause)
            updateScoreDisplay(0)
        }
    }
    
    private fun setupGameCallbacks() {
        snakeGameView.onScoreChanged = { score ->
            updateScoreDisplay(score)
        }
        
        snakeGameView.onGameOver = { score ->
            tvGameOver.visibility = TextView.VISIBLE
            btnPauseResume.text = getString(R.string.pause)
            updateHighScoreDisplay()
        }
    }
    
    private fun updateScoreDisplay(score: Int) {
        tvScore.text = getString(R.string.score, score)
    }
    
    private fun updateHighScoreDisplay() {
        val highScore = snakeGameView.getHighScore()
        tvHighScore.text = getString(R.string.high_score, highScore)
    }
    
    override fun onResume() {
        super.onResume()
        snakeGameView.resumeGame()
    }
    
    override fun onPause() {
        super.onPause()
        snakeGameView.pauseGame()
    }
}