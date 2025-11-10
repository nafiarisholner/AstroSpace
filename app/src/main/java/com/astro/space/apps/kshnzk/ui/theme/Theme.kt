package com.astro.space.apps.kshnzk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.astro.space.apps.kshnzk.ui.theme.SpaceBackground
import com.astro.space.apps.kshnzk.ui.theme.SpaceDarkRed40
import com.astro.space.apps.kshnzk.ui.theme.SpaceDarkRed80
import com.astro.space.apps.kshnzk.ui.theme.SpaceRed40
import com.astro.space.apps.kshnzk.ui.theme.SpaceRed80
import com.astro.space.apps.kshnzk.ui.theme.SpaceRedGrey40
import com.astro.space.apps.kshnzk.ui.theme.SpaceRedGrey80

private val DarkColorScheme = darkColorScheme(
    primary = SpaceRed80,
    secondary = SpaceRedGrey80,
    tertiary = SpaceDarkRed80,
    background = SpaceBackground,
    surface = SpaceBackground
)

private val LightColorScheme = lightColorScheme(
    primary = SpaceRed40,
    secondary = SpaceRedGrey40,
    tertiary = SpaceDarkRed40,
    background = SpaceBackground,
    surface = SpaceBackground
)

@Composable
fun AstroSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}