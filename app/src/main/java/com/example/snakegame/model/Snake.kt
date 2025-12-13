package com.example.snakegame.model

class Snake(
    private val gridSize: Int,
    private val gameWidth: Int,
    private val gameHeight: Int
) {
    private val body = mutableListOf<Point>()
    var direction = Direction.RIGHT
        private set
    var nextDirection = Direction.RIGHT
    
    init {
        reset()
    }
    
    fun reset() {
        body.clear()
        val startX = gridSize / 2
        val startY = gridSize / 2
        // Create initial snake with 3 segments
        for (i in 0..2) {
            body.add(Point(startX - i, startY))
        }
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
    }
    
    fun move(): Boolean {
        // Update direction
        direction = nextDirection
        
        // Calculate new head position
        val head = body.first()
        val newHead = when (direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }
        
        // Check wall collision
        if (newHead.x < 0 || newHead.x >= gridSize || 
            newHead.y < 0 || newHead.y >= gridSize) {
            return false
        }
        
        // Check self collision
        if (body.contains(newHead)) {
            return false
        }
        
        // Add new head
        body.add(0, newHead)
        // Remove tail (will be added back if food is eaten)
        return true
    }
    
    fun grow() {
        // Snake grows by not removing tail
    }
    
    fun shrink() {
        if (body.size > 3) {
            body.removeAt(body.size - 1)
        }
    }
    
    fun getBody(): List<Point> = body.toList()
    
    fun getHead(): Point = body.first()
    
    fun setDirection(newDirection: Direction) {
        // Prevent 180-degree turns
        when (newDirection) {
            Direction.UP -> if (direction != Direction.DOWN) nextDirection = newDirection
            Direction.DOWN -> if (direction != Direction.UP) nextDirection = newDirection
            Direction.LEFT -> if (direction != Direction.RIGHT) nextDirection = newDirection
            Direction.RIGHT -> if (direction != Direction.LEFT) nextDirection = newDirection
        }
    }
    
    fun getLength(): Int = body.size
}