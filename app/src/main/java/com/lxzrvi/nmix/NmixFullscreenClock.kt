package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val CLOCK_PREFS = "nmix_fullscreen_clock"

private data class ClockTone(
    val name: String,
    val main: Color,
    val accent: Color
)

private data class ClockParts(
    val hour: String,
    val minute: String,
    val second: String,
    val period: String
)

private val clockTones = listOf(
    ClockTone(
        "White",
        Color(0xFFF8FAF9),
        Color.White
    ),
    ClockTone(
        "Black",
        Color(0xFF111311),
        Color(0xFF111311)
    ),
    ClockTone(
        "Ice",
        Color(0xFFEAF8FF),
        Color(0xFF62CFF9)
    ),
    ClockTone(
        "Mint",
        Color(0xFFE8FFF5),
        Color(0xFF4CD9A5)
    ),
    ClockTone(
        "Amber",
        Color(0xFFFFF1D9),
        Color(0xFFFFAE43)
    ),
    ClockTone(
        "Rose",
        Color(0xFFFFEAF1),
        Color(0xFFFF749F)
    ),
    ClockTone(
        "Violet",
        Color(0xFFF1E8FF),
        Color(0xFFA983F5)
    ),
    ClockTone(
        "Aqua",
        Color(0xFFE5FDFF),
        Color(0xFF43D4E0)
    )
)

private val clockStyles = listOf(
    "Digital",
    "Minimal",
    "Stack",
    "Focus",
    "Orbit",
    "Terminal",
    "Capsule",
    "Studio"
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
    val context = LocalContext.current
    val a = LocalNmixAppearance.current
    val activity = LocalActivity.current
    val configuration = LocalConfiguration.current

    val prefs = remember(context) {
        context.getSharedPreferences(
            CLOCK_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val landscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

    val defaultFont = when (a.font) {
        NmixFontName.INTER -> 0
        NmixFontName.NUNITO -> 1
        NmixFontName.OUTFIT -> 2
        NmixFontName.POPPINS -> 3
        NmixFontName.QUICKSAND -> 4
    }

    val defaultColor =
        if (a.darkMode) 0 else 1

    var fontIndex by remember {
        mutableIntStateOf(
            prefs.getInt(
                "font",
                defaultFont
            ).coerceIn(
                0,
                clockFonts.lastIndex
            )
        )
    }

    var styleIndex by remember {
        mutableIntStateOf(
            prefs.getInt(
                "style",
                0
            ).coerceIn(
                0,
                clockStyles.lastIndex
            )
        )
    }

    var colorIndex by remember {
        mutableIntStateOf(
            prefs.getInt(
                "color",
                defaultColor
            ).coerceIn(
                0,
                clockTones.lastIndex
            )
        )
    }

    var wallpaperIndex by remember {
        mutableIntStateOf(
            prefs.getInt(
                "wallpaper",
                a.theme.ordinal
            ).coerceIn(
                0,
                NmixThemeName.entries.lastIndex
            )
        )
    }

    var showHours by remember {
        mutableStateOf(
            prefs.getBoolean(
                "hours",
                true
            )
        )
    }

    var showMinutes by remember {
        mutableStateOf(
            prefs.getBoolean(
                "minutes",
                true
            )
        )
    }

    var showSeconds by remember {
        mutableStateOf(
            prefs.getBoolean(
                "seconds",
                true
            )
        )
    }

    var showDate by remember {
        mutableStateOf(
            prefs.getBoolean(
                "date",
                true
            )
        )
    }

    var customUriString by remember {
        mutableStateOf(
            prefs.getString(
                "custom_wallpaper",
                null
            )
        )
    }

    var clean by remember {
        mutableStateOf(false)
    }

    var popup by remember {
        mutableStateOf<String?>(null)
    }

    fun saveInt(
        key: String,
        value: Int
    ) {
        prefs.edit()
            .putInt(
                key,
                value
            )
            .apply()
    }

    fun saveBoolean(
        key: String,
        value: Boolean
    ) {
        prefs.edit()
            .putBoolean(
                key,
                value
            )
            .apply()
    }

    val customUri =
        customUriString?.let(
            Uri::parse
        )

    val picker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                customUriString =
                    uri.toString()

                prefs.edit()
                    .putString(
                        "custom_wallpaper",
                        uri.toString()
                    )
                    .apply()

                popup = null
            }
        }

    DisposableEffect(activity) {
        val window =
            activity?.window

        if (window != null) {
            WindowCompat
                .setDecorFitsSystemWindows(
                    window,
                    false
                )

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                hide(
                    WindowInsetsCompat.Type
                        .statusBars() or
                        WindowInsetsCompat.Type
                            .navigationBars()
                )

                systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose {
            activity?.requestedOrientation =
                ActivityInfo
                    .SCREEN_ORIENTATION_UNSPECIFIED

            if (window != null) {
                WindowCompat
                    .setDecorFitsSystemWindows(
                        window,
                        true
                    )

                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).show(
                    WindowInsetsCompat.Type
                        .statusBars() or
                        WindowInsetsCompat.Type
                            .navigationBars()
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

    val tone =
        clockTones[colorIndex]

    val wall =
        NmixThemeName.entries[
            wallpaperIndex
        ].palette()

    val parts =
        parseClockTime(time)

    val motion =
        rememberInfiniteTransition(
            label = "clockWallpaper"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                2900,
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
                3700,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallY"
    )

    val z by motion.animateFloat(
        -.85f,
        .85f,
        infiniteRepeatable(
            tween(
                4700,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "wallZ"
    )

    val pulse by motion.animateFloat(
        .91f,
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

    val base =
        if (a.darkMode) {
            Color(0xFF090C0B)
        } else {
            Color(0xFFD5DDD9)
        }

    val controlSurface =
        if (a.darkMode) {
            Color(0xFF101513)
                .copy(alpha = .90f)
        } else {
            Color(0xFFF7FAF8)
                .copy(alpha = .84f)
        }

    val popupSurface =
        if (a.darkMode) {
            Color(0xFF111614)
        } else {
            Color(0xFFF4F7F5)
        }

    val border =
        if (a.darkMode) {
            Color.White.copy(
                alpha = .15f
            )
        } else {
            Color(0xFF26312C)
                .copy(alpha = .17f)
        }

    val uiText =
        if (a.darkMode) {
            Color(0xFFF1F5F3)
        } else {
            Color(0xFF18201C)
        }

    val sideText =
        if (a.darkMode) {
            Color(0xFFD5DCD9)
        } else {
            Color(0xFF414B46)
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
                    clean ->
                        clean = false

                    popup != null ->
                        popup = null
                }
            }
    ) {
        if (customUri != null) {
            CustomWallpaper(
                customUri
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (a.darkMode) {
                            Color.Black.copy(
                                alpha = .12f
                            )
                        } else {
                            Color.White.copy(
                                alpha = .04f
                            )
                        }
                    )
            )
        } else {
            ClockGlow(
                color = wall.accent,
                alpha =
                    if (a.darkMode)
                        .50f
                    else
                        .48f,
                size = 620,
                modifier = Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .offset(
                        x = (-190).dp,
                        y = (-175).dp
                    )
                    .graphicsLayer {
                        translationX =
                            x * 275f

                        translationY =
                            y * 125f

                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            ClockGlow(
                color = wall.accentLight,
                alpha =
                    if (a.darkMode)
                        .36f
                    else
                        .42f,
                size = 570,
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .offset(
                        x = 180.dp,
                        y = 165.dp
                    )
                    .graphicsLayer {
                        translationX =
                            -x * 250f

                        translationY =
                            z * 145f
                    }
            )

            ClockGlow(
                color = wall.accent,
                alpha =
                    if (a.darkMode)
                        .21f
                    else
                        .29f,
                size = 430,
                modifier = Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer {
                        translationX =
                            z * 215f

                        translationY =
                            -y * 130f
                    }
            )
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(
                    Alignment.TopStart
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(
                    tween(320)
                ) +
                    slideInVertically(
                        initialOffsetY = {
                            -it / 3
                        },
                        animationSpec = tween(
                            390,
                            easing =
                                EaseOutCubic
                        )
                    ),
            exit =
                fadeOut(
                    tween(210)
                )
        ) {
            ClockBrand(
                modifier =
                    Modifier.padding(
                        start = 22.dp,
                        top = 15.dp
                    ),
                color = uiText
            )
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(
                    Alignment.TopEnd
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(
                    tween(330)
                ),
            exit =
                fadeOut(
                    tween(200)
                )
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 12.dp,
                        end = 14.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    ClockCarousel(
                        title = "FONT",
                        options = clockFonts,
                        index = fontIndex,
                        width = 158,
                        surface =
                            controlSurface,
                        border = border,
                        centerColor =
                            uiText,
                        sideColor =
                            sideText,
                        labelColor =
                            uiText,
                        headingFont =
                            a.fontFamily,
                        optionFont =
                            font,
                        colorMode =
                            false
                    ) {
                        fontIndex = it
                        saveInt(
                            "font",
                            it
                        )
                    }

                    ClockCarousel(
                        title = "STYLE",
                        options = clockStyles,
                        index = styleIndex,
                        width = 158,
                        surface =
                            controlSurface,
                        border = border,
                        centerColor =
                            uiText,
                        sideColor =
                            sideText,
                        labelColor =
                            uiText,
                        headingFont =
                            a.fontFamily,
                        optionFont =
                            font,
                        colorMode =
                            false
                    ) {
                        styleIndex = it
                        saveInt(
                            "style",
                            it
                        )
                    }

                    ClockCarousel(
                        title = "COLOR",
                        options =
                            clockTones.map {
                                it.name
                            },
                        index = colorIndex,
                        width = 158,
                        surface =
                            controlSurface,
                        border = border,
                        centerColor =
                            uiText,
                        sideColor =
                            sideText,
                        labelColor =
                            uiText,
                        headingFont =
                            a.fontFamily,
                        optionFont =
                            font,
                        colorMode =
                            true
                    ) {
                        colorIndex = it

                        saveInt(
                            "color",
                            it
                        )
                    }
                }
            } else {
                Column(
                    Modifier.padding(
                        top = 13.dp,
                        end = 10.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {
                    ClockCarousel(
                        "FONT",
                        clockFonts,
                        fontIndex,
                        174,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        uiText,
                        a.fontFamily,
                        font,
                        false
                    ) {
                        fontIndex = it
                        saveInt(
                            "font",
                            it
                        )
                    }

                    ClockCarousel(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        174,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        uiText,
                        a.fontFamily,
                        font,
                        false
                    ) {
                        styleIndex = it

                        saveInt(
                            "style",
                            it
                        )
                    }

                    ClockCarousel(
                        "COLOR",
                        clockTones.map {
                            it.name
                        },
                        colorIndex,
                        174,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        uiText,
                        a.fontFamily,
                        font,
                        true
                    ) {
                        colorIndex = it

                        saveInt(
                            "color",
                            it
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .align(
                    Alignment.Center
                )
                .fillMaxWidth(
                    if (landscape)
                        .88f
                    else
                        .94f
                )
                .height(
                    if (landscape)
                        330.dp
                    else
                        350.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            AnimatedContent(
                targetState =
                    styleIndex,
                transitionSpec = {
                    (
                        fadeIn(
                            tween(
                                390,
                                easing =
                                    EaseOutCubic
                            )
                        ) +
                            scaleIn(
                                initialScale =
                                    .97f,
                                animationSpec =
                                    tween(
                                        390,
                                        easing =
                                            EaseOutCubic
                                    )
                            )
                        ) togetherWith (
                        fadeOut(
                            tween(230)
                        ) +
                            scaleOut(
                                targetScale =
                                    1.015f,
                                animationSpec =
                                    tween(270)
                            )
                        )
                },
                label = "clockStyle"
            ) { style ->
                ClockFace(
                    style = style,
                    parts = parts,
                    date = date,
                    tone = tone,
                    font = font,
                    landscape =
                        landscape,
                    showHours =
                        showHours,
                    showMinutes =
                        showMinutes,
                    showSeconds =
                        showSeconds,
                    showDate =
                        showDate,
                    dark =
                        a.darkMode
                )
            }

            AnimatedVisibility(
                visible = clean,
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    ),
                enter =
                    fadeIn(
                        tween(390)
                    ) +
                        slideInVertically(
                            initialOffsetY = {
                                it / 2
                            },
                            animationSpec = tween(
                                440,
                                easing =
                                    EaseOutCubic
                            )
                        ),
                exit =
                    fadeOut(
                        tween(210)
                    )
            ) {
                ClockBrand(
                    modifier =
                        Modifier.padding(
                            bottom = 8.dp
                        ),
                    centered = true,
                    color = uiText
                )
            }
        }

        /*
         * Popups sit in the same overlay position.
         * No AnimatedContent size-box is used.
         */
        Box(
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
                .padding(
                    bottom = 71.dp
                ),
            contentAlignment =
                Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible =
                    !clean &&
                    popup == "wallpaper",
                enter =
                    fadeIn(
                        tween(
                            260,
                            easing =
                                EaseOutCubic
                        )
                    ) +
                        scaleIn(
                            initialScale = .97f,
                            animationSpec = tween(
                                280,
                                easing =
                                    EaseOutCubic
                            )
                        ),
                exit =
                    fadeOut(
                        tween(190)
                    ) +
                        scaleOut(
                            targetScale = .985f,
                            animationSpec =
                                tween(210)
                        )
            ) {
                WallpaperBar(
                    selected =
                        wallpaperIndex,
                    customSelected =
                        customUri != null,
                    surface =
                        popupSurface,
                    accent =
                        wall.accent,
                    onSelect = {
                        wallpaperIndex = it
                        customUriString =
                            null

                        saveInt(
                            "wallpaper",
                            it
                        )

                        prefs.edit()
                            .remove(
                                "custom_wallpaper"
                            )
                            .apply()
                    },
                    onCustom = {
                        picker.launch(
                            arrayOf(
                                "image/*"
                            )
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible =
                    !clean &&
                    popup == "display",
                enter =
                    fadeIn(
                        tween(
                            260,
                            easing =
                                EaseOutCubic
                        )
                    ) +
                        scaleIn(
                            initialScale = .97f,
                            animationSpec = tween(
                                280,
                                easing =
                                    EaseOutCubic
                            )
                        ),
                exit =
                    fadeOut(
                        tween(190)
                    ) +
                        scaleOut(
                            targetScale = .985f,
                            animationSpec =
                                tween(210)
                        )
            ) {
                DisplayBar(
                    surface =
                        popupSurface,
                    accent =
                        wall.accent,
                    textColor =
                        uiText,
                    font =
                        font,
                    hours =
                        showHours,
                    minutes =
                        showMinutes,
                    seconds =
                        showSeconds,
                    date =
                        showDate,
                    onHours = {
                        showHours =
                            !showHours

                        saveBoolean(
                            "hours",
                            showHours
                        )
                    },
                    onMinutes = {
                        showMinutes =
                            !showMinutes

                        saveBoolean(
                            "minutes",
                            showMinutes
                        )
                    },
                    onSeconds = {
                        showSeconds =
                            !showSeconds

                        saveBoolean(
                            "seconds",
                            showSeconds
                        )
                    },
                    onDate = {
                        showDate =
                            !showDate

                        saveBoolean(
                            "date",
                            showDate
                        )
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = !clean,
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter =
                fadeIn(
                    tween(320)
                ),
            exit =
                fadeOut(
                    tween(210)
                )
        ) {
            Row(
                Modifier.padding(
                    start = 11.dp,
                    end = 11.dp,
                    bottom = 15.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                ClockAction(
                    "Wallpaper",
                    NmixIcon.WALLPAPER,
                    font,
                    controlSurface,
                    border,
                    uiText
                ) {
                    popup =
                        if (
                            popup ==
                            "wallpaper"
                        ) {
                            null
                        } else {
                            "wallpaper"
                        }
                }

                ClockAction(
                    "Rotate",
                    NmixIcon.ROTATE,
                    font,
                    controlSurface,
                    border,
                    uiText
                ) {
                    /*
                     * Lock explicitly to the opposite
                     * current configuration.
                     */
                    activity
                        ?.requestedOrientation =
                        if (landscape) {
                            ActivityInfo
                                .SCREEN_ORIENTATION_PORTRAIT
                        } else {
                            ActivityInfo
                                .SCREEN_ORIENTATION_LANDSCAPE
                        }
                }

                ClockAction(
                    "Display",
                    NmixIcon.CLOCK,
                    font,
                    controlSurface,
                    border,
                    uiText
                ) {
                    popup =
                        if (
                            popup ==
                            "display"
                        ) {
                            null
                        } else {
                            "display"
                        }
                }

                ClockAction(
                    "Clean",
                    NmixIcon.FULLSCREEN,
                    font,
                    controlSurface,
                    border,
                    uiText
                ) {
                    popup = null
                    clean = true
                }

                ClockAction(
                    "Exit",
                    NmixIcon.CLOSE,
                    font,
                    controlSurface,
                    border,
                    uiText,
                    red = true
                ) {
                    activity
                        ?.requestedOrientation =
                        ActivityInfo
                            .SCREEN_ORIENTATION_UNSPECIFIED

                    onExit()
                }
            }
        }
    }
}

@Composable
private fun ClockCarousel(
    title: String,
    options: List<String>,
    index: Int,
    width: Int,
    surface: Color,
    border: Color,
    centerColor: Color,
    sideColor: Color,
    labelColor: Color,
    headingFont: FontFamily,
    optionFont: FontFamily,
    colorMode: Boolean,
    onIndex: (Int) -> Unit
) {
    val density =
        LocalDensity.current

    val scope =
        rememberCoroutineScope()

    /*
     * Exactly equal slots.
     * Text width cannot push neighbours around.
     */
    val slot =
        with(density) {
            (width.dp / 3f)
                .toPx()
        }

    val drag =
        remember {
            Animatable(0f)
        }

    fun wrap(
        value: Int
    ): Int {
        return (
            (value % options.size) +
                options.size
            ) % options.size
    }

    val shape =
        RoundedCornerShape(50)

    Column(
        Modifier
            .width(width.dp)
            .height(48.dp)
            .clip(shape)
            .background(surface)
            .border(
                .5.dp,
                border,
                shape
            )
            .pointerInput(
                index,
                options.size
            ) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        scope.launch {
                            drag.stop()
                        }
                    },
                    onHorizontalDrag = {
                        change,
                        amount ->

                        change.consume()

                        val target =
                            (
                                drag.value +
                                    amount
                                )
                                .coerceIn(
                                    -slot,
                                    slot
                                )

                        scope.launch {
                            drag.snapTo(
                                target
                            )
                        }
                    },
                    onDragEnd = {
                        val move =
                            when {
                                drag.value <
                                    -slot *
                                    .28f -> 1

                                drag.value >
                                    slot *
                                    .28f -> -1

                                else -> 0
                            }

                        scope.launch {
                            if (move == 0) {
                                drag.animateTo(
                                    0f,
                                    tween(
                                        180,
                                        easing =
                                            EaseOutCubic
                                    )
                                )
                            } else {
                                val target =
                                    if (move > 0) {
                                        -slot
                                    } else {
                                        slot
                                    }

                                /*
                                 * Finish into the exact
                                 * centre with a tween,
                                 * never a spring.
                                 */
                                drag.animateTo(
                                    target,
                                    tween(
                                        180,
                                        easing =
                                            EaseOutCubic
                                    )
                                )

                                onIndex(
                                    wrap(
                                        index +
                                            move
                                    )
                                )

                                drag.snapTo(
                                    0f
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            drag.animateTo(
                                0f,
                                tween(
                                    170,
                                    easing =
                                        EaseOutCubic
                                )
                            )
                        }
                    }
                )
            },
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = labelColor,
            fontSize = 7.5.sp,
            fontWeight =
                FontWeight.Bold,
            letterSpacing = .8.sp,
            fontFamily =
                headingFont,
            modifier =
                Modifier.padding(
                    top = 3.dp
                )
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment =
                Alignment.Center
        ) {
            val progress =
                if (slot == 0f) {
                    0f
                } else {
                    drag.value /
                        slot
                }

            for (
                offset in
                -2..2
            ) {
                val position =
                    offset +
                        progress

                if (
                    abs(position) <
                    1.65f
                ) {
                    val center =
                        1f -
                            abs(position)
                                .coerceIn(
                                    0f,
                                    1f
                                )

                    val scale =
                        .86f +
                            center *
                            .37f

                    val alpha =
                        .47f +
                            center *
                            .53f

                    val itemColor =
                        if (
                            colorMode &&
                            center >
                            .55f
                        ) {
                            clockTones[
                                wrap(
                                    index +
                                        offset
                                )
                            ].accent
                        } else if (
                            center >
                            .55f
                        ) {
                            centerColor
                        } else {
                            sideColor.copy(
                                alpha = .68f
                            )
                        }

                    Box(
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .width(
                                (width / 3).dp
                            )
                            .graphicsLayer {
                                translationX =
                                    position *
                                        slot

                                scaleX =
                                    scale

                                scaleY =
                                    scale

                                this.alpha =
                                    alpha
                            },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            options[
                                wrap(
                                    index +
                                        offset
                                )
                            ],
                            color =
                                itemColor,
                            fontSize =
                                10.5.sp,
                            fontWeight =
                                if (
                                    center >
                                    .55f
                                ) {
                                    FontWeight
                                        .Bold
                                } else {
                                    FontWeight
                                        .Normal
                                },
                            fontFamily =
                                optionFont,
                            textAlign =
                                TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperBar(
    selected: Int,
    customSelected: Boolean,
    surface: Color,
    accent: Color,
    onSelect: (Int) -> Unit,
    onCustom: () -> Unit
) {
    val colors =
        NmixThemeName.entries.map {
            it.palette().accent
        }

    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(62.dp)
            .clip(shape)
            .background(surface)
            .padding(
                horizontal = 15.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                17.dp
            )
    ) {
        colors.forEachIndexed {
            index,
            color ->

            RingChoice(
                selected =
                    !customSelected &&
                        index ==
                        selected,
                ringColor =
                    accent
            ) {
                Box(
                    Modifier
                        .size(27.dp)
                        .clip(
                            CircleShape
                        )
                        .background(
                            color
                        )
                )
            }

            Spacer(
                Modifier.width(
                    0.dp
                )
            )
        }

        RingChoice(
            selected =
                customSelected,
            ringColor =
                accent,
            onClick =
                onCustom
        ) {
            Box(
                Modifier
                    .size(27.dp)
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color(0xFF66706B)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                NmixIcon(
                    NmixIcon.WALLPAPER,
                    Modifier.size(
                        13.dp
                    ),
                    Color.White
                )
            }
        }
    }
}

@Composable
private fun RingChoice(
    selected: Boolean,
    ringColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    /*
     * Screenshot-style selection:
     *
     * outer ring
     * transparent gap
     * inner circle
     *
     * Inner circle never changes size.
     */
    Box(
        Modifier
            .size(39.dp)
            .clickable(
                interactionSource =
                    remember {
                        MutableInteractionSource()
                    },
                indication = null,
                onClick = onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        if (selected) {
            Box(
                Modifier
                    .size(37.dp)
                    .border(
                        2.dp,
                        ringColor,
                        CircleShape
                    )
            )
        }

        content()
    }
}

@Composable
private fun DisplayBar(
    surface: Color,
    accent: Color,
    textColor: Color,
    font: FontFamily,
    hours: Boolean,
    minutes: Boolean,
    seconds: Boolean,
    date: Boolean,
    onHours: () -> Unit,
    onMinutes: () -> Unit,
    onSeconds: () -> Unit,
    onDate: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(62.dp)
            .clip(shape)
            .background(surface)
            .padding(
                horizontal = 15.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                17.dp
            )
    ) {
        DisplayRing(
            "H",
            hours,
            accent,
            textColor,
            font,
            onHours
        )

        DisplayRing(
            "M",
            minutes,
            accent,
            textColor,
            font,
            onMinutes
        )

        DisplayRing(
            "S",
            seconds,
            accent,
            textColor,
            font,
            onSeconds
        )

        DisplayRing(
            "D",
            date,
            accent,
            textColor,
            font,
            onDate
        )
    }
}

@Composable
private fun DisplayRing(
    text: String,
    selected: Boolean,
    accent: Color,
    textColor: Color,
    font: FontFamily,
    onClick: () -> Unit
) {
    RingChoice(
        selected =
            selected,
        ringColor =
            accent,
        onClick =
            onClick
    ) {
        Box(
            Modifier
                .size(27.dp)
                .clip(
                    CircleShape
                )
                .background(
                    textColor.copy(
                        alpha =
                            if (selected)
                                .15f
                            else
                                .07f
                    )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text,
                color =
                    if (selected) {
                        textColor
                    } else {
                        textColor.copy(
                            alpha = .40f
                        )
                    },
                fontSize = 9.sp,
                fontWeight =
                    FontWeight.Bold,
                fontFamily = font
            )
        }
    }
}

@Composable
private fun ClockFace(
    style: Int,
    parts: ClockParts,
    date: String,
    tone: ClockTone,
    font: FontFamily,
    landscape: Boolean,
    showHours: Boolean,
    showMinutes: Boolean,
    showSeconds: Boolean,
    showDate: Boolean,
    dark: Boolean
) {
    /*
     * AM/PM is intentionally always independent.
     * S only affects the seconds component.
     */
    @Composable
    fun MainTime(
        fontSize:
            androidx.compose.ui.unit.TextUnit
    ) {
        val numeric =
            buildList {
                if (showHours) {
                    add(parts.hour)
                }

                if (showMinutes) {
                    add(parts.minute)
                }

                if (showSeconds) {
                    add(parts.second)
                }
            }.joinToString(":")

        Row(
            verticalAlignment =
                Alignment.Bottom,
            horizontalArrangement =
                Arrangement.Center
        ) {
            if (
                numeric.isNotEmpty()
            ) {
                Text(
                    numeric,
                    color =
                        tone.main,
                    fontSize =
                        fontSize,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily =
                        font,
                    maxLines = 1
                )
            }

            if (
                parts.period
                    .isNotEmpty()
            ) {
                Spacer(
                    Modifier.width(
                        8.dp
                    )
                )

                Text(
                    parts.period,
                    color =
                        tone.accent,
                    fontSize =
                        if (
                            landscape
                        ) {
                            17.sp
                        } else {
                            14.sp
                        },
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily =
                        font,
                    maxLines = 1,
                    modifier =
                        Modifier.padding(
                            bottom = 8.dp
                        )
                )
            }
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        when (style) {
            1 -> {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    MainTime(
                        if (landscape)
                            78.sp
                        else
                            57.sp
                    )

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                tone.accent,
                            fontSize =
                                12.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }

            2 -> {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            20.dp
                        )
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.End
                    ) {
                        if (showHours) {
                            Text(
                                parts.hour,
                                color =
                                    tone.main,
                                fontSize =
                                    if (
                                        landscape
                                    )
                                        70.sp
                                    else
                                        55.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                fontFamily =
                                    font
                            )
                        }

                        if (showMinutes) {
                            Text(
                                parts.minute,
                                color =
                                    tone.accent,
                                fontSize =
                                    if (
                                        landscape
                                    )
                                        70.sp
                                    else
                                        55.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                fontFamily =
                                    font
                            )
                        }
                    }

                    Column {
                        if (showSeconds) {
                            Text(
                                parts.second,
                                color =
                                    tone.main,
                                fontSize =
                                    34.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                fontFamily =
                                    font
                            )
                        }

                        Text(
                            parts.period,
                            color =
                                tone.accent,
                            fontSize =
                                12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily =
                                font
                        )

                        if (showDate) {
                            Spacer(
                                Modifier.height(
                                    8.dp
                                )
                            )

                            Text(
                                date,
                                color =
                                    tone.main
                                        .copy(
                                            alpha =
                                                .68f
                                        ),
                                fontSize =
                                    10.sp,
                                fontFamily =
                                    font
                            )
                        }
                    }
                }
            }

            3 -> {
                /*
                 * Focus replaces Cards.
                 * Large centred time with quiet
                 * side markers, no card grid.
                 */
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "FOCUS",
                        color =
                            tone.accent,
                        fontSize =
                            9.sp,
                        fontWeight =
                            FontWeight.Bold,
                        letterSpacing =
                            3.5.sp,
                        fontFamily =
                            font
                    )

                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )

                    MainTime(
                        if (landscape)
                            82.sp
                        else
                            59.sp
                    )

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                9.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                tone.main
                                    .copy(
                                        alpha =
                                            .62f
                                    ),
                            fontSize =
                                10.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }

            4 -> {
                /*
                 * Larger Orbit so the whole
                 * time + AM/PM fits one line.
                 */
                Box(
                    Modifier.size(
                        if (landscape)
                            310.dp
                        else
                            272.dp
                    ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(
                                1.2.dp,
                                tone.accent
                                    .copy(
                                        alpha =
                                            .38f
                                    ),
                                CircleShape
                            )
                    )

                    Box(
                        Modifier
                            .size(
                                if (
                                    landscape
                                )
                                    248.dp
                                else
                                    218.dp
                            )
                            .border(
                                .7.dp,
                                tone.accent
                                    .copy(
                                        alpha =
                                            .18f
                                    ),
                                CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        MainTime(
                            if (
                                landscape
                            )
                                47.sp
                            else
                                36.sp
                        )

                        if (showDate) {
                            Spacer(
                                Modifier.height(
                                    7.dp
                                )
                            )

                            Text(
                                date,
                                color =
                                    tone.accent,
                                fontSize =
                                    8.sp,
                                fontFamily =
                                    font
                            )
                        }
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
                        color =
                            tone.accent,
                        fontSize =
                            10.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            font
                    )

                    Spacer(
                        Modifier.height(
                            7.dp
                        )
                    )

                    MainTime(
                        if (landscape)
                            67.sp
                        else
                            47.sp
                    )

                    if (showDate) {
                        Text(
                            "> DATE  $date",
                            color =
                                tone.accent
                                    .copy(
                                        alpha =
                                            .82f
                                    ),
                            fontSize =
                                10.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }

            6 -> {
                /*
                 * Wider capsule; no PM crop/wrap.
                 */
                val shape =
                    RoundedCornerShape(
                        50
                    )

                Column(
                    Modifier
                        .fillMaxWidth(
                            if (landscape)
                                .84f
                            else
                                .94f
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(
                                if (
                                    landscape
                                )
                                    112.dp
                                else
                                    96.dp
                            )
                            .clip(shape)
                            .background(
                                if (dark) {
                                    Color.White
                                        .copy(
                                            alpha =
                                                .08f
                                        )
                                } else {
                                    Color.Black
                                        .copy(
                                            alpha =
                                                .055f
                                        )
                                }
                            )
                            .border(
                                .7.dp,
                                tone.accent
                                    .copy(
                                        alpha =
                                            .38f
                                    ),
                                shape
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        MainTime(
                            if (
                                landscape
                            )
                                63.sp
                            else
                                44.sp
                        )
                    }

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                tone.accent,
                            fontSize =
                                11.sp,
                            fontFamily =
                                font
                        )
                    }
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
                            tone.accent,
                        fontSize =
                            9.sp,
                        letterSpacing =
                            4.sp,
                        fontFamily =
                            font
                    )

                    Spacer(
                        Modifier.height(
                            3.dp
                        )
                    )

                    MainTime(
                        if (landscape)
                            80.sp
                        else
                            57.sp
                    )

                    Box(
                        Modifier
                            .padding(
                                top = 9.dp
                            )
                            .width(
                                if (
                                    landscape
                                )
                                    330.dp
                                else
                                    250.dp
                            )
                            .height(
                                1.dp
                            )
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

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                9.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                tone.main
                                    .copy(
                                        alpha =
                                            .68f
                                    ),
                            fontSize =
                                11.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }

            else -> {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "NMIX • LOCAL TIME",
                        color =
                            tone.accent,
                        fontSize =
                            10.sp,
                        letterSpacing =
                            1.9.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            font
                    )

                    Spacer(
                        Modifier.height(
                            12.dp
                        )
                    )

                    MainTime(
                        if (landscape)
                            78.sp
                        else
                            57.sp
                    )

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                tone.main
                                    .copy(
                                        alpha =
                                            .70f
                                    ),
                            fontSize =
                                12.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }
        }
    }
}

private fun parseClockTime(
    time: String
): ClockParts {
    val period =
        when {
            time.contains("AM") ->
                "AM"

            time.contains("PM") ->
                "PM"

            else ->
                ""
        }

    val raw =
        time
            .removeSuffix(" AM")
            .removeSuffix(" PM")

    val pieces =
        raw.split(":")

    return ClockParts(
        hour =
            pieces.getOrElse(0) {
                "00"
            },
        minute =
            pieces.getOrElse(1) {
                "00"
            },
        second =
            pieces.getOrElse(2) {
                "00"
            },
        period =
            period
    )
}

@Composable
private fun ClockBrand(
    modifier: Modifier =
        Modifier,
    centered: Boolean =
        false,
    color: Color
) {
    val a =
        LocalNmixAppearance.current

    Column(
        modifier,
        horizontalAlignment =
            if (centered) {
                Alignment
                    .CenterHorizontally
            } else {
                Alignment.Start
            }
    ) {
        Text(
            "EVERYTHING WITH NUMBERS",
            color =
                color.copy(
                    alpha = .58f
                ),
            fontSize = 7.sp,
            letterSpacing =
                1.5.sp,
            fontFamily =
                a.fontFamily
        )

        Text(
            "NMIX",
            color = color,
            fontSize = 24.sp,
            fontWeight =
                FontWeight.Bold,
            letterSpacing =
                2.sp,
            fontFamily =
                NmixLogoFont
        )
    }
}

@Composable
private fun ClockAction(
    text: String,
    icon: NmixIcon,
    font: FontFamily,
    surface: Color,
    border: Color,
    textColor: Color,
    red: Boolean =
        false,
    onClick: () -> Unit
) {
    val foreground =
        if (red) {
            Color(0xFFFF8585)
        } else {
            textColor
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
                        .copy(
                            alpha =
                                .20f
                        )
                } else {
                    surface
                }
            )
            .border(
                .6.dp,
                if (red) {
                    foreground.copy(
                        alpha =
                            .50f
                    )
                } else {
                    border
                },
                shape
            )
            .clickable(
                interactionSource =
                    remember {
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
            Arrangement.spacedBy(
                7.dp
            )
    ) {
        NmixIcon(
            icon,
            Modifier.size(17.dp),
            foreground
        )

        Text(
            text,
            color =
                foreground,
            fontSize = 9.sp,
            fontWeight =
                FontWeight.SemiBold,
            fontFamily =
                font
        )
    }
}

@Composable
private fun ClockGlow(
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
                    colorStops =
                        arrayOf(
                            0f to
                                color.copy(
                                    alpha =
                                        alpha
                                ),

                            .27f to
                                color.copy(
                                    alpha =
                                        alpha *
                                            .74f
                                ),

                            .54f to
                                color.copy(
                                    alpha =
                                        alpha *
                                            .34f
                                ),

                            .79f to
                                color.copy(
                                    alpha =
                                        alpha *
                                            .08f
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
private fun CustomWallpaper(
    uri: Uri
) {
    val context =
        LocalContext.current

    var bitmap by
        remember(uri) {
            mutableStateOf<
                androidx.compose.ui.graphics.ImageBitmap?
                >(null)
        }

    LaunchedEffect(uri) {
        bitmap =
            withContext(
                Dispatchers.IO
            ) {
                try {
                    context
                        .contentResolver
                        .openInputStream(
                            uri
                        )
                        ?.use {
                            BitmapFactory
                                .decodeStream(
                                    it
                                )
                                ?.asImageBitmap()
                        }
                } catch (
                    _: Exception
                ) {
                    null
                }
            }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )
    }
}
