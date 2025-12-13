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
    private lateinit var btnControl: Button
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // View'leri bağla
        snakeGameView = findViewById(R.id.snakeGameView)
        tvScore = findViewById(R.id.tvScore)
        tvHighScore = findViewById(R.id.tvHighScore)
        tvGameOver = findViewById(R.id.tvGameOver)
        btnControl = findViewById(R.id.btnControl)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)
        
        // Oyun durumunu güncelle
        updateScore()
        updateHighScore()
        
        // Kontrol butonu
        btnControl.setOnClickListener {
            if (snakeGameView.isGameRunning()) {
                if (snakeGameView.isGamePaused()) {
                    snakeGameView.resumeGame()
                    btnControl.text = getString(R.string.pause_game)
                } else {
                    snakeGameView.pauseGame()
                    btnControl.text = getString(R.string.resume_game)
                }
            } else {
                snakeGameView.startGame()
                btnControl.text = getString(R.string.pause_game)
                tvGameOver.visibility = TextView.GONE
            }
        }
        
        // Yeniden başlat butonu
        btnRestart.setOnClickListener {
            snakeGameView.restartGame()
            btnControl.text = getString(R.string.start_game)
            tvGameOver.visibility = TextView.GONE
            updateScore()
        }
        
        // Çıkış butonu
        btnExit.setOnClickListener {
            finish()
        }
        
        // Oyun durum değişikliklerini dinle
        snakeGameView.setOnGameStateChangeListener(object : SnakeGameView.OnGameStateChangeListener {
            override fun onScoreChanged(score: Int) {
                updateScore()
            }
            
            override fun onGameOver() {
                runOnUiThread {
                    tvGameOver.visibility = TextView.VISIBLE
                    btnControl.text = getString(R.string.start_game)
                    updateHighScore()
                }
            }
            
            override fun onGameStarted() {
                runOnUiThread {
                    btnControl.text = getString(R.string.pause_game)
                }
            }
        })
    }
    
    private fun updateScore() {
        tvScore.text = getString(R.string.score, snakeGameView.getScore())
    }
    
    private fun updateHighScore() {
        tvHighScore.text = getString(R.string.high_score, snakeGameView.getHighScore())
    }
    
    override fun onPause() {
        super.onPause()
        snakeGameView.pauseGame()
    }
    
    override fun onResume() {
        super.onResume()
        if (snakeGameView.isGameRunning() && !snakeGameView.isGamePaused()) {
            snakeGameView.resumeGame()
        }
    }
}