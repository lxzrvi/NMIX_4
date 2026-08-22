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
        alpha=if(a.darkMode).14f else .27f
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

private fun clamp01(value:Float)=
    value.coerceIn(0f,1f)

private fun rangeProgress(
    value:Float,
    start:Float,
    end:Float
):Float{
    if(start==end)
        return if(value>=end)1f else 0f

    return clamp01(
        (value-start)/(end-start)
    )
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

    val outerRotation=180f*progress
    val innerRotation=-180f*progress
    val arrowRotation=180f*progress

    val outerSize=(42f-3f*progress).dp
    val innerSize=(36f-2f*progress).dp

    val outerRadius=(8.5f+10f*progress).dp
    val innerRadius=(6.5f+9f*progress).dp

    /*
     * Whole label follows the same morph family
     * as the rotating icon.
     */
    val cardRadius=(16f+7f*progress).dp
    val shape=RoundedCornerShape(cardRadius)

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
                contentAlignment=Alignment.Center
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
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)

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
            heightPx.toDp()
        }

    val h=
        if(heightPx==0)
            250f
        else
            heightDp.value

    /*
     * Continuous resize stages.
     *
     * Stage A:
     * large Display.
     *
     * Stage B:
     * fields begin moving towards left.
     *
     * Stage C:
     * fields become vertical.
     *
     * Stage D:
     * Display radius starts increasing only
     * after the field layout is settled.
     */
    val compactProgress=
        rangeProgress(
            225f-h,
            0f,
            82f
        )

    val verticalProgress=
        rangeProgress(
            198f-h,
            0f,
            56f
        )

    val extremeProgress=
        rangeProgress(
            132f-h,
            0f,
            35f
        )

    val calcProgress by
        animateFloatAsState(
            if(calcVisible)1f else 0f,
            tween(
                330,
                easing=EaseInOutCubic
            ),
            label="calcVisibility"
        )

    val radius=
        (
            19f+
                extremeProgress*
                35f
            ).dp

    val displayShape=
        RoundedCornerShape(radius)

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
            /*
             * Tall/top layout fades away as
             * resizing enters the left-layout
             * stage.
             */
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
                        RoundedCornerShape(0.dp)
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

            /*
             * LEFT RESIZE LAYOUT
             *
             * Starts narrow and grows to about
             * 30% as the Display shrinks.
             */
            val leftWidth=
                .12f+
                    compactProgress*
                    .20f

            val leftCorner=
                (
                    radius.value*
                    extremeProgress
                ).dp

            val fieldHeight=
                (
                    36f-
                        extremeProgress*
                        5f
                ).dp

            Box(
                Modifier
                    .fillMaxWidth(
                        leftWidth.coerceIn(
                            .10f,
                            .32f
                        )
                    )
                    .fillMaxHeight()
                    .align(
                        Alignment.CenterStart
                    )
                    .graphicsLayer{
                        alpha=
                            calcProgress*
                            compactProgress
                    },
                contentAlignment=
                    Alignment.CenterStart
            ){
                /*
                 * Horizontal stage.
                 */
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start=7.dp,
                            end=3.dp
                        )
                        .graphicsLayer{
                            alpha=
                                1f-
                                verticalProgress
                        },
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            2.dp
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
                                topStart=
                                    leftCorner
                            ),
                        height=fieldHeight,
                        textSize=9.sp
                    )

                    NmixCalcField(
                        text=
                            calcOperator.ifEmpty{
                                "·"
                            },
                        modifier=
                            Modifier.width(22.dp),
                        shape=
                            RoundedCornerShape(0.dp),
                        height=fieldHeight,
                        textSize=8.sp
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
                                bottomStart=
                                    leftCorner
                            ),
                        height=fieldHeight,
                        textSize=9.sp
                    )
                }

                /*
                 * Vertical final stage.
                 *
                 * Num1 owns the top-left outer
                 * radius.
                 * Operator stays square.
                 * Num2 owns bottom-left radius.
                 */
                Column(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(
                            start=7.dp,
                            top=7.dp,
                            bottom=7.dp,
                            end=3.dp
                        )
                        .graphicsLayer{
                            alpha=
                                verticalProgress
                        },
                    verticalArrangement=
                        Arrangement.spacedBy(
                            2.dp
                        )
                ){
                    NmixCalcField(
                        text=
                            calcFirst.ifEmpty{
                                "_"
                            },
                        modifier=
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        shape=
                            RoundedCornerShape(
                                topStart=
                                    leftCorner
                            ),
                        height=null,
                        textSize=9.sp
                    )

                    NmixCalcField(
                        text=
                            calcOperator.ifEmpty{
                                "·"
                            },
                        modifier=
                            Modifier
                                .weight(.72f)
                                .fillMaxWidth(),
                        shape=
                            RoundedCornerShape(
                                0.dp
                            ),
                        height=null,
                        textSize=8.sp
                    )

                    NmixCalcField(
                        text=
                            calcSecond.ifEmpty{
                                "_"
                            },
                        modifier=
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        shape=
                            RoundedCornerShape(
                                bottomStart=
                                    leftCorner
                            ),
                        height=null,
                        textSize=9.sp
                    )
                }
            }
        }

        /*
         * Main content smoothly moves towards
         * the right while calculator fields take
         * over the left side.
         */
        val contentShift=
            if(calcVisible)
                compactProgress*
                    verticalProgress
            else
                0f

        if(
            compactProgress<
            .16f
        ){
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
                                18f*
                                calcProgress
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
                color=nmixDisplayText(),
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
            Column(
                Modifier
                    .fillMaxWidth(
                        if(calcVisible)
                            (
                                .82f-
                                    contentShift*
                                    .12f
                            )
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
                        start=
                            if(calcVisible)
                                10.dp
                            else
                                6.dp,
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
                if(extremeProgress<.50f){
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
                    color=nmixDisplayText(),
                    fontSize=
                        (
                            34f-
                                compactProgress*
                                6f-
                                extremeProgress*
                                4f
                            ).sp,
                    fontWeight=
                        FontWeight.SemiBold,
                    fontFamily=a.fontFamily,
                    maxLines=1,
                    textAlign=
                        TextAlign.Center
                )

                if(extremeProgress<.50f){
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
                            if(extremeProgress>.5f)
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
                    (
                        47f-
                            compactProgress*
                            7f-
                            extremeProgress*
                            8f
                        ).dp
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
                            if(extremeProgress>.5f)
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
                    (
                        47f-
                            compactProgress*
                            7f-
                            extremeProgress*
                            8f
                        ).dp
                ),
                onClick=onPlus
            )
        }
    }
}

private data class DisplayPlacement(
    val x:Float,
    val y:Float,
    val dx:Float,
    val dy:Float,
    val scale:Float
)

private val displayPlacements=
    listOf(
        DisplayPlacement(
            -.28f,-.23f,
            1f,.65f,1f
        ),
        DisplayPlacement(
            .28f,.24f,
            -.82f,-.72f,.80f
        ),
        DisplayPlacement(
            .27f,-.25f,
            .70f,-.82f,.68f
        ),
        DisplayPlacement(
            -.27f,.25f,
            -.65f,.78f,.72f
        ),
        DisplayPlacement(
            .02f,.02f,
            .52f,-.50f,.61f
        )
    )

@Composable
private fun BoxScope.DisplayMotionLayer(
    motion:NmixMotionValues
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val soft=
        a.animation in
            listOf(
                NmixAnimationName.DRIFT,
                NmixAnimationName.ORBIT,
                NmixAnimationName.FLOW
            )

    /*
     * Quantity now controls the real Display,
     * not only Settings preview.
     *
     * Every item has its own home position and
     * direction so increasing quantity spreads
     * geometry across the Display.
     */
    repeat(
        a.animationQuantity
            .coerceIn(1,5)
    ){index->
        val placement=
            displayPlacements[
                index%
                    displayPlacements.size
            ]

        val direction=
            if(index%2==0)
                1f
            else
                -1f

        if(soft){
            val shape=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        CircleShape

                    NmixAnimationName.ORBIT->
                        RoundedCornerShape(
                            48.dp
                        )

                    NmixAnimationName.FLOW->
                        RoundedCornerShape(
                            32.dp
                        )

                    else->
                        CircleShape
                }

            Box(
                Modifier
                    .size(
                        (
                            205f*
                                placement.scale
                            ).dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=
                            (
                                placement.x*
                                    128f
                                ).dp,
                        y=
                            (
                                placement.y*
                                    100f
                                ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.x*
                                82f*
                                placement.dx

                        translationY=
                            motion.y*
                                52f*
                                placement.dy

                        scaleX=
                            motion.pulse

                        scaleY=
                            motion.pulse

                        rotationZ=
                            if(
                                a.animation==
                                NmixAnimationName.ORBIT
                            )
                                motion.z*
                                    13f*
                                    direction
                            else
                                0f
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
                                                    .27f
                                                else
                                                    .22f
                                        ),

                                    .44f to
                                        p.accent.copy(
                                            alpha=.14f
                                        ),

                                    .76f to
                                        p.accent.copy(
                                            alpha=.045f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        ),
                        shape
                    )
            )
        }else{
            Canvas(
                Modifier
                    .size(
                        (
                            82f*
                                placement.scale
                            ).dp
                    )
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=
                            (
                                placement.x*
                                    150f
                                ).dp,
                        y=
                            (
                                placement.y*
                                    110f
                                ).dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.x*
                                72f*
                                placement.dx

                        translationY=
                            motion.y*
                                42f*
                                placement.dy

                        scaleX=
                            if(
                                a.animation==
                                NmixAnimationName.PULSE
                            )
                                motion.pulse
                            else
                                1f

                        scaleY=
                            if(
                                a.animation==
                                NmixAnimationName.PULSE
                            )
                                motion.pulse
                            else
                                1f

                        rotationZ=
                            motion.z*
                                14f*
                                direction
                    }
            ){
                val main=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val alpha=
                    if(a.darkMode)
                        .17f
                    else
                        .14f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        /*
                         * Soft outer edge +
                         * maintained square body.
                         */
                        drawRoundRect(
                            color=
                                main.copy(
                                    alpha=.055f
                                ),
                            cornerRadius=
                                CornerRadius(
                                    16.dp.toPx()
                                )
                        )

                        val inset=
                            3.dp.toPx()

                        drawRoundRect(
                            color=
                                main.copy(
                                    alpha=alpha
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
                                    13.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val path=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.07f
                                )

                                lineTo(
                                    size.width*.93f,
                                    size.height*.89f
                                )

                                lineTo(
                                    size.width*.07f,
                                    size.height*.89f
                                )

                                close()
                            }

                        drawPath(
                            path,
                            main.copy(
                                alpha=alpha
                            )
                        )

                        drawPath(
                            path,
                            main.copy(
                                alpha=.07f
                            ),
                            style=Stroke(
                                2.5.dp.toPx()
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val path=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.04f
                                )

                                lineTo(
                                    size.width*.96f,
                                    size.height*.5f
                                )

                                lineTo(
                                    size.width*.5f,
                                    size.height*.96f
                                )

                                lineTo(
                                    size.width*.04f,
                                    size.height*.5f
                                )

                                close()
                            }

                        drawPath(
                            path,
                            main.copy(
                                alpha=alpha
                            )
                        )

                        drawPath(
                            path,
                            main.copy(
                                alpha=.065f
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
                .5.dp,
                nmixScreenBorder(),
                shape
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=nmixDisplayText(),
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
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode){
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                }else{
                    Color(0xFFE8ECEA)
                        .copy(alpha=.84f)
                }
            )
            .background(
                if(accentTint)
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .055f
                            else
                                .045f
                    )
                else
                    Color.Transparent
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
        if(pressed).95f else 1f,
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
