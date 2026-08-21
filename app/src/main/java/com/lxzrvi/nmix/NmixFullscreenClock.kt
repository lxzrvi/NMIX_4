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
import kotlin.math.abs

private data class FullClockTone(
    val name: String,
    val main: Color,
    val accent: Color
)

private val fullClockTones = listOf(
    FullClockTone(
        "Ice",
        Color(0xFFF3FAFF),
        Color(0xFF70D8FF)
    ),
    FullClockTone(
        "Mint",
        Color(0xFFE9FFF7),
        Color(0xFF52E0AD)
    ),
    FullClockTone(
        "Amber",
        Color(0xFFFFF5E2),
        Color(0xFFFFB85A)
    ),
    FullClockTone(
        "Rose",
        Color(0xFFFFEDF4),
        Color(0xFFFF7EA8)
    ),
    FullClockTone(
        "Violet",
        Color(0xFFF6EEFF),
        Color(0xFFB792FF)
    ),
    FullClockTone(
        "Aqua",
        Color(0xFFE8FEFF),
        Color(0xFF50DDE8)
    )
)

private val fullClockStyles = listOf(
    "Digital",
    "Minimal",
    "Stack",
    "Glow",
    "Orbit",
    "Terminal",
    "Capsule",
    "Studio"
)

private val fullClockFonts = listOf(
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

    var wallpaperIndex by rememberSaveable {
        mutableIntStateOf(a.theme.ordinal)
    }

    var wallpaperOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var secondsVisible by rememberSaveable {
        mutableStateOf(true)
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

    val selectedFont =
        when (fontIndex) {
            1 -> NmixNunito
            2 -> NmixOutfit
            3 -> NmixPoppins
            4 -> NmixQuicksand
            else -> NmixInter
        }

    val tone =
        fullClockTones[colorIndex]

    val wall =
        NmixThemeName.entries[
            wallpaperIndex.coerceIn(
                0,
                NmixThemeName.entries.lastIndex
            )
        ].palette()

    val displayedTime =
        displayClockTime(
            time,
            secondsVisible
        )

    val motion =
        rememberInfiniteTransition(
            label = "clockWallpaperMotion"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                2850,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                3550,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallY"
    )

    val z by motion.animateFloat(
        -.9f,
        .9f,
        infiniteRepeatable(
            tween(
                4400,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallZ"
    )

    val pulse by motion.animateFloat(
        .92f,
        1.09f,
        infiniteRepeatable(
            tween(
                2600,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallPulse"
    )

    val baseBrush =
        if (a.darkMode) {
            Brush.verticalGradient(
                listOf(
                    wall.topDark.copy(alpha = .98f),
                    wall.accentDark.copy(alpha = .92f),
                    wall.topEnd.copy(alpha = .98f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    wall.accentLight,
                    wall.accent.copy(alpha = .91f),
                    wall.accentDark.copy(alpha = .82f)
                )
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(baseBrush)
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
            FullClockCustomWallpaper(
                customUri
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (a.darkMode) {
                            Color.Black.copy(alpha = .22f)
                        } else {
                            Color.Black.copy(alpha = .08f)
                        }
                    )
            )
        } else {
            FullClockGlow(
                wall.accentLight,
                if (a.darkMode) .34f else .38f,
                440,
                Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (-120).dp,
                        y = (-100).dp
                    )
                    .graphicsLayer {
                        translationX = x * 235f
                        translationY = y * 105f
                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            FullClockGlow(
                wall.accent,
                if (a.darkMode) .29f else .34f,
                420,
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x = 120.dp,
                        y = 105.dp
                    )
                    .graphicsLayer {
                        translationX = -x * 220f
                        translationY = z * 120f
                        scaleX = 1.06f
                        scaleY = 1.06f
                    }
            )

            FullClockGlow(
                wall.accentLight,
                if (a.darkMode) .18f else .24f,
                330,
                Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (-90).dp
                    )
                    .graphicsLayer {
                        translationX = z * 190f
                        translationY = -y * 130f
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
                fadeIn(tween(340)) +
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
            FullClockBrand(
                modifier = Modifier.padding(
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
                fadeIn(tween(340)) +
                    slideInVertically(
                        initialOffsetY = {
                            -it / 3
                        },
                        animationSpec = tween(
                            410,
                            easing = EaseOutCubic
                        )
                    ),
            exit = fadeOut(tween(210))
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 15.dp,
                        end = 16.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    FullClockCarousel(
                        title = "FONT",
                        options = fullClockFonts,
                        index = fontIndex,
                        accent = tone.accent,
                        width = 180,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = {
                            fontIndex = it
                        }
                    )

                    FullClockCarousel(
                        title = "STYLE",
                        options = fullClockStyles,
                        index = styleIndex,
                        accent = tone.accent,
                        width = 180,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = {
                            styleIndex = it
                        }
                    )

                    FullClockCarousel(
                        title = "COLOR",
                        options =
                            fullClockTones.map {
                                it.name
                            },
                        index = colorIndex,
                        accent = tone.accent,
                        width = 180,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = {
                            colorIndex = it
                        }
                    )
                }
            } else {
                Column(
                    Modifier.padding(
                        top = 86.dp,
                        end = 12.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    FullClockCarousel(
                        "FONT",
                        fullClockFonts,
                        fontIndex,
                        tone.accent,
                        196,
                        a.fontFamily,
                        selectedFont
                    ) {
                        fontIndex = it
                    }

                    FullClockCarousel(
                        "STYLE",
                        fullClockStyles,
                        styleIndex,
                        tone.accent,
                        196,
                        a.fontFamily,
                        selectedFont
                    ) {
                        styleIndex = it
                    }

                    FullClockCarousel(
                        "COLOR",
                        fullClockTones.map {
                            it.name
                        },
                        colorIndex,
                        tone.accent,
                        196,
                        a.fontFamily,
                        selectedFont
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
                                400,
                                easing = EaseOutCubic
                            )
                        ) +
                            scaleIn(
                                initialScale = .95f,
                                animationSpec = tween(
                                    420,
                                    easing = EaseOutCubic
                                )
                            ) +
                            slideInVertically(
                                initialOffsetY = {
                                    it / 8
                                },
                                animationSpec = tween(
                                    420,
                                    easing = EaseOutCubic
                                )
                            )
                        ) togetherWith (
                        fadeOut(tween(240)) +
                            scaleOut(
                                targetScale = 1.025f,
                                animationSpec = tween(290)
                            ) +
                            slideOutVertically(
                                targetOffsetY = {
                                    -it / 9
                                },
                                animationSpec = tween(290)
                            )
                        )
                },
                label = "clockFaceStyle"
            ) { style ->
                FullClockFace(
                    style = style,
                    time = displayedTime,
                    originalTime = time,
                    date = date,
                    tone = tone,
                    font = selectedFont,
                    landscape = landscape,
                    secondsVisible = secondsVisible
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
                    fadeOut(tween(220)) +
                        slideOutVertically(
                            targetOffsetY = {
                                it / 2
                            },
                            animationSpec = tween(280)
                        )
            ) {
                FullClockBrand(
                    modifier =
                        Modifier.padding(top = 20.dp),
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
                    FullClockWallpaperPanel(
                        selected = wallpaperIndex,
                        customSelected =
                            customUri != null,
                        selectedColor =
                            if (customUri != null) {
                                Color.White.copy(
                                    alpha = .35f
                                )
                            } else {
                                wall.accentLight
                            },
                        font = selectedFont,
                        dark = a.darkMode,
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
                        Arrangement.spacedBy(8.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    FullClockAction(
                        "Wallpaper",
                        NmixIcon.WALLPAPER,
                        selectedFont
                    ) {
                        wallpaperOpen =
                            !wallpaperOpen
                    }

                    FullClockAction(
                        "Rotate",
                        NmixIcon.ROTATE,
                        selectedFont
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

                    FullClockAction(
                        if (secondsVisible) {
                            "Seconds"
                        } else {
                            "No sec"
                        },
                        NmixIcon.CLOCK,
                        selectedFont,
                        selected = secondsVisible,
                        selectedColor = tone.accent
                    ) {
                        secondsVisible =
                            !secondsVisible
                    }

                    FullClockAction(
                        "Clean",
                        NmixIcon.FULLSCREEN,
                        selectedFont
                    ) {
                        wallpaperOpen = false
                        clean = true
                    }

                    FullClockAction(
                        "Exit",
                        NmixIcon.CLOSE,
                        selectedFont,
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
private fun FullClockCarousel(
    title: String,
    options: List<String>,
    index: Int,
    accent: Color,
    width: Int,
    headingFont: FontFamily,
    optionFont: FontFamily,
    onIndex: (Int) -> Unit
) {
    val density =
        androidx.compose.ui.platform.LocalDensity.current

    var dragPx by remember {
        mutableFloatStateOf(0f)
    }

    val slotPx =
        with(density) {
            (width.dp / 3f).toPx()
        }

    val animatedDrag by animateFloatAsState(
        targetValue = dragPx,
        animationSpec =
            if (dragPx == 0f) {
                spring(
                    dampingRatio = .82f,
                    stiffness = 500f
                )
            } else {
                snap()
            },
        label = "carouselDrag"
    )

    fun wrap(value: Int): Int {
        return (
            (value % options.size) +
                options.size
            ) % options.size
    }

    fun commit(direction: Int) {
        onIndex(
            wrap(index + direction)
        )
    }

    val shape =
        RoundedCornerShape(50)

    Column(
        Modifier
            .width(width.dp)
            .height(56.dp)
            .clip(shape)
            .background(
                Color.White.copy(alpha = .095f)
            )
            .border(
                .5.dp,
                Color.White.copy(alpha = .19f),
                shape
            )
            .pointerInput(
                index,
                options.size
            ) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragPx = 0f
                    },
                    onHorizontalDrag = {
                        change,
                        amount ->

                        change.consume()
                        dragPx += amount

                        while (dragPx <= -slotPx) {
                            dragPx += slotPx
                            commit(1)
                        }

                        while (dragPx >= slotPx) {
                            dragPx -= slotPx
                            commit(-1)
                        }
                    },
                    onDragEnd = {
                        if (
                            abs(dragPx) >
                            slotPx * .36f
                        ) {
                            if (dragPx < 0f) {
                                commit(1)
                            } else {
                                commit(-1)
                            }
                        }

                        dragPx = 0f
                    },
                    onDragCancel = {
                        dragPx = 0f
                    }
                )
            }
            .padding(top = 5.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = .90f),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = headingFont
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 50.dp,
                        bottomEnd = 50.dp
                    )
                )
        ) {
            val progress =
                if (slotPx > 0f) {
                    animatedDrag / slotPx
                } else {
                    0f
                }

            for (offset in -2..2) {
                val relative =
                    offset + progress

                if (abs(relative) <= 1.75f) {
                    val centerAmount =
                        (
                            1f -
                                abs(relative)
                                    .coerceIn(0f, 1f)
                            )

                    val scale =
                        .78f +
                            centerAmount * .24f

                    val alpha =
                        .42f +
                            centerAmount * .58f

                    val textColor =
                        Color.White.copy(alpha = alpha)

                    val finalColor =
                        if (
                            centerAmount > .72f
                        ) {
                            lerpColor(
                                Color.White,
                                accent,
                                (
                                    (centerAmount - .72f) /
                                        .28f
                                    ).coerceIn(
                                    0f,
                                    1f
                                )
                            )
                        } else {
                            textColor
                        }

                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationX =
                                    relative * slotPx

                                scaleX = scale
                                scaleY = scale
                            },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            options[
                                wrap(index + offset)
                            ],
                            color = finalColor,
                            fontSize = 10.sp,
                            fontWeight =
                                if (
                                    centerAmount > .65f
                                ) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                            fontFamily = optionFont,
                            maxLines = 1,
                            textAlign =
                                TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun lerpColor(
    start: Color,
    end: Color,
    amount: Float
): Color {
    val t =
        amount.coerceIn(0f, 1f)

    return Color(
        red =
            start.red +
                (end.red - start.red) * t,
        green =
            start.green +
                (end.green - start.green) * t,
        blue =
            start.blue +
                (end.blue - start.blue) * t,
        alpha =
            start.alpha +
                (end.alpha - start.alpha) * t
    )
}

private fun displayClockTime(
    time: String,
    seconds: Boolean
): String {
    if (seconds) {
        return time
    }

    val period =
        when {
            time.contains(" AM") -> " AM"
            time.contains(" PM") -> " PM"
            else -> ""
        }

    val raw =
        time.removeSuffix(" AM")
            .removeSuffix(" PM")

    val parts =
        raw.split(":")

    return if (parts.size >= 2) {
        "${parts[0]}:${parts[1]}$period"
    } else {
        time
    }
}

@Composable
private fun FullClockFace(
    style: Int,
    time: String,
    originalTime: String,
    date: String,
    tone: FullClockTone,
    font: FontFamily,
    landscape: Boolean,
    secondsVisible: Boolean
) {
    val raw =
        originalTime
            .removeSuffix(" AM")
            .removeSuffix(" PM")

    val parts = raw.split(":")

    val hour =
        parts.getOrElse(0) { "00" }

    val minute =
        parts.getOrElse(1) { "00" }

    val second =
        parts.getOrElse(2) { "00" }

    val period =
        when {
            originalTime.contains("AM") ->
                "AM"

            originalTime.contains("PM") ->
                "PM"

            else ->
                ""
        }

    when (style) {
        1 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "$hour:$minute",
                    color = tone.main,
                    fontSize =
                        if (landscape)
                            78.sp
                        else
                            59.sp,
                    fontWeight =
                        FontWeight.Normal,
                    letterSpacing = 1.sp,
                    fontFamily = font
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

                    if (secondsVisible) {
                        Text(
                            "$second $period",
                            color = tone.accent,
                            fontSize = 11.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily = font
                        )
                    } else {
                        Text(
                            period,
                            color = tone.accent,
                            fontSize = 11.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily = font
                        )
                    }

                    Text(
                        date,
                        color =
                            tone.main.copy(
                                alpha = .68f
                            ),
                        fontSize = 10.sp,
                        fontFamily = font
                    )
                }
            }
        }

        2 -> {
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
                            if (landscape)
                                58.sp
                            else
                                47.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        minute,
                        color = tone.accent,
                        fontSize =
                            if (landscape)
                                58.sp
                            else
                                47.sp,
                        fontWeight =
                            FontWeight.Bold,
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
                    if (secondsVisible) {
                        Text(
                            "SECONDS",
                            color =
                                tone.main.copy(
                                    alpha = .46f
                                ),
                            fontSize = 7.sp,
                            letterSpacing = 1.4.sp,
                            fontFamily = font
                        )

                        Text(
                            second,
                            color = tone.main,
                            fontSize = 29.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily = font
                        )
                    }

                    Text(
                        period,
                        color = tone.accent,
                        fontSize = 10.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Text(
                        date,
                        color =
                            tone.main.copy(
                                alpha = .62f
                            ),
                        fontSize = 9.sp,
                        fontFamily = font
                    )
                }
            }
        }

        3 -> {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    time,
                    color =
                        tone.accent.copy(
                            alpha = .34f
                        ),
                    fontSize =
                        if (landscape)
                            69.sp
                        else
                            47.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily = font,
                    modifier =
                        Modifier.blur(14.dp)
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    ClockFaceCaption(
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
                            if (landscape)
                                69.sp
                            else
                                47.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    ClockFaceDate(
                        date,
                        tone,
                        font
                    )
                }
            }
        }

        4 -> {
            Box(
                Modifier.size(
                    if (landscape)
                        245.dp
                    else
                        218.dp
                ),
                contentAlignment =
                    Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            tone.accent.copy(
                                alpha = .27f
                            ),
                            CircleShape
                        )
                )

                Box(
                    Modifier
                        .size(
                            if (landscape)
                                188.dp
                            else
                                166.dp
                        )
                        .border(
                            .7.dp,
                            tone.accent.copy(
                                alpha = .16f
                            ),
                            CircleShape
                        )
                )

                Box(
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
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
                            if (landscape)
                                48.sp
                            else
                                41.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        if (secondsVisible) {
                            "$second $period"
                        } else {
                            period
                        },
                        color = tone.accent,
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    Spacer(
                        Modifier.height(5.dp)
                    )

                    Text(
                        date,
                        color =
                            tone.main.copy(
                                alpha = .60f
                            ),
                        fontSize = 8.sp,
                        fontFamily = font
                    )
                }
            }
        }

        5 -> {
            Column(
                horizontalAlignment =
                    Alignment.Start
            ) {
                Text(
                    "NMIX://LOCAL_CLOCK",
                    color = tone.accent,
                    fontSize = 9.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily = font
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    "> $time",
                    color = tone.main,
                    fontSize =
                        if (landscape)
                            58.sp
                        else
                            41.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily = font
                )

                Text(
                    "> DATE  $date",
                    color =
                        tone.accent.copy(
                            alpha = .78f
                        ),
                    fontSize = 10.sp,
                    fontFamily = font
                )

                Text(
                    "> STATUS  LIVE",
                    color =
                        tone.main.copy(
                            alpha = .46f
                        ),
                    fontSize = 8.sp,
                    fontFamily = font
                )
            }
        }

        6 -> {
            val shape =
                RoundedCornerShape(50)

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .clip(shape)
                        .background(
                            Color.White.copy(
                                alpha = .09f
                            )
                        )
                        .border(
                            .6.dp,
                            tone.accent.copy(
                                alpha = .29f
                            ),
                            shape
                        )
                        .padding(
                            horizontal =
                                if (landscape)
                                    27.dp
                                else
                                    20.dp,
                            vertical = 13.dp
                        )
                ) {
                    Text(
                        time,
                        color = tone.main,
                        fontSize =
                            if (landscape)
                                55.sp
                            else
                                39.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                Text(
                    date,
                    color =
                        tone.accent.copy(
                            alpha = .90f
                        ),
                    fontSize = 10.sp,
                    fontFamily = font
                )
            }
        }

        7 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "LOCAL",
                    color =
                        tone.main.copy(
                            alpha = .43f
                        ),
                    fontSize = 8.sp,
                    letterSpacing = 4.sp,
                    fontFamily = font
                )

                Spacer(
                    Modifier.height(6.dp)
                )

                Row(
                    verticalAlignment =
                        Alignment.Bottom,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "$hour:$minute",
                        color = tone.main,
                        fontSize =
                            if (landscape)
                                72.sp
                            else
                                53.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily = font
                    )

                    Column {
                        if (secondsVisible) {
                            Text(
                                second,
                                color = tone.accent,
                                fontSize = 21.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                fontFamily = font
                            )
                        }

                        Text(
                            period,
                            color =
                                tone.main.copy(
                                    alpha = .60f
                                ),
                            fontSize = 9.sp,
                            fontFamily = font
                        )
                    }
                }

                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .width(
                            if (landscape)
                                290.dp
                            else
                                225.dp
                        )
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    tone.accent,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    date,
                    color =
                        tone.main.copy(
                            alpha = .66f
                        ),
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
                ClockFaceCaption(
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
                        if (landscape)
                            67.sp
                        else
                            47.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily = font
                )

                ClockFaceDate(
                    date,
                    tone,
                    font
                )
            }
        }
    }
}

@Composable
private fun ClockFaceCaption(
    text: String,
    tone: FullClockTone,
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
private fun ClockFaceDate(
    date: String,
    tone: FullClockTone,
    font: FontFamily
) {
    Spacer(
        Modifier.height(9.dp)
    )

    Text(
        date,
        color =
            tone.main.copy(
                alpha = .67f
            ),
        fontSize = 11.sp,
        fontFamily = font
    )
}

@Composable
private fun FullClockBrand(
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    val a =
        LocalNmixAppearance.current

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
            color =
                Color.White.copy(
                    alpha = .64f
                ),
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
private fun FullClockAction(
    text: String,
    icon: NmixIcon,
    font: FontFamily,
    red: Boolean = false,
    selected: Boolean = false,
    selectedColor: Color = Color.White,
    onClick: () -> Unit
) {
    val color =
        when {
            red ->
                Color(0xFFFF8585)

            selected ->
                selectedColor

            else ->
                Color.White
        }

    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(46.dp)
            .clip(shape)
            .background(
                when {
                    red ->
                        Color(0xFFB8444B)
                            .copy(alpha = .21f)

                    selected ->
                        selectedColor.copy(
                            alpha = .14f
                        )

                    else ->
                        Color.White.copy(
                            alpha = .10f
                        )
                }
            )
            .border(
                .6.dp,
                color.copy(
                    alpha =
                        if (red)
                            .50f
                        else
                            .24f
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
                horizontal = 12.dp
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
            fontWeight =
                FontWeight.SemiBold,
            fontFamily = font
        )
    }
}

@Composable
private fun FullClockWallpaperPanel(
    selected: Int,
    customSelected: Boolean,
    selectedColor: Color,
    font: FontFamily,
    dark: Boolean,
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
        RoundedCornerShape(50)

    Row(
        Modifier
            .clip(shape)
            .background(
                Color.White.copy(
                    alpha =
                        if (dark)
                            .105f
                        else
                            .17f
                )
            )
            .border(
                .55.dp,
                selectedColor.copy(
                    alpha = .45f
                ),
                shape
            )
            .padding(7.dp),
        horizontalArrangement =
            Arrangement.spacedBy(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        names.forEachIndexed {
            index,
            name ->

            FullClockWallpaperPill(
                name = name,
                color = colors[index],
                selected =
                    selected == index &&
                        !customSelected,
                font = font
            ) {
                onSelect(index)
            }
        }

        GalleryWallpaperPill(
            selected = customSelected,
            font = font,
            onClick = onCustom
        )
    }
}

@Composable
private fun FullClockWallpaperPill(
    name: String,
    color: Color,
    selected: Boolean,
    font: FontFamily,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(30.dp)
            .clip(shape)
            .background(
                Color.White.copy(
                    alpha =
                        if (selected)
                            .14f
                        else
                            .055f
                )
            )
            .border(
                .5.dp,
                if (selected) {
                    color.copy(alpha = .78f)
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
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            name,
            color = Color.White,
            fontSize = 7.sp,
            fontFamily = font,
            maxLines = 1
        )
    }
}

@Composable
private fun GalleryWallpaperPill(
    selected: Boolean,
    font: FontFamily,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(30.dp)
            .clip(shape)
            .background(
                Color.White.copy(
                    alpha =
                        if (selected)
                            .14f
                        else
                            .055f
                )
            )
            .border(
                .5.dp,
                Color.White.copy(
                    alpha =
                        if (selected)
                            .38f
                        else
                            .12f
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
                horizontal = 8.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        NmixIcon(
            NmixIcon.WALLPAPER,
            Modifier.size(12.dp),
            Color.White.copy(alpha = .88f)
        )

        Text(
            "Custom",
            color = Color.White,
            fontSize = 7.sp,
            fontFamily = font
        )
    }
}

@Composable
private fun FullClockGlow(
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
                            color.copy(
                                alpha = alpha
                            ),

                        .22f to
                            color.copy(
                                alpha =
                                    alpha * .83f
                            ),

                        .48f to
                            color.copy(
                                alpha =
                                    alpha * .43f
                            ),

                        .73f to
                            color.copy(
                                alpha =
                                    alpha * .13f
                            ),

                        1f to
                            Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

@Composable
private fun FullClockCustomWallpaper(
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
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )
    }
}
