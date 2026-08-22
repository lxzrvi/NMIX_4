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
        keys.chunked(5).forEach{row->
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
        targetValue=
            if(pressed)
                .965f
            else
                1f,
        animationSpec=spring(
            dampingRatio=.74f,
            stiffness=620f
        ),
        label="modePress"
    )

    val selectedProgress by
        animateFloatAsState(
            targetValue=
                if(selected)1f else 0f,
            animationSpec=tween(220),
            label="modeSelected"
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
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.91f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .035f+
                                selectedProgress*.05f
                        else
                            .02f+
                                selectedProgress*.035f
                )
            )
            .border(
                (
                    .45f+
                        selectedProgress*.55f
                ).dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f+
                                selectedProgress*.32f
                        else
                            .22f+
                                selectedProgress*.30f
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
                    p.accent.copy(
                        alpha=
                            if(selected)
                                .20f
                            else
                                .12f
                    )
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

        Spacer(
            Modifier.width(12.dp)
        )

        Text(
            title,
            color=ui.text,
            fontSize=13.sp,
            fontWeight=
                FontWeight.SemiBold,
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

    val shape=
        RoundedCornerShape(12.dp)

    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.91f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .04f
                        else
                            .02f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .22f
                ),
                shape
            )
    ){
        NmixPressBox(
            modifier=Modifier.fillMaxSize(),
            shape=shape,
            color=Color.Transparent,
            onClick=onClick
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

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    title,
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=
                        FontWeight.SemiBold,
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
                "Use decimal, ±, backspace and AC. Calculator fields change placement while resizing."
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
                "Settings controls colors, fonts, vibration, animation and launcher icon."
            ),
            HelpItem(
                "Navigation",
                "Use the top-left control to collapse the Display."
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
                            item=item,
                            selected=
                                selected==
                                    item.title,
                            modifier=
                                Modifier.weight(1f)
                        ){
                            selected=
                                if(
                                    selected==
                                    item.title
                                )
                                    null
                                else
                                    item.title
                        }
                    }

                    repeat(
                        5-rowItems.size
                    ){
                        Spacer(
                            Modifier.weight(1f)
                        )
                    }
                }
            }

        AnimatedContent(
            targetState=selected,
            transitionSpec={
                fadeIn(
                    tween(260)
                ) togetherWith
                    fadeOut(
                        tween(170)
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
                Spacer(
                    Modifier.height(0.dp)
                )
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

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .95f
                else
                    1f,
            label="helpPress"
        )

    val selectedProgress by
        animateFloatAsState(
            targetValue=
                if(selected)1f else 0f,
            animationSpec=tween(220),
            label="helpSelected"
        )

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(62.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.91f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        .02f+
                            selectedProgress*.055f
                )
            )
            .border(
                (
                    .45f+
                        selectedProgress*.55f
                ).dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .52f
                        else if(a.darkMode)
                            .14f
                        else
                            .22f
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
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.91f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=.025f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .15f
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

        Spacer(
            Modifier.height(5.dp)
        )

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

    var pendingIconTheme by remember{
        mutableStateOf<NmixThemeName?>(null)
    }

    var iconCountdown by remember{
        mutableIntStateOf(-1)
    }

    var pendingApplyIcon by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        while(true){
            delay(3200)

            detail=
                (detail+1)%2
        }
    }

    /*
     * Mandatory 3 -> 2 -> 1 -> 0.
     * Alias is changed only after 0 has been shown.
     */
    LaunchedEffect(
        pendingApplyIcon,
        iconCountdown
    ){
        if(!pendingApplyIcon){
            return@LaunchedEffect
        }

        when{
            iconCountdown>0->{
                delay(1000)
                iconCountdown--
            }

            iconCountdown==0->{
                delay(650)

                NmixIconManager.applyFromState(
                    context,
                    a
                )

                pendingApplyIcon=false
                iconCountdown=-1
            }
        }
    }

    fun beginIconApply(){
        pendingApplyIcon=true
        iconCountdown=3
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
                .height(15.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if(a.darkMode)
                                Color(0xFF151917)
                                    .copy(alpha=.25f)
                            else
                                Color(0xFFF4F6F5)
                                    .copy(alpha=.34f),

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
                .padding(
                    top=10.dp,
                    bottom=26.dp
                )
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

            Spacer(
                Modifier.height(14.dp)
            )

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
                visible=a.colorEnabled
            ){
                Column{
                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Text(
                        "Color Theme",
                        color=ui.text,
                        fontSize=12.sp,
                        fontWeight=
                            FontWeight.SemiBold,
                        fontFamily=a.fontFamily
                    )

                    Text(
                        "Choose your NMIX color",
                        color=ui.muted,
                        fontSize=9.sp,
                        fontFamily=a.fontFamily
                    )

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    ThemeGrid(
                        detail=detail,
                        onThemeChosen={
                            theme->

                            a.setTheme(theme)

                            if(a.appIconEnabled){
                                pendingIconTheme=
                                    theme
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible=
                            pendingIconTheme!=null &&
                                a.appIconEnabled
                    ){
                        Column{
                            Spacer(
                                Modifier.height(8.dp)
                            )

                            ApplyIconPrompt(
                                onNo={
                                    pendingIconTheme=null
                                },
                                onYes={
                                    val selected=
                                        pendingIconTheme

                                    if(selected!=null){
                                        a.setIconTheme(
                                            selected
                                        )

                                        pendingIconTheme=null
                                        beginIconApply()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(9.dp)
                    )

                    NmixCustomThemeButton(
                        onClick=onCustomColor
                    )
                }
            }

            Spacer(
                Modifier.height(17.dp)
            )

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
                visible=a.fontEnabled
            ){
                Column{
                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Column(
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
                }
            }

            Spacer(
                Modifier.height(17.dp)
            )

            /*
             * VIBRATION
             */
            SettingsToggleRow(
                title="Vibration",
                detail=
                    if(a.hapticsEnabled)
                        "${a.hapticStrength.label()} feedback"
                    else
                        "Haptics disabled",
                enabled=
                    a.hapticsEnabled
            ){
                val enable=
                    !a.hapticsEnabled

                a.setHapticsEnabled(
                    enable
                )

                if(enable){
                    haptic{}
                }
            }

            AnimatedVisibility(
                visible=
                    a.hapticsEnabled
            ){
                Column{
                    Spacer(
                        Modifier.height(9.dp)
                    )

                    HapticChoice(
                        strength=
                            NmixHapticStrength.SOFT,
                        selected=
                            a.hapticStrength==
                                NmixHapticStrength.SOFT
                    )

                    Spacer(
                        Modifier.height(7.dp)
                    )

                    HapticChoice(
                        strength=
                            NmixHapticStrength.MEDIUM,
                        selected=
                            a.hapticStrength==
                                NmixHapticStrength.MEDIUM
                    )

                    Spacer(
                        Modifier.height(7.dp)
                    )

                    HapticChoice(
                        strength=
                            NmixHapticStrength.HARD,
                        selected=
                            a.hapticStrength==
                                NmixHapticStrength.HARD
                    )
                }
            }

            Spacer(
                Modifier.height(17.dp)
            )

            /*
             * ANIMATION:
             * deliberately between Vibration and
             * App Icon.
             */
            SettingsToggleRow(
                title="Animation",
                detail=
                    if(a.animationEnabled)
                        "${a.animation.label()} • ${a.animationQuantity} elements"
                    else
                        "Motion disabled",
                enabled=
                    a.animationEnabled
            ){
                haptic{
                    a.setAnimationEnabled(
                        !a.animationEnabled
                    )
                }
            }

            AnimatedVisibility(
                visible=
                    a.animationEnabled
            ){
                Column{
                    Spacer(
                        Modifier.height(12.dp)
                    )

                    NmixAnimationSettings()
                }
            }

            Spacer(
                Modifier.height(20.dp)
            )

            AppIconSettings(
                countdown=
                    iconCountdown,
                applying=
                    pendingApplyIcon,
                beginApply={
                    beginIconApply()
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
                        .copy(alpha=.82f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .03f
                        else
                            .018f
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
                interactionSource=
                    remember{
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
                fontWeight=
                    FontWeight.SemiBold,
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

@Composable
private fun HapticChoice(
    strength:NmixHapticStrength,
    selected:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(12.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.88f)
                else
                    Color.White
                        .copy(alpha=.88f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .07f
                        else
                            .018f
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
                            .55f
                        else if(a.darkMode)
                            .13f
                        else
                            .20f
                ),
                shape
            )
            .clickable(
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null
            ){
                /*
                 * Set first so the tap itself previews
                 * the newly selected strength.
                 */
                a.setHapticStrength(
                    strength
                )
            }
            .padding(horizontal=12.dp),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Text(
            strength.label(),
            Modifier.weight(1f),
            color=
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=10.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=a.fontFamily
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
private fun ApplyIconPrompt(
    onNo:()->Unit,
    onYes:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(13.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.92f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=.025f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .16f
                        else
                            .24f
                ),
                shape
            )
            .padding(10.dp)
    ){
        Text(
            "Apply for icon too?",
            color=ui.text,
            fontSize=9.5.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(8.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            PromptButton(
                text="No",
                modifier=
                    Modifier.weight(1f),
                accent=false,
                onClick=onNo
            )

            PromptButton(
                text="Yes",
                modifier=
                    Modifier.weight(1f),
                accent=true,
                onClick=onYes
            )
        }
    }
}

@Composable
private fun PromptButton(
    text:String,
    modifier:Modifier,
    accent:Boolean,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val shape=
        RoundedCornerShape(10.dp)

    NmixPressBox(
        modifier=
            modifier.height(37.dp),
        shape=shape,
        color=
            if(accent)
                p.accent.copy(
                    alpha=.80f
                )
            else if(a.darkMode)
                Color(0xFF111614)
                    .copy(alpha=.90f)
            else
                Color.White.copy(
                    alpha=.90f
                ),
        onClick=onClick
    ){
        Text(
            text,
            color=
                if(accent)
                    Color.White
                else
                    a.uiColors().text,
            fontSize=9.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=a.fontFamily
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
    countdown:Int,
    applying:Boolean,
    beginApply:()->Unit
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
                        .copy(alpha=.82f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=.018f
                )
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

        Spacer(
            Modifier.height(8.dp)
        )

        SettingsToggleRow(
            title="Set App Icon",
            detail=
                if(a.appIconEnabled)
                    "Alternate NMIX icon active"
                else
                    "Use default app icon",
            enabled=
                a.appIconEnabled
        ){
            if(!applying){
                a.setAppIconEnabled(
                    !a.appIconEnabled
                )

                beginApply()
            }
        }

        AnimatedVisibility(
            visible=
                a.appIconEnabled
        ){
            Column{
                Spacer(
                    Modifier.height(9.dp)
                )

                SettingsToggleRow(
                    title="Follow Theme",
                    detail=
                        if(a.iconFollowTheme)
                            "Ask when preset color changes"
                        else
                            "Manual icon color",
                    enabled=
                        a.iconFollowTheme
                ){
                    a.setIconFollowTheme(
                        !a.iconFollowTheme
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                Text(
                    "Icon Color",
                    color=ui.text,
                    fontSize=10.sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(7.dp)
                )

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
                                    a.iconTheme==
                                        theme,
                                enabled=
                                    !applying,
                                modifier=
                                    Modifier.weight(1f)
                            ){
                                a.setIconTheme(
                                    theme
                                )

                                beginApply()
                            }
                        }
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                Text(
                    "Icon Style",
                    color=ui.text,
                    fontSize=10.sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(7.dp)
                )

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
                        enabled=!applying,
                        modifier=
                            Modifier.weight(1f)
                    ){
                        a.setIconStyle(
                            NmixIconStyle.ADAPTIVE
                        )

                        beginApply()
                    }

                    IconStyleChoice(
                        text="Round",
                        selected=
                            a.iconStyle==
                                NmixIconStyle.ROUND,
                        enabled=!applying,
                        modifier=
                            Modifier.weight(1f)
                    ){
                        a.setIconStyle(
                            NmixIconStyle.ROUND
                        )

                        beginApply()
                    }
                }
            }
        }

        AnimatedVisibility(
            visible=
                applying &&
                    countdown>=0
        ){
            Column{
                Spacer(
                    Modifier.height(10.dp)
                )

                val noticeShape=
                    RoundedCornerShape(11.dp)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(noticeShape)
                        .background(
                            p.accent.copy(
                                alpha=.055f
                            )
                        )
                        .border(
                            .4.dp,
                            p.accent.copy(
                                alpha=.18f
                            ),
                            noticeShape
                        )
                        .padding(
                            horizontal=10.dp,
                            vertical=8.dp
                        ),
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    Column(
                        Modifier.weight(1f)
                    ){
                        Text(
                            "Launcher refresh",
                            color=ui.text,
                            fontSize=8.5.sp,
                            fontWeight=
                                FontWeight.SemiBold,
                            fontFamily=a.fontFamily
                        )

                        Text(
                            if(countdown>0)
                                "Applying icon in"
                            else
                                "Refreshing icon…",
                            color=ui.muted,
                            fontSize=7.sp,
                            fontFamily=a.fontFamily
                        )
                    }

                    Text(
                        "$countdown",
                        color=
                            Color(0xFFE34E55),
                        fontSize=20.sp,
                        fontWeight=
                            FontWeight.Bold,
                        fontFamily=a.fontFamily
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

    val color=
        theme.palette().accent

    Box(
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
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
                        CircleShape
                    )
                }else{
                    Modifier
                }
            )
            .clickable(
                enabled=enabled,
                interactionSource=
                    remember{
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
    enabled:Boolean,
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
                if(a.darkMode)
                    Color(0xFF121715)
                        .copy(alpha=.88f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .07f
                        else
                            .018f
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
                            .58f
                        else
                            .16f
                ),
                shape
            )
            .clickable(
                enabled=enabled,
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=9.sp,
            fontWeight=
                FontWeight.SemiBold,
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
    detail:Int,
    onThemeChosen:(NmixThemeName)->Unit
){
    val a=LocalNmixAppearance.current

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
                            onThemeChosen(theme)
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
        targetValue=
            if(pressed)
                .97f
            else
                1f,
        label="themePress"
    )

    val shape=
        RoundedCornerShape(14.dp)

    Column(
        modifier
            .height(132.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                else
                    Color.White
                        .copy(alpha=.88f)
            )
            .border(
                if(selected)
                    1.1.dp
                else
                    .4.dp,
                if(selected)
                    current.accent
                else
                    current.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .10f
                            else
                                .16f
                    ),
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
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(6.dp)
                        .size(13.dp),
                    Color.White
                )
            }
        }

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            theme.name
                .lowercase()
                .replaceFirstChar{
                    it.uppercase()
                },
            color=ui.text,
            fontSize=10.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(2.dp)
        )

        AnimatedContent(
            targetState=detail,
            transitionSpec={
                fadeIn(tween(280)) togetherWith
                    fadeOut(tween(190))
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

    val shape=
        RoundedCornerShape(50)

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                else
                    Color.White
                        .copy(alpha=.88f)
            )
            .border(
                if(selected)
                    1.dp
                else
                    .4.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .62f
                        else
                            .15f
                ),
                shape
            )
            .clickable(
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null,
                onClick=onClick
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
        targetValue=
            if(pressed)
                .96f
            else
                1f,
        label="switchPress"
    )

    val position by
        animateFloatAsState(
            targetValue=
                if(on)1f else 0f,
            animationSpec=tween(
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
                    x=(21f*position).dp
                )
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
