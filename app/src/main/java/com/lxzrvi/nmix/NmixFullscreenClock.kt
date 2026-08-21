package com.lxzrvi.nmix

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

private data class ClockTone(
    val name: String,
    val main: Color,
    val accent: Color
)

private val clockTones = listOf(
    ClockTone(
        "Ice",
        Color(0xFFF4FBFF),
        Color(0xFF6CD7FF)
    ),
    ClockTone(
        "Mint",
        Color(0xFFE9FFF6),
        Color(0xFF52E0AC)
    ),
    ClockTone(
        "Amber",
        Color(0xFFFFF5E3),
        Color(0xFFFFB75A)
    ),
    ClockTone(
        "Rose",
        Color(0xFFFFEDF4),
        Color(0xFFFF7EA8)
    ),
    ClockTone(
        "Violet",
        Color(0xFFF6EEFF),
        Color(0xFFB792FF)
    ),
    ClockTone(
        "Aqua",
        Color(0xFFE8FEFF),
        Color(0xFF51DDE7)
    )
)

private val clockStyles = listOf(
    "Digital",
    "Split",
    "Minimal",
    "Stack",
    "Glow",
    "Orbit",
    "Terminal",
    "Flip"
)

private val fontNames = listOf(
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
    val p = a.palette
    val ui = a.uiColors()
    val activity = LocalActivity.current
    val configuration = LocalConfiguration.current

    val landscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

    var clean by rememberSaveable {
        mutableStateOf(false)
    }

    var fontIndex by rememberSaveable {
        mutableIntStateOf(
            when (a.font) {
                NmixFontName.INTER -> 0
                NmixFontName.NUNITO -> 1
                NmixFontName.OUTFIT -> 2
                NmixFontName.POPPINS -> 3
                NmixFontName.QUICKSAND -> 4
            }
        )
    }

    var styleIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var colorIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var wallpaperOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var wallpaperIndex by rememberSaveable {
        mutableIntStateOf(
            a.theme.ordinal
        )
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

    val font = when (fontIndex) {
        1 -> NmixNunito
        2 -> NmixOutfit
        3 -> NmixPoppins
        4 -> NmixQuicksand
        else -> NmixInter
    }

    val tone = clockTones[colorIndex]

    val wallpaperPalette =
        NmixThemeName.entries[
            wallpaperIndex.coerceIn(
                0,
                NmixThemeName.entries.lastIndex
            )
        ].palette()

    val motion =
        rememberInfiniteTransition(
            label = "fullClockBackground"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                3200,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "backgroundX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                4100,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "backgroundY"
    )

    val z by motion.animateFloat(
        -.8f,
        .8f,
        infiniteRepeatable(
            tween(
                5100,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "backgroundZ"
    )

    val pulse by motion.animateFloat(
        .90f,
        1.10f,
        infiniteRepeatable(
            tween(
                2800,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "backgroundPulse"
    )

    val pageBrush =
        if (a.darkMode) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF020403),
                    wallpaperPalette.topDark,
                    Color(0xFF07100D),
                    wallpaperPalette.topEnd,
                    Color(0xFF020302)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    wallpaperPalette.accentLight
                        .copy(alpha = .96f),
                    wallpaperPalette.accent,
                    wallpaperPalette.accentDark,
                    wallpaperPalette.topEnd
                )
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(ui.page)
            .background(pageBrush)
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
            CustomWallpaper(customUri)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (a.darkMode) {
                            Color.Black.copy(alpha = .32f)
                        } else {
                            Color.Black.copy(alpha = .13f)
                        }
                    )
            )
        } else {
            FullscreenGlow(
                color = wallpaperPalette.accentLight,
                alpha =
                    if (a.darkMode) .29f
                    else .36f,
                size = 520,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (-170).dp,
                        y = (-150).dp
                    )
                    .graphicsLayer {
                        translationX = x * 210f
                        translationY = y * 105f
                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            FullscreenGlow(
                color = wallpaperPalette.accent,
                alpha =
                    if (a.darkMode) .25f
                    else .31f,
                size = 500,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x = 170.dp,
                        y = 155.dp
                    )
                    .graphicsLayer {
                        translationX = -x * 190f
                        translationY = z * 115f
                    }
            )

            FullscreenGlow(
                color = tone.accent,
                alpha =
                    if (a.darkMode) .11f
                    else .16f,
                size = 390,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (-110).dp,
                        y = 30.dp
                    )
                    .graphicsLayer {
                        translationX = z * 170f
                        translationY = -y * 100f
                        scaleX = pulse
                        scaleY = pulse
                    }
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
                        initialOffsetY = {
                            -it / 2
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
                            -it / 3
                        },
                        animationSpec = tween(300)
                    )
        ) {
            ClockBrand(
                Modifier.padding(
                    start = 22.dp,
                    top = 18.dp
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
                        initialOffsetY = {
                            -it / 3
                        },
                        animationSpec = tween(
                            420,
                            easing = EaseOutCubic
                        )
                    ),
            exit = fadeOut(tween(220))
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 15.dp,
                        end = 17.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp)
                ) {
                    ClockWheel(
                        "FONT",
                        fontNames,
                        fontIndex,
                        tone.accent,
                        174
                    ) {
                        fontIndex = it
                    }

                    ClockWheel(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        tone.accent,
                        174
                    ) {
                        styleIndex = it
                    }

                    ClockWheel(
                        "COLOR",
                        clockTones.map { it.name },
                        colorIndex,
                        tone.accent,
                        174
                    ) {
                        colorIndex = it
                    }
                }
            } else {
                Column(
                    Modifier.padding(
                        top = 87.dp,
                        end = 13.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    ClockWheel(
                        "FONT",
                        fontNames,
                        fontIndex,
                        tone.accent,
                        188
                    ) {
                        fontIndex = it
                    }

                    ClockWheel(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        tone.accent,
                        188
                    ) {
                        styleIndex = it
                    }

                    ClockWheel(
                        "COLOR",
                        clockTones.map { it.name },
                        colorIndex,
                        tone.accent,
                        188
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
                    horizontal = 18.dp
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
                                420,
                                easing = EaseOutCubic
                            )
                        ) +
                        scaleIn(
                            initialScale = .94f,
                            animationSpec = tween(
                                430,
                                easing = EaseOutCubic
                            )
                        ) +
                        slideInVertically(
                            initialOffsetY = {
                                it / 8
                            },
                            animationSpec = tween(
                                430,
                                easing = EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(tween(250)) +
                        scaleOut(
                            targetScale = 1.035f,
                            animationSpec = tween(300)
                        ) +
                        slideOutVertically(
                            targetOffsetY = {
                                -it / 10
                            },
                            animationSpec = tween(300)
                        )
                    )
                },
                label = "clockStyle"
            ) { style ->
                NmixClockFace(
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
                    fadeIn(tween(420)) +
                    slideInVertically(
                        initialOffsetY = {
                            it
                        },
                        animationSpec = tween(
                            480,
                            easing = EaseOutCubic
                        )
                    ),
                exit =
                    fadeOut(tween(230)) +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 2
                        },
                        animationSpec = tween(280)
                    )
            ) {
                ClockBrand(
                    modifier = Modifier.padding(
                        top = 20.dp
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
                    start = 13.dp,
                    end = 13.dp,
                    bottom = 16.dp
                ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = wallpaperOpen,
                    enter =
                        fadeIn(tween(250)) +
                            slideInVertically {
                                it / 3
                            },
                    exit =
                        fadeOut(tween(180)) +
                            slideOutVertically {
                                it / 3
                            }
                ) {
                    WallpaperPanel(
                        selected = wallpaperIndex,
                        customSelected =
                            customUri != null,
                        accent = p.accentLight,
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
                        Modifier.height(10.dp)
                    )
                }

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    ClockAction(
                        "Wallpaper",
                        NmixIcon.WALLPAPER
                    ) {
                        wallpaperOpen =
                            !wallpaperOpen
                    }

                    ClockAction(
                        "Rotate",
                        NmixIcon.ROTATE
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
                        "Clean",
                        NmixIcon.FULLSCREEN
                    ) {
                        wallpaperOpen = false
                        clean = true
                    }

                    ClockAction(
                        "Exit",
                        NmixIcon.CLOSE,
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
private fun ClockWheel(
    title: String,
    options: List<String>,
    index: Int,
    accent: Color,
    width: Int,
    onIndex: (Int) -> Unit
) {
    val a = LocalNmixAppearance.current

    var drag by remember {
        mutableFloatStateOf(0f)
    }

    var direction by remember {
        mutableIntStateOf(1)
    }

    fun wrapped(value: Int): Int {
        return (
            (value % options.size) +
                options.size
            ) % options.size
    }

    val previous =
        options[wrapped(index - 1)]

    val current =
        options[index]

    val next =
        options[wrapped(index + 1)]

    val shape =
        RoundedCornerShape(50)

    Column(
        Modifier
            .width(width.dp)
            .height(70.dp)
            .clip(shape)
            .background(
                Color.Black.copy(
                    alpha =
                        if (a.darkMode) .24f
                        else .16f
                )
            )
            .border(
                .55.dp,
                Color.White.copy(
                    alpha =
                        if (a.darkMode) .18f
                        else .29f
                ),
                shape
            )
            .pointerInput(index, options) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        drag += amount
                    },
                    onDragEnd = {
                        when {
                            drag < -22f -> {
                                direction = 1
                                onIndex(
                                    wrapped(index + 1)
                                )
                            }

                            drag > 22f -> {
                                direction = -1
                                onIndex(
                                    wrapped(index - 1)
                                )
                            }
                        }

                        drag = 0f
                    },
                    onDragCancel = {
                        drag = 0f
                    }
                )
            }
            .padding(
                horizontal = 10.dp,
                vertical = 7.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = .90f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            fontFamily = a.fontFamily
        )

        Spacer(
            Modifier.height(7.dp)
        )

        AnimatedContent(
            targetState = current,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                val enter =
                    if (direction > 0) {
                        slideInHorizontally(
                            initialOffsetX = {
                                it / 3
                            },
                            animationSpec = tween(
                                310,
                                easing = EaseOutCubic
                            )
                        )
                    } else {
                        slideInHorizontally(
                            initialOffsetX = {
                                -it / 3
                            },
                            animationSpec = tween(
                                310,
                                easing = EaseOutCubic
                            )
                        )
                    }

                val exit =
                    if (direction > 0) {
                        slideOutHorizontally(
                            targetOffsetX = {
                                -it / 3
                            },
                            animationSpec = tween(
                                310,
                                easing = EaseInOutCubic
                            )
                        )
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = {
                                it / 3
                            },
                            animationSpec = tween(
                                310,
                                easing = EaseInOutCubic
                            )
                        )
                    }

                (
                    enter +
                        fadeIn(tween(260))
                    ) togetherWith (
                    exit +
                        fadeOut(tween(220))
                    )
            },
            label = "wheel"
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    previous,
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = .58f),
                    fontSize = 7.5.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    fontFamily = a.fontFamily
                )

                Text(
                    current,
                    modifier = Modifier.weight(1.35f),
                    color = accent,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontFamily = a.fontFamily
                )

                Text(
                    next,
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = .58f),
                    fontSize = 7.5.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    fontFamily = a.fontFamily
                )
            }
        }
    }
}

@Composable
private fun NmixClockFace(
    style: Int,
    time: String,
    date: String,
    tone: ClockTone,
    font: FontFamily,
    landscape: Boolean
) {
    val cleanTime =
        time.replace(" AM", "")
            .replace(" PM", "")

    val pieces =
        cleanTime.split(":")

    val hour =
        pieces.getOrElse(0) { "00" }

    val minute =
        pieces.getOrElse(1) { "00" }

    val second =
        pieces.getOrElse(2) { "00" }

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
                ClockCaption(
                    "NMIX • SPLIT TIME",
                    tone,
                    font
                )

                Spacer(
                    Modifier.height(15.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    GlassTimeBlock(
                        hour,
                        tone,
                        font,
                        landscape
                    )

                    Text(
                        ":",
                        color = tone.accent,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )

                    GlassTimeBlock(
                        minute,
                        tone,
                        font,
                        landscape
                    )

                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                tone.accent.copy(alpha = .13f)
                            )
                            .border(
                                .5.dp,
                                tone.accent.copy(alpha = .32f),
                                RoundedCornerShape(50)
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            )
                    ) {
                        Text(
                            "$second $period",
                            color = tone.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        )
                    }
                }

                ClockDate(
                    date,
                    tone,
                    font
                )
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
                        if (landscape) 76.sp
                        else 59.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = font,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(tone.accent)
                    )

                    Text(
                        "$second $period",
                        color = tone.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        date,
                        color = tone.main.copy(alpha = .66f),
                        fontSize = 10.sp,
                        fontFamily = font
                    )
                }
            }
        }

        3 -> {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(17.dp)
            ) {
                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {
                    Text(
                        hour,
                        color = tone.main,
                        fontSize =
                            if (landscape) 58.sp
                            else 47.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        minute,
                        color = tone.accent,
                        fontSize =
                            if (landscape) 58.sp
                            else 47.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )
                }

                Box(
                    Modifier
                        .width(1.dp)
                        .height(94.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    tone.accent,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column {
                    Text(
                        "SECONDS",
                        color = tone.main.copy(alpha = .48f),
                        fontSize = 7.sp,
                        letterSpacing = 1.5.sp,
                        fontFamily = font
                    )

                    Text(
                        second,
                        color = tone.main,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        period,
                        color = tone.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Spacer(
                        Modifier.height(7.dp)
                    )

                    Text(
                        date,
                        color = tone.main.copy(alpha = .62f),
                        fontSize = 9.sp,
                        fontFamily = font
                    )
                }
            }
        }

        4 -> {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    time,
                    color = tone.accent.copy(alpha = .32f),
                    fontSize =
                        if (landscape) 68.sp
                        else 47.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font,
                    modifier =
                        Modifier.blur(14.dp)
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    ClockCaption(
                        "NMIX • GLOW",
                        tone,
                        font
                    )

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Text(
                        time,
                        color = tone.main,
                        fontSize =
                            if (landscape) 68.sp
                            else 47.sp,
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

        5 -> {
            Box(
                Modifier.size(
                    if (landscape) 245.dp
                    else 218.dp
                ),
                contentAlignment =
                    Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            tone.accent.copy(alpha = .19f),
                            CircleShape
                        )
                )

                Box(
                    Modifier
                        .size(
                            if (landscape) 185.dp
                            else 165.dp
                        )
                        .border(
                            .7.dp,
                            tone.accent.copy(alpha = .13f),
                            CircleShape
                        )
                )

                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 5.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tone.accent)
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "$hour:$minute",
                        color = tone.main,
                        fontSize =
                            if (landscape) 48.sp
                            else 41.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        "$second $period",
                        color = tone.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Spacer(
                        Modifier.height(5.dp)
                    )

                    Text(
                        date,
                        color = tone.main.copy(alpha = .57f),
                        fontSize = 8.sp,
                        fontFamily = font
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
                    "NMIX://LOCAL_CLOCK",
                    color = tone.accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    "> $time",
                    color = tone.main,
                    fontSize =
                        if (landscape) 58.sp
                        else 41.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    "> DATE  $date",
                    color = tone.accent.copy(alpha = .76f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    "> STATUS  LIVE",
                    color = tone.main.copy(alpha = .45f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        7 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                ClockCaption(
                    "NMIX • FLIP",
                    tone,
                    font
                )

                Spacer(
                    Modifier.height(14.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    FlipCard(
                        hour,
                        tone,
                        font,
                        landscape
                    )

                    FlipCard(
                        minute,
                        tone,
                        font,
                        landscape
                    )

                    FlipCard(
                        second,
                        tone,
                        font,
                        landscape
                    )
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    "$period  •  $date",
                    color = tone.main.copy(alpha = .65f),
                    fontSize = 10.sp,
                    fontFamily = font
                )
            }
        }

        else -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                ClockCaption(
                    "NMIX • LOCAL TIME",
                    tone,
                    font
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(
                    time,
                    color = tone.main,
                    fontSize =
                        if (landscape) 67.sp
                        else 47.sp,
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
}

@Composable
private fun ClockCaption(
    text: String,
    tone: ClockTone,
    font: FontFamily
) {
    Text(
        text,
        color = tone.accent,
        fontSize = 9.sp,
        letterSpacing = 1.9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = font
    )
}

@Composable
private fun GlassTimeBlock(
    value: String,
    tone: ClockTone,
    font: FontFamily,
    landscape: Boolean
) {
    val shape =
        RoundedCornerShape(18.dp)

    Box(
        Modifier
            .clip(shape)
            .background(
                Color.Black.copy(alpha = .20f)
            )
            .border(
                .6.dp,
                tone.accent.copy(alpha = .29f),
                shape
            )
            .padding(
                horizontal =
                    if (landscape) 19.dp
                    else 13.dp,
                vertical = 12.dp
            )
    ) {
        Text(
            value,
            color = tone.main,
            fontSize =
                if (landscape) 43.sp
                else 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font
        )
    }
}

@Composable
private fun FlipCard(
    value: String,
    tone: ClockTone,
    font: FontFamily,
    landscape: Boolean
) {
    val shape =
        RoundedCornerShape(12.dp)

    Box(
        Modifier
            .width(
                if (landscape) 82.dp
                else 66.dp
            )
            .height(
                if (landscape) 78.dp
                else 68.dp
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = .13f),
                        Color.Black.copy(alpha = .30f)
                    )
                )
            )
            .border(
                .6.dp,
                tone.accent.copy(alpha = .28f),
                shape
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Color.Black.copy(alpha = .45f)
                )
        )

        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (
                    slideInVertically(
                        initialOffsetY = {
                            -it / 2
                        },
                        animationSpec = tween(
                            250,
                            easing = EaseOutCubic
                        )
                    ) +
                        fadeIn(tween(190))
                    ) togetherWith (
                    slideOutVertically(
                        targetOffsetY = {
                            it / 2
                        },
                        animationSpec = tween(230)
                    ) +
                        fadeOut(tween(170))
                    )
            },
            label = "flipDigit"
        ) {
            Text(
                it,
                color = tone.main,
                fontSize =
                    if (landscape) 35.sp
                    else 29.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = font
            )
        }
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
        color = tone.main.copy(alpha = .66f),
        fontSize = 11.sp,
        fontFamily = font
    )
}

@Composable
private fun ClockBrand(
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
            color = Color.White.copy(alpha = .64f),
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
private fun ClockAction(
    text: String,
    icon: NmixIcon,
    red: Boolean = false,
    onClick: () -> Unit
) {
    val color =
        if (red) {
            Color(0xFFFF8585)
        } else {
            Color.White
        }

    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(46.dp)
            .clip(shape)
            .background(
                if (red) {
                    Color(0xFFB8444B)
                        .copy(alpha = .22f)
                } else {
                    Color.White.copy(alpha = .10f)
                }
            )
            .border(
                .6.dp,
                color.copy(
                    alpha =
                        if (red) .50f
                        else .22f
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
                horizontal = 13.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        NmixIcon(
            icon,
            Modifier.size(17.dp),
            color
        )

        Text(
            text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WallpaperPanel(
    selected: Int,
    customSelected: Boolean,
    accent: Color,
    onSelect: (Int) -> Unit,
    onCustom: () -> Unit
) {
    val names =
        listOf(
            "Green",
            "Blue",
            "Purple",
            "Orange",
            "Rose",
            "Cyan"
        )

    val colors =
        NmixThemeName.entries.map {
            it.palette().accent
        }

    val shape =
        RoundedCornerShape(20.dp)

    Row(
        Modifier
            .clip(shape)
            .background(
                Color.Black.copy(alpha = .40f)
            )
            .border(
                .5.dp,
                accent.copy(alpha = .27f),
                shape
            )
            .padding(8.dp),
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        names.forEachIndexed {
            index,
            name ->

            WallpaperPill(
                name,
                colors[index],
                selected == index &&
                    !customSelected
            ) {
                onSelect(index)
            }
        }

        WallpaperPill(
            "Custom",
            Color.White,
            customSelected,
            onCustom
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
            .height(31.dp)
            .clip(shape)
            .background(
                Color.White.copy(
                    alpha =
                        if (selected) .15f
                        else .06f
                )
            )
            .border(
                .5.dp,
                if (selected) {
                    color.copy(alpha = .80f)
                } else {
                    Color.White.copy(alpha = .12f)
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
                .clip(CircleShape)
                .background(color)
        )

        Text(
            name,
            color = Color.White.copy(alpha = .90f),
            fontSize = 7.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun FullscreenGlow(
    color: Color,
    alpha: Float,
    size: Int,
    modifier: Modifier
) {
    Box(
        modifier
            .size(size.dp)
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to
                            color.copy(alpha = alpha),

                        .24f to
                            color.copy(
                                alpha = alpha * .82f
                            ),

                        .50f to
                            color.copy(
                                alpha = alpha * .42f
                            ),

                        .76f to
                            color.copy(
                                alpha = alpha * .12f
                            ),

                        1f to Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

@Composable
private fun CustomWallpaper(
    uri: Uri
) {
    val context =
        LocalContext.current

    var bitmap by remember(uri) {
        mutableStateOf<
            androidx.compose.ui.graphics.ImageBitmap?
        >(null)
    }

    LaunchedEffect(uri) {
        bitmap =
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver
                        .openInputStream(uri)
                        ?.use {
                            BitmapFactory
                                .decodeStream(it)
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
