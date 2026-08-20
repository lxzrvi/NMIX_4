package com.lxzrvi.nmix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NmixApp()
        }
    }
}

@Composable
fun NmixApp() {
    var started by remember { mutableStateOf(false) }

    val background = Brush.verticalGradient(
        listOf(
            Color(0xFF19493A),
            Color(0xFF319B79),
            Color(0xFF173E33)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EVERYTHING WITH NUMBERS",
                color = Color(0xFFDCF8EF),
                fontSize = 12.sp
            )

            Text(
                text = "NMIX",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            Button(onClick = { started = !started }) {
                Text(if (started) "NMIX READY" else "START")
            }
        }
    }
}
