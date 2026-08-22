package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private const val CLOCK_PREFS = "nmix_fullscreen_clock"

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

private val clockTones=listOf(
    ClockTone(
        "White",
        Color(0xFFF7F8F7),
        Color(0xFFFFFFFF)
    ),
    ClockTone(
        "Black",
        Color(0xFF111311),
        Color(0xFF252825)
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

private val clockStyles=listOf(
    "Digital",
    "Minimal",
    "Stack",
    "Cards",
    "Orbit",
    "Terminal",
    "Capsule",
    "Studio"
)

private val clockFonts=listOf(
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
    val a=LocalNmixAppearance.current
    val activity=LocalActivity.current
    val configuration=LocalConfiguration.current

    val prefs=remember(context){
        context.getSharedPreferences(
            CLOCK_PREFS,
            Context.MODE_PRIVATE
        )
    }

    val landscape=
        configuration.orientation==
            Configuration.ORIENTATION_LANDSCAPE

    val defaultFont=when(a.font){
        NmixFontName.INTER->0
        NmixFontName.NUNITO->1
        NmixFontName.OUTFIT->2
        NmixFontName.POPPINS->3
        NmixFontName.QUICKSAND->4
    }

    val defaultColor=
        if(a.darkMode)0 else 1

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
                defaultColor
            ).coerceIn(
                0,
                clockTones.lastIndex
            )
        )
    }

    var wallpaperIndex by remember{
        mutableIntStateOf(
            prefs.getInt(
                "wallpaper",
                a.theme.ordinal
            ).coerceIn(
                0,
                NmixThemeName.entries.lastIndex
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

    var showPeriod by remember{
        mutableStateOf(
            prefs.getBoolean(
                "period",
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

    var popup by remember{
        mutableStateOf<String?>(null)
    }

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

    val customUri=
        customUriString?.let(
            Uri::parse
        )

    val picker=
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
        ){uri->
            if(uri!=null){
                runCatching{
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                customUriString=
                    uri.toString()

                prefs.edit()
                    .putString(
                        "custom_wallpaper",
                        uri.toString()
                    )
                    .apply()

                popup=null
            }
        }

    DisposableEffect(activity){
        val window=activity?.window

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
                ActivityInfo
                    .SCREEN_ORIENTATION_UNSPECIFIED

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

    val font=when(fontIndex){
        1->NmixNunito
        2->NmixOutfit
        3->NmixPoppins
        4->NmixQuicksand
        else->NmixInter
    }

    val tone=
        clockTones[colorIndex]

    val wall=
        NmixThemeName.entries[
            wallpaperIndex
        ].palette()

    val parts=
        parseClockTime(time)

    val motion=
        rememberInfiniteTransition(
            label="clockWallpaper"
        )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            tween(
                2800,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="x"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            tween(
                3600,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="y"
    )

    val z by motion.animateFloat(
        -.85f,
        .85f,
        infiniteRepeatable(
            tween(
                4500,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="z"
    )

    val pulse by motion.animateFloat(
        .90f,
        1.11f,
        infiniteRepeatable(
            tween(
                2550,
                easing=EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label="pulse"
    )

    /*
     * Light mode is intentionally grey/off-white instead
     * of a bright white sheet. Wallpaper colour carries
     * much more visual strength in both modes.
     */
    val base=
        if(a.darkMode)
            Color(0xFF090C0B)
        else
            Color(0xFFD7DEDB)

    val controlSurface=
        if(a.darkMode)
            Color(0xFF101513)
                .copy(alpha=.88f)
        else
            Color(0xFFF7FAF8)
                .copy(alpha=.80f)

    val popupSurface=
        if(a.darkMode)
            Color(0xFF111614)
        else
            Color(0xFFF4F7F5)

    val border=
        if(a.darkMode)
            Color.White.copy(alpha=.15f)
        else
            Color(0xFF26312C)
                .copy(alpha=.16f)

    val uiText=
        if(a.darkMode)
            Color(0xFFF0F4F2)
        else
            Color(0xFF19201D)

    val sideText=
        if(a.darkMode)
            Color(0xFFD4DCD8)
        else
            Color(0xFF414B46)

    Box(
        Modifier
            .fillMaxSize()
            .background(base)
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null
            ){
                when{
                    clean->clean=false
                    popup!=null->popup=null
                }
            }
    ){
        if(customUri!=null){
            CustomWallpaper(customUri)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if(a.darkMode)
                            Color.Black.copy(alpha=.12f)
                        else
                            Color.White.copy(alpha=.04f)
                    )
            )
        }else{
            ClockGlow(
                wall.accent,
                if(a.darkMode).52f else .50f,
                470,
                Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x=(-125).dp,
                        y=(-115).dp
                    )
                    .graphicsLayer{
                        translationX=x*245f
                        translationY=y*105f
                        scaleX=pulse
                        scaleY=pulse
                    }
            )

            ClockGlow(
                wall.accentLight,
                if(a.darkMode).37f else .46f,
                430,
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x=115.dp,
                        y=105.dp
                    )
                    .graphicsLayer{
                        translationX=-x*220f
                        translationY=z*135f
                    }
            )

            ClockGlow(
                wall.accent,
                if(a.darkMode).24f else .33f,
                350,
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=z*185f
                        translationY=-y*115f
                    }
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter=
                fadeIn(tween(320))+
                slideInVertically(
                    initialOffsetY={-it/3},
                    animationSpec=tween(
                        390,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(tween(210))+
                slideOutVertically(
                    targetOffsetY={-it/3},
                    animationSpec=tween(280)
                )
        ){
            ClockBrand(
                modifier=Modifier.padding(
                    start=22.dp,
                    top=15.dp
                ),
                color=uiText
            )
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter=fadeIn(tween(330)),
            exit=fadeOut(tween(200))
        ){
            if(landscape){
                Row(
                    Modifier.padding(
                        top=12.dp,
                        end=14.dp
                    ),
                    horizontalArrangement=
                        Arrangement.spacedBy(7.dp)
                ){
                    ClockCarousel(
                        title="FONT",
                        options=clockFonts,
                        index=fontIndex,
                        accent=tone.accent,
                        width=156,
                        surface=controlSurface,
                        border=border,
                        centerColor=uiText,
                        sideColor=sideText,
                        headingFont=a.fontFamily,
                        optionFont=font
                    ){
                        fontIndex=it
                        saveInt("font",it)
                    }

                    ClockCarousel(
                        title="STYLE",
                        options=clockStyles,
                        index=styleIndex,
                        accent=tone.accent,
                        width=156,
                        surface=controlSurface,
                        border=border,
                        centerColor=uiText,
                        sideColor=sideText,
                        headingFont=a.fontFamily,
                        optionFont=font
                    ){
                        styleIndex=it
                        saveInt("style",it)
                    }

                    ClockCarousel(
                        title="COLOR",
                        options=clockTones.map{
                            it.name
                        },
                        index=colorIndex,
                        accent=tone.accent,
                        width=156,
                        surface=controlSurface,
                        border=border,
                        centerColor=uiText,
                        sideColor=sideText,
                        headingFont=a.fontFamily,
                        optionFont=font
                    ){
                        colorIndex=it
                        saveInt("color",it)
                    }
                }
            }else{
                Column(
                    Modifier.padding(
                        top=13.dp,
                        end=10.dp
                    ),
                    verticalArrangement=
                        Arrangement.spacedBy(5.dp)
                ){
                    ClockCarousel(
                        "FONT",
                        clockFonts,
                        fontIndex,
                        tone.accent,
                        170,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        a.fontFamily,
                        font
                    ){
                        fontIndex=it
                        saveInt("font",it)
                    }

                    ClockCarousel(
                        "STYLE",
                        clockStyles,
                        styleIndex,
                        tone.accent,
                        170,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        a.fontFamily,
                        font
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
                        tone.accent,
                        170,
                        controlSurface,
                        border,
                        uiText,
                        sideText,
                        a.fontFamily,
                        font
                    ){
                        colorIndex=it
                        saveInt("color",it)
                    }
                }
            }
        }

        /*
         * The entire face container gets more room,
         * not just a larger Text font.
         */
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(
                    if(landscape).78f
                    else .90f
                )
                .height(
                    if(landscape)
                        300.dp
                    else
                        330.dp
                ),
            contentAlignment=Alignment.Center
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
                            initialScale=.965f,
                            animationSpec=tween(
                                400,
                                easing=EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(tween(230))+
                        scaleOut(
                            targetScale=1.02f,
                            animationSpec=tween(280)
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
                    showPeriod=showPeriod,
                    showDate=showDate,
                    dark=a.darkMode
                )
            }

            AnimatedVisibility(
                visible=clean,
                modifier=Modifier
                    .align(
                        Alignment.BottomCenter
                    ),
                enter=
                    fadeIn(tween(390))+
                    slideInVertically(
                        initialOffsetY={it/2},
                        animationSpec=tween(
                            440,
                            easing=EaseOutCubic
                        )
                    ),
                exit=fadeOut(tween(210))
            ){
                ClockBrand(
                    modifier=Modifier.padding(
                        bottom=8.dp
                    ),
                    centered=true,
                    color=uiText
                )
            }
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                ),
            enter=
                fadeIn(tween(320))+
                slideInVertically(
                    initialOffsetY={it/2},
                    animationSpec=tween(
                        390,
                        easing=EaseOutCubic
                    )
                ),
            exit=fadeOut(tween(210))
        ){
            Column(
                Modifier.padding(
                    start=11.dp,
                    end=11.dp,
                    bottom=15.dp
                ),
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                /*
                 * One permanent popup slot.
                 * Wallpaper and Display replace one another
                 * in the exact same position.
                 */
                AnimatedContent(
                    targetState=popup,
                    transitionSpec={
                        fadeIn(
                            tween(
                                240,
                                easing=EaseOutCubic
                            )
                        ) togetherWith
                        fadeOut(tween(160))
                    },
                    label="bottomPopup"
                ){shown->
                    when(shown){
                        "wallpaper"->{
                            WallpaperBar(
                                selected=wallpaperIndex,
                                customSelected=
                                    customUri!=null,
                                surface=popupSurface,
                                accent=wall.accent,
                                onSelect={
                                    wallpaperIndex=it
                                    customUriString=null

                                    saveInt(
                                        "wallpaper",
                                        it
                                    )

                                    prefs.edit()
                                        .remove(
                                            "custom_wallpaper"
                                        )
                                        .apply()
                                },
                                onCustom={
                                    picker.launch(
                                        arrayOf("image/*")
                                    )
                                }
                            )
                        }

                        "display"->{
                            DisplayBar(
                                surface=popupSurface,
                                accent=wall.accent,
                                textColor=uiText,
                                font=font,
                                hours=showHours,
                                minutes=showMinutes,
                                seconds=showSeconds,
                                period=showPeriod,
                                date=showDate,
                                onHours={
                                    showHours=
                                        !showHours

                                    saveBoolean(
                                        "hours",
                                        showHours
                                    )
                                },
                                onMinutes={
                                    showMinutes=
                                        !showMinutes

                                    saveBoolean(
                                        "minutes",
                                        showMinutes
                                    )
                                },
                                onSeconds={
                                    showSeconds=
                                        !showSeconds

                                    saveBoolean(
                                        "seconds",
                                        showSeconds
                                    )
                                },
                                onPeriod={
                                    showPeriod=
                                        !showPeriod

                                    saveBoolean(
                                        "period",
                                        showPeriod
                                    )
                                },
                                onDate={
                                    showDate=
                                        !showDate

                                    saveBoolean(
                                        "date",
                                        showDate
                                    )
                                }
                            )
                        }

                        else->{
                            Spacer(
                                Modifier.height(0.dp)
                            )
                        }
                    }
                }

                if(popup!=null){
                    Spacer(
                        Modifier.height(10.dp)
                    )
                }

                Row(
                    horizontalArrangement=
                        Arrangement.spacedBy(8.dp),
                    verticalAlignment=
                        Alignment.CenterVertically
                ){
                    ClockAction(
                        "Wallpaper",
                        NmixIcon.WALLPAPER,
                        font,
                        controlSurface,
                        border,
                        uiText
                    ){
                        popup=
                            if(
                                popup=="wallpaper"
                            )
                                null
                            else
                                "wallpaper"
                    }

                    ClockAction(
                        "Rotate",
                        NmixIcon.ROTATE,
                        font,
                        controlSurface,
                        border,
                        uiText
                    ){
                        activity?.requestedOrientation=
                            if(landscape)
                                ActivityInfo
                                    .SCREEN_ORIENTATION_PORTRAIT
                            else
                                ActivityInfo
                                    .SCREEN_ORIENTATION_LANDSCAPE
                    }

                    ClockAction(
                        "Display",
                        NmixIcon.CLOCK,
                        font,
                        controlSurface,
                        border,
                        uiText
                    ){
                        popup=
                            if(
                                popup=="display"
                            )
                                null
                            else
                                "display"
                    }

                    ClockAction(
                        "Clean",
                        NmixIcon.FULLSCREEN,
                        font,
                        controlSurface,
                        border,
                        uiText
                    ){
                        popup=null
                        clean=true
                    }

                    ClockAction(
                        "Exit",
                        NmixIcon.CLOSE,
                        font,
                        controlSurface,
                        border,
                        uiText,
                        red=true
                    ){
                        activity?.requestedOrientation=
                            ActivityInfo
                                .SCREEN_ORIENTATION_UNSPECIFIED

                        onExit()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockCarousel(
    title:String,
    options:List<String>,
    index:Int,
    accent:Color,
    width:Int,
    surface:Color,
    border:Color,
    centerColor:Color,
    sideColor:Color,
    headingFont:FontFamily,
    optionFont:FontFamily,
    onIndex:(Int)->Unit
){
    val density=LocalDensity.current
    val scope=rememberCoroutineScope()

    val slot=with(density){
        (width.dp*.245f).toPx()
    }

    val drag=remember{
        Animatable(0f)
    }

    var dragging by remember{
        mutableStateOf(false)
    }

    fun wrap(value:Int):Int=
        ((value%options.size)+options.size)%
            options.size

    val shape=
        RoundedCornerShape(50)

    Column(
        Modifier
            .width(width.dp)
            .height(48.dp)
            .clip(shape)
            .background(surface)
            .border(
                .5.dp,
                border,
                shape
            )
            .pointerInput(
                index,
                options.size
            ){
                detectHorizontalDragGestures(
                    onDragStart={
                        dragging=true

                        scope.launch{
                            drag.stop()
                        }
                    },

                    onHorizontalDrag={
                        change,
                        amount->

                        change.consume()

                        val next=
                            (drag.value+amount)
                                .coerceIn(
                                    -slot,
                                    slot
                                )

                        scope.launch{
                            drag.snapTo(next)
                        }
                    },

                    onDragEnd={
                        dragging=false

                        val move=when{
                            drag.value<
                                -slot*.30f->1

                            drag.value>
                                slot*.30f->-1

                            else->0
                        }

                        scope.launch{
                            if(move!=0){
                                val target=
                                    if(move>0)
                                        -slot
                                    else
                                        slot

                                /*
                                 * First visually finish the
                                 * slide into the centre.
                                 * Then commit the index while
                                 * resetting offset invisibly.
                                 */
                                drag.animateTo(
                                    target,
                                    tween(
                                        150,
                                        easing=
                                            EaseOutCubic
                                    )
                                )

                                onIndex(
                                    wrap(
                                        index+move
                                    )
                                )

                                drag.snapTo(0f)
                            }else{
                                drag.animateTo(
                                    0f,
                                    tween(
                                        170,
                                        easing=
                                            EaseOutCubic
                                    )
                                )
                            }
                        }
                    },

                    onDragCancel={
                        dragging=false

                        scope.launch{
                            drag.animateTo(
                                0f,
                                tween(
                                    160,
                                    easing=
                                        EaseOutCubic
                                )
                            )
                        }
                    }
                )
            }
            .padding(top=3.dp),
        horizontalAlignment=
            Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=accent,
            fontSize=7.5.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=headingFont
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(shape)
        ){
            val progress=
                if(slot==0f)
                    0f
                else
                    drag.value/slot

            for(offset in -2..2){
                val position=
                    offset+progress

                if(abs(position)<1.8f){
                    val center=
                        1f-
                        abs(position)
                            .coerceIn(0f,1f)

                    /*
                     * Side labels remain relatively close
                     * and readable. The centred label is
                     * visibly larger.
                     */
                    val scale=
                        .88f+
                        center*.38f

                    val alpha=
                        .48f+
                        center*.52f

                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .graphicsLayer{
                                translationX=
                                    position*slot

                                scaleX=scale
                                scaleY=scale
                                this.alpha=alpha
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
                            color=
                                if(center>.62f)
                                    centerColor
                                else
                                    sideColor.copy(
                                        alpha=.68f
                                    ),
                            fontSize=10.5.sp,
                            fontWeight=
                                if(center>.62f)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                            fontFamily=optionFont,
                            maxLines=1,
                            textAlign=TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperBar(
    selected:Int,
    customSelected:Boolean,
    surface:Color,
    accent:Color,
    onSelect:(Int)->Unit,
    onCustom:()->Unit
){
    val colors=
        NmixThemeName.entries.map{
            it.palette().accent
        }

    val shape=
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(60.dp)
            .clip(shape)
            .background(surface)
            .padding(horizontal=14.dp),
        verticalAlignment=
            Alignment.CenterVertically,
        horizontalArrangement=
            Arrangement.spacedBy(15.dp)
    ){
        colors.forEachIndexed{
            index,
            color->

            WallpaperCircle(
                color=color,
                selected=
                    !customSelected&&
                    index==selected,
                outline=accent
            ){
                onSelect(index)
            }
        }

        GalleryCircle(
            selected=customSelected,
            outline=accent,
            onClick=onCustom
        )
    }
}

@Composable
private fun WallpaperCircle(
    color:Color,
    selected:Boolean,
    outline:Color,
    onClick:()->Unit
){
    /*
     * Outer 34dp footprint never changes.
     * Actual colour circle is always 28dp.
     * Selection therefore does not shrink
     * or enlarge the swatch.
     */
    Box(
        Modifier
            .size(34.dp)
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if(selected)
                        Modifier.border(
                            1.5.dp,
                            outline,
                            CircleShape
                        )
                    else
                        Modifier
                )
        )
    }
}

@Composable
private fun GalleryCircle(
    selected:Boolean,
    outline:Color,
    onClick:()->Unit
){
    Box(
        Modifier
            .size(34.dp)
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF68716D)
                )
                .then(
                    if(selected)
                        Modifier.border(
                            1.5.dp,
                            outline,
                            CircleShape
                        )
                    else
                        Modifier
                ),
            contentAlignment=Alignment.Center
        ){
            NmixIcon(
                NmixIcon.WALLPAPER,
                Modifier.size(13.dp),
                Color.White
            )
        }
    }
}

@Composable
private fun DisplayBar(
    surface:Color,
    accent:Color,
    textColor:Color,
    font:FontFamily,
    hours:Boolean,
    minutes:Boolean,
    seconds:Boolean,
    period:Boolean,
    date:Boolean,
    onHours:()->Unit,
    onMinutes:()->Unit,
    onSeconds:()->Unit,
    onPeriod:()->Unit,
    onDate:()->Unit
){
    val shape=
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(60.dp)
            .clip(shape)
            .background(surface)
            .padding(horizontal=14.dp),
        verticalAlignment=
            Alignment.CenterVertically,
        horizontalArrangement=
            Arrangement.spacedBy(15.dp)
    ){
        DisplayCircle(
            "H",
            hours,
            accent,
            textColor,
            font,
            onHours
        )

        DisplayCircle(
            "M",
            minutes,
            accent,
            textColor,
            font,
            onMinutes
        )

        DisplayCircle(
            "S",
            seconds,
            accent,
            textColor,
            font,
            onSeconds
        )

        DisplayCircle(
            "A/P",
            period,
            accent,
            textColor,
            font,
            onPeriod
        )

        DisplayCircle(
            "D",
            date,
            accent,
            textColor,
            font,
            onDate
        )
    }
}

@Composable
private fun DisplayCircle(
    text:String,
    enabled:Boolean,
    accent:Color,
    textColor:Color,
    font:FontFamily,
    onClick:()->Unit
){
    /*
     * Same selection language as wallpaper:
     * fixed-size circle, neutral interior,
     * thin accent outline only when enabled.
     */
    Box(
        Modifier
            .size(34.dp)
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    textColor.copy(
                        alpha=
                            if(enabled)
                                .12f
                            else
                                .055f
                    )
                )
                .then(
                    if(enabled)
                        Modifier.border(
                            1.5.dp,
                            accent,
                            CircleShape
                        )
                    else
                        Modifier
                ),
            contentAlignment=Alignment.Center
        ){
            Text(
                text,
                color=
                    if(enabled)
                        textColor
                    else
                        textColor.copy(
                            alpha=.39f
                        ),
                fontSize=
                    if(text=="A/P")
                        6.sp
                    else
                        9.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=font
            )
        }
    }
}

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
    showPeriod:Boolean,
    showDate:Boolean,
    dark:Boolean
){
    /*
     * Each component is rendered independently.
     * S can no longer remove A/P and A/P can no
     * longer silently do nothing.
     */
    val timeRow=
        @Composable{
            Row(
                verticalAlignment=
                    Alignment.Bottom,
                horizontalArrangement=
                    Arrangement.Center
            ){
                val numeric=
                    mutableListOf<String>()

                if(showHours)
                    numeric.add(parts.hour)

                if(showMinutes)
                    numeric.add(parts.minute)

                if(showSeconds)
                    numeric.add(parts.second)

                Text(
                    numeric.joinToString(":"),
                    color=tone.main,
                    fontSize=
                        if(landscape)
                            76.sp
                        else
                            56.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font
                )

                if(showPeriod){
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
                        modifier=Modifier.padding(
                            bottom=9.dp
                        )
                    )
                }
            }
        }

    when(style){
        1->{
            Column(
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                timeRow()

                if(showDate){
                    Spacer(
                        Modifier.height(11.dp)
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
                    Arrangement.spacedBy(20.dp)
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
                                    69.sp
                                else
                                    54.sp,
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
                                    69.sp
                                else
                                    54.sp,
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
                            fontSize=33.sp,
                            fontWeight=FontWeight.Bold,
                            fontFamily=font
                        )
                    }

                    if(showPeriod){
                        Text(
                            parts.period,
                            color=tone.accent,
                            fontSize=12.sp,
                            fontWeight=FontWeight.Bold,
                            fontFamily=font
                        )
                    }

                    if(showDate){
                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Text(
                            date,
                            color=tone.main.copy(
                                alpha=.67f
                            ),
                            fontSize=10.sp,
                            fontFamily=font
                        )
                    }
                }
            }
        }

        3->{
            val pieces=
                buildList{
                    if(showHours)
                        add(
                            "H" to
                            parts.hour
                        )

                    if(showMinutes)
                        add(
                            "M" to
                            parts.minute
                        )

                    if(showSeconds)
                        add(
                            "S" to
                            parts.second
                        )
                }

            Column(
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Row(
                    horizontalArrangement=
                        Arrangement.spacedBy(10.dp)
                ){
                    pieces.forEach{
                        part->

                        TimeCard(
                            label=part.first,
                            value=part.second,
                            tone=tone,
                            font=font,
                            dark=dark,
                            landscape=landscape
                        )
                    }
                }

                if(showPeriod||showDate){
                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Row(
                        horizontalArrangement=
                            Arrangement.spacedBy(12.dp)
                    ){
                        if(showPeriod){
                            Text(
                                parts.period,
                                color=tone.accent,
                                fontSize=11.sp,
                                fontWeight=FontWeight.Bold,
                                fontFamily=font
                            )
                        }

                        if(showDate){
                            Text(
                                date,
                                color=tone.main.copy(
                                    alpha=.70f
                                ),
                                fontSize=10.sp,
                                fontFamily=font
                            )
                        }
                    }
                }
            }
        }

        4->{
            Box(
                Modifier.size(
                    if(landscape)
                        270.dp
                    else
                        235.dp
                ),
                contentAlignment=
                    Alignment.Center
            ){
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            1.25.dp,
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
                                212.dp
                            else
                                184.dp
                        )
                        .border(
                            .7.dp,
                            tone.accent.copy(
                                alpha=.19f
                            ),
                            CircleShape
                        )
                )

                Column(
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    timeRow()

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
            Column{
                Text(
                    "NMIX://LOCAL_CLOCK",
                    color=tone.accent,
                    fontSize=10.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=font
                )

                Spacer(
                    Modifier.height(6.dp)
                )

                timeRow()

                if(showDate){
                    Text(
                        "> DATE  $date",
                        color=tone.accent.copy(
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
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Box(
                    Modifier
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
                        )
                        .padding(
                            horizontal=30.dp,
                            vertical=16.dp
                        )
                ){
                    timeRow()
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
                    Modifier.height(4.dp)
                )

                timeRow()

                Box(
                    Modifier
                        .padding(top=9.dp)
                        .width(
                            if(landscape)
                                320.dp
                            else
                                245.dp
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
                        color=tone.main.copy(
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

                timeRow()

                if(showDate){
                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Text(
                        date,
                        color=tone.main.copy(
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

@Composable
private fun TimeCard(
    label:String,
    value:String,
    tone:ClockTone,
    font:FontFamily,
    dark:Boolean,
    landscape:Boolean
){
    val shape=
        RoundedCornerShape(18.dp)

    Column(
        Modifier
            .width(
                if(landscape)
                    92.dp
                else
                    75.dp
            )
            .height(
                if(landscape)
                    92.dp
                else
                    80.dp
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
                    alpha=.34f
                ),
                shape
            ),
        horizontalAlignment=
            Alignment.CenterHorizontally,
        verticalArrangement=
            Arrangement.Center
    ){
        Text(
            label,
            color=tone.accent,
            fontSize=7.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=font
        )

        Text(
            value,
            color=tone.main,
            fontSize=
                if(landscape)
                    41.sp
                else
                    34.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=font
        )
    }
}

private fun parseClockTime(
    time:String
):ClockParts{
    val period=when{
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
        hour=pieces.getOrElse(0){
            "00"
        },
        minute=pieces.getOrElse(1){
            "00"
        },
        second=pieces.getOrElse(2){
            "00"
        },
        period=period
    )
}

@Composable
private fun ClockBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false,
    color:Color
){
    val a=
        LocalNmixAppearance.current

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
            color=color.copy(
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

@Composable
private fun ClockAction(
    text:String,
    icon:NmixIcon,
    font:FontFamily,
    surface:Color,
    border:Color,
    textColor:Color,
    red:Boolean=false,
    onClick:()->Unit
){
    val foreground=
        if(red)
            Color(0xFFFF8585)
        else
            textColor

    val shape=
        RoundedCornerShape(50)

    Row(
        Modifier
            .height(46.dp)
            .clip(shape)
            .background(
                if(red)
                    Color(0xFFB8444B)
                        .copy(alpha=.20f)
                else
                    surface
            )
            .border(
                .6.dp,
                if(red)
                    foreground.copy(
                        alpha=.50f
                    )
                else
                    border,
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
                horizontal=12.dp
            ),
        verticalAlignment=
            Alignment.CenterVertically,
        horizontalArrangement=
            Arrangement.spacedBy(7.dp)
    ){
        NmixIcon(
            icon,
            Modifier.size(17.dp),
            foreground
        )

        Text(
            text,
            color=foreground,
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=font
        )
    }
}

@Composable
private fun ClockGlow(
    color:Color,
    alpha:Float,
    size:Int,
    modifier:Modifier
){
    Box(
        modifier
            .size(size.dp)
            .background(
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to
                            color.copy(
                                alpha=alpha
                            ),

                        .26f to
                            color.copy(
                                alpha=alpha*.77f
                            ),

                        .52f to
                            color.copy(
                                alpha=alpha*.36f
                            ),

                        .78f to
                            color.copy(
                                alpha=alpha*.08f
                            ),

                        1f to
                            Color.Transparent
                    )
                ),
                CircleShape
            )
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
                    context.contentResolver
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
            modifier=Modifier.fillMaxSize(),
            contentScale=ContentScale.Crop
        )
    }
}
