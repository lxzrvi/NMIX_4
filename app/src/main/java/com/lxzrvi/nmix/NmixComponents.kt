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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val shape=RoundedCornerShape(16.dp)

    val glass=Brush.horizontalGradient(
    listOf(
        p.accent.copy(
            alpha=if(a.darkMode).10f else .11f
        ),
        p.accent.copy(
            alpha=if(a.darkMode).085f else .09f
        ),
        p.accent.copy(
            alpha=if(a.darkMode).10f else .11f
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
                    alpha=if(a.darkMode).14f else .27f
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
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(
                Modifier.size(42.dp),
                contentAlignment=Alignment.Center
            ){
                Box(
                    Modifier
                        .size(outerSize)
                        .rotate(outerRotation)
                        .clip(
                            RoundedCornerShape(
                                outerRadius
                            )
                        )
                        .background(
                            p.accent.copy(alpha=.66f)
                        )
                        .border(
                            .65.dp,
                            p.accentLight.copy(alpha=.60f),
                            RoundedCornerShape(
                                outerRadius
                            )
                        )
                )

                Canvas(
                    Modifier
                        .size(innerSize)
                        .rotate(innerRotation)
                ){
                    val sw=.62.dp.toPx()

                    drawRoundRect(
                        color=Color.White.copy(alpha=.46f),
                        topLeft=Offset(sw,sw),
                        size=Size(
                            size.width-sw*2,
                            size.height-sw*2
                        ),
                        cornerRadius=CornerRadius(
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
                    fontWeight=FontWeight.SemiBold,
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

    val pressed by interaction.collectIsPressedAsState()

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
                        alpha=if(a.darkMode).085f else .08f
                    )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).13f else .23f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
            .padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically
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
            color=if(selected)Color.White else ui.text,
            fontSize=13.sp,
            fontWeight=FontWeight.SemiBold,
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
        color?:a.palette.accent.copy(alpha=.80f),
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
            if(selected)Color.White else p.accent
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
                alpha=if(a.darkMode).09f else .08f
            )

    Box(
        modifier
            .clip(shape)
            .background(bg)
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).14f else .30f
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
                color=if(accent)
                    Color.White
                else
                    a.uiColors().text,
                fontSize=12.sp,
                fontWeight=FontWeight.SemiBold,
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

        2->Color(0xFFD83939)
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
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
fun NmixDisplay(
    label:String,
    value:String,
    status:String,
    timer:Boolean,
    onMinus:()->Unit,
    onPlus:()->Unit,
    onClick:()->Unit,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val interaction=remember{
        MutableInteractionSource()
    }

    val motion=rememberInfiniteTransition(
        label="displayMotion"
    )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                2600,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="displayX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                3300,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="displayY"
    )

    val pulse by motion.animateFloat(
        .88f,
        1.15f,
        infiniteRepeatable(
            tween(
                2900,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="displayPulse"
    )

    val shape=RoundedCornerShape(19.dp)

    Box(
        modifier
            .clip(shape)
            .background(nmixScreenColor())
            .border(
                .55.dp,
                nmixScreenBorder(),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
    ){
        Box(
            Modifier
                .size(340.dp)
                .align(Alignment.TopStart)
                .offset(
                    x=(-145).dp,
                    y=(-155).dp
                )
                .graphicsLayer{
                    translationX=x*190f
                    translationY=y*85f
                    scaleX=pulse
                    scaleY=pulse
                }
                .background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to p.accent.copy(
                                alpha=if(a.darkMode).22f else .18f
                            ),
                            .28f to p.accent.copy(
                                alpha=if(a.darkMode).15f else .12f
                            ),
                            .60f to p.accent.copy(
                                alpha=.055f
                            ),
                            1f to Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(315.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x=135.dp,
                    y=135.dp
                )
                .graphicsLayer{
                    translationX=-x*160f
                    translationY=-y*75f
                }
                .background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to p.accentLight.copy(
                                alpha=if(a.darkMode).16f else .15f
                            ),
                            .35f to p.accentLight.copy(
                                alpha=.09f
                            ),
                            .68f to p.accentLight.copy(
                                alpha=.035f
                            ),
                            1f to Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Text(
            label,
            Modifier
                .align(Alignment.TopCenter)
                .padding(top=17.dp),
            color=if(a.darkMode)
                p.accentLight
            else
                p.accentDark,
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=2.sp,
            fontFamily=a.fontFamily
        )

        Text(
            value,
            Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal=if(timer)
                        70.dp
                    else
                        16.dp
                ),
            color=Color.White,
            fontSize=40.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Text(
            status,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal=18.dp,
                    vertical=15.dp
                ),
            color=if(a.darkMode)
                p.accentLight.copy(alpha=.78f)
            else
                p.accentDark.copy(alpha=.80f),
            fontSize=11.sp,
            lineHeight=15.sp,
            fontWeight=FontWeight.Medium,
            fontFamily=a.fontFamily,
            textAlign=TextAlign.Center,
            maxLines=2
        )

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterStart)
                .padding(start=13.dp),
            enter=fadeIn(tween(230))+scaleIn(),
            exit=fadeOut(tween(190))+scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.MINUS,
                Modifier.size(47.dp),
                onClick=onMinus
            )
        }

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterEnd)
                .padding(end=13.dp),
            enter=fadeIn(tween(230))+scaleIn(),
            exit=fadeOut(tween(190))+scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.PLUS,
                Modifier.size(47.dp),
                onClick=onPlus
            )
        }
    }
}

@Composable
fun NmixCalcField(
    text:String,
    modifier:Modifier=Modifier
){
    val shape=RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(49.dp)
            .clip(shape)
            .background(nmixScreenColor())
            .border(
                .5.dp,
                nmixScreenBorder(),
                shape
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=Color.White,
            fontSize=16.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=
                LocalNmixAppearance.current.fontFamily,
            maxLines=1
        )
    }
}

@Composable
fun NmixGlassBox(
    modifier:Modifier=Modifier,
    accentTint:Boolean=true,
    content:@Composable BoxScope.()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    p.accent.copy(alpha=.065f)
                else if(accentTint)
                    Color.White.copy(alpha=.73f)
                else
                    Color.White.copy(alpha=.70f)
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).12f else .22f
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

    val pressed by interaction.collectIsPressedAsState()

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
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        content()
    }
}
