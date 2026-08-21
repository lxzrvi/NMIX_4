package com.lxzrvi.nmix

import android.content.Context
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

private const val FULL_CLOCK_PREFS =
    "nmix_fullscreen_clock"

private data class FullClockTone(
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

private val fullClockTones = listOf(
    FullClockTone(
        "White",
        Color(0xFFF5F7F6),
        Color(0xFFFFFFFF)
    ),
    FullClockTone(
        "Black",
        Color(0xFF111412),
        Color(0xFF202522)
    ),
    FullClockTone(
        "Ice",
        Color(0xFFF1FAFF),
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
    val context = LocalContext.current
    val a = LocalNmixAppearance.current
    val activity = LocalActivity.current
    val configuration = LocalConfiguration.current

    val prefs = remember(context) {
        context.getSharedPreferences(
            FULL_CLOCK_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val landscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

    val defaultFont =
        when (a.font) {
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
                fullClockFonts.lastIndex
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
                fullClockStyles.lastIndex
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
                fullClockTones.lastIndex
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

    var showPeriod by remember {
        mutableStateOf(
            prefs.getBoolean(
                "period",
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

    var wallpaperOpen by remember {
        mutableStateOf(false)
    }

    var displayOpen by remember {
        mutableStateOf(false)
    }

    fun saveInt(
        key: String,
        value: Int
    ) {
        prefs.edit()
            .putInt(key, value)
            .apply()
    }

    fun saveBool(
        key: String,
        value: Boolean
    ) {
        prefs.edit()
            .putBoolean(key, value)
            .apply()
    }

    val customUri =
        customUriString?.let(Uri::parse)

    val picker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                try {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            android.content.Intent
                                .FLAG_GRANT_READ_URI_PERMISSION
                        )
                } catch (_: Exception) {
                }

                customUriString =
                    uri.toString()

                prefs.edit()
                    .putString(
                        "custom_wallpaper",
                        uri.toString()
                    )
                    .apply()

                wallpaperOpen = false
            }
        }

    DisposableEffect(activity) {
        val window =
            activity?.window

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
            activity?.requestedOrientation =
                ActivityInfo
                    .SCREEN_ORIENTATION_UNSPECIFIED

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
            wallpaperIndex
        ].palette()

    val parts =
        parseFullClockTime(time)

    val visibleTime =
        buildFullClockTime(
            parts = parts,
            hours = showHours,
            minutes = showMinutes,
            seconds = showSeconds,
            period = showPeriod
        )

    val darkSurface =
        Color(0xFF101412)

    val lightSurface =
        Color(0xFFFFFFFF)

    val controlSurface =
        if (a.darkMode) {
            darkSurface.copy(alpha = .82f)
        } else {
            lightSurface.copy(alpha = .88f)
        }

    val popupSurface =
        if (a.darkMode) {
            Color(0xFF121715)
        } else {
            Color(0xFFFDFEFD)
        }

    val controlBorder =
        if (a.darkMode) {
            Color.White.copy(alpha = .15f)
        } else {
            Color(0xFF59645F)
                .copy(alpha = .20f)
        }

    val textColor =
        if (a.darkMode) {
            Color(0xFFF3F6F5)
        } else {
            Color(0xFF1C211F)
        }

    val sideText =
        if (a.darkMode) {
            Color(0xFFD9DFDC)
        } else {
            Color(0xFF414A46)
        }

    val base =
        if (a.darkMode) {
            Color(0xFF080B0A)
        } else {
            Color(0xFFE8ECEA)
        }

    val motion =
        rememberInfiniteTransition(
            label = "wallMotion"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                3000,
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
                3900,
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
                2800,
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(base)
            .clickable(
                interactionSource =
                    remember {
                        MutableInteractionSource()
                    },
                indication = null
            ) {
                when {
                    clean ->
                        clean = false

                    wallpaperOpen ->
                        wallpaperOpen = false

                    displayOpen ->
                        displayOpen = false
                }
            }
    ) {
        if (customUri != null) {
            CustomClockWallpaper(
                uri = customUri
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (a.darkMode) {
                            Color.Black.copy(
                                alpha = .14f
                            )
                        } else {
                            Color.White.copy(
                                alpha = .08f
                            )
                        }
                    )
            )
        } else {
            ClockGlow(
                color = wall.accent,
                alpha =
                    if (a.darkMode) {
                        .37f
                    } else {
                        .26f
                    },
                size = 440,
                modifier = Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .offset(
                        x = (-120).dp,
                        y = (-125).dp
                    )
                    .graphicsLayer {
                        translationX =
                            x * 235f

                        translationY =
                            y * 105f

                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            ClockGlow(
                color = wall.accentLight,
                alpha =
                    if (a.darkMode) {
                        .28f
                    } else {
                        .34f
                    },
                size = 400,
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .offset(
                        x = 115.dp,
                        y = 105.dp
                    )
                    .graphicsLayer {
                        translationX =
                            -x * 210f

                        translationY =
                            z * 125f
                    }
            )

            ClockGlow(
                color = wall.accent,
                alpha =
                    if (a.darkMode) {
                        .17f
                    } else {
                        .20f
                    },
                size = 315,
                modifier = Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer {
                        translationX =
                            z * 175f

                        translationY =
                            -y * 105f
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
                ) +
                    slideOutVertically(
                        targetOffsetY = {
                            -it / 3
                        },
                        animationSpec =
                            tween(280)
                    )
        ) {
            FullscreenBrand(
                modifier =
                    Modifier.padding(
                        start = 22.dp,
                        top = 15.dp
                    ),
                textColor =
                    textColor
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
                    tween(340)
                ) +
                    slideInVertically(
                        initialOffsetY = {
                            -it / 4
                        },
                        animationSpec = tween(
                            400,
                            easing =
                                EaseOutCubic
                        )
                    ),
            exit =
                fadeOut(
                    tween(210)
                )
        ) {
            if (landscape) {
                Row(
                    Modifier.padding(
                        top = 13.dp,
                        end = 15.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    ClockCarousel(
                        "FONT",
                        fullClockFonts,
                        fontIndex,
                        tone.accent,
                        158,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
                    ) {
                        fontIndex = it
                        saveInt(
                            "font",
                            it
                        )
                    }

                    ClockCarousel(
                        "STYLE",
                        fullClockStyles,
                        styleIndex,
                        tone.accent,
                        158,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
                    ) {
                        styleIndex = it
                        saveInt(
                            "style",
                            it
                        )
                    }

                    ClockCarousel(
                        "COLOR",
                        fullClockTones.map {
                            it.name
                        },
                        colorIndex,
                        tone.accent,
                        158,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
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
                        top = 14.dp,
                        end = 10.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {
                    ClockCarousel(
                        "FONT",
                        fullClockFonts,
                        fontIndex,
                        tone.accent,
                        174,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
                    ) {
                        fontIndex = it
                        saveInt(
                            "font",
                            it
                        )
                    }

                    ClockCarousel(
                        "STYLE",
                        fullClockStyles,
                        styleIndex,
                        tone.accent,
                        174,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
                    ) {
                        styleIndex = it
                        saveInt(
                            "style",
                            it
                        )
                    }

                    ClockCarousel(
                        "COLOR",
                        fullClockTones.map {
                            it.name
                        },
                        colorIndex,
                        tone.accent,
                        174,
                        controlSurface,
                        controlBorder,
                        textColor,
                        sideText,
                        a.fontFamily,
                        selectedFont
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

        Column(
            Modifier
                .align(
                    Alignment.Center
                )
                .padding(
                    horizontal = 16.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState =
                    styleIndex,
                transitionSpec = {
                    (
                        fadeIn(
                            tween(
                                400,
                                easing =
                                    EaseOutCubic
                            )
                        ) +
                            scaleIn(
                                initialScale =
                                    .965f,
                                animationSpec =
                                    tween(
                                        400,
                                        easing =
                                            EaseOutCubic
                                    )
                            )
                        ) togetherWith (
                        fadeOut(
                            tween(240)
                        ) +
                            scaleOut(
                                targetScale =
                                    1.02f,
                                animationSpec =
                                    tween(280)
                            )
                        )
                },
                label = "clockStyle"
            ) { style ->
                ClockFace(
                    style = style,
                    time = visibleTime,
                    parts = parts,
                    date = date,
                    tone = tone,
                    font = selectedFont,
                    landscape =
                        landscape,
                    showHours =
                        showHours,
                    showMinutes =
                        showMinutes,
                    showSeconds =
                        showSeconds,
                    showPeriod =
                        showPeriod,
                    showDate =
                        showDate,
                    dark =
                        a.darkMode
                )
            }

            AnimatedVisibility(
                visible = clean,
                enter =
                    fadeIn(
                        tween(390)
                    ) +
                        slideInVertically(
                            initialOffsetY = {
                                it
                            },
                            animationSpec =
                                tween(
                                    470,
                                    easing =
                                        EaseOutCubic
                                )
                        ),
                exit =
                    fadeOut(
                        tween(220)
                    ) +
                        slideOutVertically(
                            targetOffsetY = {
                                it / 2
                            },
                            animationSpec =
                                tween(270)
                        )
            ) {
                FullscreenBrand(
                    modifier =
                        Modifier.padding(
                            top = 19.dp
                        ),
                    centered = true,
                    textColor =
                        textColor
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
                ) +
                    slideInVertically(
                        initialOffsetY = {
                            it / 2
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
                ) +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 2
                        },
                        animationSpec =
                            tween(280)
                    )
        ) {
            Column(
                Modifier.padding(
                    start = 11.dp,
                    end = 11.dp,
                    bottom = 15.dp
                ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible =
                        wallpaperOpen,
                    enter =
                        fadeIn(
                            tween(240)
                        ) +
                            slideInVertically(
                                initialOffsetY = {
                                    it / 4
                                },
                                animationSpec =
                                    tween(
                                        300,
                                        easing =
                                            EaseOutCubic
                                    )
                            ),
                    exit =
                        fadeOut(
                            tween(170)
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
                        font =
                            selectedFont,
                        onSelect = {
                            wallpaperIndex =
                                it

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
                                "image/*"
                            )
                        }
                    )
                }

                AnimatedVisibility(
                    visible =
                        displayOpen,
                    enter =
                        fadeIn(
                            tween(240)
                        ) +
                            slideInVertically(
                                initialOffsetY = {
                                    it / 4
                                },
                                animationSpec =
                                    tween(
                                        300,
                                        easing =
                                            EaseOutCubic
                                    )
                            ),
                    exit =
                        fadeOut(
                            tween(170)
                        )
                ) {
                    DisplayBar(
                        surface =
                            popupSurface,
                        textColor =
                            textColor,
                        accent =
                            tone.accent,
                        font =
                            selectedFont,
                        hours =
                            showHours,
                        minutes =
                            showMinutes,
                        seconds =
                            showSeconds,
                        period =
                            showPeriod,
                        date =
                            showDate,
                        onHours = {
                            showHours =
                                !showHours

                            saveBool(
                                "hours",
                                showHours
                            )
                        },
                        onMinutes = {
                            showMinutes =
                                !showMinutes

                            saveBool(
                                "minutes",
                                showMinutes
                            )
                        },
                        onSeconds = {
                            showSeconds =
                                !showSeconds

                            saveBool(
                                "seconds",
                                showSeconds
                            )
                        },
                        onPeriod = {
                            showPeriod =
                                !showPeriod

                            saveBool(
                                "period",
                                showPeriod
                            )
                        },
                        onDate = {
                            showDate =
                                !showDate

                            saveBool(
                                "date",
                                showDate
                            )
                        }
                    )
                }

                if (
                    wallpaperOpen ||
                    displayOpen
                ) {
                    Spacer(
                        Modifier.height(
                            10.dp
                        )
                    )
                }

                Row(
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
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        textColor
                    ) {
                        displayOpen = false
                        wallpaperOpen =
                            !wallpaperOpen
                    }

                    ClockAction(
                        "Rotate",
                        NmixIcon.ROTATE,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        textColor
                    ) {
                        activity
                            ?.requestedOrientation =
                            if (
                                landscape
                            ) {
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
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        textColor
                    ) {
                        wallpaperOpen = false
                        displayOpen =
                            !displayOpen
                    }

                    ClockAction(
                        "Clean",
                        NmixIcon.FULLSCREEN,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        textColor
                    ) {
                        wallpaperOpen = false
                        displayOpen = false
                        clean = true
                    }

                    ClockAction(
                        "Exit",
                        NmixIcon.CLOSE,
                        selectedFont,
                        controlSurface,
                        controlBorder,
                        textColor,
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
}

@Composable
private fun ClockCarousel(
    title: String,
    options: List<String>,
    index: Int,
    accent: Color,
    width: Int,
    surface: Color,
    border: Color,
    centerColor: Color,
    sideColor: Color,
    headingFont: FontFamily,
    optionFont: FontFamily,
    onIndex: (Int) -> Unit
) {
    val density =
        LocalDensity.current

    var drag by remember {
        mutableFloatStateOf(0f)
    }

    var dragging by remember {
        mutableStateOf(false)
    }

    val slot =
        with(density) {
            (width.dp * .27f)
                .toPx()
        }

    val shownDrag by
        animateFloatAsState(
            targetValue = drag,
            animationSpec =
                if (dragging) {
                    snap()
                } else {
                    tween(
                        210,
                        easing =
                            EaseOutCubic
                    )
                },
            label =
                "carouselSettle"
        )

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
                        dragging = true
                        drag = 0f
                    },
                    onHorizontalDrag = {
                        change,
                        amount ->

                        change.consume()

                        drag =
                            (drag + amount)
                                .coerceIn(
                                    -slot * .90f,
                                    slot * .90f
                                )
                    },
                    onDragEnd = {
                        val move =
                            when {
                                drag <
                                    -slot *
                                    .27f -> 1

                                drag >
                                    slot *
                                    .27f -> -1

                                else -> 0
                            }

                        dragging = false

                        if (move != 0) {
                            onIndex(
                                wrap(
                                    index +
                                        move
                                )
                            )
                        }

                        drag = 0f
                    },
                    onDragCancel = {
                        dragging = false
                        drag = 0f
                    }
                )
            }
            .padding(top = 3.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = accent,
            fontSize = 7.5.sp,
            fontWeight =
                FontWeight.Bold,
            letterSpacing = .8.sp,
            fontFamily =
                headingFont
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(shape)
        ) {
            val progress =
                if (slot == 0f) {
                    0f
                } else {
                    shownDrag / slot
                }

            for (
                offset in
                -2..2
            ) {
                val position =
                    offset + progress

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
                        .85f +
                            center *
                            .34f

                    val alpha =
                        .45f +
                            center *
                            .55f

                    Box(
                        Modifier
                            .align(
                                Alignment.Center
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
                                if (
                                    center >
                                    .62f
                                ) {
                                    centerColor
                                } else {
                                    sideColor
                                        .copy(
                                            alpha =
                                                .62f
                                        )
                                },
                            fontSize =
                                10.5.sp,
                            fontWeight =
                                if (
                                    center >
                                    .62f
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
    font: FontFamily,
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
            .height(58.dp)
            .clip(shape)
            .background(surface)
            .padding(
                horizontal = 13.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                11.dp
            )
    ) {
        colors.forEachIndexed {
            index,
            color ->

            ColorCircle(
                color = color,
                selected =
                    !customSelected &&
                        selected ==
                        index,
                selectedAccent =
                    accent
            ) {
                onSelect(index)
            }
        }

        GalleryCircle(
            selected =
                customSelected,
            accent =
                accent,
            onClick =
                onCustom
        )
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    selected: Boolean,
    selectedAccent: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(
                        2.dp,
                        selectedAccent,
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource =
                    remember {
                        MutableInteractionSource()
                    },
                indication = null,
                onClick = onClick
            )
    )
}

@Composable
private fun GalleryCircle(
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF727A76)
            )
            .then(
                if (selected) {
                    Modifier.border(
                        2.dp,
                        accent,
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
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
        NmixIcon(
            NmixIcon.WALLPAPER,
            Modifier.size(15.dp),
            Color.White
        )
    }
}

@Composable
private fun DisplayBar(
    surface: Color,
    textColor: Color,
    accent: Color,
    font: FontFamily,
    hours: Boolean,
    minutes: Boolean,
    seconds: Boolean,
    period: Boolean,
    date: Boolean,
    onHours: () -> Unit,
    onMinutes: () -> Unit,
    onSeconds: () -> Unit,
    onPeriod: () -> Unit,
    onDate: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(58.dp)
            .clip(shape)
            .background(surface)
            .padding(
                horizontal = 13.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                11.dp
            )
    ) {
        DisplayCircle(
            "H",
            hours,
            accent,
            textColor,
            font,
            onHours
        )

        DisplayCircle(
            "M",
            minutes,
            accent,
            textColor,
            font,
            onMinutes
        )

        DisplayCircle(
            "S",
            seconds,
            accent,
            textColor,
            font,
            onSeconds
        )

        DisplayCircle(
            "A/P",
            period,
            accent,
            textColor,
            font,
            onPeriod
        )

        DisplayCircle(
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
private fun DisplayCircle(
    text: String,
    enabled: Boolean,
    accent: Color,
    textColor: Color,
    font: FontFamily,
    onClick: () -> Unit
) {
    val bg =
        if (enabled) {
            accent.copy(
                alpha = .18f
            )
        } else {
            textColor.copy(
                alpha = .07f
            )
        }

    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                if (enabled)
                    1.3.dp
                else
                    .5.dp,
                if (enabled)
                    accent
                else
                    textColor.copy(
                        alpha = .14f
                    ),
                CircleShape
            )
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
        Text(
            text,
            color =
                if (enabled) {
                    accent
                } else {
                    textColor.copy(
                        alpha = .42f
                    )
                },
            fontSize =
                if (text == "A/P")
                    6.5.sp
                else
                    9.sp,
            fontWeight =
                FontWeight.Bold,
            fontFamily =
                font
        )
    }
}

@Composable
private fun ClockFace(
    style: Int,
    time: String,
    parts: ClockParts,
    date: String,
    tone: FullClockTone,
    font: FontFamily,
    landscape: Boolean,
    showHours: Boolean,
    showMinutes: Boolean,
    showSeconds: Boolean,
    showPeriod: Boolean,
    showDate: Boolean,
    dark: Boolean
) {
    val size =
        if (landscape) {
            72.sp
        } else {
            52.sp
        }

    val mainColor =
        tone.main

    when (style) {
        1 -> {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    time,
                    color = mainColor,
                    fontSize = size,
                    fontFamily = font,
                    fontWeight =
                        FontWeight.Normal
                )

                if (showDate) {
                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        date,
                        color =
                            tone.accent,
                        fontSize = 11.sp,
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
                    Arrangement.spacedBy(
                        17.dp
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
                                mainColor,
                            fontSize =
                                if (
                                    landscape
                                )
                                    64.sp
                                else
                                    50.sp,
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
                                    64.sp
                                else
                                    50.sp,
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
                                mainColor,
                            fontSize =
                                30.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily =
                                font
                        )
                    }

                    if (showPeriod) {
                        Text(
                            parts.period,
                            color =
                                tone.accent,
                            fontSize =
                                11.sp,
                            fontWeight =
                                FontWeight.Bold,
                            fontFamily =
                                font
                        )
                    }

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                7.dp
                            )
                        )

                        Text(
                            date,
                            color =
                                mainColor.copy(
                                    alpha =
                                        .62f
                                ),
                            fontSize =
                                9.sp,
                            fontFamily =
                                font
                        )
                    }
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
                            alpha = .32f
                        ),
                    fontSize = size,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily = font,
                    modifier =
                        Modifier.blur(
                            14.dp
                        )
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        time,
                        color =
                            mainColor,
                        fontSize =
                            size,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            font
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
                                tone.accent,
                            fontSize =
                                10.sp,
                            fontFamily =
                                font
                        )
                    }
                }
            }
        }

        4 -> {
            Box(
                Modifier.size(
                    if (landscape)
                        260.dp
                    else
                        225.dp
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
                                        .34f
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
                                205.dp
                            else
                                178.dp
                        )
                        .border(
                            .7.dp,
                            tone.accent
                                .copy(
                                    alpha =
                                        .17f
                                ),
                            CircleShape
                        )
                )

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        time,
                        color =
                            mainColor,
                        fontSize =
                            if (
                                landscape
                            )
                                45.sp
                            else
                                36.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            font
                    )

                    if (showDate) {
                        Spacer(
                            Modifier.height(
                                6.dp
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
            Column {
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
                    Modifier.height(5.dp)
                )

                Text(
                    "> $time",
                    color =
                        mainColor,
                    fontSize =
                        if (landscape)
                            62.sp
                        else
                            44.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily =
                        font
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
            val shape =
                RoundedCornerShape(
                    50
                )

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
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
                                            .045f
                                    )
                            }
                        )
                        .border(
                            .7.dp,
                            tone.accent
                                .copy(
                                    alpha =
                                        .35f
                                ),
                            shape
                        )
                        .padding(
                            horizontal =
                                28.dp,
                            vertical =
                                14.dp
                        )
                ) {
                    Text(
                        time,
                        color =
                            mainColor,
                        fontSize =
                            if (
                                landscape
                            )
                                60.sp
                            else
                                43.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            font
                    )
                }

                if (showDate) {
                    Spacer(
                        Modifier.height(
                            9.dp
                        )
                    )

                    Text(
                        date,
                        color =
                            tone.accent,
                        fontSize =
                            10.sp,
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

                Text(
                    time,
                    color =
                        mainColor,
                    fontSize =
                        if (landscape)
                            76.sp
                        else
                            55.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily =
                        font
                )

                Box(
                    Modifier
                        .width(
                            if (
                                landscape
                            )
                                300.dp
                            else
                                230.dp
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

                if (showDate) {
                    Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )

                    Text(
                        date,
                        color =
                            mainColor.copy(
                                alpha =
                                    .65f
                            ),
                        fontSize =
                            10.sp,
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

                Text(
                    time,
                    color =
                        mainColor,
                    fontSize =
                        if (landscape)
                            73.sp
                        else
                            53.sp,
                    fontWeight =
                        FontWeight.Bold,
                    fontFamily =
                        font
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
                            mainColor.copy(
                                alpha =
                                    .67f
                            ),
                        fontSize =
                            11.sp,
                        fontFamily =
                            font
                    )
                }
            }
        }
    }
}

private fun parseFullClockTime(
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

private fun buildFullClockTime(
    parts: ClockParts,
    hours: Boolean,
    minutes: Boolean,
    seconds: Boolean,
    period: Boolean
): String {
    val numeric =
        mutableListOf<String>()

    if (hours) {
        numeric.add(
            parts.hour
        )
    }

    if (minutes) {
        numeric.add(
            parts.minute
        )
    }

    if (seconds) {
        numeric.add(
            parts.second
        )
    }

    val main =
        numeric.joinToString(
            ":"
        )

    return when {
        main.isNotEmpty() &&
            period &&
            parts.period
                .isNotEmpty() -> {
            "$main ${parts.period}"
        }

        main.isNotEmpty() -> {
            main
        }

        period -> {
            parts.period
        }

        else -> {
            ""
        }
    }
}

@Composable
private fun FullscreenBrand(
    modifier: Modifier =
        Modifier,
    centered: Boolean = false,
    textColor: Color
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
                textColor.copy(
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
            color = textColor,
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
    red: Boolean = false,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(50)

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
private fun CustomClockWallpaper(
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
                Modifier
                    .fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )
    }
}
