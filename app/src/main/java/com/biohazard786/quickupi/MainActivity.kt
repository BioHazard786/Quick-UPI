package com.biohazard786.quickupi

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.biohazard786.quickupi.data.UserStore
import com.biohazard786.quickupi.ui.QuickUpiApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userStore = UserStore(this)

        setContent {
            QuickUpiTheme {
                QuickUpiApp(
                    userStore = userStore,
                    onQrShown = { maxBrightness() },
                    onRestoreBrightness = { restoreBrightness() },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private fun maxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f // 1.0f is 100% brightness
        window.attributes = layoutParams
    }

    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness =
            android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}

// Simple Theme Wrapper
@Composable
fun QuickUpiTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme =
        when {
            supportsDynamicColor && isDarkTheme -> {
                dynamicDarkColorScheme(LocalContext.current)
            }

            supportsDynamicColor && !isDarkTheme -> {
                dynamicLightColorScheme(LocalContext.current)
            }

            isDarkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}