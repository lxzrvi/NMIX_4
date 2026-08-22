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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
                            "+","−","×","÷","%","="
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
            NmixIcon.TIMER,
            "Timer",
            mode=="timer",
            onTimer,
            onTimerReset
        )

        Box(
            Modifier.fillMaxWidth()
        ){
            ModeRow(
                NmixIcon.CLOCK,
                "Clock",
                mode=="clock",
                onClock
            )

            NmixSmallIconButton(
                NmixIcon.FULLSCREEN,
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(end=10.dp)
                    .size(38.dp),
                mode=="clock",
                onFullscreen
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

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed)
            .965f
        else
            1f,
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
            onClick
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
 * ------------------------------------------------
 * HOW TO USE NMIX
 * ------------------------------------------------
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
                "Open Calculator and enter values with the native keypad. Use +, −, ×, ÷ or %. Tap = or the large Display when the expression is complete."
            ),

            HelpItem(
                "Display",
                "The large NMIX Display shows results and live tool values. Drag the •••• handle on the bottom-right of the colored header to resize the top screen."
            ),

            HelpItem(
                "Editing",
                "Calculator supports decimal input, ±, backspace and AC. When the Display becomes compact, calculator fields automatically adapt to the available space."
            ),

            HelpItem(
                "Timer",
                "Tap Timer to start or pause. Hold Timer to reset to zero. Use the − and + controls on the large Display to remove or add five seconds."
            ),

            HelpItem(
                "Clock",
                "Opening Clock immediately activates local time. Use the fullscreen control for the customizable fullscreen clock."
            ),

            HelpItem(
                "Fullscreen",
                "Fullscreen Clock supports clock styles, fonts, colors, wallpapers, custom images, Display options, rotation and Clean View. Your customization is preserved."
            ),

            HelpItem(
                "Stopwatch",
                "Tap Stopwatch to start or pause. Hold it to reset. The large Display shows elapsed time with hundredths."
            ),

            HelpItem(
                "Counters",
                "Add and Minus change the counter. Reset returns it to zero. Random generates a number from 1 to 1000."
            ),

            HelpItem(
                "Appearance",
                "Use Settings to switch dark or light mode, choose one of six NMIX colors, select a UI font and change the background animation style."
            ),

            HelpItem(
                "Navigation",
                "Use the top-left arrow to collapse or restore the top screen. Use Back to the Start at the end of the Main page to return to Landing."
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
                            Modifier.weight(1f)
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
                        animationSpec=tween(
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

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed)
            .95f
        else
            1f,
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
 * ------------------------------------------------
 * SETTINGS
 * ------------------------------------------------
 */

@Composable
fun NmixSettings(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val p=a.palette
    val scroll=rememberScrollState()

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
            fontWeight=FontWeight.Bold,
            fontFamily=NmixLogoFont
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
                .padding(vertical=5.dp),
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

        ThemeGrid(detail)

        Spacer(
            Modifier.height(20.dp)
        )

        /*
         * New shared motion selector.
         */
        Text(
            "Animation",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Choose how NMIX backgrounds move",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(10.dp)
        )

        AnimationGrid()

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
                Arrangement.spacedBy(7.dp)
        ){
            NmixFontName.values()
                .forEach{font->
                    FontPill(
                        font,
                        a.font==font
                    ){
                        a.setFont(font)
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
 * ------------------------------------------------
 * ANIMATION CARDS
 * ------------------------------------------------
 */

@Composable
private fun AnimationGrid(){
    val a=LocalNmixAppearance.current

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement=
            Arrangement.spacedBy(8.dp)
    ){
        NmixAnimationName.values()
            .toList()
            .chunked(3)
            .forEach{row->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    row.forEach{animation->
                        AnimationCard(
                            animation=
                                animation,
                            selected=
                                a.animation==
                                    animation,
                            modifier=
                                Modifier.weight(1f)
                        ){
                            a.setAnimation(
                                animation
                            )
                        }
                    }
                }
            }
    }
}

@Composable
private fun AnimationCard(
    animation:NmixAnimationName,
    selected:Boolean,
    modifier:Modifier,
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
        if(pressed)
            .97f
        else
            1f,
        label="animationPress"
    )

    val shape=
        RoundedCornerShape(14.dp)

    val previewShape=
        RoundedCornerShape(10.dp)

    Column(
        modifier
            .height(111.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color.White.copy(
                        alpha=.035f
                    )
                else
                    Color.White.copy(
                        alpha=.70f
                    )
            )
            .border(
                if(selected)
                    1.1.dp
                else
                    .4.dp,
                if(selected)
                    p.accent
                else
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .08f
                            else
                                .14f
                    ),
                shape
            )
            .clickable(
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick
            )
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(previewShape)
                .background(
                    if(a.darkMode)
                        Color(0xFF111614)
                    else
                        Color(0xFFE7ECE9)
                )
        ){
            AnimationPreview(
                animation
            )

            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(6.dp)
                        .size(12.dp),
                    Color.White
                )
            }
        }

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            animation.label(),
            color=ui.text,
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily,
            maxLines=1
        )
    }
}

@Composable
private fun AnimationPreview(
    animation:NmixAnimationName
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val motion=
        rememberInfiniteTransition(
            label=
                "preview${animation.name}"
        )

    val t by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                when(animation){
                    NmixAnimationName.DRIFT->
                        2400

                    NmixAnimationName.ORBIT->
                        1900

                    NmixAnimationName.FLOW->
                        2200

                    NmixAnimationName.FLOAT->
                        2700

                    NmixAnimationName.PULSE->
                        1800

                    NmixAnimationName.CROSS->
                        2100
                },
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewT"
    )

    val u by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                2850,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewU"
    )

    val pulse=
        .82f+
            (
                (t+1f)/2f
            )*.32f

    val firstX:Float
    val firstY:Float
    val secondX:Float
    val secondY:Float
    val firstScale:Float
    val secondScale:Float

    when(animation){
        NmixAnimationName.DRIFT->{
            firstX=t*30f
            firstY=u*8f
            secondX=-t*24f
            secondY=-u*10f
            firstScale=1f
            secondScale=1f
        }

        NmixAnimationName.ORBIT->{
            firstX=t*27f
            firstY=u*17f
            secondX=-u*25f
            secondY=t*16f
            firstScale=1f
            secondScale=1f
        }

        NmixAnimationName.FLOW->{
            firstX=t*38f
            firstY=t*7f
            secondX=t*30f
            secondY=-t*8f
            firstScale=1.1f
            secondScale=.9f
        }

        NmixAnimationName.FLOAT->{
            firstX=t*12f
            firstY=u*20f
            secondX=-t*14f
            secondY=-u*18f
            firstScale=1f
            secondScale=1f
        }

        NmixAnimationName.PULSE->{
            firstX=t*6f
            firstY=u*5f
            secondX=-t*5f
            secondY=-u*5f
            firstScale=pulse
            secondScale=
                1.25f-
                    (pulse-.82f)
        }

        NmixAnimationName.CROSS->{
            firstX=t*37f
            firstY=u*12f
            secondX=-t*37f
            secondY=-u*12f
            firstScale=1f
            secondScale=1f
        }
    }

    Box(
        Modifier.fillMaxSize()
    ){
        PreviewGlow(
            color=p.accent,
            modifier=Modifier
                .align(
                    Alignment.CenterStart
                )
                .offset(
                    x=(-19).dp
                )
                .graphicsLayer{
                    translationX=firstX
                    translationY=firstY
                    scaleX=firstScale
                    scaleY=firstScale
                }
        )

        PreviewGlow(
            color=p.accentLight,
            modifier=Modifier
                .align(
                    Alignment.CenterEnd
                )
                .offset(
                    x=19.dp
                )
                .graphicsLayer{
                    translationX=secondX
                    translationY=secondY
                    scaleX=secondScale
                    scaleY=secondScale
                }
        )
    }
}

@Composable
private fun PreviewGlow(
    color:Color,
    modifier:Modifier
){
    Box(
        modifier
            .size(72.dp)
            .background(
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to
                            color.copy(
                                alpha=.55f
                            ),

                        .30f to
                            color.copy(
                                alpha=.34f
                            ),

                        .64f to
                            color.copy(
                                alpha=.11f
                            ),

                        1f to
                            Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

/*
 * ------------------------------------------------
 * COLOR CARDS
 * ------------------------------------------------
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
                            theme=
                                theme,
                            selected=
                                a.theme==
                                    theme,
                            detail=
                                detail,
                            modifier=
                                Modifier.weight(1f)
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

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            if(pressed)
                .97f
            else
                1f,
            label="themePress"
        )

    val shape=
        RoundedCornerShape(14.dp)

    val bg=
        if(a.darkMode)
            Color.White.copy(
                alpha=.035f
            )
        else
            Color.White.copy(
                alpha=.70f
            )

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
                fontFamily=a.fontFamily,
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
                            animationSpec=tween(
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
                            animationSpec=tween(
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
                    fontFamily=a.fontFamily,
                    maxLines=3,
                    textAlign=
                        TextAlign.Start
                )
            }
        }
    }
}

/*
 * ------------------------------------------------
 * FONT PILLS
 * ------------------------------------------------
 */

@Composable
private fun FontPill(
    font:NmixFontName,
    selected:Boolean,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=
        a.palette

    val ui=
        a.uiColors()

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            if(pressed)
                .97f
            else
                1f,
            label="fontPress"
        )

    val shape=
        RoundedCornerShape(50)

    val bg=
        if(a.darkMode)
            Color.White.copy(
                alpha=.035f
            )
        else
            Color.White.copy(
                alpha=.67f
            )

    val outline=
        if(selected)
            p.accent
        else
            p.accent.copy(
                alpha=
                    if(a.darkMode)
                        .08f
                    else
                        .14f
            )

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
    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction
            .collectIsPressedAsState()

    val scale by
        animateFloatAsState(
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
                if(on)
                    accent
                else
                    Color(0xFF6F7773)
                        .copy(alpha=.42f)
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
