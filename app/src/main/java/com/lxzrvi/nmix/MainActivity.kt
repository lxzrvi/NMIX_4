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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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

val Accent = Color(0xFF319B79)

@Composable
fun NmixApp() {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "nmix_preferences",
            android.content.Context.MODE_PRIVATE
        )
    }

    var started by remember {
        mutableStateOf(
            prefs.getString("home_screen", "landing") == "main"
        )
    }

    if (started) {
        NativeMainPageV2(
            onBack = {
                prefs.edit()
                    .putString("home_screen", "landing")
                    .apply()

                started = false
            }
        )
    } else {
        LandingScreen(
            onStart = {
                prefs.edit()
                    .putString("home_screen", "main")
                    .apply()

                started = true
            }
        )
    }
}

@Composable
private fun LandingScreen(onStart: () -> Unit) {
    val context = LocalContext.current

    val appearancePrefs = remember {
        context.getSharedPreferences(
            "nmix_appearance",
            android.content.Context.MODE_PRIVATE
        )
    }

    val landingTheme =
        appearancePrefs.getString("theme", "green") ?: "green"

    val landingAccent = when(landingTheme) {
        "blue" -> Color(0xFF348BB8)
        "purple" -> Color(0xFF8A62C8)
        "orange" -> Color(0xFFD57D35)
        "rose" -> Color(0xFFC85878)
        else -> Color(0xFF319B79)
    }
    val motion = rememberInfiniteTransition(label = "movingBackground")

    val moveOne by motion.animateFloat(
        initialValue = -260f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(3900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveOne"
    )

    val moveTwo by motion.animateFloat(
        initialValue = 250f,
        targetValue = -280f,
        animationSpec = infiniteRepeatable(
            animation = tween(5100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveTwo"
    )

    val moveVertical by motion.animateFloat(
        initialValue = -190f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical"
    )

    val pulse by motion.animateFloat(
        initialValue = .92f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF010A07),
                        Color(0xFF07392B),
                        landingAccent.copy(alpha = .88f),
                        Color(0xFF07382B),
                        Color(0xFF020C09)
                    )
                )
            )
    ) {
        // Soft moving gradient lights — no visible square layer edges.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = moveOne * .42f
                    translationY = moveVertical * .28f
                    scaleX = 1.65f
                    scaleY = 1.65f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF72E8C0).copy(alpha = .48f),
                            Color(0xFF35B98D).copy(alpha = .24f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = moveTwo * .40f
                    translationY = -moveVertical * .25f
                    scaleX = 1.75f
                    scaleY = 1.75f
                    rotationZ = moveOne * .018f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF13B982).copy(alpha = .38f),
                            Color(0xFF087253).copy(alpha = .20f),
                            Color.Transparent
                        ),
                        radius = 1050f
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -moveOne * .30f
                    translationY = moveTwo * .20f
                    scaleX = 1.8f
                    scaleY = 1.8f
                }
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF9EF2D5).copy(alpha = .12f),
                            Color(0xFF20A77C).copy(alpha = .18f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Logo and main actions stay centered.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-55).dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EVERYTHING WITH NUMBERS",
                color = Color.White.copy(alpha = .76f),
                fontSize = 10.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "NMIX",
                color = Color.White,
                fontSize = 55.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(28.dp))

            GlassAction("Start", onStart)

            Spacer(Modifier.height(10.dp))

            GlassAction("Share") {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                    )
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share NMIX")
                )
            }
        }

        // Taller info card, deliberately separated from footer.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(bottom = 70.dp)
        ) {
            AppInfoCard()
        }

        // Independent branding at absolute bottom.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NMIX",
                color = Color.White.copy(alpha = .92f),
                fontSize = 12.sp,
                letterSpacing = .7.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "  •  lxzrvi  •  © 2026",
                color = Color.White.copy(alpha = .65f),
                fontSize = 12.sp,
                letterSpacing = 0.sp
            )
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
        targetValue = if (pressed) .95f else 1f,
        animationSpec = spring(
            dampingRatio = .62f,
            stiffness = 700f
        ),
        label = "press"
    )

    Box(
        modifier = Modifier
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
            text = text,
            color = Color.White.copy(alpha = .95f),
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
            "NMIX brings useful number tools together in one place. Calculate everyday values, count things, track time and generate numbers without moving between multiple apps.",
            "Built around speed and simplicity, NMIX combines a calculator, timer, local clock, stopwatch, counters and random-number tools in one focused native Android experience.",
            "NMIX is designed to work offline. Its core number and time tools stay available without depending on a website, browser or continuous internet connection.",
            "EVERYTHING WITH NUMBERS is the idea behind NMIX: a clean toolbox where calculations, counting and time-based utilities can live together with a consistent interface.",
            "The interface is being designed around smooth motion, clear controls and quick interactions so common number tasks feel simple whether they take seconds or stay open longer."
        )
    }

    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4800)
            index = (index + 1) % messages.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = .21f))
            .padding(15.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "APP INFO",
                        color = Color.White.copy(alpha = .62f),
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "NMIX",
                        color = Color.White,
                        fontSize = 21.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
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

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Changing information
                Box(
                    modifier = Modifier
                        .weight(1.18f)
                        .height(144.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = {
                            fadeIn(tween(500)) togetherWith
                                fadeOut(tween(400))
                        },
                        label = "information"
                    ) { current ->
                        Text(
                            text = messages[current],
                            color = Color.White.copy(alpha = .91f),
                            fontSize = 10.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Technology section
                Box(
                    modifier = Modifier
                        .weight(.82f)
                        .height(144.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(11.dp)
                ) {
                    Column {
                        Text(
                            "BUILT WITH",
                            color = Color.White.copy(alpha = .65f),
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(9.dp))

                        TechPill("Kotlin")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Jetpack Compose")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Android SDK")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Gradle")
                    }
                }
            }
        }
    }
}

@Composable
private fun TechPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Accent.copy(alpha = .42f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFC5F8E7),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
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
        targetValue = if (pressed) .90f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "miniPress"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .20f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

