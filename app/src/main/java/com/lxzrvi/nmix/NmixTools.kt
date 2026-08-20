@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.lxzrvi.nmix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            icon=NmixIcon.TIMER,
            title="Timer",
            selected=mode=="timer",
            onClick=onTimer,
            onLong=onTimerReset
        )

        Box(Modifier.fillMaxWidth()){
            ModeRow(
                icon=NmixIcon.CLOCK,
                title="Clock",
                selected=mode=="clock",
                onClick=onClock
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
            icon=NmixIcon.STOPWATCH,
            title="Stopwatch",
            selected=mode=="stopwatch",
            onClick=onStopwatch,
            onLong=onStopwatchReset
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
    val ui=a.uiColors()
    val accent=a.palette.accent
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if(pressed).965f else 1f,
        label="modePress"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if(selected)accent.copy(alpha=.88f)
                else ui.accentGlassStrong
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
                    if(selected)CircleShape
                    else RoundedCornerShape(9.dp)
                )
                .background(
                    if(selected)Color.White.copy(alpha=.92f)
                    else accent.copy(alpha=.17f)
                ),
            contentAlignment=Alignment.Center
        ){
            NmixIcon(
                icon=icon,
                modifier=Modifier.size(18.dp),
                color=accent
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text=title,
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
    val ui=a.uiColors()

    NmixPressBox(
        modifier=modifier.height(64.dp),
        shape=RoundedCornerShape(12.dp),
        color=ui.accentGlassStrong,
        onClick=onClick
    ){
        Row(verticalAlignment=Alignment.CenterVertically){
            NmixIcon(
                icon,
                Modifier.size(18.dp),
                a.palette.accent
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

@Composable
fun NmixInstructions(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val accent=a.palette.accent

    val data=listOf(
        "Calculator" to "Enter numbers with the NMIX keypad. Use +, −, ×, ÷ or %. Tap = or the large display to calculate.",
        "Editing" to "Use decimal, ±, backspace and AC to edit or clear calculations.",
        "Timer" to "Tap Timer to start or pause. Hold Timer to reset to zero. Use − / + on the main display.",
        "Clock" to "Tap Clock for local time. Use the fullscreen icon for the full-screen clock.",
        "Stopwatch" to "Tap to start or pause. Hold Stopwatch to reset.",
        "Counters" to "Add and Minus change the value. Reset returns to zero. Random generates 1–1000.",
        "Top Screen" to "Use the top-left vector arrow to hide or restore the NMIX display.",
        "Settings" to "Use the top-right menu for appearance, colors and fonts."
    )

    Column(
        Modifier.padding(11.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp)
    ){
        data.forEach{item->
            NmixGlassBox(
                modifier=Modifier.fillMaxWidth(),
                accentTint=true
            ){
                Column(Modifier.padding(11.dp)){
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
    val accent=a.palette.accent
    val scroll=rememberScrollState()

    Column(
        Modifier
            .width(330.dp)
            .fillMaxHeight()
            .clip(
                RoundedCornerShape(
                    topStart=22.dp,
                    bottomStart=22.dp
                )
            )
            .background(ui.page)
            .verticalScroll(scroll)
            .padding(17.dp)
    ){
        Text(
            "NMIX Settings",
            color=ui.text,
            fontSize=15.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )

        Text(
            "Personalize your interface",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical=7.dp),
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
                    if(a.darkMode)"Dark mode" else "Light mode",
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=a.fontFamily
                )
            }

            NmixSwitch(
                on=a.darkMode,
                accent=accent,
                onClick={a.toggleDarkMode()}
            )
        }

        Spacer(Modifier.height(20.dp))

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

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(7.dp)
        ){
            NmixThemeName.values().forEach{theme->
                ThemeChoice(
                    theme=theme,
                    selected=a.theme==theme,
                    modifier=Modifier.weight(1f),
                    onClick={a.setTheme(theme)}
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Fonts",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Choose your interface typeface",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(11.dp))

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement=Arrangement.spacedBy(8.dp)
        ){
            NmixFontName.values().forEach{font->
                FontChoice(
                    font=font,
                    selected=a.font==font,
                    onClick={a.setFont(font)}
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "NMIX logo uses Cinzel Decorative.",
            color=ui.muted,
            fontSize=8.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Appearance, color and font are saved on this device.",
            color=ui.muted,
            fontSize=8.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ThemeChoice(
    theme:NmixThemeName,
    selected:Boolean,
    modifier:Modifier=Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val palette=theme.palette()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if(pressed).94f else 1f,
        spring(),
        label="themePress"
    )

    Column(
        modifier.scale(scale),
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if(selected)
                        palette.accent
                    else
                        palette.accent.copy(alpha=.72f)
                )
                .combinedClickable(
                    interactionSource=interaction,
                    indication=null,
                    onClick=onClick,
                    onLongClick={}
                ),
            contentAlignment=Alignment.Center
        ){
            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier.size(18.dp),
                    Color.White
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        Text(
            theme.name.lowercase()
                .replaceFirstChar{it.uppercase()},
            color=if(selected)palette.accent else ui.muted,
            fontSize=7.sp,
            fontWeight=if(selected)FontWeight.Bold else FontWeight.Normal,
            fontFamily=a.fontFamily,
            maxLines=1,
            textAlign=TextAlign.Center
        )
    }
}

@Composable
private fun FontChoice(
    font:NmixFontName,
    selected:Boolean,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val accent=a.palette.accent
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        label="fontPress"
    )

    val bg by animateColorAsState(
        if(selected)
            accent.copy(alpha=.88f)
        else
            ui.accentGlassStrong,
        label="fontColor"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(horizontal=16.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Text(
            text=font.label(),
            modifier=Modifier.weight(1f),
            color=if(selected)Color.White else ui.text,
            fontSize=15.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=font.family()
        )

        if(selected){
            NmixIcon(
                NmixIcon.CHECK,
                Modifier.size(17.dp),
                Color.White
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
    val interaction=remember{MutableInteractionSource()}
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
                if(on)accent
                else Color(0xFFD0D5D2)
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(4.dp),
        contentAlignment=
            if(on)Alignment.CenterEnd
            else Alignment.CenterStart
    ){
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
