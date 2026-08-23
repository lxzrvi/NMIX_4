package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun nmixScreenColor():Color{
    val a=LocalNmixAppearance.current
    return if(a.darkMode) Color(0xFF111614) else Color(0xFFF8F9F8)
}

@Composable
private fun nmixScreenBorder():Color{
    val a=LocalNmixAppearance.current
    return a.palette.accent.copy(alpha=if(a.darkMode).15f else .23f)
}

@Composable
private fun nmixDisplayText():Color{
    val a=LocalNmixAppearance.current
    return if(a.darkMode) Color.White.copy(alpha=.93f) else Color(0xFF202522)
}

private fun nmixMix(start:Float,end:Float,progress:Float):Float{
    val t=progress.coerceIn(0f,1f)
    return start+(end-start)*t
}

/* ==================================================
 * TOOL SECTION
 * ================================================== */

@Composable
fun NmixToolSection(
    icon:NmixIcon,
    title:String,
    subtitle:String,
    open:Boolean,
    onClick:()->Unit,
    content:@Composable ()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val progress by animateFloatAsState(
        targetValue=if(open)1f else 0f,
        animationSpec=tween(360,easing=EaseInOutCubic),
        label="toolOpen"
    )

    /*
     * Closed stays close to old radius.
     * Open radius grows more so it visually matches
     * the transformed icon without becoming a pill.
     */
    val sectionShape=RoundedCornerShape(
        nmixMix(17f,27f,progress).dp
    )

    Column(
        Modifier
            .padding(horizontal=12.dp)
            .clip(sectionShape)
            .background(
                if(a.darkMode)
                    Color(0xFF121715).copy(alpha=.94f)
                else
                    Color.White.copy(alpha=.92f)
            )
            .background(
                p.accent.copy(alpha=if(a.darkMode).04f else .022f)
            )
            .border(
                nmixMix(.45f,1f,progress).dp,
                p.accent.copy(
                    alpha=nmixMix(
                        if(a.darkMode).14f else .22f,
                        if(a.darkMode).46f else .52f,
                        progress
                    )
                ),
                sectionShape
            )
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource=remember{MutableInteractionSource()},
                    indication=null,
                    onClick=onClick
                )
                .padding(13.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(
                Modifier.size(42.dp),
                contentAlignment=Alignment.Center
            ){
                val iconShape=RoundedCornerShape(
                    nmixMix(9f,20f,progress).dp
                )

                Box(
                    Modifier
                        .size(nmixMix(42f,39f,progress).dp)
                        .rotate(180f*progress)
                        .clip(iconShape)
                        .background(p.accent.copy(alpha=.68f))
                        .border(
                            .65.dp,
                            p.accentLight.copy(alpha=.54f),
                            iconShape
                        )
                )

                Canvas(
                    Modifier
                        .size(nmixMix(36f,34f,progress).dp)
                        .rotate(-180f*progress)
                ){
                    val sw=.62.dp.toPx()

                    drawRoundRect(
                        color=Color.White.copy(alpha=.48f),
                        topLeft=Offset(sw,sw),
                        size=Size(size.width-sw*2,size.height-sw*2),
                        cornerRadius=CornerRadius(
                            nmixMix(7f,16f,progress).dp.toPx()
                        ),
                        style=Stroke(sw)
                    )
                }

                NmixIcon(
                    icon,
                    Modifier.size(19.dp),
                    Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)){
                Text(
                    title,
                    color=ui.text,
                    fontSize=14.sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily
                )

                Text(
                    subtitle,
                    color=ui.muted,
                    fontSize=9.sp,
                    fontFamily=a.fontFamily
                )
            }

            NmixIcon(
                NmixIcon.CHEVRON_DOWN,
                Modifier
                    .size(18.dp)
                    .rotate(180f*progress),
                ui.muted
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=expandVertically(
                animationSpec=tween(430,easing=EaseOutCubic),
                expandFrom=Alignment.Top
            )+fadeIn(tween(330)),
            exit=shrinkVertically(
                animationSpec=tween(330,easing=EaseInOutCubic),
                shrinkTowards=Alignment.Top
            )+fadeOut(tween(220))
        ){
            content()
        }
    }
}

/* ==================================================
 * COMMON CONTROLS
 * ================================================== */

@Composable
fun NmixOption(
    icon:NmixIcon,
    title:String,
    selected:Boolean=false,
    modifier:Modifier=Modifier,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).965f else 1f,
        spring(dampingRatio=.72f,stiffness=620f),
        label="optionPress"
    )

    val selection by animateFloatAsState(
        if(selected)1f else 0f,
        tween(220),
        label="optionSelected"
    )

    val shape=RoundedCornerShape(13.dp)

    Row(
        modifier
            .scale(scale)
            .height(58.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917).copy(alpha=.91f)
                else
                    Color.White.copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=if(a.darkMode)
                        .035f+selection*.05f
                    else
                        .02f+selection*.035f
                )
            )
            .border(
                nmixMix(.45f,1f,selection).dp,
                p.accent.copy(
                    alpha=if(a.darkMode)
                        .14f+selection*.32f
                    else
                        .22f+selection*.30f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
            .padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Box(
            Modifier
                .size(35.dp)
                .clip(if(selected) CircleShape else RoundedCornerShape(9.dp))
                .background(p.accent.copy(alpha=if(selected).20f else .12f)),
            contentAlignment=Alignment.Center
        ){
            NmixIcon(icon,Modifier.size(18.dp),p.accent)
        }

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            color=ui.text,
            fontSize=13.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
fun NmixCircleButton(
    icon:NmixIcon,
    modifier:Modifier=Modifier,
    color:Color?=null,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current

    NmixPressBox(
        modifier=modifier,
        shape=CircleShape,
        color=color ?: a.palette.accent.copy(alpha=.80f),
        onClick=onClick
    ){
        NmixIcon(icon,Modifier.size(21.dp),Color.White)
    }
}

@Composable
fun NmixSmallIconButton(
    icon:NmixIcon,
    modifier:Modifier=Modifier,
    selected:Boolean=false,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(9.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18).copy(alpha=.91f)
                else
                    Color.White.copy(alpha=.92f)
            )
            .background(p.accent.copy(alpha=if(selected).09f else .025f))
            .border(
                if(selected)1.dp else .45.dp,
                p.accent.copy(
                    alpha=if(selected).50f
                    else if(a.darkMode).14f
                    else .22f
                ),
                shape
            )
    ){
        NmixPressBox(
            Modifier.fillMaxSize(),
            shape,
            Color.Transparent,
            onClick
        ){
            NmixIcon(icon,Modifier.size(19.dp),p.accent)
        }
    }
}

@Composable
fun NmixTextButton(
    text:String,
    modifier:Modifier=Modifier,
    accent:Boolean=false,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)

    Box(
        modifier
            .clip(shape)
            .background(
                when{
                    accent->p.accent.copy(alpha=.80f)
                    a.darkMode->Color(0xFF141917).copy(alpha=.92f)
                    else->Color.White.copy(alpha=.92f)
                }
            )
            .background(
                if(accent) Color.Transparent
                else p.accent.copy(alpha=if(a.darkMode).035f else .02f)
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=if(accent).42f
                    else if(a.darkMode).15f
                    else .25f
                ),
                shape
            )
    ){
        NmixPressBox(
            Modifier.fillMaxSize(),
            shape,
            Color.Transparent,
            onClick
        ){
            Text(
                text,
                color=if(accent) Color.White else a.uiColors().text,
                fontSize=12.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
fun NmixKey(
    text:String,
    modifier:Modifier=Modifier,
    type:Int=0,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val bg=when(type){
        1->p.accent.copy(alpha=.86f)
        2->Color(0xFFD83939).copy(alpha=if(a.darkMode).19f else .14f)
        else->if(a.darkMode)
            p.accent.copy(alpha=.10f)
        else
            Color.White.copy(alpha=.92f)
    }

    val fg=when(type){
        1->Color.White
        2->Color(0xFFE15A5A)
        else->a.uiColors().text
    }

    Box(
        modifier
            .clip(CircleShape)
            .background(bg)
            .border(
                .45.dp,
                when(type){
                    1->p.accentLight.copy(alpha=.28f)
                    2->Color(0xFFE15A5A).copy(alpha=.20f)
                    else->p.accent.copy(alpha=if(a.darkMode).14f else .21f)
                },
                CircleShape
            )
    ){
        NmixPressBox(
            Modifier.fillMaxSize(),
            CircleShape,
            Color.Transparent,
            onClick
        ){
            Text(
                text,
                color=fg,
                fontSize=15.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

/* ==================================================
 * DISPLAY
 * ================================================== */

@Composable
fun NmixDisplay(
    label:String,
    value:String,
    status:String,
    timer:Boolean,
    calcVisible:Boolean,
    calcFirst:String,
    calcOperator:String,
    calcSecond:String,
    onMinus:()->Unit,
    onPlus:()->Unit,
    onClick:()->Unit,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val density=LocalDensity.current
    val interaction=remember{MutableInteractionSource()}

    var heightPx by remember{mutableIntStateOf(0)}

    val heightDp=with(density){heightPx.toDp().value}
    val maxH=305f
    val minH=82f

    val percent=if(heightPx<=0) 1f else
        ((heightDp-minH)/(maxH-minH)).coerceIn(0f,1f)

    /*
     * Slightly wider transition bands make field
     * movement feel less abrupt while still keeping
     * the long live vertical middle range.
     */
    val fullToHalf=((.76f-percent)/.18f).coerceIn(0f,1f)
    val halfShrink=((.58f-percent)/.34f).coerceIn(0f,1f)
    val halfToSmall=((.27f-percent)/.17f).coerceIn(0f,1f)
    val smallShrink=((.10f-percent)/.10f).coerceIn(0f,1f)
    val radiusProgress=((.24f-percent)/.24f).coerceIn(0f,1f)

    val displayRadius=nmixMix(19f,54f,radiusProgress).dp
    val displayShape=RoundedCornerShape(displayRadius)

    val calcAlpha by animateFloatAsState(
        if(calcVisible)1f else 0f,
        tween(420,easing=EaseInOutCubic),
        label="calcVisibility"
    )

    val world=rememberNmixWorldMotion("mainDisplayWorld")

    Box(
        modifier
            .onSizeChanged{heightPx=it.height}
            .clip(displayShape)
            .background(nmixScreenColor())
            .border(.55.dp,nmixScreenBorder(),displayShape)
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
    ){
        if(a.animationEnabled){
            DisplayWorldLayer(world)
        }

        if(calcVisible){
            NmixCalculatorMorphFields(
                first=calcFirst.ifEmpty{"_"},
                operator=calcOperator.ifEmpty{"sign"},
                second=calcSecond.ifEmpty{"_"},
                fullToHalf=fullToHalf,
                halfShrink=halfShrink,
                halfToSmall=halfToSmall,
                smallShrink=smallShrink,
                radiusProgress=radiusProgress,
                modifier=Modifier
                    .fillMaxSize()
                    .graphicsLayer{alpha=calcAlpha}
            )
        }

        val contentShift=when{
            !calcVisible->0f
            halfToSmall>0f->1f
            else->fullToHalf
        }

        if(percent>.58f){
            Text(
                label,
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top=if(calcVisible)71.dp else 17.dp)
                    .graphicsLayer{
                        translationX=44f*contentShift
                        translationY=6f*contentShift
                        alpha=1f-(fullToHalf*.07f)
                    },
                color=if(a.darkMode) p.accentLight else p.accentDark,
                fontSize=nmixMix(9f,8.3f,contentShift).sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=2.sp,
                fontFamily=a.fontFamily,
                maxLines=1
            )

            Text(
                value,
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=57f*contentShift
                        translationY=if(calcVisible)
                            nmixMix(18f,7f,contentShift)
                        else 0f
                    }
                    .padding(horizontal=if(timer)70.dp else 16.dp),
                color=nmixDisplayText(),
                fontSize=nmixMix(40f,35f,contentShift).sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily,
                maxLines=1
            )

            Text(
                status,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal=18.dp,vertical=15.dp)
                    .graphicsLayer{
                        translationX=42f*contentShift
                        translationY=5f*contentShift
                    },
                color=if(a.darkMode)
                    p.accentLight.copy(alpha=.78f)
                else
                    p.accentDark.copy(alpha=.80f),
                fontSize=nmixMix(11f,9.5f,contentShift).sp,
                lineHeight=15.sp,
                fontWeight=FontWeight.Medium,
                fontFamily=a.fontFamily,
                textAlign=TextAlign.Center,
                maxLines=2
            )
        }else{
            val fieldFraction=if(calcVisible)
                nmixMix(.30f,.48f,halfToSmall)
            else 0f

            val contentWidth=if(calcVisible)
                (1f-fieldFraction).coerceIn(.49f,.70f)
            else .84f

            val detailsAlpha=
                (1f-halfToSmall*.82f-smallShrink*.18f).coerceIn(0f,1f)

            Column(
                Modifier
                    .fillMaxWidth(contentWidth)
                    .align(
                        if(calcVisible)
                            Alignment.CenterEnd
                        else
                            Alignment.Center
                    )
                    .padding(
                        start=if(calcVisible)8.dp else 5.dp,
                        end=10.dp
                    )
                    .graphicsLayer{
                        translationX=2f*halfShrink
                    },
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center
            ){
                if(detailsAlpha>.05f){
                    Text(
                        label,
                        Modifier.graphicsLayer{alpha=detailsAlpha},
                        color=if(a.darkMode) p.accentLight else p.accentDark,
                        fontSize=(
                            8f-halfShrink*.8f-halfToSmall*.45f
                        ).coerceAtLeast(6.6f).sp,
                        fontWeight=FontWeight.Bold,
                        letterSpacing=1.2.sp,
                        fontFamily=a.fontFamily,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.height(
                            nmixMix(3f,1f,halfShrink).dp
                        )
                    )
                }

                Text(
                    value,
                    color=nmixDisplayText(),
                    fontSize=(
                        34f-
                            halfShrink*6f-
                            halfToSmall*4f-
                            smallShrink*2f
                    ).coerceAtLeast(21f).sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily,
                    maxLines=1,
                    textAlign=TextAlign.Center
                )

                if(detailsAlpha>.15f){
                    Spacer(
                        Modifier.height(
                            nmixMix(3f,1f,halfShrink).dp
                        )
                    )

                    Text(
                        status,
                        Modifier.graphicsLayer{alpha=detailsAlpha},
                        color=if(a.darkMode)
                            p.accentLight.copy(alpha=.76f)
                        else
                            p.accentDark.copy(alpha=.78f),
                        fontSize=(
                            8.5f-halfShrink*.9f-halfToSmall*.4f
                        ).coerceAtLeast(6.7f).sp,
                        lineHeight=10.sp,
                        fontWeight=FontWeight.Medium,
                        fontFamily=a.fontFamily,
                        textAlign=TextAlign.Center,
                        maxLines=2
                    )
                }
            }
        }

        val timerSize=(
            47f-
                halfShrink*8f-
                halfToSmall*5f-
                smallShrink*3f
        ).coerceAtLeast(31f).dp

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterStart)
                .padding(start=if(percent<.18f)8.dp else 13.dp),
            enter=fadeIn(tween(250))+scaleIn(),
            exit=fadeOut(tween(190))+scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.MINUS,
                Modifier.size(timerSize),
                onClick=onMinus
            )
        }

        AnimatedVisibility(
            visible=timer,
            modifier=Modifier
                .align(Alignment.CenterEnd)
                .padding(end=if(percent<.18f)8.dp else 13.dp),
            enter=fadeIn(tween(250))+scaleIn(),
            exit=fadeOut(tween(190))+scaleOut()
        ){
            NmixCircleButton(
                NmixIcon.PLUS,
                Modifier.size(timerSize),
                onClick=onPlus
            )
        }
    }
}

/* ==================================================
 * GIANT IMAGINARY-WORLD VIEWPORT
 * ================================================== */

@Composable
private fun BoxScope.DisplayWorldLayer(
    world:NmixWorldMotion
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{index,body->
        /*
         * Deliberately enormous world/camera scale.
         * Screen edge is never a collision boundary.
         */
        val worldX=body.x*(560f+index*48f)
        val worldY=body.y*(410f+index*38f)

        if(soft){
            val elementSize=when(index){
                0->820.dp
                1->720.dp
                2->630.dp
                3->680.dp
                else->560.dp
            }

            Box(
                Modifier
                    .size(elementSize)
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=worldX
                        translationY=worldY
                        scaleX=body.pulse
                        scaleY=body.pulse
                        rotationZ=body.rotation*.08f
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    (if(index%2==0) p.accent else p.accentLight)
                                        .copy(alpha=if(a.darkMode).39f else .32f),
                                .20f to p.accent.copy(
                                    alpha=if(a.darkMode).28f else .22f
                                ),
                                .45f to p.accent.copy(alpha=.135f),
                                .65f to p.accent.copy(alpha=.062f),
                                .82f to p.accent.copy(alpha=.020f),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val elementSize=when(index){
                0->410.dp
                1->360.dp
                2->315.dp
                3->340.dp
                else->285.dp
            }

            Canvas(
                Modifier
                    .size(elementSize)
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=worldX
                        translationY=worldY
                        scaleX=body.pulse
                        scaleY=body.pulse
                        rotationZ=body.rotation
                    }
            ){
                val color=if(index%2==0) p.accent else p.accentLight
                val inset=13.dp.toPx()

                drawRoundRect(
                    color=color.copy(alpha=.035f),
                    cornerRadius=CornerRadius(48.dp.toPx())
                )

                drawRoundRect(
                    color=color.copy(alpha=if(a.darkMode).19f else .145f),
                    topLeft=Offset(inset,inset),
                    size=Size(
                        (size.width-inset*2).coerceAtLeast(0f),
                        (size.height-inset*2).coerceAtLeast(0f)
                    ),
                    cornerRadius=CornerRadius(38.dp.toPx())
                )

                drawRoundRect(
                    color=color.copy(alpha=if(a.darkMode).18f else .13f),
                    topLeft=Offset(inset,inset),
                    size=Size(
                        (size.width-inset*2).coerceAtLeast(0f),
                        (size.height-inset*2).coerceAtLeast(0f)
                    ),
                    cornerRadius=CornerRadius(38.dp.toPx()),
                    style=Stroke(1.7.dp.toPx())
                )
            }
        }
    }
}

/* ==================================================
 * CALCULATOR THREE-FIELD MORPH
 * ================================================== */

@Composable
private fun NmixCalculatorMorphFields(
    first:String,
    operator:String,
    second:String,
    fullToHalf:Float,
    halfShrink:Float,
    halfToSmall:Float,
    smallShrink:Float,
    radiusProgress:Float,
    modifier:Modifier=Modifier
){
    val density=LocalDensity.current

    fun dataScale(text:String)=when{
        text.length>=15->.55f
        text.length>=12->.63f
        text.length>=9->.71f
        text.length>=7->.79f
        text.length>=5->.88f
        else->1f
    }

    val baseText=(
        14f-
            halfShrink*3f-
            halfToSmall*1.3f-
            smallShrink*1.2f
    ).coerceAtLeast(7.8f)

    Layout(
        modifier=modifier,
        content={
            MorphFieldVisual(
                first,
                (baseText*dataScale(first)).coerceAtLeast(6.8f).sp,
                0,
                fullToHalf,
                halfToSmall,
                radiusProgress
            )

            MorphFieldVisual(
                operator,
                (baseText*.78f).coerceAtLeast(6.3f).sp,
                1,
                fullToHalf,
                halfToSmall,
                radiusProgress
            )

            MorphFieldVisual(
                second,
                (baseText*dataScale(second)).coerceAtLeast(6.8f).sp,
                2,
                fullToHalf,
                halfToSmall,
                radiusProgress
            )
        }
    ){measurables,constraints->
        val width=constraints.maxWidth
        val height=constraints.maxHeight

        fun px(v:Float)=with(density){v.dp.toPx()}

        val fullOuter=px(12f)
        val fullGap=px(7f)
        val fullTop=px(12f)
        val fullHeight=px(46f)
        val fullSignWidth=px(58f)

        val fullNumberWidth=(
            width-fullOuter*2-fullGap*2-fullSignWidth
        )/2f

        val fullX=floatArrayOf(
            fullOuter,
            fullOuter+fullNumberWidth+fullGap,
            fullOuter+fullNumberWidth+fullGap+fullSignWidth+fullGap
        )

        val fullY=floatArrayOf(fullTop,fullTop,fullTop)
        val fullW=floatArrayOf(fullNumberWidth,fullSignWidth,fullNumberWidth)
        val fullH=floatArrayOf(fullHeight,fullHeight,fullHeight)

        val halfWidth=width*nmixMix(.31f,.265f,halfShrink)
        val halfLeft=nmixMix(px(7f),px(4.5f),halfShrink)

        val outerGap=maxOf(
            nmixMix(height*.065f,height*.038f,halfShrink),
            px(2.5f)
        )

        val innerGap=nmixMix(px(5f),px(1.8f),halfShrink)

        val availableHeight=(
            height-outerGap*2-innerGap*2
        ).coerceAtLeast(px(48f))

        val numberHeight=availableHeight/2.78f
        val signHeight=numberHeight*.78f

        val halfX=floatArrayOf(halfLeft,halfLeft,halfLeft)

        val halfY=floatArrayOf(
            outerGap,
            outerGap+numberHeight+innerGap,
            outerGap+numberHeight+innerGap+signHeight+innerGap
        )

        val halfW=floatArrayOf(halfWidth,halfWidth,halfWidth)
        val halfH=floatArrayOf(numberHeight,signHeight,numberHeight)

        val smallLeft=nmixMix(px(7f),px(4f),smallShrink)
        val smallTotalWidth=width*nmixMix(.45f,.48f,smallShrink)
        val smallGap=nmixMix(px(4f),px(2f),smallShrink)
        val smallSignWidth=smallTotalWidth*.24f

        val smallNumberWidth=(
            smallTotalWidth-smallSignWidth-smallGap*2
        )/2f

        val smallOuterY=nmixMix(px(7f),px(3f),smallShrink)

        val smallHeight=(
            height-smallOuterY*2
        ).coerceAtLeast(px(24f))

        val smallY=(height-smallHeight)/2f

        val smallX=floatArrayOf(
            smallLeft,
            smallLeft+smallNumberWidth+smallGap,
            smallLeft+smallNumberWidth+smallGap+smallSignWidth+smallGap
        )

        val smallYs=floatArrayOf(smallY,smallY,smallY)
        val smallW=floatArrayOf(smallNumberWidth,smallSignWidth,smallNumberWidth)
        val smallH=floatArrayOf(smallHeight,smallHeight,smallHeight)

        val stageX=FloatArray(3)
        val stageY=FloatArray(3)
        val stageW=FloatArray(3)
        val stageH=FloatArray(3)

        repeat(3){i->
            stageX[i]=nmixMix(fullX[i],halfX[i],fullToHalf)
            stageY[i]=nmixMix(fullY[i],halfY[i],fullToHalf)
            stageW[i]=nmixMix(fullW[i],halfW[i],fullToHalf)
            stageH[i]=nmixMix(fullH[i],halfH[i],fullToHalf)
        }

        val finalX=FloatArray(3)
        val finalY=FloatArray(3)
        val finalW=FloatArray(3)
        val finalH=FloatArray(3)

        repeat(3){i->
            finalX[i]=nmixMix(stageX[i],smallX[i],halfToSmall)
            finalY[i]=nmixMix(stageY[i],smallYs[i],halfToSmall)
            finalW[i]=nmixMix(stageW[i],smallW[i],halfToSmall)
            finalH[i]=nmixMix(stageH[i],smallH[i],halfToSmall)
        }

        val placeables=measurables.mapIndexed{i,m->
            m.measure(
                androidx.compose.ui.unit.Constraints.fixed(
                    finalW[i]
                        .toInt()
                        .coerceAtLeast(1)
                        .coerceAtMost(width.coerceAtLeast(1)),
                    finalH[i]
                        .toInt()
                        .coerceAtLeast(1)
                        .coerceAtMost(height.coerceAtLeast(1))
                )
            )
        }

        layout(width,height){
            placeables.forEachIndexed{i,placeable->
                val maxX=(width-placeable.width).coerceAtLeast(0)
                val maxY=(height-placeable.height).coerceAtLeast(0)

                placeable.placeRelative(
                    finalX[i].toInt().coerceIn(0,maxX),
                    finalY[i].toInt().coerceIn(0,maxY)
                )
            }
        }
    }
}

@Composable
private fun MorphFieldVisual(
    text:String,
    textSize:TextUnit,
    kind:Int,
    fullToHalf:Float,
    halfToSmall:Float,
    radiusProgress:Float
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val full=1f-fullToHalf
    val small=halfToSmall
    val normalRadius=11f
    val pillRadius=nmixMix(11f,36f,radiusProgress)

    val shape=when(kind){
        0->RoundedCornerShape(
            topStart=nmixMix(normalRadius,pillRadius,small).dp,
            topEnd=0.dp,
            bottomEnd=0.dp,
            bottomStart=nmixMix(0f,pillRadius,small).dp
        )

        1->RoundedCornerShape(0.dp)

        else->RoundedCornerShape(
            topStart=0.dp,
            topEnd=(normalRadius*full).dp,
            bottomEnd=0.dp,
            bottomStart=(
                normalRadius*
                    fullToHalf*
                    (1f-small)
            ).dp
        )
    }

    /*
     * Slightly more transparent/soft than before.
     */
    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151B18).copy(alpha=.78f)
                else
                    Color.White.copy(alpha=.76f)
            )
            .background(
                p.accent.copy(alpha=if(a.darkMode).045f else .028f)
            )
            .border(
                .5.dp,
                nmixScreenBorder(),
                shape
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=nmixDisplayText().copy(
                alpha=if(a.darkMode).88f else .84f
            ),
            fontSize=textSize,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1,
            textAlign=TextAlign.Center
        )
    }
}

/* ==================================================
 * CALCULATOR FIELD
 * ================================================== */

@Composable
fun NmixCalcField(
    text:String,
    modifier:Modifier=Modifier,
    shape:Shape=RoundedCornerShape(11.dp),
    height:Dp?=46.dp,
    textSize:TextUnit=15.sp
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    Box(
        modifier
            .then(
                if(height!=null)
                    Modifier.height(height)
                else Modifier
            )
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151B18).copy(alpha=.78f)
                else
                    Color.White.copy(alpha=.76f)
            )
            .background(
                p.accent.copy(alpha=if(a.darkMode).045f else .028f)
            )
            .border(.5.dp,nmixScreenBorder(),shape),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=nmixDisplayText().copy(alpha=.87f),
            fontSize=textSize,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1,
            textAlign=TextAlign.Center
        )
    }
}

/* ==================================================
 * GLASS
 * ================================================== */

@Composable
fun NmixGlassBox(
    modifier:Modifier=Modifier,
    accentTint:Boolean=true,
    content:@Composable BoxScope.()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(13.dp)

    Box(
        modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917).copy(alpha=.92f)
                else
                    Color.White.copy(alpha=.92f)
            )
            .background(
                if(accentTint)
                    p.accent.copy(alpha=if(a.darkMode).04f else .022f)
                else Color.Transparent
            )
            .border(
                .45.dp,
                p.accent.copy(alpha=if(a.darkMode).14f else .22f),
                shape
            ),
        content=content
    )
}

/* ==================================================
 * PRESS
 * ================================================== */

@Composable
fun NmixPressBox(
    modifier:Modifier,
    shape:Shape,
    color:Color,
    onClick:()->Unit,
    content:@Composable ()->Unit
){
    val haptic=rememberNmixHapticAction()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        if(pressed).95f else 1f,
        spring(dampingRatio=.72f,stiffness=620f),
        label="press"
    )

    Box(
        modifier
            .scale(pressScale)
            .clip(shape)
            .background(color)
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                haptic(onClick)
            },
        contentAlignment=Alignment.Center
    ){
        content()
    }
}
