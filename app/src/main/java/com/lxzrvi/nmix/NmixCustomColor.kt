package com.lxzrvi.nmix

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun NmixCustomColorPicker(
    visible:Boolean,
    onClose:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val ui=a.uiColors()

    var hue by remember(
        visible
    ){
        mutableFloatStateOf(
            colorHue(
                a.palette.accent
            )
        )
    }

    var saturation by remember(
        visible
    ){
        mutableFloatStateOf(
            .76f
        )
    }

    var value by remember(
        visible
    ){
        mutableFloatStateOf(
            .88f
        )
    }

    var hex by remember(
        visible
    ){
        mutableStateOf(
            colorHex(
                a.palette.accent
            )
        )
    }

    val selected=
        hsvColor(
            hue,
            saturation,
            value
        )

    LaunchedEffect(
        hue,
        saturation,
        value
    ){
        hex=
            colorHex(
                selected
            )
    }

    AnimatedVisibility(
        visible=visible,
        enter=
            fadeIn(
                tween(240)
            )+
            scaleIn(
                initialScale=.96f,
                animationSpec=tween(
                    300,
                    easing=EaseOutCubic
                )
            ),
        exit=
            fadeOut(
                tween(180)
            )+
            scaleOut(
                targetScale=.97f,
                animationSpec=
                    tween(220)
            )
    ){
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha=.22f
                    )
                )
                .clickable(
                    interactionSource=
                        remember{
                            MutableInteractionSource()
                        },
                    indication=null,
                    onClick=onClose
                ),
            contentAlignment=
                Alignment.Center
        ){
            val shape=
                RoundedCornerShape(
                    24.dp
                )

            Column(
                Modifier
                    .width(290.dp)
                    .clip(shape)
                    .background(
                        if(a.darkMode)
                            Color(0xFF151A18)
                        else
                            Color(0xFFF2F5F3)
                    )
                    .border(
                        .7.dp,
                        a.palette.accent
                            .copy(
                                alpha=.42f
                            ),
                        shape
                    )
                    .clickable(
                        interactionSource=
                            remember{
                                MutableInteractionSource()
                            },
                        indication=null
                    ){}
                    .padding(18.dp),
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Text(
                    "CUSTOM COLOR",
                    color=ui.text,
                    fontSize=11.sp,
                    fontWeight=
                        FontWeight.Bold,
                    letterSpacing=1.sp,
                    fontFamily=
                        a.fontFamily
                )

                Spacer(
                    Modifier.height(
                        15.dp
                    )
                )

                ColorWheel(
                    hue=hue,
                    saturation=saturation,
                    value=value,
                    onChange={
                        newHue,
                        newSat->

                        hue=newHue
                        saturation=newSat
                    }
                )

                Spacer(
                    Modifier.height(
                        14.dp
                    )
                )

                Box(
                    Modifier
                        .size(38.dp)
                        .clip(
                            CircleShape
                        )
                        .background(
                            selected
                        )
                        .border(
                            2.dp,
                            Color.White.copy(
                                alpha=.65f
                            ),
                            CircleShape
                        )
                )

                Spacer(
                    Modifier.height(
                        13.dp
                    )
                )

                val fieldShape=
                    RoundedCornerShape(
                        12.dp
                    )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(fieldShape)
                        .background(
                            a.palette.accent
                                .copy(
                                    alpha=.07f
                                )
                        )
                        .border(
                            .5.dp,
                            a.palette.accent
                                .copy(
                                    alpha=.24f
                                ),
                            fieldShape
                        )
                        .padding(
                            horizontal=12.dp
                        ),
                    contentAlignment=
                        Alignment.Center
                ){
                    BasicTextField(
                        value=hex,
                        onValueChange={
                            valueText->

                            val cleaned=
                                valueText
                                    .uppercase()
                                    .take(7)

                            hex=cleaned

                            parseHex(
                                cleaned
                            )?.let{
                                color->

                                hue=
                                    colorHue(
                                        color
                                    )

                                saturation=
                                    colorSaturation(
                                        color
                                    )

                                value=
                                    colorValue(
                                        color
                                    )
                            }
                        },
                        modifier=
                            Modifier
                                .fillMaxWidth(),
                        textStyle=
                            androidx.compose.ui.text.TextStyle(
                                color=ui.text,
                                fontSize=13.sp,
                                fontWeight=
                                    FontWeight.Bold,
                                textAlign=
                                    TextAlign.Center,
                                fontFamily=
                                    a.fontFamily
                            ),
                        singleLine=true
                    )
                }

                Spacer(
                    Modifier.height(
                        14.dp
                    )
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            9.dp
                        )
                ){
                    PickerButton(
                        text="Cancel",
                        modifier=
                            Modifier.weight(1f),
                        accent=false,
                        onClick=onClose
                    )

                    PickerButton(
                        text="Apply",
                        modifier=
                            Modifier.weight(1f),
                        accent=true
                    ){
                        val finalColor=
                            parseHex(hex)
                                ?:selected

                        a.setCustomColor(
                            finalColor
                        )

                        onClose()
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorWheel(
    hue:Float,
    saturation:Float,
    value:Float,
    onChange:(Float,Float)->Unit
){
    val density=
        LocalDensity.current

    Canvas(
        Modifier
            .size(190.dp)
            .pointerInput(Unit){
                detectDragGestures(
                    onDragStart={
                        point->

                        wheelPoint(
                            point.x,
                            point.y,
                            size.width,
                            size.height,
                            onChange
                        )
                    },
                    onDrag={
                        change,
                        _->

                        wheelPoint(
                            change.position.x,
                            change.position.y,
                            size.width,
                            size.height,
                            onChange
                        )
                    }
                )
            }
    ){
        val radius=
            min(
                size.width,
                size.height
            )/2f

        val center=
            Offset(
                size.width/2f,
                size.height/2f
            )

        for(angle in 0 until 360 step 3){
            val start=
                Math.toRadians(
                    angle.toDouble()
                )

            val end=
                Math.toRadians(
                    (angle+4).toDouble()
                )

            val path=
                androidx.compose.ui.graphics.Path()
                    .apply{
                        moveTo(
                            center.x,
                            center.y
                        )

                        lineTo(
                            center.x+
                                cos(start)
                                    .toFloat()*
                                radius,
                            center.y+
                                sin(start)
                                    .toFloat()*
                                radius
                        )

                        lineTo(
                            center.x+
                                cos(end)
                                    .toFloat()*
                                radius,
                            center.y+
                                sin(end)
                                    .toFloat()*
                                radius
                        )

                        close()
                    }

            drawPath(
                path,
                hsvColor(
                    angle.toFloat(),
                    1f,
                    value
                )
            )
        }

        drawCircle(
            brush=
                Brush.radialGradient(
                    colors=listOf(
                        Color.White,
                        Color.Transparent
                    ),
                    center=center,
                    radius=radius
                ),
            radius=radius,
            center=center
        )

        val r=
            saturation*
                radius

        val rad=
            Math.toRadians(
                hue.toDouble()
            )

        val marker=
            Offset(
                center.x+
                    cos(rad)
                        .toFloat()*
                    r,
                center.y+
                    sin(rad)
                        .toFloat()*
                    r
            )

        drawCircle(
            color=Color.White,
            radius=7.dp.toPx(),
            center=marker,
            style=Stroke(
                width=2.dp.toPx()
            )
        )

        drawCircle(
            color=
                Color.Black.copy(
                    alpha=.36f
                ),
            radius=9.dp.toPx(),
            center=marker,
            style=Stroke(
                width=1.dp.toPx()
            )
        )
    }
}

private fun wheelPoint(
    x:Float,
    y:Float,
    width:Float,
    height:Float,
    onChange:(Float,Float)->Unit
){
    val cx=width/2f
    val cy=height/2f

    val dx=x-cx
    val dy=y-cy

    val radius=
        min(width,height)/2f

    var angle=
        Math.toDegrees(
            atan2(
                dy.toDouble(),
                dx.toDouble()
            )
        ).toFloat()

    if(angle<0f){
        angle+=360f
    }

    val saturation=
        (
            sqrt(
                dx*dx+
                dy*dy
            )/
            radius
            )
            .coerceIn(
                0f,
                1f
            )

    onChange(
        angle,
        saturation
    )
}

@Composable
private fun PickerButton(
    text:String,
    modifier:Modifier,
    accent:Boolean,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(50)

    Box(
        modifier
            .height(42.dp)
            .clip(shape)
            .background(
                if(accent)
                    p.accent.copy(
                        alpha=.82f
                    )
                else
                    p.accent.copy(
                        alpha=.08f
                    )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(accent)
                            .55f
                        else
                            .24f
                ),
                shape
            )
            .clickable(
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=
                if(accent)
                    Color.White
                else
                    ui.text,
            fontSize=10.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=
                a.fontFamily
        )
    }
}

private fun hsvColor(
    hue:Float,
    saturation:Float,
    value:Float
):Color{
    val argb=
        AndroidColor.HSVToColor(
            floatArrayOf(
                hue.coerceIn(
                    0f,
                    360f
                ),
                saturation.coerceIn(
                    0f,
                    1f
                ),
                value.coerceIn(
                    0f,
                    1f
                )
            )
        )

    return Color(
        red=AndroidColor.red(argb)/255f,
        green=AndroidColor.green(argb)/255f,
        blue=AndroidColor.blue(argb)/255f,
        alpha=AndroidColor.alpha(argb)/255f
    )
}

private fun colorHue(
    color:Color
):Float{
    val hsv=FloatArray(3)

    AndroidColor.RGBToHSV(
        (color.red*255)
            .toInt(),
        (color.green*255)
            .toInt(),
        (color.blue*255)
            .toInt(),
        hsv
    )

    return hsv[0]
}

private fun colorSaturation(
    color:Color
):Float{
    val hsv=FloatArray(3)

    AndroidColor.RGBToHSV(
        (color.red*255)
            .toInt(),
        (color.green*255)
            .toInt(),
        (color.blue*255)
            .toInt(),
        hsv
    )

    return hsv[1]
}

private fun colorValue(
    color:Color
):Float{
    val hsv=FloatArray(3)

    AndroidColor.RGBToHSV(
        (color.red*255)
            .toInt(),
        (color.green*255)
            .toInt(),
        (color.blue*255)
            .toInt(),
        hsv
    )

    return hsv[2]
}

private fun colorHex(
    color:Color
):String{
    val r=
        (color.red*255)
            .toInt()
            .coerceIn(0,255)

    val g=
        (color.green*255)
            .toInt()
            .coerceIn(0,255)

    val b=
        (color.blue*255)
            .toInt()
            .coerceIn(0,255)

    return "#%02X%02X%02X".format(
        r,
        g,
        b
    )
}

private fun parseHex(
    value:String
):Color?{
    val clean=value.trim()

    if(
        !clean.matches(
            Regex("^#[0-9A-Fa-f]{6}$")
        )
    ){
        return null
    }

    return runCatching{
        val argb=
            AndroidColor.parseColor(clean)

        Color(
            (
                AndroidColor.red(argb)/
                255f
            ),
            (
                AndroidColor.green(argb)/
                255f
            ),
            (
                AndroidColor.blue(argb)/
                255f
            ),
            1f
        )
    }.getOrNull()
}
