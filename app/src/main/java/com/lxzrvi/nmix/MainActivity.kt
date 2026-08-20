package com.lxzrvi.nmix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NmixApp() }
    }
}

private val Emerald = Color(0xFF319B79)
private val DarkGreen = Color(0xFF03140F)

@Composable
fun NmixApp() {
    var started by remember { mutableStateOf(false) }

    if (!started) {
        LandingScreen(onStart = { started = true })
    } else {
        MainScreen()
    }
}

@Composable
private fun LandingScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    val infinite = rememberInfiniteTransition(label = "nmixBackground")

    val movement by infinite.animateFloat(
        initialValue = -90f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movement"
    )

    val pulse by infinite.animateFloat(
        initialValue = .92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        DarkGreen,
                        Color(0xFF0D3B2D),
                        Emerald,
                        Color(0xFF0A3528),
                        DarkGreen
                    )
                )
            )
    ) {
        Box(
            Modifier
                .size(280.dp)
                .graphicsLayer {
                    translationX = movement
                    translationY = movement * .55f
                    scaleX = pulse
                    scaleY = pulse
                    alpha = .22f
                }
                .background(
                    Color(0xFF91E8CA),
                    RoundedCornerShape(140.dp)
                )
        )

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(330.dp)
                .graphicsLayer {
                    translationX = -movement
                    translationY = movement * -.35f
                    scaleX = 1.1f - (pulse - 1f)
                    scaleY = 1.1f - (pulse - 1f)
                    alpha = .16f
                }
                .background(
                    Color(0xFF69D6B2),
                    RoundedCornerShape(165.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color(0xFFDCF8EF),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.2.sp
            )

            Text(
                "NMIX",
                color = Color.White,
                fontSize = 51.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(275.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF174C3B)
                )
            ) {
                Text("Start", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "NMIX — EVERYTHING WITH NUMBERS"
                        )
                    }
                    context.startActivity(Intent.createChooser(send, "Share NMIX"))
                },
                modifier = Modifier
                    .width(275.dp)
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Share")
            }

            Spacer(Modifier.height(22.dp))

            ContributorCard()
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "© 2026 Alex Ravi",
                color = Color.White.copy(alpha = .68f),
                fontSize = 9.sp
            )
            Text(
                "All Rights Reserved",
                color = Color.White.copy(alpha = .5f),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun ContributorCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F3F3).copy(alpha = .95f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Contributor",
                color = Color(0xFF202321),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(9.dp))

            Text(
                "Alex Ravi",
                color = Emerald,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(7.dp))

            Text(
                "I'm currently doing a diploma in web development and building my skills step by step.",
                color = Color(0xFF66706C),
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Skill("HTML")
                Skill("CSS")
                Skill("JavaScript")
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Learning More",
                color = Color(0xFF66706C),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Responsive Design  •  UI / UX  •  Web APIs",
                color = Emerald,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun Skill(text: String) {
    Surface(
        color = Emerald.copy(alpha = .12f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            color = Color(0xFF216E56),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDEDEDE)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "NMIX",
                color = Emerald,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color(0xFF66706C),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Native workspace ready.",
                color = Color(0xFF202321)
            )
        }
    }
}
