package com.lxzrvi.nmix

import android.content.Context
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
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

private const val CLOCK_PREFS=
    "nmix_fullscreen_clock"

private data class ClockParts(
    val hour:String,
    val minute:String,
    val second:String,
    val period:String
)

private val clockStyles=
    listOf(
        "Digital",
        "Minimal",
        "Stack",
        "Focus"
    )

private val clockFonts=
    listOf(
        "Inter",
        "Nunito",
        "Outfit",
        "Poppins",
        "Quicksand"
    )

private val clockColors=
    listOf(
        "White",
        "Green",
        "Blue",
        "Purple",
        "Orange",
        "Rose",
        "Cyan"
    )

private val clockColorValues=
    listOf(
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
    val context=
        LocalContext.current

    val activity=
        LocalActivity.current

    val configuration=
        LocalConfiguration.current

    val a=
        LocalNmixAppearance.current

    val p=a.palette
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
                "text_color",
                0
            ).coerceIn(
                0,
                clockColors.lastIndex
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

    val clockTextColor=
        clockColorValues[
            colorIndex.coerceIn(
                0,
                clockColorValues.lastIndex
            )
        ]

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

    fun exitFullscreen(){
        activity?.requestedOrientation=
            originalOrientation

        onExit()
    }

    val imagePicker=
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ){uri->
            if(uri!=null){
                runCatching{
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            IntentFlags.READ
                        )
                }

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

    BackHandler{
        exitFullscreen()
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

    Box(
        Modifier
            .fillMaxSize()
            .background(
                if(a.darkMode)
                    Color(0xFF050807)
                else
                    Color(0xFFF8FAF9)
            )
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
                                alpha=.43f
                            )
                        else
                            Color.White.copy(
                                alpha=.68f
                            )
                    )
            )
        }

        FullscreenWorldBackground(
            wallpaper=
                customWallpaper!=null
        )

        AnimatedVisibility(
            visible=!clean,
            modifier=
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                    )
        ){
            ClockBrand(
                Modifier.padding(
                    start=19.dp,
                    top=17.dp
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
                    )
        ){
            if(landscape){
                Row(
                    Modifier.padding(
                        top=14.dp,
                        end=12.dp
                    ),
                    horizontalArrangement=
                        Arrangement.spacedBy(4.dp)
                ){
                    DragSelector(
                        title="FONT",
                        options=clockFonts,
                        selected=fontIndex,
                        selectedColor=
                            clockTextColor,
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

                    DragSelector(
                        title="STYLE",
                        options=clockStyles,
                        selected=styleIndex,
                        selectedColor=
                            clockTextColor,
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

                    DragSelector(
                        title="COLOR",
                        options=clockColors,
                        selected=colorIndex,
                        selectedColor=
                            clockColorValues[itSafe(
                                colorIndex,
                                clockColorValues.size
                            )],
                        font=font
                    ){
                        colorIndex=it

                        prefs.edit()
                            .putInt(
                                "text_color",
                                it
                            )
                            .apply()
                    }
                }
            }else{
                Column(
                    Modifier.padding(
                        top=14.dp,
                        end=8.dp
                    ),
                    verticalArrangement=
                        Arrangement.spacedBy(3.dp)
                ){
                    DragSelector(
                        title="FONT",
                        options=clockFonts,
                        selected=fontIndex,
                        selectedColor=
                            clockTextColor,
                        font=font,
                        compact=true
                    ){
                        fontIndex=it

                        prefs.edit()
                            .putInt(
                                "font",
                                it
                            )
                            .apply()
                    }

                    DragSelector(
                        title="STYLE",
                        options=clockStyles,
                        selected=styleIndex,
                        selectedColor=
                            clockTextColor,
                        font=font,
                        compact=true
                    ){
                        styleIndex=it

                        prefs.edit()
                            .putInt(
                                "style",
                                it
                            )
                            .apply()
                    }

                    DragSelector(
                        title="COLOR",
                        options=clockColors,
                        selected=colorIndex,
                        selectedColor=
                            clockColorValues[
                                itSafe(
                                    colorIndex,
                                    clockColorValues.size
                                )
                            ],
                        font=font,
                        compact=true
                    ){
                        colorIndex=it

                        prefs.edit()
                            .putInt(
                                "text_color",
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
                        .90f
                    else
                        .95f
                )
                .height(
                    if(landscape)
                        330.dp
                    else
                        370.dp
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
                                260,
                                easing=EaseOutCubic
                            )
                        )+
                        scaleIn(
                            initialScale=.985f,
                            animationSpec=
                                tween(280)
                        )
                    ) togetherWith (
                        fadeOut(
                            tween(180)
                        )+
                        scaleOut(
                            targetScale=1.008f
                        )
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
                    clockColor=
                        clockTextColor
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
                        bottom=75.dp
                    ),
            enter=
                fadeIn(tween(180))+
                    scaleIn(
                        initialScale=.97f
                    ),
            exit=
                fadeOut(tween(150))
        ){
            DisplayOptions(
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
                    )
        ){
            Row(
                Modifier.padding(
                    start=8.dp,
                    end=8.dp,
                    bottom=16.dp
                ),
                horizontalArrangement=
                    Arrangement.spacedBy(5.dp),
                verticalAlignment=
                    Alignment.CenterVertically
            ){
                ClockAction(
                    "Wallpaper",
                    NmixIcon.WALLPAPER,
                    font
                ){
                    haptic{
                        wallpaperConsent=true
                    }
                }

                ClockAction(
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

                ClockAction(
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
                    haptic{
                        exitFullscreen()
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
                fadeIn(tween(180))+
                    scaleIn(
                        initialScale=.97f
                    ),
            exit=
                fadeOut(tween(140))
        ){
            WallpaperDialog(
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
 * DRAG SELECTOR
 * ==================================================
 */

@Composable
private fun DragSelector(
    title:String,
    options:List<String>,
    selected:Int,
    selectedColor:Color,
    font:FontFamily,
    compact:Boolean=false,
    onSelect:(Int)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    var widthPx by remember{
        mutableIntStateOf(1)
    }

    var dragPx by remember{
        mutableFloatStateOf(0f)
    }

    val safe=
        selected.coerceIn(
            0,
            options.lastIndex
        )

    fun indexAt(delta:Int):Int{
        var result=
            (safe+delta)%
                options.size

        if(result<0){
            result+=options.size
        }

        return result
    }

    val threshold=
        widthPx*
            .22f

    val visual=
        if(threshold<=0f)
            0f
        else
            (
                dragPx/
                    threshold
            ).coerceIn(
                -1f,
                1f
            )

    val previous=
        indexAt(-1)

    val next=
        indexAt(1)

    val shape=
        RoundedCornerShape(13.dp)

    val boxWidth=
        if(compact)
            155.dp
        else
            158.dp

    Column(
        Modifier
            .width(boxWidth)
            .height(48.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF131816)
                        .copy(alpha=.93f)
                else
                    Color.White
                        .copy(alpha=.94f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .025f
                        else
                            .016f
                )
            )
            .border(
                .4.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .14f
                        else
                            .21f
                ),
                shape
            )
            .onSizeChanged{
                widthPx=
                    it.width.coerceAtLeast(1)
            }
            .pointerInput(
                safe,
                widthPx,
                options.size
            ){
                detectHorizontalDragGestures(
                    onDragStart={
                        dragPx=0f
                    },

                    onHorizontalDrag={
                        change,
                        amount->

                        change.consume()

                        dragPx=
                            (
                                dragPx+
                                    amount
                            ).coerceIn(
                                -threshold*1.18f,
                                threshold*1.18f
                            )
                    },

                    onDragEnd={
                        when{
                            dragPx<=
                                -threshold*.62f->{
                                haptic{
                                    onSelect(next)
                                }
                            }

                            dragPx>=
                                threshold*.62f->{
                                haptic{
                                    onSelect(previous)
                                }
                            }
                        }

                        dragPx=0f
                    },

                    onDragCancel={
                        dragPx=0f
                    }
                )
            },
        horizontalAlignment=
            Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=p.accent,
            fontSize=6.4.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.7.sp,
            fontFamily=a.fontFamily,
            modifier=
                Modifier.padding(top=3.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        bottomStart=13.dp,
                        bottomEnd=13.dp
                    )
                )
        ){
            /*
             * Finger directly moves the three labels.
             * Side text becoming center simultaneously
             * inherits center scale/weight/color.
             */
            SelectorMovingText(
                text=
                    options[previous],
                position=
                    -1f+visual,
                selectedAmount=
                    (visual)
                        .coerceIn(
                            0f,
                            1f
                        ),
                color=
                    selectedColor,
                muted=ui.muted,
                font=font
            )

            SelectorMovingText(
                text=
                    options[safe],
                position=visual,
                selectedAmount=
                    1f-abs(visual),
                color=
                    selectedColor,
                muted=ui.muted,
                font=font
            )

            SelectorMovingText(
                text=
                    options[next],
                position=
                    1f+visual,
                selectedAmount=
                    (-visual)
                        .coerceIn(
                            0f,
                            1f
                        ),
                color=
                    selectedColor,
                muted=ui.muted,
                font=font
            )
        }
    }
}

@Composable
private fun BoxScope.SelectorMovingText(
    text:String,
    position:Float,
    selectedAmount:Float,
    color:Color,
    muted:Color,
    font:FontFamily
){
    val amount=
        selectedAmount.coerceIn(
            0f,
            1f
        )

    Text(
        text,
        modifier=
            Modifier
                .align(
                    Alignment.Center
                )
                .graphicsLayer{
                    translationX=
                        position*
                            54f

                    scaleX=
                        .84f+
                            amount*.16f

                    scaleY=
                        .84f+
                            amount*.16f
                },
        color=
            lerpClockColor(
                muted.copy(
                    alpha=.62f
                ),
                color,
                amount
            ),
        fontSize=
            (
                7.2f+
                    amount*2.2f
            ).sp,
        fontWeight=
            if(amount>.55f)
                FontWeight.Bold
            else
                FontWeight.Normal,
        fontFamily=font,
        maxLines=1,
        textAlign=
            TextAlign.Center
    )
}

/*
 * ==================================================
 * WORLD BACKGROUND
 * ==================================================
 */

@Composable
private fun BoxScope.FullscreenWorldBackground(
    wallpaper:Boolean
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val world=
        rememberNmixWorldMotion(
            label="fullscreenWorld"
        )

    if(!a.animationEnabled){
        Box(
            Modifier
                .size(900.dp)
                .align(
                    Alignment.Center
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(
                                alpha=
                                    if(a.darkMode)
                                        .18f
                                    else
                                        .13f
                            ),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        return
    }

    world.bodies.forEachIndexed{
        index,
        body->

        val worldX=
            body.x*
                (
                    760f+
                        index*55f
                )

        val worldY=
            body.y*
                (
                    650f+
                        index*44f
                )

        val alphaMultiplier=
            if(wallpaper)
                .62f
            else
                1f

        val soft=
            a.animation!=
                NmixAnimationName.FLOAT

        if(soft){
            val elementSize=
                when(index){
                    0->1180.dp
                    1->1010.dp
                    2->880.dp
                    3->940.dp
                    else->790.dp
                }

            Box(
                Modifier
                    .size(elementSize)
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            worldX

                        translationY=
                            worldY

                        scaleX=
                            body.pulse

                        scaleY=
                            body.pulse
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
                                                (
                                                    if(a.darkMode)
                                                        .30f
                                                    else
                                                        .23f
                                                )*
                                                    alphaMultiplier
                                        ),

                                    .28f to
                                        p.accent.copy(
                                            alpha=
                                                .17f*
                                                    alphaMultiplier
                                        ),

                                    .56f to
                                        p.accent.copy(
                                            alpha=
                                                .075f*
                                                    alphaMultiplier
                                        ),

                                    .78f to
                                        p.accent.copy(
                                            alpha=
                                                .021f*
                                                    alphaMultiplier
                                        ),

                                    1f to
                                        Color.Transparent
                                )
                        ),
                        CircleShape
                    )
            )
        }else{
            val elementSize=
                when(index){
                    0->430.dp
                    1->370.dp
                    2->320.dp
                    3->345.dp
                    else->285.dp
                }

            Canvas(
                Modifier
                    .size(elementSize)
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer{
                        translationX=
                            worldX

                        translationY=
                            worldY

                        rotationZ=
                            body.rotation

                        scaleX=
                            body.pulse

                        scaleY=
                            body.pulse
                    }
            ){
                val color=
                    if(index%2==0)
                        p.accent
                    else
                        p.accentLight

                val inset=
                    12.dp.toPx()

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                .025f*
                                    alphaMultiplier
                        ),
                    cornerRadius=
                        CornerRadius(
                            46.dp.toPx()
                        )
                )

                drawRoundRect(
                    color=
                        color.copy(
                            alpha=
                                (
                                    if(a.darkMode)
                                        .14f
                                    else
                                        .105f
                                )*
                                    alphaMultiplier
                        ),
                    topLeft=
                        Offset(
                            inset,
                            inset
                        ),
                    size=
                        Size(
                            size.width-
                                inset*2,
                            size.height-
                                inset*2
                        ),
                    cornerRadius=
                        CornerRadius(
                            37.dp.toPx()
                        )
                )
            }
        }
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
    showDate:Boolean,
    clockColor:Color
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val mainColor=
        if(
            a.darkMode &&
            clockColor==
                Color.White
        )
            Color.White.copy(
                alpha=.94f
            )
        else if(
            !a.darkMode &&
            clockColor==
                Color.White
        )
            Color(0xFF252A27)
        else
            clockColor

    val secondary=
        if(clockColor==Color.White){
            if(a.darkMode)
                p.accentLight
            else
                p.accentDark
        }else{
            clockColor.copy(
                alpha=.74f
            )
        }

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

    val safeNumeric=
        numeric.ifEmpty{
            "--"
        }

    when(style){
        1->{
            Column(
                horizontalAlignment=
                    Alignment.CenterHorizontally
            ){
                Text(
                    safeNumeric,
                    color=mainColor,
                    fontSize=
                        if(landscape)
                            78.sp
                        else
                            55.sp,
                    fontWeight=
                        FontWeight.Bold,
                    fontFamily=font,
                    maxLines=1
                )

                /*
                 * Never conditional on Seconds.
                 */
                Text(
                    parts.period,
                    color=secondary,
                    fontSize=13.sp,
                    fontWeight=
                        FontWeight.Bold,
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
                                alpha=.64f
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
                        parts.period,
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
                    fontWeight=
                        FontWeight.Bold,
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
                        safeNumeric,
                        color=mainColor,
                        fontSize=
                            if(landscape)
                                82.sp
                            else
                                57.sp,
                        fontWeight=
                            FontWeight.Bold,
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
                    fontWeight=
                        FontWeight.Bold,
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
                        safeNumeric,
                        color=mainColor,
                        fontSize=
                            if(landscape)
                                78.sp
                            else
                                55.sp,
                        fontWeight=
                            FontWeight.Bold,
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
                        Modifier.height(10.dp)
                    )

                    Text(
                        date,
                        color=
                            mainColor.copy(
                                alpha=.67f
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette

    val shape=
        RoundedCornerShape(16.dp)

    Row(
        Modifier
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF141917)
                        .copy(alpha=.95f)
                else
                    Color.White
                        .copy(alpha=.96f)
            )
            .background(
                p.accent.copy(
                    alpha=.025f
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
            .padding(9.dp),
        horizontalArrangement=
            Arrangement.spacedBy(6.dp)
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val progress by
        animateFloatAsState(
            targetValue=
                if(selected)1f else 0f,
            animationSpec=tween(190),
            label="displayChoice"
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
                else
                    Color.White
            )
            .background(
                p.accent.copy(
                    alpha=
                        .018f+
                            progress*.055f
                )
            )
            .border(
                (
                    .4f+
                        progress*.6f
                ).dp,
                p.accent.copy(
                    alpha=
                        .15f+
                            progress*.36f
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
                if(selected)
                    p.accent
                else
                    ui.text,
            fontSize=9.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * BOTTOM ACTIONS
 * ==================================================
 */

@Composable
private fun ClockAction(
    text:String,
    icon:NmixIcon,
    font:FontFamily,
    red:Boolean=false,
    selected:Boolean=false,
    onClick:()->Unit
){
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(13.dp)

    val foreground=
        if(red)
            Color(0xFFE66E75)
        else
            ui.text

    Row(
        Modifier
            .height(43.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF131816)
                        .copy(alpha=.94f)
                else
                    Color.White
                        .copy(alpha=.94f)
            )
            .background(
                when{
                    red->
                        Color(0xFFD94F57)
                            .copy(alpha=.055f)

                    selected->
                        p.accent.copy(
                            alpha=.075f
                        )

                    else->
                        p.accent.copy(
                            alpha=.018f
                        )
                }
            )
            .border(
                if(selected)
                    .9.dp
                else
                    .4.dp,
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
                                    .14f
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
            )
            .padding(
                horizontal=8.dp
            ),
        verticalAlignment=
            Alignment.CenterVertically,
        horizontalArrangement=
            Arrangement.spacedBy(5.dp)
    ){
        NmixIcon(
            icon,
            Modifier.size(15.dp),
            if(red)
                foreground
            else
                p.accent
        )

        Text(
            text,
            color=foreground,
            fontSize=7.8.sp,
            fontWeight=
                FontWeight.SemiBold,
            fontFamily=font
        )
    }
}

/*
 * ==================================================
 * WALLPAPER
 * ==================================================
 */

@Composable
private fun WallpaperDialog(
    hasWallpaper:Boolean,
    onCancel:()->Unit,
    onRemove:()->Unit,
    onChoose:()->Unit
){
    val a=
        LocalNmixAppearance.current

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
                    Color(0xFFF8FAF9)
            )
            .background(
                p.accent.copy(
                    alpha=.022f
                )
            )
            .border(
                .55.dp,
                p.accent.copy(
                    alpha=.27f
                ),
                shape
            )
            .padding(15.dp)
    ){
        Text(
            "CUSTOM WALLPAPER",
            color=p.accent,
            fontSize=9.sp,
            fontWeight=
                FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily
        )

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            "Choose an image for Fullscreen Clock.",
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
    val a=
        LocalNmixAppearance.current

    val p=a.palette
    val ui=a.uiColors()
    val haptic=
        rememberNmixHapticAction()

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

                    else->
                        Color.White
                }
            )
            .border(
                .4.dp,
                when{
                    red->
                        Color(0xFFE66E75)
                            .copy(alpha=.26f)

                    else->
                        p.accent.copy(
                            alpha=.20f
                        )
                },
                shape
            )
            .clickable(
                interactionSource=
                    remember{
                        MutableInteractionSource()
                    },
                indication=null
            ){
                haptic(onClick)
            },
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=
                when{
                    accent->
                        Color.White

                    red->
                        Color(0xFFE66E75)

                    else->
                        ui.text
                },
            fontSize=8.sp,
            fontWeight=
                FontWeight.Bold,
            fontFamily=a.fontFamily
        )
    }
}

/*
 * ==================================================
 * BRAND + IMAGE + HELPERS
 * ==================================================
 */

@Composable
private fun ClockBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false
){
    val a=
        LocalNmixAppearance.current

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
            fontWeight=
                FontWeight.Bold,
            letterSpacing=2.sp,
            fontFamily=NmixLogoFont
        )
    }
}

private fun parseFullscreenTime(
    time:String
):ClockParts{
    /*
     * Parse period independently. Seconds toggling
     * can never mutate this value.
     */
    val trimmed=
        time.trim()

    val period=
        when{
            trimmed.endsWith(
                " AM",
                ignoreCase=true
            )->"AM"

            trimmed.endsWith(
                " PM",
                ignoreCase=true
            )->"PM"

            else->""
        }

    val raw=
        trimmed
            .removeSuffix(" AM")
            .removeSuffix(" PM")
            .removeSuffix(" am")
            .removeSuffix(" pm")

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

private fun lerpClockColor(
    start:Color,
    end:Color,
    amount:Float
):Color{
    val t=
        amount.coerceIn(
            0f,
            1f
        )

    return Color(
        red=
            start.red+
                (end.red-start.red)*t,

        green=
            start.green+
                (end.green-start.green)*t,

        blue=
            start.blue+
                (end.blue-start.blue)*t,

        alpha=
            start.alpha+
                (end.alpha-start.alpha)*t
    )
}

private fun itSafe(
    value:Int,
    size:Int
):Int{
    return value.coerceIn(
        0,
        (size-1).coerceAtLeast(0)
    )
}

private object IntentFlags{
    const val READ=
        android.content.Intent
            .FLAG_GRANT_READ_URI_PERMISSION
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
