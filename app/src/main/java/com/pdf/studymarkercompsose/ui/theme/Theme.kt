package com.pdf.studymarkercompsose.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.pdf.studymarker.data.AppStyle
import com.pdf.studymarkercompsose.dataStore
import kotlinx.coroutines.flow.first

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun StudyMarkerCompsoseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    //val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var appStyle = AppStyle.System

    LaunchedEffect(key1 = Unit) {
        val appSettings = context.dataStore.data
        appStyle = appSettings.first().appStyle
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            //val context = LocalContext.current
            when(appStyle){
                AppStyle.Light -> dynamicLightColorScheme(context)
                AppStyle.Dark -> dynamicDarkColorScheme(context)
                AppStyle.System -> {
                    if (darkTheme ) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
            }

        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    Log.d("TAG", "StudyMarkerCompsoseTheme: $appStyle ,$colorScheme  ")

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}