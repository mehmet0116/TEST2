package com.example.snakegame

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.snakegame.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var gameView: GameView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ViewBinding kullanarak performans optimizasyonu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // GameView'i başlat
        gameView = GameView(this)
        binding.gameContainer.addView(gameView)
        
        setupUI()
        setupListeners()
        
        Timber.d("MainActivity created")
    }
    
    private fun setupUI() {
        // UI elementlerini başlat
        binding.scoreTextView.text = getString(R.string.score_format, 0)
        binding.highScoreTextView.text = getString(R.string.high_score_format, 0)
    }
    
    private fun setupListeners() {
        binding.startButton.setOnClickListener {
            startGame()
        }
        
        binding.pauseButton.setOnClickListener {
            pauseGame()
        }
        
        binding.restartButton.setOnClickListener {
            restartGame()
        }
        
        // Kontroller için dokunma alanları
        setupTouchControls()
    }
    
    private fun setupTouchControls() {
        binding.upButton.setOnClickListener {
            gameView.changeDirection(Direction.UP)
        }
        
        binding.downButton.setOnClickListener {
            gameView.changeDirection(Direction.DOWN)
        }
        
        binding.leftButton.setOnClickListener {
            gameView.changeDirection(Direction.LEFT)
        }
        
        binding.rightButton.setOnClickListener {
            gameView.changeDirection(Direction.RIGHT)
        }
    }
    
    private fun startGame() {
        gameView.startGame()
        binding.startButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
        binding.gameStatusTextView.text = getString(R.string.game_running)
        Timber.d("Game started")
    }
    
    private fun pauseGame() {
        if (gameView.isGameRunning()) {
            gameView.pauseGame()
            binding.pauseButton.text = getString(R.string.resume)
            binding.gameStatusTextView.text = getString(R.string.game_paused)
            Timber.d("Game paused")
        } else {
            gameView.resumeGame()
            binding.pauseButton.text = getString(R.string.pause)
            binding.gameStatusTextView.text = getString(R.string.game_running)
            Timber.d("Game resumed")
        }
    }
    
    private fun restartGame() {
        gameView.restartGame()
        binding.startButton.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.GONE
        binding.pauseButton.text = getString(R.string.pause)
        binding.gameStatusTextView.text = getString(R.string.game_ready)
        updateScore(0)
        Timber.d("Game restarted")
    }
    
    fun updateScore(score: Int) {
        binding.scoreTextView.text = getString(R.string.score_format, score)
    }
    
    fun updateHighScore(highScore: Int) {
        binding.highScoreTextView.text = getString(R.string.high_score_format, highScore)
    }
    
    fun gameOver() {
        binding.startButton.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.GONE
        binding.gameStatusTextView.text = getString(R.string.game_over)
        Timber.d("Game over")
    }
    
    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }
    
    override fun onResume() {
        super.onResume()
        if (gameView.isGameRunning()) {
            gameView.resumeGame()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameView.releaseResources()
        Timber.d("MainActivity destroyed")
    }
}