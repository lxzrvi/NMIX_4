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
import androidx.compose.ui.draw.blur
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
            value=
                (
                    a.animationSpeed-.45f
                )/
                (
                    2.20f-.45f
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
            onChange={progress->
                a.setAnimationSpeed(
                    .45f+
                        progress*
                        (
                            2.20f-.45f
                        )
                )
            }
        )

        Spacer(Modifier.height(7.dp))

        MotionBeamSlider(
            title="Animation Quantity",
            value=
                (
                    a.animationQuantity-1
                )/4f,
            valueText=
                "${a.animationQuantity}",
            onChange={progress->
                a.setAnimationQuantity(
                    (
                        1f+
                            progress*4f
                    )
                        .roundToInt()
                        .coerceIn(1,5)
                )
            }
        )

        Spacer(Modifier.height(14.dp))

        GroupLabel(
            title="SOFT",
            detail="Blurred • flowing • bounded"
        )

        Spacer(Modifier.height(7.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            MotionCard(
                animation=
                    NmixAnimationName.DRIFT,
                detail="Soft orb drift",
                soft=true,
                shape=PreviewShape.ORB,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.ORBIT,
                detail="Soft square orbit",
                soft=true,
                shape=PreviewShape.SQUARE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.FLOW,
                detail="Soft triangle flow",
                soft=true,
                shape=PreviewShape.TRIANGLE,
                modifier=
                    Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        GroupLabel(
            title="HARD",
            detail="Defined • low-opacity • bounded"
        )

        Spacer(Modifier.height(7.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            MotionCard(
                animation=
                    NmixAnimationName.FLOAT,
                detail="Square bounce",
                soft=false,
                shape=PreviewShape.SQUARE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.PULSE,
                detail="Triangle pulse",
                soft=false,
                shape=PreviewShape.TRIANGLE,
                modifier=
                    Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.CROSS,
                detail="Diamond crossing",
                soft=false,
                shape=PreviewShape.DIAMOND,
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
                if(a.darkMode){
                    Color(0xFF151A18)
                        .copy(alpha=.82f)
                }else{
                    Color(0xFFE8ECEA)
                        .copy(alpha=.88f)
                }
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .18f
                        else
                            .25f
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

        Spacer(Modifier.height(8.dp))

        /*
         * Thick flat beam.
         * No circular thumb.
         */
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
                        onDragStart={point->
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
                    .height(8.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        ui.muted.copy(
                            alpha=.15f
                        )
                    )
            )

            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
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

    val selected=
        a.animation==animation

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .97f
                else
                    1f,
            animationSpec=spring(
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
                    Color(0xFF151A18)
                        .copy(alpha=.76f)
                }else{
                    Color(0xFFE8ECEA)
                        .copy(alpha=.84f)
                }
            )
            .border(
                if(selected)
                    1.1.dp
                else
                    .4.dp,
                if(selected)
                    p.accent
                else
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .10f
                            else
                                .16f
                    ),
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
                .clip(
                    RoundedCornerShape(10.dp)
                )
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
    val dx:Float,
    val dy:Float,
    val size:Float
)

private val previewPlacements=
    listOf(
        PreviewPlacement(
            -.28f,-.22f,
            1f,.72f,.90f
        ),
        PreviewPlacement(
            .28f,.21f,
            -.86f,-.94f,.72f
        ),
        PreviewPlacement(
            .26f,-.23f,
            .74f,-.81f,.60f
        ),
        PreviewPlacement(
            -.27f,.25f,
            -.70f,.84f,.66f
        ),
        PreviewPlacement(
            0f,.02f,
            .56f,-.60f,.54f
        )
    )
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
            .coerceIn(.45f,2.20f)

    /*
     * Preview speed follows the exact user
     * Animation Speed preference.
     */
    val baseDuration=
        if(soft)
            2400f
        else
            1850f

    val duration=
        (
            baseDuration/
                speed
        )
            .roundToInt()
            .coerceAtLeast(480)

    val transition=
        rememberInfiniteTransition(
            label=
                "preview_${animation.name}_$duration"
        )

    /*
     * Three independent axes. RepeatMode.Reverse
     * makes each path turn at its bounds, giving
     * an imaginary-wall bounce feel.
     */
    val x by transition.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=
            infiniteRepeatable(
                animation=tween(
                    durationMillis=duration,
                    easing=EaseInOutSine
                ),
                repeatMode=
                    RepeatMode.Reverse
            ),
        label="previewX"
    )

    val y by transition.animateFloat(
        initialValue=1f,
        targetValue=-1f,
        animationSpec=
            infiniteRepeatable(
                animation=tween(
                    durationMillis=
                        duration+370,
                    easing=EaseInOutSine
                ),
                repeatMode=
                    RepeatMode.Reverse
            ),
        label="previewY"
    )

    val z by transition.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=
            infiniteRepeatable(
                animation=tween(
                    durationMillis=
                        duration+710,
                    easing=EaseInOutSine
                ),
                repeatMode=
                    RepeatMode.Reverse
            ),
        label="previewZ"
    )

    BoxWithConstraints(
        Modifier.fillMaxSize()
    ){
        /*
         * Spread is relative to preview bounds,
         * so quantity does not pile up at center.
         */
        val spreadX=
            maxWidth*.72f

        val spreadY=
            maxHeight*.66f

        repeat(
            a.animationQuantity
                .coerceIn(1,5)
        ){index->
            val placement=
                previewPlacements[
                    index%
                        previewPlacements.size
                ]

            val mx=
                when(index){
                    0->x
                    1->z
                    2->-y
                    3->-x
                    else->y
                }

            val my=
                when(index){
                    0->y
                    1->-x
                    2->z
                    3->-z
                    else->-x
                }

            val direction=
                if(index%2==0)
                    1f
                else
                    -1f

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
                        .align(
                            Alignment.Center
                        )
                        .offset(
                            x=
                                spreadX*
                                    placement.x,
                            y=
                                spreadY*
                                    placement.y
                        )
                        /*
                         * Soft geometry gets a small
                         * real Compose blur in
                         * addition to feathered draw.
                         */
                        .then(
                            if(soft){
                                Modifier.blur(
                                    1.6.dp
                                )
                            }else{
                                Modifier
                            }
                        )
                        .graphicsLayer{
                            translationX=
                                mx*
                                    13f*
                                    placement.dx

                            translationY=
                                my*
                                    8f*
                                    placement.dy

                            rotationZ=
                                when(animation){
                                    NmixAnimationName.ORBIT->
                                        z*
                                            18f*
                                            direction

                                    NmixAnimationName.FLOW->
                                        x*
                                            8f*
                                            direction

                                    NmixAnimationName.FLOAT->
                                        z*
                                            13f*
                                            direction

                                    NmixAnimationName.CROSS->
                                        z*
                                            17f*
                                            direction

                                    else->
                                        0f
                                }

                            val pulse=
                                when(animation){
                                    NmixAnimationName.PULSE->
                                        .82f+
                                            (
                                                (x+1f)/
                                                    2f
                                            )*.25f

                                    else->
                                        1f
                                }

                            val finalScale=
                                placement.size*
                                    pulse

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
                48.dp
            else
                38.dp
        )
    ){
        if(soft){
            /*
             * SOFT
             *
             * Geometry remains identifiable,
             * while edges fade instead of
             * disappearing completely.
             */
            when(shape){
                PreviewShape.ORB->{
                    drawCircle(
                        brush=
                            Brush.radialGradient(
                                colorStops=
                                    arrayOf(
                                        0f to
                                            color.copy(
                                                alpha=.68f
                                            ),

                                        .42f to
                                            color.copy(
                                                alpha=.40f
                                            ),

                                        .68f to
                                            color.copy(
                                                alpha=.18f
                                            ),

                                        .86f to
                                            color.copy(
                                                alpha=.055f
                                            ),

                                        1f to
                                            Color.Transparent
                                    )
                            )
                    )
                }

                PreviewShape.SQUARE->{
                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.10f
                            ),
                        cornerRadius=
                            CornerRadius(
                                9.dp.toPx()
                            )
                    )

                    val inset1=
                        3.dp.toPx()

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.22f
                            ),
                        topLeft=
                            Offset(
                                inset1,
                                inset1
                            ),
                        size=
                            Size(
                                size.width-
                                    inset1*2,
                                size.height-
                                    inset1*2
                            ),
                        cornerRadius=
                            CornerRadius(
                                8.dp.toPx()
                            )
                    )

                    val inset2=
                        6.dp.toPx()

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.38f
                            ),
                        topLeft=
                            Offset(
                                inset2,
                                inset2
                            ),
                        size=
                            Size(
                                size.width-
                                    inset2*2,
                                size.height-
                                    inset2*2
                            ),
                        cornerRadius=
                            CornerRadius(
                                6.dp.toPx()
                            )
                    )
                }

                PreviewShape.TRIANGLE->{
                    val outer=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.05f
                            )

                            lineTo(
                                size.width*.95f,
                                size.height*.91f
                            )

                            lineTo(
                                size.width*.05f,
                                size.height*.91f
                            )

                            close()
                        }

                    drawPath(
                        outer,
                        color.copy(
                            alpha=.09f
                        )
                    )

                    val inner=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.13f
                            )

                            lineTo(
                                size.width*.87f,
                                size.height*.85f
                            )

                            lineTo(
                                size.width*.13f,
                                size.height*.85f
                            )

                            close()
                        }

                    drawPath(
                        inner,
                        color.copy(
                            alpha=.38f
                        )
                    )

                    drawPath(
                        inner,
                        color.copy(
                            alpha=.12f
                        ),
                        style=Stroke(
                            width=
                                2.dp.toPx()
                        )
                    )
                }

                PreviewShape.DIAMOND->{
                    val outer=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.03f
                            )

                            lineTo(
                                size.width*.97f,
                                size.height*.5f
                            )

                            lineTo(
                                size.width*.5f,
                                size.height*.97f
                            )

                            lineTo(
                                size.width*.03f,
                                size.height*.5f
                            )

                            close()
                        }

                    drawPath(
                        outer,
                        color.copy(
                            alpha=.09f
                        )
                    )

                    val inner=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.11f
                            )

                            lineTo(
                                size.width*.89f,
                                size.height*.5f
                            )

                            lineTo(
                                size.width*.5f,
                                size.height*.89f
                            )

                            lineTo(
                                size.width*.11f,
                                size.height*.5f
                            )

                            close()
                        }

                    drawPath(
                        inner,
                        color.copy(
                            alpha=.36f
                        )
                    )
                }
            }
        }else{
            /*
             * HARD
             *
             * Shape is recognisable but has lower
             * opacity and a feather-like outer
             * shell instead of razor-hard chunks.
             */
            when(shape){
                PreviewShape.ORB->{
                    drawCircle(
                        color=
                            color.copy(
                                alpha=.10f
                            )
                    )

                    drawCircle(
                        color=
                            color.copy(
                                alpha=.43f
                            ),
                        radius=
                            size.minDimension*
                                .42f
                    )
                }

                PreviewShape.SQUARE->{
                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.08f
                            ),
                        cornerRadius=
                            CornerRadius(
                                7.dp.toPx()
                            )
                    )

                    val inset=
                        3.dp.toPx()

                    drawRoundRect(
                        color=
                            color.copy(
                                alpha=.42f
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
                                5.dp.toPx()
                            )
                    )
                }

                PreviewShape.TRIANGLE->{
                    val outer=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.04f
                            )

                            lineTo(
                                size.width*.96f,
                                size.height*.91f
                            )

                            lineTo(
                                size.width*.04f,
                                size.height*.91f
                            )

                            close()
                        }

                    drawPath(
                        outer,
                        color.copy(
                            alpha=.07f
                        )
                    )

                    val inset=
                        Path().apply{
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
                        inset,
                        color.copy(
                            alpha=.42f
                        )
                    )

                    drawPath(
                        inset,
                        color.copy(
                            alpha=.10f
                        ),
                        style=Stroke(
                            1.5.dp.toPx()
                        )
                    )
                }

                PreviewShape.DIAMOND->{
                    val outer=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.02f
                            )

                            lineTo(
                                size.width*.98f,
                                size.height*.5f
                            )

                            lineTo(
                                size.width*.5f,
                                size.height*.98f
                            )

                            lineTo(
                                size.width*.02f,
                                size.height*.5f
                            )

                            close()
                        }

                    drawPath(
                        outer,
                        color.copy(
                            alpha=.07f
                        )
                    )

                    val inner=
                        Path().apply{
                            moveTo(
                                size.width*.5f,
                                size.height*.09f
                            )

                            lineTo(
                                size.width*.91f,
                                size.height*.5f
                            )

                            lineTo(
                                size.width*.5f,
                                size.height*.91f
                            )

                            lineTo(
                                size.width*.09f,
                                size.height*.5f
                            )

                            close()
                        }

                    drawPath(
                        inner,
                        color.copy(
                            alpha=.40f
                        )
                    )

                    drawPath(
                        inner,
                        color.copy(
                            alpha=.09f
                        ),
                        style=Stroke(
                            1.5.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}
