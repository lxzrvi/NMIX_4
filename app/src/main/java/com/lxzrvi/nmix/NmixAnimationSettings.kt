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
            "Control motion, speed and elements",
            color=ui.muted,
            fontSize=9.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(10.dp)
        )

        MotionBeamSlider(
            title="Animation Speed",
            value=
                (
                    a.animationSpeed-
                        .45f
                    )/
                    (
                        2.20f-
                            .45f
                    ),
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
                progress->

                a.setAnimationSpeed(
                    .45f+
                        progress*
                        (
                            2.20f-
                                .45f
                        )
                )
            }
        )

        Spacer(
            Modifier.height(7.dp)
        )

        MotionBeamSlider(
            title="Animation Quantity",
            value=
                (
                    a.animationQuantity-
                        1
                    )/
                    4f,
            valueText=
                "${a.animationQuantity}",
            onChange={
                progress->

                val quantity=
                    (
                        1f+
                        progress*4f
                    )
                        .roundToInt()
                        .coerceIn(
                            1,
                            5
                        )

                a.setAnimationQuantity(
                    quantity
                )
            }
        )

        Spacer(
            Modifier.height(14.dp)
        )

        GroupLabel(
            title="SOFT",
            detail="Smooth • soft-edge • flowing"
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
                animation=
                    NmixAnimationName.DRIFT,
                detail=
                    "Slow soft orbs",
                soft=true,
                shape=
                    PreviewShape.ORB,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.ORBIT,
                detail=
                    "Soft square orbit",
                soft=true,
                shape=
                    PreviewShape.SQUARE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.FLOW,
                detail=
                    "Triangle flow",
                soft=true,
                shape=
                    PreviewShape.TRIANGLE,
                modifier=
                    Modifier.weight(1f)
            )
        }

        Spacer(
            Modifier.height(14.dp)
        )

        GroupLabel(
            title="HARD",
            detail="Defined • soft-edge • geometric"
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
                animation=
                    NmixAnimationName.FLOAT,
                detail=
                    "Square float",
                soft=false,
                shape=
                    PreviewShape.SQUARE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.PULSE,
                detail=
                    "Triangle pulse",
                soft=false,
                shape=
                    PreviewShape.TRIANGLE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.CROSS,
                detail=
                    "Diamond crossing",
                soft=false,
                shape=
                    PreviewShape.DIAMOND,
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
        value.coerceIn(
            0f,
            1f
        )

    val shape=
        RoundedCornerShape(15.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode){
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                }else{
                    Color.White.copy(
                        alpha=.67f
                    )
                }
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .18f
                        else
                            .27f
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
                modifier=
                    Modifier.weight(1f),
                color=ui.text,
                fontSize=9.5.sp,
                fontWeight=
                    FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Text(
                valueText,
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

        /*
         * Thick flat beam.
         * No thumb/circle.
         */
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .onSizeChanged{
                    widthPx=
                        it.width
                            .coerceAtLeast(1)
                }
                .pointerInput(
                    widthPx
                ){
                    detectDragGestures(
                        onDragStart={
                            point:Offset->

                            onChange(
                                (
                                    point.x/
                                        widthPx
                                            .toFloat()
                                    )
                                    .coerceIn(
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
                                        widthPx
                                            .toFloat()
                                    )
                                    .coerceIn(
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
                        RoundedCornerShape(
                            50
                        )
                    )
                    .background(
                        ui.muted.copy(
                            alpha=.16f
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxWidth(
                        progress
                    )
                    .height(7.dp)
                    .clip(
                        RoundedCornerShape(
                            50
                        )
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
private fun GroupLabel(
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
            fontWeight=
                FontWeight.Bold,
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
                animation=
                    animation,
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
            fontSize=9.3.sp,
            fontWeight=
                FontWeight.Bold,
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
    shape:PreviewShape
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val speed=
        a.animationSpeed
            .coerceIn(
                .45f,
                2.20f
            )

    val duration=
        (
            (
                if(soft)
                    2450f
                else
                    1850f
            )/
            speed
            )
            .roundToInt()
            .coerceAtLeast(
                500
            )

    val motion=
        rememberInfiniteTransition(
            label=
                "preview${animation.name}_${a.animationQuantity}_$duration"
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

    val pulse=
        .78f+
            ((x+1f)/2f)*
            .38f

    Box(
        Modifier.fillMaxSize()
    ){
        repeat(
            a.animationQuantity
        ){index->
            val direction=
                if(index%2==0)
                    1f
                else
                    -1f

            val factor=
                .58f+
                index*.13f

            PreviewGeometry(
                shape=shape,
                color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight,
                soft=soft,
                modifier=Modifier
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        when(animation){
                            NmixAnimationName.DRIFT->{
                                translationX=
                                    x*
                                    27f*
                                    direction*
                                    factor

                                translationY=
                                    y*
                                    11f*
                                    factor
                            }

                            NmixAnimationName.ORBIT->{
                                translationX=
                                    x*
                                    25f*
                                    direction*
                                    factor

                                translationY=
                                    y*
                                    17f*
                                    factor

                                rotationZ=
                                    x*
                                    18f*
                                    direction
                            }

                            NmixAnimationName.FLOW->{
                                translationX=
                                    x*
                                    34f*
                                    direction*
                                    factor

                                translationY=
                                    x*
                                    9f*
                                    factor
                            }

                            NmixAnimationName.FLOAT->{
                                translationX=
                                    x*
                                    27f*
                                    direction*
                                    factor

                                translationY=
                                    y*
                                    14f*
                                    factor

                                rotationZ=
                                    x*
                                    14f*
                                    direction
                            }

                            NmixAnimationName.PULSE->{
                                translationX=
                                    x*
                                    8f*
                                    direction*
                                    factor

                                translationY=
                                    y*
                                    6f*
                                    factor

                                scaleX=
                                    pulse*
                                    factor

                                scaleY=
                                    pulse*
                                    factor
                            }

                            NmixAnimationName.CROSS->{
                                translationX=
                                    x*
                                    32f*
                                    direction*
                                    factor

                                translationY=
                                    y*
                                    9f*
                                    factor

                                rotationZ=
                                    x*
                                    18f*
                                    direction
                            }
                        }
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
                50.dp
            else
                36.dp
        )
    ){
        val mainAlpha=
            if(soft)
                .46f
            else
                .69f

        when(shape){
            PreviewShape.ORB->{
                drawCircle(
                    brush=
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    color.copy(
                                        alpha=.62f
                                    ),

                                .48f to
                                    color.copy(
                                        alpha=.28f
                                    ),

                                1f to
                                    Color.Transparent
                            )
                        )
                )
            }

            PreviewShape.SQUARE->{
                /*
                 * Soft edges but square identity
                 * stays clearly visible.
                 */
                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                mainAlpha*
                                    .24f
                        ),
                    cornerRadius=
                        CornerRadius(
                            9.dp.toPx()
                        )
                )

                val inset=
                    if(soft)
                        5.dp.toPx()
                    else
                        3.dp.toPx()

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                mainAlpha
                        ),
                    topLeft=
                        Offset(
                            inset,
                            inset
                        ),
                    size=
                        androidx.compose.ui.geometry.Size(
                            size.width-
                                inset*2,
                            size.height-
                                inset*2
                        ),
                    cornerRadius=
                        CornerRadius(
                            7.dp.toPx()
                        )
                )
            }

            PreviewShape.TRIANGLE->{
                val path=
                    Path().apply{
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
                        alpha=
                            mainAlpha
                    )
                )
            }

            PreviewShape.DIAMOND->{
                val path=
                    Path().apply{
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
                        alpha=
                            mainAlpha
                    )
                )
            }
        }
    }
}
