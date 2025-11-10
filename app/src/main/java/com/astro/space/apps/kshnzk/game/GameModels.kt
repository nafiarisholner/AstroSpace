package com.astro.space.apps.kshnzk.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

enum class GameLevel(val id: Int, val displayName: String, val backgroundColor: Color, val difficulty: Float, val requiredScore: Int) {
    LEVEL_1(1, "Nebula", Color(0xFF1A0000), 1.0f, 0),
    LEVEL_2(2, "Mars", Color(0xFF2A0000), 1.5f, 100),
    LEVEL_3(3, "Deep Space", Color(0xFF0A0000), 2.0f, 250),
    LEVEL_4(4, "Red Giant", Color(0xFF3A0000), 2.5f, 500),
    LEVEL_5(5, "Supernova", Color(0xFF4A0000), 3.0f, 1000);
    
    companion object {
        fun getBackgroundImageResId(level: GameLevel): Int {
            return when (level) {
                LEVEL_1 -> com.astro.space.apps.kshnzk.R.drawable.level_1_background
                LEVEL_2 -> com.astro.space.apps.kshnzk.R.drawable.level_2_background
                LEVEL_3 -> com.astro.space.apps.kshnzk.R.drawable.level_3_background
                LEVEL_4 -> com.astro.space.apps.kshnzk.R.drawable.level_4_background
                LEVEL_5 -> com.astro.space.apps.kshnzk.R.drawable.level_5_background
            }
        }
    }
}

data class Ship(
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    val size: Float = 80f,
    val speed: Float = 5f
) {
    fun update() {
        position = position + velocity
    }
}

data class Planet(
    val position: Offset,
    val radius: Float,
    val rotation: Float = 0f
)

data class Resource(
    var position: Offset,
    val size: Float = 20f,
    var collected: Boolean = false,
    var rotation: Float = 0f
) {
    fun update() {
        rotation += 2f
    }
}

data class Comet(
    var position: Offset,
    var velocity: Offset,
    val size: Float = 60f,
    var rotation: Float = 0f
) {
    fun update() {
        position = position + velocity
        rotation += 3f
    }
}

data class Star(
    val position: Offset,
    val size: Float = 2f,
    val brightness: Float = 1f
)

data class GameState(
    var ship: Ship,
    val planets: List<Planet> = emptyList(),
    val resources: MutableList<Resource> = mutableListOf(),
    val comets: MutableList<Comet> = mutableListOf(),
    val stars: List<Star> = emptyList(),
    var score: Int = 0,
    var gameOver: Boolean = false,
    var paused: Boolean = false,
    var level: GameLevel = GameLevel.LEVEL_1,
    var screenSize: Size = Size(0f, 0f)
) {
    fun update() {
        ship.update()
        resources.forEach { it.update() }
        comets.forEach { it.update() }
    }
}

