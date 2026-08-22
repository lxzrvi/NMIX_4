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
import androidx.compose.ui.graphics.Color
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
                                Modifier.size(
                                    55.dp
                                ),
                            type=type,
                            onClick={
                                onKey(key)
                            }
                        )
                    }
                }

                Spacer(
                    Modifier.height(
                        9.dp
                    )
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
            Arrangement.spacedBy(
                8.dp
            )
    ){
        ModeRow(
            icon=NmixIcon.TIMER,
            title="Timer",
            selected=
                mode=="timer",
            onClick=onTimer,
            onLong=onTimerReset
        )

        Box(
            Modifier.fillMaxWidth()
        ){
            ModeRow(
                icon=NmixIcon.CLOCK,
                title="Clock",
                selected=
                    mode=="clock",
                onClick=onClock
            )

            NmixSmallIconButton(
                icon=
                    NmixIcon.FULLSCREEN,
                modifier=
                    Modifier
                        .align(
                            Alignment.CenterEnd
                        )
                        .padding(
                            end=10.dp
                        )
                        .size(38.dp),
                selected=
                    mode=="clock",
                onClick=
                    onFullscreen
            )
        }

        ModeRow(
            icon=
                NmixIcon.STOPWATCH,
            title=
                "Stopwatch",
            selected=
                mode=="stopwatch",
            onClick=
                onStopwatch,
            onLong=
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .965f
                else
                    1f,
            label="modePress"
        )

    val shape=
        RoundedCornerShape(
            13.dp
        )

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(selected){
                    p.accent.copy(
                        alpha=.84f
                    )
                }else{
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .085f
                            else
                                .08f
                    )
                }
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
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick,
                onLongClick={
                    onLong?.invoke()
                }
            )
            .padding(
                horizontal=13.dp
            ),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Box(
            Modifier
                .size(35.dp)
                .clip(
                    if(selected){
                        CircleShape
                    }else{
                        RoundedCornerShape(
                            9.dp
                        )
                    }
                )
                .background(
                    if(selected){
                        Color.White.copy(
                            alpha=.92f
                        )
                    }else{
                        p.accent.copy(
                            alpha=.15f
                        )
                    }
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
            color=
                if(selected)
                    Color.White
                else
                    ui.text,
            fontSize=13.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=
                a.fontFamily
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
            Arrangement.spacedBy(
                8.dp
            )
    ){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(
                    8.dp
                )
        ){
            CounterButton(
                icon=NmixIcon.PLUS,
                title="Add",
                modifier=
                    Modifier.weight(1f),
                onClick=add
            )

            CounterButton(
                icon=NmixIcon.RESET,
                title="Reset",
                modifier=
                    Modifier.weight(1f),
                onClick=reset
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(
                    8.dp
                )
        ){
            CounterButton(
                icon=NmixIcon.RANDOM,
                title="Random",
                modifier=
                    Modifier.weight(1f),
                onClick=random
            )

            CounterButton(
                icon=NmixIcon.MINUS,
                title="Minus",
                modifier=
                    Modifier.weight(1f),
                onClick=minus
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(
            12.dp
        )

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
            modifier=
                Modifier.fillMaxSize(),
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
                    Modifier.width(
                        8.dp
                    )
                )

                Text(
                    title,
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=
                        a.fontFamily
                )
            }
        }
    }
}

/*
 * ==================================================
 * HOW TO USE NMIX
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
                "Enter numbers with the native keypad. Use +, −, ×, ÷ or %. Tap = or the large Display when the expression is complete."
            ),

            HelpItem(
                "Display",
                "The large Display shows results and live values. Drag the •••• handle in the colored header to resize the top screen. Your selected height is remembered."
            ),

            HelpItem(
                "Editing",
                "Use decimal, ±, backspace and AC. Calculator fields automatically change placement when the Display becomes compact."
            ),

            HelpItem(
                "Timer",
                "Tap Timer to start or pause. Hold Timer to reset to zero. Use − and + on the Display to remove or add five seconds."
            ),

            HelpItem(
                "Clock",
                "Tap Clock for live local time. Use the fullscreen icon to open the customizable Fullscreen Clock."
            ),

            HelpItem(
                "Fullscreen",
                "Fullscreen Clock includes clock styles, fonts, colors, wallpapers, custom images, Display controls, rotation and Clean View. Customization is preserved."
            ),

            HelpItem(
                "Stopwatch",
                "Tap Stopwatch to start or pause. Hold it to reset. The large Display shows elapsed time with hundredths."
            ),

            HelpItem(
                "Counters",
                "Add and Minus change the value. Reset returns to zero. Random generates a number from 1 to 1000."
            ),

            HelpItem(
                "Appearance",
                "Settings includes dark or light appearance, six color themes, Soft and Hard background animation styles, and selectable interface fonts."
            ),

            HelpItem(
                "Navigation",
                "Use the top-left arrow to collapse or restore the top screen. Back to the Start appears at the end of the Main page."
            )
        )
    }

    var selected by remember{
        mutableStateOf<String?>(
            null
        )
    }

    Column(
        Modifier.padding(11.dp),
        verticalArrangement=
            Arrangement.spacedBy(
                8.dp
            )
    ){
        items.chunked(5)
            .forEach{rowItems->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            6.dp
                        )
                ){
                    rowItems.forEach{
                        item->

                        HelpTitleBox(
                            item=item,
                            selected=
                                selected==
                                    item.title,
                            modifier=
                                Modifier.weight(
                                    1f
                                )
                        ){
                            selected=
                                if(
                                    selected==
                                    item.title
                                ){
                                    null
                                }else{
                                    item.title
                                }
                        }
                    }

                    repeat(
                        5-rowItems.size
                    ){
                        Spacer(
                            Modifier.weight(
                                1f
                            )
                        )
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
                            easing=
                                EaseOutCubic
                        )
                    )+
                    slideInVertically(
                        initialOffsetY={
                            it/8
                        },
                        animationSpec=
                            tween(
                                340,
                                easing=
                                    EaseOutCubic
                            )
                    )
                ) togetherWith (
                    fadeOut(
                        tween(190)
                    )+
                    slideOutVertically(
                        targetOffsetY={
                            -it/10
                        },
                        animationSpec=
                            tween(240)
                    )
                )
            },
            label="helpDetail"
        ){title->
            if(title!=null){
                val item=
                    items.first{
                        it.title==title
                    }

                HelpDetailBox(
                    item
                )
            }else{
                Spacer(
                    Modifier.height(
                        0.dp
                    )
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .95f
                else
                    1f,
            animationSpec=spring(
                dampingRatio=.74f,
                stiffness=650f
            ),
            label="helpPress"
        )

    val shape=
        RoundedCornerShape(
            11.dp
        )

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
                if(selected)
                    1.dp
                else
                    .45.dp,
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
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick
            )
            .padding(
                horizontal=4.dp
            ),
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
            fontWeight=
                FontWeight.Bold,
            fontFamily=a.fontFamily,
            textAlign=
                TextAlign.Center,
            maxLines=2
        )
    }
}

@Composable
private fun HelpDetailBox(
    item:HelpItem
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(
            14.dp
        )

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
            fontWeight=
                FontWeight.Bold,
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
fun NmixSettings(){
    val a=
        LocalNmixAppearance.current

    val ui=a.uiColors()
    val p=a.palette

    val scroll=
        rememberScrollState()

    var detail by remember{
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit){
        while(true){
            delay(3200)

            detail=
                (detail+1)%2
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(
                start=13.dp,
                end=13.dp,
                top=12.dp,
                bottom=22.dp
            )
    ){
        Text(
            "NMIX",
            color=ui.text,
            fontSize=17.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=
                NmixLogoFont
        )

        Text(
            "Appearance Settings",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(17.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical=5.dp
                ),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Column(
                Modifier.weight(1f)
            ){
                Text(
                    "Appearance",
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=
                        a.fontFamily
                )

                Text(
                    if(a.darkMode)
                        "Dark mode"
                    else
                        "Light mode",
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=
                        a.fontFamily
                )
            }

            NmixSwitch(
                on=a.darkMode,
                accent=p.accent
            ){
                a.toggleDarkMode()
            }
        }

        Spacer(
            Modifier.height(18.dp)
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
            detail
        )

        Spacer(
            Modifier.height(20.dp)
        )

        /*
         * Separate component.
         *
         * Contains:
         * SOFT row
         * HARD row
         * moving geometry previews
         * persistent selection
         */
        NmixAnimationSettings()

        Spacer(
            Modifier.height(20.dp)
        )

        Text(
            "Fonts",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Preview your interface typeface",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement=
                Arrangement.spacedBy(
                    7.dp
                )
        ){
            NmixFontName.values()
                .forEach{font->
                    FontPill(
                        font=font,
                        selected=
                            a.font==font
                    ){
                        a.setFont(
                            font
                        )
                    }
                }
        }

        Spacer(
            Modifier.height(17.dp)
        )

        Text(
            "NMIX logo uses Cinzel Decorative.",
            color=ui.muted,
            fontSize=8.sp,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * COLOR THEME
 * ==================================================
 */

@Composable
private fun ThemeGrid(
    detail:Int
){
    val a=
        LocalNmixAppearance.current

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement=
            Arrangement.spacedBy(
                8.dp
            )
    ){
        NmixThemeName.values()
            .toList()
            .chunked(3)
            .forEach{row->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            7.dp
                        )
                ){
                    row.forEach{theme->
                        ThemeCard(
                            theme=theme,
                            selected=
                                a.theme==
                                    theme,
                            detail=detail,
                            modifier=
                                Modifier.weight(
                                    1f
                                )
                        ){
                            a.setTheme(
                                theme
                            )
                        }
                    }
                }
            }
    }
}

private fun themeHex(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->
        "#319B79"

    NmixThemeName.BLUE->
        "#348BB8"

    NmixThemeName.PURPLE->
        "#8A62C8"

    NmixThemeName.ORANGE->
        "#D57D35"

    NmixThemeName.ROSE->
        "#C85878"

    NmixThemeName.CYAN->
        "#26A6B5"
}

private fun themeMood(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->
        "Balanced • Focused"

    NmixThemeName.BLUE->
        "Clear • Productive"

    NmixThemeName.PURPLE->
        "Creative • Calm"

    NmixThemeName.ORANGE->
        "Energetic • Warm"

    NmixThemeName.ROSE->
        "Soft • Expressive"

    NmixThemeName.CYAN->
        "Fresh • Precise"
}

private fun themeDetail(
    theme:NmixThemeName
)=when(theme){
    NmixThemeName.GREEN->
        "Calm natural tone"

    NmixThemeName.BLUE->
        "Clean focused energy"

    NmixThemeName.PURPLE->
        "Quiet creative mood"

    NmixThemeName.ORANGE->
        "Bright active feel"

    NmixThemeName.ROSE->
        "Warm expressive mood"

    NmixThemeName.CYAN->
        "Crisp modern clarity"
}

@Composable
private fun ThemeCard(
    theme:NmixThemeName,
    selected:Boolean,
    detail:Int,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val current=
        a.palette

    val ui=
        a.uiColors()

    val palette=
        theme.palette()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .97f
                else
                    1f,
            label="themePress"
        )

    val shape=
        RoundedCornerShape(
            14.dp
        )

    val bg=
        if(a.darkMode){
            Color.White.copy(
                alpha=.035f
            )
        }else{
            Color.White.copy(
                alpha=.70f
            )
        }

    val outline=
        if(selected){
            current.accent
        }else{
            current.accent.copy(
                alpha=
                    if(a.darkMode)
                        .08f
                    else
                        .14f
            )
        }

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
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(
                    RoundedCornerShape(
                        10.dp
                    )
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
                fontWeight=
                    FontWeight.Bold,
                fontFamily=
                    a.fontFamily,
                maxLines=1
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
            name,
            color=ui.text,
            fontSize=10.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Spacer(
            Modifier.height(2.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment=
                Alignment.TopStart
        ){
            AnimatedContent(
                targetState=detail,
                transitionSpec={
                    (
                        fadeIn(
                            tween(320)
                        )+
                        slideInVertically(
                            initialOffsetY={
                                it/4
                            },
                            animationSpec=
                                tween(
                                    320,
                                    easing=
                                        EaseOutCubic
                                )
                        )
                    ) togetherWith (
                        fadeOut(
                            tween(220)
                        )+
                        slideOutVertically(
                            targetOffsetY={
                                -it/4
                            },
                            animationSpec=
                                tween(
                                    260,
                                    easing=
                                        EaseInCubic
                                )
                        )
                    )
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
                    fontWeight=
                        if(state==0)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal,
                    fontFamily=
                        a.fontFamily,
                    maxLines=3,
                    textAlign=
                        TextAlign.Start
                )
            }
        }
    }
}

/*
 * ==================================================
 * FONT
 * ==================================================
 */

@Composable
private fun FontPill(
    font:NmixFontName,
    selected:Boolean,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .97f
                else
                    1f,
            label="fontPress"
        )

    val shape=
        RoundedCornerShape(50)

    val bg=
        if(a.darkMode){
            Color.White.copy(
                alpha=.035f
            )
        }else{
            Color.White.copy(
                alpha=.67f
            )
        }

    val outline=
        if(selected){
            p.accent
        }else{
            p.accent.copy(
                alpha=
                    if(a.darkMode)
                        .08f
                    else
                        .14f
            )
        }

    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(
                if(selected)
                    1.05.dp
                else
                    .4.dp,
                outline,
                shape
            )
            .combinedClickable(
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick,
                onLongClick={}
            )
            .padding(
                horizontal=13.dp
            ),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Text(
            font.label(),
            modifier=
                Modifier.weight(1f),
            color=ui.text,
            fontSize=13.5.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=
                font.family()
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
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .96f
                else
                    1f,
            label="switchPress"
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
                if(on){
                    accent
                }else{
                    Color(0xFF6F7773)
                        .copy(
                            alpha=.42f
                        )
                }
            )
            .combinedClickable(
                interactionSource=
                    interaction,
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
                .background(
                    Color.White
                )
        )
    }
}
