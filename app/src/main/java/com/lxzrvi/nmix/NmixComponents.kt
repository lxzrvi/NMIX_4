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
        Color(0xFF111614)
    else
        Color(0xFFF8F9F8)
}

@Composable
private fun nmixScreenBorder():Color{
    val a=LocalNmixAppearance.current

    return a.palette.accent.copy(
        alpha=
            if(a.darkMode)
                .15f
            else
                .23f
    )
}

@Composable
private fun nmixDisplayText():Color{
    val a=LocalNmixAppearance.current

    return if(a.darkMode)
        Color.White.copy(alpha=.93f)
    else
        Color(0xFF202522)
}

private fun nmixMix(
    start:Float,
    end:Float,
    progress:Float
):Float{
    val t=
        progress.coerceIn(
            0f,
            1f
        )

    return start+
        (end-start)*t
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
        targetValue=
            if(open)
                1f
            else
                0f,
        animationSpec=tween(
            300,
            easing=EaseInOutCubic
        ),
        label="toolOpen"
    )

    val shape=
        RoundedCornerShape(
            (17f+5f*progress).dp
        )

    val surface=
        if(a.darkMode)
            Color(0xFF121715)
                .copy(alpha=.94f)
        else
            Color.White
                .copy(alpha=.92f)

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(shape)
            .background(surface)
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .04f
                        else
                            .022f
                )
            )
            .border(
                (
                    .45f+
                        .55f*progress
                ).dp,
                p.accent.copy(
                    alpha=
                        nmixMix(
                            if(a.darkMode)
                                .14f
                            else
                                .22f,
                            if(a.darkMode)
                                .46f
                            else
                                .52f,
                            progress
                        )
                ),
                shape
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
                val iconShape=
                    RoundedCornerShape(
                        (
                            9f+
                                11f*progress
                        ).dp
                    )

                Box(
                    Modifier
                        .size(
                            (
                                42f-
                                    3f*progress
                            ).dp
                        )
                        .rotate(
                            180f*progress
                        )
                        .clip(iconShape)
                        .background(
                            p.accent.copy(
                                alpha=.68f
                            )
                        )
                        .border(
                            .65.dp,
                            p.accentLight.copy(
                                alpha=.54f
                            ),
                            iconShape
                        )
                )

                Canvas(
                    Modifier
                        .size(
                            (
                                36f-
                                    2f*progress
                            ).dp
                        )
                        .rotate(
                            -180f*progress
                        )
                ){
                    val sw=
                        .62.dp.toPx()

                    drawRoundRect(
                        color=
                            Color.White.copy(
                                alpha=.48f
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
                                (
                                    7f+
                                        9f*progress
                                ).dp.toPx()
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
                        180f*progress
                    ),
                ui.muted
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=
                expandVertically(
                    tween(
                        280,
                        easing=EaseOutCubic
                    ),
                    expandFrom=
                        Alignment.Top
                )+
                fadeIn(tween(180)),
            exit=
                shrinkVertically(
                    tween(
                        250,
                        easing=EaseInOutCubic
                    ),
                    shrinkTowards=
                        Alignment.Top
                )+
                fadeOut(tween(150))
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

    val interaction=
        remember{
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
            dampingRatio=.72f,
            stiffness=620f
        ),
        label="optionPress"
    )

    val selectedProgress by
        animateFloatAsState(
            targetValue=
                if(selected)
                    1f
                else
                    0f,
            animationSpec=tween(220),
            label="optionSelected"
        )

    val shape=
        RoundedCornerShape(13.dp)

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
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

    val shape=
        RoundedCornerShape(9.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.91f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .09f
                        else
                            .025f
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
                            .50f
                        else if(a.darkMode)
                            .14f
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
            NmixIcon(
                icon,
                Modifier.size(19.dp),
                p.accent
            )
        }
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

    Box(
        modifier
            .clip(shape)
            .background(
                when{
                    accent->
                        p.accent.copy(
                            alpha=.80f
                        )

                    a.darkMode->
                        Color(0xFF141917)
                            .copy(alpha=.92f)

                    else->
                        Color.White
                            .copy(alpha=.92f)
                }
            )
            .background(
                if(accent)
                    Color.Transparent
                else
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .035f
                            else
                                .02f
                    )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(accent)
                            .42f
                        else if(a.darkMode)
                            .15f
                        else
                            .25f
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

    val bg=
        when(type){
            1->
                p.accent.copy(
                    alpha=.86f
                )

            2->
                Color(0xFFD83939)
                    .copy(
                        alpha=
                            if(a.darkMode)
                                .19f
                            else
                                .14f
                    )

            else->
                if(a.darkMode)
                    p.accent.copy(
                        alpha=.10f
                    )
                else
                    Color.White.copy(
                        alpha=.92f
                    )
        }

    val fg=
        when(type){
            1->Color.White
            2->Color(0xFFE15A5A)
            else->a.uiColors().text
        }

    Box(
        modifier
            .clip(CircleShape)
            .background(bg)
            .border(
                .45.dp,
                when(type){
                    1->
                        p.accentLight.copy(
                            alpha=.28f
                        )

                    2->
                        Color(0xFFE15A5A)
                            .copy(alpha=.20f)

                    else->
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .14f
                                else
                                    .21f
                        )
                },
                CircleShape
            )
    ){
        NmixPressBox(
            modifier=
                Modifier.fillMaxSize(),
            shape=CircleShape,
            color=Color.Transparent,
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
     * Full stable above 72%.
     * Full -> vertical happens quickly.
     * Half remains the long live range.
     * Half -> compact happens quickly near bottom.
     */
    val fullToHalf=
        (
            (.72f-displayPercent)/
                .13f
        ).coerceIn(
            0f,
            1f
        )

    val halfShrink=
        (
            (.59f-displayPercent)/
                .35f
        ).coerceIn(
            0f,
            1f
        )

    val halfToSmall=
        (
            (.24f-displayPercent)/
                .12f
        ).coerceIn(
            0f,
            1f
        )

    val smallShrink=
        (
            (.12f-displayPercent)/
                .12f
        ).coerceIn(
            0f,
            1f
        )

    val radiusProgress=
        (
            (.22f-displayPercent)/
                .22f
        ).coerceIn(
            0f,
            1f
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
                210,
                easing=EaseInOutCubic
            ),
            label="calculatorVisibility"
        )

    val world=
        rememberNmixWorldMotion(
            label="mainDisplayWorld"
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
        if(a.animationEnabled){
            DisplayWorldLayer(
                world=world
            )
        }

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

        val contentShift=
            when{
                !calcVisible->
                    0f

                halfToSmall>0f->
                    1f

                else->
                    fullToHalf
            }

        if(displayPercent>.59f){
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
                            44f*
                                contentShift

                        translationY=
                            4f*
                                contentShift
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
                        contentShift
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
                            57f*
                                contentShift

                        translationY=
                            if(calcVisible)
                                nmixMix(
                                    18f,
                                    6f,
                                    contentShift
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
                        contentShift
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
                                contentShift
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
                        contentShift
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
            val fieldFraction=
                if(calcVisible){
                    nmixMix(
                        .30f,
                        .48f,
                        halfToSmall
                    )
                }else{
                    0f
                }

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
                fadeIn(tween(200))+
                    scaleIn(),
            exit=
                fadeOut(tween(170))+
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
                            if(displayPercent<.18f)
                                8.dp
                            else
                                13.dp
                    ),
            enter=
                fadeIn(tween(200))+
                    scaleIn(),
            exit=
                fadeOut(tween(170))+
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
 * LARGE WORLD VIEWPORT
 * ==================================================
 */

@Composable
private fun BoxScope.DisplayWorldLayer(
    world:NmixWorldMotion
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val soft=
        a.animation!=
            NmixAnimationName.FLOAT

    /*
     * World is deliberately much larger than this
     * Display. The Display is only a cropped camera
     * looking into that world.
     */
    world.bodies.forEachIndexed{
        index,
        body->

        val scale=
            when(index){
                0->1f
                1->.88f
                2->.76f
                3->.82f
                else->.70f
            }

        /*
         * 480 x 330 movement canvas is much larger
         * than a typical visible Display section.
         * Bodies can therefore leave the camera for
         * long periods without bouncing at its edge.
         */
        val worldX=
            body.x*
                (
                    380f+
                        index*34f
                )

        val worldY=
            body.y*
                (
                    270f+
                        index*24f
                )

        if(soft){
            val baseSize=
                when(index){
                    0->560f
                    1->490f
                    2->430f
                    3->465f
                    else->385f
                }

            Box(
                Modifier
                    .size(
                        (
                            baseSize*
                                scale
                        ).dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            worldX

                        translationY=
                            worldY

                        scaleX=
                            body.pulse

                        scaleY=
                            body.pulse

                        rotationZ=
                            body.rotation*.10f
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
                                                    .36f
                                                else
                                                    .29f
                                        ),

                                    .22f to
                                        p.accent.copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .25f
                                                else
                                                    .20f
                                        ),

                                    .48f to
                                        p.accent.copy(
                                            alpha=.12f
                                        ),

                                    .68f to
                                        p.accent.copy(
                                            alpha=.055f
                                        ),

                                    .84f to
                                        p.accent.copy(
                                            alpha=.018f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        ),
                        CircleShape
                    )
            )
        }else{
            val baseSize=
                when(index){
                    0->280f
                    1->245f
                    2->215f
                    3->230f
                    else->195f
                }

            Canvas(
                Modifier
                    .size(
                        (
                            baseSize*
                                scale
                        ).dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            worldX

                        translationY=
                            worldY

                        scaleX=
                            body.pulse

                        scaleY=
                            body.pulse

                        rotationZ=
                            body.rotation
                    }
            ){
                val color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val fillAlpha=
                    if(a.darkMode)
                        .18f
                    else
                        .14f

                val inset=
                    9.dp.toPx()

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=.035f
                        ),
                    cornerRadius=
                        CornerRadius(
                            34.dp.toPx()
                        )
                )

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                fillAlpha
                        ),
                    topLeft=
                        Offset(
                            inset,
                            inset
                        ),
                    size=
                        Size(
                            (
                                size.width-
                                    inset*2
                            ).coerceAtLeast(
                                0f
                            ),
                            (
                                size.height-
                                    inset*2
                            ).coerceAtLeast(
                                0f
                            )
                        ),
                    cornerRadius=
                        CornerRadius(
                            27.dp.toPx()
                        )
                )

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                if(a.darkMode)
                                    .16f
                                else
                                    .12f
                        ),
                    topLeft=
                        Offset(
                            inset,
                            inset
                        ),
                    size=
                        Size(
                            (
                                size.width-
                                    inset*2
                            ).coerceAtLeast(
                                0f
                            ),
                            (
                                size.height-
                                    inset*2
                            ).coerceAtLeast(
                                0f
                            )
                        ),
                    cornerRadius=
                        CornerRadius(
                            27.dp.toPx()
                        ),
                    style=
                        Stroke(
                            1.5.dp.toPx()
                        )
                )
            }
        }
    }
}

/*
 * ==================================================
 * CALCULATOR MORPH
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
                fullToHalf=
                    fullToHalf,
                halfToSmall=
                    halfToSmall,
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
                fullToHalf=
                    fullToHalf,
                halfToSmall=
                    halfToSmall,
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
                fullToHalf=
                    fullToHalf,
                halfToSmall=
                    halfToSmall,
                radiusProgress=
                    radiusProgress
            )
        }
    ){measurables,constraints->
        val width=
            constraints.maxWidth

        val height=
            constraints.maxHeight

        fun px(
            value:Float
        ):Float{
            return with(density){
                value.dp.toPx()
            }
        }

        /*
         * FULL
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
         * HALF
         *
         * All measurements continue changing across
         * the long middle range.
         */
        val halfWidth=
            width*
                nmixMix(
                    .31f,
                    .265f,
                    halfShrink
                )

        val halfLeft=
            nmixMix(
                px(7f),
                px(4.5f),
                halfShrink
            )

        val outerGap=
            maxOf(
                nmixMix(
                    height*.065f,
                    height*.038f,
                    halfShrink
                ),
                px(2.5f)
            )

        val innerGap=
            nmixMix(
                px(5f),
                px(1.8f),
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

        val numberHeight=
            availableHeight/
                2.78f

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
         * SMALL
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

        val stageX=
            FloatArray(3)

        val stageY=
            FloatArray(3)

        val stageW=
            FloatArray(3)

        val stageH=
            FloatArray(3)

        repeat(3){index->
            stageX[index]=
                nmixMix(
                    fullX[index],
                    halfX[index],
                    fullToHalf
                )

            stageY[index]=
                nmixMix(
                    fullY[index],
                    halfY[index],
                    fullToHalf
                )

            stageW[index]=
                nmixMix(
                    fullW[index],
                    halfW[index],
                    fullToHalf
                )

            stageH[index]=
                nmixMix(
                    fullH[index],
                    halfH[index],
                    fullToHalf
                )
        }

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
                    stageX[index],
                    smallX[index],
                    halfToSmall
                )

            finalY[index]=
                nmixMix(
                    stageY[index],
                    smallYs[index],
                    halfToSmall
                )

            finalW[index]=
                nmixMix(
                    stageW[index],
                    smallW[index],
                    halfToSmall
                )

            finalH[index]=
                nmixMix(
                    stageH[index],
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

@Composable
private fun MorphFieldVisual(
    text:String,
    textSize:TextUnit,
    kind:Int,
    fullToHalf:Float,
    halfToSmall:Float,
    radiusProgress:Float
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val full=
        1f-
            fullToHalf

    val small=
        halfToSmall

    val normalRadius=11f

    val pillRadius=
        nmixMix(
            11f,
            36f,
            radiusProgress
        )

    val shape=
        when(kind){
            0->
                RoundedCornerShape(
                    topStart=
                        nmixMix(
                            normalRadius,
                            pillRadius,
                            small
                        ).dp,
                    topEnd=0.dp,
                    bottomEnd=0.dp,
                    bottomStart=
                        nmixMix(
                            0f,
                            pillRadius,
                            small
                        ).dp
                )

            1->
                RoundedCornerShape(0.dp)

            else->
                RoundedCornerShape(
                    topStart=0.dp,
                    topEnd=
                        (
                            normalRadius*
                                full
                        ).dp,
                    bottomEnd=0.dp,
                    bottomStart=
                        (
                            normalRadius*
                                fullToHalf*
                                (1f-small)
                        ).dp
                )
        }

    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151B18)
                        .copy(alpha=.94f)
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
                            .022f
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

@Composable
fun NmixCalcField(
    text:String,
    modifier:Modifier=Modifier,
    shape:Shape=
        RoundedCornerShape(11.dp),
    height:Dp?=46.dp,
    textSize:TextUnit=15.sp
){
    val a=
        LocalNmixAppearance.current

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
                    Color(0xFF151B18)
                        .copy(alpha=.94f)
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
                            .022f
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
                    Color(0xFF141917)
                        .copy(alpha=.92f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                if(accentTint)
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .04f
                            else
                                .022f
                    )
                else
                    Color.Transparent
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
    val haptic=
        rememberNmixHapticAction()

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
                interactionSource=
                    interaction,
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
