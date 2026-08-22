package com.lxzrvi.nmix

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NmixAnimationSettings(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()

    Column(
        Modifier.fillMaxWidth()
    ){
        Text(
            "Animation",
            color=ui.text,
            fontSize=12.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )

        Text(
            "Choose how NMIX backgrounds move",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(11.dp)
        )

        MotionGroupTitle(
            "SOFT",
            "Blurred • calm • flowing"
        )

        Spacer(
            Modifier.height(7.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            SoftMotionCard(
                animation=NmixAnimationName.DRIFT,
                detail="Slow floating glow",
                modifier=Modifier.weight(1f)
            )

            SoftMotionCard(
                animation=NmixAnimationName.ORBIT,
                detail="Circular calm motion",
                modifier=Modifier.weight(1f)
            )

            SoftMotionCard(
                animation=NmixAnimationName.FLOW,
                detail="Smooth directional flow",
                modifier=Modifier.weight(1f)
            )
        }

        Spacer(
            Modifier.height(13.dp)
        )

        MotionGroupTitle(
            "HARD",
            "Defined • geometric • visible"
        )

        Spacer(
            Modifier.height(7.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            HardMotionCard(
                animation=NmixAnimationName.FLOAT,
                detail="Floating square",
                shape=HardShape.SQUARE,
                modifier=Modifier.weight(1f)
            )

            HardMotionCard(
                animation=NmixAnimationName.PULSE,
                detail="Breathing triangle",
                shape=HardShape.TRIANGLE,
                modifier=Modifier.weight(1f)
            )

            HardMotionCard(
                animation=NmixAnimationName.CROSS,
                detail="Crossing geometry",
                shape=HardShape.DIAMOND,
                modifier=Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MotionGroupTitle(
    title:String,
    detail:String
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment=
            Alignment.CenterVertically
    ){
        Text(
            title,
            color=p.accent,
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=1.2.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.width(7.dp)
        )

        Text(
            detail,
            color=ui.muted,
            fontSize=7.sp,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun SoftMotionCard(
    animation:NmixAnimationName,
    detail:String,
    modifier:Modifier
){
    MotionCardShell(
        animation=animation,
        detail=detail,
        modifier=modifier
    ){
        SoftPreview(
            animation
        )
    }
}

private enum class HardShape{
    SQUARE,
    TRIANGLE,
    DIAMOND
}

@Composable
private fun HardMotionCard(
    animation:NmixAnimationName,
    detail:String,
    shape:HardShape,
    modifier:Modifier
){
    MotionCardShell(
        animation=animation,
        detail=detail,
        modifier=modifier
    ){
        HardPreview(
            animation,
            shape
        )
    }
}

@Composable
private fun MotionCardShell(
    animation:NmixAnimationName,
    detail:String,
    modifier:Modifier,
    preview:@Composable ()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val selected=
        a.animation==animation

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
        spring(
            dampingRatio=.74f,
            stiffness=620f
        ),
        label="motionCardPress"
    )

    val shape=
        RoundedCornerShape(14.dp)

    val background=
        if(a.darkMode){
            Color.White.copy(
                alpha=.035f
            )
        }else{
            Color.White.copy(
                alpha=.70f
            )
        }

    Column(
        modifier
            .height(132.dp)
            .scale(scale)
            .clip(shape)
            .background(background)
            .border(
                if(selected)
                    1.1.dp
                else
                    .4.dp,
                if(selected){
                    p.accent
                }else{
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .08f
                            else
                                .14f
                    )
                },
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                a.setAnimation(
                    animation
                )
            }
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(71.dp)
                .clip(
                    RoundedCornerShape(
                        10.dp
                    )
                )
                .background(
                    if(a.darkMode){
                        Color(0xFF101513)
                    }else{
                        Color(0xFFE2E7E4)
                    }
                )
        ){
            preview()

            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(6.dp)
                        .size(12.dp),
                    if(a.darkMode)
                        Color.White
                    else
                        Color(0xFF1E2924)
                )
            }
        }

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            animation.label(),
            color=ui.text,
            fontSize=9.5.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Spacer(
            Modifier.height(2.dp)
        )

        Text(
            detail,
            color=ui.muted,
            fontSize=6.8.sp,
            lineHeight=8.5.sp,
            fontFamily=a.fontFamily,
            maxLines=2
        )
    }
}

@Composable
private fun SoftPreview(
    animation:NmixAnimationName
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val motion=
        rememberInfiniteTransition(
            label="softPreview${animation.name}"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                when(animation){
                    NmixAnimationName.DRIFT->
                        2600

                    NmixAnimationName.ORBIT->
                        2200

                    else->
                        2000
                },
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="softX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                3100,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="softY"
    )

    val firstX:Float
    val firstY:Float
    val secondX:Float
    val secondY:Float

    when(animation){
        NmixAnimationName.DRIFT->{
            firstX=x*25f
            firstY=y*8f
            secondX=-x*21f
            secondY=-y*9f
        }

        NmixAnimationName.ORBIT->{
            firstX=x*22f
            firstY=y*15f
            secondX=-y*22f
            secondY=x*15f
        }

        else->{
            firstX=x*31f
            firstY=x*7f
            secondX=x*25f
            secondY=-x*8f
        }
    }

    Box(
        Modifier.fillMaxSize()
    ){
        SoftOrb(
            color=p.accent,
            modifier=Modifier
                .align(
                    Alignment.CenterStart
                )
                .offset(
                    x=(-17).dp
                )
                .graphicsLayer{
                    translationX=firstX
                    translationY=firstY
                }
        )

        SoftOrb(
            color=p.accentLight,
            modifier=Modifier
                .align(
                    Alignment.CenterEnd
                )
                .offset(
                    x=17.dp
                )
                .graphicsLayer{
                    translationX=secondX
                    translationY=secondY
                }
        )
    }
}

@Composable
private fun SoftOrb(
    color:Color,
    modifier:Modifier
){
    Box(
        modifier
            .size(78.dp)
            .background(
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to
                            color.copy(
                                alpha=.62f
                            ),

                        .28f to
                            color.copy(
                                alpha=.38f
                            ),

                        .60f to
                            color.copy(
                                alpha=.12f
                            ),

                        .84f to
                            color.copy(
                                alpha=.035f
                            ),

                        1f to
                            Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

@Composable
private fun HardPreview(
    animation:NmixAnimationName,
    shape:HardShape
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val motion=
        rememberInfiniteTransition(
            label="hardPreview${animation.name}"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                1750,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="hardX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                2250,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="hardY"
    )

    val pulse=
        .78f+
            ((x+1f)/2f)*.38f

    Box(
        Modifier.fillMaxSize()
    ){
        Canvas(
            Modifier
                .size(42.dp)
                .align(
                    Alignment.Center
                )
                .graphicsLayer{
                    when(animation){
                        NmixAnimationName.FLOAT->{
                            translationX=x*23f
                            translationY=y*11f
                        }

                        NmixAnimationName.PULSE->{
                            translationX=x*5f
                            translationY=y*4f
                            scaleX=pulse
                            scaleY=pulse
                        }

                        NmixAnimationName.CROSS->{
                            translationX=x*29f
                            translationY=y*8f
                        }

                        else->{}
                    }
                }
        ){
            when(shape){
                HardShape.SQUARE->{
                    drawRoundRect(
                        color=
                            p.accent.copy(
                                alpha=.74f
                            ),
                        cornerRadius=
                            androidx.compose.ui.geometry.CornerRadius(
                                7.dp.toPx()
                            )
                    )
                }

                HardShape.TRIANGLE->{
                    val path=Path().apply{
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
                        path,
                        p.accent.copy(
                            alpha=.76f
                        )
                    )
                }

                HardShape.DIAMOND->{
                    val path=Path().apply{
                        moveTo(
                            size.width*.5f,
                            size.height*.06f
                        )

                        lineTo(
                            size.width*.94f,
                            size.height*.5f
                        )

                        lineTo(
                            size.width*.5f,
                            size.height*.94f
                        )

                        lineTo(
                            size.width*.06f,
                            size.height*.5f
                        )

                        close()
                    }

                    drawPath(
                        path,
                        p.accent.copy(
                            alpha=.76f
                        )
                    )
                }
            }
        }

        if(
            animation==
            NmixAnimationName.CROSS
        ){
            Canvas(
                Modifier
                    .size(31.dp)
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            -x*27f

                        translationY=
                            -y*7f
                    }
            ){
                drawCircle(
                    color=
                        p.accentLight.copy(
                            alpha=.66f
                        )
                )
            }
        }
    }
}
