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
        tween(520,easing=EaseInOutCubic),
        label="outer"
    )
    val inner by animateFloatAsState(
        if(open)-180f else 0f,
        tween(520,easing=EaseInOutCubic),
        label="inner"
    )
    val radius by animateDpAsState(
        if(open)18.5.dp else 8.5.dp,
        tween(520,easing=EaseInOutCubic),
        label="radius"
    )
    val innerRadius by animateDpAsState(
        if(open)15.5.dp else 6.5.dp,
        tween(520,easing=EaseInOutCubic),
        label="innerRadius"
    )
    val arrow by animateFloatAsState(
        if(open)180f else 0f,
        tween(390,easing=EaseInOutCubic),
        label="arrow"
    )

    val card=if(a.darkMode)
        p.accent.copy(alpha=.075f)
    else
        Color.White.copy(alpha=.46f)

    val shape=RoundedCornerShape(16.dp)

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(shape)
            .background(card)
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).34f else .23f
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
                        .size(
                            if(open)39.dp else 42.dp
                        )
                        .rotate(outer)
                        .clip(RoundedCornerShape(radius))
                        .background(
                            p.accent.copy(alpha=.68f)
                        )
                        .border(
                            .75.dp,
                            p.accentLight.copy(alpha=.70f),
                            RoundedCornerShape(radius)
                        )
                )

                Canvas(
                    Modifier
                        .size(
                            if(open)34.dp else 36.dp
                        )
                        .rotate(inner)
                ){
                    val sw=.72.dp.toPx()

                    drawRoundRect(
                        color=Color.White.copy(alpha=.50f),
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
            enter=
                expandVertically(
                    animationSpec=tween(
                        340,
                        easing=EaseOutCubic
                    ),
                    expandFrom=Alignment.Top
                )+
                fadeIn(tween(190)),
            exit=
                shrinkVertically(
                    animationSpec=tween(
                        300,
                        easing=EaseInOutCubic
                    ),
                    shrinkTowards=Alignment.Top
                )+
                fadeOut(tween(170))
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
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).965f else 1f,
        spring(dampingRatio=.72f,stiffness=620f),
        label="optionPress"
    )

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if(selected)
                    p.accent.copy(alpha=.84f)
                else
                    p.accent.copy(
                        alpha=if(a.darkMode).10f else .08f
                    )
            )
            .border(
                .45.dp,
                p.accent.copy(alpha=.24f),
                RoundedCornerShape(13.dp)
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
                    if(selected)CircleShape
                    else RoundedCornerShape(9.dp)
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

    val bg=if(accent)
        p.accent.copy(alpha=.78f)
    else
        p.accent.copy(
            alpha=if(a.darkMode).10f else .08f
        )

    val shape=RoundedCornerShape(50)

    Box(
        modifier
            .clip(shape)
            .background(bg)
            .border(
                .5.dp,
                p.accent.copy(alpha=.32f),
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
                color=if(accent)Color.White
                else a.uiColors().text,
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
    val ui=a.uiColors()

    val bg=when(type){
        1->p.accent.copy(alpha=.86f)
        2->Color(0xFFD83939).copy(alpha=.17f)
        else->p.accent.copy(
            alpha=if(a.darkMode).11f else .075f
        )
    }

    val fg=when(type){
        1->Color.White
        2->Color(0xFFE15A5A)
        else->ui.text
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
    val ui=a.uiColors()
    val interaction=remember{MutableInteractionSource()}
    val motion=rememberInfiniteTransition(
        label="displayMotion"
    )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            animation=tween(
                2700,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="x"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            animation=tween(
                3400,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="y"
    )

    val pulse by motion.animateFloat(
        .84f,
        1.17f,
        infiniteRepeatable(
            animation=tween(
                3000,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="pulse"
    )

    val bg=if(a.darkMode){
        Brush.linearGradient(
            listOf(
                Color(0xFF171D1B),
                p.accent.copy(alpha=.13f),
                Color(0xFF222A27),
                Color(0xFF151A18)
            )
        )
    }else{
        Brush.linearGradient(
            listOf(
                Color(0xFFF8FAF9),
                p.accentLight.copy(alpha=.16f),
                Color(0xFFE8EDEB)
            )
        )
    }

    val shape=RoundedCornerShape(19.dp)

    Box(
        modifier
            .clip(shape)
            .background(bg)
            .border(
                .6.dp,
                p.accent.copy(alpha=.32f),
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
                .size(330.dp)
                .align(Alignment.TopStart)
                .offset(
                    x=(-120).dp,
                    y=(-135).dp
                )
                .graphicsLayer{
                    translationX=x*185f
                    translationY=y*85f
                    scaleX=pulse
                    scaleY=pulse
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(
                                alpha=if(a.darkMode).34f else .24f
                            ),
                            p.accent.copy(alpha=.09f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(290.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x=115.dp,
                    y=120.dp
                )
                .graphicsLayer{
                    translationX=-x*150f
                    translationY=-y*75f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(
                                alpha=if(a.darkMode).25f else .22f
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
            color=if(a.darkMode)
                p.accentLight
            else
                p.accentDark.copy(alpha=.88f),
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
                    horizontal=if(timer)70.dp else 16.dp
                ),
            color=if(a.darkMode)
                Color.White.copy(alpha=.90f)
            else
                Color(0xFF343A37),
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
                Color.White.copy(alpha=.62f)
            else
                Color(0xFF59635F),
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
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(49.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color.Black.copy(alpha=.32f)
                else
                    p.accent.copy(alpha=.075f)
            )
            .border(
                .45.dp,
                p.accent.copy(alpha=.28f),
                shape
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=if(a.darkMode)
                Color.White.copy(alpha=.90f)
            else
                ui.text.copy(alpha=.82f),
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
    val p=a.palette
    val shape=RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=if(a.darkMode).09f
                    else if(accentTint).075f
                    else .045f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(alpha=.23f),
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
