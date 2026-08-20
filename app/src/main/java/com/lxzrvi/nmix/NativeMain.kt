package com.lxzrvi.nmix

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

