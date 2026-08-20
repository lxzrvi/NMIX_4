package com.lxzrvi.nmix

import android.content.Context
import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private data class V2Palette(
    val accent: Color,
    val dark: Color,
    val light: Color
)

private fun palette(name: String) = when(name) {
    "blue" -> V2Palette(
        Color(0xFF348BB8),
        Color(0xFF143A50),
        Color(0xFF75C8EF)
    )
    "purple" -> V2Palette(
        Color(0xFF8A62C8),
        Color(0xFF33224D),
        Color(0xFFC2A1EF)
    )
    "orange" -> V2Palette(
        Color(0xFFD57D35),
        Color(0xFF563116),
        Color(0xFFEFAD73)
    )
    "rose" -> V2Palette(
        Color(0xFFC85878),
        Color(0xFF542338),
        Color(0xFFEF91AD)
    )
    else -> V2Palette(
        Color(0xFF319B79),
        Color(0xFF19493A),
        Color(0xFF69D6B2)
    )
}

private enum class V2Mode {
    IDLE,
    CALCULATOR,
    TIMER,
    CLOCK,
    STOPWATCH,
    COUNTER
}

@Composable
fun NativeMainPageV2(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "nmix_appearance",
            Context.MODE_PRIVATE
        )
    }

    var dark by remember {
        mutableStateOf(
            prefs.getBoolean("dark", false)
        )
    }

    var theme by remember {
        mutableStateOf(
            prefs.getString("theme", "green")
                ?: "green"
        )
    }

    val colors = palette(theme)

    val background =
        if(dark) Color(0xFF0D1110)
        else Color(0xFFE0E2E1)

    val surface =
        if(dark)
            Color.White.copy(alpha = .075f)
        else
            Color.White.copy(alpha = .62f)

    val surfaceStrong =
        if(dark) Color(0xFF202624)
        else Color(0xFFDEE1DF)

    val text =
        if(dark) Color(0xFFEDF4F1)
        else Color(0xFF202321)

    val muted =
        if(dark) Color(0xFFA4AFAA)
        else Color(0xFF66706C)

    var topOpen by remember {
        mutableStateOf(true)
    }

    var settingsOpen by remember {
        mutableStateOf(false)
    }

    var fullscreen by remember {
        mutableStateOf(false)
    }

    var openSection by remember {
        mutableStateOf<String?>(null)
    }

    var mode by remember {
        mutableStateOf(V2Mode.IDLE)
    }

    var display by remember {
        mutableStateOf("Ready")
    }

    var displayLabel by remember {
        mutableStateOf("NMIX LIVE")
    }

    var status by remember {
        mutableStateOf("Choose a tool below.")
    }

    // Calculator
    var first by remember {
        mutableStateOf("")
    }

    var second by remember {
        mutableStateOf("")
    }

    var operator by remember {
        mutableStateOf("")
    }

    var targetSecond by remember {
        mutableStateOf(false)
    }

    // Timer
    var timerSeconds by remember {
        mutableIntStateOf(0)
    }

    var timerRunning by remember {
        mutableStateOf(false)
    }

    // Stopwatch
    var stopwatchMs by remember {
        mutableLongStateOf(0L)
    }

    var stopwatchRunning by remember {
        mutableStateOf(false)
    }

    var stopwatchBase by remember {
        mutableLongStateOf(0L)
    }

    // Counter
    var counter by remember {
        mutableIntStateOf(0)
    }

    // Clock
    var now by remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    LaunchedEffect(Unit) {
        while(true) {
            now = System.currentTimeMillis()
            delay(200)
        }
    }

    LaunchedEffect(timerRunning) {
        while(timerRunning && timerSeconds > 0) {
            delay(1000)

            if(timerRunning) {
                timerSeconds =
                    (timerSeconds - 1)
                        .coerceAtLeast(0)

                if(timerSeconds == 0) {
                    timerRunning = false
                    status = "Time's up!"
                }
            }
        }
    }

    LaunchedEffect(stopwatchRunning) {
        if(stopwatchRunning) {
            stopwatchBase =
                SystemClock.elapsedRealtime() -
                    stopwatchMs

            while(stopwatchRunning) {
                stopwatchMs =
                    SystemClock.elapsedRealtime() -
                        stopwatchBase

                delay(30)
            }
        }
    }

    fun timerText(): String {
        return "%02d:%02d".format(
            timerSeconds / 60,
            timerSeconds % 60
        )
    }

    fun stopwatchText(): String {
        val total = stopwatchMs / 1000

        return "%02d:%02d.%02d".format(
            total / 60,
            total % 60,
            (stopwatchMs % 1000) / 10
        )
    }

    fun timeText(): String {
        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(Date(now))
    }

    fun dateText(): String {
        return SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        ).format(Date(now))
    }

    LaunchedEffect(
        mode,
        timerSeconds,
        stopwatchMs,
        now
    ) {
        when(mode) {
            V2Mode.TIMER -> {
                display = timerText()
                displayLabel = "TIMER"
            }

            V2Mode.CLOCK -> {
                display = timeText()
                displayLabel = "LIVE CLOCK"
            }

            V2Mode.STOPWATCH -> {
                display = stopwatchText()
                displayLabel = "STOPWATCH"
            }

            V2Mode.COUNTER -> {
                display = counter.toString()
                displayLabel = "COUNTER"
            }

            else -> Unit
        }
    }

    fun stopTimedTools() {
        timerRunning = false
        stopwatchRunning = false
    }

    fun selectSection(name: String) {
        topOpen = true
        settingsOpen = false

        openSection =
            if(openSection == name)
                null
            else
                name
    }

    fun formatNumber(n: Double): String {
        if(!n.isFinite()) return "Overflow"

        val integer = n.toLong()

        return if(
            integer.toDouble() == n
        ) {
            integer.toString()
        } else {
            String.format(
                Locale.US,
                "%.10f",
                n
            )
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    fun calculatorStatus() {
        status = when {
            first.isEmpty() ->
                "Enter your first number."

            operator.isEmpty() ->
                "Choose an operator."

            second.isEmpty() ->
                "Enter the second number."

            else ->
                "Ready — tap = or the large display."
        }
    }

    fun calculate() {
        val a = first.toDoubleOrNull()
        val b = second.toDoubleOrNull()

        if(a == null || b == null) {
            display = "Incomplete"
            status = "Enter both numbers first."
            return
        }

        val result = when(operator) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b

            "÷" -> {
                if(b == 0.0) {
                    display = "Error"
                    status =
                        "Division by zero is not allowed."
                    return
                }
                a / b
            }

            "%" -> {
                if(b == 0.0) {
                    display = "Error"
                    status =
                        "Remainder by zero is not allowed."
                    return
                }
                a % b
            }

            else -> {
                display = "No sign"
                status = "Choose an operator."
                return
            }
        }

        display = formatNumber(result)
        displayLabel = "RESULT"
        status = "Calculation complete."
    }

    fun calculatorKey(key: String) {
        stopTimedTools()
        mode = V2Mode.CALCULATOR

        when(key) {
            "+","−","×","÷","%" -> {
                if(first.isEmpty()) {
                    status =
                        "Enter the first number first."
                    return
                }

                operator = key
                targetSecond = true
                display = key
                displayLabel = "OPERATOR"
            }

            "=" -> calculate()

            "." -> {
                if(targetSecond) {
                    if(second.contains(".")) return

                    second +=
                        if(second.isEmpty())
                            "0."
                        else "."

                    display = second
                } else {
                    if(first.contains(".")) return

                    first +=
                        if(first.isEmpty())
                            "0."
                        else "."

                    display = first
                }

                displayLabel = "DECIMAL"
            }

            "±" -> {
                if(targetSecond) {
                    val n =
                        second.toDoubleOrNull()
                            ?: return

                    second = formatNumber(-n)
                    display = second
                } else {
                    val n =
                        first.toDoubleOrNull()
                            ?: return

                    first = formatNumber(-n)
                    display = first
                }

                displayLabel = "SIGN CHANGED"
            }

            "⌫" -> {
                if(targetSecond) {
                    if(second.isNotEmpty()) {
                        second =
                            second.dropLast(1)

                        display =
                            second.ifEmpty { "0" }
                    } else {
                        operator = ""
                        targetSecond = false
                    }
                } else {
                    first = first.dropLast(1)

                    display =
                        first.ifEmpty { "0" }
                }

                displayLabel = "EDITING"
            }

            "AC" -> {
                first = ""
                second = ""
                operator = ""
                targetSecond = false

                display = "Ready"
                displayLabel = "CALCULATOR"
                status = "Calculator cleared."
            }

            else -> {
                if(!key.all { it.isDigit() })
                    return

                if(targetSecond) {
                    if(second.length >= 18)
                        return

                    second += key
                    display = second
                    displayLabel = "SECOND NUMBER"
                } else {
                    if(first.length >= 18)
                        return

                    first += key
                    display = first
                    displayLabel = "FIRST NUMBER"
                }
            }
        }

        calculatorStatus()
    }


    val calculatorOpen =
        openSection == "calculator"

    val expandedTopHeight =
        if(calculatorOpen) 455.dp else 375.dp

    val topHeight by animateDpAsState(
        targetValue =
            if(topOpen)
                expandedTopHeight
            else 0.dp,
        animationSpec = spring(
            dampingRatio = .88f,
            stiffness = 260f
        ),
        label = "topHeight"
    )

    val listTop by animateDpAsState(
        targetValue =
            if(topOpen)
                expandedTopHeight + 18.dp
            else 112.dp,
        animationSpec = spring(
            dampingRatio = .88f,
            stiffness = 260f
        ),
        label = "listTop"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        /*
         * Main paper/content.
         * Back + footer are LAST content at the bottom,
         * never floating over labels.
         */
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = listTop,
                bottom = 12.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            item {
                V2ToolSection(
                    icon = "÷",
                    title = "Calculator",
                    subtitle = "Numbers and operations",
                    open = calculatorOpen,
                    accent = colors.accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                    onClick = {
                        selectSection("calculator")
                        mode = V2Mode.CALCULATOR
                        displayLabel = "CALCULATOR"
                        calculatorStatus()
                    }
                ) {
                    V2Calculator(
                        accent = colors.accent,
                        surface = surfaceStrong,
                        text = text,
                        onKey = ::calculatorKey
                    )
                }
            }

            item {
                V2ToolSection(
                    icon = "◷",
                    title = "Clock",
                    subtitle = "Timer, clock and stopwatch",
                    open = openSection == "clock",
                    accent = colors.accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                    onClick = {
                        selectSection("clock")
                        status =
                            "Choose Timer, Clock or Stopwatch."
                    }
                ) {
                    V2ClockTools(
                        mode = mode,
                        accent = colors.accent,
                        surface = surfaceStrong,
                        text = text,

                        onTimerTap = {
                            stopwatchRunning = false
                            mode = V2Mode.TIMER

                            if(timerSeconds <= 0) {
                                status =
                                    "Add five seconds before starting."
                            } else {
                                timerRunning =
                                    !timerRunning

                                status =
                                    if(timerRunning)
                                        "Timer running."
                                    else
                                        "Timer paused."
                            }
                        },

                        onTimerHold = {
                            timerRunning = false
                            mode = V2Mode.TIMER
                            timerSeconds = 0
                            status =
                                "Timer reset to zero."
                        },

                        onClock = {
                            stopTimedTools()
                            mode = V2Mode.CLOCK
                            status =
                                "Live clock is active."
                        },

                        onFullscreen = {
                            stopTimedTools()
                            mode = V2Mode.CLOCK
                            fullscreen = true
                        },

                        onStopwatchTap = {
                            timerRunning = false
                            mode = V2Mode.STOPWATCH

                            stopwatchRunning =
                                !stopwatchRunning

                            status =
                                if(stopwatchRunning)
                                    "Stopwatch running."
                                else
                                    "Stopwatch paused."
                        },

                        onStopwatchHold = {
                            stopwatchRunning = false
                            stopwatchMs = 0L
                            mode = V2Mode.STOPWATCH
                            status =
                                "Stopwatch reset."
                        }
                    )
                }
            }

            item {
                V2ToolSection(
                    icon = "+",
                    title = "Counters",
                    subtitle = "Count and generate",
                    open = openSection == "counter",
                    accent = colors.accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                    onClick = {
                        selectSection("counter")
                        stopTimedTools()
                        mode = V2Mode.COUNTER
                        status = "Counter ready."
                    }
                ) {
                    V2Counters(
                        accent = colors.accent,
                        surface = surfaceStrong,
                        text = text,

                        add = {
                            counter++
                            mode = V2Mode.COUNTER
                            display =
                                counter.toString()
                            status =
                                "Counter increased."
                        },

                        minus = {
                            counter =
                                (counter - 1)
                                    .coerceAtLeast(0)

                            mode = V2Mode.COUNTER
                            display =
                                counter.toString()
                            status =
                                "Counter decreased."
                        },

                        reset = {
                            counter = 0
                            mode = V2Mode.COUNTER
                            display = "0"
                            status =
                                "Counter reset to zero."
                        },

                        random = {
                            counter =
                                Random.nextInt(
                                    1,
                                    1001
                                )

                            mode = V2Mode.COUNTER
                            display =
                                counter.toString()
                            status =
                                "Random number generated."
                        }
                    )
                }
            }

            item {
                V2ToolSection(
                    icon = "?",
                    title = "How to use NMIX",
                    subtitle = "Instructions and controls",
                    open = openSection == "help",
                    accent = colors.accent,
                    surface = surface,
                    text = text,
                    muted = muted,
                    onClick = {
                        selectSection("help")
                    }
                ) {
                    V2Instructions(
                        accent = colors.accent,
                        surface = surfaceStrong,
                        text = text,
                        muted = muted
                    )
                }
            }

            // Paper breathing space
            item {
                Spacer(
                    Modifier.height(70.dp)
                )
            }

            // This belongs to the END of the page,
            // not to the tool labels above it.
            item {
                V2PillButton(
                    text = "Back to the Start",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .height(44.dp),
                    color = colors.accent,
                    onClick = onBack
                )
            }

            item {
                Spacer(
                    Modifier.height(17.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        "NMIX",
                        color =
                            text.copy(alpha = .82f),
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "  •  lxzrvi  •  © 2026",
                        color =
                            text.copy(alpha = .55f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        /*
         * Animated top screen.
         */
        AnimatedVisibility(
            visible = topOpen,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(
                    440,
                    easing = EaseOutCubic
                )
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(
                    400,
                    easing = EaseInCubic
                )
            ) + fadeOut(tween(180))
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(topHeight)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 23.dp,
                            bottomEnd = 23.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.dark,
                                colors.accent,
                                colors.dark
                            )
                        )
                    )
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 11.dp
                    )
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    /*
                     * Same horizontal zone as arrow/menu.
                     */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(63.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Text(
                                "EVERYTHING WITH NUMBERS",
                                color =
                                    Color.White
                                        .copy(alpha = .70f),
                                fontSize = 7.5.sp,
                                letterSpacing = 1.9.sp
                            )

                            Text(
                                "NMIX",
                                color = Color.White,
                                fontSize = 29.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = calculatorOpen,
                        enter =
                            fadeIn(tween(280)) +
                            expandVertically(
                                tween(380),
                                expandFrom =
                                    Alignment.Top
                            ) +
                            scaleIn(
                                initialScale = .97f,
                                animationSpec =
                                    tween(380)
                            ),
                        exit =
                            fadeOut(tween(180)) +
                            shrinkVertically(
                                tween(320),
                                shrinkTowards =
                                    Alignment.Top
                            )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 7.dp,
                                    bottom = 8.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            V2CalcField(
                                value =
                                    first.ifEmpty { "_" },
                                modifier =
                                    Modifier.weight(1f),
                                dark = dark
                            )

                            V2CalcField(
                                value =
                                    operator.ifEmpty {
                                        "sign"
                                    },
                                modifier =
                                    Modifier.width(58.dp),
                                dark = dark
                            )

                            V2CalcField(
                                value =
                                    second.ifEmpty { "_" },
                                modifier =
                                    Modifier.weight(1f),
                                dark = dark
                            )
                        }
                    }

                    V2Display(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = displayLabel,
                        value = display,
                        status = status,
                        accent = colors.accent,
                        accentLight = colors.light,
                        dark = dark,
                        timerControls =
                            mode == V2Mode.TIMER,
                        minus = {
                            timerSeconds =
                                (timerSeconds - 5)
                                    .coerceAtLeast(0)

                            if(timerSeconds == 0) {
                                timerRunning = false
                            }

                            status =
                                "Five seconds removed."
                        },
                        plus = {
                            timerSeconds += 5
                            status =
                                "Five seconds added."
                        },
                        onDisplay = {
                            if(
                                mode ==
                                    V2Mode.CALCULATOR &&
                                first.isNotEmpty() &&
                                operator.isNotEmpty() &&
                                second.isNotEmpty()
                            ) {
                                calculate()
                            }
                        }
                    )
                }
            }
        }

        /*
         * Top controls ALWAYS stay below status bar.
         * When screen closes, content can only rise
         * to below these controls.
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 9.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            V2CircleButton(
                text =
                    if(topOpen) "↑"
                    else "↓",
                modifier = Modifier.size(48.dp),
                color = colors.accent,
                onClick = {
                    topOpen = !topOpen

                    if(!topOpen) {
                        settingsOpen = false
                    }
                }
            )

            V2CircleButton(
                text =
                    if(settingsOpen) "×"
                    else "☰",
                modifier = Modifier.size(48.dp),
                color = colors.accent,
                onClick = {
                    settingsOpen =
                        !settingsOpen
                }
            )
        }


        // Settings backdrop
        AnimatedVisibility(
            visible = settingsOpen,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = .24f)
                    )
                    .clickable {
                        settingsOpen = false
                    }
            )
        }

        /*
         * Attached right drawer.
         * Hamburger itself is the X.
         */
        AnimatedVisibility(
            visible = settingsOpen,
            modifier = Modifier.align(
                Alignment.TopEnd
            ),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(
                    380,
                    easing = EaseOutCubic
                )
            ) + fadeIn(tween(200)),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(
                    330,
                    easing = EaseInCubic
                )
            ) + fadeOut(tween(170))
        ) {
            V2Settings(
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
                    .padding(top = 66.dp),
                dark = dark,
                theme = theme,
                accent = colors.accent,
                text = text,
                muted = muted,
                onDark = {
                    dark = !dark

                    prefs.edit()
                        .putBoolean("dark",dark)
                        .apply()
                },
                onTheme = {
                    theme = it

                    prefs.edit()
                        .putString("theme",it)
                        .apply()
                }
            )
        }

        if(fullscreen) {
            Dialog(
                onDismissRequest = {
                    fullscreen = false
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF06100D),
                                    colors.dark,
                                    colors.accent
                                        .copy(alpha = .65f),
                                    Color(0xFF06100D)
                                )
                            )
                        )
                ) {
                    Column(
                        Modifier.align(
                            Alignment.Center
                        ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            "NMIX • LOCAL TIME",
                            color =
                                Color.White
                                    .copy(alpha = .62f),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            timeText(),
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Text(
                            dateText(),
                            color =
                                Color.White
                                    .copy(alpha = .68f),
                            fontSize = 12.sp
                        )
                    }

                    V2PillButton(
                        text = "×  Exit",
                        modifier = Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .windowInsetsPadding(
                                WindowInsets
                                    .navigationBars
                            )
                            .padding(18.dp)
                            .width(92.dp)
                            .height(42.dp),
                        color =
                            Color.White
                                .copy(alpha = .15f),
                        onClick = {
                            fullscreen = false
                        }
                    )
                }
            }
        }
    }
}

/* =========================================================
   RESULT DISPLAY
   ========================================================= */

@Composable
private fun V2Display(
    modifier: Modifier,
    label: String,
    value: String,
    status: String,
    accent: Color,
    accentLight: Color,
    dark: Boolean,
    timerControls: Boolean,
    minus: () -> Unit,
    plus: () -> Unit,
    onDisplay: () -> Unit
) {
    val motion =
        rememberInfiniteTransition(
            label = "displayMotion"
        )

    val move by motion.animateFloat(
        initialValue = -120f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                4700,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "displayGlow"
    )

    Box(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    if(dark) {
                        listOf(
                            Color(0xFF202725),
                            Color(0xFF121816)
                        )
                    } else {
                        listOf(
                            Color(0xFFF0F3F1),
                            Color(0xFFD7DFDC)
                        )
                    }
                )
            )
            .clickable(onClick = onDisplay)
    ) {
        Box(
            Modifier
                .size(230.dp)
                .graphicsLayer {
                    translationX = move
                    translationY = move * .18f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentLight.copy(
                                alpha = .27f
                            ),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .align(
                    Alignment.BottomEnd
                )
                .size(240.dp)
                .graphicsLayer {
                    translationX =
                        -move * .65f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(
                                alpha = .18f
                            ),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Label always at top
        Text(
            label,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 15.dp),
            color = accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        // Main result always centered
        Text(
            value,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal =
                        if(timerControls)
                            68.dp
                        else 12.dp
                ),
            color =
                if(dark)
                    Color.White
                else
                    Color(0xFF152C24),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        // Status always at bottom
        Text(
            status,
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(
                    start = 15.dp,
                    end = 15.dp,
                    bottom = 13.dp
                ),
            color = accent.copy(alpha = .86f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        AnimatedVisibility(
            visible = timerControls,
            modifier = Modifier
                .align(
                    Alignment.CenterStart
                )
                .padding(start = 12.dp),
            enter =
                fadeIn(tween(220)) +
                scaleIn(
                    initialScale = .8f
                ),
            exit =
                fadeOut(tween(170)) +
                scaleOut(
                    targetScale = .8f
                )
        ) {
            V2CircleButton(
                text = "−",
                modifier = Modifier.size(47.dp),
                color = accent,
                onClick = minus
            )
        }

        AnimatedVisibility(
            visible = timerControls,
            modifier = Modifier
                .align(
                    Alignment.CenterEnd
                )
                .padding(end = 12.dp),
            enter =
                fadeIn(tween(220)) +
                scaleIn(
                    initialScale = .8f
                ),
            exit =
                fadeOut(tween(170)) +
                scaleOut(
                    targetScale = .8f
                )
        ) {
            V2CircleButton(
                text = "+",
                modifier = Modifier.size(47.dp),
                color = accent,
                onClick = plus
            )
        }
    }
}

@Composable
private fun V2CalcField(
    value: String,
    modifier: Modifier,
    dark: Boolean
) {
    Box(
        modifier
            .height(49.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if(dark)
                    Color(0xFF202624)
                else
                    Color.White.copy(
                        alpha = .88f
                    )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            value,
            color =
                if(dark)
                    Color.White
                else Color(0xFF202321),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/* =========================================================
   TOOL SECTION + DOUBLE SPIN
   ========================================================= */

@Composable
private fun V2ToolSection(
    icon: String,
    title: String,
    subtitle: String,
    open: Boolean,
    accent: Color,
    surface: Color,
    text: Color,
    muted: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val outer by animateFloatAsState(
        targetValue =
            if(open) 180f else 0f,
        animationSpec = tween(
            620,
            easing = EaseInOutCubic
        ),
        label = "outerSpin"
    )

    val inner by animateFloatAsState(
        targetValue =
            if(open) -180f else 0f,
        animationSpec = tween(
            620,
            easing = EaseInOutCubic
        ),
        label = "innerSpin"
    )

    val arrow by animateFloatAsState(
        targetValue =
            if(open) 180f else 0f,
        animationSpec = tween(400),
        label = "arrow"
    )

    val outerRadius by animateDpAsState(
        targetValue = if(open) 21.dp else 9.dp,
        animationSpec = tween(
            620,
            easing = EaseInOutCubic
        ),
        label = "outerRadius"
    )

    val innerRadius by animateDpAsState(
        targetValue = if(open) 15.dp else 6.dp,
        animationSpec = tween(
            620,
            easing = EaseInOutCubic
        ),
        label = "innerRadius"
    )

    Column(
        Modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(13.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                // OUTER: clockwise + smoothly square -> circle
                Box(
                    Modifier
                        .fillMaxSize()
                        .rotate(outer)
                        .clip(
                            RoundedCornerShape(
                                outerRadius
                            )
                        )
                        .background(accent)
                )

                // INNER OUTLINE: anti-clockwise
                Canvas(
                    Modifier
                        .size(31.dp)
                        .rotate(inner)
                ) {
                    val stroke =
                        1.35.dp.toPx()

                    drawRoundRect(
                        color =
                            Color.White.copy(
                                alpha = .42f
                            ),
                        topLeft = Offset(
                            stroke,
                            stroke
                        ),
                        size = Size(
                            size.width -
                                stroke * 2,
                            size.height -
                                stroke * 2
                        ),
                        cornerRadius =
                            CornerRadius(
                                innerRadius.toPx(),
                                innerRadius.toPx()
                            ),
                        style = Stroke(
                            width = stroke
                        )
                    )
                }

                // VECTOR ICON: never rotates
                V2VectorIcon(
                    name = icon,
                    tint = Color.White,
                    modifier =
                        Modifier.size(19.dp)
                )
            }

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = text,
                    fontSize = 14.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    subtitle,
                    color = muted,
                    fontSize = 9.sp
                )
            }

            Text(
                "⌄",
                modifier =
                    Modifier.rotate(arrow),
                color = muted,
                fontSize = 18.sp
            )
        }

        AnimatedVisibility(
            visible = open,
            enter =
                fadeIn(tween(280)) +
                expandVertically(
                    animationSpec =
                        tween(
                            330,
                            easing = EaseOutCubic
                        ),
                    expandFrom =
                        Alignment.Top
                ),
            exit =
                fadeOut(tween(180)) +
                shrinkVertically(
                    animationSpec =
                        tween(
                            280,
                            easing = EaseInCubic
                        ),
                    shrinkTowards =
                        Alignment.Top
                )
        ) {
            content()
        }
    }
}

@Composable
private fun V2VectorIcon(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = size.minDimension * .105f

        fun line(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float
        ) {
            drawLine(
                color = tint,
                start = Offset(w*x1,h*y1),
                end = Offset(w*x2,h*y2),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
        }

        when(name) {
            "÷" -> {
                drawCircle(
                    tint,
                    w*.055f,
                    Offset(w*.5f,h*.20f)
                )
                line(.20f,.50f,.80f,.50f)
                drawCircle(
                    tint,
                    w*.055f,
                    Offset(w*.5f,h*.80f)
                )
            }

            "◷" -> {
                drawCircle(
                    tint,
                    w*.35f,
                    Offset(w*.5f,h*.5f),
                    style = Stroke(sw)
                )
                line(.5f,.5f,.5f,.30f)
                line(.5f,.5f,.68f,.58f)
            }

            "+" -> {
                line(.5f,.20f,.5f,.80f)
                line(.20f,.5f,.80f,.5f)
            }

            "?" -> {
                val path =
                    androidx.compose.ui.graphics.Path()

                path.moveTo(
                    w*.28f,
                    h*.32f
                )

                path.cubicTo(
                    w*.34f,h*.10f,
                    w*.74f,h*.10f,
                    w*.74f,h*.36f
                )

                path.cubicTo(
                    w*.74f,h*.54f,
                    w*.50f,h*.57f,
                    w*.50f,h*.68f
                )

                drawPath(
                    path,
                    tint,
                    style = Stroke(
                        sw,
                        cap = StrokeCap.Round
                    )
                )

                drawCircle(
                    tint,
                    w*.055f,
                    Offset(
                        w*.5f,
                        h*.86f
                    )
                )
            }
        }
    }
}

/* =========================================================
   CALCULATOR
   ========================================================= */

@Composable
private fun V2Calculator(
    accent: Color,
    surface: Color,
    text: Color,
    onKey: (String) -> Unit
) {
    val keys = listOf(
        "1","2","3","4","5",
        "6","7","8","9","0",
        "+","−","×","÷","%",
        ".","±","⌫","AC","="
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 8.dp,
                bottom = 14.dp
            )
    ) {
        keys.chunked(5).forEach {
            row ->

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    val operator =
                        key in listOf(
                            "+","−","×",
                            "÷","%","="
                        )

                    val danger =
                        key == "AC"

                    V2CircleButton(
                        text = key,
                        modifier =
                            Modifier.size(55.dp),
                        color = when {
                            operator -> accent

                            danger ->
                                Color(0xFFD83939)
                                    .copy(alpha = .18f)

                            else -> surface
                        },
                        textColor = when {
                            operator ->
                                Color.White

                            danger ->
                                Color(0xFFD83939)

                            else -> text
                        },
                        onClick = {
                            onKey(key)
                        }
                    )
                }
            }

            Spacer(
                Modifier.height(9.dp)
            )
        }
    }
}

/* =========================================================
   CLOCK TOOLS
   ========================================================= */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun V2ClockTools(
    mode: V2Mode,
    accent: Color,
    surface: Color,
    text: Color,
    onTimerTap: () -> Unit,
    onTimerHold: () -> Unit,
    onClock: () -> Unit,
    onFullscreen: () -> Unit,
    onStopwatchTap: () -> Unit,
    onStopwatchHold: () -> Unit
) {
    Column(
        Modifier.padding(12.dp),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        V2ModeRow(
            title = "Timer",
            icon = "◴",
            selected =
                mode == V2Mode.TIMER,
            accent = accent,
            surface = surface,
            text = text,
            onClick = onTimerTap,
            onLong = onTimerHold
        )

        Box {
            V2ModeRow(
                title = "Clock",
                icon = "◷",
                selected =
                    mode == V2Mode.CLOCK,
                accent = accent,
                surface = surface,
                text = text,
                onClick = onClock
            )

            V2SmallButton(
                text = "⛶",
                modifier = Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(end = 10.dp)
                    .size(38.dp),
                color =
                    if(mode == V2Mode.CLOCK)
                        Color.White.copy(
                            alpha = .18f
                        )
                    else
                        accent.copy(
                            alpha = .16f
                        ),
                textColor =
                    if(mode == V2Mode.CLOCK)
                        Color.White
                    else accent,
                onClick = onFullscreen
            )
        }

        V2ModeRow(
            title = "Stopwatch",
            icon = "◉",
            selected =
                mode == V2Mode.STOPWATCH,
            accent = accent,
            surface = surface,
            text = text,
            onClick = onStopwatchTap,
            onLong = onStopwatchHold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun V2ModeRow(
    title: String,
    icon: String,
    selected: Boolean,
    accent: Color,
    surface: Color,
    text: Color,
    onClick: () -> Unit,
    onLong: (() -> Unit)? = null
) {
    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed) .965f else 1f,
        spring(
            dampingRatio = .7f,
            stiffness = 700f
        ),
        label = "modePress"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if(selected)
                    accent
                else surface
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    onLong?.invoke()
                }
            )
            .padding(horizontal = 13.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(35.dp)
                .clip(
                    if(selected)
                        CircleShape
                    else
                        RoundedCornerShape(8.dp)
                )
                .background(
                    if(selected)
                        Color.White
                            .copy(alpha = .92f)
                    else
                        accent
                            .copy(alpha = .15f)
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                icon,
                color =
                    if(selected)
                        accent
                    else accent,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            Modifier.width(12.dp)
        )

        Text(
            title,
            color =
                if(selected)
                    Color.White
                else text,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

/* =========================================================
   COUNTERS
   ========================================================= */

@Composable
private fun V2Counters(
    accent: Color,
    surface: Color,
    text: Color,
    add: () -> Unit,
    minus: () -> Unit,
    reset: () -> Unit,
    random: () -> Unit
) {
    Column(
        Modifier.padding(12.dp),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            V2RectButton(
                "Add",
                Modifier.weight(1f),
                surface,
                text,
                add
            )

            V2RectButton(
                "Reset",
                Modifier.weight(1f),
                surface,
                text,
                reset
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            V2RectButton(
                "Random",
                Modifier.weight(1f),
                accent.copy(alpha = .16f),
                text,
                random
            )

            V2RectButton(
                "Minus",
                Modifier.weight(1f),
                surface,
                text,
                minus
            )
        }
    }
}

/* =========================================================
   INSTRUCTIONS
   ========================================================= */

@Composable
private fun V2Instructions(
    accent: Color,
    surface: Color,
    text: Color,
    muted: Color
) {
    val help = listOf(
        "Calculator" to
            "Use the NMIX keypad for numbers, operators and editing.",

        "Timer" to
            "Tap Timer to start or pause. Hold Timer to reset to zero. Use − / + on the main display.",

        "Clock" to
            "Tap Clock for local time. Use ⛶ for the full-screen clock.",

        "Stopwatch" to
            "Tap to start or pause. Hold Stopwatch to reset.",

        "Counters" to
            "Add and Minus change the count. Reset returns to zero. Random generates 1–1000.",

        "Top Screen" to
            "Use the top-left arrow to hide or restore the NMIX screen.",

        "Settings" to
            "Use the top-right menu for light/dark appearance and color themes."
    )

    Column(
        Modifier.padding(11.dp),
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        help.forEach { item ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            11.dp
                        )
                    )
                    .background(surface)
                    .padding(11.dp)
            ) {
                Column {
                    Text(
                        item.first,
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Text(
                        item.second,
                        color = muted,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/* =========================================================
   SETTINGS DRAWER
   ========================================================= */

@Composable
private fun V2Settings(
    modifier: Modifier,
    dark: Boolean,
    theme: String,
    accent: Color,
    text: Color,
    muted: Color,
    onDark: () -> Unit,
    onTheme: (String) -> Unit
) {
    val bg =
        if(dark)
            Color(0xFF171C1A)
        else
            Color(0xFFF3F4F3)

    Column(
        modifier
            .width(330.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 22.dp,
                    bottomStart = 22.dp
                )
            )
            .background(bg)
            .padding(17.dp)
    ) {
        Text(
            "NMIX Settings",
            color = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Personalize your interface",
            color = muted,
            fontSize = 9.sp
        )

        Spacer(
            Modifier.height(20.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = onDark
                )
                .padding(vertical = 6.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    "Appearance",
                    color = text,
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    if(dark)
                        "Dark mode"
                    else
                        "Light mode",
                    color = muted,
                    fontSize = 9.sp
                )
            }

            Box(
                Modifier
                    .width(49.dp)
                    .height(28.dp)
                    .clip(
                        RoundedCornerShape(
                            50
                        )
                    )
                    .background(
                        if(dark)
                            accent
                        else
                            Color(0xFFD0D5D2)
                    )
                    .padding(4.dp)
            ) {
                Box(
                    Modifier
                        .align(
                            if(dark)
                                Alignment.CenterEnd
                            else
                                Alignment.CenterStart
                        )
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White
                        )
                )
            }
        }

        Spacer(
            Modifier.height(20.dp)
        )

        Text(
            "Color Theme",
            color = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            "Choose your NMIX color",
            color = muted,
            fontSize = 9.sp
        )

        Spacer(
            Modifier.height(12.dp)
        )

        val choices = listOf(
            "green" to Color(0xFF319B79),
            "blue" to Color(0xFF348BB8),
            "purple" to Color(0xFF8A62C8),
            "orange" to Color(0xFFD57D35),
            "rose" to Color(0xFFC85878)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            choices.forEach { choice ->
                val selected =
                    choice.first == theme

                Box(
                    Modifier
                        .size(
                            if(selected)
                                42.dp
                            else
                                38.dp
                        )
                        .clip(CircleShape)
                        .background(
                            choice.second
                        )
                        .clickable {
                            onTheme(
                                choice.first
                            )
                        },
                    contentAlignment =
                        Alignment.Center
                ) {
                    if(selected) {
                        Text(
                            "✓",
                            color = Color.White,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Text(
            "Appearance and theme are saved on this device.",
            color = muted,
            fontSize = 8.sp
        )
    }
}

/* =========================================================
   BUTTONS
   ========================================================= */

@Composable
private fun V2CircleButton(
    text: String,
    modifier: Modifier,
    color: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    V2PressBox(
        modifier = modifier,
        shape = CircleShape,
        color = color,
        onClick = onClick
    ) {
        Text(
            text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun V2SmallButton(
    text: String,
    modifier: Modifier,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    V2PressBox(
        modifier,
        RoundedCornerShape(9.dp),
        color,
        onClick
    ) {
        Text(
            text,
            color = textColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun V2PillButton(
    text: String,
    modifier: Modifier,
    color: Color,
    onClick: () -> Unit
) {
    V2PressBox(
        modifier,
        RoundedCornerShape(50),
        color,
        onClick
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun V2RectButton(
    text: String,
    modifier: Modifier,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    V2PressBox(
        modifier.height(65.dp),
        RoundedCornerShape(12.dp),
        color,
        onClick
    ) {
        Text(
            text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun V2PressBox(
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed) .93f else 1f,
        animationSpec = spring(
            dampingRatio = .65f,
            stiffness = 720f
        ),
        label = "press"
    )

    Box(
        modifier
            .scale(scale)
            .clip(shape)
            .background(color)
            .clickable(
                interactionSource =
                    interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
