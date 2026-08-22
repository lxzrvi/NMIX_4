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
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val context=LocalContext.current
    val activity=LocalActivity.current
    val configuration=LocalConfiguration.current

    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val prefs=remember(context){
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
        customWallpaperString?.let(
            Uri::parse
        )

    fun saveBoolean(
        key:String,
        value:Boolean
    ){
        prefs.edit()
            .putBoolean(key,value)
            .apply()
    }

    val imagePicker=
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
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
        val window=activity?.window

        if(window!=null){
            WindowCompat.setDecorFitsSystemWindows(
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
            /*
             * Fullscreen rotation never survives
             * leaving this composable.
             */
            activity?.requestedOrientation=
                originalOrientation

            if(window!=null){
                WindowCompat.setDecorFitsSystemWindows(
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
            /*
             * Much more opaque light background.
             * No washed-through main screen.
             */
            Brush.verticalGradient(
                colorStops=arrayOf(
                    0f to Color(0xFFF9FAF9),
                    .20f to
                        mixClockColor(
                            Color(0xFFF9FAF9),
                            p.accent,
                            .16f
                        ),
                    .50f to Color(0xFFF4F7F5),
                    .76f to
                        mixClockColor(
                            Color(0xFFF7F9F8),
                            p.accent,
                            .12f
                        ),
                    1f to Color(0xFFF9FAF9)
                )
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                interactionSource=
                    remember{
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
                                alpha=.34f
                            )
                        else
                            Color.White.copy(
                                alpha=.58f
                            )
                    )
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .035f
                                else
                                    .075f
                        )
                    )
            )
        }else{
            if(a.animationEnabled){
                FullscreenSharedMotion()
            }else{
                Box(
                    Modifier
                        .size(780.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.radialGradient(
                                colorStops=arrayOf(
                                    0f to
                                        p.accent.copy(
                                            alpha=
                                                if(a.darkMode)
                                                    .24f
                                                else
                                                    .20f
                                        ),
                                    .52f to
                                        p.accent.copy(
                                            alpha=.065f
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
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    ),
            enter=fadeIn(tween(280)),
            exit=fadeOut(tween(190))
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
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    ),
            enter=fadeIn(tween(280)),
            exit=fadeOut(tween(190))
        ){
            if(landscape){
                Row(
                    Modifier.padding(
                        top=17.dp,
                        end=14.dp
                    ),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    FullscreenSelector(
                        title="FONT",
                        options=fullscreenFonts,
                        selected=fontIndex,
                        font=font
                    ){
                        fontIndex=it

                        prefs.edit()
                            .putInt("font",it)
                            .apply()
                    }

                    FullscreenSelector(
                        title="STYLE",
                        options=fullscreenStyles,
                        selected=styleIndex,
                        font=font
                    ){
                        styleIndex=it

                        prefs.edit()
                            .putInt("style",it)
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
                        Arrangement.spacedBy(6.dp)
                ){
                    FullscreenSelector(
                        title="FONT",
                        options=fullscreenFonts,
                        selected=fontIndex,
                        font=font
                    ){
                        fontIndex=it

                        prefs.edit()
                            .putInt("font",it)
                            .apply()
                    }

                    FullscreenSelector(
                        title="STYLE",
                        options=fullscreenStyles,
                        selected=styleIndex,
                        font=font
                    ){
                        styleIndex=it

                        prefs.edit()
                            .putInt("style",it)
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
            contentAlignment=Alignment.Center
        ){
            AnimatedContent(
                targetState=styleIndex,
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
                    .padding(bottom=76.dp),
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
            enter=fadeIn(tween(280)),
            exit=fadeOut(tween(180))
        ){
            Row(
                Modifier.padding(
                    start=10.dp,
                    end=10.dp,
                    bottom=18.dp
                ),
                horizontalArrangement=
                    Arrangement.spacedBy(7.dp),
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
                        activity?.requestedOrientation=
                            if(landscape)
                                ActivityInfo
                                    .SCREEN_ORIENTATION_PORTRAIT
                            else
                                ActivityInfo
                                    .SCREEN_ORIENTATION_LANDSCAPE
                    }
                }

                FullscreenAction(
                    "Display",
                    NmixIcon.CLOCK,
                    font,
                    selected=displayOptions
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
                        activity?.requestedOrientation=
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
                    .align(Alignment.Center)
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
                    imagePicker.launch("image/*")
                }
            )
        }
    }
}

/*
 * ==================================================
 * ANIMATION
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

    val homes=listOf(
        Offset(-.43f,-.35f),
        Offset(.43f,.35f),
        Offset(.42f,-.35f),
        Offset(-.42f,.35f),
        Offset(0f,0f)
    )

    repeat(
        a.animationQuantity.coerceIn(1,5)
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
            val size=
                when(index){
                    0->1080f
                    1->910f
                    2->790f
                    3->850f
                    else->690f
                }

            Box(
                Modifier
                    .size(size.dp)
                    .align(Alignment.Center)
                    .offset(
                        x=(home.x*590f).dp,
                        y=(home.y*520f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*(390f+index*27f)

                        translationY=
                            my*(285f+index*21f)

                        scaleX=motion.pulse
                        scaleY=motion.pulse

                        rotationZ=
                            when(a.animation){
                                NmixAnimationName.ORBIT->
                                    motion.z*
                                        19f*
                                        direction

                                NmixAnimationName.FLOW->
                                    motion.x*
                                        12f*
                                        direction

                                else->0f
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
                                                .26f
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
                        CircleShape
                    )
            )
        }else{
            val size=
                when(index){
                    0->370f
                    1->320f
                    2->280f
                    3->300f
                    else->250f
                }

            Canvas(
                Modifier
                    .size(size.dp)
                    .align(Alignment.Center)
                    .offset(
                        x=(home.x*610f).dp,
                        y=(home.y*525f).dp
                    )
                    .graphicsLayer{
                        translationX=
                            mx*(410f+index*25f)

                        translationY=
                            my*(300f+index*19f)

                        rotationZ=
                            motion.z*
                                22f*
                                direction

                        if(
                            a.animation==
                                NmixAnimationName.PULSE
                        ){
                            scaleX=motion.pulse
                            scaleY=motion.pulse
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
                        .16f
                    else
                        .13f

                when(a.animation){
                    NmixAnimationName.FLOAT->{
                        drawRoundRect(
                            color=
                                color.copy(
                                    alpha=.035f
                                ),
                            cornerRadius=
                                CornerRadius(
                                    34.dp.toPx()
                                )
                        )

                        val inset=
                            9.dp.toPx()

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
                                androidx.compose.ui.geometry
                                    .Size(
                                        (
                                            size.width-
                                                inset*2
                                        ).coerceAtLeast(
                                            0f
                                        ),
                                        (
                                            size.height-
                                                inset*2
                                        ).coerceAtLeast(
                                            0f
                                        )
                                    ),
                            cornerRadius=
                                CornerRadius(
                                    27.dp.toPx()
                                )
                        )
                    }

                    NmixAnimationName.PULSE->{
                        val triangle=
                            Path().apply{
                                moveTo(
                                    size.width*.50f,
                                    size.height*.04f
                                )
                                lineTo(
                                    size.width*.96f,
                                    size.height*.92f
                                )
                                lineTo(
                                    size.width*.04f,
                                    size.height*.92f
                                )
                                close()
                            }

                        drawPath(
                            triangle,
                            color.copy(alpha=alpha)
                        )

                        drawPath(
                            triangle,
                            color.copy(
                                alpha=.12f
                            ),
                            style=Stroke(
                                2.dp.toPx()
                            )
                        )
                    }

                    NmixAnimationName.CROSS->{
                        val diamond=
                            Path().apply{
                                moveTo(
                                    size.width*.50f,
                                    size.height*.03f
                                )
                                lineTo(
                                    size.width*.97f,
                                    size.height*.50f
                                )
                                lineTo(
                                    size.width*.50f,
                                    size.height*.97f
                                )
                                lineTo(
                                    size.width*.03f,
                                    size.height*.50f
                                )
                                close()
                            }

                        drawPath(
                            diamond,
                            color.copy(alpha=alpha)
                        )

                        drawPath(
                            diamond,
                            color.copy(
                                alpha=.10f
                            ),
                            style=Stroke(
                                2.dp.toPx()
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
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

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
                clockSurface(a.darkMode)
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
                Modifier.padding(top=3.dp)
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
                modifier=Modifier.weight(1f),
                color=ui.muted,
                font=font
            ){
                haptic{
                    onSelect(previous)
                }
            }

            SelectorOption(
                text=options[safe],
                modifier=Modifier.weight(1.25f),
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
                modifier=Modifier.weight(1f),
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
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
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
            textAlign=TextAlign.Center
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

    /*
     * Period is independent from Seconds.
     * AM/PM remains visible regardless of S toggle.
     */
    val period=parts.period

    when(style){
        1->{
            Column(
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Text(
                    numeric.ifEmpty{"--"},
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
                    period,
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
                    Arrangement.spacedBy(20.dp)
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
                            fontWeight=
                                FontWeight.Bold,
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
                            fontWeight=
                                FontWeight.Bold,
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
                            fontWeight=
                                FontWeight.Bold,
                            fontFamily=font
                        )
                    }

                    Text(
                        period,
                        color=secondary,
                        fontSize=12.sp,
                        fontWeight=
                            FontWeight.Bold,
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
                        numeric.ifEmpty{"--"},
                        color=mainColor,
                        fontSize=
                            if(landscape)
                                82.sp
                            else
                                58.sp,
                        fontWeight=
                            FontWeight.Bold,
                        fontFamily=font,
                        maxLines=1
                    )

                    Spacer(
                        Modifier.width(7.dp)
                    )

                    Text(
                        period,
                        color=secondary,
                        fontSize=13.sp,
                        fontWeight=
                            FontWeight.Bold,
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
                        numeric.ifEmpty{"--"},
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
                        period,
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
                clockSurface(a.darkMode)
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
                            .17f
                        else
                            .25f
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

    val selection by animateFloatAsState(
        targetValue=
            if(selected)
                1f
            else
                0f,
        animationSpec=tween(220),
        label="clockChoice"
    )

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        Modifier
            .size(38.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF111614)
                        .copy(alpha=.92f)
                else
                    Color.White
                        .copy(alpha=.92f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .035f+
                                selection*.06f
                        else
                            .02f+
                                selection*.04f
                )
            )
            .border(
                (
                    .4f+
                        selection*.6f
                ).dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f+
                                selection*.36f
                        else
                            .20f+
                                selection*.34f
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
        contentAlignment=Alignment.Center
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
 * ACTIONS
 * ==================================================
 */

@Composable
private fun FullscreenAction(
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
                if(a.darkMode)
                    Color(0xFF131816)
                        .copy(alpha=.93f)
                else
                    Color.White
                        .copy(alpha=.93f)
            )
            .background(
                when{
                    red->
                        Color(0xFFD94F57)
                            .copy(alpha=.055f)

                    selected->
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .085f
                                else
                                    .055f
                        )

                    else->
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .035f
                                else
                                    .02f
                        )
                }
            )
            .border(
                if(selected)
                    .9.dp
                else
                    .45.dp,
                when{
                    red->
                        foreground.copy(
                            alpha=.28f
                        )

                    selected->
                        p.accent.copy(
                            alpha=.48f
                        )

                    else->
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .15f
                                else
                                    .23f
                        )
                },
                shape
            )
            .clickable(
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null,
                onClick=onClick
            )
            .padding(horizontal=10.dp),
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
                    Color(0xFFF8F9F8)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .035f
                        else
                            .025f
                )
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
                            alpha=.82f
                        )

                    red->
                        Color(0xFFD94F57)
                            .copy(alpha=.11f)

                    a.darkMode->
                        Color(0xFF111614)
                            .copy(alpha=.91f)

                    else->
                        Color.White
                            .copy(alpha=.91f)
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
                            alpha=
                                if(a.darkMode)
                                    .15f
                                else
                                    .22f
                        )
                },
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
        contentAlignment=Alignment.Center
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
 * BRAND / HELPERS
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

private fun mixClockColor(
    first:Color,
    second:Color,
    amount:Float
):Color{
    val t=
        amount.coerceIn(0f,1f)

    return Color(
        red=
            first.red+
                (
                    second.red-
                        first.red
                )*t,

        green=
            first.green+
                (
                    second.green-
                        first.green
                )*t,

        blue=
            first.blue+
                (
                    second.blue-
                        first.blue
                )*t,

        alpha=1f
    )
}

private fun clockSurface(
    dark:Boolean
):Color{
    return if(dark)
        Color(0xFF151A18)
            .copy(alpha=.91f)
    else
        Color.White
            .copy(alpha=.93f)
}

@Composable
private fun FullscreenWallpaper(
    uri:Uri
){
    val context=LocalContext.current

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
            contentScale=ContentScale.Crop
        )
    }
}
