package com.fadhilmanfa.pingo

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fadhilmanfa.pingo.ui.pages.BrowsingMainPage
import com.fadhilmanfa.pingo.ui.theme.PingoTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val sharedPrefs = remember {
                getSharedPreferences("pingo_settings", Context.MODE_PRIVATE)
            }
            var themeMode by remember {
                mutableStateOf(sharedPrefs.getString("theme_mode", "system") ?: "system")
            }

            PingoTheme(themeMode = themeMode) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                ) {
                    BrowsingMainPage(
                            currentTheme = themeMode,
                            onThemeChanged = { newTheme ->
                                themeMode = newTheme
                                sharedPrefs.edit().putString("theme_mode", newTheme).apply()
                            }
                    )
                }
            }
        }
    }
}
