package com.lxzrvi.nmix

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
            "Control motion, speed and elements",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(10.dp))

        MotionBeamSlider(
            title="Animation Speed",
            value=(a.animationSpeed-.45f)/(2.20f-.45f),
            valueText=when{
                a.animationSpeed<.75f->"Slow"
                a.animationSpeed<1.25f->"Normal"
                a.animationSpeed<1.70f->"Fast"
                else->"Rapid"
            },
            onChange={progress->
                a.setAnimationSpeed(
                    .45f+progress*(2.20f-.45f)
                )
            }
        )

        Spacer(Modifier.height(7.dp))

        MotionBeamSlider(
            title="Animation Quantity",
            value=(a.animationQuantity-1)/4f,
            valueText="${a.animationQuantity}",
            onChange={progress->
                a.setAnimationQuantity(
                    (1f+progress*4f)
                        .roundToInt()
                        .coerceIn(1,5)
                )
            }
        )

        Spacer(Modifier.height(14.dp))

        GroupLabel(
            "SOFT",
            "Smooth • visible • flowing"
        )

        Spacer(Modifier.height(7.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(7.dp)
        ){
            MotionCard(
                NmixAnimationName.DRIFT,
                "Soft orb drift",
                true,
                PreviewShape.ORB,
                Modifier.weight(1f)
            )

            MotionCard(
                NmixAnimationName.ORBIT,
                "Soft square orbit",
                true,
                PreviewShape.SQUARE,
                Modifier.weight(1f)
            )

            MotionCard(
                NmixAnimationName.FLOW,
                "Triangle flow",
                true,
                PreviewShape.TRIANGLE,
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        GroupLabel(
            "HARD",
            "Defined • feathered • geometric"
        )

        Spacer(Modifier.height(7.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(7.dp)
        ){
            MotionCard(
                NmixAnimationName.FLOAT,
                "Square float",
                false,
                PreviewShape.SQUARE,
                Modifier.weight(1f)
            )

            MotionCard(
                NmixAnimationName.PULSE,
                "Triangle pulse",
                false,
                PreviewShape.TRIANGLE,
                Modifier.weight(1f)
            )

            MotionCard(
                NmixAnimationName.CROSS,
                "Diamond crossing",
                false,
                PreviewShape.DIAMOND,
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

    val progress=value.coerceIn(0f,1f)
    val shape=RoundedCornerShape(14.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode){
                    Color(0xFF151A18).copy(alpha=.82f)
                }else{
                    Color(0xFFE8ECEA).copy(alpha=.88f)
                }
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).18f else .25f
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
            verticalAlignment=Alignment.CenterVertically
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
                    widthPx=it.width.coerceAtLeast(1)
                }
                .pointerInput(widthPx){
                    detectDragGestures(
                        onDragStart={point->
                            onChange(
                                (point.x/widthPx.toFloat())
                                    .coerceIn(0f,1f)
                            )
                        },
                        onDrag={change,_->

                            change.consume()

                            onChange(
                                (change.position.x/widthPx.toFloat())
                                    .coerceIn(0f,1f)
                            )
                        }
                    )
                },
            contentAlignment=Alignment.CenterStart
        ){
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        ui.muted.copy(alpha=.15f)
                    )
            )

            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
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
private fun GroupLabel(
    title:String,
    detail:String
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment=Alignment.CenterVertically
    ){
        Text(
            title,
            color=p.accent,
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=1.2.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.width(7.dp))

        Text(
            detail,
            color=ui.muted,
            fontSize=7.sp,
            fontFamily=a.fontFamily
        )
    }
}

private enum class PreviewShape{
    ORB,
    SQUARE,
    TRIANGLE,
    DIAMOND
}

@Composable
private fun MotionCard(
    animation:NmixAnimationName,
    detail:String,
    soft:Boolean,
    shape:PreviewShape,
    modifier:Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val selected=a.animation==animation

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).97f else 1f,
        spring(
            dampingRatio=.74f,
            stiffness=620f
        ),
        label="motionCardPress"
    )

    val cardShape=RoundedCornerShape(14.dp)

    Column(
        modifier
            .height(132.dp)
            .scale(scale)
            .clip(cardShape)
            .background(
                if(a.darkMode){
                    Color(0xFF151A18).copy(alpha=.76f)
                }else{
                    Color(0xFFE8ECEA).copy(alpha=.84f)
                }
            )
            .border(
                if(selected)1.1.dp else .4.dp,
                if(selected){
                    p.accent
                }else{
                    p.accent.copy(
                        alpha=if(a.darkMode).10f else .16f
                    )
                },
                cardShape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                a.setAnimation(animation)
            }
            .padding(6.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(71.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if(a.darkMode)
                        Color(0xFF101513)
                    else
                        Color(0xFFE1E7E4)
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
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(12.dp),
                    if(a.darkMode)
                        Color.White
                    else
                        Color(0xFF202824)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            animation.label(),
            color=ui.text,
            fontSize=9.3.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Spacer(Modifier.height(2.dp))

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

private data class PreviewPlacement(
    val x:Float,
    val y:Float,
    val phaseX:Float,
    val phaseY:Float,
    val scale:Float
)

private val previewPlacements=listOf(
    PreviewPlacement(-.29f,-.20f, 1f, .72f,.88f),
    PreviewPlacement( .27f, .22f,-.82f,-1f,.72f),
    PreviewPlacement( .25f,-.24f, .70f,-.78f,.61f),
    PreviewPlacement(-.27f, .25f,-.62f, .68f,.67f),
    PreviewPlacement( .02f, .03f, .48f,-.52f,.57f)
)

@Composable
private fun MotionPreview(
    animation:NmixAnimationName,
    soft:Boolean,
    shape:PreviewShape
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val speed=a.animationSpeed.coerceIn(.45f,2.20f)

    val duration=(
        (if(soft)2450f else 1850f)/speed
    )
        .roundToInt()
        .coerceAtLeast(500)

    val motion=rememberInfiniteTransition(
        label="preview_${animation.name}_${duration}"
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
                duration+430,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewY"
    )

    val pulse=.82f+((x+1f)/2f)*.28f

    BoxWithConstraints(
        Modifier.fillMaxSize()
    ){
        val spreadX=maxWidth*.72f
        val spreadY=maxHeight*.68f

        repeat(a.animationQuantity){index->
            val placement=
                previewPlacements[
                    index%previewPlacements.size
                ]

            PreviewGeometry(
                shape=shape,
                color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight,
                soft=soft,
                modifier=
                    Modifier
                        .align(Alignment.Center)
                        .offset(
                            x=spreadX*placement.x,
                            y=spreadY*placement.y
                        )
                        .graphicsLayer{
                            val direction=
                                if(index%2==0)
                                    1f
                                else
                                    -1f

                            when(animation){
                                NmixAnimationName.DRIFT->{
                                    translationX=
                                        x*13f*
                                        placement.phaseX

                                    translationY=
                                        y*8f*
                                        placement.phaseY
                                }

                                NmixAnimationName.ORBIT->{
                                    translationX=
                                        x*14f*
                                        placement.phaseX

                                    translationY=
                                        y*10f*
                                        placement.phaseY

                                    rotationZ=
                                        x*15f*direction
                                }

                                NmixAnimationName.FLOW->{
                                    translationX=
                                        x*18f*
                                        placement.phaseX

                                    translationY=
                                        x*7f*
                                        placement.phaseY
                                }

                                NmixAnimationName.FLOAT->{
                                    translationX=
                                        x*15f*
                                        placement.phaseX

                                    translationY=
                                        y*9f*
                                        placement.phaseY

                                    rotationZ=
                                        x*12f*direction
                                }

                                NmixAnimationName.PULSE->{
                                    translationX=
                                        x*5f*
                                        placement.phaseX

                                    translationY=
                                        y*5f*
                                        placement.phaseY
                                }

                                NmixAnimationName.CROSS->{
                                    translationX=
                                        x*18f*
                                        placement.phaseX

                                    translationY=
                                        y*7f*
                                        placement.phaseY

                                    rotationZ=
                                        x*15f*direction
                                }
                            }

                            val finalScale=
                                placement.scale*
                                if(
                                    animation==
                                    NmixAnimationName.PULSE
                                )
                                    pulse
                                else
                                    1f

                            scaleX=finalScale
                            scaleY=finalScale
                        }
            )
        }
    }
}

@Composable
private fun PreviewGeometry(
    shape:PreviewShape,
    color:Color,
    soft:Boolean,
    modifier:Modifier
){
    Canvas(
        modifier.size(
            if(soft)
                46.dp
            else
                36.dp
        )
    ){
        val mainAlpha=
            if(soft)
                .58f
            else
                .55f

        when(shape){
            PreviewShape.ORB->{
                drawCircle(
                    brush=Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to color.copy(alpha=.70f),
                            .55f to color.copy(alpha=.35f),
                            .82f to color.copy(alpha=.12f),
                            1f to Color.Transparent
                        )
                    )
                )
            }

            PreviewShape.SQUARE->{
                /*
                 * Outer translucent layer gives
                 * the square a feathered edge.
                 */
                drawRoundRect(
                    color=color.copy(
                        alpha=
                            if(soft)
                                .12f
                            else
                                .10f
                    ),
                    cornerRadius=
                        CornerRadius(8.dp.toPx())
                )

                val inset=
                    if(soft)
                        4.dp.toPx()
                    else
                        2.5.dp.toPx()

                drawRoundRect(
                    color=color.copy(
                        alpha=mainAlpha
                    ),
                    topLeft=Offset(inset,inset),
                    size=Size(
                        size.width-inset*2,
                        size.height-inset*2
                    ),
                    cornerRadius=
                        CornerRadius(
                            if(soft)
                                6.dp.toPx()
                            else
                                5.dp.toPx()
                        )
                )
            }

            PreviewShape.TRIANGLE->{
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
                    path,
                    color.copy(
                        alpha=mainAlpha
                    )
                )

                drawPath(
                    path,
                    color.copy(alpha=.16f),
                    style=Stroke(
                        width=2.dp.toPx()
                    )
                )
            }

            PreviewShape.DIAMOND->{
                val path=Path().apply{
                    moveTo(
                        size.width*.5f,
                        size.height*.05f
                    )
                    lineTo(
                        size.width*.95f,
                        size.height*.5f
                    )
                    lineTo(
                        size.width*.5f,
                        size.height*.95f
                    )
                    lineTo(
                        size.width*.05f,
                        size.height*.5f
                    )
                    close()
                }

                drawPath(
                    path,
                    color.copy(
                        alpha=mainAlpha
                    )
                )

                drawPath(
                    path,
                    color.copy(alpha=.15f),
                    style=Stroke(
                        width=2.dp.toPx()
                    )
                )
            }
        }
    }
}
