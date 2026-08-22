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
    val t=
        progress.coerceIn(0f,1f)

    return start+
        (end-start)*t
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
        targetValue=
            if(open)1f else 0f,
        animationSpec=tween(
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

    /*
     * Uses the same progress as the icon.
     */
    val sectionRadius=
        (16f+7f*progress).dp

    val sectionShape=
        RoundedCornerShape(
            sectionRadius
        )

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
                    interactionSource=
                        remember{
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
                    expandFrom=
                        Alignment.Top
                )+
                fadeIn(tween(210)),
            exit=
                shrinkVertically(
                    animationSpec=tween(
                        320,
                        easing=EaseInOutCubic
                    ),
                    shrinkTowards=
                        Alignment.Top
                )+
                fadeOut(tween(180))
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

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val pressScale by
        animateFloatAsState(
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
            .scale(pressScale)
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
    val a=LocalNmixAppearance.current

    NmixPressBox(
        modifier=modifier,
        shape=CircleShape,
        color=
            color
                ?:a.palette.accent.copy(
                    alpha=.80f
                ),
        onClick=onClick
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
        modifier=modifier,
        shape=
            RoundedCornerShape(9.dp),
        color=
            if(selected)
                Color.White.copy(
                    alpha=.17f
                )
            else
                p.accent.copy(
                    alpha=.13f
                ),
        onClick=onClick
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
            modifier=
                Modifier.fillMaxSize(),
            shape=shape,
            color=Color.Transparent,
            onClick=onClick
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
        modifier=modifier,
        shape=CircleShape,
        color=bg,
        onClick=onClick
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

    val interaction=
        remember{
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
     * Normalized Display height:
     * 1 = Full
     * 0 = minimum Small
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
            ).coerceIn(
                0f,
                1f
            )
        }

    /*
     * Full -> Half transition.
     */
    val fullToHalf=
        (
            (0.72f-displayPercent)/
                .14f
        ).coerceIn(
            0f,
            1f
        )

    /*
     * Half occupies the longest range.
     */
    val halfShrink=
        (
            (0.58f-displayPercent)/
                .34f
        ).coerceIn(
            0f,
            1f
        )

    /*
     * Direct Half -> Small.
     * No extra intermediate layout.
     */
    val halfToSmall=
        (
            (0.24f-displayPercent)/
                .14f
        ).coerceIn(
            0f,
            1f
        )

    val smallShrink=
        (
            (0.10f-displayPercent)/
                .10f
        ).coerceIn(
            0f,
            1f
        )

    /*
     * Display radius changes late,
     * only as Small begins.
     */
    val radiusProgress=
        maxOf(
            (
                (halfToSmall-.40f)/
                    .60f
            ).coerceIn(
                0f,
                1f
            ),
            smallShrink
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

    val calcAlpha by
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
                interactionSource=
                    interaction,
                indication=null,
                onClick=onClick
            )
    ){
        DisplayMotionLayer(
            motion=motion
        )

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
                fullToHalf=
                    fullToHalf,
                halfShrink=
                    halfShrink,
                halfToSmall=
                    halfToSmall,
                smallShrink=
                    smallShrink,
                radiusProgress=
                    radiusProgress,
                modifier=
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer{
                            alpha=calcAlpha
                        }
            )
        }

        val fieldFraction=
            when{
                !calcVisible->
                    0f

                halfToSmall>0f->
                    nmixMix(
                        .30f,
                        .48f,
                        halfToSmall
                    )

                fullToHalf>0f->
                    .30f*
                        fullToHalf

                else->
                    0f
            }

        val rightShift=
            when{
                !calcVisible->
                    0f

                halfToSmall>0f->
                    1f

                else->
                    fullToHalf
            }

        /*
         * Full + Full->Half.
         */
        if(displayPercent>.58f){
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
                                rightShift

                        translationY=
                            4f*
                                rightShift
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
                        rightShift
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
                                rightShift

                        translationY=
                            if(calcVisible)
                                nmixMix(
                                    18f,
                                    7f,
                                    rightShift
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
                        rightShift
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
                                rightShift
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
                        rightShift
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
             * Half + Small.
             */
            val contentWidth=
                if(calcVisible)
                    (
                        1f-
                            fieldFraction
                    ).coerceIn(
                        .49f,
                        .70f
                    )
                else
                    .84f

            val detailsAlpha=
                (
                    1f-
                        halfToSmall*.82f-
                        smallShrink*.18f
                ).coerceIn(
                    0f,
                    1f
                )

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
                                    halfShrink*.8f-
                                    halfToSmall*.45f
                            )
                                .coerceAtLeast(
                                    6.6f
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
                                halfShrink
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
                                halfShrink*6f-
                                halfToSmall*4f-
                                smallShrink*2f
                        )
                            .coerceAtLeast(
                                21f
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
                                halfShrink
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
                                    halfShrink*.9f-
                                    halfToSmall*.4f
                            )
                                .coerceAtLeast(
                                    6.7f
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

        val timerSize=
            (
                47f-
                    halfShrink*8f-
                    halfToSmall*5f-
                    smallShrink*3f
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
                            if(displayPercent<.18f)
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
                Modifier.size(timerSize),
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
                            if(displayPercent<.18f)
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
                Modifier.size(timerSize),
                onClick=onPlus
            )
        }
    }
}

/*
 * ==================================================
 * CALCULATOR THREE-FIELD MORPH
 * ==================================================
 */

@Composable
private fun NmixCalculatorMorphFields(
    first:String,
    operator:String,
    second:String,
    fullToHalf:Float,
    halfShrink:Float,
    halfToSmall:Float,
    smallShrink:Float,
    radiusProgress:Float,
    modifier:Modifier=Modifier
){
    val density=
        LocalDensity.current

    fun dataScale(
        text:String
    ):Float{
        return when{
            text.length>=15->.55f
            text.length>=12->.63f
            text.length>=9->.71f
            text.length>=7->.79f
            text.length>=5->.88f
            else->1f
        }
    }

    val baseText=
        (
            14f-
                halfShrink*3f-
                halfToSmall*1.3f-
                smallShrink*1.2f
        ).coerceAtLeast(
            7.8f
        )

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
                            6.8f
                        ).sp,
                kind=0,
                fullToHalf=fullToHalf,
                halfToSmall=halfToSmall,
                radiusProgress=
                    radiusProgress
            )

            MorphFieldVisual(
                text=operator,
                textSize=
                    (
                        baseText*.78f
                    )
                        .coerceAtLeast(
                            6.3f
                        ).sp,
                kind=1,
                fullToHalf=fullToHalf,
                halfToSmall=halfToSmall,
                radiusProgress=
                    radiusProgress
            )

            MorphFieldVisual(
                text=second,
                textSize=
                    (
                        baseText*
                            dataScale(second)
                    )
                        .coerceAtLeast(
                            6.8f
                        ).sp,
                kind=2,
                fullToHalf=fullToHalf,
                halfToSmall=halfToSmall,
                radiusProgress=
                    radiusProgress
            )
        }
    ){measurables,constraints->
        val width=
            constraints.maxWidth

        val height=
            constraints.maxHeight

        fun px(value:Float):Float{
            return with(density){
                value.dp.toPx()
            }
        }

        /*
         * FULL:
         * [1][sign][2]
         * across top.
         */
        val fullOuter=
            px(12f)

        val fullGap=
            px(7f)

        val fullTop=
            px(12f)

        val fullHeight=
            px(46f)

        val fullSignWidth=
            px(58f)

        val fullNumberWidth=
            (
                width-
                    fullOuter*2-
                    fullGap*2-
                    fullSignWidth
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
                    fullSignWidth+
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
                fullSignWidth,
                fullNumberWidth
            )

        val fullH=
            floatArrayOf(
                fullHeight,
                fullHeight,
                fullHeight
            )

        /*
         * HALF:
         *
         * outer gap
         * 1
         * small gap
         * sign
         * small gap
         * 2
         * outer gap
         *
         * outer gaps are equal.
         * inner gaps are equal.
         */
        val halfWidth=
            width*
                nmixMix(
                    .31f,
                    .27f,
                    halfShrink
                )

        val halfLeft=
            nmixMix(
                px(7f),
                px(5f),
                halfShrink
            )

        val outerGap=
            maxOf(
                nmixMix(
                    height*.065f,
                    height*.040f,
                    halfShrink
                ),
                px(2.5f)
            )

        val innerGap=
            nmixMix(
                px(5f),
                px(2f),
                halfShrink
            )

        val availableHeight=
            (
                height-
                    outerGap*2-
                    innerGap*2
            ).coerceAtLeast(
                px(48f)
            )

        val ratio=2.78f

        val numberHeight=
            availableHeight/
                ratio

        val signHeight=
            numberHeight*.78f

        val halfX=
            floatArrayOf(
                halfLeft,
                halfLeft,
                halfLeft
            )

        val halfY=
            floatArrayOf(
                outerGap,

                outerGap+
                    numberHeight+
                    innerGap,

                outerGap+
                    numberHeight+
                    innerGap+
                    signHeight+
                    innerGap
            )

        val halfW=
            floatArrayOf(
                halfWidth,
                halfWidth,
                halfWidth
            )

        val halfH=
            floatArrayOf(
                numberHeight,
                signHeight,
                numberHeight
            )

        /*
         * SMALL:
         * [1][sign][2] on left.
         */
        val smallLeft=
            nmixMix(
                px(7f),
                px(4f),
                smallShrink
            )

        val smallTotalWidth=
            width*
                nmixMix(
                    .45f,
                    .48f,
                    smallShrink
                )

        val smallGap=
            nmixMix(
                px(4f),
                px(2f),
                smallShrink
            )

        val smallSignWidth=
            smallTotalWidth*.24f

        val smallNumberWidth=
            (
                smallTotalWidth-
                    smallSignWidth-
                    smallGap*2
            )/2f

        val smallOuterY=
            nmixMix(
                px(7f),
                px(3f),
                smallShrink
            )

        val smallHeight=
            (
                height-
                    smallOuterY*2
            ).coerceAtLeast(
                px(24f)
            )

        val smallY=
            (
                height-
                    smallHeight
            )/2f

        val smallX=
            floatArrayOf(
                smallLeft,

                smallLeft+
                    smallNumberWidth+
                    smallGap,

                smallLeft+
                    smallNumberWidth+
                    smallGap+
                    smallSignWidth+
                    smallGap
            )

        val smallYs=
            floatArrayOf(
                smallY,
                smallY,
                smallY
            )

        val smallW=
            floatArrayOf(
                smallNumberWidth,
                smallSignWidth,
                smallNumberWidth
            )

        val smallH=
            floatArrayOf(
                smallHeight,
                smallHeight,
                smallHeight
            )

        /*
         * Direct FULL -> HALF.
         */
        val halfStageX=
            FloatArray(3)

        val halfStageY=
            FloatArray(3)

        val halfStageW=
            FloatArray(3)

        val halfStageH=
            FloatArray(3)

        repeat(3){index->
            halfStageX[index]=
                nmixMix(
                    fullX[index],
                    halfX[index],
                    fullToHalf
                )

            halfStageY[index]=
                nmixMix(
                    fullY[index],
                    halfY[index],
                    fullToHalf
                )

            halfStageW[index]=
                nmixMix(
                    fullW[index],
                    halfW[index],
                    fullToHalf
                )

            halfStageH[index]=
                nmixMix(
                    fullH[index],
                    halfH[index],
                    fullToHalf
                )
        }

        /*
         * Direct HALF -> SMALL.
         */
        val finalX=
            FloatArray(3)

        val finalY=
            FloatArray(3)

        val finalW=
            FloatArray(3)

        val finalH=
            FloatArray(3)

        repeat(3){index->
            finalX[index]=
                nmixMix(
                    halfStageX[index],
                    smallX[index],
                    halfToSmall
                )

            finalY[index]=
                nmixMix(
                    halfStageY[index],
                    smallYs[index],
                    halfToSmall
                )

            finalW[index]=
                nmixMix(
                    halfStageW[index],
                    smallW[index],
                    halfToSmall
                )

            finalH[index]=
                nmixMix(
                    halfStageH[index],
                    smallH[index],
                    halfToSmall
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
                        .coerceAtMost(
                            width.coerceAtLeast(1)
                        )

                val childHeight=
                    finalH[index]
                        .toInt()
                        .coerceAtLeast(1)
                        .coerceAtMost(
                            height.coerceAtLeast(1)
                        )

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

/*
 * Exact radius rules:
 *
 * FULL
 * 1    -> top-left only
 * sign -> square
 * 2    -> top-right only
 *
 * HALF
 * 1    -> top-left only
 * sign -> square
 * 2    -> bottom-left only
 *
 * SMALL
 * 1    -> top-left + bottom-left
 * sign -> square
 * 2    -> square
 */
@Composable
private fun MorphFieldVisual(
    text:String,
    textSize:TextUnit,
    kind:Int,
    fullToHalf:Float,
    halfToSmall:Float,
    radiusProgress:Float
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val full=
        1f-
            fullToHalf

    val small=
        halfToSmall

    val normalRadius=
        11f

    val pillRadius=
        nmixMix(
            11f,
            34f,
            radiusProgress
        )

    val shape=
        when(kind){
            0->{
                val topLeft=
                    nmixMix(
                        normalRadius,
                        pillRadius,
                        small
                    )

                val bottomLeft=
                    nmixMix(
                        0f,
                        pillRadius,
                        small
                    )

                RoundedCornerShape(
                    topStart=topLeft.dp,
                    topEnd=0.dp,
                    bottomEnd=0.dp,
                    bottomStart=
                        bottomLeft.dp
                )
            }

            1->
                RoundedCornerShape(0.dp)

            else->{
                val topRight=
                    normalRadius*
                        full

                val bottomLeft=
                    normalRadius*
                        fullToHalf*
                        (1f-small)

                RoundedCornerShape(
                    topStart=0.dp,
                    topEnd=topRight.dp,
                    bottomEnd=0.dp,
                    bottomStart=
                        bottomLeft.dp
                )
            }
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
            .coerceIn(1,5)

    /*
     * Home points are deliberately separated.
     */
    val homes=
        listOf(
            Offset(-.40f,-.31f),
            Offset(.40f,.30f),
            Offset(.39f,-.32f),
            Offset(-.39f,.32f),
            Offset(.02f,.02f)
        )

    repeat(quantity){index->
        val home=homes[index]

        val direction=
            if(index%2==0)
                1f
            else
                -1f

        val mx=when(index){
            0->motion.x
            1->motion.z
            2->-motion.y
            3->-motion.x
            else->motion.y
        }

        val my=when(index){
            0->motion.y
            1->-motion.x
            2->motion.z
            3->-motion.z
            else->-motion.x
        }

        val itemScale=when(index){
            0->1f
            1->.82f
            2->.70f
            3->.76f
            else->.64f
        }

        if(soft){
            val shape=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        CircleShape

                    NmixAnimationName.ORBIT->
                        RoundedCornerShape(
                            52.dp
                        )

                    NmixAnimationName.FLOW->
                        RoundedCornerShape(
                            30.dp
                        )

                    else->
                        CircleShape
                }

            val baseSize=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        285f

                    NmixAnimationName.ORBIT->
                        245f

                    NmixAnimationName.FLOW->
                        225f

                    else->
                        250f
                }

            Box(
                Modifier
                    .size(
                        (
                            baseSize*
                                itemScale
                        ).dp
                    )
                    .align(Alignment.Center)
                    .offset(
                        x=
                            (
                                home.x*
                                    205f
                            ).dp,
                        y=
                            (
                                home.y*
                                    150f
                            ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    125f+
                                        index*10f
                                )

                        translationY=
                            my*
                                (
                                    78f+
                                        index*7f
                                )

                        val pulse=
                            motion.pulse*
                                when(index){
                                    0->1f
                                    1->.96f
                                    2->1.04f
                                    3->.98f
                                    else->1.02f
                                }

                        scaleX=pulse
                        scaleY=pulse

                        rotationZ=
                            when(a.animation){
                                NmixAnimationName.ORBIT->
                                    motion.z*
                                        18f*
                                        direction

                                NmixAnimationName.FLOW->
                                    motion.x*
                                        10f*
                                        direction

                                else->0f
                            }
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=
                                arrayOf(
                                    0f to
                                        (
                                            if(index%2==0)
                                                p.accent
                                            else
                                                p.accentLight
                                        ).copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .30f
                                                else
                                                    .24f
                                        ),

                                    .30f to
                                        p.accent.copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .19f
                                                else
                                                    .16f
                                        ),

                                    .58f to
                                        p.accent.copy(
                                            alpha=.09f
                                        ),

                                    .78f to
                                        p.accent.copy(
                                            alpha=.035f
                                        ),

                                    .92f to
                                        p.accent.copy(
                                            alpha=.009f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        ),
                        shape
                    )
            )
        }else{
            val hardSize=
                when(index){
                    0->122f
                    1->103f
                    2->91f
                    3->98f
                    else->82f
                }

            Canvas(
                Modifier
                    .size(hardSize.dp)
                    .align(Alignment.Center)
                    .offset(
                        x=
                            (
                                home.x*
                                    205f
                            ).dp,
                        y=
                            (
                                home.y*
                                    150f
                            ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    132f+
                                        index*9f
                                )

                        translationY=
                            my*
                                (
                                    82f+
                                        index*6f
                                )

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
                                18f*
                                direction
                    }
            ){
                val color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val alpha=
                    if(a.darkMode)
                        .16f
                    else
                        .13f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.035f
                                ),
                            cornerRadius=
                                CornerRadius(
                                    20.dp.toPx()
                                )
                        )

                        val shell=
                            3.dp.toPx()

                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.065f
                                ),
                            topLeft=
                                Offset(
                                    shell,
                                    shell
                                ),
                            size=
                                Size(
                                    size.width-shell*2,
                                    size.height-shell*2
                                ),
                            cornerRadius=
                                CornerRadius(
                                    17.dp.toPx()
                                )
                        )

                        val inset=
                            7.dp.toPx()

                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=alpha
                                ),
                            topLeft=
                                Offset(
                                    inset,
                                    inset
                                ),
                            size=
                                Size(
                                    size.width-inset*2,
                                    size.height-inset*2
                                ),
                            cornerRadius=
                                CornerRadius(
                                    14.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val outer=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.02f
                                )

                                lineTo(
                                    size.width*.98f,
                                    size.height*.93f
                                )

                                lineTo(
                                    size.width*.02f,
                                    size.height*.93f
                                )

                                close()
                            }

                        drawPath(
                            outer,
                            color.copy(
                                alpha=.035f
                            )
                        )

                        val inner=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.09f
                                )

                                lineTo(
                                    size.width*.91f,
                                    size.height*.87f
                                )

                                lineTo(
                                    size.width*.09f,
                                    size.height*.87f
                                )

                                close()
                            }

                        drawPath(
                            inner,
                            color.copy(
                                alpha=alpha
                            )
                        )

                        drawPath(
                            inner,
                            color.copy(
                                alpha=.055f
                            ),
                            style=Stroke(
                                2.5.dp.toPx()
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val outer=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.01f
                                )

                                lineTo(
                                    size.width*.99f,
                                    size.height*.5f
                                )

                                lineTo(
                                    size.width*.5f,
                                    size.height*.99f
                                )

                                lineTo(
                                    size.width*.01f,
                                    size.height*.5f
                                )

                                close()
                            }

                        drawPath(
                            outer,
                            color.copy(
                                alpha=.035f
                            )
                        )

                        val inner=
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
                            inner,
                            color.copy(
                                alpha=alpha
                            )
                        )

                        drawPath(
                            inner,
                            color.copy(
                                alpha=.05f
                            ),
                            style=Stroke(
                                2.5.dp.toPx()
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
    val a=LocalNmixAppearance.current
    val haptic=rememberNmixHapticAction()

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val pressScale by
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
            .scale(pressScale)
            .clip(shape)
            .background(color)
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                haptic(onClick)
            },
        contentAlignment=
            Alignment.Center
    ){
        content()
    }
}
