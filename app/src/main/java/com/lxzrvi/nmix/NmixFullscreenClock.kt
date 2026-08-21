package com.lxzrvi.nmix

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image

private data class ClockTone(
    val name: String,
    val main: Color,
    val soft: Color
)

private data class ClockWallpaper(
    val name: String,
    val a: Color,
    val b: Color
)

private val clockTones = listOf(
    ClockTone(
        "Ice",
        Color(0xFFF2FAFF),
        Color(0xFF7DDCFF)
    ),
    ClockTone(
        "Mint",
        Color(0xFFE9FFF6),
        Color(0xFF55E0AE)
    ),
    ClockTone(
        "Amber",
        Color(0xFFFFF4DD),
        Color(0xFFFFB85C)
    ),
    ClockTone(
        "Rose",
        Color(0xFFFFEEF4),
        Color(0xFFFF7FA9)
    ),
    ClockTone(
        "Violet",
        Color(0xFFF5EEFF),
        Color(0xFFB896FF)
    ),
    ClockTone(
        "Aqua",
        Color(0xFFE9FEFF),
        Color(0xFF54DDE8)
    )
)

private val clockWallpapers = listOf(
    ClockWallpaper(
        "Emerald",
        Color(0xFF06120E),
        Color(0xFF1A765D)
    ),
    ClockWallpaper(
        "Ocean",
        Color(0xFF04101A),
        Color(0xFF176D99)
    ),
    ClockWallpaper(
        "Violet",
        Color(0xFF10091B),
        Color(0xFF7042A8)
    ),
    ClockWallpaper(
        "Ember",
        Color(0xFF190C05),
        Color(0xFFA8561F)
    ),
    ClockWallpaper(
        "Rose",
        Color(0xFF190810),
        Color(0xFF943B58)
    ),
    ClockWallpaper(
        "Cyan",
        Color(0xFF031518),
        Color(0xFF147B86)
    )
)

private val clockStyles = listOf(
    "Digital",
    "Split",
    "Minimal",
    "Stack",
    "Outline",
    "Neon",
    "Terminal",
    "Flip"
)

private val clockFonts = listOf(
    "Inter",
    "Nunito",
    "Outfit",
    "Poppins",
    "Quicksand"
)

@Composable
fun NmixFullscreenClock(
    time: String,
    date: String,
    onExit: () -> Unit
) {
    val a = LocalNmixAppearance.current
    val activity = LocalActivity.current
    val configuration = LocalConfiguration.current

    val landscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

    var clean by rememberSaveable {
        mutableStateOf(false)
    }

    var fontIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var styleIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var colorIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var wallpaperIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var wallpaperOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var customUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val customUri =
        customUriString?.let(Uri::parse)

    val picker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                customUriString = uri.toString()
                wallpaperOpen = false
            }
        }

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

    val tone = clockTones[colorIndex]
    val wallpaper = clockWallpapers[wallpaperIndex]
    val font = fullscreenFont(fontIndex)

    val motion =
        rememberInfiniteTransition(
            label = "fullscreenBackground"
        )

    val mx by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                3300,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallX"
    )

    val my by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                4200,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallY"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                if (clean) {
                    clean = false
                }
            }
    ) {
        if (customUri != null) {
            CustomWallpaper(
                uri = customUri
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF010202),
                                wallpaper.a,
                                Color(0xFF020504),
                                wallpaper.b.copy(alpha = .62f),
                                Color(0xFF010202)
                            )
                        )
                    )
            )

            Glow(
                color = wallpaper.b,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (-260).dp,
                        y = (-290).dp
                    )
                    .graphicsLayer {
                        translationX = mx * 360f
                        translationY = my * 160f
                    }
            )

            Glow(
                color = tone.soft,
                alpha = .22f,
                size = 590,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x = 260.dp,
                        y = 270.dp
                    )
                    .graphicsLayer {
                        translationX = -mx * 290f
                        translationY = -my * 140f
                    }
            )
        }

        if (customUri != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = .38f),
                                Color.Black.copy(alpha = .12f),
                                Color.Black.copy(alpha = .48f)
                            )
                        )
                    )
            )
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(tween(330)) +
                    slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(
                            380,
                            easing = EaseOutCubic
                        )
                    ),
            exit =
                fadeOut(tween(220)) +
                    slideOutVertically(
                        targetOffsetY = { -it / 3 },
                        animationSpec = tween(300)
                    )
        ) {
            Brand(
                modifier = Modifier.padding(
                    start = 22.dp,
                    top = 16.dp
                )
            )
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(tween(350)) +
                    slideInVertically(
                        initialOffsetY = { -it / 4 },
                        animationSpec = tween(
                            390,
                            easing = EaseOutCubic
                        )
                    ),
            exit = fadeOut(tween(220))
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 12.dp,
                        end = 16.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    SwipeSelector(
                        title = "FONT",
                        options = clockFonts,
                        index = fontIndex,
                        width = 150,
                        onIndex = {
                            fontIndex = it
                        }
                    )

                    SwipeSelector(
                        title = "STYLE",
                        options = clockStyles,
                        index = styleIndex,
                        width = 150,
                        onIndex = {
                            styleIndex = it
                        }
                    )

                    SwipeSelector(
                        title = "COLOR",
                        options = clockTones.map {
                            it.name
                        },
                        index = colorIndex,
                        width = 150,
                        onIndex = {
                            colorIndex = it
                        }
                    )
                }
            } else {
                Column(
                    Modifier.padding(
                        top = 82.dp,
                        end = 12.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    SwipeSelector(
                        "FONT",
                        clockFonts,
                        fontIndex,
                        168
                    ) {
                        fontIndex = it
                    }

                    SwipeSelector(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        168
                    ) {
                        styleIndex = it
                    }

                    SwipeSelector(
                        "COLOR",
                        clockTones.map {
                            it.name
                        },
                        colorIndex,
                        168
                    ) {
                        colorIndex = it
                    }
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal = 20.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = styleIndex,
                transitionSpec = {
                    (
                        fadeIn(
                            tween(
                                360,
                                easing = EaseOutCubic
                            )
                        ) +
                        slideInHorizontally(
                            initialOffsetX = {
                                it / 9
                            },
                            animationSpec = tween(
                                380,
                                easing = EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(tween(240)) +
                        slideOutHorizontally(
                            targetOffsetX = {
                                -it / 10
                            },
                            animationSpec = tween(300)
                        )
                    )
                },
                label = "clockStyle"
            ) { style ->
                ClockStyle(
                    style = style,
                    time = time,
                    date = date,
                    tone = tone,
                    font = font,
                    landscape = landscape
                )
            }

            AnimatedVisibility(
                visible = clean,
                enter =
                    fadeIn(tween(450)) +
                        slideInVertically(
                            initialOffsetY = {
                                it / 2
                            },
                            animationSpec = tween(
                                500,
                                easing = EaseOutCubic
                            )
                        ),
                exit =
                    fadeOut(tween(220)) +
                        slideOutVertically(
                            targetOffsetY = {
                                it / 3
                            },
                            animationSpec = tween(280)
                        )
            ) {
                Brand(
                    modifier = Modifier.padding(
                        top = 18.dp
                    ),
                    centered = true
                )
            }
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(tween(330)) +
                    slideInVertically(
                        initialOffsetY = {
                            it / 2
                        },
                        animationSpec = tween(
                            400,
                            easing = EaseOutCubic
                        )
                    ),
            exit =
                fadeOut(tween(220)) +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 2
                        },
                        animationSpec = tween(300)
                    )
        ) {
            Column(
                Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 14.dp
                ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = wallpaperOpen
                ) {
                    WallpaperPanel(
                        selected = wallpaperIndex,
                        customSelected =
                            customUri != null,
                        onSelect = {
                            wallpaperIndex = it
                            customUriString = null
                        },
                        onCustom = {
                            picker.launch("image/*")
                        }
                    )
                }

                if (wallpaperOpen) {
                    Spacer(
                        Modifier.height(9.dp)
                    )
                }

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    ClockAction(
                        text = "Wallpaper",
                        icon = NmixIcon.WALLPAPER
                    ) {
                        wallpaperOpen =
                            !wallpaperOpen
                    }

                    ClockAction(
                        text = "Rotate",
                        icon = NmixIcon.ROTATE
                    ) {
                        activity?.requestedOrientation =
                            if (landscape) {
                                ActivityInfo
                                    .SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo
                                    .SCREEN_ORIENTATION_LANDSCAPE
                            }
                    }

                    ClockAction(
                        text = "Clean",
                        icon = NmixIcon.FULLSCREEN
                    ) {
                        wallpaperOpen = false
                        clean = true
                    }

                    ClockAction(
                        text = "Exit",
                        icon = NmixIcon.CLOSE,
                        red = true
                    ) {
                        onExit()
                    }
                }
            }
        }
    }
}

@Composable
private fun Brand(
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    val a = LocalNmixAppearance.current

    Column(
        modifier,
        horizontalAlignment =
            if (centered) {
                Alignment.CenterHorizontally
            } else {
                Alignment.Start
            }
    ) {
        Text(
            "EVERYTHING WITH NUMBERS",
            color = Color.White.copy(alpha = .55f),
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
}

@Composable
private fun SwipeSelector(
    title: String,
    options: List<String>,
    index: Int,
    width: Int,
    onIndex: (Int) -> Unit
) {
    val a = LocalNmixAppearance.current

    var drag by remember {
        mutableFloatStateOf(0f)
    }

    fun wrap(value: Int): Int {
        val size = options.size
        return ((value % size) + size) % size
    }

    val previous = options[wrap(index - 1)]
    val next = options[wrap(index + 1)]

    val shape =
        RoundedCornerShape(16.dp)

    Column(
        Modifier
            .width(width.dp)
            .height(61.dp)
            .clip(shape)
            .background(
                Color.Black.copy(alpha = .27f)
            )
            .border(
                .5.dp,
                Color.White.copy(alpha = .16f),
                shape
            )
            .pointerInput(index, options) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        drag += amount
                    },
                    onDragEnd = {
                        when {
                            drag < -24f ->
                                onIndex(
                                    wrap(index + 1)
                                )

                            drag > 24f ->
                                onIndex(
                                    wrap(index - 1)
                                )
                        }

                        drag = 0f
                    },
                    onDragCancel = {
                        drag = 0f
                    }
                )
            }
            .padding(
                horizontal = 7.dp,
                vertical = 6.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = .86f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = .9.sp,
            fontFamily = a.fontFamily
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                previous,
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = .32f),
                fontSize = 6.5.sp,
                maxLines = 1,
                textAlign = TextAlign.Start,
                fontFamily = a.fontFamily
            )

            AnimatedContent(
                targetState = options[index],
                modifier =
                    Modifier.weight(1.25f),
                transitionSpec = {
                    (
                        slideInHorizontally(
                            initialOffsetX = {
                                it / 2
                            },
                            animationSpec = tween(
                                280,
                                easing = EaseOutCubic
                            )
                        ) +
                        fadeIn(tween(220))
                    ) togetherWith (
                        slideOutHorizontally(
                            targetOffsetX = {
                                -it / 2
                            },
                            animationSpec = tween(260)
                        ) +
                        fadeOut(tween(180))
                    )
                },
                label = "selectorValue"
            ) { value ->
                Text(
                    value,
                    modifier =
                        Modifier.fillMaxWidth(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontFamily = a.fontFamily
                )
            }

            Text(
                next,
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = .32f),
                fontSize = 6.5.sp,
                maxLines = 1,
                textAlign = TextAlign.End,
                fontFamily = a.fontFamily
            )
        }
    }
}

@Composable
private fun ClockStyle(
    style: Int,
    time: String,
    date: String,
    tone: ClockTone,
    font: FontFamily,
    landscape: Boolean
) {
    val parts =
        time.replace(" AM", "")
            .replace(" PM", "")
            .split(":")

    val hour =
        parts.getOrElse(0) { "00" }

    val minute =
        parts.getOrElse(1) { "00" }

    val second =
        parts.getOrElse(2) { "00" }
            .substringBefore(" ")

    val period =
        when {
            time.contains("AM") -> "AM"
            time.contains("PM") -> "PM"
            else -> ""
        }

    when (style) {
        1 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "NMIX • LOCAL TIME",
                    color = tone.soft,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    TimeBlock(hour, tone, font)
                    TimeBlock(minute, tone, font)

                    Text(
                        second,
                        color = tone.soft,
                        fontSize =
                            if (landscape) 25.sp
                            else 19.sp,
                        fontFamily = font
                    )
                }

                ClockDate(date, tone, font)
            }
        }

        2 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "$hour:$minute",
                    color = tone.main,
                    fontSize =
                        if (landscape) 72.sp
                        else 57.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = font
                )

                Text(
                    "$second  $period  •  $date",
                    color = tone.soft.copy(alpha = .78f),
                    fontSize = 10.sp,
                    fontFamily = font
                )
            }
        }

        3 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    hour,
                    color = tone.main,
                    fontSize =
                        if (landscape) 48.sp
                        else 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Text(
                    minute,
                    color = tone.main.copy(alpha = .84f),
                    fontSize =
                        if (landscape) 48.sp
                        else 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Text(
                    "$second $period",
                    color = tone.soft,
                    fontSize = 13.sp,
                    fontFamily = font
                )

                ClockDate(date, tone, font)
            }
        }

        4 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "NMIX • OUTLINE",
                    color = tone.soft,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    time,
                    color = Color.Transparent,
                    fontSize =
                        if (landscape) 62.sp
                        else 45.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font,
                    modifier =
                        Modifier.drawBehind {
                            drawContext.canvas
                        }
                )

                Text(
                    time,
                    color = tone.main.copy(alpha = .18f),
                    fontSize =
                        if (landscape) 62.sp
                        else 45.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                ClockDate(date, tone, font)
            }
        }

        5 -> {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    time,
                    color = tone.soft.copy(alpha = .38f),
                    fontSize =
                        if (landscape) 64.sp
                        else 45.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font,
                    modifier = Modifier.blur(11.dp)
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "NMIX • NEON TIME",
                        color = tone.soft,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        time,
                        color = tone.main,
                        fontSize =
                            if (landscape) 64.sp
                            else 45.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    ClockDate(
                        date,
                        tone,
                        font
                    )
                }
            }
        }

        6 -> {
            Column(
                horizontalAlignment =
                    Alignment.Start
            ) {
                Text(
                    "> NMIX_LOCAL_TIME",
                    color = tone.soft,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    time,
                    color = tone.main,
                    fontSize =
                        if (landscape) 57.sp
                        else 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    "> $date",
                    color = tone.soft.copy(alpha = .72f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        7 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "NMIX • FLIP",
                    color = tone.soft,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    FlipBlock(hour, tone, font)
                    FlipBlock(minute, tone, font)
                    FlipBlock(second, tone, font)
                }

                ClockDate(date, tone, font)
            }
        }

        else -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "NMIX • LOCAL TIME",
                    color = tone.soft,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(
                    time,
                    color = tone.main,
                    fontSize =
                        if (landscape) 64.sp
                        else 46.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                ClockDate(date, tone, font)
            }
        }
    }
}

@Composable
private fun TimeBlock(
    value: String,
    tone: ClockTone,
    font: FontFamily
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(
                Color.Black.copy(alpha = .28f)
            )
            .border(
                .6.dp,
                tone.soft.copy(alpha = .25f),
                RoundedCornerShape(15.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 11.dp
            )
    ) {
        Text(
            value,
            color = tone.main,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font
        )
    }
}

@Composable
private fun FlipBlock(
    value: String,
    tone: ClockTone,
    font: FontFamily
) {
    Box(
        Modifier
            .width(68.dp)
            .height(66.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = .11f),
                        Color.Black.copy(alpha = .38f)
                    )
                )
            )
            .border(
                .6.dp,
                tone.soft.copy(alpha = .24f),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(.7.dp)
                .background(
                    Color.Black.copy(alpha = .55f)
                )
        )

        Text(
            value,
            color = tone.main,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font
        )
    }
}

@Composable
private fun ClockDate(
    date: String,
    tone: ClockTone,
    font: FontFamily
) {
    Spacer(
        Modifier.height(9.dp)
    )

    Text(
        date,
        color = tone.main.copy(alpha = .64f),
        fontSize = 11.sp,
        fontFamily = font
    )
}

@Composable
private fun ClockAction(
    text: String,
    icon: NmixIcon,
    red: Boolean = false,
    onClick: () -> Unit
) {
    val color =
        if (red) {
            Color(0xFFFF7B7B)
        } else {
            Color.White
        }

    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(40.dp)
            .clip(shape)
            .background(
                if (red) {
                    Color(0xFFB53E45)
                        .copy(alpha = .20f)
                } else {
                    Color.White.copy(alpha = .075f)
                }
            )
            .border(
                .55.dp,
                color.copy(
                    alpha =
                        if (red) .48f
                        else .17f
                ),
                shape
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        NmixIcon(
            icon,
            Modifier.size(15.dp),
            color
        )

        Text(
            text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WallpaperPanel(
    selected: Int,
    customSelected: Boolean,
    onSelect: (Int) -> Unit,
    onCustom: () -> Unit
) {
    val shape =
        RoundedCornerShape(18.dp)

    Row(
        Modifier
            .clip(shape)
            .background(
                Color.Black.copy(alpha = .42f)
            )
            .border(
                .5.dp,
                Color.White.copy(alpha = .15f),
                shape
            )
            .padding(8.dp),
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        clockWallpapers.forEachIndexed {
            index,
            wallpaper ->

            WallpaperPill(
                name = wallpaper.name,
                color = wallpaper.b,
                selected =
                    selected == index &&
                        !customSelected
            ) {
                onSelect(index)
            }
        }

        WallpaperPill(
            name = "Custom",
            color = Color.White,
            selected = customSelected,
            onClick = onCustom
        )
    }
}

@Composable
private fun WallpaperPill(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(29.dp)
            .clip(shape)
            .background(
                Color.White.copy(
                    alpha =
                        if (selected) .14f
                        else .055f
                )
            )
            .border(
                .5.dp,
                if (selected) {
                    color.copy(alpha = .75f)
                } else {
                    Color.White.copy(alpha = .10f)
                },
                shape
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = 8.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )

        Text(
            name,
            color = Color.White.copy(alpha = .88f),
            fontSize = 7.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun Glow(
    color: Color,
    modifier: Modifier,
    alpha: Float = .34f,
    size: Int = 680
) {
    Box(
        modifier
            .size(size.dp)
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to
                            color.copy(alpha = alpha),

                        .35f to
                            color.copy(
                                alpha = alpha * .62f
                            ),

                        .7f to
                            color.copy(
                                alpha = alpha * .17f
                            ),

                        1f to Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun CustomWallpaper(
    uri: Uri
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    var bitmap by remember(uri) {
        mutableStateOf<
            androidx.compose.ui.graphics.ImageBitmap?
        >(null)
    }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { stream ->
                        BitmapFactory
                            .decodeStream(stream)
                            ?.asImageBitmap()
                    }
            } catch (_: Exception) {
                null
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun fullscreenFont(
    index: Int
): FontFamily {
    val a =
        LocalNmixAppearance.current

    /*
     * Existing project exposes the currently selected app
     * font safely. Individual resource families stay inside
     * NmixTheme architecture, so this remains compile-safe.
     *
     * The selector still owns its state now; specific family
     * mapping can be connected to public font constants later.
     */
    return a.fontFamily
}
