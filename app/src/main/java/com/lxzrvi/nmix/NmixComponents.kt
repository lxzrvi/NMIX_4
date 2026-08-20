package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
        tween(560,easing=EaseInOutCubic),
        label="outer"
    )
    val inner by animateFloatAsState(
        if(open)-180f else 0f,
        tween(560,easing=EaseInOutCubic),
        label="inner"
    )
    val radius by animateDpAsState(
        if(open)21.dp else 9.dp,
        tween(560,easing=EaseInOutCubic),
        label="radius"
    )
    val innerRadius by animateDpAsState(
        if(open)15.dp else 6.dp,
        tween(560,easing=EaseInOutCubic),
        label="innerRadius"
    )
    val arrow by animateFloatAsState(
        if(open)180f else 0f,
        tween(360),
        label="arrow"
    )

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(ui.glass)
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick=onClick)
                .padding(13.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(
                Modifier.size(42.dp),
                contentAlignment=Alignment.Center
            ){
                Box(
                    Modifier
                        .fillMaxSize()
                        .rotate(outer)
                        .clip(RoundedCornerShape(radius))
                        .background(p.accent)
                )

                Canvas(Modifier.size(31.dp).rotate(inner)){
                    val sw=1.3.dp.toPx()
                    drawRoundRect(
                        color=Color.White.copy(alpha=.40f),
                        topLeft=Offset(sw,sw),
                        size=Size(size.width-sw*2,size.height-sw*2),
                        cornerRadius=CornerRadius(innerRadius.toPx()),
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
                androidx.compose.material3.Text(
                    title,
                    color=ui.text,
                    fontSize=14.sp,
                    fontWeight=FontWeight.SemiBold
                )
                androidx.compose.material3.Text(
                    subtitle,
                    color=ui.muted,
                    fontSize=9.sp
                )
            }

            NmixIcon(
                NmixIcon.CHEVRON_DOWN,
                Modifier.size(18.dp).rotate(arrow),
                ui.muted
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=expandVertically(
                animationSpec=tween(320,easing=EaseOutCubic),
                expandFrom=Alignment.Top
            )+fadeIn(tween(220)),
            exit=shrinkVertically(
                animationSpec=tween(270,easing=EaseInCubic),
                shrinkTowards=Alignment.Top
            )+fadeOut(tween(160))
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
        spring(dampingRatio=.7f,stiffness=700f),
        label="optionPress"
    )

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if(selected)p.accent.copy(alpha=.88f)
                else ui.accentGlassStrong
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
                .clip(if(selected)CircleShape else RoundedCornerShape(9.dp))
                .background(
                    if(selected)Color.White.copy(alpha=.92f)
                    else p.accent.copy(alpha=.17f)
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

        androidx.compose.material3.Text(
            title,
            color=if(selected)Color.White else ui.text,
            fontSize=13.sp,
            fontWeight=FontWeight.SemiBold
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
        color=color?:a.palette.accent,
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

    NmixPressBox(
        modifier=modifier,
        shape=RoundedCornerShape(50),
        color=if(accent)
            p.accent.copy(alpha=.88f)
        else
            ui.accentGlassStrong,
        onClick=onClick
    ){
        androidx.compose.material3.Text(
            text,
            color=if(accent)Color.White else ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold
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
        1->p.accent
        2->Color(0xFFD83939).copy(alpha=.17f)
        else->ui.accentGlass
    }

    val fg=when(type){
        1->Color.White
        2->Color(0xFFD83939)
        else->ui.text
    }

    NmixPressBox(
        modifier=modifier,
        shape=CircleShape,
        color=bg,
        onClick=onClick
    ){
        androidx.compose.material3.Text(
            text,
            color=fg,
            fontSize=15.sp,
            fontWeight=FontWeight.SemiBold
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
    val inf=rememberInfiniteTransition(label="display")

    val move by inf.animateFloat(
        initialValue=-100f,
        targetValue=150f,
        animationSpec=infiniteRepeatable(
            animation=tween(4700,easing=EaseInOutSine),
            repeatMode=RepeatMode.Reverse
        ),
        label="move"
    )

    Box(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    listOf(ui.displayStart,ui.displayEnd)
                )
            )
            .clickable(onClick=onClick)
    ){
        Box(
            Modifier
                .size(230.dp)
                .offset(
                    x=(move/5).dp,
                    y=(move/14).dp
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(alpha=.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        androidx.compose.material3.Text(
            label,
            Modifier
                .align(Alignment.TopCenter)
                .padding(top=15.dp),
            color=p.accent,
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=2.sp
        )

        androidx.compose.material3.Text(
            value,
            Modifier
                .align(Alignment.Center)
                .padding(horizontal=if(timer)68.dp else 12.dp),
            color=ui.text,
            fontSize=40.sp,
            fontWeight=FontWeight.Bold,
            maxLines=1
        )

        androidx.compose.material3.Text(
            status,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal=15.dp,vertical=13.dp),
            color=p.accent.copy(alpha=.86f),
            fontSize=11.sp,
            fontWeight=FontWeight.Medium,
            textAlign=TextAlign.Center,
            maxLines=2
        )

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterStart)
                .padding(start=12.dp),
            enter=fadeIn()+scaleIn(),
            exit=fadeOut()+scaleOut()
        ){
            NmixCircleButton(
                icon=NmixIcon.MINUS,
                modifier=Modifier.size(47.dp),
                onClick=onMinus
            )
        }

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterEnd)
                .padding(end=12.dp),
            enter=fadeIn()+scaleIn(),
            exit=fadeOut()+scaleOut()
        ){
            NmixCircleButton(
                icon=NmixIcon.PLUS,
                modifier=Modifier.size(47.dp),
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
    val ui=LocalNmixAppearance.current.uiColors()

    Box(
        modifier
            .height(49.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ui.glassStrong),
        contentAlignment=Alignment.Center
    ){
        androidx.compose.material3.Text(
            text,
            color=ui.text,
            fontSize=16.sp,
            fontWeight=FontWeight.SemiBold,
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
    val ui=LocalNmixAppearance.current.uiColors()

    Box(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(
                if(accentTint)ui.accentGlass
                else ui.glass
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
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).93f else 1f,
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
