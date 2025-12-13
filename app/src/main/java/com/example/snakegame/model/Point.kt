package com.example.snakegame.model

data class Point(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Point
        return x == other.x && y == other.y
    }
    
    override fun hashCode(): Int {
        return 31 * x + y
    }
}