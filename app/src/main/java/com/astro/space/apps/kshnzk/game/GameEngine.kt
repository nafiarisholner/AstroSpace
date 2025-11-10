package com.astro.space.apps.kshnzk.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine {
    
    fun initializeGame(screenSize: Size, level: GameLevel): GameState {
        val ship = Ship(
            position = Offset(screenSize.width / 2, screenSize.height / 2),
            velocity = Offset.Zero
        )
        
        val planets = generatePlanets(screenSize, level)
        val resources = generateResources(screenSize, level)
        val comets = generateComets(screenSize, level)
        val stars = generateStars(screenSize)
        
        return GameState(
            ship = ship,
            planets = planets,
            resources = resources,
            comets = comets,
            stars = stars,
            level = level,
            screenSize = screenSize
        )
    }
    
    private fun generatePlanets(screenSize: Size, level: GameLevel): List<Planet> {
        val planets = mutableListOf<Planet>()
        val planetCount = (5 * level.difficulty).toInt()
        
        repeat(planetCount) {
            val radius = Random.nextFloat() * 60f + 40f
            val x = Random.nextFloat() * screenSize.width
            val y = Random.nextFloat() * screenSize.height
            planets.add(Planet(Offset(x, y), radius))
        }
        
        return planets
    }
    
    private fun generateResources(screenSize: Size, level: GameLevel): MutableList<Resource> {
        val resources = mutableListOf<Resource>()
        val resourceCount = (15 * level.difficulty).toInt()
        
        repeat(resourceCount) {
            val x = Random.nextFloat() * screenSize.width
            val y = Random.nextFloat() * screenSize.height
            resources.add(Resource(Offset(x, y)))
        }
        
        return resources
    }
    
    private fun generateComets(screenSize: Size, level: GameLevel): MutableList<Comet> {
        val comets = mutableListOf<Comet>()
        val cometCount = (3 * level.difficulty).toInt()
        
        repeat(cometCount) {
            val startX = if (Random.nextBoolean()) -50f else screenSize.width + 50f
            val startY = Random.nextFloat() * screenSize.height
            val targetX = if (startX < 0) screenSize.width + 50f else -50f
            val targetY = Random.nextFloat() * screenSize.height
            
            val direction = Offset(targetX - startX, targetY - startY)
            val distance = sqrt(direction.x * direction.x + direction.y * direction.y)
            val speed = 2f + level.difficulty * 0.5f
            val velocity = Offset(
                (direction.x / distance) * speed,
                (direction.y / distance) * speed
            )
            
            comets.add(Comet(
                position = Offset(startX, startY),
                velocity = velocity
            ))
        }
        
        return comets
    }
    
    private fun generateStars(screenSize: Size): List<Star> {
        val stars = mutableListOf<Star>()
        val starCount = 100
        
        repeat(starCount) {
            val x = Random.nextFloat() * screenSize.width
            val y = Random.nextFloat() * screenSize.height
            val brightness = Random.nextFloat() * 0.5f + 0.5f
            stars.add(Star(Offset(x, y), brightness = brightness))
        }
        
        return stars
    }
    
    fun updateGameState(gameState: GameState) {
        if (gameState.paused || gameState.gameOver) return
        
        gameState.update()
        
        val ship = gameState.ship
        ship.position = Offset(
            ship.position.x.coerceIn(0f, gameState.screenSize.width),
            ship.position.y.coerceIn(0f, gameState.screenSize.height)
        )
        
        updateComets(gameState)
        
        checkCometCollisions(gameState)
        
        checkResourceCollection(gameState)
        
        if (gameState.resources.count { !it.collected } < 5) {
            addNewResource(gameState)
        }
        
        if (gameState.comets.size < (3 * gameState.level.difficulty).toInt()) {
            addNewComet(gameState)
        }
    }
    
    private fun updateComets(gameState: GameState) {
        val cometsToRemove = mutableListOf<Comet>()
        
        gameState.comets.forEach { comet ->
            if (comet.position.x < -100f || comet.position.x > gameState.screenSize.width + 100f ||
                comet.position.y < -100f || comet.position.y > gameState.screenSize.height + 100f) {
                cometsToRemove.add(comet)
            }
        }
        
        gameState.comets.removeAll(cometsToRemove)
    }
    
    private fun addNewComet(gameState: GameState) {
        val startX = if (Random.nextBoolean()) -50f else gameState.screenSize.width + 50f
        val startY = Random.nextFloat() * gameState.screenSize.height
        val targetX = if (startX < 0) gameState.screenSize.width + 50f else -50f
        val targetY = Random.nextFloat() * gameState.screenSize.height
        
        val direction = Offset(targetX - startX, targetY - startY)
        val distance = sqrt(direction.x * direction.x + direction.y * direction.y)
        val speed = 2f + gameState.level.difficulty * 0.5f
        val velocity = Offset(
            (direction.x / distance) * speed,
            (direction.y / distance) * speed
        )
        
        gameState.comets.add(Comet(
            position = Offset(startX, startY),
            velocity = velocity
        ))
    }
    
    private fun checkCometCollisions(gameState: GameState) {
        val ship = gameState.ship
        val shipRadius = ship.size / 2
        
        gameState.comets.forEach { comet ->
            val distance = distanceBetween(ship.position, comet.position)
            val cometRadius = comet.size / 2
            
            if (distance < shipRadius + cometRadius) {
                gameState.gameOver = true
            }
        }
    }
    
    private fun checkResourceCollection(gameState: GameState) {
        val ship = gameState.ship
        val shipRadius = ship.size / 2
        
        gameState.resources.forEach { resource ->
            if (!resource.collected) {
                val distance = distanceBetween(ship.position, resource.position)
                val resourceRadius = resource.size / 2
                
                if (distance < shipRadius + resourceRadius) {
                    resource.collected = true
                    gameState.score += 10
                }
            }
        }
    }
    
    private fun addNewResource(gameState: GameState) {
        val x = Random.nextFloat() * gameState.screenSize.width
        val y = Random.nextFloat() * gameState.screenSize.height
        gameState.resources.add(Resource(Offset(x, y)))
    }
    
    fun moveShipTowards(gameState: GameState, target: Offset) {
        val ship = gameState.ship
        val direction = target - ship.position
        val distance = sqrt(direction.x * direction.x + direction.y * direction.y)
        
        if (distance > 10f) {
            val normalizedDirection = Offset(
                direction.x / distance,
                direction.y / distance
            )
            ship.velocity = Offset(
                normalizedDirection.x * ship.speed,
                normalizedDirection.y * ship.speed
            )
        } else {
            ship.velocity = Offset.Zero
        }
    }
    
    private fun distanceBetween(p1: Offset, p2: Offset): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
}

