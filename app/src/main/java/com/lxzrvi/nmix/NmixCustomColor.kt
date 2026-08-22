package com.lxzrvi.nmix

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
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

    val ui=
        a.uiColors()

    var hue by remember(visible){
        mutableFloatStateOf(
            hueOf(
                a.palette.accent
            )
        )
    }

    var saturation by remember(visible){
        mutableFloatStateOf(
            saturationOf(
                a.palette.accent
            )
        )
    }

    var brightness by remember(visible){
        mutableFloatStateOf(
            brightnessOf(
                a.palette.accent
            )
        )
    }

    var hex by remember(visible){
        mutableStateOf(
            toHex(
                a.palette.accent
            )
        )
    }

    var editingHex by remember{
        mutableStateOf(false)
    }

    val selectedColor=
        hsvToComposeColor(
            hue=hue,
            saturation=saturation,
            brightness=brightness
        )

    LaunchedEffect(
        hue,
        saturation,
        brightness,
        editingHex
    ){
        if(!editingHex){
            hex=
                toHex(
                    selectedColor
                )
        }
    }

    AnimatedVisibility(
        visible=visible,
        enter=
            fadeIn(
                animationSpec=
                    tween(230)
            )+
            scaleIn(
                initialScale=.96f,
                animationSpec=tween(
                    290,
                    easing=EaseOutCubic
                )
            ),
        exit=
            fadeOut(
                animationSpec=
                    tween(180)
            )+
            scaleOut(
                targetScale=.97f,
                animationSpec=
                    tween(210)
            )
    ){
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha=.24f
                    )
                )
                .clickable(
                    interactionSource=remember{
                        MutableInteractionSource()
                    },
                    indication=null,
                    onClick=onClose
                ),
            contentAlignment=
                Alignment.Center
        ){
            val panelShape=
                RoundedCornerShape(
                    24.dp
                )

            Column(
                Modifier
                    .width(292.dp)
                    .clip(panelShape)
                    .background(
                        if(a.darkMode){
                            Color(0xFF151A18)
                        }else{
                            Color(0xFFF2F5F3)
                        }
                    )
                    .border(
                        .7.dp,
                        a.palette.accent.copy(
                            alpha=.42f
                        ),
                        panelShape
                    )
                    .clickable(
                        interactionSource=remember{
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
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Text(
                    "Choose any NMIX accent",
                    color=ui.muted,
                    fontSize=8.sp,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(15.dp)
                )

                NmixHueWheel(
                    hue=hue,
                    saturation=saturation,
                    brightness=brightness,
                    onChange={
                        newHue:Float,
                        newSaturation:Float->

                        editingHex=false
                        hue=newHue
                        saturation=
                            newSaturation
                    }
                )

                Spacer(
                    Modifier.height(14.dp)
                )

                BrightnessControl(
                    brightness=brightness,
                    hue=hue,
                    saturation=saturation,
                    onChange={
                        newValue:Float->

                        editingHex=false
                        brightness=
                            newValue
                    }
                )

                Spacer(
                    Modifier.height(14.dp)
                )

                Row(
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                selectedColor
                            )
                            .border(
                                1.5.dp,
                                ui.text.copy(
                                    alpha=.30f
                                ),
                                CircleShape
                            )
                    )

                    Spacer(
                        Modifier.width(10.dp)
                    )

                    HexField(
                        value=hex,
                        onValueChange={
                            input:String->

                            editingHex=true

                            var cleaned=
                                input
                                    .uppercase()
                                    .filter{
                                        char:Char->

                                        char=='#' ||
                                        char in '0'..'9' ||
                                        char in 'A'..'F'
                                    }

                            if(
                                cleaned.isNotEmpty() &&
                                !cleaned.startsWith(
                                    "#"
                                )
                            ){
                                cleaned=
                                    "#$cleaned"
                            }

                            cleaned=
                                cleaned.take(7)

                            hex=cleaned

                            parseHexColor(
                                cleaned
                            )?.let{
                                parsed:Color->

                                hue=
                                    hueOf(
                                        parsed
                                    )

                                saturation=
                                    saturationOf(
                                        parsed
                                    )

                                brightness=
                                    brightnessOf(
                                        parsed
                                    )
                            }
                        },
                        modifier=
                            Modifier.weight(1f)
                    )
                }

                Spacer(
                    Modifier.height(15.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            9.dp
                        )
                ){
                    CustomPickerButton(
                        text="Cancel",
                        accent=false,
                        modifier=
                            Modifier.weight(1f),
                        onClick=onClose
                    )

                    CustomPickerButton(
                        text="Apply",
                        accent=true,
                        modifier=
                            Modifier.weight(1f)
                    ){
                        val applied=
                            parseHexColor(
                                hex
                            )
                                ?:selectedColor

                        a.setCustomColor(
                            applied
                        )

                        editingHex=false
                        onClose()
                    }
                }
            }
        }
    }
}

@Composable
private fun NmixHueWheel(
    hue:Float,
    saturation:Float,
    brightness:Float,
    onChange:(Float,Float)->Unit
){
    Canvas(
        Modifier
            .size(188.dp)
            .pointerInput(Unit){
                detectDragGestures(
                    onDragStart={
                        point:Offset->

                        updateWheelFromPoint(
                            x=point.x,
                            y=point.y,
                            width=
                                size.width
                                    .toFloat(),
                            height=
                                size.height
                                    .toFloat(),
                            onChange=onChange
                        )
                    },
                    onDrag={
                        change,
                        _->

                        change.consume()

                        updateWheelFromPoint(
                            x=
                                change.position.x,
                            y=
                                change.position.y,
                            width=
                                size.width
                                    .toFloat(),
                            height=
                                size.height
                                    .toFloat(),
                            onChange=onChange
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

        var angle=0

        while(angle<360){
            val start=
                Math.toRadians(
                    angle.toDouble()
                )

            val finish=
                Math.toRadians(
                    (
                        angle+4
                    ).toDouble()
                )

            val path=
                Path().apply{
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
                            cos(finish)
                                .toFloat()*
                            radius,
                        center.y+
                            sin(finish)
                                .toFloat()*
                            radius
                    )

                    close()
                }

            drawPath(
                path=path,
                color=
                    hsvToComposeColor(
                        hue=
                            angle.toFloat(),
                        saturation=1f,
                        brightness=
                            brightness
                    )
            )

            angle+=3
        }

        drawCircle(
            brush=
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to
                            Color.White,

                        .12f to
                            Color.White.copy(
                                alpha=.94f
                            ),

                        .48f to
                            Color.White.copy(
                                alpha=.50f
                            ),

                        1f to
                            Color.Transparent
                    ),
                    center=center,
                    radius=radius
                ),
            radius=radius,
            center=center
        )

        val markerRadius=
            saturation
                .coerceIn(
                    0f,
                    1f
                )*
                radius

        val radians=
            Math.toRadians(
                hue.toDouble()
            )

        val marker=
            Offset(
                center.x+
                    cos(radians)
                        .toFloat()*
                    markerRadius,

                center.y+
                    sin(radians)
                        .toFloat()*
                    markerRadius
            )

        drawCircle(
            color=
                Color.Black.copy(
                    alpha=.38f
                ),
            radius=
                10.dp.toPx(),
            center=marker,
            style=Stroke(
                width=
                    2.dp.toPx()
            )
        )

        drawCircle(
            color=Color.White,
            radius=
                8.dp.toPx(),
            center=marker,
            style=Stroke(
                width=
                    2.dp.toPx()
            )
        )
    }
}

private fun updateWheelFromPoint(
    x:Float,
    y:Float,
    width:Float,
    height:Float,
    onChange:(Float,Float)->Unit
){
    val cx=
        width/2f

    val cy=
        height/2f

    val dx=
        x-cx

    val dy=
        y-cy

    val radius=
        min(
            width,
            height
        )/2f

    if(radius<=0f){
        return
    }

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

    val distance=
        sqrt(
            dx*dx+
            dy*dy
        )

    val saturation=
        (
            distance/
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
private fun BrightnessControl(
    brightness:Float,
    hue:Float,
    saturation:Float,
    onChange:(Float)->Unit
){
    var widthPx by remember{
        mutableIntStateOf(1)
    }

    val color=
        hsvToComposeColor(
            hue=hue,
            saturation=saturation,
            brightness=1f
        )

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(24.dp)
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
                                    .12f,
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
                                    .12f,
                                    1f
                                )
                        )
                    }
                )
            }
    ){
        widthPx=
            size.width
                .toInt()
                .coerceAtLeast(1)

        drawRoundRect(
            brush=
                Brush.horizontalGradient(
                    listOf(
                        Color.Black,
                        color
                    )
                ),
            cornerRadius=
                androidx.compose.ui.geometry
                    .CornerRadius(
                        12.dp.toPx()
                    )
        )

        val markerX=
            size.width*
                brightness
                    .coerceIn(
                        .12f,
                        1f
                    )

        val marker=
            Offset(
                markerX,
                size.height/2f
            )

        drawCircle(
            color=Color.White,
            radius=
                7.dp.toPx(),
            center=marker
        )

        drawCircle(
            color=
                Color.Black.copy(
                    alpha=.35f
                ),
            radius=
                9.dp.toPx(),
            center=marker,
            style=Stroke(
                width=
                    1.dp.toPx()
            )
        )
    }
}

@Composable
private fun HexField(
    value:String,
    onValueChange:(String)->Unit,
    modifier:Modifier
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(
            12.dp
        )

    Box(
        modifier
            .height(44.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=.07f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=.24f
                ),
                shape
            )
            .padding(
                horizontal=10.dp
            ),
        contentAlignment=
            Alignment.Center
    ){
        BasicTextField(
            value=value,
            onValueChange=
                onValueChange,
            modifier=
                Modifier.fillMaxWidth(),
            textStyle=
                TextStyle(
                    color=ui.text,
                    fontSize=12.sp,
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
}

@Composable
private fun CustomPickerButton(
    text:String,
    accent:Boolean,
    modifier:Modifier,
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
                if(accent){
                    p.accent.copy(
                        alpha=.82f
                    )
                }else{
                    p.accent.copy(
                        alpha=.08f
                    )
                }
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
                interactionSource=remember{
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
            fontFamily=a.fontFamily
        )
    }
}

private fun hsvToComposeColor(
    hue:Float,
    saturation:Float,
    brightness:Float
):Color{
    val h=
        hue.coerceIn(
            0f,
            360f
        )

    val s=
        saturation.coerceIn(
            0f,
            1f
        )

    val v=
        brightness.coerceIn(
            0f,
            1f
        )

    val c=
        v*s

    val sector=
        h/60f

    val x=
        c*
            (
                1f-
                abs(
                    (
                        sector%2f
                    )-
                    1f
                )
            )

    val values=
        when{
            sector<1f->
                Triple(
                    c,
                    x,
                    0f
                )

            sector<2f->
                Triple(
                    x,
                    c,
                    0f
                )

            sector<3f->
                Triple(
                    0f,
                    c,
                    x
                )

            sector<4f->
                Triple(
                    0f,
                    x,
                    c
                )

            sector<5f->
                Triple(
                    x,
                    0f,
                    c
                )

            else->
                Triple(
                    c,
                    0f,
                    x
                )
        }

    val m=
        v-c

    return Color(
        red=
            (
                values.first+m
            ).coerceIn(
                0f,
                1f
            ),

        green=
            (
                values.second+m
            ).coerceIn(
                0f,
                1f
            ),

        blue=
            (
                values.third+m
            ).coerceIn(
                0f,
                1f
            ),

        alpha=1f
    )
}

private fun rgbToHsv(
    color:Color
):FloatArray{
    val r=
        color.red.coerceIn(
            0f,
            1f
        )

    val g=
        color.green.coerceIn(
            0f,
            1f
        )

    val b=
        color.blue.coerceIn(
            0f,
            1f
        )

    val maximum=
        maxOf(
            r,
            g,
            b
        )

    val minimum=
        minOf(
            r,
            g,
            b
        )

    val delta=
        maximum-
            minimum

    var hue=
        when{
            delta==0f->
                0f

            maximum==r->
                60f*
                    (
                        (
                            (
                                g-b
                            )/
                            delta
                        )%
                        6f
                    )

            maximum==g->
                60f*
                    (
                        (
                            b-r
                        )/
                        delta+
                        2f
                    )

            else->
                60f*
                    (
                        (
                            r-g
                        )/
                        delta+
                        4f
                    )
        }

    if(hue<0f){
        hue+=360f
    }

    val saturation=
        if(maximum==0f)
            0f
        else
            delta/
                maximum

    return floatArrayOf(
        hue,
        saturation,
        maximum
    )
}

private fun hueOf(
    color:Color
):Float=
    rgbToHsv(
        color
    )[0]

private fun saturationOf(
    color:Color
):Float=
    rgbToHsv(
        color
    )[1]

private fun brightnessOf(
    color:Color
):Float=
    rgbToHsv(
        color
    )[2]

private fun toHex(
    color:Color
):String{
    val r=
        (
            color.red*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    val g=
        (
            color.green*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    val b=
        (
            color.blue*
            255f
        )
            .toInt()
            .coerceIn(
                0,
                255
            )

    return String.format(
        "#%02X%02X%02X",
        r,
        g,
        b
    )
}

private fun parseHexColor(
    input:String
):Color?{
    val clean=
        input.trim()

    if(
        !clean.matches(
            Regex(
                "^#[0-9A-Fa-f]{6}$"
            )
        )
    ){
        return null
    }

    return runCatching{
        val number=
            clean
                .substring(1)
                .toLong(16)

        val r=
            (
                (
                    number shr 16
                ) and
                0xFF
            ).toFloat()/
                255f

        val g=
            (
                (
                    number shr 8
                ) and
                0xFF
            ).toFloat()/
                255f

        val b=
            (
                number and
                0xFF
            ).toFloat()/
                255f

        Color(
            red=r,
            green=g,
            blue=b,
            alpha=1f
        )
    }.getOrNull()
}
