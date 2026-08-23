package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val CLOCK_PREFS="nmix_fullscreen_clock"

private data class ClockParts(
    val hour:String,
    val minute:String,
    val second:String,
    val period:String
)

private val clockStyles=listOf("Digital","Minimal","Stack","Focus")
private val clockFonts=listOf("Inter","Nunito","Outfit","Poppins","Quicksand")
private val clockColors=listOf("White","Green","Blue","Purple","Orange","Rose","Cyan")

private val clockColorValues=listOf(
    Color.White,
    Color(0xFF6CBBA1),
    Color(0xFF71B4D8),
    Color(0xFFB295DD),
    Color(0xFFE4A16B),
    Color(0xFFDC8AA2),
    Color(0xFF68C6D0)
)

@Composable
fun NmixFullscreenClock(
    time:String,
    date:String,
    onExit:()->Unit
){
    val context=LocalContext.current
    val activity=LocalActivity.current
    val configuration=LocalConfiguration.current
    val a=LocalNmixAppearance.current
    val haptic=rememberNmixHapticAction()

    val prefs=remember(context){
        context.getSharedPreferences(CLOCK_PREFS,Context.MODE_PRIVATE)
    }

    val landscape=
        configuration.orientation==Configuration.ORIENTATION_LANDSCAPE

    val originalOrientation=remember(activity){
        activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    val initialFont=when(a.font){
        NmixFontName.INTER->0
        NmixFontName.NUNITO->1
        NmixFontName.OUTFIT->2
        NmixFontName.POPPINS->3
        NmixFontName.QUICKSAND->4
    }

    var fontIndex by remember{
        mutableIntStateOf(
            prefs.getInt("font",initialFont)
                .coerceIn(0,clockFonts.lastIndex)
        )
    }

    var styleIndex by remember{
        mutableIntStateOf(
            prefs.getInt("style",0)
                .coerceIn(0,clockStyles.lastIndex)
        )
    }

    var colorIndex by remember{
        mutableIntStateOf(
            prefs.getInt("text_color",0)
                .coerceIn(0,clockColors.lastIndex)
        )
    }

    var showHours by remember{
        mutableStateOf(prefs.getBoolean("hours",true))
    }

    var showMinutes by remember{
        mutableStateOf(prefs.getBoolean("minutes",true))
    }

    var showSeconds by remember{
        mutableStateOf(prefs.getBoolean("seconds",true))
    }

    var showDate by remember{
        mutableStateOf(prefs.getBoolean("date",true))
    }

    var clean by remember{mutableStateOf(false)}
    var displayOptions by remember{mutableStateOf(false)}
    var wallpaperConsent by remember{mutableStateOf(false)}

    var customWallpaperString by remember{
        mutableStateOf(prefs.getString("custom_wallpaper",null))
    }

    val customWallpaper=customWallpaperString?.let(Uri::parse)

    val font=when(fontIndex){
        1->NmixNunito
        2->NmixOutfit
        3->NmixPoppins
        4->NmixQuicksand
        else->NmixInter
    }

    val parts=parseFullscreenTime(time)
    val clockTextColor=clockColorValues[
        colorIndex.coerceIn(0,clockColorValues.lastIndex)
    ]

    fun saveBoolean(key:String,value:Boolean){
        prefs.edit().putBoolean(key,value).apply()
    }

    fun exitFullscreen(){
        activity?.requestedOrientation=originalOrientation
        onExit()
    }

    val imagePicker=rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri->
        if(uri!=null){
            customWallpaperString=uri.toString()
            prefs.edit()
                .putString("custom_wallpaper",uri.toString())
                .apply()
        }
    }

    BackHandler{exitFullscreen()}

    DisposableEffect(activity){
        val window=activity?.window

        if(window!=null){
            WindowCompat.setDecorFitsSystemWindows(window,false)

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply{
                hide(
                    WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
                )
                systemBarsBehavior=
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose{
            activity?.requestedOrientation=originalOrientation

            if(window!=null){
                WindowCompat.setDecorFitsSystemWindows(window,true)
                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).show(
                    WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
                )
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                if(a.darkMode)Color(0xFF050807)
                else Color(0xFFF8FAF9)
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},
                indication=null
            ){
                when{
                    wallpaperConsent->wallpaperConsent=false
                    displayOptions->displayOptions=false
                    clean->clean=false
                }
            }
    ){
        if(customWallpaper!=null){
            FullscreenWallpaper(customWallpaper)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if(a.darkMode)
                            Color.Black.copy(alpha=.43f)
                        else
                            Color.White.copy(alpha=.68f)
                    )
            )
        }

        FullscreenWorldBackground(customWallpaper!=null)

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            ClockBrand(
                Modifier.padding(start=19.dp,top=17.dp)
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            /*
             * All three are physically the same size.
             * FONT/STYLE keep accent selection.
             * COLOR alone previews its selected clock color.
             */
            if(landscape){
                Row(
                    Modifier.padding(top=14.dp,end=10.dp),
                    horizontalArrangement=Arrangement.spacedBy(5.dp)
                ){
                    ClockDragSelector(
                        title="FONT",
                        options=clockFonts,
                        selected=fontIndex,
                        centerColor=a.palette.accent,
                        font=font
                    ){
                        fontIndex=it
                        prefs.edit().putInt("font",it).apply()
                    }

                    ClockDragSelector(
                        title="STYLE",
                        options=clockStyles,
                        selected=styleIndex,
                        centerColor=a.palette.accent,
                        font=font
                    ){
                        styleIndex=it
                        prefs.edit().putInt("style",it).apply()
                    }

                    ClockDragSelector(
                        title="COLOR",
                        options=clockColors,
                        selected=colorIndex,
                        centerColor=clockTextColor,
                        font=font,
                        colorSelector=true
                    ){
                        colorIndex=it
                        prefs.edit().putInt("text_color",it).apply()
                    }
                }
            }else{
                Column(
                    Modifier.padding(top=14.dp,end=8.dp),
                    verticalArrangement=Arrangement.spacedBy(5.dp)
                ){
                    ClockDragSelector(
                        "FONT",
                        clockFonts,
                        fontIndex,
                        a.palette.accent,
                        font
                    ){
                        fontIndex=it
                        prefs.edit().putInt("font",it).apply()
                    }

                    ClockDragSelector(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        a.palette.accent,
                        font
                    ){
                        styleIndex=it
                        prefs.edit().putInt("style",it).apply()
                    }

                    ClockDragSelector(
                        "COLOR",
                        clockColors,
                        colorIndex,
                        clockTextColor,
                        font,
                        colorSelector=true
                    ){
                        colorIndex=it
                        prefs.edit().putInt("text_color",it).apply()
                    }
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if(landscape).90f else .95f)
                .height(if(landscape)330.dp else 370.dp),
            contentAlignment=Alignment.Center
        ){
            AnimatedContent(
                targetState=styleIndex,
                transitionSpec={
                    (
                        fadeIn(tween(260,easing=EaseOutCubic))+
                            scaleIn(.985f,tween(280))
                    ) togetherWith (
                        fadeOut(tween(180))+
                            scaleOut(1.008f)
                    )
                },
                label="clockStyle"
            ){style->
                FullscreenClockFace(
                    style=style,
                    parts=parts,
                    date=date,
                    font=font,
                    landscape=landscape,
                    showHours=showHours,
                    showMinutes=showMinutes,
                    showSeconds=showSeconds,
                    showDate=showDate,
                    clockColor=clockTextColor
                )
            }

            AnimatedVisibility(
                visible=clean,
                modifier=Modifier.align(Alignment.BottomCenter)
            ){
                ClockBrand(
                    Modifier.padding(bottom=7.dp),
                    centered=true
                )
            }
        }

        AnimatedVisibility(
            visible=!clean && displayOptions,
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom=77.dp),
            enter=fadeIn(tween(180))+scaleIn(initialScale=.97f),
            exit=fadeOut(tween(150))
        ){
            DisplayOptions(
                showHours,
                showMinutes,
                showSeconds,
                showDate,
                onHours={
                    haptic{
                        showHours=!showHours
                        saveBoolean("hours",showHours)
                    }
                },
                onMinutes={
                    haptic{
                        showMinutes=!showMinutes
                        saveBoolean("minutes",showMinutes)
                    }
                },
                onSeconds={
                    haptic{
                        showSeconds=!showSeconds
                        saveBoolean("seconds",showSeconds)
                    }
                },
                onDate={
                    haptic{
                        showDate=!showDate
                        saveBoolean("date",showDate)
                    }
                }
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            Row(
                Modifier.padding(start=7.dp,end=7.dp,bottom=16.dp),
                horizontalArrangement=Arrangement.spacedBy(5.dp),
                verticalAlignment=Alignment.CenterVertically
            ){
                ClockAction(
                    "Wallpaper",
                    NmixIcon.WALLPAPER,
                    font
                ){
                    haptic{wallpaperConsent=true}
                }

                ClockAction(
                    "Rotate",
                    NmixIcon.ROTATE,
                    font
                ){
                    haptic{
                        activity?.requestedOrientation=
                            if(landscape)
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            else
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }

                ClockAction(
                    "Display",
                    NmixIcon.CLOCK,
                    font,
                    selected=displayOptions
                ){
                    haptic{displayOptions=!displayOptions}
                }

                ClockAction(
                    "Clean",
                    NmixIcon.FULLSCREEN,
                    font
                ){
                    haptic{
                        displayOptions=false
                        clean=true
                    }
                }

                ClockAction(
                    "Exit",
                    NmixIcon.CLOSE,
                    font,
                    red=true
                ){
                    haptic{exitFullscreen()}
                }
            }
        }

        AnimatedVisibility(
            visible=wallpaperConsent,
            modifier=Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            enter=fadeIn(tween(180))+scaleIn(initialScale=.97f),
            exit=fadeOut(tween(140))
        ){
            WallpaperDialog(
                hasWallpaper=customWallpaper!=null,
                onCancel={wallpaperConsent=false},
                onRemove={
                    customWallpaperString=null
                    prefs.edit().remove("custom_wallpaper").apply()
                    wallpaperConsent=false
                },
                onChoose={
                    wallpaperConsent=false
                    imagePicker.launch("image/*")
                }
            )
        }
    }
}

/* ==================================================
 * FINGER-FOLLOW CAROUSEL
 * ================================================== */

@Composable
private fun ClockDragSelector(
    title:String,
    options:List<String>,
    selected:Int,
    centerColor:Color,
    font:FontFamily,
    colorSelector:Boolean=false,
    onSelect:(Int)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    var widthPx by remember{mutableIntStateOf(1)}
    var dragPx by remember{mutableFloatStateOf(0f)}

    val safe=selected.coerceIn(0,options.lastIndex)

    fun index(delta:Int):Int{
        var value=(safe+delta)%options.size
        if(value<0)value+=options.size
        return value
    }

    val previous=index(-1)
    val next=index(1)
    val threshold=(widthPx*.23f).coerceAtLeast(1f)

    val visual=(dragPx/threshold).coerceIn(-1f,1f)

    val shape=RoundedCornerShape(50)

    Column(
        Modifier
            .width(162.dp)
            .height(52.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF131816).copy(alpha=.94f)
                else
                    Color.White.copy(alpha=.95f)
            )
            .background(
                p.accent.copy(alpha=if(a.darkMode).027f else .018f)
            )
            .border(
                .55.dp,
                p.accent.copy(alpha=if(a.darkMode).18f else .27f),
                shape
            )
            .onSizeChanged{widthPx=it.width.coerceAtLeast(1)}
            .pointerInput(safe,widthPx,options.size){
                detectHorizontalDragGestures(
                    onDragStart={dragPx=0f},
                    onHorizontalDrag={_,amount->
                        dragPx=(dragPx+amount).coerceIn(
                            -threshold*1.18f,
                            threshold*1.18f
                        )
                    },
                    onDragEnd={
                        when{
                            dragPx<=-threshold*.58f->
                                haptic{onSelect(next)}

                            dragPx>=threshold*.58f->
                                haptic{onSelect(previous)}
                        }
                        dragPx=0f
                    },
                    onDragCancel={dragPx=0f}
                )
            },
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=p.accent,
            fontSize=7.3.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily,
            modifier=Modifier.padding(top=4.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        bottomStart=50.dp,
                        bottomEnd=50.dp
                    )
                )
        ){
            val previousSelected=visual.coerceIn(0f,1f)
            val currentSelected=(1f-abs(visual)).coerceIn(0f,1f)
            val nextSelected=(-visual).coerceIn(0f,1f)

            SelectorText(
                text=options[previous],
                position=-1f+visual,
                selectedAmount=previousSelected,
                selectedColor=
                    if(colorSelector)
                        clockColorValues[previous]
                    else
                        centerColor,
                muted=ui.muted,
                font=font
            )

            SelectorText(
                text=options[safe],
                position=visual,
                selectedAmount=currentSelected,
                selectedColor=
                    if(colorSelector)
                        clockColorValues[safe]
                    else
                        centerColor,
                muted=ui.muted,
                font=font
            )

            SelectorText(
                text=options[next],
                position=1f+visual,
                selectedAmount=nextSelected,
                selectedColor=
                    if(colorSelector)
                        clockColorValues[next]
                    else
                        centerColor,
                muted=ui.muted,
                font=font
            )
        }
    }
}

@Composable
private fun BoxScope.SelectorText(
    text:String,
    position:Float,
    selectedAmount:Float,
    selectedColor:Color,
    muted:Color,
    font:FontFamily
){
    val amount=selectedAmount.coerceIn(0f,1f)

    Text(
        text,
        Modifier
            .align(Alignment.Center)
            .graphicsLayer{
                /*
                 * More separation between side and
                 * center labels while finger-follow
                 * movement remains continuous.
                 */
                translationX=position*58f
                scaleX=.88f+amount*.14f
                scaleY=.88f+amount*.14f
            },
        color=lerpClockColor(
            muted.copy(alpha=.72f),
            selectedColor,
            amount
        ),
        fontSize=(8.3f+amount*2.2f).sp,
        fontWeight=
            if(amount>.52f)FontWeight.Bold
            else FontWeight.Medium,
        fontFamily=font,
        maxLines=1,
        textAlign=TextAlign.Center
    )
}

/* ==================================================
 * GIANT WORLD BACKGROUND
 * ================================================== */

@Composable
private fun BoxScope.FullscreenWorldBackground(
    wallpaper:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val world=rememberNmixWorldMotion("fullscreenWorld")

    if(!a.animationEnabled){
        Box(
            Modifier
                .size(1000.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(
                                alpha=if(a.darkMode).18f else .13f
                            ),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        return
    }

    world.bodies.forEachIndexed{index,body->
        /*
         * Much larger than the actual screen.
         * Only imaginary world walls affect direction.
         */
        val worldX=body.x*(900f+index*66f)
        val worldY=body.y*(760f+index*55f)
        val alpha=if(wallpaper).58f else 1f
        val soft=a.animation!=NmixAnimationName.FLOAT

        if(soft){
            Box(
                Modifier
                    .size(
                        when(index){
                            0->1480.dp
                            1->1280.dp
                            2->1110.dp
                            3->1190.dp
                            else->980.dp
                        }
                    )
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=worldX
                        translationY=worldY
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    (if(index%2==0)p.accent else p.accentLight)
                                        .copy(
                                            alpha=(if(a.darkMode).31f else .24f)*alpha
                                        ),
                                .28f to p.accent.copy(alpha=.17f*alpha),
                                .56f to p.accent.copy(alpha=.075f*alpha),
                                .78f to p.accent.copy(alpha=.020f*alpha),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val elementSize=when(index){
                0->560.dp
                1->485.dp
                2->420.dp
                3->455.dp
                else->375.dp
            }

            Canvas(
                Modifier
                    .size(elementSize)
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=worldX
                        translationY=worldY
                        rotationZ=body.rotation
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
            ){
                val color=if(index%2==0)p.accent else p.accentLight
                val inset=14.dp.toPx()

                drawRoundRect(
                    color=color.copy(alpha=.025f*alpha),
                    cornerRadius=CornerRadius(55.dp.toPx())
                )

                drawRoundRect(
                    color=color.copy(
                        alpha=(if(a.darkMode).145f else .11f)*alpha
                    ),
                    topLeft=Offset(inset,inset),
                    size=Size(
                        (size.width-inset*2).coerceAtLeast(0f),
                        (size.height-inset*2).coerceAtLeast(0f)
                    ),
                    cornerRadius=CornerRadius(43.dp.toPx())
                )
            }
        }
    }
}

/* ==================================================
 * CLOCK FACE
 * ================================================== */

@Composable
private fun FullscreenClockFace(
    style:Int,
    parts:ClockParts,
    date:String,
    font:FontFamily,
    landscape:Boolean,
    showHours:Boolean,
    showMinutes:Boolean,
    showSeconds:Boolean,
    showDate:Boolean,
    clockColor:Color
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val mainColor=when{
        clockColor!=Color.White->clockColor
        a.darkMode->Color.White.copy(alpha=.94f)
        else->Color(0xFF252A27)
    }

    val secondary=
        if(clockColor==Color.White){
            if(a.darkMode)p.accentLight else p.accentDark
        }else{
            clockColor.copy(alpha=.74f)
        }

    val numeric=buildList{
        if(showHours)add(parts.hour)
        if(showMinutes)add(parts.minute)
        if(showSeconds)add(parts.second)
    }.joinToString(":").ifEmpty{"--"}

    /*
     * period is never linked to showSeconds.
     */
    when(style){
        1->Column(
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                numeric,
                color=mainColor,
                fontSize=if(landscape)78.sp else 55.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=font,
                maxLines=1
            )
            Text(
                parts.period,
                color=secondary,
                fontSize=14.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=font
            )

            if(showDate){
                Spacer(Modifier.height(10.dp))
                Text(
                    date,
                    color=mainColor.copy(alpha=.64f),
                    fontSize=11.sp,
                    fontFamily=font
                )
            }
        }

        2->Row(
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(20.dp)
        ){
            Column{
                if(showHours){
                    Text(
                        parts.hour,
                        color=mainColor,
                        fontSize=if(landscape)68.sp else 52.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font
                    )
                }

                if(showMinutes){
                    Text(
                        parts.minute,
                        color=secondary,
                        fontSize=if(landscape)68.sp else 52.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font
                    )
                }
            }

            Column{
                if(showSeconds){
                    Text(
                        parts.second,
                        color=mainColor,
                        fontSize=31.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font
                    )
                }

                Text(
                    parts.period,
                    color=secondary,
                    fontSize=13.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font
                )

                if(showDate){
                    Spacer(Modifier.height(8.dp))
                    Text(
                        date,
                        color=mainColor.copy(alpha=.60f),
                        fontSize=9.sp,
                        fontFamily=font
                    )
                }
            }
        }

        3->Column(
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                "FOCUS",
                color=secondary,
                fontSize=9.sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=3.5.sp,
                fontFamily=font
            )

            Spacer(Modifier.height(9.dp))

            Row(verticalAlignment=Alignment.Bottom){
                Text(
                    numeric,
                    color=mainColor,
                    fontSize=if(landscape)82.sp else 57.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    maxLines=1
                )

                Spacer(Modifier.width(7.dp))

                Text(
                    parts.period,
                    color=secondary,
                    fontSize=14.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    modifier=Modifier.padding(bottom=8.dp)
                )
            }

            if(showDate){
                Spacer(Modifier.height(9.dp))
                Text(
                    date,
                    color=mainColor.copy(alpha=.60f),
                    fontSize=10.sp,
                    fontFamily=font
                )
            }
        }

        else->Column(
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                "NMIX • LOCAL TIME",
                color=secondary,
                fontSize=10.sp,
                letterSpacing=1.9.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=font
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment=Alignment.Bottom){
                Text(
                    numeric,
                    color=mainColor,
                    fontSize=if(landscape)78.sp else 55.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    maxLines=1
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    parts.period,
                    color=secondary,
                    fontSize=14.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    modifier=Modifier.padding(bottom=8.dp)
                )
            }

            if(showDate){
                Spacer(Modifier.height(10.dp))
                Text(
                    date,
                    color=mainColor.copy(alpha=.67f),
                    fontSize=12.sp,
                    fontFamily=font
                )
            }
        }
    }
}

/* ==================================================
 * DISPLAY OPTIONS
 * ================================================== */

@Composable
private fun DisplayOptions(
    hours:Boolean,
    minutes:Boolean,
    seconds:Boolean,
    date:Boolean,
    onHours:()->Unit,
    onMinutes:()->Unit,
    onSeconds:()->Unit,
    onDate:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)

    Row(
        Modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917).copy(alpha=.95f)
                else
                    Color.White.copy(alpha=.96f)
            )
            .background(p.accent.copy(alpha=.025f))
            .border(
                .55.dp,
                p.accent.copy(alpha=if(a.darkMode).18f else .27f),
                shape
            )
            .padding(8.dp),
        horizontalArrangement=Arrangement.spacedBy(6.dp)
    ){
        DisplayChoice("H",hours,onHours)
        DisplayChoice("M",minutes,onMinutes)
        DisplayChoice("S",seconds,onSeconds)
        DisplayChoice("D",date,onDate)
    }
}

@Composable
private fun DisplayChoice(
    text:String,
    selected:Boolean,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val progress by animateFloatAsState(
        if(selected)1f else 0f,
        tween(190),
        label="displayChoice"
    )

    val shape=RoundedCornerShape(50)

    Box(
        Modifier
            .width(44.dp)
            .height(39.dp)
            .clip(shape)
            .background(
                if(a.darkMode)Color(0xFF111614)
                else Color.White
            )
            .background(
                p.accent.copy(alpha=.018f+progress*.055f)
            )
            .border(
                (.45f+progress*.6f).dp,
                p.accent.copy(alpha=.16f+progress*.36f),
                shape
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=if(selected)p.accent else ui.text,
            fontSize=10.5.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/* ==================================================
 * BOTTOM ACTIONS
 * ================================================== */

@Composable
private fun ClockAction(
    text:String,
    icon:NmixIcon,
    font:FontFamily,
    red:Boolean=false,
    selected:Boolean=false,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(50)

    val foreground=
        if(red)Color(0xFFE66E75)
        else ui.text

    Row(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF131816).copy(alpha=.94f)
                else Color.White.copy(alpha=.94f)
            )
            .background(
                when{
                    red->Color(0xFFD94F57).copy(alpha=.055f)
                    selected->p.accent.copy(alpha=.075f)
                    else->p.accent.copy(alpha=.018f)
                }
            )
            .border(
                if(selected).95.dp else .5.dp,
                when{
                    red->foreground.copy(alpha=.28f)
                    selected->p.accent.copy(alpha=.50f)
                    else->p.accent.copy(
                        alpha=if(a.darkMode).17f else .25f
                    )
                },
                shape
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},
                indication=null,
                onClick=onClick
            )
            .padding(horizontal=10.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(6.dp)
    ){
        NmixIcon(
            icon,
            Modifier.size(16.dp),
            if(red)foreground else p.accent
        )

        Text(
            text,
            color=foreground,
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=font,
            maxLines=1
        )
    }
}

/* ==================================================
 * WALLPAPER
 * ================================================== */

@Composable
private fun WallpaperDialog(
    hasWallpaper:Boolean,
    onCancel:()->Unit,
    onRemove:()->Unit,
    onChoose:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(22.dp)

    Column(
        Modifier
            .width(286.dp)
            .clip(shape)
            .background(
                if(a.darkMode)Color(0xFF151A18)
                else Color(0xFFF8FAF9)
            )
            .background(p.accent.copy(alpha=.022f))
            .border(.55.dp,p.accent.copy(alpha=.27f),shape)
            .padding(15.dp)
    ){
        Text(
            "CUSTOM WALLPAPER",
            color=p.accent,
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Choose an image for Fullscreen Clock.",
            color=ui.muted,
            fontSize=9.sp,
            lineHeight=14.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(13.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(7.dp)
        ){
            DialogButton(
                "Cancel",
                Modifier.weight(1f),
                onClick=onCancel
            )

            if(hasWallpaper){
                DialogButton(
                    "Remove",
                    Modifier.weight(1f),
                    red=true,
                    onClick=onRemove
                )
            }

            DialogButton(
                "Choose",
                Modifier.weight(1f),
                accent=true,
                onClick=onChoose
            )
        }
    }
}

@Composable
private fun DialogButton(
    text:String,
    modifier:Modifier,
    accent:Boolean=false,
    red:Boolean=false,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()
    val shape=RoundedCornerShape(50)

    Box(
        modifier
            .height(40.dp)
            .clip(shape)
            .background(
                when{
                    accent->p.accent.copy(alpha=.82f)
                    red->Color(0xFFD94F57).copy(alpha=.11f)
                    a.darkMode->Color(0xFF111614)
                    else->Color.White
                }
            )
            .border(
                .5.dp,
                when{
                    red->Color(0xFFE66E75).copy(alpha=.26f)
                    else->p.accent.copy(alpha=.22f)
                },
                shape
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},
                indication=null
            ){haptic(onClick)},
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=when{
                accent->Color.White
                red->Color(0xFFE66E75)
                else->ui.text
            },
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/* ==================================================
 * BRAND / PARSING / IMAGE
 * ================================================== */

@Composable
private fun ClockBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false
){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val color=if(a.darkMode)Color.White else ui.text

    Column(
        modifier,
        horizontalAlignment=
            if(centered)Alignment.CenterHorizontally
            else Alignment.Start
    ){
        Text(
            "EVERYTHING WITH NUMBERS",
            color=color.copy(alpha=.58f),
            fontSize=7.sp,
            letterSpacing=1.5.sp,
            fontFamily=a.fontFamily
        )

        Text(
            "NMIX",
            color=color,
            fontSize=24.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=2.sp,
            fontFamily=NmixLogoFont
        )
    }
}

private fun parseFullscreenTime(time:String):ClockParts{
    val trimmed=time.trim()

    val period=when{
        trimmed.endsWith(" AM",true)->"AM"
        trimmed.endsWith(" PM",true)->"PM"
        else->""
    }

    val raw=trimmed
        .removeSuffix(" AM")
        .removeSuffix(" PM")
        .removeSuffix(" am")
        .removeSuffix(" pm")

    val pieces=raw.split(":")

    return ClockParts(
        pieces.getOrElse(0){"00"},
        pieces.getOrElse(1){"00"},
        pieces.getOrElse(2){"00"},
        period
    )
}

private fun lerpClockColor(
    start:Color,
    end:Color,
    amount:Float
):Color{
    val t=amount.coerceIn(0f,1f)

    return Color(
        red=start.red+(end.red-start.red)*t,
        green=start.green+(end.green-start.green)*t,
        blue=start.blue+(end.blue-start.blue)*t,
        alpha=start.alpha+(end.alpha-start.alpha)*t
    )
}

@Composable
private fun FullscreenWallpaper(uri:Uri){
    val context=LocalContext.current

    var bitmap by remember(uri){
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(uri){
        bitmap=withContext(Dispatchers.IO){
            try{
                context.contentResolver
                    .openInputStream(uri)
                    ?.use{
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
            }catch(_:Exception){
                null
            }
        }
    }

    bitmap?.let{
        Image(
            bitmap=it,
            contentDescription=null,
            modifier=Modifier.fillMaxSize(),
            contentScale=ContentScale.Crop
        )
    }
}
