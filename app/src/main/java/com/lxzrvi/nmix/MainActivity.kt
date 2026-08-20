package com.lxzrvi.nmix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NmixApp() }
    }
}

private val Emerald = Color(0xFF319B79)

@Composable
fun NmixApp() {
    var started by remember { mutableStateOf(false) }

    if (started) {
        MainScreen()
    } else {
        LandingScreen { started = true }
    }
}

@Composable
private fun LandingScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    val infinite = rememberInfiniteTransition(label = "bg")

    val x1 by infinite.animateFloat(
        -170f, 190f,
        infiniteRepeatable(tween(8000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "x1"
    )

    val y1 by infinite.animateFloat(
        -80f, 210f,
        infiniteRepeatable(tween(10500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "y1"
    )

    val x2 by infinite.animateFloat(
        170f, -180f,
        infiniteRepeatable(tween(12000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "x2"
    )

    val pulse by infinite.animateFloat(
        .88f, 1.18f,
        infiniteRepeatable(tween(6000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020A08),
                        Color(0xFF063126),
                        Color(0xFF0E5B45),
                        Color(0xFF07362A),
                        Color(0xFF020B08)
                    )
                )
            )
    ) {
        // Smooth full-screen animated color wash.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = x1 * .65f
                    translationY = y1 * .42f
                    scaleX = 1.55f
                    scaleY = 1.55f
                    rotationZ = x2 * .055f
                }
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF06291F).copy(alpha = .20f),
                            Color(0xFF42C79A).copy(alpha = .42f),
                            Color.Transparent,
                            Color(0xFF137A5B).copy(alpha = .36f)
                        )
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = x2 * .55f
                    translationY = -y1 * .38f
                    scaleX = 1.65f
                    scaleY = 1.65f
                    rotationZ = x1 * .08f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF8BE6C6).copy(alpha = .28f),
                            Color(0xFF249B76).copy(alpha = .14f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main branding + actions stay centered independently.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 22.dp)
                .offset(y = (-38).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color.White.copy(alpha = .72f),
                fontSize = 10.sp,
                letterSpacing = 2.35.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                "NMIX",
                color = Color.White,
                fontSize = 55.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(30.dp))

            GlassAction("Start", onStart)

            Spacer(Modifier.height(11.dp))

            GlassAction("Share") {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                    )
                }
                context.startActivity(Intent.createChooser(share, "Share NMIX"))
            }
        }

        // Bottom information area.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 27.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppInfoCard()

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "NMIX",
                    color = Color.White.copy(alpha = .72f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Text(
                    "  •  lxzrvi  •  © 2026",
                    color = Color.White.copy(alpha = .42f),
                    fontSize = 9.sp,
                    letterSpacing = .2.sp
                )
            }
        }
    }
}

@Composable
private fun GlassAction(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if (pressed) .955f else 1f,
        spring(dampingRatio = .62f, stiffness = 650f),
        label = "actionPress"
    )

    Box(
        Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .20f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White.copy(alpha = .94f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AppInfoCard() {
    val context = LocalContext.current

    val messages = remember {
        listOf(
            "One place for the numbers you use every day.",
            "Calculate, count and track time without leaving NMIX.",
            "Fast number tools designed for a clean native experience.",
            "Made to stay useful even when you're completely offline.",
            "Numbers and time, brought together in one simple app."
        )
    }

    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            index = (index + 1) % messages.size
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = .20f))
            .padding(14.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "APP INFO",
                        color = Color.White.copy(alpha = .6f),
                        fontSize = 8.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "NMIX",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    MiniGlassButton("Web") {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://lxzrvi.github.io/NMIX/")
                            )
                        )
                    }

                    MiniGlassButton("GitHub") {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/lxzrvi")
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(11.dp))

            // Exactly two horizontal boxes.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(11.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = {
                            fadeIn(tween(550)) togetherWith fadeOut(tween(400))
                        },
                        label = "details"
                    ) { i ->
                        Text(
                            messages[i],
                            color = Color.White.copy(alpha = .9f),
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Box(
                    Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(11.dp)
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "BUILT WITH",
                            color = Color.White.copy(alpha = .52f),
                            fontSize = 7.sp,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Kotlin\nJetpack Compose\nAndroid",
                            color = Color.White.copy(alpha = .9f),
                            fontSize = 9.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            "Native • Offline",
                            color = Color(0xFF9DEBD1),
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniGlassButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if (pressed) .9f else 1f,
        spring(stiffness = 700f),
        label = "mini"
    )

    Box(
        Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .18f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MainScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFDEDEDE)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "NMIX",
                color = Emerald,
                fontSize = 43.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color(0xFF66706C),
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
