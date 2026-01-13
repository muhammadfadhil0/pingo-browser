package com.fadhilmanfa.pingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.fadhilmanfa.pingo.ui.pages.BrowsingMainPage
import com.fadhilmanfa.pingo.ui.theme.PingoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Memastikan sistem navigasi dan status bar transparan
        enableEdgeToEdge()
        
        setContent {
            PingoTheme {
                // Gunakan Box sebagai root dengan background warna solid
                // Ini mencegah "jejak hitam" karena area di bawah keyboard akan berwarna ini
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    BrowsingMainPage()
                }
            }
        }
    }
}
