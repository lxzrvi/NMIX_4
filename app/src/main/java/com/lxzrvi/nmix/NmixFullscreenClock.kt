package com.lxzrvi.nmix

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun NmixFullscreenClock(
    time: String,
    date: String,
    onExit: () -> Unit
) {
    val a = LocalNmixAppearance.current
    val p = a.palette
    val activity = LocalActivity.current

    DisposableEffect(activity) {
        val window = activity?.window

        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(
                window,
                false
            )

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                hide(
                    WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
                )

                systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose {
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )

                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).show(
                    WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
                )
            }
        }
    }

    val motion = rememberInfiniteTransition(
        label = "fullscreenClockMotion"
    )

    val x by motion.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                3400,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockX"
    )

    val y by motion.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                4300,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockY"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020403),
                        p.topDark,
                        Color(0xFF07100D),
                        Color(0xFF010201)
                    )
                )
            )
    ) {
        Box(
            Modifier
                .size(680.dp)
                .align(Alignment.TopStart)
                .offset(
                    x = (-300).dp,
                    y = (-290).dp
                )
                .graphicsLayer {
                    translationX = x * 340f
                    translationY = y * 160f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(alpha = .38f),
                            p.accent.copy(alpha = .12f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(600.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x = 260.dp,
                    y = 250.dp
                )
                .graphicsLayer {
                    translationX = -x * 280f
                    translationY = -y * 140f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(alpha = .24f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
                .padding(
                    start = 22.dp,
                    top = 18.dp
                )
        ) {
            Text(
                "EVERYTHING WITH NUMBERS",
                color = Color.White.copy(alpha = .54f),
                fontSize = 7.sp,
                letterSpacing = 1.5.sp,
                fontFamily = a.fontFamily
            )

            Text(
                "NMIX",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = NmixLogoFont
            )
        }

        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NMIX • LOCAL TIME",
                color = p.accentLight.copy(alpha = .92f),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = a.fontFamily
            )

            Spacer(
                Modifier.height(13.dp)
            )

            Text(
                time,
                color = Color.White,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = a.fontFamily
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                date,
                color = Color.White.copy(alpha = .68f),
                fontSize = 12.sp,
                fontFamily = a.fontFamily
            )
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
                .padding(20.dp)
        ) {
            NmixTextButton(
                text = "Exit",
                modifier = Modifier
                    .width(94.dp)
                    .height(42.dp),
                onClick = onExit
            )
        }
    }
}
