package com.lxzrvi.nmix
import kotlin.random.Random
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NmixColors(
    val accent: Color,
    val dark: Color,
    val light: Color
)

private val NativeGreen = NmixColors(
    Color(0xFF319B79),
    Color(0xFF19493A),
    Color(0xFF69D6B2)
)

@Composable
fun NativeMainPage(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "nmix_appearance",
            android.content.Context.MODE_PRIVATE
        )
    }

    var settingsOpen by remember { mutableStateOf(false) }
    var fullscreenClock by remember { mutableStateOf(false) }

    var darkMode by remember {
        mutableStateOf(prefs.getBoolean("dark", false))
    }

    var themeName by remember {
        mutableStateOf(
            prefs.getString("theme", "green") ?: "green"
        )
    }

    val themeAccent = when (themeName) {
        "blue" -> Color(0xFF348BB8)
        "purple" -> Color(0xFF8A62C8)
        "orange" -> Color(0xFFD57D35)
        "rose" -> Color(0xFFC85878)
        else -> Color(0xFF319B79)
    }

    val pageBackground =
        if (darkMode) Color(0xFF0D1110)
        else Color(0xFFE0E2E1)

    val surfaceColor =
        if (darkMode) Color(0xFF171C1A)
        else Color(0xFFF0F1F0)

    val primaryText =
        if (darkMode) Color(0xFFEDF4F1)
        else Color(0xFF202321)
    var topOpen by remember { mutableStateOf(true) }
    var calculatorOpen by remember { mutableStateOf(false) }
    var clockOpen by remember { mutableStateOf(false) }
    var counterOpen by remember { mutableStateOf(false) }
    var instructionsOpen by remember { mutableStateOf(false) }
    var counter by remember { mutableIntStateOf(0) }

    var activeMode by remember { mutableStateOf("idle") }

    var timerSeconds by remember { mutableIntStateOf(10) }
    var timerRunning by remember { mutableStateOf(false) }

    var stopwatchMs by remember { mutableLongStateOf(0L) }
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchBase by remember { mutableLongStateOf(0L) }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(250)
        }
    }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerRunning && timerSeconds > 0) {
                delay(1000)
                if (timerRunning) {
                    timerSeconds = (timerSeconds - 1).coerceAtLeast(0)

                    if (timerSeconds == 0) {
                        timerRunning = false
                        display = "00:00"
                        displayLabel = "TIMER"
                        status = "Time's up!"
                    }
                }
            }
        }
    }

    LaunchedEffect(stopwatchRunning) {
        if (stopwatchRunning) {
            stopwatchBase =
                android.os.SystemClock.elapsedRealtime() - stopwatchMs

            while (stopwatchRunning) {
                stopwatchMs =
                    android.os.SystemClock.elapsedRealtime() - stopwatchBase

                delay(30)
            }
        }
    }

    fun timerText(): String {
        val m = timerSeconds / 60
        val sec = timerSeconds % 60
        return "%02d:%02d".format(m, sec)
    }

    fun stopwatchText(): String {
        val total = stopwatchMs / 1000
        val minutes = total / 60
        val seconds = total % 60
        val hundredths = (stopwatchMs % 1000) / 10

        return "%02d:%02d.%02d".format(
            minutes,
            seconds,
            hundredths
        )
    }

    fun liveTime(): String =
        SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(Date(now))

    fun liveDate(): String =
        SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        ).format(Date(now))

    LaunchedEffect(
        activeMode,
        timerSeconds,
        stopwatchMs,
        now
    ) {
        when (activeMode) {
            "timer" -> {
                display = timerText()
                displayLabel = "TIMER"
            }

            "stopwatch" -> {
                display = stopwatchText()
                displayLabel = "STOPWATCH"
            }

            "clock" -> {
                display = liveTime()
                displayLabel = "LIVE CLOCK"
            }
        }
    }

    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }
    var targetSecond by remember { mutableStateOf(false) }

    var display by remember { mutableStateOf("Ready") }
    var displayLabel by remember { mutableStateOf("NMIX LIVE") }
    var status by remember { mutableStateOf("Choose a tool below.") }

    val colors = NativeGreen

    val topHeight by animateDpAsState(
        targetValue = when {
            !topOpen -> 0.dp
            calculatorOpen -> 395.dp
            else -> 325.dp
        },
        animationSpec = spring(
            dampingRatio = .85f,
            stiffness = 240f
        ),
        label = "topHeight"
    )

    val contentTop by animateDpAsState(
        targetValue = when {
            !topOpen -> 72.dp
            calculatorOpen -> 415.dp
            else -> 345.dp
        },
        animationSpec = spring(
            dampingRatio = .85f,
            stiffness = 240f
        ),
        label = "contentTop"
    )

    fun openCalculator() {
        topOpen = true
        calculatorOpen = true
        clockOpen = false
        timerRunning = false
        stopwatchRunning = false
        activeMode = "calculator"
        displayLabel = "CALCULATOR"

        status = when {
            first.isEmpty() -> "Enter your first number."
            operator.isEmpty() -> "Choose an operator."
            second.isEmpty() -> "Enter the second number."
            else -> "Ready — tap = to calculate."
        }
    }

    fun formatResult(value: Double): String {
        if (!value.isFinite()) return "Overflow"

        val rounded = value.toLong()

        return if (value == rounded.toDouble()) {
            rounded.toString()
        } else {
            String.format(
                java.util.Locale.US,
                "%.10f",
                value
            ).trimEnd('0').trimEnd('.')
        }
    }

    fun calculate() {
        openCalculator()

        val a = first.toDoubleOrNull()
        val b = second.toDoubleOrNull()

        if (a == null || b == null) {
            display = "Incomplete"
            status = "Enter both numbers first."
            return
        }

        val result = when (operator) {
            "+" -> a + b
            "−", "-" -> a - b
            "×" -> a * b

            "÷" -> {
                if (b == 0.0) {
                    display = "Error"
                    status = "Division by zero is not allowed."
                    return
                }
                a / b
            }

            "%" -> {
                if (b == 0.0) {
                    display = "Error"
                    status = "Remainder by zero is not allowed."
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

        display = formatResult(result)
        displayLabel = "RESULT"
        status = "Calculation complete."
    }

    fun calculatorPress(value: String) {
        openCalculator()

        when (value) {
            "+", "−", "×", "÷", "%" -> {
                if (first.isEmpty()) {
                    status = "Enter the first number before selecting an operator."
                    return
                }

                operator = value
                targetSecond = true
                display = value
                displayLabel = "OPERATOR"
                status = "Enter the second number."
            }

            "=" -> calculate()

            "." -> {
                if (targetSecond) {
                    if (second.contains(".")) {
                        status = "This number already contains a decimal."
                        return
                    }

                    second += if (second.isEmpty()) "0." else "."
                    display = second
                    displayLabel = "DECIMAL"
                } else {
                    if (first.contains(".")) {
                        status = "This number already contains a decimal."
                        return
                    }

                    first += if (first.isEmpty()) "0." else "."
                    display = first
                    displayLabel = "DECIMAL"
                }
            }

            "±" -> {
                if (targetSecond) {
                    val n = second.toDoubleOrNull()

                    if (n == null) {
                        status = "Enter a number before using ±."
                        return
                    }

                    second = formatResult(-n)
                    display = second
                } else {
                    val n = first.toDoubleOrNull()

                    if (n == null) {
                        status = "Enter a number before using ±."
                        return
                    }

                    first = formatResult(-n)
                    display = first
                }

                displayLabel = "SIGN CHANGED"
            }

            "⌫" -> {
                if (targetSecond) {
                    if (second.isNotEmpty()) {
                        second = second.dropLast(1)
                        display = second.ifEmpty { "0" }
                    } else if (operator.isNotEmpty()) {
                        operator = ""
                        targetSecond = false
                        display = first.ifEmpty { "Ready" }
                    }
                } else {
                    first = first.dropLast(1)
                    display = first.ifEmpty { "0" }
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
                if (!value.all { it.isDigit() }) return

                if (targetSecond) {
                    if (second.length >= 18) {
                        status = "Maximum number length reached."
                        return
                    }

                    second += value
                    display = second
                    displayLabel = "SECOND NUMBER"
                } else {
                    if (first.length >= 18) {
                        status = "Maximum number length reached."
                        return
                    }

                    first += value
                    display = first
                    displayLabel = "FIRST NUMBER"
                }

                status = when {
                    first.isNotEmpty() &&
                        operator.isNotEmpty() &&
                        second.isNotEmpty() ->
                        "Ready — tap = to calculate."

                    first.isNotEmpty() &&
                        operator.isNotEmpty() ->
                        "Enter the second number."

                    else ->
                        "Choose an operator."
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentTop,
                bottom = 66.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NativeToolSection(
                    icon = "÷",
                    title = "Calculator",
                    subtitle = "Numbers and operations",
                    open = calculatorOpen,
                    onClick = {
                        topOpen = true
                        calculatorOpen = !calculatorOpen

                        if (calculatorOpen) {
                            openCalculator()
                        }
                    }
                ) {
                    NativeCalculatorGrid {
                        calculatorPress(it)
                    }
                }
            }

            item {
                NativeToolSection(
                    icon = "◷",
                    title = "Clock",
                    subtitle = "Timer, clock and stopwatch",
                    open = clockOpen,
                    onClick = {
                        topOpen = true
                        calculatorOpen = false
                        clockOpen = !clockOpen

                        if (clockOpen) {
                            status = "Choose Timer, Clock or Stopwatch."
                        }
                    }
                ) {
                    ClockTools(
                        activeMode = activeMode,
                        timerText = timerText(),
                        timerRunning = timerRunning,
                        clockText = liveTime(),
                        stopwatchText = stopwatchText(),
                        stopwatchRunning = stopwatchRunning,

                        onTimerTap = {
                            calculatorOpen = false
                            activeMode = "timer"
                            display = timerText()
                            displayLabel = "TIMER"
                            status =
                                "Use − / + for five seconds. Hold Timer to start."
                        },

                        onTimerHold = {
                            calculatorOpen = false
                            activeMode = "timer"

                            if (timerSeconds <= 0) {
                                status =
                                    "Add five seconds before starting."
                            } else {
                                timerRunning = !timerRunning

                                status = if (timerRunning)
                                    "Timer running. Hold Timer to pause."
                                else
                                    "Timer paused. Hold Timer to continue."
                            }
                        },

                        onMinus = {
                            activeMode = "timer"
                            timerSeconds =
                                (timerSeconds - 5).coerceAtLeast(0)

                            if (timerSeconds == 0) {
                                timerRunning = false
                                status = "Timer is at zero."
                            } else {
                                status = "Five seconds removed."
                            }
                        },

                        onPlus = {
                            activeMode = "timer"
                            timerSeconds += 5
                            status = "Five seconds added."
                        },

                        onClock = {
                            timerRunning = false
                            stopwatchRunning = false
                            activeMode = "clock"
                            display = liveTime()
                            displayLabel = "LIVE CLOCK"
                            status = "Live clock is active."
                        },

                        onStopwatchTap = {
                            timerRunning = false
                            activeMode = "stopwatch"

                            stopwatchRunning = !stopwatchRunning

                            status = if (stopwatchRunning)
                                "Stopwatch running."
                            else
                                "Stopwatch paused. Tap again to continue."
                        },

                        onStopwatchHold = {
                            activeMode = "stopwatch"
                            stopwatchRunning = false
                            stopwatchMs = 0L
                            display = "00:00.00"
                            displayLabel = "STOPWATCH"
                            status = "Stopwatch reset."
                        }
                    )
                }
            }

            item {
                NativeToolSection(
                    icon = "+",
                    title = "Counters",
                    subtitle = "Count and generate",
                    open = counterOpen,
                    onClick = {
                        topOpen = true
                        calculatorOpen = false
                        clockOpen = false
                        instructionsOpen = false
                        counterOpen = !counterOpen
                        timerRunning = false
                        stopwatchRunning = false

                        if (counterOpen) {
                            activeMode = "counter"
                            displayLabel = "COUNTER"
                            display = counter.toString()
                            status = "Counter ready."
                        }
                    }
                ) {
                    CounterTools(
                        value = counter,
                        onAdd = {
                            activeMode = "counter"
                            counter++
                            display = counter.toString()
                            displayLabel = "COUNTER"
                            status = "Counter increased."
                        },
                        onMinus = {
                            activeMode = "counter"
                            counter = (counter - 1).coerceAtLeast(0)
                            display = counter.toString()
                            displayLabel = "COUNTER"
                            status = "Counter decreased."
                        },
                        onReset = {
                            activeMode = "counter"
                            counter = 0
                            display = "0"
                            displayLabel = "COUNTER"
                            status = "Counter reset to zero."
                        },
                        onRandom = {
                            activeMode = "counter"
                            counter = Random.nextInt(1,1001)
                            display = counter.toString()
                            displayLabel = "COUNTER"
                            status = "Random number generated."
                        }
                    )
                }
            }

            item {
                NativeToolSection(
                    icon = "?",
                    title = "How to use NMIX",
                    subtitle = "Instructions and controls",
                    open = instructionsOpen,
                    onClick = {
                        topOpen = true
                        calculatorOpen = false
                        clockOpen = false
                        counterOpen = false
                        instructionsOpen = !instructionsOpen

                        if (instructionsOpen) {
                            status = "NMIX instructions opened."
                        }
                    }
                ) {
                    InstructionsPanel()
                }
            }

            item {
                Spacer(Modifier.height(5.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NativePillButton(
                        text = "Back to the Start",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        background = colors.accent,
                        textColor = Color.White,
                        onClick = onBack
                    )
                }
            }


        }

        AnimatedVisibility(
            visible = topOpen,
            enter = slideInVertically(
                initialOffsetY = { -it }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it }
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topHeight)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 22.dp,
                            bottomEnd = 22.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                themeAccent.copy(alpha = .55f),
                                themeAccent,
                                Color(0xFF102B23)
                            )
                        )
                    )
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 27.dp,
                        bottom = 11.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(7.dp))

                    Text(
                        "EVERYTHING WITH NUMBERS",
                        color = Color.White.copy(alpha = .68f),
                        fontSize = 7.sp,
                        letterSpacing = 1.8.sp
                    )

                    Text(
                        "NMIX",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )

                    AnimatedVisibility(
                        visible = calculatorOpen,
                        enter = fadeIn(tween(300)) +
                            expandVertically(
                                animationSpec = tween(380),
                                expandFrom = Alignment.Top
                            ) +
                            scaleIn(
                                initialScale = .96f,
                                animationSpec = tween(380)
                            ),
                        exit = fadeOut(tween(220)) +
                            shrinkVertically(
                                animationSpec = tween(330),
                                shrinkTowards = Alignment.Top
                            ) +
                            scaleOut(
                                targetScale = .97f,
                                animationSpec = tween(300)
                            )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 15.dp,
                                    bottom = 9.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            CalcValueBox(
                                text = first.ifEmpty { "_" },
                                Modifier.weight(1f)
                            )

                            CalcValueBox(
                                text = operator.ifEmpty { "sign" },
                                Modifier.width(58.dp)
                            )

                            CalcValueBox(
                                text = second.ifEmpty { "_" },
                                Modifier.weight(1f)
                            )
                        }
                    }

                    NativeResultDisplay(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = displayLabel,
                        value = display,
                        status = status,
                        accent = colors.accent,
                        light = colors.light,
                        onClick = {
                            if (
                                calculatorOpen &&
                                first.isNotEmpty() &&
                                operator.isNotEmpty() &&
                                second.isNotEmpty()
                            ) {
                                calculate()
                            } else if (activeMode == "clock") {
                                fullscreenClock = true
                            }
                        }
                    )
                }
            }
        }

        if (settingsOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = .32f))
                    .clickable { settingsOpen = false }
            )

            SettingsPanel(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 82.dp, end = 13.dp),
                darkMode = darkMode,
                themeName = themeName,
                onDarkChange = {
                    darkMode = !darkMode
                    prefs.edit()
                        .putBoolean("dark", darkMode)
                        .apply()
                },
                onTheme = {
                    themeName = it
                    prefs.edit()
                        .putString("theme", it)
                        .apply()
                },
                onClose = {
                    settingsOpen = false
                }
            )
        }

        if (fullscreenClock) {
            Dialog(
                onDismissRequest = {
                    fullscreenClock = false
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
                                    Color(0xFF07110F),
                                    themeAccent.copy(alpha = .55f),
                                    Color(0xFF070D0B)
                                )
                            )
                        )
                ) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "NMIX • LOCAL TIME",
                            color = Color.White.copy(alpha = .65f),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            liveTime(),
                            color = Color.White,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            liveDate(),
                            color = Color.White.copy(alpha = .65f),
                            fontSize = 12.sp
                        )
                    }

                    NativePillButton(
                        text = "×  Exit",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(22.dp)
                            .width(90.dp)
                            .height(42.dp),
                        background =
                            Color.White.copy(alpha = .15f),
                        textColor = Color.White,
                        onClick = {
                            fullscreenClock = false
                        }
                    )
                }
            }
        }

        // Fixed branding footer — independent from scrolling tools.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE0E2E1).copy(alpha = .92f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "NMIX",
                color = Color(0xFF4E5753),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .7.sp
            )

            Text(
                "  •  lxzrvi  •  © 2026",
                color = Color(0xFF737C78),
                fontSize = 12.sp
            )
        }

        // Persistent top controls.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 36.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NativePressButton(
                text = if (topOpen) "↑" else "↓",
                modifier = Modifier.size(50.dp),
                background = themeAccent.copy(alpha = .94f),
                textColor = Color.White,
                onClick = {
                    topOpen = !topOpen
                }
            )

            NativePressButton(
                text = "☰",
                modifier = Modifier.size(50.dp),
                background = themeAccent.copy(alpha = .94f),
                textColor = Color.White,
                onClick = {
                    settingsOpen = true
                }
            )
        }
    }
}

@Composable
private fun NativeResultDisplay(
    modifier: Modifier,
    label: String,
    value: String,
    status: String,
    accent: Color,
    light: Color,
    onClick: () -> Unit
) {
    val motion = rememberInfiniteTransition(label = "resultMotion")

    val move by motion.animateFloat(
        initialValue = -120f,
        targetValue = 160f,
        animationSpec = infiniteRepeatable(
            tween(4500, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "resultMove"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFF0F3F1),
                        Color(0xFFD7DFDC)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(210.dp)
                .graphicsLayer {
                    translationX = move
                    translationY = move * .15f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            light.copy(alpha = .32f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(220.dp)
                .graphicsLayer {
                    translationX = -move * .65f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(alpha = .20f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                color = Color(0xFF216E56),
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                value,
                color = Color(0xFF152C24),
                fontSize = 31.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(Modifier.height(5.dp))

            Text(
                status,
                color = Color(0xFF397C68),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalcValueBox(
    text: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(49.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White.copy(alpha = .84f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color(0xFF202321),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun NativeToolSection(
    icon: String,
    title: String,
    subtitle: String,
    open: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFF0F1F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(
                        if (open) CircleShape
                        else RoundedCornerShape(9.dp)
                    )
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color(0xFF202321),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    subtitle,
                    color = Color(0xFF66706C),
                    fontSize = 9.sp
                )
            }

            Text(
                if (open) "⌃" else "⌄",
                color = Color(0xFF66706C),
                fontSize = 18.sp
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = fadeIn(tween(320)) +
                expandVertically(
                    animationSpec = tween(420),
                    expandFrom = Alignment.Top
                ) +
                scaleIn(
                    initialScale = .985f,
                    animationSpec = tween(420)
                ),
            exit = fadeOut(tween(190)) +
                shrinkVertically(
                    animationSpec = tween(350),
                    shrinkTowards = Alignment.Top
                ) +
                scaleOut(
                    targetScale = .985f,
                    animationSpec = tween(300)
                )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsPanel(
    modifier: Modifier,
    darkMode: Boolean,
    themeName: String,
    onDarkChange: () -> Unit,
    onTheme: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier
            .width(330.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(
                if (darkMode) Color(0xFF171C1A)
                else Color(0xFFF4F4F4)
            )
            .padding(17.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "NMIX Settings",
                        color = if (darkMode) Color.White
                        else Color(0xFF202321),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Personalize your interface",
                        color = Color(0xFF89928E),
                        fontSize = 9.sp
                    )
                }

                NativePressButton(
                    text = "×",
                    modifier = Modifier.size(35.dp),
                    background = Color(0xFF89928E)
                        .copy(alpha = .18f),
                    textColor = if (darkMode)
                        Color.White else Color(0xFF202321),
                    onClick = onClose
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDarkChange),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Appearance",
                        color = if (darkMode) Color.White
                        else Color(0xFF202321),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    Text(
                        if (darkMode) "Dark mode" else "Light mode",
                        color = Color(0xFF89928E),
                        fontSize = 9.sp
                    )
                }

                Box(
                    Modifier
                        .width(48.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (darkMode) Accent
                            else Color(0xFFD0D5D2)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        Modifier
                            .align(
                                if (darkMode)
                                    Alignment.CenterEnd
                                else Alignment.CenterStart
                            )
                            .size(20.dp)
                            .background(Color.White,CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Color Theme",
                color = if (darkMode) Color.White
                else Color(0xFF202321),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            val options = listOf(
                "green" to Color(0xFF319B79),
                "blue" to Color(0xFF348BB8),
                "purple" to Color(0xFF8A62C8),
                "orange" to Color(0xFFD57D35),
                "rose" to Color(0xFFC85878)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { (name,color) ->
                    val selected = name == themeName

                    Box(
                        Modifier
                            .size(if (selected) 42.dp else 38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                onTheme(name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Text(
                                "✓",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Theme and dark mode are saved on this device.",
                color = Color(0xFF89928E),
                fontSize = 8.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClockTools(
    activeMode: String,
    timerText: String,
    timerRunning: Boolean,
    clockText: String,
    stopwatchText: String,
    stopwatchRunning: Boolean,
    onTimerTap: () -> Unit,
    onTimerHold: () -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onClock: () -> Unit,
    onStopwatchTap: () -> Unit,
    onStopwatchHold: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockModeButton(
                title = "Timer",
                subtitle = if (timerRunning) "Running • $timerText"
                else "Countdown • $timerText",
                icon = "◴",
                active = activeMode == "timer",
                modifier = Modifier.weight(1f),
                onClick = onTimerTap,
                onLongClick = onTimerHold
            )

            ClockModeButton(
                title = "Clock",
                subtitle = clockText,
                icon = "◷",
                active = activeMode == "clock",
                modifier = Modifier.weight(1f),
                onClick = onClock
            )

            ClockModeButton(
                title = "Stopwatch",
                subtitle = if (stopwatchRunning)
                    "Running • $stopwatchText"
                else stopwatchText,
                icon = "◉",
                active = activeMode == "stopwatch",
                modifier = Modifier.weight(1f),
                onClick = onStopwatchTap,
                onLongClick = onStopwatchHold
            )
        }

        AnimatedVisibility(
            visible = activeMode == "timer",
            enter = fadeIn(tween(250)) +
                expandVertically(tween(300)),
            exit = fadeOut(tween(180)) +
                shrinkVertically(tween(250))
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NativePressButton(
                    text = "−",
                    modifier = Modifier.size(44.dp),
                    background = Accent,
                    textColor = Color.White,
                    onClick = onMinus
                )

                Text(
                    timerText,
                    modifier = Modifier.padding(horizontal = 22.dp),
                    color = Color(0xFF202321),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                NativePressButton(
                    text = "+",
                    modifier = Modifier.size(44.dp),
                    background = Accent,
                    textColor = Color.White,
                    onClick = onPlus
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClockModeButton(
    title: String,
    subtitle: String,
    icon: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if (pressed) .94f else 1f,
        spring(stiffness = 700f),
        label = "clockPress"
    )

    Box(
        modifier
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (active) Accent
                else Color(0xFFDEE1DF)
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            )
            .padding(10.dp)
    ) {
        Column {
            Text(
                icon,
                color = if (active) Color.White else Accent,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                title,
                color = if (active) Color.White
                else Color(0xFF202321),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                subtitle,
                color = if (active)
                    Color.White.copy(alpha = .75f)
                else Color(0xFF66706C),
                fontSize = 7.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CounterTools(
    value: Int,
    onAdd: () -> Unit,
    onMinus: () -> Unit,
    onReset: () -> Unit,
    onRandom: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            value.toString(),
            modifier = Modifier.fillMaxWidth(),
            color = Accent,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CounterButton(
                "Add",
                "Increase",
                Modifier.weight(1f),
                onAdd
            )

            CounterButton(
                "Reset",
                "Back to zero",
                Modifier.weight(1f),
                onReset
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CounterButton(
                "Random",
                "1 – 1000",
                Modifier.weight(1f),
                onRandom
            )

            CounterButton(
                "Minus",
                "Decrease",
                Modifier.weight(1f),
                onMinus
            )
        }
    }
}

@Composable
private fun CounterButton(
    title: String,
    subtitle: String,
    modifier: Modifier,
    action: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        if (pressed) .94f else 1f,
        spring(stiffness = 700f),
        label = "counterPress"
    )

    Box(
        modifier
            .scale(scale)
            .height(68.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFFDEE1DF))
            .pointerInput(action) {
                detectTapGestures(
                    onPress = {
                        pressed = true

                        val start =
                            android.os.SystemClock.elapsedRealtime()

                        val released = tryAwaitRelease()

                        val duration =
                            android.os.SystemClock.elapsedRealtime() - start

                        pressed = false

                        if (released) {
                            action()

                            // Long press gives a quick burst.
                            if (duration >= 520) {
                                repeat(
                                    (duration / 120)
                                        .toInt()
                                        .coerceAtMost(20)
                                ) {
                                    action()
                                }
                            }
                        }
                    }
                )
            }
            .padding(11.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                title,
                color = Color(0xFF202321),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                subtitle,
                color = Color(0xFF66706C),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun InstructionsPanel() {
    val items = listOf(
        "Calculator" to
            "Use the NMIX keypad for numbers and operations. Press = to calculate.",

        "Operators" to
            "NMIX supports +, −, ×, ÷ and remainder percentage (%).",

        "Editing" to
            "Use decimal, ±, backspace and AC to edit or clear calculations.",

        "Timer" to
            "Tap Timer to select it. Use − / + for five seconds and hold Timer to start or pause.",

        "Clock" to
            "Clock displays your device's local time. Full screen mode can be opened from Clock.",

        "Stopwatch" to
            "Tap Stopwatch to start or pause. Hold it to reset back to zero.",

        "Counters" to
            "Add and Minus change the value. Reset returns to zero. Random generates 1–1000.",

        "Top Screen" to
            "Use the top-left control to hide or restore the NMIX display.",

        "Settings" to
            "Use the top-right menu for appearance, themes and other personalization."
    )

    Column(
        Modifier.padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items.forEach { (title,text) ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFDEE1DF))
                    .padding(11.dp)
            ) {
                Column {
                    Text(
                        title,
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text,
                        color = Color(0xFF66706C),
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NativeCalculatorGrid(
    onPress: (String) -> Unit
) {
    val keys = listOf(
        "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "0",
        "+", "−", "×", "÷", "%",
        ".", "±", "⌫", "AC", "="
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 7.dp,
                bottom = 16.dp
            )
    ) {
        keys.chunked(5).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    val special =
                        key in listOf("+", "−", "×", "÷", "%", "=")

                    val danger = key == "AC"

                    NativePressButton(
                        text = key,
                        modifier = Modifier.size(55.dp),
                        background = when {
                            danger ->
                                Color(0xFFD83939).copy(alpha = .15f)

                            special ->
                                Accent

                            else ->
                                Color(0xFFDEE1DF)
                        },
                        textColor = when {
                            danger ->
                                Color(0xFFD83939)

                            special ->
                                Color.White

                            else ->
                                Color(0xFF202321)
                        },
                        onClick = {
                            onPress(key)
                        }
                    )
                }
            }

            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun NativePillButton(
    text: String,
    modifier: Modifier,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .96f else 1f,
        animationSpec = spring(
            dampingRatio = .65f,
            stiffness = 700f
        ),
        label = "pillPress"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NativePressButton(
    text: String,
    modifier: Modifier,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interaction = remember {
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .90f else 1f,
        animationSpec = spring(
            dampingRatio = .62f,
            stiffness = 750f
        ),
        label = "nativePress"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

