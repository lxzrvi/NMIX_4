@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.lxzrvi.nmix

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun NmixCalculator(onKey:(String)->Unit){
    val keys=listOf(
        "1","2","3","4","5",
        "6","7","8","9","0",
        "+","−","×","÷","%",
        ".","±","⌫","AC","="
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ){
        keys.chunked(5).forEach{row->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.SpaceEvenly
            ){
                row.forEach{key->
                    val type=when{
                        key in listOf("+","−","×","÷","%","=")->1
                        key=="AC"->2
                        else->0
                    }

                    NmixKey(
                        text=key,
                        modifier=Modifier.size(55.dp),
                        type=type,
                        onClick={onKey(key)}
                    )
                }
            }

            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
fun NmixClockTools(
    mode:String,
    onTimer:()->Unit,
    onTimerReset:()->Unit,
    onClock:()->Unit,
    onFullscreen:()->Unit,
    onStopwatch:()->Unit,
    onStopwatchReset:()->Unit
){
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)
    ){
        ModeRow(
            NmixIcon.TIMER,
            "Timer",
            mode=="timer",
            onTimer,
            onTimerReset
        )

        Box(Modifier.fillMaxWidth()){
            ModeRow(
                NmixIcon.CLOCK,
                "Clock",
                mode=="clock",
                onClock
            )

            NmixSmallIconButton(
                icon=NmixIcon.FULLSCREEN,
                modifier=Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end=10.dp)
                    .size(38.dp),
                selected=mode=="clock",
                onClick=onFullscreen
            )
        }

        ModeRow(
            NmixIcon.STOPWATCH,
            "Stopwatch",
            mode=="stopwatch",
            onStopwatch,
            onStopwatchReset
        )
    }
}

@Composable
private fun ModeRow(
    icon:NmixIcon,
    title:String,
    selected:Boolean,
    onClick:()->Unit,
    onLong:(()->Unit)?=null
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).965f else 1f,
        label="modePress"
    )

    val shape=RoundedCornerShape(13.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(selected)
                    p.accent.copy(alpha=.84f)
                else
                    p.accent.copy(
                        alpha=if(a.darkMode).085f else .08f
                    )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).12f else .22f
                ),
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={onLong?.invoke()}
            )
            .padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Box(
            Modifier
                .size(35.dp)
                .clip(
                    if(selected)
                        CircleShape
                    else
                        RoundedCornerShape(9.dp)
                )
                .background(
                    if(selected)
                        Color.White.copy(alpha=.92f)
                    else
                        p.accent.copy(alpha=.15f)
                ),
            contentAlignment=Alignment.Center
        ){
            NmixIcon(
                icon,
                Modifier.size(18.dp),
                p.accent
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            color=if(selected)Color.White else ui.text,
            fontSize=13.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
fun NmixCounters(
    add:()->Unit,
    reset:()->Unit,
    random:()->Unit,
    minus:()->Unit
){
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(8.dp)
        ){
            CounterButton(
                NmixIcon.PLUS,
                "Add",
                Modifier.weight(1f),
                add
            )

            CounterButton(
                NmixIcon.RESET,
                "Reset",
                Modifier.weight(1f),
                reset
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(8.dp)
        ){
            CounterButton(
                NmixIcon.RANDOM,
                "Random",
                Modifier.weight(1f),
                random
            )

            CounterButton(
                NmixIcon.MINUS,
                "Minus",
                Modifier.weight(1f),
                minus
            )
        }
    }
}

@Composable
private fun CounterButton(
    icon:NmixIcon,
    title:String,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(12.dp)

    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=if(a.darkMode).085f else .08f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).12f else .22f
                ),
                shape
            )
    ){
        NmixPressBox(
            Modifier.fillMaxSize(),
            shape,
            Color.Transparent,
            onClick
        ){
            Row(
                verticalAlignment=Alignment.CenterVertically
            ){
                NmixIcon(
                    icon,
                    Modifier.size(18.dp),
                    p.accent
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    title,
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )
            }
        }
    }
}

@Composable
fun NmixInstructions(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val accent=a.palette.accent

    val data=listOf(
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
            "Use the top-left arrow to hide or restore the NMIX display.",

        "Settings" to
            "Use the top-right menu for appearance, colors and fonts."
    )

    Column(
        Modifier.padding(11.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp)
    ){
        data.forEach{item->
            NmixGlassBox(
                Modifier.fillMaxWidth(),
                true
            ){
                Column(
                    Modifier.padding(11.dp)
                ){
                    Text(
                        item.first,
                        color=accent,
                        fontSize=11.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=a.fontFamily
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        item.second,
                        color=ui.muted,
                        fontSize=9.sp,
                        lineHeight=14.sp,
                        fontFamily=a.fontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun NmixSettings(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val p=a.palette
    val scroll=rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(
                start=14.dp,
                end=14.dp,
                top=12.dp,
                bottom=22.dp
            )
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column{
                Text(
                    "NMIX",
                    color=ui.text,
                    fontSize=17.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=NmixLogoFont
                )

                Text(
                    "Appearance Settings",
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=a.fontFamily
                )
            }
        }

        Spacer(Modifier.height(17.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical=5.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(Modifier.weight(1f)){
                Text(
                    "Appearance",
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Text(
                    if(a.darkMode)
                        "Dark mode"
                    else
                        "Light mode",
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=a.fontFamily
                )
            }

            NmixSwitch(
                a.darkMode,
                p.accent
            ){
                a.toggleDarkMode()
            }
        }

        Spacer(Modifier.height(19.dp))

        Text(
            "Color Theme",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Choose your NMIX color",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(10.dp))

        ThemeGrid()

        Spacer(Modifier.height(20.dp))

        Text(
            "Fonts",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Preview your interface typeface",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(10.dp))

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement=Arrangement.spacedBy(7.dp)
        ){
            NmixFontName.values().forEach{font->
                FontPill(
                    font=font,
                    selected=a.font==font
                ){
                    a.setFont(font)
                }
            }
        }

        Spacer(Modifier.height(17.dp))

        Text(
            "NMIX logo uses Cinzel Decorative.",
            color=ui.muted,
            fontSize=8.sp,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun ThemeGrid(){
    val a=LocalNmixAppearance.current

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement=Arrangement.spacedBy(8.dp)
    ){
        NmixThemeName.values()
            .toList()
            .chunked(2)
            .forEach{row->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(8.dp)
                ){
                    row.forEach{theme->
                        ThemeCard(
                            theme=theme,
                            selected=a.theme==theme,
                            modifier=Modifier.weight(1f)
                        ){
                            a.setTheme(theme)
                        }
                    }
                }
            }
    }
}

@Composable
private fun ThemeCard(
    theme:NmixThemeName,
    selected:Boolean,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val current=a.palette
    val ui=a.uiColors()
    val themePalette=theme.palette()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        label="themePress"
    )

    val shape=RoundedCornerShape(16.dp)

    val cardBg=
        if(a.darkMode)
            Color.White.copy(alpha=.035f)
        else
            Color.White.copy(alpha=.70f)

    val borderColor=
        if(selected)
            current.accent
        else
            current.accent.copy(
                alpha=if(a.darkMode).09f else .15f
            )

    val title=theme.name
        .lowercase()
        .replaceFirstChar{
            it.uppercase()
        }

    val hex=when(theme){
        NmixThemeName.GREEN->"#319B79"
        NmixThemeName.BLUE->"#348BB8"
        NmixThemeName.PURPLE->"#8A62C8"
        NmixThemeName.ORANGE->"#D57D35"
        NmixThemeName.ROSE->"#C85878"
        NmixThemeName.CYAN->"#26A6B5"
    }

    Column(
        modifier
            .height(142.dp)
            .scale(scale)
            .clip(shape)
            .background(cardBg)
            .border(
                if(selected)1.15.dp else .4.dp,
                borderColor,
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(7.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(themePalette.accent)
        ){
            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(9.dp)
                        .size(17.dp),
                    Color.White
                )
            }
        }

        Spacer(Modifier.height(7.dp))

        Text(
            title,
            color=ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(1.dp))

        Text(
            hex,
            color=ui.muted,
            fontSize=7.5.sp,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun FontPill(
    font:NmixFontName,
    selected:Boolean,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        label="fontPress"
    )

    val shape=RoundedCornerShape(50)

    val bg=
        if(a.darkMode)
            Color.White.copy(alpha=.035f)
        else
            Color.White.copy(alpha=.67f)

    val outline=
        if(selected)
            p.accent
        else
            p.accent.copy(
                alpha=if(a.darkMode).09f else .14f
            )

    Row(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(
                if(selected)1.1.dp else .4.dp,
                outline,
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(horizontal=14.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Text(
            font.label(),
            modifier=Modifier.weight(1f),
            color=ui.text,
            fontSize=14.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=font.family()
        )

        if(selected){
            NmixIcon(
                NmixIcon.CHECK,
                Modifier.size(15.dp),
                p.accent
            )
        }
    }
}

@Composable
private fun NmixSwitch(
    on:Boolean,
    accent:Color,
    onClick:()->Unit
){
    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).96f else 1f,
        label="switchPress"
    )

    Box(
        Modifier
            .width(49.dp)
            .height(28.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                if(on)
                    accent
                else
                    Color(0xFF6F7773).copy(alpha=.42f)
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(4.dp),
        contentAlignment=
            if(on)
                Alignment.CenterEnd
            else
                Alignment.CenterStart
    ){
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
