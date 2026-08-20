@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.lxzrvi.nmix

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NmixCalculator(onKey: (String) -> kotlin.Unit) {
    val keys = listOf(
        "1","2","3","4","5",
        "6","7","8","9","0",
        "+","−","×","÷","%",
        ".","±","⌫","AC","="
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        keys.chunked(5).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    val type = when {
                        key in listOf("+","−","×","÷","%","=") -> 1
                        key == "AC" -> 2
                        else -> 0
                    }

                    NmixKey(
                        text = key,
                        modifier = Modifier.size(55.dp),
                        type = type,
                        onClick = { onKey(key) }
                    )
                }
            }

            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
fun NmixClockTools(
    mode: String,
    onTimer: () -> kotlin.Unit,
    onTimerReset: () -> kotlin.Unit,
    onClock: () -> kotlin.Unit,
    onFullscreen: () -> kotlin.Unit,
    onStopwatch: () -> kotlin.Unit,
    onStopwatchReset: () -> kotlin.Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeRow(
            icon = NmixIcon.TIMER,
            title = "Timer",
            selected = mode == "timer",
            onClick = onTimer,
            onLong = onTimerReset
        )

        Box(Modifier.fillMaxWidth()) {
            ModeRow(
                icon = NmixIcon.CLOCK,
                title = "Clock",
                selected = mode == "clock",
                onClick = onClock
            )

            NmixSmallIconButton(
                icon = NmixIcon.FULLSCREEN,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(38.dp),
                selected = mode == "clock",
                onClick = onFullscreen
            )
        }

        ModeRow(
            icon = NmixIcon.STOPWATCH,
            title = "Stopwatch",
            selected = mode == "stopwatch",
            onClick = onStopwatch,
            onLong = onStopwatchReset
        )
    }
}

@Composable
private fun ModeRow(
    icon: NmixIcon,
    title: String,
    selected: Boolean,
    onClick: () -> kotlin.Unit,
    onLong: (() -> kotlin.Unit)? = null
) {
    val appearance = LocalNmixAppearance.current
    val ui = appearance.uiColors()
    val accent = appearance.palette.accent

    val interaction = remember {
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) .965f else 1f,
        label = "modePress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (selected) {
                    accent.copy(alpha = .88f)
                } else {
                    ui.accentGlassStrong
                }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(
                    if (selected) {
                        CircleShape
                    } else {
                        RoundedCornerShape(9.dp)
                    }
                )
                .background(
                    if (selected) {
                        Color.White.copy(alpha = .92f)
                    } else {
                        accent.copy(alpha = .17f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            NmixIcon(
                icon = icon,
                modifier = Modifier.size(18.dp),
                color = accent
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            color = if (selected) Color.White else ui.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun NmixCounters(
    add: () -> kotlin.Unit,
    reset: () -> kotlin.Unit,
    random: () -> kotlin.Unit,
    minus: () -> kotlin.Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CounterButton(
                icon = NmixIcon.PLUS,
                title = "Add",
                modifier = Modifier.weight(1f),
                onClick = add
            )

            CounterButton(
                icon = NmixIcon.RESET,
                title = "Reset",
                modifier = Modifier.weight(1f),
                onClick = reset
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CounterButton(
                icon = NmixIcon.RANDOM,
                title = "Random",
                modifier = Modifier.weight(1f),
                onClick = random
            )

            CounterButton(
                icon = NmixIcon.MINUS,
                title = "Minus",
                modifier = Modifier.weight(1f),
                onClick = minus
            )
        }
    }
}

@Composable
private fun CounterButton(
    icon: NmixIcon,
    title: String,
    modifier: Modifier,
    onClick: () -> kotlin.Unit
) {
    val appearance = LocalNmixAppearance.current
    val ui = appearance.uiColors()

    NmixPressBox(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = ui.accentGlassStrong,
        onClick = onClick,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                NmixIcon(
                    icon = icon,
                    modifier = Modifier.size(18.dp),
                    color = appearance.palette.accent
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = title,
                    color = ui.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
fun NmixInstructions() {
    val appearance = LocalNmixAppearance.current
    val ui = appearance.uiColors()
    val accent = appearance.palette.accent

    val data = listOf(
        "Calculator" to
            "Enter numbers with the NMIX keypad. Use +, −, ×, ÷ or %. Tap = or the large display to calculate.",

        "Editing" to
            "Use decimal, ±, backspace and AC to edit or clear calculations.",

        "Timer" to
            "Tap Timer to start or pause. Hold Timer to reset to zero. Use − / + on the main display.",

        "Clock" to
            "Tap Clock for local time. Use the fullscreen icon for the full-screen clock.",

        "Stopwatch" to
            "Tap to start or pause. Hold Stopwatch to reset.",

        "Counters" to
            "Add and Minus change the value. Reset returns to zero. Random generates 1–1000.",

        "Top Screen" to
            "Use the top-left vector arrow to hide or restore the NMIX display.",

        "Settings" to
            "Use the top-right menu for dark mode and color themes."
    )

    Column(
        Modifier.padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        data.forEach { item ->
            NmixGlassBox(
                modifier = Modifier.fillMaxWidth(),
                accentTint = true
            ) {
                Column(
                    Modifier.padding(11.dp)
                ) {
                    Text(
                        text = item.first,
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = item.second,
                        color = ui.muted,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NmixSettings() {
    val appearance = LocalNmixAppearance.current
    val ui = appearance.uiColors()
    val accent = appearance.palette.accent

    Column(
        modifier = Modifier
            .width(330.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 22.dp,
                    bottomStart = 22.dp
                )
            )
            .background(ui.page)
            .padding(17.dp)
    ) {
        Text(
            text = "NMIX Settings",
            color = ui.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Personalize your interface",
            color = ui.muted,
            fontSize = 9.sp
        )

        Spacer(Modifier.height(20.dp))

        val appearanceInteraction = remember {
            MutableInteractionSource()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = appearanceInteraction,
                    indication = null,
                    onClick = {
                        appearance.toggleDarkMode()
                    },
                    onLongClick = {}
                )
                .padding(vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = "Appearance",
                    color = ui.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (appearance.darkMode) {
                        "Dark mode"
                    } else {
                        "Light mode"
                    },
                    color = ui.muted,
                    fontSize = 9.sp
                )
            }

            NmixSwitch(
                on = appearance.darkMode,
                accent = accent,
                onClick = {
                    appearance.toggleDarkMode()
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Color Theme",
            color = ui.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Choose your NMIX color",
            color = ui.muted,
            fontSize = 9.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NmixThemeName.values().forEach { theme ->
                val selected = appearance.theme == theme

                NmixPressBox(
                    modifier = Modifier.size(
                        if (selected) 42.dp else 38.dp
                    ),
                    shape = CircleShape,
                    color = theme.palette().accent,
                    onClick = {
                        appearance.setTheme(theme)
                    },
                    content = {
                        if (selected) {
                            NmixIcon(
                                icon = NmixIcon.CHECK,
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Theme and appearance are saved on this device.",
            color = ui.muted,
            fontSize = 8.sp
        )
    }
}

@Composable
private fun NmixSwitch(
    on: Boolean,
    accent: Color,
    onClick: () -> kotlin.Unit
) {
    Box(
        modifier = Modifier
            .width(49.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (on) {
                    accent
                } else {
                    Color(0xFFD0D5D2)
                }
            )
            .padding(4.dp),
        contentAlignment = if (on) {
            Alignment.CenterEnd
        } else {
            Alignment.CenterStart
        }
    ) {
        NmixPressBox(
            modifier = Modifier.size(20.dp),
            shape = CircleShape,
            color = Color.White,
            onClick = onClick,
            content = {}
        )
    }
}
