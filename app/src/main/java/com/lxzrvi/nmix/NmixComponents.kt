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
import androidx.compose.ui.layout.Layout
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

private fun nmixMix(
    start:Float,
    end:Float,
    progress:Float
):Float{
    val t=progress.coerceIn(0f,1f)
    return start+(end-start)*t
}

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
        targetValue=if(open)1f else 0f,
        animationSpec=tween(
            520,
            easing=EaseInOutCubic
        ),
        label="toolProgress"
    )

    val outerRotation=180f*progress
    val innerRotation=-180f*progress
    val arrowRotation=180f*progress

    val outerSize=(42f-3f*progress).dp
    val innerSize=(36f-2f*progress).dp

    val outerRadius=(8.5f+10f*progress).dp
    val innerRadius=(6.5f+9f*progress).dp

    /*
     * Label/card uses the SAME progress as icon.
     */
    val sectionRadius=(16f+7f*progress).dp
    val sectionShape=
        RoundedCornerShape(sectionRadius)

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
            .clip(sectionShape)
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
                sectionShape
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
                contentAlignment=Alignment.Center
            ){
                val outerShape=
                    RoundedCornerShape(outerRadius)

                Box(
                    Modifier
                        .size(outerSize)
                        .rotate(outerRotation)
                        .clip(outerShape)
                        .background(
                            p.accent.copy(alpha=.66f)
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
                        topLeft=Offset(sw,sw),
                        size=Size(
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

            Spacer(Modifier.width(12.dp))

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
                    .rotate(arrowRotation),
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
                    expandFrom=Alignment.Top
                )+
                fadeIn(tween(210)),
            exit=
                shrinkVertically(
                    animationSpec=tween(
                        320,
                        easing=EaseInOutCubic
                    ),
                    shrinkTowards=Alignment.Top
                )+
                fadeOut(tween(180))
        ){
            content()
        }
    }
}

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

    val shape=RoundedCornerShape(13.dp)

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
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
                            .13f
                        else
                            .23f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
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
    val a=LocalNmixAppearance.current

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
    val a=LocalNmixAppearance.current
    val p=a.palette

    NmixPressBox(
        modifier,
        RoundedCornerShape(9.dp),
        if(selected)
            Color.White.copy(alpha=.17f)
        else
            p.accent.copy(alpha=.13f),
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
    val a=LocalNmixAppearance.current
    val p=a.palette

    val shape=RoundedCornerShape(50)

    val bg=
        if(accent)
            p.accent.copy(alpha=.78f)
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
    val a=LocalNmixAppearance.current
    val p=a.palette

    val bg=when(type){
        1->p.accent.copy(alpha=.86f)

        2->
            Color(0xFFD83939)
                .copy(alpha=.17f)

        else->
            if(a.darkMode)
                Color(0xFF090C0B)
                    .copy(alpha=.75f)
            else
                p.accent.copy(alpha=.08f)
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
    val a=LocalNmixAppearance.current
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
            heightPx.toDp().value
        }

    /*
     * Normalize actual Display height.
     *
     * 1.0 = full
     * 0.0 = minimum
     */
    val maximumDisplayHeight=305f
    val minimumDisplayHeight=82f

    val displayPercent=
        if(heightPx<=0){
            1f
        }else{
            (
                (
                    heightDp-
                        minimumDisplayHeight
                    )/
                    (
                        maximumDisplayHeight-
                            minimumDisplayHeight
                    )
                )
                .coerceIn(0f,1f)
        }

    /*
     * 100-72 FULL
     * 72-62  FULL -> SEMI
     * 62-38  SEMI, continuously shrinking
     * 38-28  SEMI -> MINI
     * 28-0   MINI, continuously shrinking
     */
    val fullToSemi=
        (
            (0.72f-displayPercent)/
                .10f
        ).coerceIn(0f,1f)

    val semiShrink=
        (
            (0.62f-displayPercent)/
                .24f
        ).coerceIn(0f,1f)

    val semiToMini=
        (
            (0.38f-displayPercent)/
                .10f
        ).coerceIn(0f,1f)

    val miniShrink=
        (
            (0.28f-displayPercent)/
                .28f
        ).coerceIn(0f,1f)

    /*
     * Radius begins late during SEMI -> MINI.
     */
    val transitionRadius=
        (
            (semiToMini-.52f)/
                .48f
        ).coerceIn(0f,1f)

    val radiusProgress=
        maxOf(
            transitionRadius,
            miniShrink
        )

    val displayRadius=
        nmixMix(
            19f,
            54f,
            radiusProgress
        ).dp

    val displayShape=
        RoundedCornerShape(
            displayRadius
        )

    val calcProgress by
        animateFloatAsState(
            targetValue=
                if(calcVisible)
                    1f
                else
                    0f,
            animationSpec=tween(
                300,
                easing=EaseInOutCubic
            ),
            label="calculatorVisibility"
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
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
    ){
        DisplayMotionLayer(
            motion=motion
        )

        /*
         * Same three physical children during
         * Full -> Semi -> Mini.
         */
        if(calcVisible){
            NmixCalculatorMorphFields(
                first=
                    calcFirst.ifEmpty{
                        "_"
                    },
                operator=
                    calcOperator.ifEmpty{
                        "sign"
                    },
                second=
                    calcSecond.ifEmpty{
                        "_"
                    },
                fullToSemi=fullToSemi,
                semiShrink=semiShrink,
                semiToMini=semiToMini,
                miniShrink=miniShrink,
                displayRadiusProgress=
                    radiusProgress,
                modifier=
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer{
                            alpha=calcProgress
                        }
            )
        }

        /*
         * Main Display content.
         *
         * This is kept independent of calculator
         * fields so it can smoothly move right.
         */
        val sideProgress=
            when{
                !calcVisible->
                    0f

                semiToMini>0f->
                    1f

                else->
                    fullToSemi
            }

        val fieldWidthFraction=
            when{
                !calcVisible->
                    0f

                semiToMini>0f->
                    nmixMix(
                        .30f,
                        .48f,
                        semiToMini
                    )

                fullToSemi>0f->
                    .30f*fullToSemi

                else->
                    0f
            }

        /*
         * FULL portion.
         */
        if(displayPercent>.62f){
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
                    )
                    .graphicsLayer{
                        translationX=
                            43f*
                                sideProgress

                        translationY=
                            4f*
                                sideProgress
                    },
                color=
                    if(a.darkMode)
                        p.accentLight
                    else
                        p.accentDark,
                fontSize=
                    nmixMix(
                        9f,
                        8.3f,
                        sideProgress
                    ).sp,
                fontWeight=
                    FontWeight.Bold,
                letterSpacing=2.sp,
                fontFamily=a.fontFamily,
                maxLines=1
            )

            Text(
                value,
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            56f*
                                sideProgress

                        translationY=
                            if(calcVisible)
                                nmixMix(
                                    18f,
                                    8f,
                                    sideProgress
                                )
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
                fontSize=
                    nmixMix(
                        40f,
                        35f,
                        sideProgress
                    ).sp,
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
                    )
                    .graphicsLayer{
                        translationX=
                            42f*
                                sideProgress
                    },
                color=
                    if(a.darkMode)
                        p.accentLight.copy(
                            alpha=.78f
                        )
                    else
                        p.accentDark.copy(
                            alpha=.80f
                        ),
                fontSize=
                    nmixMix(
                        11f,
                        9.5f,
                        sideProgress
                    ).sp,
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
             * SEMI / MINI.
             */
            val contentWidth=
                if(calcVisible)
                    (
                        1f-
                            fieldWidthFraction
                    ).coerceIn(
                        .49f,
                        .70f
                    )
                else
                    .84f

            val detailsAlpha=
                (
                    1f-
                        semiToMini*.82f-
                        miniShrink*.18f
                ).coerceIn(0f,1f)

            Column(
                Modifier
                    .fillMaxWidth(
                        contentWidth
                    )
                    .align(
                        if(calcVisible)
                            Alignment.CenterEnd
                        else
                            Alignment.Center
                    )
                    .padding(
                        start=
                            if(calcVisible)
                                8.dp
                            else
                                5.dp,
                        end=10.dp
                    ),
                horizontalAlignment=
                    Alignment.CenterHorizontally,
                verticalArrangement=
                    Arrangement.Center
            ){
                if(detailsAlpha>.05f){
                    Text(
                        label,
                        Modifier.graphicsLayer{
                            alpha=
                                detailsAlpha
                        },
                        color=
                            if(a.darkMode)
                                p.accentLight
                            else
                                p.accentDark,
                        fontSize=
                            (
                                8f-
                                    semiShrink*.7f-
                                    semiToMini*.5f
                            )
                                .coerceAtLeast(
                                    6.7f
                                ).sp,
                        fontWeight=
                            FontWeight.Bold,
                        letterSpacing=1.2.sp,
                        fontFamily=a.fontFamily,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.height(
                            nmixMix(
                                3f,
                                1f,
                                semiShrink
                            ).dp
                        )
                    )
                }

                Text(
                    value,
                    color=
                        nmixDisplayText(),
                    fontSize=
                        (
                            34f-
                                semiShrink*5f-
                                semiToMini*4f-
                                miniShrink*3f
                        )
                            .coerceAtLeast(
                                22f
                            ).sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily,
                    maxLines=1,
                    textAlign=
                        TextAlign.Center
                )

                if(detailsAlpha>.15f){
                    Spacer(
                        Modifier.height(
                            nmixMix(
                                3f,
                                1f,
                                semiShrink
                            ).dp
                        )
                    )

                    Text(
                        status,
                        Modifier.graphicsLayer{
                            alpha=
                                detailsAlpha
                        },
                        color=
                            if(a.darkMode)
                                p.accentLight.copy(
                                    alpha=.76f
                                )
                            else
                                p.accentDark.copy(
                                    alpha=.78f
                                ),
                        fontSize=
                            (
                                8.5f-
                                    semiShrink*.8f-
                                    semiToMini*.4f
                            )
                                .coerceAtLeast(
                                    6.8f
                                ).sp,
                        lineHeight=10.sp,
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

        /*
         * Timer controls remain unchanged in
         * behavior, only scale with Display.
         */
        val timerSize=
            (
                47f-
                    semiShrink*7f-
                    semiToMini*5f-
                    miniShrink*4f
            )
                .coerceAtLeast(
                    31f
                ).dp

        AnimatedVisibility(
            visible=timer,
            modifier=
                Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .padding(
                        start=
                            if(displayPercent<.28f)
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
                    timerSize
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
                            if(displayPercent<.28f)
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
                    timerSize
                ),
                onClick=onPlus
            )
        }
    }
}

/*
 * ==================================================
 * THREE-FIELD PHYSICAL MORPH
 * ==================================================
 */

@Composable
private fun NmixCalculatorMorphFields(
    first:String,
    operator:String,
    second:String,
    fullToSemi:Float,
    semiShrink:Float,
    semiToMini:Float,
    miniShrink:Float,
    displayRadiusProgress:Float,
    modifier:Modifier=Modifier
){
    val density=LocalDensity.current

    fun dataScale(
        text:String
    ):Float{
        return when{
            text.length>=15->.56f
            text.length>=12->.64f
            text.length>=9->.72f
            text.length>=7->.80f
            text.length>=5->.89f
            else->1f
        }
    }

    val baseText=
        (
            14f-
                semiShrink*2.5f-
                semiToMini*1.5f-
                miniShrink*1.5f
        ).coerceAtLeast(8f)

    Layout(
        modifier=modifier,
        content={
            MorphFieldVisual(
                text=first,
                textSize=
                    (
                        baseText*
                            dataScale(first)
                    )
                        .coerceAtLeast(
                            7f
                        ).sp,
                kind=0,
                radiusProgress=
                    displayRadiusProgress,
                miniProgress=
                    semiToMini
            )

            MorphFieldVisual(
                text=operator,
                textSize=
                    (
                        baseText*.78f
                    )
                        .coerceAtLeast(
                            6.5f
                        ).sp,
                kind=1,
                radiusProgress=
                    displayRadiusProgress,
                miniProgress=
                    semiToMini
            )

            MorphFieldVisual(
                text=second,
                textSize=
                    (
                        baseText*
                            dataScale(second)
                    )
                        .coerceAtLeast(
                            7f
                        ).sp,
                kind=2,
                radiusProgress=
                    displayRadiusProgress,
                miniProgress=
                    semiToMini
            )
        }
    ){measurables,constraints->

        val width=
            constraints.maxWidth

        val height=
            constraints.maxHeight

        fun px(dpValue:Float):Float{
            return with(density){
                dpValue.dp.toPx()
            }
        }

        /*
         * ------------------------------
         * FULL
         * ------------------------------
         */
        val fullOuter=
            px(12f)

        val fullGap=
            px(7f)

        val fullTop=
            px(12f)

        val fullHeight=
            px(46f)

        val fullOperatorWidth=
            px(58f)

        val fullNumberWidth=
            (
                width-
                    fullOuter*2-
                    fullGap*2-
                    fullOperatorWidth
            )/2f

        val fullX=
            floatArrayOf(
                fullOuter,

                fullOuter+
                    fullNumberWidth+
                    fullGap,

                fullOuter+
                    fullNumberWidth+
                    fullGap+
                    fullOperatorWidth+
                    fullGap
            )

        val fullY=
            floatArrayOf(
                fullTop,
                fullTop,
                fullTop
            )

        val fullW=
            floatArrayOf(
                fullNumberWidth,
                fullOperatorWidth,
                fullNumberWidth
            )

        val fullH=
            floatArrayOf(
                fullHeight,
                fullHeight,
                fullHeight
            )

        /*
         * ------------------------------
         * SEMI
         * ------------------------------
         *
         * Outer top/bottom gaps equal.
         * Inner gaps equal and smaller.
         */
        val semiWidth=
            width*
                nmixMix(
                    .30f,
                    .27f,
                    semiShrink
                )

        val semiLeft=
            nmixMix(
                px(7f),
                px(5f),
                semiShrink
            )

        val outerGap=
            maxOf(
                nmixMix(
                    height*.065f,
                    height*.045f,
                    semiShrink
                ),
                px(3f)
            )

        val innerGap=
            nmixMix(
                px(5f),
                px(2.5f),
                semiShrink
            )

        val availableSemiHeight=
            (
                height-
                    outerGap*2-
                    innerGap*2
            ).coerceAtLeast(
                px(54f)
            )

        val heightRatio=
            1f+
                .78f+
                1f

        val semiNumberHeight=
            availableSemiHeight/
                heightRatio

        val semiOperatorHeight=
            semiNumberHeight*
                .78f

        val semiX=
            floatArrayOf(
                semiLeft,
                semiLeft,
                semiLeft
            )

        val semiY=
            floatArrayOf(
                outerGap,

                outerGap+
                    semiNumberHeight+
                    innerGap,

                outerGap+
                    semiNumberHeight+
                    innerGap+
                    semiOperatorHeight+
                    innerGap
            )

        val semiW=
            floatArrayOf(
                semiWidth,
                semiWidth,
                semiWidth
            )

        val semiH=
            floatArrayOf(
                semiNumberHeight,
                semiOperatorHeight,
                semiNumberHeight
            )

        /*
         * ------------------------------
         * MINI
         * ------------------------------
         */
        val miniLeft=
            nmixMix(
                px(7f),
                px(5f),
                miniShrink
            )

        val miniTotalWidth=
            width*
                nmixMix(
                    .45f,
                    .48f,
                    miniShrink
                )

        val miniGap=
            nmixMix(
                px(4f),
                px(2f),
                miniShrink
            )

        val miniOperatorWidth=
            miniTotalWidth*
                .25f

        val miniNumberWidth=
            (
                miniTotalWidth-
                    miniOperatorWidth-
                    miniGap*2
            )/2f

        val miniOuterVertical=
            nmixMix(
                px(8f),
                px(4f),
                miniShrink
            )

        val miniHeight=
            (
                height-
                    miniOuterVertical*2
            ).coerceAtLeast(
                px(25f)
            )

        val miniY=
            (
                height-
                    miniHeight
            )/2f

        val miniX=
            floatArrayOf(
                miniLeft,

                miniLeft+
                    miniNumberWidth+
                    miniGap,

                miniLeft+
                    miniNumberWidth+
                    miniGap+
                    miniOperatorWidth+
                    miniGap
            )

        val miniYs=
            floatArrayOf(
                miniY,
                miniY,
                miniY
            )

        val miniW=
            floatArrayOf(
                miniNumberWidth,
                miniOperatorWidth,
                miniNumberWidth
            )

        val miniH=
            floatArrayOf(
                miniHeight,
                miniHeight,
                miniHeight
            )

        /*
         * FULL -> SEMI.
         */
        val stageX=FloatArray(3)
        val stageY=FloatArray(3)
        val stageW=FloatArray(3)
        val stageH=FloatArray(3)

        repeat(3){index->
            stageX[index]=
                nmixMix(
                    fullX[index],
                    semiX[index],
                    fullToSemi
                )

            stageY[index]=
                nmixMix(
                    fullY[index],
                    semiY[index],
                    fullToSemi
                )

            stageW[index]=
                nmixMix(
                    fullW[index],
                    semiW[index],
                    fullToSemi
                )

            stageH[index]=
                nmixMix(
                    fullH[index],
                    semiH[index],
                    fullToSemi
                )
        }

        /*
         * SEMI -> MINI.
         */
        val finalX=FloatArray(3)
        val finalY=FloatArray(3)
        val finalW=FloatArray(3)
        val finalH=FloatArray(3)

        repeat(3){index->
            finalX[index]=
                nmixMix(
                    stageX[index],
                    miniX[index],
                    semiToMini
                )

            finalY[index]=
                nmixMix(
                    stageY[index],
                    miniYs[index],
                    semiToMini
                )

            finalW[index]=
                nmixMix(
                    stageW[index],
                    miniW[index],
                    semiToMini
                )

            finalH[index]=
                nmixMix(
                    stageH[index],
                    miniH[index],
                    semiToMini
                )
        }

        val placeables=
            measurables.mapIndexed{
                index,
                measurable->

                val childWidth=
                    finalW[index]
                        .toInt()
                        .coerceAtLeast(1)
                        .coerceAtMost(width)

                val childHeight=
                    finalH[index]
                        .toInt()
                        .coerceAtLeast(1)
                        .coerceAtMost(height)

                measurable.measure(
                    androidx.compose.ui.unit
                        .Constraints.fixed(
                            childWidth,
                            childHeight
                        )
                )
            }

        layout(width,height){
            placeables.forEachIndexed{
                index,
                placeable->

                val maxX=
                    (
                        width-
                            placeable.width
                    ).coerceAtLeast(0)

                val maxY=
                    (
                        height-
                            placeable.height
                    ).coerceAtLeast(0)

                placeable.placeRelative(
                    x=
                        finalX[index]
                            .toInt()
                            .coerceIn(
                                0,
                                maxX
                            ),
                    y=
                        finalY[index]
                            .toInt()
                            .coerceIn(
                                0,
                                maxY
                            )
                )
            }
        }
    }
}

@Composable
private fun MorphFieldVisual(
    text:String,
    textSize:TextUnit,
    kind:Int,
    radiusProgress:Float,
    miniProgress:Float
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val outerRadius=
        nmixMix(
            6f,
            30f,
            radiusProgress
        ).dp

    /*
     * During MINI, fields become a connected
     * horizontal family without rounding every
     * internal edge.
     */
    val mini=
        miniProgress.coerceIn(
            0f,
            1f
        )

    val shape=
        when(kind){
            0->
                RoundedCornerShape(
                    topStart=
                        outerRadius,
                    bottomStart=
                        nmixMix(
                            3f,
                            18f,
                            mini*
                                radiusProgress
                        ).dp,
                    topEnd=
                        nmixMix(
                            3f,
                            6f,
                            mini
                        ).dp,
                    bottomEnd=
                        nmixMix(
                            3f,
                            6f,
                            mini
                        ).dp
                )

            1->
                RoundedCornerShape(
                    nmixMix(
                        0f,
                        4f,
                        mini
                    ).dp
                )

            else->
                RoundedCornerShape(
                    topStart=
                        nmixMix(
                            3f,
                            5f,
                            mini
                        ).dp,
                    bottomStart=
                        if(mini<.5f)
                            outerRadius
                        else
                            5.dp,
                    topEnd=
                        nmixMix(
                            3f,
                            18f,
                            mini*
                                radiusProgress
                        ).dp,
                    bottomEnd=
                        nmixMix(
                            3f,
                            18f,
                            mini*
                                radiusProgress
                        ).dp
                )
        }

    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF1A201E)
                        .copy(alpha=.86f)
                else
                    Color(0xFFE4E8E6)
                        .copy(alpha=.89f)
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
            fontFamily=a.fontFamily,
            maxLines=1,
            textAlign=
                TextAlign.Center
        )
    }
}
/*
 * ==================================================
 * DISPLAY MOTION
 * ==================================================
 */

private data class DisplayMotionPlacement(
    val x:Float,
    val y:Float,
    val dx:Float,
    val dy:Float,
    val scale:Float,
    val phase:Float
)

/*
 * Elements start in different areas instead of
 * stacking at one center point.
 */
private val displayMotionPlacements=
    listOf(
        DisplayMotionPlacement(
            -.31f,
            -.24f,
            1.00f,
            .74f,
            .92f,
            .00f
        ),

        DisplayMotionPlacement(
            .29f,
            .23f,
            -.88f,
            -.96f,
            .74f,
            .37f
        ),

        DisplayMotionPlacement(
            .28f,
            -.27f,
            .76f,
            -.82f,
            .62f,
            .68f
        ),

        DisplayMotionPlacement(
            -.29f,
            .27f,
            -.72f,
            .86f,
            .68f,
            .91f
        ),

        DisplayMotionPlacement(
            .02f,
            .02f,
            .58f,
            -.61f,
            .56f,
            .52f
        )
    )

@Composable
private fun BoxScope.DisplayMotionLayer(
    motion:NmixMotionValues
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val soft=
        a.animation in listOf(
            NmixAnimationName.DRIFT,
            NmixAnimationName.ORBIT,
            NmixAnimationName.FLOW
        )

    val quantity=
        a.animationQuantity
            .coerceIn(
                1,
                5
            )

    repeat(quantity){index->
        val item=
            displayMotionPlacements[
                index%
                    displayMotionPlacements.size
            ]

        /*
         * Different combinations of x/y/z create
         * independent-looking paths while still
         * staying inside an imaginary bounded area.
         *
         * Reverse infinite animation in
         * rememberNmixMotion makes motion turn at
         * path limits like a soft wall bounce.
         */
        val mx=
            when(index){
                0->motion.x
                1->motion.z
                2->-motion.y
                3->-motion.x
                else->motion.y
            }

        val my=
            when(index){
                0->motion.y
                1->-motion.x
                2->motion.z
                3->-motion.z
                else->-motion.x
            }

        val direction=
            if(index%2==0)
                1f
            else
                -1f

        if(soft){
            val geometryShape=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        CircleShape

                    NmixAnimationName.ORBIT->
                        RoundedCornerShape(
                            48.dp
                        )

                    NmixAnimationName.FLOW->
                        RoundedCornerShape(
                            28.dp
                        )

                    else->
                        CircleShape
                }

            /*
             * SOFT:
             * larger feathered geometry.
             * Shape remains visible but does not
             * become a hard solid chunk.
             */
            val baseSize=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        210f

                    NmixAnimationName.ORBIT->
                        175f

                    NmixAnimationName.FLOW->
                        165f

                    else->
                        190f
                }

            Box(
                Modifier
                    .size(
                        (
                            baseSize*
                                item.scale
                        ).dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=
                            (
                                item.x*
                                    150f
                            ).dp,
                        y=
                            (
                                item.y*
                                    112f
                            ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                72f*
                                item.dx

                        translationY=
                            my*
                                46f*
                                item.dy

                        val localPulse=
                            motion.pulse*
                                (
                                    .94f+
                                        item.phase*
                                        .08f
                                    )

                        scaleX=localPulse
                        scaleY=localPulse

                        rotationZ=
                            when(
                                a.animation
                            ){
                                NmixAnimationName.ORBIT->
                                    motion.z*
                                        14f*
                                        direction

                                NmixAnimationName.FLOW->
                                    motion.x*
                                        7f*
                                        direction

                                else->
                                    0f
                            }
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=
                                arrayOf(
                                    0f to
                                        (
                                            if(
                                                index%2==0
                                            )
                                                p.accent
                                            else
                                                p.accentLight
                                        ).copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .27f
                                                else
                                                    .22f
                                        ),

                                    .36f to
                                        p.accent.copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .17f
                                                else
                                                    .14f
                                        ),

                                    .65f to
                                        p.accent.copy(
                                            alpha=.075f
                                        ),

                                    .84f to
                                        p.accent.copy(
                                            alpha=.022f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        ),
                        geometryShape
                    )
            )
        }else{
            /*
             * HARD:
             * actual recognisable shape,
             * lower opacity,
             * mildly feathered outer edge.
             */
            val hardSize=
                (
                    78f*
                        item.scale
                )
                    .coerceAtLeast(
                        38f
                    )

            Canvas(
                Modifier
                    .size(
                        hardSize.dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=
                            (
                                item.x*
                                    155f
                            ).dp,
                        y=
                            (
                                item.y*
                                    112f
                            ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                82f*
                                item.dx

                        translationY=
                            my*
                                52f*
                                item.dy

                        if(
                            a.animation==
                            NmixAnimationName.PULSE
                        ){
                            scaleX=
                                motion.pulse

                            scaleY=
                                motion.pulse
                        }

                        rotationZ=
                            motion.z*
                                14f*
                                direction
                    }
            ){
                val color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val mainAlpha=
                    if(a.darkMode)
                        .17f
                    else
                        .14f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        /*
                         * Feather shell.
                         */
                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.045f
                                ),
                            cornerRadius=
                                CornerRadius(
                                    16.dp.toPx()
                                )
                        )

                        val outerInset=
                            2.dp.toPx()

                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.065f
                                ),
                            topLeft=
                                Offset(
                                    outerInset,
                                    outerInset
                                ),
                            size=
                                Size(
                                    size.width-
                                        outerInset*2,
                                    size.height-
                                        outerInset*2
                                ),
                            cornerRadius=
                                CornerRadius(
                                    14.dp.toPx()
                                )
                        )

                        val inset=
                            4.dp.toPx()

                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=
                                        mainAlpha
                                ),
                            topLeft=
                                Offset(
                                    inset,
                                    inset
                                ),
                            size=
                                Size(
                                    size.width-
                                        inset*2,
                                    size.height-
                                        inset*2
                                ),
                            cornerRadius=
                                CornerRadius(
                                    12.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val outerPath=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.035f
                                )

                                lineTo(
                                    size.width*.965f,
                                    size.height*.91f
                                )

                                lineTo(
                                    size.width*.035f,
                                    size.height*.91f
                                )

                                close()
                            }

                        drawPath(
                            outerPath,
                            color.copy(
                                alpha=.045f
                            )
                        )

                        val innerPath=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.10f
                                )

                                lineTo(
                                    size.width*.90f,
                                    size.height*.86f
                                )

                                lineTo(
                                    size.width*.10f,
                                    size.height*.86f
                                )

                                close()
                            }

                        drawPath(
                            innerPath,
                            color.copy(
                                alpha=
                                    mainAlpha
                            )
                        )

                        drawPath(
                            innerPath,
                            color.copy(
                                alpha=.055f
                            ),
                            style=Stroke(
                                2.dp.toPx()
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val outerPath=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.02f
                                )

                                lineTo(
                                    size.width*.98f,
                                    size.height*.5f
                                )

                                lineTo(
                                    size.width*.5f,
                                    size.height*.98f
                                )

                                lineTo(
                                    size.width*.02f,
                                    size.height*.5f
                                )

                                close()
                            }

                        drawPath(
                            outerPath,
                            color.copy(
                                alpha=.04f
                            )
                        )

                        val innerPath=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.08f
                                )

                                lineTo(
                                    size.width*.92f,
                                    size.height*.5f
                                )

                                lineTo(
                                    size.width*.5f,
                                    size.height*.92f
                                )

                                lineTo(
                                    size.width*.08f,
                                    size.height*.5f
                                )

                                close()
                            }

                        drawPath(
                            innerPath,
                            color.copy(
                                alpha=
                                    mainAlpha
                            )
                        )

                        drawPath(
                            innerPath,
                            color.copy(
                                alpha=.055f
                            ),
                            style=Stroke(
                                2.dp.toPx()
                            )
                        )
                    }

                    else->{}
                }
            }
        }
    }
}

/*
 * ==================================================
 * CALCULATOR FIELD
 * ==================================================
 */

@Composable
fun NmixCalcField(
    text:String,
    modifier:Modifier=Modifier,
    shape:Shape=
        RoundedCornerShape(11.dp),
    height:Dp?=46.dp,
    textSize:TextUnit=15.sp
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val heightModifier=
        if(height!=null)
            Modifier.height(height)
        else
            Modifier

    Box(
        modifier
            .then(heightModifier)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF1A201E)
                        .copy(alpha=.86f)
                else
                    Color(0xFFE4E8E6)
                        .copy(alpha=.89f)
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
            fontFamily=a.fontFamily,
            maxLines=1,
            textAlign=
                TextAlign.Center
        )
    }
}
/*
 * ==================================================
 * GLASS BOX
 * ==================================================
 */

@Composable
fun NmixGlassBox(
    modifier:Modifier=Modifier,
    accentTint:Boolean=true,
    content:
        @Composable
        BoxScope.()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val shape=
        RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode){
                    Color(0xFF151A18)
                        .copy(alpha=.80f)
                }else{
                    /*
                     * Light mode keeps translucency,
                     * but no washed-out pure white
                     * glass surface.
                     */
                    Color(0xFFE6EAE8)
                        .copy(alpha=.88f)
                }
            )
            .background(
                if(accentTint){
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .055f
                            else
                                .045f
                    )
                }else{
                    Color.Transparent
                }
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .12f
                        else
                            .21f
                ),
                shape
            ),
        content=content
    )
}

/*
 * ==================================================
 * PRESS BOX
 * ==================================================
 */

@Composable
fun NmixPressBox(
    modifier:Modifier,
    shape:Shape,
    color:Color,
    onClick:()->Unit,
    content:@Composable ()->Unit
){
    val interaction=
        remember{
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
            animationSpec=spring(
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
