package com.lxzrvi.nmix

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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
            "Choose motion and speed",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(10.dp)
        )

        AnimationSpeedSlider()

        Spacer(
            Modifier.height(13.dp)
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
            MotionCard(
                animation=NmixAnimationName.DRIFT,
                detail="Soft orb drift",
                soft=true,
                shape=MotionShape.ORB,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=NmixAnimationName.ORBIT,
                detail="Blurred square orbit",
                soft=true,
                shape=MotionShape.SQUARE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=NmixAnimationName.FLOW,
                detail="Soft triangle flow",
                soft=true,
                shape=MotionShape.TRIANGLE,
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
            MotionCard(
                animation=NmixAnimationName.FLOAT,
                detail="Sharp square float",
                soft=false,
                shape=MotionShape.SQUARE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=NmixAnimationName.PULSE,
                detail="Triangle pulse",
                soft=false,
                shape=MotionShape.TRIANGLE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=NmixAnimationName.CROSS,
                detail="Crossing diamonds",
                soft=false,
                shape=MotionShape.DIAMOND,
                modifier=Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnimationSpeedSlider(){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    var widthPx by remember{
        mutableIntStateOf(1)
    }

    val min=.55f
    val max=1.80f

    fun update(
        x:Float
    ){
        val progress=
            (
                x/
                widthPx
                    .toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )

        a.setAnimationSpeed(
            min+
                (
                    max-min
                )*
                progress
        )
    }

    val progress=
        (
            (
                a.animationSpeed-
                min
            )/
            (
                max-min
            )
            )
            .coerceIn(
                0f,
                1f
            )

    val label=
        when{
            a.animationSpeed<.82f->
                "Slow"

            a.animationSpeed<1.22f->
                "Normal"

            a.animationSpeed<1.52f->
                "Fast"

            else->
                "Rapid"
        }

    val shape=
        RoundedCornerShape(50)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode){
                    Color.White.copy(
                        alpha=.04f
                    )
                }else{
                    Color.White.copy(
                        alpha=.68f
                    )
                }
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .13f
                        else
                            .21f
                ),
                shape
            )
            .padding(
                horizontal=12.dp,
                vertical=9.dp
            )
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Text(
                "Animation Speed",
                modifier=
                    Modifier.weight(1f),
                color=ui.text,
                fontSize=9.5.sp,
                fontWeight=
                    FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Text(
                label,
                color=p.accent,
                fontSize=8.sp,
                fontWeight=
                    FontWeight.Bold,
                fontFamily=a.fontFamily
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(22.dp)
                .onSizeChanged{
                    widthPx=
                        it.width
                            .coerceAtLeast(1)
                }
                .pointerInput(
                    widthPx
                ){
                    detectHorizontalDragGestures(
                        onDragStart={
                            update(it.x)
                        },
                        onHorizontalDrag={
                            change,
                            _->

                            change.consume()

                            update(
                                change.position.x
                            )
                        }
                    )
                },
            contentAlignment=
                Alignment.CenterStart
        ){
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        ui.muted.copy(
                            alpha=.20f
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxWidth(
                        progress
                    )
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        p.accent
                    )
            )

            Box(
                Modifier
                    .offset(
                        x=
                            (
                                (
                                    widthPx*
                                    progress
                                )/
                                androidx.compose.ui.platform
                                    .LocalDensity
                                    .current
                                    .density
                            ).dp-
                            7.dp
                    )
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        p.accent
                    )
                    .border(
                        2.dp,
                        if(a.darkMode)
                            Color(0xFF151A18)
                        else
                            Color.White,
                        CircleShape
                    )
            )
        }
    }
}

private enum class MotionShape{
    ORB,
    SQUARE,
    TRIANGLE,
    DIAMOND
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
private fun MotionCard(
    animation:NmixAnimationName,
    detail:String,
    soft:Boolean,
    shape:MotionShape,
    modifier:Modifier
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
        if(pressed).97f else 1f,
        spring(
            dampingRatio=.74f,
            stiffness=620f
        ),
        label="motionCardPress"
    )

    val cardShape=
        RoundedCornerShape(14.dp)

    Column(
        modifier
            .height(132.dp)
            .scale(scale)
            .clip(cardShape)
            .background(
                if(a.darkMode){
                    Color.White.copy(
                        alpha=.035f
                    )
                }else{
                    Color.White.copy(
                        alpha=.70f
                    )
                }
            )
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
                cardShape
            )
            .clickable(
                interactionSource=
                    interaction,
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
                        Color(0xFFE1E7E4)
                    }
                )
        ){
            MotionPreview(
                animation=animation,
                soft=soft,
                shape=shape
            )

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
                        Color(0xFF202824)
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
private fun MotionPreview(
    animation:NmixAnimationName,
    soft:Boolean,
    shape:MotionShape
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val speed=
        a.animationSpeed
            .coerceIn(
                .55f,
                1.80f
            )

    val duration=
        (
            if(soft)
                2500/speed
            else
                1800/speed
            )
            .roundToInt()
            .coerceAtLeast(
                650
            )

    val motion=
        rememberInfiniteTransition(
            label=
                "motionPreview${animation.name}"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                duration,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                duration+570,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewY"
    )

    val pulse=
        .78f+
            ((x+1f)/2f)*
            .38f

    Box(
        Modifier.fillMaxSize()
    ){
        PreviewShape(
            shape=shape,
            color=p.accent,
            soft=soft,
            modifier=Modifier
                .align(
                    Alignment.Center
                )
                .graphicsLayer{
                    when(animation){
                        NmixAnimationName.DRIFT->{
                            translationX=x*25f
                            translationY=y*8f
                        }

                        NmixAnimationName.ORBIT->{
                            translationX=x*23f
                            translationY=y*15f
                            rotationZ=x*16f
                        }

                        NmixAnimationName.FLOW->{
                            translationX=x*31f
                            translationY=x*7f
                        }

                        NmixAnimationName.FLOAT->{
                            translationX=x*24f
                            translationY=y*12f
                            rotationZ=x*12f
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
                            rotationZ=x*18f
                        }
                    }
                }
        )

        if(
            animation==
                NmixAnimationName.CROSS
        ){
            PreviewShape(
                shape=MotionShape.DIAMOND,
                color=p.accentLight,
                soft=false,
                modifier=Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            -x*28f

                        translationY=
                            -y*7f

                        rotationZ=
                            -x*18f
                    }
            )
        }
    }
}

@Composable
private fun PreviewShape(
    shape:MotionShape,
    color:Color,
    soft:Boolean,
    modifier:Modifier
){
    Canvas(
        modifier.size(
            if(soft)
                58.dp
            else
                38.dp
        )
    ){
        val alpha=
            if(soft)
                .43f
            else
                .78f

        when(shape){
            MotionShape.ORB->{
                drawCircle(
                    brush=
                        Brush.radialGradient(
                            colorStops=
                                arrayOf(
                                    0f to
                                        color.copy(
                                            alpha=.60f
                                        ),

                                    .42f to
                                        color.copy(
                                            alpha=.28f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        )
                )
            }

            MotionShape.SQUARE->{
                if(soft){
                    drawRoundRect(
                        brush=
                            Brush.radialGradient(
                                listOf(
                                    color.copy(
                                        alpha=.52f
                                    ),
                                    color.copy(
                                        alpha=.18f
                                    ),
                                    Color.Transparent
                                )
                            ),
                        cornerRadius=
                            CornerRadius(
                                10.dp.toPx()
                            )
                    )
                }else{
                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=alpha
                            ),
                        cornerRadius=
                            CornerRadius(
                                6.dp.toPx()
                            )
                    )
                }
            }

            MotionShape.TRIANGLE->{
                val path=Path().apply{
                    moveTo(
                        size.width*.5f,
                        size.height*.08f
                    )

                    lineTo(
                        size.width*.92f,
                        size.height*.88f
                    )

                    lineTo(
                        size.width*.08f,
                        size.height*.88f
                    )

                    close()
                }

                drawPath(
                    path=path,
                    color=
                        color.copy(
                            alpha=
                                if(soft)
                                    .40f
                                else
                                    .80f
                        )
                )
            }

            MotionShape.DIAMOND->{
                val path=Path().apply{
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
                    color.copy(
                        alpha=alpha
                    )
                )
            }
        }
    }
}
