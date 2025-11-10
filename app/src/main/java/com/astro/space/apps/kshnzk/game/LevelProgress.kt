package com.astro.space.apps.kshnzk.game

import android.content.Context
import android.content.SharedPreferences

class LevelProgress(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("level_progress", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_PREFIX = "level_score_"
        private const val KEY_UNLOCKED = "unlocked_levels"
        private const val KEY_TOAST_SHOWN = "toast_shown_level_"
        
        val requiredScores = mapOf(
            GameLevel.LEVEL_1 to 0,
            GameLevel.LEVEL_2 to 100,
            GameLevel.LEVEL_3 to 250,
            GameLevel.LEVEL_4 to 500,
            GameLevel.LEVEL_5 to 1000
        )
    }
    
    fun getBestScore(level: GameLevel): Int {
        return prefs.getInt("${KEY_PREFIX}${level.id}", 0)
    }
    
    fun saveScore(level: GameLevel, score: Int) {
        val currentBest = getBestScore(level)
        if (score > currentBest) {
            prefs.edit().putInt("${KEY_PREFIX}${level.id}", score).apply()
        }
    }
    
    fun isLevelUnlocked(level: GameLevel): Boolean {
        if (level == GameLevel.LEVEL_1) return true
        
        val previousLevel = GameLevel.values()[level.id - 2]
        val bestScore = getBestScore(previousLevel)
        val requiredScore = level.requiredScore
        
        return bestScore >= requiredScore
    }
    
    fun unlockLevel(level: GameLevel) {
        val unlocked = getUnlockedLevels().toMutableSet()
        unlocked.add(level.id)
        prefs.edit().putStringSet(KEY_UNLOCKED, unlocked.map { it.toString() }.toSet()).apply()
    }
    
    private fun getUnlockedLevels(): Set<Int> {
        return prefs.getStringSet(KEY_UNLOCKED, setOf("1"))?.map { it.toInt() }?.toSet() ?: setOf(1)
    }
    
    fun wasToastShownForLevel(level: GameLevel): Boolean {
        return prefs.getBoolean("${KEY_TOAST_SHOWN}${level.id}", false)
    }
    
    fun markToastShownForLevel(level: GameLevel) {
        prefs.edit().putBoolean("${KEY_TOAST_SHOWN}${level.id}", true).apply()
    }
    
    fun checkAndUnlockNextLevel(currentLevel: GameLevel, currentScore: Int) {
        saveScore(currentLevel, currentScore)
        
        if (currentLevel.id < GameLevel.values().size) {
            val nextLevel = GameLevel.values()[currentLevel.id]
            if (!isLevelUnlocked(nextLevel)) {
                val requiredScore = requiredScores[nextLevel] ?: 0
                val previousLevel = GameLevel.values()[currentLevel.id - 1]
                val bestScore = getBestScore(previousLevel)
                
                if (bestScore >= requiredScore) {
                    unlockLevel(nextLevel)
                }
            }
        }
    }
}

