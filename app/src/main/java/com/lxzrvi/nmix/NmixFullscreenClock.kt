package com.lxzrvi.nmix

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val CLOCK_PREFS=
    "nmix_fullscreen_clock"

private data class ClockTone(
    val name:String,
    val main:Color,
    val accent:Color
)

private data class ClockParts(
    val hour:String,
    val minute:String,
    val second:String,
    val period:String
)

private val clockTones=
    listOf(
        ClockTone(
            "White",
            Color(0xFFF8FAF9),
            Color.White
        ),
        ClockTone(
            "Black",
            Color(0xFF111311),
            Color(0xFF111311)
        ),
        ClockTone(
            "Ice",
            Color(0xFFEAF8FF),
            Color(0xFF62CFF9)
        ),
        ClockTone(
            "Mint",
            Color(0xFFE8FFF5),
            Color(0xFF4CD9A5)
        ),
        ClockTone(
            "Amber",
            Color(0xFFFFF1D9),
            Color(0xFFFFAE43)
        ),
        ClockTone(
            "Rose",
            Color(0xFFFFEAF1),
            Color(0xFFFF749F)
        ),
        ClockTone(
            "Violet",
            Color(0xFFF1E8FF),
            Color(0xFFA983F5)
        ),
        ClockTone(
            "Aqua",
            Color(0xFFE5FDFF),
            Color(0xFF43D4E0)
        )
    )

private val clockStyles=
    listOf(
        "Digital",
        "Minimal",
        "Stack",
        "Focus",
        "Orbit",
        "Terminal",
        "Capsule",
        "Studio"
    )

private val clockFonts=
    listOf(
        "Inter",
        "Nunito",
        "Outfit",
        "Poppins",
        "Quicksand"
    )

@Composable
fun NmixFullscreenClock(
    time:String,
    date:String,
    onExit:()->Unit
){
    val context=
        LocalContext.current

    val activity=
        LocalActivity.current

    val configuration=
        LocalConfiguration.current

    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()
    val haptic=
        rememberNmixHapticAction()

    val prefs=
        remember(context){
            context.getSharedPreferences(
                CLOCK_PREFS,
                Context.MODE_PRIVATE
            )
        }

    val landscape=
        configuration.orientation==
            Configuration.ORIENTATION_LANDSCAPE

    val originalOrientation=
        remember(activity){
            activity?.requestedOrientation
                ?:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

    val defaultFont=
        when(a.font){
            NmixFontName.INTER->0
            NmixFontName.NUNITO->1
            NmixFontName.OUTFIT->2
            NmixFontName.POPPINS->3
            NmixFontName.QUICKSAND->4
        }

    var fontIndex by remember{
        mutableIntStateOf(
            prefs.getInt(
                "font",
                defaultFont
            ).coerceIn(
                0,
                clockFonts.lastIndex
            )
        )
    }

    var styleIndex by remember{
        mutableIntStateOf(
            prefs.getInt(
                "style",
                0
            ).coerceIn(
                0,
                clockStyles.lastIndex
            )
        )
    }

    var colorIndex by remember{
        mutableIntStateOf(
            prefs.getInt(
                "color",
                if(a.darkMode)0 else 1
            ).coerceIn(
                0,
                clockTones.lastIndex
            )
        )
    }

    var showHours by remember{
        mutableStateOf(
            prefs.getBoolean(
                "hours",
                true
            )
        )
    }

    var showMinutes by remember{
        mutableStateOf(
            prefs.getBoolean(
                "minutes",
                true
            )
        )
    }

    var showSeconds by remember{
        mutableStateOf(
            prefs.getBoolean(
                "seconds",
                true
            )
        )
    }

    var showDate by remember{
        mutableStateOf(
            prefs.getBoolean(
                "date",
                true
            )
        )
    }

    var customUriString by remember{
        mutableStateOf(
            prefs.getString(
                "custom_wallpaper",
                null
            )
        )
    }

    var clean by remember{
        mutableStateOf(false)
    }

    var displayPopup by remember{
        mutableStateOf(false)
    }

    var photoConsent by remember{
        mutableStateOf(false)
    }

    val customUri=
        customUriString?.let(
            Uri::parse
        )

    fun saveInt(
        key:String,
        value:Int
    ){
        prefs.edit()
            .putInt(key,value)
            .apply()
    }

    fun saveBoolean(
        key:String,
        value:Boolean
    ){
        prefs.edit()
            .putBoolean(key,value)
            .apply()
    }

    val photoPicker=
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .GetContent()
        ){uri->
            if(uri!=null){
                customUriString=
                    uri.toString()

                prefs.edit()
                    .putString(
                        "custom_wallpaper",
                        uri.toString()
                    )
                    .apply()
            }
        }

    DisposableEffect(activity){
        val window=
            activity?.window

        if(window!=null){
            WindowCompat
                .setDecorFitsSystemWindows(
                    window,
                    false
                )

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply{
                hide(
                    WindowInsetsCompat.Type
                        .statusBars() or
                        WindowInsetsCompat.Type
                            .navigationBars()
                )

                systemBarsBehavior=
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose{
            activity?.requestedOrientation=
                originalOrientation

            if(window!=null){
                WindowCompat
                    .setDecorFitsSystemWindows(
                        window,
                        true
                    )

                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).show(
                    WindowInsetsCompat.Type
                        .statusBars() or
                        WindowInsetsCompat.Type
                            .navigationBars()
                )
            }
        }
    }

    val font=
        when(fontIndex){
            1->NmixNunito
            2->NmixOutfit
            3->NmixPoppins
            4->NmixQuicksand
            else->NmixInter
        }

    val tone=
        clockTones[colorIndex]

    val parts=
        parseClockTime(time)

    val baseBackground=
        if(a.darkMode){
            Brush.verticalGradient(
                listOf(
                    Color(0xFF020403),
                    p.topDark,
                    Color(0xFF07100D),
                    p.topEnd,
                    Color(0xFF020302)
                )
            )
        }else{
            Brush.verticalGradient(
                listOf(
                    ui.page,
                    p.accent.copy(alpha=.28f),
                    ui.page,
                    p.accentLight.copy(alpha=.18f)
                )
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(baseBackground)
            .clip(
                RoundedCornerShape(0.dp)
            )
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null
            ){
                when{
                    photoConsent->
                        photoConsent=false

                    displayPopup->
                        displayPopup=false

                    clean->
                        clean=false
                }
            }
    ){
        if(customUri!=null){
            CustomWallpaper(
                customUri
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if(a.darkMode)
                            Color.Black.copy(alpha=.22f)
                        else
                            Color.White.copy(alpha=.12f)
                    )
            )
        }

        if(
            a.animationEnabled &&
            customUri==null
        ){
            FullscreenMotionLayer()
        }else if(
            customUri==null
        ){
            Box(
                Modifier
                    .size(720.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    p.accent.copy(
                                        alpha=
                                            if(a.darkMode)
                                                .20f
                                            else
                                                .16f
                                    ),

                                .52f to
                                    p.accent.copy(
                                        alpha=.06f
                                    ),

                                1f to
                                    Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    ),
            enter=
                fadeIn(tween(320))+
                slideInVertically(
                    initialOffsetY={
                        -it/3
                    },
                    animationSpec=tween(
                        390,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(tween(210))
        ){
            ClockBrand(
                modifier=
                    Modifier.padding(
                        start=22.dp,
                        top=18.dp
                    ),
                color=
                    if(a.darkMode)
                        Color.White
                    else
                        ui.text
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    ),
            enter=
                fadeIn(tween(330)),
            exit=
                fadeOut(tween(200))
        ){
            if(landscape){
                Row(
                    Modifier.padding(
                        top=15.dp,
                        end=14.dp
                    ),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    ClockCarousel(
                        title="FONT",
                        options=clockFonts,
                        index=fontIndex,
                        width=158,
                        optionFont=font,
                        colorMode=false
                    ){
                        fontIndex=it
                        saveInt("font",it)
                    }

                    ClockCarousel(
                        title="STYLE",
                        options=clockStyles,
                        index=styleIndex,
                        width=158,
                        optionFont=font,
                        colorMode=false
                    ){
                        styleIndex=it
                        saveInt("style",it)
                    }

                    ClockCarousel(
                        title="COLOR",
                        options=
                            clockTones.map{
                                it.name
                            },
                        index=colorIndex,
                        width=158,
                        optionFont=font,
                        colorMode=true
                    ){
                        colorIndex=it
                        saveInt("color",it)
                    }
                }
            }else{
                Column(
                    Modifier.padding(
                        top=16.dp,
                        end=10.dp
                    ),
                    verticalArrangement=
                        Arrangement.spacedBy(6.dp)
                ){
                    ClockCarousel(
                        "FONT",
                        clockFonts,
                        fontIndex,
                        174,
                        font,
                        false
                    ){
                        fontIndex=it
                        saveInt("font",it)
                    }

                    ClockCarousel(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        174,
                        font,
                        false
                    ){
                        styleIndex=it
                        saveInt("style",it)
                    }

                    ClockCarousel(
                        "COLOR",
                        clockTones.map{
                            it.name
                        },
                        colorIndex,
                        174,
                        font,
                        true
                    ){
                        colorIndex=it
                        saveInt("color",it)
                    }
                }
            }
        }

        Box(
            Modifier
                .align(
                    Alignment.Center
                )
                .fillMaxWidth(
                    if(landscape)
                        .88f
                    else
                        .94f
                )
                .height(
                    if(landscape)
                        330.dp
                    else
                        350.dp
                ),
            contentAlignment=
                Alignment.Center
        ){
            AnimatedContent(
                targetState=styleIndex,
                transitionSpec={
                    (
                        fadeIn(
                            tween(
                                390,
                                easing=EaseOutCubic
                            )
                        )+
                        scaleIn(
                            initialScale=.97f,
                            animationSpec=tween(
                                390,
                                easing=EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(tween(230))+
                        scaleOut(
                            targetScale=1.015f,
                            animationSpec=tween(270)
                        )
                    )
                },
                label="clockStyle"
            ){style->
                ClockFace(
                    style=style,
                    parts=parts,
                    date=date,
                    tone=tone,
                    font=font,
                    landscape=landscape,
                    showHours=showHours,
                    showMinutes=showMinutes,
                    showSeconds=showSeconds,
                    showDate=showDate,
                    dark=a.darkMode
                )
            }

            AnimatedVisibility(
                visible=clean,
                modifier=
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ){
                ClockBrand(
                    modifier=
                        Modifier.padding(
                            bottom=8.dp
                        ),
                    centered=true,
                    color=
                        if(a.darkMode)
                            Color.White
                        else
                            ui.text
                )
            }
        }

        AnimatedVisibility(
            visible=
                !clean &&
                displayPopup,
            modifier=
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
                    .padding(
                        bottom=74.dp
                    ),
            enter=
                fadeIn(tween(250))+
                scaleIn(
                    initialScale=.97f,
                    animationSpec=tween(
                        280,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(tween(180))+
                scaleOut(
                    targetScale=.98f
                )
        ){
            DisplayBar(
                hours=showHours,
                minutes=showMinutes,
                seconds=showSeconds,
                date=showDate,
                onHours={
                    haptic{
                        showHours=!showHours
                        saveBoolean(
                            "hours",
                            showHours
                        )
                    }
                },
                onMinutes={
                    haptic{
                        showMinutes=!showMinutes
                        saveBoolean(
                            "minutes",
                            showMinutes
                        )
                    }
                },
                onSeconds={
                    haptic{
                        showSeconds=!showSeconds
                        saveBoolean(
                            "seconds",
                            showSeconds
                        )
                    }
                },
                onDate={
                    haptic{
                        showDate=!showDate
                        saveBoolean(
                            "date",
                            showDate
                        )
                    }
                }
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    ),
            enter=
                fadeIn(tween(320)),
            exit=
                fadeOut(tween(210))
        ){
            Row(
                Modifier.padding(
                    start=10.dp,
                    end=10.dp,
                    bottom=17.dp
                ),
                horizontalArrangement=
                    Arrangement.spacedBy(7.dp),
                verticalAlignment=
                    Alignment.CenterVertically
            ){
                ClockAction(
                    "Wallpaper",
                    NmixIcon.WALLPAPER,
                    font
                ){
                    haptic{
                        photoConsent=true
                    }
                }

                ClockAction(
                    "Rotate",
                    NmixIcon.ROTATE,
                    font
                ){
                    haptic{
                        activity?.requestedOrientation=
                            if(landscape){
                                ActivityInfo
                                    .SCREEN_ORIENTATION_PORTRAIT
                            }else{
                                ActivityInfo
                                    .SCREEN_ORIENTATION_LANDSCAPE
                            }
                    }
                }

                ClockAction(
                    "Display",
                    NmixIcon.CLOCK,
                    font
                ){
                    haptic{
                        displayPopup=
                            !displayPopup
                    }
                }

                ClockAction(
                    "Clean",
                    NmixIcon.FULLSCREEN,
                    font
                ){
                    haptic{
                        displayPopup=false
                        clean=true
                    }
                }

                ClockAction(
                    "Exit",
                    NmixIcon.CLOSE,
                    font,
                    red=true
                ){
                    haptic{
                        activity?.requestedOrientation=
                            originalOrientation

                        onExit()
                    }
                }
            }
        }

        AnimatedVisibility(
            visible=photoConsent,
            modifier=
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .padding(20.dp),
            enter=
                fadeIn(tween(220))+
                scaleIn(
                    initialScale=.96f
                ),
            exit=
                fadeOut(tween(170))+
                scaleOut(
                    targetScale=.97f
                )
        ){
            PhotoConsentCard(
                hasWallpaper=
                    customUri!=null,
                onCancel={
                    photoConsent=false
                },
                onRemove={
                    customUriString=null

                    prefs.edit()
                        .remove(
                            "custom_wallpaper"
                        )
                        .apply()

                    photoConsent=false
                },
                onChoose={
                    photoConsent=false

                    photoPicker.launch(
                        "image/*"
                    )
                }
            )
        }
    }
}

/*
 * ==================================================
 * SHARED FULLSCREEN MOTION
 * ==================================================
 */

@Composable
private fun BoxScope.FullscreenMotionLayer(){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val motion=
        rememberNmixMotion(
            "fullscreenSharedMotion"
        )

    val soft=
        a.animation in listOf(
            NmixAnimationName.DRIFT,
            NmixAnimationName.ORBIT,
            NmixAnimationName.FLOW
        )

    val homes=
        listOf(
            Offset(-.38f,-.31f),
            Offset(.38f,.30f),
            Offset(.36f,-.30f),
            Offset(-.36f,.31f),
            Offset(.01f,.02f)
        )

    repeat(
        a.animationQuantity
            .coerceIn(1,5)
    ){index->
        val home=homes[index]

        val mx=when(index){
            0->motion.x
            1->motion.z
            2->-motion.y
            3->-motion.x
            else->motion.y
        }

        val my=when(index){
            0->motion.y
            1->-motion.x
            2->motion.z
            3->-motion.z
            else->-motion.x
        }

        val direction=
            if(index%2==0)
                1f
            else
                -1f

        if(soft){
            val shape=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        CircleShape

                    NmixAnimationName.ORBIT->
                        RoundedCornerShape(
                            90.dp
                        )

                    NmixAnimationName.FLOW->
                        RoundedCornerShape(
                            48.dp
                        )

                    else->
                        CircleShape
                }

            val size=
                when(index){
                    0->880f
                    1->710f
                    2->620f
                    3->680f
                    else->540f
                }

            Box(
                Modifier
                    .size(size.dp)
                    .align(Alignment.Center)
                    .offset(
                        x=(home.x*470f).dp,
                        y=(home.y*430f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    270f+
                                        index*20f
                                )

                        translationY=
                            my*
                                (
                                    195f+
                                        index*16f
                                )

                        scaleX=
                            motion.pulse

                        scaleY=
                            motion.pulse

                        rotationZ=
                            if(
                                a.animation==
                                    NmixAnimationName.ORBIT
                            ){
                                motion.z*
                                    16f*
                                    direction
                            }else{
                                0f
                            }
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    (
                                        if(index%2==0)
                                            p.accent
                                        else
                                            p.accentLight
                                    ).copy(
                                        alpha=
                                            if(a.darkMode)
                                                .34f
                                            else
                                                .25f
                                    ),

                                .32f to
                                    p.accent.copy(
                                        alpha=.19f
                                    ),

                                .60f to
                                    p.accent.copy(
                                        alpha=.085f
                                    ),

                                .82f to
                                    p.accent.copy(
                                        alpha=.026f
                                    ),

                                1f to
                                    Color.Transparent
                            )
                        ),
                        shape
                    )
            )
        }else{
            val size=
                when(index){
                    0->270f
                    1->220f
                    2->185f
                    3->205f
                    else->165f
                }

            Canvas(
                Modifier
                    .size(size.dp)
                    .align(Alignment.Center)
                    .offset(
                        x=(home.x*500f).dp,
                        y=(home.y*440f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    300f+
                                        index*17f
                                )

                        translationY=
                            my*
                                (
                                    210f+
                                        index*13f
                                )

                        rotationZ=
                            motion.z*
                                18f*
                                direction

                        if(
                            a.animation==
                                NmixAnimationName.PULSE
                        ){
                            scaleX=
                                motion.pulse

                            scaleY=
                                motion.pulse
                        }
                    }
            ){
                val color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val alpha=
                    if(a.darkMode)
                        .15f
                    else
                        .12f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.035f
                                ),
                            cornerRadius=
                                CornerRadius(
                                    30.dp.toPx()
                                )
                        )

                        val inset=
                            8.dp.toPx()

                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=alpha
                                ),
                            topLeft=
                                Offset(
                                    inset,
                                    inset
                                ),
                            size=
                                Size(
                                    size.width-inset*2,
                                    size.height-inset*2
                                ),
                            cornerRadius=
                                CornerRadius(
                                    24.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val path=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.06f
                                )

                                lineTo(
                                    size.width*.94f,
                                    size.height*.90f
                                )

                                lineTo(
                                    size.width*.06f,
                                    size.height*.90f
                                )

                                close()
                            }

                        drawPath(
                            path,
                            color.copy(
                                alpha=alpha
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val path=
                            Path().apply{
                                moveTo(
                                    size.width*.5f,
                                    size.height*.04f
                                )

                                lineTo(
                                    size.width*.96f,
                                    size.height*.5f
                                )

                                lineTo(
                                    size.width*.5f,
                                    size.height*.96f
                                )

                                lineTo(
                                    size.width*.04f,
                                    size.height*.5f
                                )

                                close()
                            }

                        drawPath(
                            path,
                            color.copy(
                                alpha=alpha
                            )
                        )
                    }

                    else->{}
                }
            }
        }
    }
}

/*
 * ==================================================
 * CAROUSEL
 * ==================================================
 */

@Composable
private fun ClockCarousel(
    title:String,
    options:List<String>,
    index:Int,
    width:Int,
    optionFont:FontFamily,
    colorMode:Boolean,
    onIndex:(Int)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val density=LocalDensity.current
    val scope=rememberCoroutineScope()
    val haptic=rememberNmixHapticAction()

    val slot=
        with(density){
            (width.dp/3f).toPx()
        }

    val drag=
        remember{
            Animatable(0f)
        }

    fun wrap(value:Int):Int{
        return (
            (value%options.size)+
                options.size
        )%options.size
    }

    val shape=
        RoundedCornerShape(16.dp)

    Column(
        Modifier
            .width(width.dp)
            .height(50.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.78f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.86f)
            )
            .background(
                p.accent.copy(
                    alpha=.045f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .16f
                        else
                            .24f
                ),
                shape
            )
            .pointerInput(
                index,
                options.size
            ){
                detectHorizontalDragGestures(
                    onDragStart={
                        scope.launch{
                            drag.stop()
                        }
                    },

                    onHorizontalDrag={
                        change,
                        amount->

                        change.consume()

                        scope.launch{
                            drag.snapTo(
                                (
                                    drag.value+
                                        amount
                                ).coerceIn(
                                    -slot,
                                    slot
                                )
                            )
                        }
                    },

                    onDragEnd={
                        val move=
                            when{
                                drag.value<
                                    -slot*.28f->1

                                drag.value>
                                    slot*.28f->-1

                                else->0
                            }

                        scope.launch{
                            if(move==0){
                                drag.animateTo(
                                    0f,
                                    tween(
                                        180,
                                        easing=EaseOutCubic
                                    )
                                )
                            }else{
                                drag.animateTo(
                                    if(move>0)
                                        -slot
                                    else
                                        slot,
                                    tween(
                                        180,
                                        easing=EaseOutCubic
                                    )
                                )

                                haptic{
                                    onIndex(
                                        wrap(
                                            index+move
                                        )
                                    )
                                }

                                drag.snapTo(0f)
                            }
                        }
                    }
                )
            },
        horizontalAlignment=
            Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=p.accent,
            fontSize=7.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.7.sp,
            fontFamily=a.fontFamily,
            modifier=
                Modifier.padding(
                    top=3.dp
                )
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment=
                Alignment.Center
        ){
            val progress=
                if(slot==0f)
                    0f
                else
                    drag.value/slot

            for(offset in -2..2){
                val position=
                    offset+progress

                if(abs(position)<1.65f){
                    val center=
                        1f-
                            abs(position)
                                .coerceIn(
                                    0f,
                                    1f
                                )

                    val itemColor=
                        if(
                            colorMode &&
                            center>.55f
                        ){
                            clockTones[
                                wrap(index+offset)
                            ].accent
                        }else if(
                            center>.55f
                        ){
                            ui.text
                        }else{
                            ui.muted
                        }

                    Box(
                        Modifier
                            .align(
                                Alignment.Center
                            )
                            .width(
                                (width/3).dp
                            )
                            .graphicsLayer{
                                translationX=
                                    position*slot

                                scaleX=
                                    .86f+
                                        center*.34f

                                scaleY=
                                    .86f+
                                        center*.34f

                                alpha=
                                    .46f+
                                        center*.54f
                            },
                        contentAlignment=
                            Alignment.Center
                    ){
                        Text(
                            options[
                                wrap(
                                    index+offset
                                )
                            ],
                            color=itemColor,
                            fontSize=10.sp,
                            fontWeight=
                                if(center>.55f)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                            fontFamily=optionFont,
                            maxLines=1,
                            textAlign=
                                TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/*
 * ==================================================
 * DISPLAY OPTIONS
 * ==================================================
 */

@Composable
private fun DisplayBar(
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

    val shape=
        RoundedCornerShape(16.dp)

    Row(
        Modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.90f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.94f)
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=.26f
                ),
                shape
            )
            .padding(10.dp),
        horizontalArrangement=
            Arrangement.spacedBy(7.dp)
    ){
        DisplayChoice(
            "H",
            hours,
            onHours
        )

        DisplayChoice(
            "M",
            minutes,
            onMinutes
        )

        DisplayChoice(
            "S",
            seconds,
            onSeconds
        )

        DisplayChoice(
            "D",
            date,
            onDate
        )
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

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        Modifier
            .size(38.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(selected)
                            .17f
                        else
                            .055f
                )
            )
            .border(
                if(selected)
                    1.dp
                else
                    .4.dp,
                p.accent.copy(
                    alpha=
                        if(selected)
                            .70f
                        else
                            .16f
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
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=9.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * PHOTO CONSENT
 * ==================================================
 */

@Composable
private fun PhotoConsentCard(
    hasWallpaper:Boolean,
    onCancel:()->Unit,
    onRemove:()->Unit,
    onChoose:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(20.dp)

    Column(
        Modifier
            .width(286.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                else
                    Color(0xFFECEFED)
            )
            .border(
                .55.dp,
                p.accent.copy(
                    alpha=.28f
                ),
                shape
            )
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
            "NMIX will open Android's image picker. Only the picture you choose is used as the Fullscreen Clock background.",
            color=ui.muted,
            fontSize=9.sp,
            lineHeight=14.sp,
            fontFamily=a.fontFamily
        )

        Spacer(Modifier.height(13.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            MiniClockButton(
                "Cancel",
                Modifier.weight(1f),
                onCancel
            )

            if(hasWallpaper){
                MiniClockButton(
                    "Remove",
                    Modifier.weight(1f),
                    onRemove,
                    red=true
                )
            }

            MiniClockButton(
                "Choose",
                Modifier.weight(1f),
                onChoose,
                accent=true
            )
        }
    }
}

/*
 * ==================================================
 * CLOCK FACE
 * ==================================================
 */

@Composable
private fun ClockFace(
    style:Int,
    parts:ClockParts,
    date:String,
    tone:ClockTone,
    font:FontFamily,
    landscape:Boolean,
    showHours:Boolean,
    showMinutes:Boolean,
    showSeconds:Boolean,
    showDate:Boolean,
    dark:Boolean
){
    @Composable
    fun MainTime(
        fontSize:
            androidx.compose.ui.unit.TextUnit
    ){
        val numeric=
            buildList{
                if(showHours){
                    add(parts.hour)
                }

                if(showMinutes){
                    add(parts.minute)
                }

                if(showSeconds){
                    add(parts.second)
                }
            }.joinToString(":")

        Row(
            verticalAlignment=
                Alignment.Bottom,
            horizontalArrangement=
                Arrangement.Center
        ){
            if(numeric.isNotEmpty()){
                Text(
                    numeric,
                    color=tone.main,
                    fontSize=fontSize,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    maxLines=1
                )
            }

            if(parts.period.isNotEmpty()){
                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    parts.period,
                    color=tone.accent,
                    fontSize=
                        if(landscape)
                            17.sp
                        else
                            14.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    modifier=
                        Modifier.padding(
                            bottom=8.dp
                        )
                )
            }
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment=
            Alignment.Center
    ){
        when(style){
            1->{
                Column(
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    MainTime(
                        if(landscape)
                            78.sp
                        else
                            57.sp
                    )

                    if(showDate){
                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Text(
                            date,
                            color=tone.accent,
                            fontSize=12.sp,
                            fontFamily=font
                        )
                    }
                }
            }

            2->{
                Row(
                    verticalAlignment=
                        Alignment.CenterVertically,
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            20.dp
                        )
                ){
                    Column(
                        horizontalAlignment=
                            Alignment.End
                    ){
                        if(showHours){
                            Text(
                                parts.hour,
                                color=tone.main,
                                fontSize=
                                    if(landscape)
                                        70.sp
                                    else
                                        55.sp,
                                fontWeight=FontWeight.Bold,
                                fontFamily=font
                            )
                        }

                        if(showMinutes){
                            Text(
                                parts.minute,
                                color=tone.accent,
                                fontSize=
                                    if(landscape)
                                        70.sp
                                    else
                                        55.sp,
                                fontWeight=FontWeight.Bold,
                                fontFamily=font
                            )
                        }
                    }

                    Column{
                        if(showSeconds){
                            Text(
                                parts.second,
                                color=tone.main,
                                fontSize=34.sp,
                                fontWeight=FontWeight.Bold,
                                fontFamily=font
                            )
                        }

                        Text(
                            parts.period,
                            color=tone.accent,
                            fontSize=12.sp,
                            fontWeight=FontWeight.Bold,
                            fontFamily=font
                        )

                        if(showDate){
                            Spacer(
                                Modifier.height(8.dp)
                            )

                            Text(
                                date,
                                color=
                                    tone.main.copy(
                                        alpha=.68f
                                    ),
                                fontSize=10.sp,
                                fontFamily=font
                            )
                        }
                    }
                }
            }

            3->{
                Column(
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    Text(
                        "FOCUS",
                        color=tone.accent,
                        fontSize=9.sp,
                        fontWeight=FontWeight.Bold,
                        letterSpacing=3.5.sp,
                        fontFamily=font
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    MainTime(
                        if(landscape)
                            82.sp
                        else
                            59.sp
                    )

                    if(showDate){
                        Spacer(
                            Modifier.height(9.dp)
                        )

                        Text(
                            date,
                            color=
                                tone.main.copy(
                                    alpha=.62f
                                ),
                            fontSize=10.sp,
                            fontFamily=font
                        )
                    }
                }
            }

            4->{
                Box(
                    Modifier.size(
                        if(landscape)
                            310.dp
                        else
                            272.dp
                    ),
                    contentAlignment=
                        Alignment.Center
                ){
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(
                                1.2.dp,
                                tone.accent.copy(
                                    alpha=.38f
                                ),
                                CircleShape
                            )
                    )

                    Box(
                        Modifier
                            .size(
                                if(landscape)
                                    248.dp
                                else
                                    218.dp
                            )
                            .border(
                                .7.dp,
                                tone.accent.copy(
                                    alpha=.18f
                                ),
                                CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment=
                            Alignment.CenterHorizontally
                    ){
                        MainTime(
                            if(landscape)
                                47.sp
                            else
                                36.sp
                        )

                        if(showDate){
                            Spacer(
                                Modifier.height(7.dp)
                            )

                            Text(
                                date,
                                color=tone.accent,
                                fontSize=8.sp,
                                fontFamily=font
                            )
                        }
                    }
                }
            }

            5->{
                Column(
                    horizontalAlignment=
                        Alignment.Start
                ){
                    Text(
                        "NMIX://LOCAL_CLOCK",
                        color=tone.accent,
                        fontSize=10.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font
                    )

                    Spacer(
                        Modifier.height(7.dp)
                    )

                    MainTime(
                        if(landscape)
                            67.sp
                        else
                            47.sp
                    )

                    if(showDate){
                        Text(
                            "> DATE  $date",
                            color=
                                tone.accent.copy(
                                    alpha=.82f
                                ),
                            fontSize=10.sp,
                            fontFamily=font
                        )
                    }
                }
            }

            6->{
                val shape=
                    RoundedCornerShape(50)

                Column(
                    Modifier.fillMaxWidth(
                        if(landscape)
                            .84f
                        else
                            .94f
                    ),
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(
                                if(landscape)
                                    112.dp
                                else
                                    96.dp
                            )
                            .clip(shape)
                            .background(
                                if(dark)
                                    Color.White.copy(
                                        alpha=.08f
                                    )
                                else
                                    Color.Black.copy(
                                        alpha=.055f
                                    )
                            )
                            .border(
                                .7.dp,
                                tone.accent.copy(
                                    alpha=.38f
                                ),
                                shape
                            ),
                        contentAlignment=
                            Alignment.Center
                    ){
                        MainTime(
                            if(landscape)
                                63.sp
                            else
                                44.sp
                        )
                    }

                    if(showDate){
                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Text(
                            date,
                            color=tone.accent,
                            fontSize=11.sp,
                            fontFamily=font
                        )
                    }
                }
            }

            7->{
                Column(
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    Text(
                        "LOCAL",
                        color=tone.accent,
                        fontSize=9.sp,
                        letterSpacing=4.sp,
                        fontFamily=font
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    MainTime(
                        if(landscape)
                            80.sp
                        else
                            57.sp
                    )

                    Box(
                        Modifier
                            .padding(top=9.dp)
                            .width(
                                if(landscape)
                                    330.dp
                                else
                                    250.dp
                            )
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        tone.accent,
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    if(showDate){
                        Spacer(
                            Modifier.height(9.dp)
                        )

                        Text(
                            date,
                            color=
                                tone.main.copy(
                                    alpha=.68f
                                ),
                            fontSize=11.sp,
                            fontFamily=font
                        )
                    }
                }
            }

            else->{
                Column(
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    Text(
                        "NMIX • LOCAL TIME",
                        color=tone.accent,
                        fontSize=10.sp,
                        letterSpacing=1.9.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font
                    )

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    MainTime(
                        if(landscape)
                            78.sp
                        else
                            57.sp
                    )

                    if(showDate){
                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Text(
                            date,
                            color=
                                tone.main.copy(
                                    alpha=.70f
                                ),
                            fontSize=12.sp,
                            fontFamily=font
                        )
                    }
                }
            }
        }
    }
}

/*
 * ==================================================
 * ACTION BUTTONS
 * ==================================================
 */

@Composable
private fun ClockAction(
    text:String,
    icon:NmixIcon,
    font:FontFamily,
    red:Boolean=false,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(14.dp)

    val foreground=
        if(red)
            Color(0xFFE66E75)
        else
            ui.text

    Row(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(
                if(red)
                    Color(0xFFD94F57)
                        .copy(alpha=.10f)
                else if(a.darkMode)
                    p.accent.copy(
                        alpha=.085f
                    )
                else
                    p.accent.copy(
                        alpha=.075f
                    )
            )
            .border(
                .45.dp,
                if(red)
                    foreground.copy(
                        alpha=.28f
                    )
                else
                    p.accent.copy(
                        alpha=
                            if(a.darkMode)
                                .16f
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
            )
            .padding(
                horizontal=10.dp
            ),
        verticalAlignment=
            Alignment.CenterVertically,
        horizontalArrangement=
            Arrangement.spacedBy(6.dp)
    ){
        NmixIcon(
            icon,
            Modifier.size(16.dp),
            if(red)
                foreground
            else
                p.accent
        )

        Text(
            text,
            color=foreground,
            fontSize=8.5.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=font
        )
    }
}

@Composable
private fun MiniClockButton(
    text:String,
    modifier:Modifier,
    onClick:()->Unit,
    accent:Boolean=false,
    red:Boolean=false
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(38.dp)
            .clip(shape)
            .background(
                when{
                    accent->
                        p.accent.copy(
                            alpha=.80f
                        )

                    red->
                        Color(0xFFD94F57)
                            .copy(alpha=.12f)

                    else->
                        p.accent.copy(
                            alpha=.06f
                        )
                }
            )
            .border(
                .45.dp,
                when{
                    accent->
                        p.accent.copy(
                            alpha=.48f
                        )

                    red->
                        Color(0xFFE66E75)
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
            color=
                when{
                    accent->Color.White
                    red->Color(0xFFE66E75)
                    else->ui.text
                },
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * BRAND / WALLPAPER
 * ==================================================
 */

@Composable
private fun ClockBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false,
    color:Color
){
    val a=LocalNmixAppearance.current

    Column(
        modifier,
        horizontalAlignment=
            if(centered)
                Alignment.CenterHorizontally
            else
                Alignment.Start
    ){
        Text(
            "EVERYTHING WITH NUMBERS",
            color=
                color.copy(
                    alpha=.58f
                ),
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

private fun parseClockTime(
    time:String
):ClockParts{
    val period=
        when{
            time.contains("AM")->"AM"
            time.contains("PM")->"PM"
            else->""
        }

    val raw=
        time
            .removeSuffix(" AM")
            .removeSuffix(" PM")

    val pieces=
        raw.split(":")

    return ClockParts(
        hour=
            pieces.getOrElse(0){
                "00"
            },
        minute=
            pieces.getOrElse(1){
                "00"
            },
        second=
            pieces.getOrElse(2){
                "00"
            },
        period=period
    )
}

@Composable
private fun CustomWallpaper(
    uri:Uri
){
    val context=
        LocalContext.current

    var bitmap by remember(uri){
        mutableStateOf<
            androidx.compose.ui.graphics.ImageBitmap?
        >(null)
    }

    LaunchedEffect(uri){
        bitmap=
            withContext(
                Dispatchers.IO
            ){
                try{
                    context
                        .contentResolver
                        .openInputStream(uri)
                        ?.use{
                            BitmapFactory
                                .decodeStream(it)
                                ?.asImageBitmap()
                        }
                }catch(
                    _:Exception
                ){
                    null
                }
            }
    }

    bitmap?.let{
        Image(
            bitmap=it,
            contentDescription=null,
            modifier=
                Modifier.fillMaxSize(),
            contentScale=
                ContentScale.Crop
        )
    }
}
