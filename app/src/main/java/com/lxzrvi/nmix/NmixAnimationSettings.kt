package com.lxzrvi.nmix

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
            "Live motion controls",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(10.dp))

        MotionBeamSlider(
            title="Animation Speed",
            value=
                (a.animationSpeed-.45f)/
                    (2.20f-.45f),
            valueText=
                when{
                    a.animationSpeed<.75f->
                        "Slow"

                    a.animationSpeed<1.25f->
                        "Normal"

                    a.animationSpeed<1.70f->
                        "Fast"

                    else->
                        "Rapid"
                },
            onChange={
                a.setAnimationSpeed(
                    .45f+
                        it*
                        (2.20f-.45f)
                )
            }
        )

        Spacer(Modifier.height(7.dp))

        MotionBeamSlider(
            title="Animation Quantity",
            value=
                (a.animationQuantity-1)/4f,
            valueText=
                "${a.animationQuantity}",
            onChange={
                a.setAnimationQuantity(
                    (
                        1f+
                            it*4f
                    )
                        .roundToInt()
                        .coerceIn(1,5)
                )
            }
        )

        Spacer(Modifier.height(13.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(8.dp)
        ){
            MotionChoice(
                title="Orb Drift",
                detail="Soft",
                animation=
                    NmixAnimationName.DRIFT,
                soft=true,
                modifier=
                    Modifier.weight(1f)
            )

            MotionChoice(
                title="Box Float",
                detail="Hard",
                animation=
                    NmixAnimationName.FLOAT,
                soft=false,
                modifier=
                    Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MotionBeamSlider(
    title:String,
    value:Float,
    valueText:String,
    onChange:(Float)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    var widthPx by remember{
        mutableIntStateOf(1)
    }

    val progress=
        value.coerceIn(0f,1f)

    val shape=
        RoundedCornerShape(14.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.82f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .025f
                        else
                            .018f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .16f
                        else
                            .23f
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
                title,
                Modifier.weight(1f),
                color=ui.text,
                fontSize=9.5.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Text(
                valueText,
                color=p.accent,
                fontSize=8.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=a.fontFamily
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(18.dp)
                .onSizeChanged{
                    widthPx=
                        it.width.coerceAtLeast(1)
                }
                .pointerInput(widthPx){
                    detectDragGestures(
                        onDragStart={
                            point->

                            onChange(
                                (
                                    point.x/
                                        widthPx.toFloat()
                                ).coerceIn(
                                    0f,
                                    1f
                                )
                            )
                        },
                        onDrag={
                            change,
                            _->

                            change.consume()

                            onChange(
                                (
                                    change.position.x/
                                        widthPx.toFloat()
                                ).coerceIn(
                                    0f,
                                    1f
                                )
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
                    .height(7.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        ui.muted.copy(
                            alpha=.14f
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(7.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                p.accentDark,
                                p.accent,
                                p.accentLight
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun MotionChoice(
    title:String,
    detail:String,
    animation:NmixAnimationName,
    soft:Boolean,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val selected=
        if(soft)
            a.animation!=
                NmixAnimationName.FLOAT
        else
            a.animation==
                NmixAnimationName.FLOAT

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by
        androidx.compose.animation.core
            .animateFloatAsState(
                targetValue=
                    if(pressed)
                        .97f
                    else
                        1f,
                label="motionChoice"
            )

    val shape=
        RoundedCornerShape(14.dp)

    Column(
        modifier
            .height(154.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.80f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .055f
                        else
                            .018f
                )
            )
            .border(
                if(selected)
                    1.dp
                else
                    .4.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .58f
                        else if(a.darkMode)
                            .13f
                        else
                            .20f
                ),
                shape
            )
            .clickable(
                interactionSource=
                    interaction,
                indication=null
            ){
                haptic{
                    a.setAnimation(
                        animation
                    )
                }
            }
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(103.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    if(a.darkMode)
                        Color(0xFF0F1412)
                    else
                        Color(0xFFF0F3F1)
                )
        ){
            WorldPreview(
                soft=soft
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
                    p.accent
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            title,
            color=ui.text,
            fontSize=9.5.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
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
private fun WorldPreview(
    soft:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val world=
        rememberNmixWorldMotion(
            label=
                if(soft)
                    "softPreview"
                else
                    "hardPreview"
        )

    Box(
        Modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(10.dp)
            )
    ){
        world.bodies.forEachIndexed{
            index,
            body->

            /*
             * Preview is a tiny window into exactly
             * the same oversized imaginary world.
             */
            val x=
                body.x*
                    (
                        125f+
                            index*10f
                    )

            val y=
                body.y*
                    (
                        88f+
                            index*8f
                    )

            if(soft){
                val elementSize=
                    when(index){
                        0->156.dp
                        1->137.dp
                        2->121.dp
                        3->130.dp
                        else->108.dp
                    }

                Box(
                    Modifier
                        .size(elementSize)
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer{
                            translationX=x
                            translationY=y

                            scaleX=
                                body.pulse

                            scaleY=
                                body.pulse
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
                                                        .58f
                                                    else
                                                        .48f
                                            ),

                                        .32f to
                                            p.accent.copy(
                                                alpha=.29f
                                            ),

                                        .62f to
                                            p.accent.copy(
                                                alpha=.105f
                                            ),

                                        .82f to
                                            p.accent.copy(
                                                alpha=.025f
                                            ),

                                        1f to
                                            Color.Transparent
                                    )
                            ),
                            CircleShape
                        )
                )
            }else{
                val elementSize=
                    when(index){
                        0->86.dp
                        1->76.dp
                        2->67.dp
                        3->72.dp
                        else->61.dp
                    }

                Canvas(
                    Modifier
                        .size(elementSize)
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer{
                            translationX=x
                            translationY=y

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

                    val inset=
                        4.dp.toPx()

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.05f
                            ),
                        cornerRadius=
                            CornerRadius(
                                13.dp.toPx()
                            )
                    )

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=
                                    if(a.darkMode)
                                        .26f
                                    else
                                        .21f
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
                                10.dp.toPx()
                            )
                    )

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.25f
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
                                10.dp.toPx()
                            ),
                        style=
                            Stroke(
                                1.dp.toPx()
                            )
                    )
                }
            }
        }
    }
}
