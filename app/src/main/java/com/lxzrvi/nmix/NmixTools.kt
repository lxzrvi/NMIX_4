@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NmixCalculator(
    onKey:(String)->Unit
){
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
        keys.chunked(5)
            .forEach{row->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.SpaceEvenly
                ){
                    row.forEach{key->
                        val type=when{
                            key in listOf(
                                "+","−","×",
                                "÷","%","="
                            )->1

                            key=="AC"->2
                            else->0
                        }

                        NmixKey(
                            text=key,
                            modifier=
                                Modifier.size(55.dp),
                            type=type,
                            onClick={
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
        verticalArrangement=
            Arrangement.spacedBy(8.dp)
    ){
        ModeRow(
            icon=NmixIcon.TIMER,
            title="Timer",
            selected=mode=="timer",
            onClick=onTimer,
            onLong=onTimerReset
        )

        Box(
            Modifier.fillMaxWidth()
        ){
            ModeRow(
                icon=NmixIcon.CLOCK,
                title="Clock",
                selected=mode=="clock",
                onClick=onClock
            )

            NmixSmallIconButton(
                icon=NmixIcon.FULLSCREEN,
                modifier=
                    Modifier
                        .align(
                            Alignment.CenterEnd
                        )
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
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).965f else 1f,
        label="modePress"
    )

    val shape=
        RoundedCornerShape(13.dp)

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
                        alpha=
                            if(a.darkMode)
                                .085f
                            else
                                .08f
                    )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .12f
                        else
                            .22f
                ),
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick={
                    haptic(onClick)
                },
                onLongClick={
                    onLong?.let{
                        haptic(it)
                    }
                }
            )
            .padding(horizontal=13.dp),
        verticalAlignment=
            Alignment.CenterVertically
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
            contentAlignment=
                Alignment.Center
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
            color=
                if(selected)
                    Color.White
                else
                    ui.text,
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
        verticalArrangement=
            Arrangement.spacedBy(8.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(8.dp)
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
            horizontalArrangement=
                Arrangement.spacedBy(8.dp)
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
    val haptic=rememberNmixHapticAction()

    val shape=
        RoundedCornerShape(12.dp)

    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .085f
                        else
                            .08f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .12f
                        else
                            .22f
                ),
                shape
            )
    ){
        NmixPressBox(
            Modifier.fillMaxSize(),
            shape,
            Color.Transparent,
            {
                haptic(onClick)
            }
        ){
            Row(
                verticalAlignment=
                    Alignment.CenterVertically
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

/*
 * ==================================================
 * HELP
 * ==================================================
 */

private data class HelpItem(
    val title:String,
    val detail:String
)

@Composable
fun NmixInstructions(){
    val items=remember{
        listOf(
            HelpItem(
                "Calculator",
                "Enter numbers with the native keypad. Use +, −, ×, ÷ or %. Tap = or the Display when complete."
            ),
            HelpItem(
                "Display",
                "The large Display shows results and live values. Drag the four-dot handle to resize it."
            ),
            HelpItem(
                "Editing",
                "Use decimal, ±, backspace and AC. Calculator fields smoothly change placement while resizing."
            ),
            HelpItem(
                "Timer",
                "Tap Timer to start or pause. Hold Timer to reset. Use − and + on the Display for five-second changes."
            ),
            HelpItem(
                "Clock",
                "Tap Clock for local time. Use fullscreen for the customizable Fullscreen Clock."
            ),
            HelpItem(
                "Fullscreen",
                "Fullscreen Clock follows NMIX appearance and animation preferences."
            ),
            HelpItem(
                "Stopwatch",
                "Tap Stopwatch to start or pause. Hold it to reset."
            ),
            HelpItem(
                "Counters",
                "Add, Minus, Reset and Random operate through the main Display."
            ),
            HelpItem(
                "Appearance",
                "Settings controls appearance, colors, animation, fonts, haptics and launcher icon."
            ),
            HelpItem(
                "Navigation",
                "Use the top-left arrow to collapse the Display. Back to the Start is at the page end."
            )
        )
    }

    var selected by remember{
        mutableStateOf<String?>(null)
    }

    Column(
        Modifier.padding(11.dp),
        verticalArrangement=
            Arrangement.spacedBy(8.dp)
    ){
        items.chunked(5)
            .forEach{rowItems->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(6.dp)
                ){
                    rowItems.forEach{item->
                        HelpTitleBox(
                            item,
                            selected==item.title,
                            Modifier.weight(1f)
                        ){
                            selected=
                                if(selected==item.title)
                                    null
                                else
                                    item.title
                        }
                    }

                    repeat(5-rowItems.size){
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

        AnimatedContent(
            targetState=selected,
            transitionSpec={
                (
                    fadeIn(
                        tween(
                            320,
                            easing=EaseOutCubic
                        )
                    )+
                    slideInVertically(
                        initialOffsetY={it/8},
                        animationSpec=tween(
                            340,
                            easing=EaseOutCubic
                        )
                    )
                ) togetherWith (
                    fadeOut(tween(190))+
                    slideOutVertically(
                        targetOffsetY={-it/10},
                        animationSpec=tween(240)
                    )
                )
            },
            label="helpDetail"
        ){title->
            if(title!=null){
                HelpDetailBox(
                    items.first{
                        it.title==title
                    }
                )
            }else{
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}

@Composable
private fun HelpTitleBox(
    item:HelpItem,
    selected:Boolean,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).95f else 1f,
        spring(
            dampingRatio=.74f,
            stiffness=650f
        ),
        label="helpPress"
    )

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(62.dp)
            .scale(scale)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .17f
                        else if(a.darkMode)
                            .075f
                        else
                            .08f
                )
            )
            .border(
                if(selected)1.dp else .45.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .72f
                        else if(a.darkMode)
                            .13f
                        else
                            .23f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                haptic(onClick)
            }
            .padding(horizontal=4.dp),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            item.title,
            color=
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=7.4.sp,
            lineHeight=9.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily,
            textAlign=TextAlign.Center,
            maxLines=2
        )
    }
}

@Composable
private fun HelpDetailBox(
    item:HelpItem
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(14.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .065f
                        else
                            .07f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .24f
                ),
                shape
            )
            .padding(12.dp)
    ){
        Text(
            item.title.uppercase(),
            color=p.accent,
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(5.dp))

        Text(
            item.detail,
            color=ui.muted,
            fontSize=9.5.sp,
            lineHeight=15.sp,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * SETTINGS
 * ==================================================
 */

@Composable
fun NmixSettings(
    onCustomColor:()->Unit={}
){
    val context=LocalContext.current
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val scroll=
        rememberScrollState()

    var detail by remember{
        mutableIntStateOf(0)
    }

    var iconNotice by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        while(true){
            delay(3200)
            detail=(detail+1)%2
        }
    }

    fun applyIcon(){
        val success=
            NmixIconManager.applyFromState(
                context,
                a
            )

        if(success){
            iconNotice=true
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(
                start=13.dp,
                end=13.dp,
                top=12.dp
            )
    ){
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

        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if(a.darkMode)
                                Color(0xFF151917)
                                    .copy(alpha=.26f)
                            else
                                Color(0xFFF0F4F2)
                                    .copy(alpha=.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll)
                .padding(bottom=24.dp)
        ){
            SettingsToggleRow(
                title="Appearance",
                detail=
                    if(a.darkMode)
                        "Dark mode"
                    else
                        "Light mode",
                enabled=a.darkMode
            ){
                haptic{
                    a.toggleDarkMode()
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingsToggleRow(
                title="Colors",
                detail=
                    if(a.colorEnabled)
                        "Color controls enabled"
                    else
                        "Default Green",
                enabled=a.colorEnabled
            ){
                haptic{
                    a.setColorEnabled(
                        !a.colorEnabled
                    )
                }
            }

            AnimatedVisibility(
                visible=a.colorEnabled,
                enter=
                    expandVertically(
                        tween(
                            330,
                            easing=EaseOutCubic
                        )
                    )+
                    fadeIn(tween(220)),
                exit=
                    shrinkVertically(
                        tween(
                            280,
                            easing=EaseInOutCubic
                        )
                    )+
                    fadeOut(tween(160))
            ){
                Column{
                    Spacer(Modifier.height(12.dp))

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

                    ThemeGrid(detail)

                    Spacer(Modifier.height(9.dp))

                    NmixCustomThemeButton(
                        onClick=onCustomColor
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            SettingsToggleRow(
                title="Animation",
                detail=
                    if(a.animationEnabled)
                        "${a.animation.label()} • ${a.animationQuantity} elements"
                    else
                        "Motion disabled",
                enabled=a.animationEnabled
            ){
                haptic{
                    a.setAnimationEnabled(
                        !a.animationEnabled
                    )
                }
            }

            AnimatedVisibility(
                visible=a.animationEnabled,
                enter=
                    expandVertically(
                        tween(
                            340,
                            easing=EaseOutCubic
                        )
                    )+
                    fadeIn(tween(230)),
                exit=
                    shrinkVertically(
                        tween(
                            290,
                            easing=EaseInOutCubic
                        )
                    )+
                    fadeOut(tween(170))
            ){
                Column{
                    Spacer(Modifier.height(12.dp))
                    NmixAnimationSettings()
                }
            }

            Spacer(Modifier.height(17.dp))

            SettingsToggleRow(
                title="Fonts",
                detail=
                    if(a.fontEnabled)
                        a.font.label()
                    else
                        "Default Inter",
                enabled=a.fontEnabled
            ){
                haptic{
                    a.setFontEnabled(
                        !a.fontEnabled
                    )
                }
            }

            AnimatedVisibility(
                visible=a.fontEnabled,
                enter=
                    expandVertically(
                        tween(
                            330,
                            easing=EaseOutCubic
                        )
                    )+
                    fadeIn(tween(220)),
                exit=
                    shrinkVertically(
                        tween(
                            280,
                            easing=EaseInOutCubic
                        )
                    )+
                    fadeOut(tween(160))
            ){
                Column{
                    Spacer(Modifier.height(11.dp))

                    Text(
                        "Preview your interface typeface",
                        color=ui.muted,
                        fontSize=9.sp,
                        fontFamily=a.fontFamily
                    )

                    Spacer(Modifier.height(9.dp))

                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement=
                            Arrangement.spacedBy(7.dp)
                    ){
                        NmixFontName.values()
                            .forEach{font->
                                FontPill(
                                    font=font,
                                    selected=
                                        a.font==font
                                ){
                                    haptic{
                                        a.setFont(font)
                                    }
                                }
                            }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "NMIX logo uses Cinzel Decorative.",
                        color=ui.muted,
                        fontSize=8.sp,
                        fontFamily=a.fontFamily
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            SettingsToggleRow(
                title="Vibration",
                detail="Soft interface haptics",
                enabled=a.hapticsEnabled
            ){
                /*
                 * Perform old-state haptic first so
                 * enabling gives immediate feedback.
                 */
                a.setHapticsEnabled(
                    !a.hapticsEnabled
                )

                if(a.hapticsEnabled){
                    haptic{}
                }
            }

            Spacer(Modifier.height(20.dp))

            AppIconSettings(
                iconNotice=iconNotice,
                onDismissNotice={
                    iconNotice=false
                },
                applyIcon={
                    haptic{
                        applyIcon()
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title:String,
    detail:String,
    enabled:Boolean,
    onToggle:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(14.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.88f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .035f
                        else
                            .025f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .21f
                ),
                shape
            )
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onToggle
            )
            .padding(horizontal=11.dp),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Column(
            Modifier.weight(1f)
        ){
            Text(
                title,
                color=ui.text,
                fontSize=11.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Text(
                detail,
                color=ui.muted,
                fontSize=8.sp,
                fontFamily=a.fontFamily
            )
        }

        NmixSwitch(
            on=enabled,
            accent=p.accent,
            onClick=onToggle
        )
    }
}

/*
 * ==================================================
 * APP ICON
 * ==================================================
 */

@Composable
private fun AppIconSettings(
    iconNotice:Boolean,
    onDismissNotice:()->Unit,
    applyIcon:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(16.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.80f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.89f)
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .15f
                        else
                            .22f
                ),
                shape
            )
            .padding(11.dp)
    ){
        Text(
            "APP ICON",
            color=p.accent,
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=1.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(8.dp))

        SettingsToggleRow(
            title="Set App Icon",
            detail=
                if(a.appIconEnabled)
                    "Alternate NMIX icon active"
                else
                    "Use default app icon",
            enabled=a.appIconEnabled
        ){
            a.setAppIconEnabled(
                !a.appIconEnabled
            )

            applyIcon()
        }

        AnimatedVisibility(
            visible=a.appIconEnabled,
            enter=
                expandVertically(
                    tween(
                        330,
                        easing=EaseOutCubic
                    )
                )+
                fadeIn(tween(220)),
            exit=
                shrinkVertically(
                    tween(
                        280,
                        easing=EaseInOutCubic
                    )
                )+
                fadeOut(tween(160))
        ){
            Column{
                Spacer(Modifier.height(9.dp))

                SettingsToggleRow(
                    title="Follow Theme",
                    detail=
                        if(a.iconFollowTheme)
                            "Preset theme controls icon"
                        else
                            "Choose icon color manually",
                    enabled=a.iconFollowTheme
                ){
                    a.setIconFollowTheme(
                        !a.iconFollowTheme
                    )

                    applyIcon()
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Icon Color",
                    color=ui.text,
                    fontSize=10.sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Spacer(Modifier.height(7.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(6.dp)
                ){
                    NmixThemeName.values()
                        .forEach{theme->
                            IconColorChoice(
                                theme=theme,
                                selected=
                                    a.iconTheme==theme,
                                enabled=
                                    !a.iconFollowTheme,
                                modifier=
                                    Modifier.weight(1f)
                            ){
                                a.setIconTheme(theme)
                                applyIcon()
                            }
                        }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Icon Style",
                    color=ui.text,
                    fontSize=10.sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Spacer(Modifier.height(7.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    IconStyleChoice(
                        text="Adaptive",
                        selected=
                            a.iconStyle==
                                NmixIconStyle.ADAPTIVE,
                        modifier=Modifier.weight(1f)
                    ){
                        a.setIconStyle(
                            NmixIconStyle.ADAPTIVE
                        )

                        applyIcon()
                    }

                    IconStyleChoice(
                        text="Round",
                        selected=
                            a.iconStyle==
                                NmixIconStyle.ROUND,
                        modifier=Modifier.weight(1f)
                    ){
                        a.setIconStyle(
                            NmixIconStyle.ROUND
                        )

                        applyIcon()
                    }
                }
            }
        }

        AnimatedVisibility(
            visible=iconNotice
        ){
            Column{
                Spacer(Modifier.height(10.dp))

                val noticeShape=
                    RoundedCornerShape(11.dp)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(noticeShape)
                        .background(
                            p.accent.copy(alpha=.08f)
                        )
                        .border(
                            .4.dp,
                            p.accent.copy(alpha=.18f),
                            noticeShape
                        )
                        .padding(9.dp),
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    Text(
                        "Icon updated. Launcher may need a moment to refresh.",
                        modifier=Modifier.weight(1f),
                        color=ui.muted,
                        fontSize=7.5.sp,
                        lineHeight=10.sp,
                        fontFamily=a.fontFamily
                    )

                    Text(
                        "OK",
                        color=p.accent,
                        fontSize=8.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=a.fontFamily,
                        modifier=
                            Modifier.clickable(
                                interactionSource=remember{
                                    MutableInteractionSource()
                                },
                                indication=null,
                                onClick=onDismissNotice
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun IconColorChoice(
    theme:NmixThemeName,
    selected:Boolean,
    enabled:Boolean,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val color=theme.palette().accent

    val shape=
        CircleShape

    Box(
        modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                color.copy(
                    alpha=
                        if(enabled)
                            1f
                        else
                            .38f
                )
            )
            .then(
                if(selected){
                    Modifier.border(
                        2.dp,
                        if(a.darkMode)
                            Color.White
                        else
                            Color(0xFF252A27),
                        shape
                    )
                }else{
                    Modifier
                }
            )
            .clickable(
                enabled=enabled,
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            )
    )
}

@Composable
private fun IconStyleChoice(
    text:String,
    selected:Boolean,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(38.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .15f
                        else
                            .055f
                )
            )
            .border(
                if(selected)
                    1.dp
                else
                    .4.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .72f
                        else
                            .16f
                ),
                shape
            )
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * THEMES
 * ==================================================
 */

@Composable
private fun ThemeGrid(
    detail:Int
){
    val a=LocalNmixAppearance.current
    val context=LocalContext.current

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement=
            Arrangement.spacedBy(8.dp)
    ){
        NmixThemeName.values()
            .toList()
            .chunked(3)
            .forEach{row->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    row.forEach{theme->
                        ThemeCard(
                            theme=theme,
                            selected=
                                !a.usingCustomColor &&
                                a.theme==theme,
                            detail=detail,
                            modifier=
                                Modifier.weight(1f)
                        ){
                            a.setTheme(theme)

                            if(
                                a.appIconEnabled &&
                                a.iconFollowTheme
                            ){
                                NmixIconManager
                                    .applyFromState(
                                        context,
                                        a
                                    )
                            }
                        }
                    }
                }
            }
    }
}

private fun themeHex(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->"#319B79"
    NmixThemeName.BLUE->"#348BB8"
    NmixThemeName.PURPLE->"#8A62C8"
    NmixThemeName.ORANGE->"#D57D35"
    NmixThemeName.ROSE->"#C85878"
    NmixThemeName.CYAN->"#26A6B5"
}

private fun themeMood(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->"Balanced • Focused"
    NmixThemeName.BLUE->"Clear • Productive"
    NmixThemeName.PURPLE->"Creative • Calm"
    NmixThemeName.ORANGE->"Energetic • Warm"
    NmixThemeName.ROSE->"Soft • Expressive"
    NmixThemeName.CYAN->"Fresh • Precise"
}

private fun themeDetail(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->"Calm natural tone"
    NmixThemeName.BLUE->"Clean focused energy"
    NmixThemeName.PURPLE->"Quiet creative mood"
    NmixThemeName.ORANGE->"Bright active feel"
    NmixThemeName.ROSE->"Warm expressive mood"
    NmixThemeName.CYAN->"Crisp modern clarity"
}

@Composable
private fun ThemeCard(
    theme:NmixThemeName,
    selected:Boolean,
    detail:Int,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val current=a.palette
    val ui=a.uiColors()
    val palette=theme.palette()
    val haptic=rememberNmixHapticAction()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        label="themePress"
    )

    val shape=
        RoundedCornerShape(14.dp)

    val bg=
        if(a.darkMode)
            Color.White.copy(alpha=.035f)
        else
            Color(0xFFE8ECEA)
                .copy(alpha=.86f)

    val outline=
        if(selected)
            current.accent
        else
            current.accent.copy(
                alpha=
                    if(a.darkMode)
                        .08f
                    else
                        .14f
            )

    val name=
        theme.name
            .lowercase()
            .replaceFirstChar{
                it.uppercase()
            }

    Column(
        modifier
            .height(132.dp)
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(
                if(selected)
                    1.1.dp
                else
                    .4.dp,
                outline,
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick={
                    haptic(onClick)
                },
                onLongClick={}
            )
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    palette.accent
                ),
            contentAlignment=
                Alignment.Center
        ){
            Text(
                themeHex(theme),
                color=Color.White,
                fontSize=7.5.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=a.fontFamily
            )

            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(13.dp),
                    Color.White
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            name,
            color=ui.text,
            fontSize=10.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(2.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(34.dp)
        ){
            AnimatedContent(
                targetState=detail,
                transitionSpec={
                    fadeIn(tween(320)) togetherWith
                        fadeOut(tween(220))
                },
                label="themeDetail"
            ){state->
                Text(
                    if(state==0)
                        themeMood(theme)
                    else
                        themeDetail(theme),
                    color=ui.muted,
                    fontSize=7.1.sp,
                    lineHeight=9.sp,
                    fontFamily=a.fontFamily,
                    maxLines=3
                )
            }
        }
    }
}

/*
 * ==================================================
 * FONTS
 * ==================================================
 */

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

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        label="fontPress"
    )

    val shape=
        RoundedCornerShape(50)

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color.White.copy(alpha=.035f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.84f)
            )
            .border(
                if(selected)
                    1.05.dp
                else
                    .4.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .78f
                        else if(a.darkMode)
                            .08f
                        else
                            .14f
                ),
                shape
            )
            .combinedClickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(horizontal=13.dp),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Text(
            font.label(),
            Modifier.weight(1f),
            color=ui.text,
            fontSize=13.5.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=font.family()
        )

        if(selected){
            NmixIcon(
                NmixIcon.CHECK,
                Modifier.size(14.dp),
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

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).96f else 1f,
        label="switchPress"
    )

    val alignment by
        animateFloatAsState(
            if(on)1f else 0f,
            tween(
                220,
                easing=EaseInOutCubic
            ),
            label="switchPosition"
        )

    Box(
        Modifier
            .width(49.dp)
            .height(28.dp)
            .scale(scale)
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                if(on)
                    accent
                else
                    Color(0xFF6F7773)
                        .copy(alpha=.36f)
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
            .padding(4.dp)
    ){
        Box(
            Modifier
                .offset(
                    x=(21f*alignment).dp
                )
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
