package com.astro.space.apps.kshnzk.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.space.apps.kshnzk.ui.theme.SpaceBackground
import com.astro.space.apps.kshnzk.ui.theme.SpaceRed80

@Composable
fun MainMenuScreen(
    onLevelSelected: (GameLevel) -> Unit
) {
    BackHandler(enabled = true) {
    }
    val context = LocalContext.current
    val levelProgress = remember { LevelProgress(context) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ASTRO SPACE",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = SpaceRed80,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Select Level",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(GameLevel.values().toList()) { level ->
                    val isUnlocked = levelProgress.isLevelUnlocked(level)
                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        bestScore = levelProgress.getBestScore(level),
                        onClick = { 
                            if (isUnlocked) {
                                onLevelSelected(level)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelCard(
    level: GameLevel,
    isUnlocked: Boolean,
    bestScore: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SpaceRed80.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            level.backgroundColor.copy(alpha = if (isUnlocked) 0.5f else 0.2f),
                            level.backgroundColor.copy(alpha = if (isUnlocked) 0.3f else 0.1f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Level ${level.id}: ${level.displayName}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color.White else Color.Gray
                        )
                        Text(
                            text = "Difficulty: ${String.format("%.1f", level.difficulty)}x",
                            fontSize = 14.sp,
                            color = (if (isUnlocked) Color.White else Color.Gray).copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (bestScore > 0) {
                            Text(
                                text = "Best: $bestScore",
                                fontSize = 14.sp,
                                color = (if (isUnlocked) Color.White else Color.Gray).copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    if (!isUnlocked) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "LOCKED",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            if (level.id > 1) {
                                val previousLevel = GameLevel.values()[level.id - 2]
                                val requiredScore = level.requiredScore
                                Text(
                                    text = "Need ${requiredScore} pts",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

