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
import androidx.compose.ui.geometry.Offset
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

        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(8.dp),
            verticalAlignment=
                Alignment.Top
        ){
            MotionGroup(
                title="SOFT",
                detail="Blurred • flowing",
                soft=true,
                animations=
                    listOf(
                        NmixAnimationName.DRIFT,
                        NmixAnimationName.ORBIT,
                        NmixAnimationName.FLOW
                    ),
                modifier=
                    Modifier.weight(1f)
            )

            MotionGroup(
                title="HARD",
                detail="Defined • feathered",
                soft=false,
                animations=
                    listOf(
                        NmixAnimationName.FLOAT,
                        NmixAnimationName.PULSE,
                        NmixAnimationName.CROSS
                    ),
                modifier=
                    Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MotionGroup(
    title:String,
    detail:String,
    soft:Boolean,
    animations:List<NmixAnimationName>,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(15.dp)

    Column(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                else
                    Color.White
                        .copy(alpha=.86f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .035f
                        else
                            .022f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .21f
                ),
                shape
            )
            .padding(7.dp)
    ){
        Text(
            title,
            color=p.accent,
            fontSize=8.dp.value.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=1.1.sp,
            fontFamily=a.fontFamily
        )

        Text(
            detail,
            color=ui.muted,
            fontSize=6.7.sp,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Spacer(Modifier.height(7.dp))

        Column(
            verticalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            animations.forEach{
                animation->

                MotionCard(
                    animation=animation,
                    soft=soft
                )
            }
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
                        .copy(alpha=.88f)
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
                            alpha=.14f
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
private fun MotionCard(
    animation:NmixAnimationName,
    soft:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val selected=
        a.animation==animation

    val interaction=
        remember{
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
        label="motionPress"
    )

    val selection by animateFloatAsState(
        targetValue=
            if(selected)
                1f
            else
                0f,
        animationSpec=tween(240),
        label="motionSelection"
    )

    val shape=
        RoundedCornerShape(12.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .height(112.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF111614)
                        .copy(alpha=.90f)
                else
                    Color(0xFFF7F8F7)
                        .copy(alpha=.94f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        .025f+
                            selection*.035f
                )
            )
            .border(
                (
                    .4f+
                        selection*.65f
                ).dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .70f
                        else if(a.darkMode)
                            .12f
                        else
                            .18f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                a.setAnimation(animation)
            }
            .padding(5.dp)
    ){
        Box(
            Modifier
                .fillMaxWidth()
                .height(69.dp)
                .clip(
                    RoundedCornerShape(9.dp)
                )
                .background(
                    if(a.darkMode)
                        Color(0xFF0D1210)
                    else
                        Color(0xFFEFF3F1)
                )
        ){
            MotionPreview(
                animation=animation,
                soft=soft
            )

            if(selected){
                NmixIcon(
                    NmixIcon.CHECK,
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(5.dp)
                        .size(11.dp),
                    p.accent
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        Text(
            animation.label(),
            color=ui.text,
            fontSize=8.7.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily,
            maxLines=1
        )

        Text(
            when(animation){
                NmixAnimationName.DRIFT->
                    "Orb drift"

                NmixAnimationName.ORBIT->
                    "Wide orbit"

                NmixAnimationName.FLOW->
                    "Soft flow"

                NmixAnimationName.FLOAT->
                    "Shape float"

                NmixAnimationName.PULSE->
                    "Triangle pulse"

                NmixAnimationName.CROSS->
                    "Triangle cross"
            },
            color=ui.muted,
            fontSize=6.4.sp,
            fontFamily=a.fontFamily,
            maxLines=1
        )
    }
}

@Composable
private fun MotionPreview(
    animation:NmixAnimationName,
    soft:Boolean
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
                    2450f
                else
                    1900f
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
        initialValue=-1f,
        targetValue=1f,
        animationSpec=
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
        initialValue=1f,
        targetValue=-1f,
        animationSpec=
            infiniteRepeatable(
                tween(
                    duration+360,
                    easing=EaseInOutSine
                ),
                RepeatMode.Reverse
            ),
        label="previewY"
    )

    val z by transition.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=
            infiniteRepeatable(
                tween(
                    duration+710,
                    easing=EaseInOutSine
                ),
                RepeatMode.Reverse
            ),
        label="previewZ"
    )

    val homes=
        listOf(
            Offset(-.43f,-.30f),
            Offset(.43f,.30f),
            Offset(.40f,-.31f),
            Offset(-.40f,.31f),
            Offset(0f,0f)
        )

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(9.dp)
            )
    ){
        val spreadX=maxWidth
        val spreadY=maxHeight

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

            if(soft){
                Box(
                    Modifier
                        .size(
                            when(index){
                                0->91.dp
                                1->80.dp
                                2->72.dp
                                3->77.dp
                                else->66.dp
                            }
                        )
                        .align(
                            Alignment.Center
                        )
                        .offset(
                            x=spreadX*home.x,
                            y=spreadY*home.y
                        )
                        .blur(2.dp)
                        .graphicsLayer{
                            translationX=
                                mx*
                                    (
                                        42f+
                                            index*3f
                                    )

                            translationY=
                                my*
                                    (
                                        25f+
                                            index*2f
                                    )

                            rotationZ=
                                when(animation){
                                    NmixAnimationName.ORBIT->
                                        z*
                                            20f*
                                            direction

                                    NmixAnimationName.FLOW->
                                        x*
                                            12f*
                                            direction

                                    else->0f
                                }

                            val pulse=
                                if(
                                    animation==
                                    NmixAnimationName.DRIFT
                                )
                                    .90f+
                                        (
                                            (z+1f)/
                                                2f
                                        )*.20f
                                else
                                    1f

                            scaleX=pulse
                            scaleY=pulse
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

                                        .35f to
                                            p.accent.copy(
                                                alpha=.29f
                                            ),

                                        .65f to
                                            p.accent.copy(
                                                alpha=.11f
                                            ),

                                        .84f to
                                            p.accent.copy(
                                                alpha=.035f
                                            ),

                                        1f to
                                            Color.Transparent
                                    )
                            ),
                            RoundedCornerShape(50)
                        )
                )
            }else{
                Canvas(
                    Modifier
                        .size(
                            when(index){
                                0->61.dp
                                1->55.dp
                                2->49.dp
                                3->52.dp
                                else->45.dp
                            }
                        )
                        .align(
                            Alignment.Center
                        )
                        .offset(
                            x=spreadX*home.x,
                            y=spreadY*home.y
                        )
                        .graphicsLayer{
                            translationX=
                                mx*
                                    (
                                        45f+
                                            index*3f
                                    )

                            translationY=
                                my*
                                    (
                                        27f+
                                            index*2f
                                    )

                            rotationZ=
                                z*
                                    20f*
                                    direction

                            val pulse=
                                if(
                                    animation==
                                    NmixAnimationName.PULSE
                                )
                                    .80f+
                                        (
                                            (x+1f)/
                                                2f
                                        )*.32f
                                else
                                    1f

                            scaleX=pulse
                            scaleY=pulse
                        }
                ){
                    val color=
                        if(index%2==0)
                            p.accent
                        else
                            p.accentLight

                    val triangle=
                        Path().apply{
                            moveTo(
                                size.width*.50f,
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
                        triangle,
                        color.copy(
                            alpha=
                                if(a.darkMode)
                                    .16f
                                else
                                    .13f
                        )
                    )

                    val inner=
                        Path().apply{
                            moveTo(
                                size.width*.50f,
                                size.height*.12f
                            )

                            lineTo(
                                size.width*.88f,
                                size.height*.84f
                            )

                            lineTo(
                                size.width*.12f,
                                size.height*.84f
                            )

                            close()
                        }

                    drawPath(
                        inner,
                        color.copy(
                            alpha=
                                if(a.darkMode)
                                    .29f
                                else
                                    .23f
                        )
                    )

                    drawPath(
                        inner,
                        color.copy(
                            alpha=
                                if(a.darkMode)
                                    .38f
                                else
                                    .30f
                        ),
                        style=
                            Stroke(
                                1.5.dp.toPx()
                            )
                    )
                }
            }
        }
    }
}
