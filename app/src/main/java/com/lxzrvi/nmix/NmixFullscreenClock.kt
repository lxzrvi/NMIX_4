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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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

private const val CLOCK_PREFS=
    "nmix_fullscreen_clock"

private data class ClockParts(
    val hour:String,
    val minute:String,
    val second:String,
    val period:String
)

private val fullscreenStyles=
    listOf(
        "Digital",
        "Minimal",
        "Stack",
        "Focus"
    )

private val fullscreenFonts=
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

    /*
     * Preserve whatever orientation mode the
     * Activity had before entering fullscreen.
     */
    val originalOrientation=
        remember(activity){
            activity?.requestedOrientation
                ?:ActivityInfo
                    .SCREEN_ORIENTATION_UNSPECIFIED
        }

    val initialFont=
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
                initialFont
            ).coerceIn(
                0,
                fullscreenFonts.lastIndex
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
                fullscreenStyles.lastIndex
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

    var clean by remember{
        mutableStateOf(false)
    }

    var displayOptions by remember{
        mutableStateOf(false)
    }

    var wallpaperConsent by remember{
        mutableStateOf(false)
    }

    var customWallpaperString by remember{
        mutableStateOf(
            prefs.getString(
                "custom_wallpaper",
                null
            )
        )
    }

    val customWallpaper=
        customWallpaperString
            ?.let(Uri::parse)

    fun saveBoolean(
        key:String,
        value:Boolean
    ){
        prefs.edit()
            .putBoolean(
                key,
                value
            )
            .apply()
    }

    val imagePicker=
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .GetContent()
        ){uri->
            if(uri!=null){
                customWallpaperString=
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

    val parts=
        parseFullscreenTime(time)

    val background=
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
                    p.accent.copy(
                        alpha=.28f
                    ),
                    ui.page,
                    p.accentLight.copy(
                        alpha=.18f
                    )
                )
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(0.dp)
            )
            .background(background)
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null
            ){
                when{
                    wallpaperConsent->
                        wallpaperConsent=false

                    displayOptions->
                        displayOptions=false

                    clean->
                        clean=false
                }
            }
    ){
        if(customWallpaper!=null){
            FullscreenWallpaper(
                customWallpaper
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if(a.darkMode)
                            Color.Black.copy(
                                alpha=.22f
                            )
                        else
                            Color.White.copy(
                                alpha=.11f
                            )
                    )
            )
        }else{
            if(a.animationEnabled){
                FullscreenSharedMotion()
            }else{
                Box(
                    Modifier
                        .size(760.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.radialGradient(
                                colorStops=arrayOf(
                                    0f to
                                        p.accent.copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .22f
                                                else
                                                    .17f
                                        ),

                                    .55f to
                                        p.accent.copy(
                                            alpha=.055f
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
            }
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
                fadeIn(tween(280)),
            exit=
                fadeOut(tween(190))
        ){
            FullscreenBrand(
                modifier=
                    Modifier.padding(
                        start=22.dp,
                        top=18.dp
                    )
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
                fadeIn(tween(280)),
            exit=
                fadeOut(tween(190))
        ){
            if(landscape){
                Row(
                    Modifier.padding(
                        top=17.dp,
                        end=14.dp
                    ),
                    horizontalArrangement=
                        Arrangement.spacedBy(
                            7.dp
                        )
                ){
                    FullscreenSelector(
                        title="FONT",
                        options=
                            fullscreenFonts,
                        selected=
                            fontIndex,
                        font=font
                    ){
                        fontIndex=it

                        prefs.edit()
                            .putInt(
                                "font",
                                it
                            )
                            .apply()
                    }

                    FullscreenSelector(
                        title="STYLE",
                        options=
                            fullscreenStyles,
                        selected=
                            styleIndex,
                        font=font
                    ){
                        styleIndex=it

                        prefs.edit()
                            .putInt(
                                "style",
                                it
                            )
                            .apply()
                    }
                }
            }else{
                Column(
                    Modifier.padding(
                        top=17.dp,
                        end=10.dp
                    ),
                    verticalArrangement=
                        Arrangement.spacedBy(
                            6.dp
                        )
                ){
                    FullscreenSelector(
                        title="FONT",
                        options=
                            fullscreenFonts,
                        selected=
                            fontIndex,
                        font=font
                    ){
                        fontIndex=it

                        prefs.edit()
                            .putInt(
                                "font",
                                it
                            )
                            .apply()
                    }

                    FullscreenSelector(
                        title="STYLE",
                        options=
                            fullscreenStyles,
                        selected=
                            styleIndex,
                        font=font
                    ){
                        styleIndex=it

                        prefs.edit()
                            .putInt(
                                "style",
                                it
                            )
                            .apply()
                    }
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.Center)
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
                        360.dp
                ),
            contentAlignment=
                Alignment.Center
        ){
            AnimatedContent(
                targetState=
                    styleIndex,
                transitionSpec={
                    (
                        fadeIn(
                            tween(
                                340,
                                easing=EaseOutCubic
                            )
                        )+
                        scaleIn(
                            initialScale=.975f,
                            animationSpec=tween(
                                360,
                                easing=EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(tween(210))+
                        scaleOut(
                            targetScale=1.012f,
                            animationSpec=tween(250)
                        )
                    )
                },
                label="fullscreenStyle"
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
                    showDate=showDate
                )
            }

            AnimatedVisibility(
                visible=clean,
                modifier=
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ){
                FullscreenBrand(
                    modifier=
                        Modifier.padding(
                            bottom=7.dp
                        ),
                    centered=true
                )
            }
        }

        AnimatedVisibility(
            visible=
                !clean &&
                displayOptions,
            modifier=
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
                    .padding(
                        bottom=76.dp
                    ),
            enter=
                fadeIn(tween(230))+
                scaleIn(
                    initialScale=.97f
                ),
            exit=
                fadeOut(tween(170))+
                scaleOut(
                    targetScale=.98f
                )
        ){
            FullscreenDisplayOptions(
                hours=showHours,
                minutes=showMinutes,
                seconds=showSeconds,
                date=showDate,

                onHours={
                    haptic{
                        showHours=
                            !showHours

                        saveBoolean(
                            "hours",
                            showHours
                        )
                    }
                },

                onMinutes={
                    haptic{
                        showMinutes=
                            !showMinutes

                        saveBoolean(
                            "minutes",
                            showMinutes
                        )
                    }
                },

                onSeconds={
                    haptic{
                        showSeconds=
                            !showSeconds

                        saveBoolean(
                            "seconds",
                            showSeconds
                        )
                    }
                },

                onDate={
                    haptic{
                        showDate=
                            !showDate

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
                fadeIn(tween(280)),
            exit=
                fadeOut(tween(180))
        ){
            Row(
                Modifier.padding(
                    start=10.dp,
                    end=10.dp,
                    bottom=18.dp
                ),
                horizontalArrangement=
                    Arrangement.spacedBy(
                        7.dp
                    ),
                verticalAlignment=
                    Alignment.CenterVertically
            ){
                FullscreenAction(
                    "Wallpaper",
                    NmixIcon.WALLPAPER,
                    font
                ){
                    haptic{
                        wallpaperConsent=true
                    }
                }

                FullscreenAction(
                    "Rotate",
                    NmixIcon.ROTATE,
                    font
                ){
                    haptic{
                        activity
                            ?.requestedOrientation=
                            if(landscape){
                                ActivityInfo
                                    .SCREEN_ORIENTATION_PORTRAIT
                            }else{
                                ActivityInfo
                                    .SCREEN_ORIENTATION_LANDSCAPE
                            }
                    }
                }

                FullscreenAction(
                    "Display",
                    NmixIcon.CLOCK,
                    font
                ){
                    haptic{
                        displayOptions=
                            !displayOptions
                    }
                }

                FullscreenAction(
                    "Clean",
                    NmixIcon.FULLSCREEN,
                    font
                ){
                    haptic{
                        displayOptions=false
                        clean=true
                    }
                }

                FullscreenAction(
                    "Exit",
                    NmixIcon.CLOSE,
                    font,
                    red=true
                ){
                    haptic{
                        activity
                            ?.requestedOrientation=
                            originalOrientation

                        onExit()
                    }
                }
            }
        }

        AnimatedVisibility(
            visible=wallpaperConsent,
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
            WallpaperConsent(
                hasWallpaper=
                    customWallpaper!=null,

                onCancel={
                    wallpaperConsent=false
                },

                onRemove={
                    customWallpaperString=null

                    prefs.edit()
                        .remove(
                            "custom_wallpaper"
                        )
                        .apply()

                    wallpaperConsent=false
                },

                onChoose={
                    wallpaperConsent=false

                    imagePicker.launch(
                        "image/*"
                    )
                }
            )
        }
    }
}

/*
 * ==================================================
 * SHARED ANIMATION
 * ==================================================
 */

@Composable
private fun BoxScope.FullscreenSharedMotion(){
    val a=LocalNmixAppearance.current
    val p=a.palette

    if(!a.animationEnabled){
        return
    }

    val motion=
        rememberNmixMotion(
            "fullscreenMotion"
        )

    val soft=
        a.animation in listOf(
            NmixAnimationName.DRIFT,
            NmixAnimationName.ORBIT,
            NmixAnimationName.FLOW
        )

    val homes=
        listOf(
            Offset(-.38f,-.30f),
            Offset(.38f,.30f),
            Offset(.36f,-.30f),
            Offset(-.36f,.31f),
            Offset(.01f,.02f)
        )

    repeat(
        a.animationQuantity
            .coerceIn(1,5)
    ){index->
        val home=
            homes[index]

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
            val elementShape=
                when(a.animation){
                    NmixAnimationName.DRIFT->
                        CircleShape

                    NmixAnimationName.ORBIT->
                        RoundedCornerShape(
                            92.dp
                        )

                    NmixAnimationName.FLOW->
                        RoundedCornerShape(
                            50.dp
                        )

                    else->
                        CircleShape
                }

            val elementSize=
                when(index){
                    0->900f
                    1->735f
                    2->630f
                    3->690f
                    else->550f
                }

            Box(
                Modifier
                    .size(elementSize.dp)
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=(home.x*480f).dp,
                        y=(home.y*440f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    280f+
                                        index*20f
                                )

                        translationY=
                            my*
                                (
                                    200f+
                                        index*15f
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
                                                .35f
                                            else
                                                .27f
                                    ),

                                .30f to
                                    p.accent.copy(
                                        alpha=.19f
                                    ),

                                .58f to
                                    p.accent.copy(
                                        alpha=.085f
                                    ),

                                .80f to
                                    p.accent.copy(
                                        alpha=.025f
                                    ),

                                1f to
                                    Color.Transparent
                            )
                        ),
                        elementShape
                    )
            )
        }else{
            val elementSize=
                when(index){
                    0->280f
                    1->230f
                    2->190f
                    3->210f
                    else->170f
                }

            Canvas(
                Modifier
                    .size(elementSize.dp)
                    .align(
                        Alignment.Center
                    )
                    .offset(
                        x=(home.x*500f).dp,
                        y=(home.y*445f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*
                                (
                                    300f+
                                        index*18f
                                )

                        translationY=
                            my*
                                (
                                    215f+
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
                val canvasWidth=
                    this.size.width

                val canvasHeight=
                    this.size.height

                val elementColor=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val elementAlpha=
                    if(a.darkMode)
                        .15f
                    else
                        .12f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        drawRoundRect(
                            color=
                                elementColor.copy(
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
                                elementColor.copy(
                                    alpha=
                                        elementAlpha
                                ),
                            topLeft=
                                Offset(
                                    inset,
                                    inset
                                ),
                            size=
                                androidx.compose.ui.geometry.Size(
                                    (
                                        canvasWidth-
                                            inset*2f
                                    ).coerceAtLeast(
                                        0f
                                    ),

                                    (
                                        canvasHeight-
                                            inset*2f
                                    ).coerceAtLeast(
                                        0f
                                    )
                                ),
                            cornerRadius=
                                CornerRadius(
                                    24.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val triangle=
                            Path().apply{
                                moveTo(
                                    canvasWidth*.50f,
                                    canvasHeight*.06f
                                )

                                lineTo(
                                    canvasWidth*.94f,
                                    canvasHeight*.90f
                                )

                                lineTo(
                                    canvasWidth*.06f,
                                    canvasHeight*.90f
                                )

                                close()
                            }

                        drawPath(
                            triangle,
                            elementColor.copy(
                                alpha=
                                    elementAlpha
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val diamond=
                            Path().apply{
                                moveTo(
                                    canvasWidth*.50f,
                                    canvasHeight*.04f
                                )

                                lineTo(
                                    canvasWidth*.96f,
                                    canvasHeight*.50f
                                )

                                lineTo(
                                    canvasWidth*.50f,
                                    canvasHeight*.96f
                                )

                                lineTo(
                                    canvasWidth*.04f,
                                    canvasHeight*.50f
                                )

                                close()
                            }

                        drawPath(
                            diamond,
                            elementColor.copy(
                                alpha=
                                    elementAlpha
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
 * SELECTORS
 * ==================================================
 */

@Composable
private fun FullscreenSelector(
    title:String,
    options:List<String>,
    selected:Int,
    font:FontFamily,
    onSelect:(Int)->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()
    val haptic=
        rememberNmixHapticAction()

    val safe=
        selected.coerceIn(
            0,
            options.lastIndex
        )

    val previous=
        if(safe<=0)
            options.lastIndex
        else
            safe-1

    val next=
        if(safe>=options.lastIndex)
            0
        else
            safe+1

    val shape=
        RoundedCornerShape(14.dp)

    Column(
        Modifier
            .width(166.dp)
            .height(50.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.80f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.88f)
            )
            .background(
                p.accent.copy(
                    alpha=.04f
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
            ),
        horizontalAlignment=
            Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=p.accent,
            fontSize=6.8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily,
            modifier=
                Modifier.padding(
                    top=3.dp
                )
        )

        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            SelectorOption(
                text=options[previous],
                modifier=
                    Modifier.weight(1f),
                color=ui.muted,
                font=font
            ){
                haptic{
                    onSelect(previous)
                }
            }

            SelectorOption(
                text=options[safe],
                modifier=
                    Modifier.weight(1.25f),
                color=ui.text,
                font=font,
                strong=true
            ){
                haptic{
                    onSelect(next)
                }
            }

            SelectorOption(
                text=options[next],
                modifier=
                    Modifier.weight(1f),
                color=ui.muted,
                font=font
            ){
                haptic{
                    onSelect(next)
                }
            }
        }
    }
}

@Composable
private fun SelectorOption(
    text:String,
    modifier:Modifier,
    color:Color,
    font:FontFamily,
    strong:Boolean=false,
    onClick:()->Unit
){
    Box(
        modifier
            .fillMaxHeight()
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
            color=color,
            fontSize=
                if(strong)
                    9.5.sp
                else
                    7.5.sp,
            fontWeight=
                if(strong)
                    FontWeight.Bold
                else
                    FontWeight.Normal,
            fontFamily=font,
            maxLines=1,
            textAlign=
                TextAlign.Center
        )
    }
}

/*
 * ==================================================
 * CLOCK FACE
 * ==================================================
 */

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
    showDate:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val mainColor=
        if(a.darkMode)
            Color.White.copy(alpha=.94f)
        else
            ui.text

    val secondary=
        if(a.darkMode)
            p.accentLight
        else
            p.accentDark

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

    when(style){
        1->{
            Column(
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Text(
                    numeric,
                    color=mainColor,
                    fontSize=
                        if(landscape)
                            78.sp
                        else
                            56.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font,
                    maxLines=1
                )

                Text(
                    parts.period,
                    color=secondary,
                    fontSize=13.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font
                )

                if(showDate){
                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Text(
                        date,
                        color=
                            mainColor.copy(
                                alpha=.65f
                            ),
                        fontSize=11.sp,
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
                Column{
                    if(showHours){
                        Text(
                            parts.hour,
                            color=mainColor,
                            fontSize=
                                if(landscape)
                                    68.sp
                                else
                                    52.sp,
                            fontWeight=FontWeight.Bold,
                            fontFamily=font
                        )
                    }

                    if(showMinutes){
                        Text(
                            parts.minute,
                            color=secondary,
                            fontSize=
                                if(landscape)
                                    68.sp
                                else
                                    52.sp,
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
                                mainColor.copy(
                                    alpha=.60f
                                ),
                            fontSize=9.sp,
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
                    color=secondary,
                    fontSize=9.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=3.5.sp,
                    fontFamily=font
                )

                Spacer(
                    Modifier.height(9.dp)
                )

                Row(
                    verticalAlignment=
                        Alignment.Bottom
                ){
                    Text(
                        numeric,
                        color=mainColor,
                        fontSize=
                            if(landscape)
                                82.sp
                            else
                                58.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.width(7.dp)
                    )

                    Text(
                        parts.period,
                        color=secondary,
                        fontSize=13.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font,
                        modifier=
                            Modifier.padding(
                                bottom=8.dp
                            )
                    )
                }

                if(showDate){
                    Spacer(
                        Modifier.height(9.dp)
                    )

                    Text(
                        date,
                        color=
                            mainColor.copy(
                                alpha=.60f
                            ),
                        fontSize=10.sp,
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
                    color=secondary,
                    fontSize=10.sp,
                    letterSpacing=1.9.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                Row(
                    verticalAlignment=
                        Alignment.Bottom
                ){
                    Text(
                        numeric,
                        color=mainColor,
                        fontSize=
                            if(landscape)
                                78.sp
                            else
                                56.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        parts.period,
                        color=secondary,
                        fontSize=13.sp,
                        fontWeight=FontWeight.Bold,
                        fontFamily=font,
                        modifier=
                            Modifier.padding(
                                bottom=8.dp
                            )
                    )
                }

                if(showDate){
                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Text(
                        date,
                        color=
                            mainColor.copy(
                                alpha=.68f
                            ),
                        fontSize=12.sp,
                        fontFamily=font
                    )
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
private fun FullscreenDisplayOptions(
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
                        .copy(alpha=.94f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.96f)
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=.25f
                ),
                shape
            )
            .padding(10.dp),
        horizontalArrangement=
            Arrangement.spacedBy(7.dp)
    ){
        FullscreenDisplayChoice(
            "H",
            hours,
            onHours
        )

        FullscreenDisplayChoice(
            "M",
            minutes,
            onMinutes
        )

        FullscreenDisplayChoice(
            "S",
            seconds,
            onSeconds
        )

        FullscreenDisplayChoice(
            "D",
            date,
            onDate
        )
    }
}

@Composable
private fun FullscreenDisplayChoice(
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
 * ACTIONS / CONSENT
 * ==================================================
 */

@Composable
private fun FullscreenAction(
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
private fun WallpaperConsent(
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

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            "Allow NMIX to open Android's image picker? Only the image you choose will be used for the Fullscreen Clock.",
            color=ui.muted,
            fontSize=9.sp,
            lineHeight=14.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(13.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(7.dp)
        ){
            ConsentButton(
                text="Cancel",
                modifier=
                    Modifier.weight(1f),
                onClick=onCancel
            )

            if(hasWallpaper){
                ConsentButton(
                    text="Remove",
                    modifier=
                        Modifier.weight(1f),
                    red=true,
                    onClick=onRemove
                )
            }

            ConsentButton(
                text="Choose",
                modifier=
                    Modifier.weight(1f),
                accent=true,
                onClick=onChoose
            )
        }
    }
}

@Composable
private fun ConsentButton(
    text:String,
    modifier:Modifier,
    accent:Boolean=false,
    red:Boolean=false,
    onClick:()->Unit
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
 * BRAND / PARSING / IMAGE
 * ==================================================
 */

@Composable
private fun FullscreenBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false
){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()

    val color=
        if(a.darkMode)
            Color.White
        else
            ui.text

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

private fun parseFullscreenTime(
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
private fun FullscreenWallpaper(
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
