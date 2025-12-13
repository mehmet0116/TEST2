package com.example.snakegame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.snakegame.model.Direction
import com.example.snakegame.model.GameState

class MainActivity : AppCompatActivity() {
    
    private lateinit var snakeGameView: SnakeGameView
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnRestart: Button
    private lateinit var btnUp: Button
    private lateinit var btnDown: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button
    
    private val gameHandler = Handler(Looper.getMainLooper())
    private val gameUpdateInterval = 150L // milliseconds
    
    private val gameUpdateRunnable = object : Runnable {
        override fun run() {
            snakeGameView.update()
            updateUI()
            gameHandler.postDelayed(this, gameUpdateInterval)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupClickListeners()
        updateUI()
    }
    
    private fun initializeViews() {
        snakeGameView = findViewById(R.id.snakeGameView)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnRestart = findViewById(R.id.btnRestart)
        btnUp = findViewById(R.id.btnUp)
        btnDown = findViewById(R.id.btnDown)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
    }
    
    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            when (snakeGameView.getGame().gameState) {
                GameState.NOT_STARTED, GameState.GAME_OVER -> {
                    snakeGameView.getGame().start()
                    startGameLoop()
                    updateButtonStates()
                }
                GameState.PAUSED -> {
                    snakeGameView.getGame().resume()
                    startGameLoop()
                    updateButtonStates()
                }
                else -> {}
            }
        }
        
        btnPause.setOnClickListener {
            if (snakeGameView.getGame().gameState == GameState.RUNNING) {
                snakeGameView.getGame().pause()
                stopGameLoop()
                updateButtonStates()
            }
        }
        
        btnRestart.setOnClickListener {
            snakeGameView.getGame().restart()
            startGameLoop()
            updateButtonStates()
        }
        
        // Direction buttons
        btnUp.setOnClickListener { snakeGameView.getGame().setDirection(Direction.UP) }
        btnDown.setOnClickListener { snakeGameView.getGame().setDirection(Direction.DOWN) }
        btnLeft.setOnClickListener { snakeGameView.getGame().setDirection(Direction.LEFT) }
        btnRight.setOnClickListener { snakeGameView.getGame().setDirection(Direction.RIGHT) }
    }
    
    private fun startGameLoop() {
        stopGameLoop() // Ensure no duplicate loops
        gameHandler.post(gameUpdateRunnable)
    }
    
    private fun stopGameLoop() {
        gameHandler.removeCallbacks(gameUpdateRunnable)
    }
    
    private fun updateUI() {
        val game = snakeGameView.getGame()
        tvScore.text = getString(R.string.score, game.getScore())
        tvHighScore.text = getString(R.string.high_score, game.getHighScore())
        
        // Update button states based on game state
        updateButtonStates()
        
        // Force redraw
        snakeGameView.invalidate()
    }
    
    private fun updateButtonStates() {
        val gameState = snakeGameView.getGame().gameState
        
        when (gameState) {
            GameState.NOT_STARTED -> {
                btnStart.text = getString(R.string.start_game)
                btnStart.isEnabled = true
                btnPause.isEnabled = false
                btnRestart.isEnabled = false
            }
            GameState.RUNNING -> {
                btnStart.text = getString(R.string.pause_game)
                btnStart.isEnabled = false
                btnPause.isEnabled = true
                btnRestart.isEnabled = true
            }
            GameState.PAUSED -> {
                btnStart.text = getString(R.string.resume_game)
                btnStart.isEnabled = true
                btnPause.isEnabled = false
                btnRestart.isEnabled = true
            }
            GameState.GAME_OVER -> {
                btnStart.text = getString(R.string.start_game)
                btnStart.isEnabled = true
                btnPause.isEnabled = false
                btnRestart.isEnabled = true
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        snakeGameView.getGame().pause()
        stopGameLoop()
    }
    
    override fun onResume() {
        super.onResume()
        if (snakeGameView.getGame().gameState == GameState.RUNNING) {
            startGameLoop()
        }
        updateUI()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopGameLoop()
    }
}