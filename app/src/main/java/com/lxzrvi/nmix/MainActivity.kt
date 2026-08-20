package com.lxzrvi.nmix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NmixApp() }
    }
}

private val Accent = Color(0xFF319B79)

@Composable
fun NmixApp() {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "nmix_preferences",
            android.content.Context.MODE_PRIVATE
        )
    }

    var started by remember {
        mutableStateOf(
            prefs.getString("home_screen", "landing") == "main"
        )
    }

    if (started) {
        NativeMainPage(
            onBack = {
                prefs.edit()
                    .putString("home_screen", "landing")
                    .apply()

                started = false
            }
        )
    } else {
        LandingScreen(
            onStart = {
                prefs.edit()
                    .putString("home_screen", "main")
                    .apply()

                started = true
            }
        )
    }
}

@Composable
private fun LandingScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    val motion = rememberInfiniteTransition(label = "movingBackground")

    val moveOne by motion.animateFloat(
        initialValue = -260f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(3900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveOne"
    )

    val moveTwo by motion.animateFloat(
        initialValue = 250f,
        targetValue = -280f,
        animationSpec = infiniteRepeatable(
            animation = tween(5100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveTwo"
    )

    val moveVertical by motion.animateFloat(
        initialValue = -190f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical"
    )

    val pulse by motion.animateFloat(
        initialValue = .92f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF010A07),
                        Color(0xFF07392B),
                        Color(0xFF0D6249),
                        Color(0xFF07382B),
                        Color(0xFF020C09)
                    )
                )
            )
    ) {
        // Soft moving gradient lights — no visible square layer edges.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = moveOne * .42f
                    translationY = moveVertical * .28f
                    scaleX = 1.65f
                    scaleY = 1.65f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF72E8C0).copy(alpha = .48f),
                            Color(0xFF35B98D).copy(alpha = .24f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = moveTwo * .40f
                    translationY = -moveVertical * .25f
                    scaleX = 1.75f
                    scaleY = 1.75f
                    rotationZ = moveOne * .018f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF13B982).copy(alpha = .38f),
                            Color(0xFF087253).copy(alpha = .20f),
                            Color.Transparent
                        ),
                        radius = 1050f
                    )
                )
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -moveOne * .30f
                    translationY = moveTwo * .20f
                    scaleX = 1.8f
                    scaleY = 1.8f
                }
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF9EF2D5).copy(alpha = .12f),
                            Color(0xFF20A77C).copy(alpha = .18f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Logo and main actions stay centered.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-55).dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EVERYTHING WITH NUMBERS",
                color = Color.White.copy(alpha = .76f),
                fontSize = 10.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "NMIX",
                color = Color.White,
                fontSize = 55.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(28.dp))

            GlassAction("Start", onStart)

            Spacer(Modifier.height(10.dp))

            GlassAction("Share") {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                    )
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share NMIX")
                )
            }
        }

        // Taller info card, deliberately separated from footer.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .padding(bottom = 70.dp)
        ) {
            AppInfoCard()
        }

        // Independent branding at absolute bottom.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NMIX",
                color = Color.White.copy(alpha = .92f),
                fontSize = 12.sp,
                letterSpacing = .7.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "  •  lxzrvi  •  © 2026",
                color = Color.White.copy(alpha = .65f),
                fontSize = 12.sp,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun GlassAction(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .95f else 1f,
        animationSpec = spring(
            dampingRatio = .62f,
            stiffness = 700f
        ),
        label = "press"
    )

    Box(
        modifier = Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .20f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = .95f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AppInfoCard() {
    val context = LocalContext.current

    val messages = remember {
        listOf(
            "NMIX brings useful number tools together in one place. Calculate everyday values, count things, track time and generate numbers without moving between multiple apps.",
            "Built around speed and simplicity, NMIX combines a calculator, timer, local clock, stopwatch, counters and random-number tools in one focused native Android experience.",
            "NMIX is designed to work offline. Its core number and time tools stay available without depending on a website, browser or continuous internet connection.",
            "EVERYTHING WITH NUMBERS is the idea behind NMIX: a clean toolbox where calculations, counting and time-based utilities can live together with a consistent interface.",
            "The interface is being designed around smooth motion, clear controls and quick interactions so common number tasks feel simple whether they take seconds or stay open longer."
        )
    }

    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4800)
            index = (index + 1) % messages.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = .21f))
            .padding(15.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "APP INFO",
                        color = Color.White.copy(alpha = .62f),
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "NMIX",
                        color = Color.White,
                        fontSize = 21.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    MiniGlassButton("Web") {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://lxzrvi.github.io/NMIX/")
                            )
                        )
                    }

                    MiniGlassButton("GitHub") {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/lxzrvi")
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Changing information
                Box(
                    modifier = Modifier
                        .weight(1.18f)
                        .height(144.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = {
                            fadeIn(tween(500)) togetherWith
                                fadeOut(tween(400))
                        },
                        label = "information"
                    ) { current ->
                        Text(
                            text = messages[current],
                            color = Color.White.copy(alpha = .91f),
                            fontSize = 10.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Technology section
                Box(
                    modifier = Modifier
                        .weight(.82f)
                        .height(144.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(11.dp)
                ) {
                    Column {
                        Text(
                            "BUILT WITH",
                            color = Color.White.copy(alpha = .65f),
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(9.dp))

                        TechPill("Kotlin")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Jetpack Compose")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Android SDK")
                        Spacer(Modifier.height(5.dp))
                        TechPill("Gradle")
                    }
                }
            }
        }
    }
}

@Composable
private fun TechPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Accent.copy(alpha = .42f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFC5F8E7),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MiniGlassButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .90f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "miniPress"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = .20f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* =========================================================
   MAIN PAGE — NATIVE NMIX
   ========================================================= */

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
private fun NativeMainPage(
    onBack: () -> Unit
) {
    var topOpen by remember { mutableStateOf(true) }
    var calculatorOpen by remember { mutableStateOf(false) }

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
            .background(Color(0xFFE0E2E1))
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
                    open = false,
                    onClick = {
                        topOpen = true
                        status = "Clock tools are coming next."
                    }
                ) {}
            }

            item {
                NativeToolSection(
                    icon = "+",
                    title = "Counters",
                    subtitle = "Count and generate",
                    open = false,
                    onClick = {
                        topOpen = true
                        status = "Counters are coming next."
                    }
                ) {}
            }

            item {
                NativeToolSection(
                    icon = "?",
                    title = "How to use NMIX",
                    subtitle = "Instructions and controls",
                    open = false,
                    onClick = {
                        topOpen = true
                        status = "Instructions are coming next."
                    }
                ) {}
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
                                colors.dark,
                                colors.accent,
                                Color(0xFF173E33)
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
                            }
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
                modifier = Modifier.size(43.dp),
                background = colors.accent.copy(alpha = .90f),
                textColor = Color.White,
                onClick = {
                    topOpen = !topOpen
                }
            )

            NativePressButton(
                text = "☰",
                modifier = Modifier.size(43.dp),
                background = colors.accent.copy(alpha = .90f),
                textColor = Color.White,
                onClick = {
                    status = "Settings will be added next."
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

