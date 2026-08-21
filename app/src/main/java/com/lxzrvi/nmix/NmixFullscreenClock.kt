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
import androidx.compose.ui.platform.LocalDensity
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
    FullClockTone("Ice", Color(0xFFF3FAFF), Color(0xFF70D8FF)),
    FullClockTone("Mint", Color(0xFFE9FFF7), Color(0xFF52E0AD)),
    FullClockTone("Amber", Color(0xFFFFF5E2), Color(0xFFFFB85A)),
    FullClockTone("Rose", Color(0xFFFFEDF4), Color(0xFFFF7EA8)),
    FullClockTone("Violet", Color(0xFFF6EEFF), Color(0xFFB792FF)),
    FullClockTone("Aqua", Color(0xFFE8FEFF), Color(0xFF50DDE8))
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
    val activity = LocalActivity.current
    val config = LocalConfiguration.current

    val landscape =
        config.orientation == Configuration.ORIENTATION_LANDSCAPE

    var clean by rememberSaveable { mutableStateOf(false) }

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

    var styleIndex by rememberSaveable { mutableIntStateOf(0) }
    var colorIndex by rememberSaveable { mutableIntStateOf(0) }
    var wallpaperIndex by rememberSaveable { mutableIntStateOf(a.theme.ordinal) }

    var wallpaperOpen by rememberSaveable { mutableStateOf(false) }
    var displayOpen by rememberSaveable { mutableStateOf(false) }

    var showHours by rememberSaveable { mutableStateOf(true) }
    var showMinutes by rememberSaveable { mutableStateOf(true) }
    var showSeconds by rememberSaveable { mutableStateOf(true) }
    var showPeriod by rememberSaveable { mutableStateOf(true) }
    var showLabel by rememberSaveable { mutableStateOf(true) }

    var customUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val customUri = customUriString?.let(Uri::parse)

    val picker = rememberLauncherForActivityResult(
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
            WindowCompat.setDecorFitsSystemWindows(window, false)

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
                WindowCompat.setDecorFitsSystemWindows(window, true)

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

    val selectedFont = when (fontIndex) {
        1 -> NmixNunito
        2 -> NmixOutfit
        3 -> NmixPoppins
        4 -> NmixQuicksand
        else -> NmixInter
    }

    val tone = fullClockTones[colorIndex]

    val wall = NmixThemeName.entries[
        wallpaperIndex.coerceIn(
            0,
            NmixThemeName.entries.lastIndex
        )
    ].palette()

    val parts = parseTime(time)

    val visibleTime = buildVisibleTime(
        parts = parts,
        hours = showHours,
        minutes = showMinutes,
        seconds = showSeconds,
        period = showPeriod
    )

    val motion = rememberInfiniteTransition(
        label = "fullscreenWallpaper"
    )

    val mx by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(3000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "mx"
    )

    val my by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(3900, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "my"
    )

    val mz by motion.animateFloat(
        -.8f,
        .8f,
        infiniteRepeatable(
            tween(4800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "mz"
    )

    val pulse by motion.animateFloat(
        .91f,
        1.10f,
        infiniteRepeatable(
            tween(2700, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val base =
        if (a.darkMode) {
            Color(0xFF0A0D0C)
        } else {
            Color(0xFFF1F3F2)
        }

    val controlSurface =
        if (a.darkMode) {
            Color.Black.copy(alpha = .24f)
        } else {
            Color.White.copy(alpha = .54f)
        }

    val controlBorder =
        if (a.darkMode) {
            Color.White.copy(alpha = .16f)
        } else {
            Color.White.copy(alpha = .68f)
        }

    val neutralText =
        if (a.darkMode) {
            Color(0xFFF3F6F5)
        } else {
            Color(0xFF26302C)
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(base)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                when {
                    clean -> clean = false
                    wallpaperOpen -> wallpaperOpen = false
                    displayOpen -> displayOpen = false
                }
            }
    ) {
        if (customUri != null) {
            FullClockCustomWallpaper(customUri)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (a.darkMode) {
                            Color.Black.copy(alpha = .16f)
                        } else {
                            Color.White.copy(alpha = .08f)
                        }
                    )
            )
        } else {
            WallpaperGlow(
                color = wall.accent,
                alpha = if (a.darkMode) .37f else .31f,
                size = 430,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (-115).dp,
                        y = (-125).dp
                    )
                    .graphicsLayer {
                        translationX = mx * 235f
                        translationY = my * 100f
                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            WallpaperGlow(
                color = wall.accentLight,
                alpha = if (a.darkMode) .27f else .35f,
                size = 390,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x = 110.dp,
                        y = 105.dp
                    )
                    .graphicsLayer {
                        translationX = -mx * 205f
                        translationY = mz * 125f
                    }
            )

            WallpaperGlow(
                color = wall.accent,
                alpha = if (a.darkMode) .16f else .20f,
                size = 300,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = mz * 170f
                        translationY = -my * 105f
                    }
            )
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            enter =
                fadeIn(tween(320)) +
                    slideInVertically(
                        { -it / 3 },
                        tween(390, easing = EaseOutCubic)
                    ),
            exit =
                fadeOut(tween(210)) +
                    slideOutVertically(
                        { -it / 3 },
                        tween(280)
                    )
        ) {
            FullClockBrand(
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
                .windowInsetsPadding(WindowInsets.safeDrawing),
            enter =
                fadeIn(tween(340)) +
                    slideInVertically(
                        { -it / 4 },
                        tween(400, easing = EaseOutCubic)
                    ),
            exit = fadeOut(tween(210))
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 13.dp,
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
                        width = 176,
                        surface = controlSurface,
                        border = controlBorder,
                        sideColor = neutralText,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = { fontIndex = it }
                    )

                    FullClockCarousel(
                        title = "STYLE",
                        options = fullClockStyles,
                        index = styleIndex,
                        accent = tone.accent,
                        width = 176,
                        surface = controlSurface,
                        border = controlBorder,
                        sideColor = neutralText,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = { styleIndex = it }
                    )

                    FullClockCarousel(
                        title = "COLOR",
                        options = fullClockTones.map { it.name },
                        index = colorIndex,
                        accent = tone.accent,
                        width = 176,
                        surface = controlSurface,
                        border = controlBorder,
                        sideColor = neutralText,
                        headingFont = a.fontFamily,
                        optionFont = selectedFont,
                        onIndex = { colorIndex = it }
                    )
                }
            } else {
                Column(
                    Modifier.padding(
                        top = 16.dp,
                        end = 12.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    FullClockCarousel(
                        "FONT",
                        fullClockFonts,
                        fontIndex,
                        tone.accent,
                        188,
                        controlSurface,
                        controlBorder,
                        neutralText,
                        a.fontFamily,
                        selectedFont
                    ) { fontIndex = it }

                    FullClockCarousel(
                        "STYLE",
                        fullClockStyles,
                        styleIndex,
                        tone.accent,
                        188,
                        controlSurface,
                        controlBorder,
                        neutralText,
                        a.fontFamily,
                        selectedFont
                    ) { styleIndex = it }

                    FullClockCarousel(
                        "COLOR",
                        fullClockTones.map { it.name },
                        colorIndex,
                        tone.accent,
                        188,
                        controlSurface,
                        controlBorder,
                        neutralText,
                        a.fontFamily,
                        selectedFont
                    ) { colorIndex = it }
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 18.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = styleIndex,
                transitionSpec = {
                    (
                        fadeIn(
                            tween(
                                380,
                                easing = EaseOutCubic
                            )
                        ) +
                            scaleIn(
                                .96f,
                                tween(
                                    400,
                                    easing = EaseOutCubic
                                )
                            ) +
                            slideInVertically(
                                { it / 10 },
                                tween(
                                    400,
                                    easing = EaseOutCubic
                                )
                            )
                        ) togetherWith (
                        fadeOut(tween(230)) +
                            scaleOut(
                                1.025f,
                                tween(280)
                            ) +
                            slideOutVertically(
                                { -it / 10 },
                                tween(280)
                            )
                        )
                },
                label = "clockStyle"
            ) { style ->
                FullClockFace(
                    style = style,
                    visibleTime = visibleTime,
                    parts = parts,
                    date = date,
                    tone = tone,
                    font = selectedFont,
                    landscape = landscape,
                    showHours = showHours,
                    showMinutes = showMinutes,
                    showSeconds = showSeconds,
                    showPeriod = showPeriod
                )
            }

            AnimatedVisibility(
                visible = clean && showLabel,
                enter =
                    fadeIn(tween(400)) +
                        slideInVertically(
                            { it },
                            tween(
                                470,
                                easing = EaseOutCubic
                            )
                        ),
                exit =
                    fadeOut(tween(220)) +
                        slideOutVertically(
                            { it / 2 },
                            tween(270)
                        )
            ) {
                FullClockBrand(
                    Modifier.padding(top = 18.dp),
                    centered = true
                )
            }
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            enter =
                fadeIn(tween(320)) +
                    slideInVertically(
                        { it / 2 },
                        tween(
                            390,
                            easing = EaseOutCubic
                        )
                    ),
            exit =
                fadeOut(tween(210)) +
                    slideOutVertically(
                        { it / 2 },
                        tween(280)
                    )
        ) {
            Column(
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 16.dp
                ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = wallpaperOpen,
                    enter =
                        fadeIn(tween(260)) +
                            slideInVertically(
                                { it / 3 },
                                tween(
                                    320,
                                    easing = EaseOutCubic
                                )
                            ),
                    exit =
                        fadeOut(tween(180)) +
                            slideOutVertically(
                                { it / 4 },
                                tween(240)
                            )
                ) {
                    WallpaperPanel(
                        selected = wallpaperIndex,
                        customSelected = customUri != null,
                        selectedColor = wall.accent,
                        surface = controlSurface,
                        border = controlBorder,
                        textColor = neutralText,
                        font = selectedFont,
                        onSelect = {
                            wallpaperIndex = it
                            customUriString = null
                        },
                        onCustom = {
                            picker.launch("image/*")
                        }
                    )
                }

                AnimatedVisibility(
                    visible = displayOpen,
                    enter =
                        fadeIn(tween(260)) +
                            slideInVertically(
                                { it / 3 },
                                tween(
                                    320,
                                    easing = EaseOutCubic
                                )
                            ),
                    exit =
                        fadeOut(tween(180)) +
                            slideOutVertically(
                                { it / 4 },
                                tween(240)
                            )
                ) {
                    DisplayPanel(
                        surface = controlSurface,
                        border = controlBorder,
                        textColor = neutralText,
                        accent = tone.accent,
                        font = selectedFont,
                        hours = showHours,
                        minutes = showMinutes,
                        seconds = showSeconds,
                        period = showPeriod,
                        label = showLabel,
                        onHours = { showHours = !showHours },
                        onMinutes = { showMinutes = !showMinutes },
                        onSeconds = { showSeconds = !showSeconds },
                        onPeriod = { showPeriod = !showPeriod },
                        onLabel = { showLabel = !showLabel }
                    )
                }

                if (wallpaperOpen || displayOpen) {
                    Spacer(Modifier.height(10.dp))
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
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        neutralText
                    ) {
                        displayOpen = false
                        wallpaperOpen = !wallpaperOpen
                    }

                    FullClockAction(
                        "Rotate",
                        NmixIcon.ROTATE,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        neutralText
                    ) {
                        activity?.requestedOrientation =
                            if (landscape) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            }
                    }

                    FullClockAction(
                        "Display",
                        NmixIcon.CLOCK,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        neutralText
                    ) {
                        wallpaperOpen = false
                        displayOpen = !displayOpen
                    }

                    FullClockAction(
                        "Clean",
                        NmixIcon.FULLSCREEN,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        neutralText
                    ) {
                        wallpaperOpen = false
                        displayOpen = false
                        clean = true
                    }

                    FullClockAction(
                        "Exit",
                        NmixIcon.CLOSE,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        neutralText,
                        red = true,
                        onClick = onExit
                    )
                }
            }
        }
    }
}

private data class ClockParts(
    val hour: String,
    val minute: String,
    val second: String,
    val period: String
)

private fun parseTime(time: String): ClockParts {
    val period = when {
        time.contains("AM") -> "AM"
        time.contains("PM") -> "PM"
        else -> ""
    }

    val raw =
        time.removeSuffix(" AM")
            .removeSuffix(" PM")

    val p = raw.split(":")

    return ClockParts(
        hour = p.getOrElse(0) { "00" },
        minute = p.getOrElse(1) { "00" },
        second = p.getOrElse(2) { "00" },
        period = period
    )
}

private fun buildVisibleTime(
    parts: ClockParts,
    hours: Boolean,
    minutes: Boolean,
    seconds: Boolean,
    period: Boolean
): String {
    val numeric = buildList {
        if (hours) add(parts.hour)
        if (minutes) add(parts.minute)
        if (seconds) add(parts.second)
    }.joinToString(":")

    return when {
        period && numeric.isNotEmpty() ->
            "$numeric ${parts.period}"

        period ->
            parts.period

        else ->
            numeric
    }
}

@Composable
private fun FullClockCarousel(
    title: String,
    options: List<String>,
    index: Int,
    accent: Color,
    width: Int,
    surface: Color,
    border: Color,
    sideColor: Color,
    headingFont: FontFamily,
    optionFont: FontFamily,
    onIndex: (Int) -> Unit
) {
    val density = LocalDensity.current

    var drag by remember {
        mutableFloatStateOf(0f)
    }

    var dragging by remember {
        mutableStateOf(false)
    }

    val slot =
        with(density) {
            (width.dp * .31f).toPx()
        }

    val displayedDrag by animateFloatAsState(
        targetValue = drag,
        animationSpec =
            if (dragging) {
                snap()
            } else {
                spring(
                    dampingRatio = .88f,
                    stiffness = 520f
                )
            },
        label = "carouselReturn"
    )

    fun wrap(i: Int): Int {
        return ((i % options.size) + options.size) % options.size
    }

    val shape = RoundedCornerShape(50)

    Column(
        Modifier
            .width(width.dp)
            .height(52.dp)
            .clip(shape)
            .background(surface)
            .border(.5.dp, border, shape)
            .pointerInput(index, options.size) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging = true
                        drag = 0f
                    },
                    onHorizontalDrag = {
                        change,
                        amount ->

                        change.consume()

                        drag = (
                            drag + amount
                            ).coerceIn(
                            -slot * .92f,
                            slot * .92f
                        )
                    },
                    onDragEnd = {
                        val move = when {
                            drag < -slot * .28f -> 1
                            drag > slot * .28f -> -1
                            else -> 0
                        }

                        if (move != 0) {
                            onIndex(
                                wrap(index + move)
                            )
                        }

                        drag = 0f
                        dragging = false
                    },
                    onDragCancel = {
                        drag = 0f
                        dragging = false
                    }
                )
            }
            .padding(top = 4.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = accent,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = .9.sp,
            fontFamily = headingFont
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(shape)
        ) {
            val progress =
                if (slot == 0f) 0f
                else displayedDrag / slot

            for (offset in -2..2) {
                val position = offset + progress

                if (abs(position) < 1.7f) {
                    val center =
                        1f -
                            abs(position)
                                .coerceIn(0f, 1f)

                    val scale =
                        .76f + center * .34f

                    val alpha =
                        .38f + center * .62f

                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationX =
                                    position * slot

                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            options[wrap(index + offset)],
                            color =
                                if (center > .62f) {
                                    sideColor.copy(
                                        alpha = .98f
                                    )
                                } else {
                                    sideColor.copy(
                                        alpha = .50f
                                    )
                                },
                            fontSize = 10.sp,
                            fontWeight =
                                if (center > .62f) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                            fontFamily = optionFont,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayPanel(
    surface: Color,
    border: Color,
    textColor: Color,
    accent: Color,
    font: FontFamily,
    hours: Boolean,
    minutes: Boolean,
    seconds: Boolean,
    period: Boolean,
    label: Boolean,
    onHours: () -> Unit,
    onMinutes: () -> Unit,
    onSeconds: () -> Unit,
    onPeriod: () -> Unit,
    onLabel: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        Modifier
            .width(190.dp)
            .clip(shape)
            .background(surface)
            .border(.5.dp, border, shape)
            .padding(9.dp),
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        VisibilityOption(
            "Hours",
            hours,
            accent,
            textColor,
            font,
            onHours
        )

        VisibilityOption(
            "Minutes",
            minutes,
            accent,
            textColor,
            font,
            onMinutes
        )

        VisibilityOption(
            "Seconds",
            seconds,
            accent,
            textColor,
            font,
            onSeconds
        )

        VisibilityOption(
            "AM / PM",
            period,
            accent,
            textColor,
            font,
            onPeriod
        )

        VisibilityOption(
            "Label",
            label,
            accent,
            textColor,
            font,
            onLabel
        )
    }
}

@Composable
private fun VisibilityOption(
    text: String,
    enabled: Boolean,
    accent: Color,
    textColor: Color,
    font: FontFamily,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(shape)
            .background(
                if (enabled) {
                    accent.copy(alpha = .13f)
                } else {
                    Color.White.copy(alpha = .055f)
                }
            )
            .border(
                .45.dp,
                if (enabled) {
                    accent.copy(alpha = .35f)
                } else {
                    textColor.copy(alpha = .11f)
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
            .padding(horizontal = 11.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = textColor,
            fontSize = 9.sp,
            fontFamily = font
        )

        Text(
            if (enabled) "ON" else "OFF",
            color =
                if (enabled) accent
                else textColor.copy(alpha = .45f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font
        )
    }
}

@Composable
private fun WallpaperPanel(
    selected: Int,
    customSelected: Boolean,
    selectedColor: Color,
    surface: Color,
    border: Color,
    textColor: Color,
    font: FontFamily,
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

    val shape = RoundedCornerShape(22.dp)

    Column(
        Modifier
            .clip(shape)
            .background(surface)
            .border(
                .6.dp,
                if (customSelected) {
                    border
                } else {
                    selectedColor.copy(alpha = .48f)
                },
                shape
            )
            .padding(11.dp),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "WALLPAPER",
            color = textColor,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = font
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            names.forEachIndexed {
                index,
                name ->

                WallpaperChoice(
                    name = name,
                    color = colors[index],
                    selected =
                        selected == index &&
                            !customSelected,
                    textColor = textColor,
                    font = font
                ) {
                    onSelect(index)
                }
            }

            GalleryChoice(
                selected = customSelected,
                textColor = textColor,
                font = font,
                onClick = onCustom
            )
        }
    }
}

@Composable
private fun WallpaperChoice(
    name: String,
    color: Color,
    selected: Boolean,
    textColor: Color,
    font: FontFamily,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        Modifier
            .height(34.dp)
            .clip(shape)
            .background(
                color.copy(
                    alpha =
                        if (selected) .19f
                        else .09f
                )
            )
            .border(
                .5.dp,
                color.copy(
                    alpha =
                        if (selected) .68f
                        else .23f
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
            .padding(horizontal = 9.dp),
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
            color = textColor,
            fontSize = 7.sp,
            fontFamily = font,
            maxLines = 1
        )
    }
}

@Composable
private fun GalleryChoice(
    selected: Boolean,
    textColor: Color,
    font: FontFamily,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        Modifier
            .height(34.dp)
            .clip(shape)
            .background(
                textColor.copy(
                    alpha =
                        if (selected) .14f
                        else .06f
                )
            )
            .border(
                .5.dp,
                textColor.copy(
                    alpha =
                        if (selected) .35f
                        else .13f
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
            .padding(horizontal = 9.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        NmixIcon(
            NmixIcon.WALLPAPER,
            Modifier.size(13.dp),
            textColor
        )

        Text(
            "Custom",
            color = textColor,
            fontSize = 7.sp,
            fontFamily = font
        )
    }
}

@Composable
private fun FullClockFace(
    style: Int,
    visibleTime: String,
    parts: ClockParts,
    date: String,
    tone: FullClockTone,
    font: FontFamily,
    landscape: Boolean,
    showHours: Boolean,
    showMinutes: Boolean,
    showSeconds: Boolean,
    showPeriod: Boolean
) {
    when (style) {
        1 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    visibleTime,
                    color = tone.main,
                    fontSize =
                        if (landscape) 74.sp
                        else 56.sp,
                    fontFamily = font
                )

                Text(
                    date,
                    color = tone.accent,
                    fontSize = 10.sp,
                    fontFamily = font
                )
            }
        }

        2 -> {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(15.dp)
            ) {
                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {
                    if (showHours) {
                        Text(
                            parts.hour,
                            color = tone.main,
                            fontSize =
                                if (landscape) 56.sp
                                else 45.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        )
                    }

                    if (showMinutes) {
                        Text(
                            parts.minute,
                            color = tone.accent,
                            fontSize =
                                if (landscape) 56.sp
                                else 45.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        )
                    }
                }

                Column {
                    if (showSeconds) {
                        Text(
                            parts.second,
                            color = tone.main,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        )
                    }

                    if (showPeriod) {
                        Text(
                            parts.period,
                            color = tone.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font
                        )
                    }

                    Text(
                        date,
                        color = tone.main.copy(alpha = .60f),
                        fontSize = 9.sp,
                        fontFamily = font
                    )
                }
            }
        }

        3 -> {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    visibleTime,
                    color = tone.accent.copy(alpha = .32f),
                    fontSize =
                        if (landscape) 66.sp
                        else 46.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font,
                    modifier = Modifier.blur(13.dp)
                )

                Text(
                    visibleTime,
                    color = tone.main,
                    fontSize =
                        if (landscape) 66.sp
                        else 46.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )
            }
        }

        4 -> {
            Box(
                Modifier.size(
                    if (landscape) 230.dp
                    else 205.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            tone.accent.copy(alpha = .27f),
                            CircleShape
                        )
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        visibleTime,
                        color = tone.main,
                        fontSize =
                            if (landscape) 41.sp
                            else 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )

                    Text(
                        date,
                        color = tone.accent,
                        fontSize = 8.sp,
                        fontFamily = font
                    )
                }
            }
        }

        5 -> {
            Column {
                Text(
                    "NMIX://LOCAL_CLOCK",
                    color = tone.accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Text(
                    "> $visibleTime",
                    color = tone.main,
                    fontSize =
                        if (landscape) 55.sp
                        else 39.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Text(
                    "> $date",
                    color = tone.accent.copy(alpha = .78f),
                    fontSize = 10.sp,
                    fontFamily = font
                )
            }
        }

        6 -> {
            val shape = RoundedCornerShape(50)

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .clip(shape)
                        .background(
                            Color.White.copy(alpha = .09f)
                        )
                        .border(
                            .6.dp,
                            tone.accent.copy(alpha = .28f),
                            shape
                        )
                        .padding(
                            horizontal = 25.dp,
                            vertical = 13.dp
                        )
                ) {
                    Text(
                        visibleTime,
                        color = tone.main,
                        fontSize =
                            if (landscape) 53.sp
                            else 38.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font
                    )
                }

                Spacer(Modifier.height(9.dp))

                Text(
                    date,
                    color = tone.accent,
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
                    color = tone.accent,
                    fontSize = 8.sp,
                    letterSpacing = 4.sp,
                    fontFamily = font
                )

                Text(
                    visibleTime,
                    color = tone.main,
                    fontSize =
                        if (landscape) 68.sp
                        else 49.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Box(
                    Modifier
                        .width(220.dp)
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

                Spacer(Modifier.height(8.dp))

                Text(
                    date,
                    color = tone.main.copy(alpha = .64f),
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
                Text(
                    "NMIX • LOCAL TIME",
                    color = tone.accent,
                    fontSize = 9.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Spacer(Modifier.height(11.dp))

                Text(
                    visibleTime,
                    color = tone.main,
                    fontSize =
                        if (landscape) 65.sp
                        else 46.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    date,
                    color = tone.main.copy(alpha = .66f),
                    fontSize = 11.sp,
                    fontFamily = font
                )
            }
        }
    }
}

@Composable
private fun FullClockBrand(
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
private fun FullClockAction(
    text: String,
    icon: NmixIcon,
    font: FontFamily,
    surface: Color,
    border: Color,
    textColor: Color,
    red: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    val foreground =
        if (red) {
            Color(0xFFFF8585)
        } else {
            textColor
        }

    Row(
        Modifier
            .height(46.dp)
            .clip(shape)
            .background(
                if (red) {
                    Color(0xFFB8444B).copy(alpha = .20f)
                } else {
                    surface
                }
            )
            .border(
                .6.dp,
                if (red) {
                    foreground.copy(alpha = .50f)
                } else {
                    border
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
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        NmixIcon(
            icon,
            Modifier.size(17.dp),
            foreground
        )

        Text(
            text,
            color = foreground,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = font
        )
    }
}

@Composable
private fun WallpaperGlow(
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
                        0f to color.copy(alpha = alpha),
                        .26f to color.copy(alpha = alpha * .76f),
                        .53f to color.copy(alpha = alpha * .35f),
                        .78f to color.copy(alpha = alpha * .09f),
                        1f to Color.Transparent
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
    val context = LocalContext.current

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
