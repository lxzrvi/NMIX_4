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
                progress->

                a.setAnimationSpeed(
                    .45f+
                        progress*
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
                progress->

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
            detail="Blurred • flowing • wide"
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
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.ORBIT,
                detail="Soft square orbit",
                soft=true,
                shape=PreviewShape.SQUARE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.FLOW,
                detail="Soft triangle flow",
                soft=true,
                shape=PreviewShape.TRIANGLE,
                modifier=Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        GroupLabel(
            title="HARD",
            detail="Defined • translucent • wide"
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
                detail="Square float",
                soft=false,
                shape=PreviewShape.SQUARE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.PULSE,
                detail="Triangle pulse",
                soft=false,
                shape=PreviewShape.TRIANGLE,
                modifier=Modifier.weight(1f)
            )

            MotionCard(
                animation=
                    NmixAnimationName.CROSS,
                detail="Diamond crossing",
                soft=false,
                shape=PreviewShape.DIAMOND,
                modifier=Modifier.weight(1f)
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
                    Color(0xFFE8ECEA)
                        .copy(alpha=.88f)
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

    val scale by animateFloatAsState(
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
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.76f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.84f)
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

@Composable
private fun MotionPreview(
    animation:NmixAnimationName,
    soft:Boolean,
    shape:PreviewShape
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val speed=
        a.animationSpeed.coerceIn(
            .45f,
            2.20f
        )

    val duration=
        (
            (
                if(soft)
                    2400f
                else
                    1850f
            )/
                speed
        )
            .roundToInt()
            .coerceAtLeast(480)

    val transition=
        rememberInfiniteTransition(
            label=
                "preview_${animation.name}_$duration"
        )

    val x by transition.animateFloat(
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

    val y by transition.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                duration+390,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewY"
    )

    val z by transition.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                duration+760,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="previewZ"
    )

    val homes=listOf(
        Offset(-.39f,-.30f),
        Offset(.39f,.29f),
        Offset(.37f,-.31f),
        Offset(-.38f,.31f),
        Offset(.01f,.01f)
    )

    BoxWithConstraints(
        Modifier.fillMaxSize()
    ){
        val spreadX=
            maxWidth*.90f

        val spreadY=
            maxHeight*.82f

        repeat(
            a.animationQuantity
                .coerceIn(1,5)
        ){index->
            val home=homes[index]

            val mx=when(index){
                0->x
                1->z
                2->-y
                3->-x
                else->y
            }

            val my=when(index){
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

            val itemScale=
                when(index){
                    0->1.04f
                    1->.86f
                    2->.72f
                    3->.80f
                    else->.66f
                }

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
                            x=spreadX*home.x,
                            y=spreadY*home.y
                        )
                        .then(
                            if(soft)
                                Modifier.blur(1.4.dp)
                            else
                                Modifier
                        )
                        .graphicsLayer{
                            translationX=
                                mx*
                                    (
                                        25f+
                                            index*2f
                                    )

                            translationY=
                                my*
                                    (
                                        15f+
                                            index*1.5f
                                    )

                            rotationZ=
                                when(animation){
                                    NmixAnimationName.ORBIT->
                                        z*19f*
                                            direction

                                    NmixAnimationName.FLOW->
                                        x*10f*
                                            direction

                                    NmixAnimationName.FLOAT->
                                        z*15f*
                                            direction

                                    NmixAnimationName.CROSS->
                                        z*19f*
                                            direction

                                    else->0f
                                }

                            val pulse=
                                if(
                                    animation==
                                    NmixAnimationName.PULSE
                                ){
                                    .80f+
                                        (
                                            (x+1f)/2f
                                        )*.30f
                                }else{
                                    1f
                                }

                            scaleX=
                                itemScale*pulse

                            scaleY=
                                itemScale*pulse
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
                52.dp
            else
                40.dp
        )
    ){
        when(shape){
            PreviewShape.ORB->{
                drawCircle(
                    brush=
                        Brush.radialGradient(
                            colorStops=
                                if(soft){
                                    arrayOf(
                                        0f to
                                            color.copy(
                                                alpha=.66f
                                            ),
                                        .45f to
                                            color.copy(
                                                alpha=.34f
                                            ),
                                        .72f to
                                            color.copy(
                                                alpha=.12f
                                            ),
                                        1f to
                                            Color.Transparent
                                    )
                                }else{
                                    arrayOf(
                                        0f to
                                            color.copy(
                                                alpha=.38f
                                            ),
                                        .80f to
                                            color.copy(
                                                alpha=.16f
                                            ),
                                        1f to
                                            Color.Transparent
                                    )
                                }
                        )
                )
            }

            PreviewShape.SQUARE->{
                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                if(soft)
                                    .10f
                                else
                                    .06f
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
                                if(soft)
                                    .36f
                                else
                                    .36f
                        ),
                    topLeft=
                        Offset(inset,inset),
                    size=
                        Size(
                            size.width-inset*2,
                            size.height-inset*2
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
                        alpha=
                            if(soft)
                                .08f
                            else
                                .055f
                    )
                )

                val inner=
                    Path().apply{
                        moveTo(
                            size.width*.5f,
                            size.height*.12f
                        )

                        lineTo(
                            size.width*.88f,
                            size.height*.85f
                        )

                        lineTo(
                            size.width*.12f,
                            size.height*.85f
                        )

                        close()
                    }

                drawPath(
                    inner,
                    color.copy(
                        alpha=
                            if(soft)
                                .35f
                            else
                                .34f
                    )
                )

                drawPath(
                    inner,
                    color.copy(alpha=.08f),
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
                        alpha=
                            if(soft)
                                .08f
                            else
                                .055f
                    )
                )

                val inner=
                    Path().apply{
                        moveTo(
                            size.width*.5f,
                            size.height*.10f
                        )

                        lineTo(
                            size.width*.90f,
                            size.height*.5f
                        )

                        lineTo(
                            size.width*.5f,
                            size.height*.90f
                        )

                        lineTo(
                            size.width*.10f,
                            size.height*.5f
                        )

                        close()
                    }

                drawPath(
                    inner,
                    color.copy(
                        alpha=
                            if(soft)
                                .35f
                            else
                                .34f
                    )
                )

                drawPath(
                    inner,
                    color.copy(alpha=.08f),
                    style=Stroke(
                        1.5.dp.toPx()
                    )
                )
            }
        }
    }
}
