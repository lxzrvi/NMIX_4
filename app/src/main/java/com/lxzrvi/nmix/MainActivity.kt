package com.lxzrvi.nmix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
    val infinite = rememberInfiniteTransition(label = "background")

    val moveA by infinite.animateFloat(
        initialValue = -130f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            tween(6500, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "a"
    )

    val moveB by infinite.animateFloat(
        initialValue = 160f,
        targetValue = -150f,
        animationSpec = infiniteRepeatable(
            tween(8500, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "b"
    )

    val breathe by infinite.animateFloat(
        initialValue = .85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(5000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF020D0A),
                        Color(0xFF073326),
                        Color(0xFF16785B),
                        Color(0xFF0B4D39),
                        Color(0xFF021510)
                    )
                )
            )
    ) {
        // Large soft animated light masses
        Box(
            Modifier
                .offset(x = (-100).dp, y = 40.dp)
                .size(380.dp)
                .graphicsLayer {
                    translationX = moveA
                    translationY = moveB * .32f
                    scaleX = breathe
                    scaleY = breathe
                }
                .blur(80.dp)
                .background(
                    Color(0xFF55D5A9).copy(alpha = .7f),
                    RoundedCornerShape(50)
                )
        )

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 130.dp, y = 80.dp)
                .size(430.dp)
                .graphicsLayer {
                    translationX = moveB
                    translationY = moveA * .35f
                    scaleX = 1.25f - (breathe - .85f)
                    scaleY = 1.25f - (breathe - .85f)
                }
                .blur(95.dp)
                .background(
                    Color(0xFF15936C).copy(alpha = .7f),
                    RoundedCornerShape(50)
                )
        )

        Box(
            Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .graphicsLayer {
                    translationX = moveA * -.55f
                    translationY = moveB * -.35f
                }
                .blur(110.dp)
                .background(
                    Color(0xFF9AF2D4).copy(alpha = .3f),
                    RoundedCornerShape(50)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 55.dp, bottom = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(.55f))

            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color.White.copy(alpha = .75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.5.sp
            )

            Text(
                "NMIX",
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(27.dp))

            LandingButton("Start", onStart)

            Spacer(Modifier.height(10.dp))

            LandingButton("Share") {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Share NMIX"))
            }

            Spacer(Modifier.height(20.dp))

            AppInfoCard()

            Spacer(Modifier.weight(1f))

            Text(
                "© 2026 NMIX  •  All Rights Reserved",
                color = Color.White.copy(alpha = .45f),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun LandingButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .955f else 1f,
        animationSpec = spring(
            dampingRatio = .65f,
            stiffness = 550f
        ),
        label = "press"
    )

    Box(
        modifier = Modifier
            .width(275.dp)
            .height(51.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .90f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color(0xFF174C3B),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AppInfoCard() {
    val context = LocalContext.current

    val messages = remember {
        listOf(
            "A simple space for working with numbers.",
            "Calculate, count and track time from one app.",
            "Built to keep everyday number tools together.",
            "Fast, lightweight and designed to work offline.",
            "Numbers, time and useful tools — all in NMIX."
        )
    }

    var messageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3300)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    GlassBox(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "App Info",
                        color = Color.White.copy(alpha = .68f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        "NMIX",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    MiniButton("Web") {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://lxzrvi.github.io/NMIX/")
                            )
                        )
                    }

                    MiniButton("GitHub") {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = .075f)),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = messageIndex,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith
                            fadeOut(tween(400))
                    },
                    label = "info"
                ) { index ->
                    Text(
                        messages[index],
                        modifier = Modifier.padding(horizontal = 13.dp),
                        color = Color.White.copy(alpha = .82f),
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = .075f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        "Built With",
                        color = Color.White.copy(alpha = .55f),
                        fontSize = 9.sp
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        "Kotlin  •  Jetpack Compose  •  Android",
                        color = Color.White.copy(alpha = .9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        "100% Native  •  Offline",
                        color = Color(0xFF8BE3C5),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(19.dp))
            .background(Color.White.copy(alpha = .115f))
    ) {
        content()
    }
}

@Composable
private fun MiniButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if (pressed) .91f else 1f,
        spring(stiffness = 650f),
        label = "miniPress"
    )

    Box(
        Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .14f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color(0xFF66706C),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
