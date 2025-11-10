package com.astro.space.apps.kshnzk.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.astro.space.apps.kshnzk.ui.theme.SpacePlanet
import com.astro.space.apps.kshnzk.ui.theme.SpaceResource
import com.astro.space.apps.kshnzk.ui.theme.SpaceShip
import com.astro.space.apps.kshnzk.ui.theme.SpaceStar
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpaceGameScreen(
    level: GameLevel,
    onPause: () -> Unit,
    onGameOver: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val levelProgress = remember { LevelProgress(context) }
    val gameEngine = remember { GameEngine() }
    var gameState by remember { mutableStateOf<GameState?>(null) }
    var screenSize by remember { mutableStateOf(Size(0f, 0f)) }
    var targetPosition by remember { mutableStateOf<Offset?>(null) }
    var showPauseMenu by remember { mutableStateOf(false) }
    var showScoreNotification by remember { mutableStateOf(false) }
    var frameCount by remember { mutableLongStateOf(0L) }
    
    BackHandler(enabled = true) {
        gameState?.let { state ->
            if (!state.gameOver) {
                if (!state.paused && !showPauseMenu) {
                    showPauseMenu = true
                    state.paused = true
                }
            }
        }
    }
    
    val shipImage = remember(context) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, com.astro.space.apps.kshnzk.R.drawable.ship)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    val cometImage = remember(context) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, com.astro.space.apps.kshnzk.R.drawable.comet)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )
    
    LaunchedEffect(level) {
        if (screenSize.width > 0 && screenSize.height > 0 && gameState == null) {
            gameState = gameEngine.initializeGame(screenSize, level)
            showPauseMenu = false
            showScoreNotification = false
        }
    }
    
    LaunchedEffect(screenSize) {
        gameState?.let { state ->
            if (screenSize.width > 0 && screenSize.height > 0 && 
                (state.screenSize.width != screenSize.width || state.screenSize.height != screenSize.height)) {
                state.screenSize = screenSize
                state.ship.position = Offset(
                    state.ship.position.x.coerceIn(0f, screenSize.width),
                    state.ship.position.y.coerceIn(0f, screenSize.height)
                )
            }
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            gameState?.let { state ->
                val wasGameOver = state.gameOver
                
                if (!state.paused && !state.gameOver) {
                    targetPosition?.let { target ->
                        gameEngine.moveShipTowards(state, target)
                    }
                    gameEngine.updateGameState(state)
                    frameCount++
                    gameState = state
                    
                    val previousBest = levelProgress.getBestScore(state.level)
                    if (state.score > previousBest) {
                        levelProgress.saveScore(state.level, state.score)
                    }
                    
                    if (state.level.id < GameLevel.entries.size) {
                        val nextLevel = GameLevel.entries[state.level.id]
                        val scoreThreshold = when (state.level.id) {
                            1 -> 100
                            2 -> 250
                            3 -> 500
                            4 -> 1000
                            else -> -1
                        }
                        
                        val currentBest = levelProgress.getBestScore(state.level)
                        val isNextLevelUnlocked = levelProgress.isLevelUnlocked(nextLevel)
                        val wasToastShown = levelProgress.wasToastShownForLevel(state.level)
                        
                        if (scoreThreshold > 0 && state.score >= scoreThreshold && !wasToastShown && !showScoreNotification) {
                            if (!isNextLevelUnlocked && state.score >= nextLevel.requiredScore) {
                                levelProgress.unlockLevel(nextLevel)
                            }
                            
                            levelProgress.markToastShownForLevel(state.level)
                            showScoreNotification = true
                            
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Time to move to Level ${nextLevel.id}: ${nextLevel.displayName}!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            
                            kotlinx.coroutines.delay(3500)
                            showScoreNotification = false
                        }
                    }
                }
                
                if (state.gameOver && !wasGameOver) {
                    levelProgress.saveScore(state.level, state.score)
                    onGameOver()
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = Size(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (!showPauseMenu) {
                        targetPosition = tapOffset
                        gameState?.let { state ->
                            if (!state.paused && !state.gameOver) {
                                gameEngine.moveShipTowards(state, tapOffset)
                            }
                        }
                    }
                }
            }
    ) {
        Image(
            painter = painterResource(id = GameLevel.getBackgroundImageResId(level)),
            contentDescription = "Level background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        gameState?.let { state ->
            key(frameCount) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.resources.forEach { resource ->
                        if (!resource.collected) {
                            drawResource(resource)
                        }
                    }
                    
                    state.comets.forEach { comet ->
                        if (cometImage != null) {
                            drawCometImage(comet, cometImage)
                        } else {
                            drawComet(comet)
                        }
                    }
                    
                    if (shipImage != null) {
                        drawShipImage(state.ship, shipImage)
                    } else {
                        drawShip(state.ship)
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Score: ${state.score}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Best: ${levelProgress.getBestScore(state.level)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Level: ${state.level.displayName}",
                            color = Color.White,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Resources: ${state.resources.count { !it.collected }}",
                            color = Color.White,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    if (!state.gameOver) {
                        Text(
                            text = "PAUSE",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    showPauseMenu = true
                                    state.paused = true
                                }
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                        )
                    }
                }
            }
            
            if (state.gameOver) {
                GameOverScreen(
                    score = state.score,
                    level = state.level,
                    onRestart = {
                        gameState = gameEngine.initializeGame(screenSize, level)
                    },
                    onMainMenu = {
                        onBackToMenu()
                    }
                )
            }
            else if (showPauseMenu || state.paused) {
                PauseMenuScreen(
                    onResume = {
                        showPauseMenu = false
                        state.paused = false
                    },
                    onRestart = {
                        gameState = gameEngine.initializeGame(screenSize, level)
                        showPauseMenu = false
                    },
                    onMainMenu = {
                        onBackToMenu()
                    }
                )
            }
            
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    level: GameLevel,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GAME OVER",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Final Score: $score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Text(
                    text = "Level: ${level.displayName}",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            
            MenuButton(
                text = "Restart",
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(top = 32.dp)
            )
            
            MenuButton(
                text = "Main Menu",
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(top = 16.dp)
            )
        }
    }
}

private fun DrawScope.drawStars(stars: List<Star>, animation: Float) {
    stars.forEach { star ->
        val alpha = (star.brightness + animation * 0.3f).coerceIn(0.3f, 1f)
        drawCircle(
            color = SpaceStar.copy(alpha = alpha),
            radius = star.size,
            center = star.position
        )
    }
}

private fun DrawScope.drawPlanet(planet: Planet) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                SpacePlanet.copy(alpha = 0.9f),
                SpacePlanet.copy(alpha = 0.5f),
                SpacePlanet.copy(alpha = 0.2f)
            ),
            center = planet.position,
            radius = planet.radius
        ),
        radius = planet.radius,
        center = planet.position
    )
    
    drawCircle(
        color = SpacePlanet.copy(alpha = 0.3f),
        radius = planet.radius * 1.2f,
        center = planet.position,
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawResource(resource: Resource) {
    val path = Path().apply {
        val center = resource.position
        val size = resource.size / 2
        
        for (i in 0 until 8) {
            val angle = (i * 45f + resource.rotation) * (Math.PI / 180).toFloat()
            val radius = if (i % 2 == 0) size else size * 0.5f
            val x = center.x + cos(angle) * radius
            val y = center.y + sin(angle) * radius
            
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }
    
    drawPath(
        path = path,
        color = SpaceResource,
        style = Stroke(width = 3f)
    )
    
    drawCircle(
        color = SpaceResource,
        radius = 4f,
        center = resource.position
    )
}

private fun DrawScope.drawComet(comet: Comet) {
    val center = comet.position
    val size = comet.size
    
    val tailLength = size * 2f
    val tailPath = Path().apply {
        val tailStart = Offset(
            center.x - cos(comet.rotation * (Math.PI / 180).toFloat()) * tailLength,
            center.y - sin(comet.rotation * (Math.PI / 180).toFloat()) * tailLength
        )
        moveTo(tailStart.x, tailStart.y)
        lineTo(center.x, center.y)
    }
    
    drawPath(
        path = tailPath,
        color = Color(0xFFFF6600).copy(alpha = 0.6f),
        style = Stroke(width = 3f)
    )
    
    rotate(comet.rotation, center) {
        val cometPath = Path().apply {
            moveTo(center.x + size / 2, center.y)
            lineTo(center.x - size / 2, center.y - size / 3)
            lineTo(center.x - size / 2, center.y + size / 3)
            close()
        }
        
        drawPath(
            path = cometPath,
            color = Color(0xFFFF4444),
            style = Stroke(width = 2f)
        )
        
        drawPath(
            path = cometPath,
            color = Color(0xFFFF4444).copy(alpha = 0.5f)
        )
    }
}

private fun DrawScope.drawShip(ship: Ship) {
    val center = ship.position
    val size = ship.size
    
    val angle = if (ship.velocity != Offset.Zero) {
        kotlin.math.atan2(ship.velocity.y, ship.velocity.x) * (180 / Math.PI).toFloat()
    } else {
        0f
    }
    
    rotate(angle, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size / 2)
            lineTo(center.x - size / 2, center.y + size / 2)
            lineTo(center.x + size / 2, center.y + size / 2)
            close()
        }
        
        drawPath(
            path = path,
            color = SpaceShip,
            style = Stroke(width = 4f)
        )
        
        drawPath(
            path = path,
            color = SpaceShip.copy(alpha = 0.5f)
        )
        
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = size / 6,
            center = Offset(center.x, center.y - size / 6)
        )
    }
    
    if (ship.velocity != Offset.Zero) {
        val enginePath = Path().apply {
            val backX = center.x - cos(angle * (Math.PI / 180).toFloat()) * size / 2
            val backY = center.y - sin(angle * (Math.PI / 180).toFloat()) * size / 2
            
            moveTo(backX - size / 4, backY)
            lineTo(backX, backY + size / 3)
            lineTo(backX + size / 4, backY)
        }
        
        drawPath(
            path = enginePath,
            color = Color(0xFFFF6600).copy(alpha = 0.6f)
        )
    }
}

private fun DrawScope.drawShipImage(ship: Ship, shipImage: ImageBitmap) {
    val center = ship.position
    val size = ship.size
    
    val angle = if (ship.velocity != Offset.Zero) {
        kotlin.math.atan2(ship.velocity.y, ship.velocity.x) * (180 / Math.PI).toFloat() + 90f
    } else {
        90f
    }
    
    val imageSize = Size(shipImage.width.toFloat(), shipImage.height.toFloat())
    val scale = size / imageSize.width.coerceAtLeast(imageSize.height)
    val scaledSize = Size(imageSize.width * scale, imageSize.height * scale)
    
    rotate(angle, center) {
        val topLeftX = (center.x - scaledSize.width / 2).toInt()
        val topLeftY = (center.y - scaledSize.height / 2).toInt()
        val paint = Paint()
        drawContext.canvas.drawImageRect(
            image = shipImage,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(shipImage.width, shipImage.height),
            dstOffset = IntOffset(topLeftX, topLeftY),
            dstSize = IntSize(scaledSize.width.toInt(), scaledSize.height.toInt()),
            paint = paint
        )
    }
    
    if (ship.velocity != Offset.Zero) {
        val movementAngle = kotlin.math.atan2(ship.velocity.y, ship.velocity.x) * (180 / Math.PI).toFloat()
        val backX = center.x - cos(movementAngle * (Math.PI / 180).toFloat()) * size / 2
        val backY = center.y - sin(movementAngle * (Math.PI / 180).toFloat()) * size / 2
        
        val enginePath = Path().apply {
            moveTo(backX - size / 4, backY)
            lineTo(backX, backY + size / 3)
            lineTo(backX + size / 4, backY)
        }
        
        drawPath(
            path = enginePath,
            color = Color(0xFFFF6600).copy(alpha = 0.6f)
        )
    }
}

private fun DrawScope.drawCometImage(comet: Comet, cometImage: ImageBitmap) {
    val center = comet.position
    val size = comet.size
    
    val imageSize = Size(cometImage.width.toFloat(), cometImage.height.toFloat())
    val scale = size / imageSize.width.coerceAtLeast(imageSize.height)
    val scaledSize = Size(imageSize.width * scale, imageSize.height * scale)
    
    rotate(comet.rotation, center) {
        val topLeftX = (center.x - scaledSize.width / 2).toInt()
        val topLeftY = (center.y - scaledSize.height / 2).toInt()
        val paint = Paint()
        drawContext.canvas.drawImageRect(
            image = cometImage,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(cometImage.width, cometImage.height),
            dstOffset = IntOffset(topLeftX, topLeftY),
            dstSize = IntSize(scaledSize.width.toInt(), scaledSize.height.toInt()),
            paint = paint
        )
    }
}
