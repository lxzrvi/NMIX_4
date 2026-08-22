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
import androidx.compose.ui.layout.onSizeChanged
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
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()

    /*
     * Picker edits locally.
     * Nothing is permanently applied until Apply.
     */
    var hue by remember(visible){
        mutableFloatStateOf(
            hueOf(a.palette.accent)
        )
    }

    var saturation by remember(visible){
        mutableFloatStateOf(
            saturationOf(a.palette.accent)
        )
    }

    var brightness by remember(visible){
        mutableFloatStateOf(
            brightnessOf(a.palette.accent)
        )
    }

    var transparency by remember(visible){
        mutableFloatStateOf(
            if(a.usingCustomColor)
                a.customTransparency
            else
                0f
        )
    }

    var hex by remember(visible){
        mutableStateOf(
            toHex(a.palette.accent)
        )
    }

    var editingHex by remember(visible){
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
            hex=toHex(selectedColor)
        }
    }

    AnimatedVisibility(
        visible=visible,
        enter=
            fadeIn(
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
                tween(180)
            )+
            scaleOut(
                targetScale=.97f,
                animationSpec=tween(210)
            )
    ){
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha=
                            if(a.darkMode)
                                .31f
                            else
                                .20f
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
                RoundedCornerShape(23.dp)

            Column(
                Modifier
                    .width(292.dp)
                    .clip(panelShape)
                    .background(
                        if(a.darkMode){
                            Color(0xFF151A18)
                                .copy(alpha=.97f)
                        }else{
                            Color(0xFFE9EDEA)
                                .copy(alpha=.97f)
                        }
                    )
                    .background(
                        a.palette.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .035f
                                else
                                    .025f
                        )
                    )
                    .border(
                        .55.dp,
                        a.palette.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .24f
                                else
                                    .28f
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
                    fontWeight=FontWeight.Bold,
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
                    Modifier.height(14.dp)
                )

                NmixHueWheel(
                    hue=hue,
                    saturation=saturation,
                    brightness=brightness,
                    onChange={
                        newHue,
                        newSaturation->

                        editingHex=false
                        hue=newHue
                        saturation=newSaturation
                    }
                )

                Spacer(
                    Modifier.height(13.dp)
                )

                ThinColorSlider(
                    title="Brightness",
                    value=brightness,
                    valueText=
                        "${(brightness*100f).toInt()}%",
                    colors=
                        listOf(
                            Color.Black,
                            hsvToComposeColor(
                                hue=hue,
                                saturation=saturation,
                                brightness=1f
                            )
                        ),
                    minimum=.12f,
                    maximum=1f,
                    onChange={
                        editingHex=false
                        brightness=it
                    }
                )

                Spacer(
                    Modifier.height(9.dp)
                )

                /*
                 * 0 = opaque
                 * .80 = maximum allowed transparency.
                 */
                ThinColorSlider(
                    title="Transparency",
                    value=transparency,
                    valueText=
                        "${(transparency*100f).toInt()}%",
                    colors=
                        listOf(
                            selectedColor,
                            selectedColor.copy(
                                alpha=.20f
                            )
                        ),
                    minimum=0f,
                    maximum=.80f,
                    onChange={
                        transparency=it
                    },
                    checker=true
                )

                Spacer(
                    Modifier.height(13.dp)
                )

                Row(
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    /*
                     * Checker behind preview makes
                     * transparency visually readable.
                     */
                    Box(
                        Modifier.size(40.dp),
                        contentAlignment=
                            Alignment.Center
                    ){
                        CheckerSurface(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )

                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    selectedColor.copy(
                                        alpha=
                                            (
                                                1f-
                                                    transparency
                                            )
                                                .coerceIn(
                                                    .20f,
                                                    1f
                                                )
                                    )
                                )
                                .border(
                                    1.dp,
                                    ui.text.copy(
                                        alpha=.22f
                                    ),
                                    CircleShape
                                )
                        )
                    }

                    Spacer(
                        Modifier.width(10.dp)
                    )

                    HexField(
                        value=hex,
                        onValueChange={
                            input->

                            editingHex=true

                            var cleaned=
                                input
                                    .uppercase()
                                    .filter{
                                        it=='#' ||
                                        it in '0'..'9' ||
                                        it in 'A'..'F'
                                    }

                            if(
                                cleaned.isNotEmpty() &&
                                !cleaned.startsWith("#")
                            ){
                                cleaned="#$cleaned"
                            }

                            cleaned=
                                cleaned.take(7)

                            hex=cleaned

                            parseHexColor(
                                cleaned
                            )?.let{
                                parsed->

                                hue=hueOf(parsed)
                                saturation=
                                    saturationOf(parsed)
                                brightness=
                                    brightnessOf(parsed)
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
                        Arrangement.spacedBy(7.dp)
                ){
                    PickerActionButton(
                        text="Cancel",
                        style=0,
                        modifier=
                            Modifier.weight(1f),
                        onClick=onClose
                    )

                    PickerActionButton(
                        text="Reset",
                        style=1,
                        modifier=
                            Modifier.weight(1f)
                    ){
                        /*
                         * Return to currently stored
                         * preset and close Custom mode.
                         */
                        a.setTheme(a.theme)
                        onClose()
                    }

                    PickerActionButton(
                        text="Apply",
                        style=2,
                        modifier=
                            Modifier.weight(1f)
                    ){
                        val applied=
                            parseHexColor(hex)
                                ?:selectedColor

                        a.setCustomAppearance(
                            color=applied,
                            transparency=transparency
                        )

                        editingHex=false
                        onClose()
                    }
                }
            }
        }
    }
}

/*
 * ==================================================
 * HUE WHEEL
 * ==================================================
 */

@Composable
private fun NmixHueWheel(
    hue:Float,
    saturation:Float,
    brightness:Float,
    onChange:(Float,Float)->Unit
){
    Canvas(
        Modifier
            .size(184.dp)
            .pointerInput(Unit){
                detectDragGestures(
                    onDragStart={
                        point->

                        updateWheelFromPoint(
                            x=point.x,
                            y=point.y,
                            width=size.width.toFloat(),
                            height=size.height.toFloat(),
                            onChange=onChange
                        )
                    },
                    onDrag={
                        change,
                        _->

                        change.consume()

                        updateWheelFromPoint(
                            x=change.position.x,
                            y=change.position.y,
                            width=size.width.toFloat(),
                            height=size.height.toFloat(),
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
                    (angle+4).toDouble()
                )

            val path=
                Path().apply{
                    moveTo(
                        center.x,
                        center.y
                    )

                    lineTo(
                        center.x+
                            cos(start).toFloat()*
                                radius,
                        center.y+
                            sin(start).toFloat()*
                                radius
                    )

                    lineTo(
                        center.x+
                            cos(finish).toFloat()*
                                radius,
                        center.y+
                            sin(finish).toFloat()*
                                radius
                    )

                    close()
                }

            drawPath(
                path=path,
                color=
                    hsvToComposeColor(
                        hue=angle.toFloat(),
                        saturation=1f,
                        brightness=brightness
                    )
            )

            angle+=3
        }

        drawCircle(
            brush=
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to Color.White,

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
                .coerceIn(0f,1f)*
                radius

        val radians=
            Math.toRadians(
                hue.toDouble()
            )

        val marker=
            Offset(
                center.x+
                    cos(radians).toFloat()*
                        markerRadius,

                center.y+
                    sin(radians).toFloat()*
                        markerRadius
            )

        drawCircle(
            color=
                Color.Black.copy(
                    alpha=.38f
                ),
            radius=10.dp.toPx(),
            center=marker,
            style=Stroke(
                width=2.dp.toPx()
            )
        )

        drawCircle(
            color=Color.White,
            radius=8.dp.toPx(),
            center=marker,
            style=Stroke(
                width=2.dp.toPx()
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
    val cx=width/2f
    val cy=height/2f

    val dx=x-cx
    val dy=y-cy

    val radius=
        min(width,height)/2f

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
            distance/radius
        ).coerceIn(0f,1f)

    onChange(
        angle,
        saturation
    )
}

/*
 * ==================================================
 * THIN SLIDERS
 * ==================================================
 */

@Composable
private fun ThinColorSlider(
    title:String,
    value:Float,
    valueText:String,
    colors:List<Color>,
    minimum:Float,
    maximum:Float,
    onChange:(Float)->Unit,
    checker:Boolean=false
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    var widthPx by remember{
        mutableIntStateOf(1)
    }

    val range=
        (maximum-minimum)
            .coerceAtLeast(.001f)

    val progress=
        (
            (value-minimum)/
                range
        ).coerceIn(0f,1f)

    Column(
        Modifier.fillMaxWidth()
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Text(
                title,
                Modifier.weight(1f),
                color=ui.muted,
                fontSize=8.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Text(
                valueText,
                color=p.accent,
                fontSize=7.5.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=a.fontFamily
            )
        }

        Spacer(
            Modifier.height(5.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .onSizeChanged{
                    widthPx=
                        it.width.coerceAtLeast(1)
                }
                .pointerInput(
                    widthPx,
                    minimum,
                    maximum
                ){
                    detectDragGestures(
                        onDragStart={
                            point->

                            val t=
                                (
                                    point.x/
                                        widthPx.toFloat()
                                ).coerceIn(0f,1f)

                            onChange(
                                minimum+
                                    t*range
                            )
                        },
                        onDrag={
                            change,
                            _->

                            change.consume()

                            val t=
                                (
                                    change.position.x/
                                        widthPx.toFloat()
                                ).coerceIn(0f,1f)

                            onChange(
                                minimum+
                                    t*range
                            )
                        }
                    )
                },
            contentAlignment=
                Alignment.CenterStart
        ){
            if(checker){
                CheckerSurface(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(50)
                        )
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors
                        )
                    )
            )

            /*
             * Flat marker instead of circular thumb.
             */
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(9.dp),
                contentAlignment=
                    Alignment.CenterEnd
            ){
                Box(
                    Modifier
                        .width(2.dp)
                        .height(9.dp)
                        .clip(
                            RoundedCornerShape(50)
                        )
                        .background(
                            if(a.darkMode)
                                Color.White
                            else
                                Color(0xFF202421)
                        )
                )
            }
        }
    }
}

/*
 * ==================================================
 * CHECKER
 * ==================================================
 */

@Composable
private fun CheckerSurface(
    modifier:Modifier
){
    Canvas(modifier){
        val cell=
            6.dp.toPx()

        var row=0
        var y=0f

        while(y<size.height){
            var column=0
            var x=0f

            while(x<size.width){
                drawRect(
                    color=
                        if(
                            (row+column)%2==0
                        ){
                            Color(0xFFD8DDDA)
                        }else{
                            Color(0xFFAEB6B2)
                        },
                    topLeft=
                        Offset(x,y),
                    size=
                        androidx.compose.ui.geometry.Size(
                            cell,
                            cell
                        )
                )

                x+=cell
                column++
            }

            y+=cell
            row++
        }
    }
}

/*
 * ==================================================
 * HEX
 * ==================================================
 */

@Composable
private fun HexField(
    value:String,
    onValueChange:(String)->Unit,
    modifier:Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(12.dp)

    Box(
        modifier
            .height(44.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color.White.copy(
                        alpha=.035f
                    )
                else
                    Color.White.copy(
                        alpha=.46f
                    )
            )
            .background(
                p.accent.copy(
                    alpha=.045f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=.20f
                ),
                shape
            )
            .padding(horizontal=10.dp),
        contentAlignment=
            Alignment.Center
    ){
        BasicTextField(
            value=value,
            onValueChange=onValueChange,
            modifier=
                Modifier.fillMaxWidth(),
            textStyle=
                TextStyle(
                    color=ui.text,
                    fontSize=12.sp,
                    fontWeight=FontWeight.Bold,
                    textAlign=TextAlign.Center,
                    fontFamily=a.fontFamily
                ),
            singleLine=true
        )
    }
}

/*
 * ==================================================
 * ACTIONS
 * ==================================================
 */

@Composable
private fun PickerActionButton(
    text:String,
    style:Int,
    modifier:Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(11.dp)

    val bg=
        when(style){
            2->
                p.accent.copy(
                    alpha=.82f
                )

            1->
                Color(0xFFD55C5C)
                    .copy(
                        alpha=
                            if(a.darkMode)
                                .12f
                            else
                                .10f
                    )

            else->
                if(a.darkMode)
                    Color.White.copy(
                        alpha=.045f
                    )
                else
                    Color.White.copy(
                        alpha=.48f
                    )
        }

    val fg=
        when(style){
            2->Color.White
            1->Color(0xFFD55C5C)
            else->ui.text
        }

    Box(
        modifier
            .height(40.dp)
            .clip(shape)
            .background(bg)
            .border(
                .45.dp,
                when(style){
                    2->
                        p.accent.copy(
                            alpha=.52f
                        )

                    1->
                        Color(0xFFD55C5C)
                            .copy(alpha=.26f)

                    else->
                        p.accent.copy(
                            alpha=.16f
                        )
                },
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
            color=fg,
            fontSize=8.5.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * COLOR MATH
 * ==================================================
 */

private fun hsvToComposeColor(
    hue:Float,
    saturation:Float,
    brightness:Float
):Color{
    val h=
        hue.coerceIn(0f,360f)

    val s=
        saturation.coerceIn(0f,1f)

    val v=
        brightness.coerceIn(0f,1f)

    val c=v*s
    val sector=h/60f

    val x=
        c*
            (
                1f-
                    abs(
                        (sector%2f)-1f
                    )
            )

    val values=
        when{
            sector<1f->
                Triple(c,x,0f)

            sector<2f->
                Triple(x,c,0f)

            sector<3f->
                Triple(0f,c,x)

            sector<4f->
                Triple(0f,x,c)

            sector<5f->
                Triple(x,0f,c)

            else->
                Triple(c,0f,x)
        }

    val m=v-c

    return Color(
        red=
            (values.first+m)
                .coerceIn(0f,1f),

        green=
            (values.second+m)
                .coerceIn(0f,1f),

        blue=
            (values.third+m)
                .coerceIn(0f,1f),

        alpha=1f
    )
}

private fun rgbToHsv(
    color:Color
):FloatArray{
    val r=
        color.red.coerceIn(0f,1f)

    val g=
        color.green.coerceIn(0f,1f)

    val b=
        color.blue.coerceIn(0f,1f)

    val maximum=
        maxOf(r,g,b)

    val minimum=
        minOf(r,g,b)

    val delta=
        maximum-minimum

    var hue=
        when{
            delta==0f->
                0f

            maximum==r->
                60f*
                    (
                        ((g-b)/delta)%
                            6f
                    )

            maximum==g->
                60f*
                    (
                        (b-r)/delta+
                            2f
                    )

            else->
                60f*
                    (
                        (r-g)/delta+
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
            delta/maximum

    return floatArrayOf(
        hue,
        saturation,
        maximum
    )
}

private fun hueOf(
    color:Color
):Float=
    rgbToHsv(color)[0]

private fun saturationOf(
    color:Color
):Float=
    rgbToHsv(color)[1]

private fun brightnessOf(
    color:Color
):Float=
    rgbToHsv(color)[2]

private fun toHex(
    color:Color
):String{
    val r=
        (
            color.red*255f
        )
            .toInt()
            .coerceIn(0,255)

    val g=
        (
            color.green*255f
        )
            .toInt()
            .coerceIn(0,255)

    val b=
        (
            color.blue*255f
        )
            .toInt()
            .coerceIn(0,255)

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
            ).toFloat()/255f

        val g=
            (
                (
                    number shr 8
                ) and
                    0xFF
            ).toFloat()/255f

        val b=
            (
                number and
                    0xFF
            ).toFloat()/255f

        Color(
            red=r,
            green=g,
            blue=b,
            alpha=1f
        )
    }.getOrNull()
}
