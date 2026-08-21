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

    val outer by animateFloatAsState(
        if(open)180f else 0f,
        tween(540,easing=EaseInOutCubic),
        label="outer"
    )

    val inner by animateFloatAsState(
        if(open)-180f else 0f,
        tween(540,easing=EaseInOutCubic),
        label="inner"
    )

    val radius by animateDpAsState(
        if(open)21.dp else 9.dp,
        tween(540,easing=EaseInOutCubic),
        label="radius"
    )

    val innerRadius by animateDpAsState(
        if(open)17.dp else 7.dp,
        tween(540,easing=EaseInOutCubic),
        label="innerRadius"
    )

    val arrow by animateFloatAsState(
        if(open)180f else 0f,
        tween(370,easing=EaseInOutCubic),
        label="arrow"
    )

    val cardColor=if(a.darkMode)
        Color.Black.copy(alpha=.42f)
    else
        Color.White.copy(alpha=.46f)

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
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
                Modifier.size(44.dp),
                contentAlignment=Alignment.Center
            ){
                Box(
                    Modifier
                        .size(42.dp)
                        .rotate(outer)
                        .clip(RoundedCornerShape(radius))
                        .background(
                            p.accent.copy(alpha=.72f)
                        )
                        .border(
                            width=1.4.dp,
                            color=p.accentLight.copy(alpha=.90f),
                            shape=RoundedCornerShape(radius)
                        )
                )

                Canvas(
                    Modifier
                        .size(36.dp)
                        .rotate(inner)
                ){
                    val sw=1.25.dp.toPx()

                    drawRoundRect(
                        color=Color.White.copy(alpha=.52f),
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

            Column(Modifier.weight(1f)){
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
                    .rotate(arrow),
                ui.muted
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=expandVertically(
                animationSpec=tween(
                    320,
                    easing=EaseOutCubic
                ),
                expandFrom=Alignment.Top
            )+fadeIn(tween(180)),
            exit=shrinkVertically(
                animationSpec=tween(
                    270,
                    easing=EaseInCubic
                ),
                shrinkTowards=Alignment.Top
            )+fadeOut(tween(140))
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
            dampingRatio=.7f,
            stiffness=700f
        ),
        label="optionPress"
    )

    val background=when{
        selected->p.accent.copy(alpha=.86f)
        a.darkMode->Color.Black.copy(alpha=.38f)
        else->p.accent.copy(alpha=.10f)
    }

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(background)
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
                        p.accent.copy(alpha=.17f)
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
        modifier=modifier,
        shape=CircleShape,
        color=color?:a.palette.accent.copy(alpha=.82f),
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
        shape=RoundedCornerShape(9.dp),
        color=if(selected)
            Color.White.copy(alpha=.18f)
        else
            p.accent.copy(alpha=.17f),
        onClick=onClick
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
    val ui=a.uiColors()

    val color=when{
        accent->p.accent.copy(alpha=.82f)
        a.darkMode->Color.Black.copy(alpha=.38f)
        else->ui.glass.copy(alpha=.55f)
    }

    NmixPressBox(
        modifier=modifier,
        shape=RoundedCornerShape(50),
        color=color,
        onClick=onClick
    ){
        Text(
            text,
            color=if(accent)Color.White else ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
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
    val ui=a.uiColors()

    val bg=when(type){
        1->p.accent.copy(alpha=.88f)
        2->Color(0xFFD83939).copy(alpha=.18f)
        else->if(a.darkMode)
            Color.Black.copy(alpha=.40f)
        else
            Color.White.copy(alpha=.46f)
    }

    val fg=when(type){
        1->Color.White
        2->Color(0xFFE75454)
        else->ui.text
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
    val ui=a.uiColors()

    val interaction=remember{
        MutableInteractionSource()
    }

    val motion=rememberInfiniteTransition(
        label="displayMotion"
    )

    val x by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                3900,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="displayX"
    )

    val y by motion.animateFloat(
        initialValue=1f,
        targetValue=-1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                5200,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="displayY"
    )

    val pulse by motion.animateFloat(
        initialValue=.86f,
        targetValue=1.18f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                4300,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="displayPulse"
    )

    val displayBg=if(a.darkMode){
        Brush.linearGradient(
            listOf(
                Color(0xFF070A09),
                Color(0xFF101715),
                Color(0xFF050706)
            )
        )
    }else{
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha=.92f),
                p.accentLight.copy(alpha=.17f),
                Color.White.copy(alpha=.72f)
            )
        )
    }

    Box(
        modifier
            .clip(RoundedCornerShape(19.dp))
            .background(displayBg)
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
    ){
        Box(
            Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(
                    x=(-100).dp,
                    y=(-120).dp
                )
                .graphicsLayer{
                    translationX=x*125f
                    translationY=y*60f
                    scaleX=pulse
                    scaleY=pulse
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(
                                alpha=if(a.darkMode).32f else .24f
                            ),
                            p.accent.copy(alpha=.08f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x=100.dp,
                    y=105.dp
                )
                .graphicsLayer{
                    translationX=-x*100f
                    translationY=-y*55f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(
                                alpha=if(a.darkMode).18f else .22f
                            ),
                            Color.Transparent
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
            color=p.accentLight,
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
                    horizontal=if(timer)70.dp
                    else 16.dp
                ),
            color=ui.text,
            fontSize=40.sp,
            fontWeight=FontWeight.Bold,
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
                Color.White.copy(alpha=.66f)
            else
                p.accentDark.copy(alpha=.88f),
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
            enter=fadeIn()+scaleIn(),
            exit=fadeOut()+scaleOut()
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
            enter=fadeIn()+scaleIn(),
            exit=fadeOut()+scaleOut()
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
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()

    Box(
        modifier
            .height(49.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                if(a.darkMode)
                    Color.Black.copy(alpha=.45f)
                else
                    Color.White.copy(alpha=.47f)
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=ui.text,
            fontSize=16.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
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
    val ui=a.uiColors()

    val color=if(a.darkMode)
        Color.Black.copy(alpha=.38f)
    else if(accentTint)
        ui.accentGlass
    else
        Color.White.copy(alpha=.45f)

    Box(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(color),
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
        if(pressed).94f else 1f,
        spring(
            dampingRatio=.65f,
            stiffness=720f
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
