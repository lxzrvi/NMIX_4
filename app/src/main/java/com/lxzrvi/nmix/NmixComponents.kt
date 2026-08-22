package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun nmixScreenColor():Color{
    val a=LocalNmixAppearance.current

    return if(a.darkMode)
        Color(0xFF151A18)
    else
        Color(0xFFE9ECEA)
}

@Composable
private fun nmixScreenBorder():Color{
    val a=LocalNmixAppearance.current

    return a.palette.accent.copy(
        alpha=
            if(a.darkMode)
                .14f
            else
                .27f
    )
}

@Composable
private fun nmixDisplayText():Color{
    val a=LocalNmixAppearance.current

    return if(a.darkMode)
        Color.White.copy(alpha=.92f)
    else
        a.uiColors().text.copy(alpha=.88f)
}

/*
 * ==================================================
 * TOOL SECTION
 * ==================================================
 */

@Composable
fun NmixToolSection(
    icon:NmixIcon,
    title:String,
    subtitle:String,
    open:Boolean,
    onClick:()->Unit,
    content:@Composable ()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val progress by animateFloatAsState(
        if(open)1f else 0f,
        tween(
            520,
            easing=EaseInOutCubic
        ),
        label="toolProgress"
    )

    val outerRotation=
        180f*progress

    val innerRotation=
        -180f*progress

    val arrowRotation=
        180f*progress

    val outerSize=
        (42f-3f*progress).dp

    val innerSize=
        (36f-2f*progress).dp

    val outerRadius=
        (8.5f+10f*progress).dp

    val innerRadius=
        (6.5f+9f*progress).dp

    val shape=
        RoundedCornerShape(16.dp)

    val glass=
        Brush.horizontalGradient(
            listOf(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .10f
                        else
                            .11f
                ),
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .085f
                        else
                            .09f
                ),
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .10f
                        else
                            .11f
                )
            )
        )

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(shape)
            .background(glass)
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .27f
                ),
                shape
            )
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource=remember{
                        MutableInteractionSource()
                    },
                    indication=null,
                    onClick=onClick
                )
                .padding(13.dp),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Box(
                Modifier.size(42.dp),
                contentAlignment=
                    Alignment.Center
            ){
                val outerShape=
                    RoundedCornerShape(
                        outerRadius
                    )

                Box(
                    Modifier
                        .size(outerSize)
                        .rotate(outerRotation)
                        .clip(outerShape)
                        .background(
                            p.accent.copy(
                                alpha=.66f
                            )
                        )
                        .border(
                            .65.dp,
                            p.accentLight.copy(
                                alpha=.60f
                            ),
                            outerShape
                        )
                )

                Canvas(
                    Modifier
                        .size(innerSize)
                        .rotate(innerRotation)
                ){
                    val sw=.62.dp.toPx()

                    drawRoundRect(
                        color=
                            Color.White.copy(
                                alpha=.46f
                            ),
                        topLeft=
                            Offset(sw,sw),
                        size=
                            Size(
                                size.width-sw*2,
                                size.height-sw*2
                            ),
                        cornerRadius=
                            CornerRadius(
                                innerRadius.toPx()
                            ),
                        style=Stroke(sw)
                    )
                }

                NmixIcon(
                    icon,
                    Modifier.size(19.dp),
                    Color.White
                )
            }

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                Modifier.weight(1f)
            ){
                Text(
                    title,
                    color=ui.text,
                    fontSize=14.sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Text(
                    subtitle,
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=a.fontFamily
                )
            }

            NmixIcon(
                NmixIcon.CHEVRON_DOWN,
                Modifier
                    .size(18.dp)
                    .rotate(
                        arrowRotation
                    ),
                ui.muted
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=
                expandVertically(
                    animationSpec=tween(
                        350,
                        easing=EaseOutCubic
                    ),
                    expandFrom=
                        Alignment.Top
                )+
                fadeIn(
                    tween(210)
                ),
            exit=
                shrinkVertically(
                    animationSpec=tween(
                        320,
                        easing=
                            EaseInOutCubic
                    ),
                    shrinkTowards=
                        Alignment.Top
                )+
                fadeOut(
                    tween(180)
                )
        ){
            content()
        }
    }
}

/*
 * ==================================================
 * COMMON CONTROLS
 * ==================================================
 */

@Composable
fun NmixOption(
    icon:NmixIcon,
    title:String,
    selected:Boolean=false,
    modifier:Modifier=Modifier,
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
        if(pressed).965f else 1f,
        spring(
            dampingRatio=.72f,
            stiffness=620f
        ),
        label="optionPress"
    )

    val shape=
        RoundedCornerShape(13.dp)

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
            .clip(shape)
            .background(
                if(selected)
                    p.accent.copy(
                        alpha=.84f
                    )
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
                horizontal=13.dp
            ),
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
                        Color.White.copy(
                            alpha=.92f
                        )
                    else
                        p.accent.copy(
                            alpha=.15f
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
fun NmixCircleButton(
    icon:NmixIcon,
    modifier:Modifier=Modifier,
    color:Color?=null,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    NmixPressBox(
        modifier,
        CircleShape,
        color?:a.palette.accent.copy(
            alpha=.80f
        ),
        onClick
    ){
        NmixIcon(
            icon,
            Modifier.size(21.dp),
            Color.White
        )
    }
}

@Composable
fun NmixSmallIconButton(
    icon:NmixIcon,
    modifier:Modifier=Modifier,
    selected:Boolean=false,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    NmixPressBox(
        modifier,
        RoundedCornerShape(9.dp),
        if(selected)
            Color.White.copy(
                alpha=.17f
            )
        else
            p.accent.copy(
                alpha=.13f
            ),
        onClick
    ){
        NmixIcon(
            icon,
            Modifier.size(19.dp),
            if(selected)
                Color.White
            else
                p.accent
        )
    }
}

@Composable
fun NmixTextButton(
    text:String,
    modifier:Modifier=Modifier,
    accent:Boolean=false,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val shape=
        RoundedCornerShape(50)

    val bg=
        if(accent)
            p.accent.copy(
                alpha=.78f
            )
        else
            p.accent.copy(
                alpha=
                    if(a.darkMode)
                        .09f
                    else
                        .08f
            )

    Box(
        modifier
            .clip(shape)
            .background(bg)
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .30f
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
            Text(
                text,
                color=
                    if(accent)
                        Color.White
                    else
                        a.uiColors().text,
                fontSize=12.sp,
                fontWeight=
                    FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
fun NmixKey(
    text:String,
    modifier:Modifier=Modifier,
    type:Int=0,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val bg=when(type){
        1->
            p.accent.copy(
                alpha=.86f
            )

        2->
            Color(0xFFD83939)
                .copy(alpha=.17f)

        else->
            if(a.darkMode)
                Color(0xFF090C0B)
                    .copy(alpha=.75f)
            else
                p.accent.copy(
                    alpha=.08f
                )
    }

    val fg=when(type){
        1->Color.White
        2->Color(0xFFE15A5A)
        else->a.uiColors().text
    }

    NmixPressBox(
        modifier,
        CircleShape,
        bg,
        onClick
    ){
        Text(
            text,
            color=fg,
            fontSize=15.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * DISPLAY
 * ==================================================
 */

@Composable
fun NmixDisplay(
    label:String,
    value:String,
    status:String,
    timer:Boolean,
    calcVisible:Boolean,
    calcFirst:String,
    calcOperator:String,
    calcSecond:String,
    onMinus:()->Unit,
    onPlus:()->Unit,
    onClick:()->Unit,
    modifier:Modifier=Modifier
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val density=LocalDensity.current

    val interaction=remember{
        MutableInteractionSource()
    }

    var heightPx by remember{
        mutableIntStateOf(0)
    }

    val heightDp=
        with(density){
            heightPx.toDp()
        }

    val compact=
        heightPx>0 &&
        heightDp<195.dp

    val extremeCompact=
        heightPx>0 &&
        heightDp<125.dp

    val compactProgress by
        animateFloatAsState(
            if(compact)1f else 0f,
            tween(
                240,
                easing=EaseInOutCubic
            ),
            label="displayCompact"
        )

    val extremeProgress by
        animateFloatAsState(
            if(extremeCompact)
                1f
            else
                0f,
            tween(
                240,
                easing=EaseInOutCubic
            ),
            label="displayExtreme"
        )

    val calcProgress by
        animateFloatAsState(
            if(calcVisible)1f else 0f,
            tween(
                280,
                easing=EaseOutCubic
            ),
            label="calcVisibility"
        )

    /*
     * Display radius smoothly grows until the
     * extreme compact Display becomes pill-like.
     */
    val radius=
        (
            19f+
            extremeProgress*35f
        ).dp

    val displayShape=
        RoundedCornerShape(
            radius
        )

    val motion=
        rememberNmixMotion(
            "displayMotion"
        )

    Box(
        modifier
            .onSizeChanged{
                heightPx=it.height
            }
            .clip(displayShape)
            .background(
                nmixScreenColor()
            )
            .border(
                .55.dp,
                nmixScreenBorder(),
                displayShape
            )
            .clickable(
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick
            )
    ){
        DisplayMotionLayer(
            motion=motion
        )

        /*
         * Large mode calculator row.
         */
        if(calcVisible){
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(
                        Alignment.TopCenter
                    )
                    .padding(
                        start=12.dp,
                        end=12.dp,
                        top=12.dp
                    )
                    .graphicsLayer{
                        alpha=
                            calcProgress*
                            (
                                1f-
                                compactProgress
                            )
                    },
                horizontalArrangement=
                    Arrangement.spacedBy(
                        7.dp
                    )
            ){
                NmixCalcField(
                    text=
                        calcFirst.ifEmpty{
                            "_"
                        },
                    modifier=
                        Modifier.weight(1f),
                    shape=
                        RoundedCornerShape(
                            topStart=11.dp
                        )
                )

                NmixCalcField(
                    text=
                        calcOperator.ifEmpty{
                            "sign"
                        },
                    modifier=
                        Modifier.width(58.dp),
                    shape=
                        RoundedCornerShape(
                            0.dp
                        )
                )

                NmixCalcField(
                    text=
                        calcSecond.ifEmpty{
                            "_"
                        },
                    modifier=
                        Modifier.weight(1f),
                    shape=
                        RoundedCornerShape(
                            topEnd=11.dp
                        )
                )
            }
        }

        /*
         * Compact Calculator.
         *
         * Left ~35%, always horizontal.
         * Field height follows actual Display
         * height in extreme compact state.
         */
        if(calcVisible){
            val fieldHeight=
                when{
                    extremeCompact->
                        (
                            heightDp-
                            20.dp
                        )
                            .coerceIn(
                                30.dp,
                                42.dp
                            )

                    else->
                        39.dp
                }

            val fieldRadius=
                if(extremeCompact)
                    fieldHeight/2
                else
                    9.dp

            Box(
                Modifier
                    .fillMaxWidth(.37f)
                    .fillMaxHeight()
                    .align(
                        Alignment.CenterStart
                    )
                    .padding(
                        start=8.dp,
                        end=3.dp
                    )
                    .graphicsLayer{
                        alpha=
                            calcProgress*
                            compactProgress
                    },
                contentAlignment=
                    Alignment.Center
            ){
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            4.dp
                        ),
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    NmixCalcField(
                        text=
                            calcFirst.ifEmpty{
                                "_"
                            },
                        modifier=
                            Modifier.weight(1f),
                        shape=
                            RoundedCornerShape(
                                topStart=fieldRadius,
                                bottomStart=fieldRadius,
                                topEnd=
                                    if(extremeCompact)
                                        fieldRadius
                                    else
                                        0.dp,
                                bottomEnd=
                                    if(extremeCompact)
                                        fieldRadius
                                    else
                                        0.dp
                            ),
                        height=fieldHeight,
                        textSize=
                            if(extremeCompact)
                                9.sp
                            else
                                11.sp
                    )

                    NmixCalcField(
                        text=
                            calcOperator.ifEmpty{
                                "·"
                            },
                        modifier=
                            Modifier.width(
                                if(extremeCompact)
                                    27.dp
                                else
                                    32.dp
                            ),
                        shape=
                            if(extremeCompact)
                                RoundedCornerShape(
                                    fieldRadius
                                )
                            else
                                RoundedCornerShape(
                                    0.dp
                                ),
                        height=fieldHeight,
                        textSize=
                            if(extremeCompact)
                                8.sp
                            else
                                10.sp
                    )

                    NmixCalcField(
                        text=
                            calcSecond.ifEmpty{
                                "_"
                            },
                        modifier=
                            Modifier.weight(1f),
                        shape=
                            RoundedCornerShape(
                                topEnd=fieldRadius,
                                bottomEnd=fieldRadius,
                                topStart=
                                    if(extremeCompact)
                                        fieldRadius
                                    else
                                        0.dp,
                                bottomStart=
                                    if(extremeCompact)
                                        fieldRadius
                                    else
                                        0.dp
                            ),
                        height=fieldHeight,
                        textSize=
                            if(extremeCompact)
                                9.sp
                            else
                                11.sp
                    )
                }
            }
        }

        if(!compact){
            Text(
                label,
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .padding(
                        top=
                            if(calcVisible)
                                71.dp
                            else
                                17.dp
                    ),
                color=
                    if(a.darkMode)
                        p.accentLight
                    else
                        p.accentDark,
                fontSize=9.sp,
                fontWeight=
                    FontWeight.Bold,
                letterSpacing=2.sp,
                fontFamily=a.fontFamily
            )

            Text(
                value,
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationY=
                            if(calcVisible)
                                18f
                            else
                                0f
                    }
                    .padding(
                        horizontal=
                            if(timer)
                                70.dp
                            else
                                16.dp
                    ),
                color=
                    nmixDisplayText(),
                fontSize=40.sp,
                fontWeight=
                    FontWeight.SemiBold,
                fontFamily=a.fontFamily,
                maxLines=1
            )

            Text(
                status,
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        horizontal=18.dp,
                        vertical=15.dp
                    ),
                color=
                    if(a.darkMode)
                        p.accentLight.copy(
                            alpha=.78f
                        )
                    else
                        p.accentDark.copy(
                            alpha=.80f
                        ),
                fontSize=11.sp,
                lineHeight=15.sp,
                fontWeight=
                    FontWeight.Medium,
                fontFamily=a.fontFamily,
                textAlign=
                    TextAlign.Center,
                maxLines=2
            )
        }else{
            /*
             * Compact content sits on the right.
             *
             * With Calculator:
             * left ~37%, right ~63%.
             *
             * Without Calculator:
             * full Display width remains available.
             */
            Column(
                Modifier
                    .fillMaxWidth(
                        if(calcVisible)
                            .61f
                        else
                            .82f
                    )
                    .align(
                        if(calcVisible)
                            Alignment.CenterEnd
                        else
                            Alignment.Center
                    )
                    .padding(
                        start=6.dp,
                        end=
                            if(calcVisible)
                                12.dp
                            else
                                6.dp
                    ),
                horizontalAlignment=
                    Alignment.CenterHorizontally,
                verticalArrangement=
                    Arrangement.Center
            ){
                if(!extremeCompact){
                    Text(
                        label,
                        color=
                            if(a.darkMode)
                                p.accentLight
                            else
                                p.accentDark,
                        fontSize=8.sp,
                        fontWeight=
                            FontWeight.Bold,
                        letterSpacing=1.4.sp,
                        fontFamily=a.fontFamily,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )
                }

                Text(
                    value,
                    color=
                        nmixDisplayText(),
                    fontSize=
                        when{
                            extremeCompact->
                                if(calcVisible)
                                    23.sp
                                else
                                    28.sp

                            calcVisible->
                                29.sp

                            else->
                                34.sp
                        },
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily,
                    maxLines=1,
                    textAlign=
                        TextAlign.Center
                )

                if(!extremeCompact){
                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Text(
                        status,
                        color=
                            if(a.darkMode)
                                p.accentLight.copy(
                                    alpha=.76f
                                )
                            else
                                p.accentDark.copy(
                                    alpha=.78f
                                ),
                        fontSize=8.5.sp,
                        lineHeight=11.sp,
                        fontWeight=
                            FontWeight.Medium,
                        fontFamily=a.fontFamily,
                        textAlign=
                            TextAlign.Center,
                        maxLines=2
                    )
                }
            }
        }

        AnimatedVisibility(
            visible=timer,
            modifier=
                Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .padding(
                        start=
                            if(extremeCompact)
                                8.dp
                            else
                                13.dp
                    ),
            enter=
                fadeIn(tween(230))+
                scaleIn(),
            exit=
                fadeOut(tween(190))+
                scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.MINUS,
                Modifier.size(
                    when{
                        extremeCompact->
                            32.dp

                        compact->
                            40.dp

                        else->
                            47.dp
                    }
                ),
                onClick=onMinus
            )
        }

        AnimatedVisibility(
            visible=timer,
            modifier=
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(
                        end=
                            if(extremeCompact)
                                8.dp
                            else
                                13.dp
                    ),
            enter=
                fadeIn(tween(230))+
                scaleIn(),
            exit=
                fadeOut(tween(190))+
                scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.PLUS,
                Modifier.size(
                    when{
                        extremeCompact->
                            32.dp

                        compact->
                            40.dp

                        else->
                            47.dp
                    }
                ),
                onClick=onPlus
            )
        }
    }
}

/*
 * Soft modes retain feathered shapes.
 * Hard modes render actual moving geometry.
 *
 * This fixes the previous behavior where
 * HARD changed only the movement coordinates.
 */
@Composable
private fun BoxScope.DisplayMotionLayer(
    motion:NmixMotionValues
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val soft=
        a.animation in listOf(
            NmixAnimationName.DRIFT,
            NmixAnimationName.ORBIT,
            NmixAnimationName.FLOW
        )

    if(soft){
        Box(
            Modifier
                .size(340.dp)
                .align(
                    Alignment.TopStart
                )
                .offset(
                    x=(-145).dp,
                    y=(-155).dp
                )
                .graphicsLayer{
                    translationX=
                        motion.x*190f

                    translationY=
                        motion.y*85f

                    scaleX=
                        motion.pulse

                    scaleY=
                        motion.pulse
                }
                .background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to
                                p.accent.copy(
                                    alpha=
                                        if(a.darkMode)
                                            .24f
                                        else
                                            .20f
                                ),

                            .30f to
                                p.accent.copy(
                                    alpha=.14f
                                ),

                            .62f to
                                p.accent.copy(
                                    alpha=.055f
                                ),

                            1f to
                                Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        /*
         * Soft shape 2 changes geometry slightly
         * between selected animation families.
         */
        val secondShape=
            when(a.animation){
                NmixAnimationName.ORBIT->
                    RoundedCornerShape(
                        72.dp
                    )

                NmixAnimationName.FLOW->
                    RoundedCornerShape(
                        36.dp
                    )

                else->
                    CircleShape
            }

        Box(
            Modifier
                .size(300.dp)
                .align(
                    Alignment.BottomEnd
                )
                .offset(
                    x=130.dp,
                    y=130.dp
                )
                .graphicsLayer{
                    translationX=
                        -motion.x*155f

                    translationY=
                        -motion.y*75f

                    rotationZ=
                        if(
                            a.animation==
                            NmixAnimationName.ORBIT
                        )
                            motion.x*12f
                        else
                            0f
                }
                .background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to
                                p.accentLight.copy(
                                    alpha=.18f
                                ),

                            .38f to
                                p.accentLight.copy(
                                    alpha=.09f
                                ),

                            .72f to
                                p.accentLight.copy(
                                    alpha=.025f
                                ),

                            1f to
                                Color.Transparent
                        )
                    ),
                    secondShape
                )
        )
    }else{
        /*
         * HARD geometry is kept translucent so
         * it never blocks Display readability.
         */
        Canvas(
            Modifier
                .size(92.dp)
                .align(
                    Alignment.CenterStart
                )
                .offset(
                    x=(-24).dp
                )
                .graphicsLayer{
                    translationX=
                        motion.x*125f

                    translationY=
                        motion.y*38f

                    scaleX=
                        motion.pulse

                    scaleY=
                        motion.pulse

                    rotationZ=
                        motion.x*16f
                }
        ){
            when(a.animation){
                NmixAnimationName.FLOAT->{
                    drawRoundRect(
                        color=
                            p.accent.copy(
                                alpha=
                                    if(a.darkMode)
                                        .17f
                                    else
                                        .14f
                            ),
                        cornerRadius=
                            CornerRadius(
                                15.dp.toPx()
                            )
                    )
                }

                NmixAnimationName.PULSE->{
                    val path=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.06f
                            )

                            lineTo(
                                size.width*.94f,
                                size.height*.90f
                            )

                            lineTo(
                                size.width*.06f,
                                size.height*.90f
                            )

                            close()
                        }

                    drawPath(
                        path,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .18f
                                else
                                    .14f
                        )
                    )
                }

                NmixAnimationName.CROSS->{
                    val path=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                0f
                            )

                            lineTo(
                                size.width,
                                size.height*.5f
                            )

                            lineTo(
                                size.width*.5f,
                                size.height
                            )

                            lineTo(
                                0f,
                                size.height*.5f
                            )

                            close()
                        }

                    drawPath(
                        path,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .18f
                                else
                                    .14f
                        )
                    )
                }

                else->{}
            }
        }

        if(
            a.animation==
            NmixAnimationName.CROSS
        ){
            Canvas(
                Modifier
                    .size(70.dp)
                    .align(
                        Alignment.CenterEnd
                    )
                    .offset(
                        x=20.dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.z*110f

                        translationY=
                            -motion.y*28f

                        rotationZ=
                            -motion.x*16f
                    }
            ){
                val path=
                    Path().apply{
                        moveTo(
                            size.width*.5f,
                            0f
                        )

                        lineTo(
                            size.width,
                            size.height*.5f
                        )

                        lineTo(
                            size.width*.5f,
                            size.height
                        )

                        lineTo(
                            0f,
                            size.height*.5f
                        )

                        close()
                    }

                drawPath(
                    path,
                    p.accentLight.copy(
                        alpha=.13f
                    )
                )
            }
        }
    }
}

@Composable
fun NmixCalcField(
    text:String,
    modifier:Modifier=Modifier,
    shape:Shape=
        RoundedCornerShape(11.dp),
    height:Dp=46.dp,
    textSize:TextUnit=15.sp
){
    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(
                nmixScreenColor()
            )
            .border(
                .5.dp,
                nmixScreenBorder(),
                shape
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=
                nmixDisplayText(),
            fontSize=textSize,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=
                LocalNmixAppearance
                    .current
                    .fontFamily,
            maxLines=1,
            textAlign=
                TextAlign.Center
        )
    }
}

@Composable
fun NmixGlassBox(
    modifier:Modifier=Modifier,
    accentTint:Boolean=true,
    content:
        @Composable
        BoxScope.()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val shape=
        RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    p.accent.copy(
                        alpha=.065f
                    )
                else if(accentTint)
                    Color.White.copy(
                        alpha=.73f
                    )
                else
                    Color.White.copy(
                        alpha=.70f
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
            ),
        content=content
    )
}

@Composable
fun NmixPressBox(
    modifier:Modifier,
    shape:Shape,
    color:Color,
    onClick:()->Unit,
    content:@Composable ()->Unit
){
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
            dampingRatio=.72f,
            stiffness=620f
        ),
        label="press"
    )

    Box(
        modifier
            .scale(scale)
            .clip(shape)
            .background(color)
            .clickable(
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick
            ),
        contentAlignment=
            Alignment.Center
    ){
        content()
    }
}
