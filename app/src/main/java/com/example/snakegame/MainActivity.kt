package com.example.snakegame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
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
    
    private var gameHandler = Handler(Looper.getMainLooper())
    private var isGameRunning = false
    private var isPaused = false
    
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
        
        updateScoreDisplay()
    }
    
    private fun setupClickListeners() {
        startButton.setOnClickListener {
            if (!isGameRunning) {
                startGame()
            }
        }
        
        pauseButton.setOnClickListener {
            if (isGameRunning) {
                if (isPaused) {
                    resumeGame()
                } else {
                    pauseGame()
                }
            }
        }
        
        restartButton.setOnClickListener {
            restartGame()
        }
    }
    
    private fun startGame() {
        isGameRunning = true
        isPaused = false
        gameView.resetGame()
        startButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        restartButton.visibility = View.VISIBLE
        pauseButton.text = getString(R.string.pause)
        
        gameHandler.post(object : Runnable {
            override fun run() {
                if (isGameRunning && !isPaused) {
                    gameView.update()
                    updateScoreDisplay()
                    
                    if (gameView.isGameOver()) {
                        gameOver()
                    } else {
                        gameHandler.postDelayed(this, 150) // Game speed
                    }
                }
            }
        })
    }
    
    private fun pauseGame() {
        isPaused = true
        pauseButton.text = getString(R.string.resume)
    }
    
    private fun resumeGame() {
        isPaused = false
        pauseButton.text = getString(R.string.pause)
        gameHandler.post { startGame() }
    }
    
    private fun restartGame() {
        gameHandler.removeCallbacksAndMessages(null)
        isGameRunning = false
        isPaused = false
        gameView.resetGame()
        updateScoreDisplay()
        startButton.visibility = View.VISIBLE
        pauseButton.visibility = View.GONE
        restartButton.visibility = View.GONE
    }
    
    private fun gameOver() {
        isGameRunning = false
        pauseButton.visibility = View.GONE
        restartButton.visibility = View.VISIBLE
        startButton.visibility = View.VISIBLE
        startButton.text = getString(R.string.start_game)
    }
    
    private fun updateScoreDisplay() {
        scoreTextView.text = getString(R.string.score, gameView.getScore())
        highScoreTextView.text = getString(R.string.high_score, gameView.getHighScore())
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isGameRunning && !isPaused) {
            gameView.handleSwipe(event)
        }
        return super.onTouchEvent(event)
    }
    
    override fun onPause() {
        super.onPause()
        if (isGameRunning && !isPaused) {
            pauseGame()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameHandler.removeCallbacksAndMessages(null)
    }
}