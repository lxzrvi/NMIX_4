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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
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

private val clockStyles=listOf(
    "Digital","Minimal","Stack","Focus",
    "Bold","Slim","Wide","Compact",
    "Hour","Minute","Split","Center",
    "Studio","Mono","Calm","Clean",
    "Large","Fine","Air","Dense",
    "Modern","Quiet","Prime","Essential"
)

private val clockFonts=listOf(
    "Inter","Nunito","Outfit","Poppins","Quicksand"
)

private val clockColors=listOf(
    "White","Green","Blue","Purple","Orange","Rose","Cyan"
)

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
    val config=LocalConfiguration.current
    val a=LocalNmixAppearance.current
    val haptic=rememberNmixHapticAction()
    val prefs=remember(context){
        context.getSharedPreferences(CLOCK_PREFS,Context.MODE_PRIVATE)
    }

    val landscape=config.orientation==Configuration.ORIENTATION_LANDSCAPE
    val originalOrientation=remember(activity){
        activity?.requestedOrientation?:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    val initialFont=when(a.font){
        NmixFontName.INTER->0
        NmixFontName.NUNITO->1
        NmixFontName.OUTFIT->2
        NmixFontName.POPPINS->3
        NmixFontName.QUICKSAND->4
    }

    var fontIndex by remember{
        mutableIntStateOf(prefs.getInt("font",initialFont).coerceIn(0,clockFonts.lastIndex))
    }
    var styleIndex by remember{
        mutableIntStateOf(prefs.getInt("style",0).coerceIn(0,clockStyles.lastIndex))
    }
    var colorIndex by remember{
        mutableIntStateOf(prefs.getInt("text_color",0).coerceIn(0,clockColors.lastIndex))
    }
    var showHours by remember{mutableStateOf(prefs.getBoolean("hours",true))}
    var showMinutes by remember{mutableStateOf(prefs.getBoolean("minutes",true))}
    var showSeconds by remember{mutableStateOf(prefs.getBoolean("seconds",true))}
    var showDate by remember{mutableStateOf(prefs.getBoolean("date",true))}
    var clean by remember{mutableStateOf(false)}
    var displayOptions by remember{mutableStateOf(false)}
    var wallpaperConsent by remember{mutableStateOf(false)}
    var wallpaperString by remember{mutableStateOf(prefs.getString("custom_wallpaper",null))}

    val wallpaper=wallpaperString?.let(Uri::parse)
    val font=when(fontIndex){
        1->NmixNunito
        2->NmixOutfit
        3->NmixPoppins
        4->NmixQuicksand
        else->NmixInter
    }
    val parts=parseClockTime(time)
    val clockColor=clockColorValues[colorIndex]

    fun save(key:String,value:Boolean){
        prefs.edit().putBoolean(key,value).apply()
    }

    fun exit(){
        activity?.requestedOrientation=originalOrientation
        onExit()
    }

    fun rotate(){
        activity?.requestedOrientation=
            if(config.orientation==Configuration.ORIENTATION_LANDSCAPE)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    val picker=rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri->
        if(uri!=null){
            wallpaperString=uri.toString()
            prefs.edit().putString("custom_wallpaper",uri.toString()).apply()
        }
    }

    BackHandler{exit()}

    DisposableEffect(activity){
        val window=activity?.window
        if(window!=null){
            WindowCompat.setDecorFitsSystemWindows(window,false)
            WindowInsetsControllerCompat(window,window.decorView).apply{
                hide(
                    WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars()
                )
                systemBarsBehavior=
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose{
            activity?.requestedOrientation=originalOrientation
            if(window!=null){
                WindowCompat.setDecorFitsSystemWindows(window,true)
                WindowInsetsControllerCompat(window,window.decorView).show(
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
        if(wallpaper!=null){
            FullscreenWallpaper(wallpaper)
            Box(
                Modifier.fillMaxSize().background(
                    if(a.darkMode)Color.Black.copy(alpha=.43f)
                    else Color.White.copy(alpha=.72f)
                )
            )
        }

        FullscreenWorldBackground(wallpaper!=null)

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            ClockBrand(Modifier.padding(start=19.dp,top=17.dp))
        }

        AnimatedVisibility(
            visible=!clean,
            modifier=Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            val selectors:@Composable ()->Unit={
                ClockSelector(
                    "FONT",clockFonts,fontIndex,a.palette.accent,
                    font,false
                ){i->
                    fontIndex=i
                    prefs.edit().putInt("font",i).apply()
                }

                ClockSelector(
                    "STYLE",clockStyles,styleIndex,a.palette.accent,
                    font,false
                ){i->
                    styleIndex=i
                    prefs.edit().putInt("style",i).apply()
                }

                ClockSelector(
                    "COLOR",clockColors,colorIndex,clockColor,
                    font,true
                ){i->
                    colorIndex=i
                    prefs.edit().putInt("text_color",i).apply()
                }
            }

            if(landscape){
                Row(
                    Modifier.padding(top=14.dp,end=10.dp),
                    horizontalArrangement=Arrangement.spacedBy(8.dp)
                ){selectors()}
            }else{
                Column(
                    Modifier.padding(top=14.dp,end=10.dp),
                    verticalArrangement=Arrangement.spacedBy(8.dp)
                ){selectors()}
            }
        }

        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if(landscape).92f else .96f)
                .height(if(landscape)330.dp else 390.dp),
            contentAlignment=Alignment.Center
        ){
            AnimatedContent(
                targetState=styleIndex,
                transitionSpec={
                    fadeIn(tween(180,easing=EaseOutCubic)) togetherWith
                        fadeOut(tween(130))
                },
                label="clockStyle"
            ){style->
                ClockFace(
                    style=style,
                    parts=parts,
                    date=date,
                    font=font,
                    landscape=landscape,
                    hours=showHours,
                    minutes=showMinutes,
                    seconds=showSeconds,
                    showDate=showDate,
                    clockColor=clockColor
                )
            }

            AnimatedVisibility(
                clean,
                Modifier.align(Alignment.BottomCenter)
            ){
                ClockBrand(Modifier.padding(bottom=7.dp),true)
            }
        }

        AnimatedVisibility(
            visible=!clean&&displayOptions,
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom=77.dp),
            enter=fadeIn(tween(160))+scaleIn(initialScale=.98f),
            exit=fadeOut(tween(130))
        ){
            DisplayOptions(
                showHours,showMinutes,showSeconds,showDate,
                {
                    haptic{
                        showHours=!showHours
                        save("hours",showHours)
                    }
                },{
                    haptic{
                        showMinutes=!showMinutes
                        save("minutes",showMinutes)
                    }
                },{
                    haptic{
                        showSeconds=!showSeconds
                        save("seconds",showSeconds)
                    }
                },{
                    haptic{
                        showDate=!showDate
                        save("date",showDate)
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
                Modifier.padding(horizontal=7.dp).padding(bottom=16.dp),
                horizontalArrangement=Arrangement.spacedBy(5.dp),
                verticalAlignment=Alignment.CenterVertically
            ){
                ClockAction("Wallpaper",NmixIcon.WALLPAPER,font){
                    haptic{wallpaperConsent=true}
                }
                ClockAction("Rotate",NmixIcon.ROTATE,font){
                    haptic{rotate()}
                }
                ClockAction(
                    "Display",NmixIcon.CLOCK,font,
                    selected=displayOptions
                ){
                    haptic{displayOptions=!displayOptions}
                }
                ClockAction("Clean",NmixIcon.FULLSCREEN,font){
                    haptic{
                        displayOptions=false
                        clean=true
                    }
                }
                ClockAction("Exit",NmixIcon.CLOSE,font,red=true){
                    haptic{exit()}
                }
            }
        }

        AnimatedVisibility(
            visible=wallpaperConsent,
            modifier=Modifier.align(Alignment.Center).padding(20.dp),
            enter=fadeIn(tween(170))+scaleIn(initialScale=.98f),
            exit=fadeOut(tween(130))
        ){
            WallpaperDialog(
                hasWallpaper=wallpaper!=null,
                onCancel={wallpaperConsent=false},
                onRemove={
                    wallpaperString=null
                    prefs.edit().remove("custom_wallpaper").apply()
                    wallpaperConsent=false
                },
                onChoose={
                    wallpaperConsent=false
                    picker.launch("image/*")
                }
            )
        }
    }
}

/* ---------- SELECTOR ---------- */

@Composable
private fun ClockSelector(
    title:String,
    options:List<String>,
    selected:Int,
    centerColor:Color,
    font:FontFamily,
    colorSelector:Boolean,
    onSelect:(Int)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    var width by remember{mutableIntStateOf(1)}
    var drag by remember{mutableFloatStateOf(0f)}
    val safe=selected.coerceIn(0,options.lastIndex)

    fun index(delta:Int):Int{
        var i=(safe+delta)%options.size
        if(i<0)i+=options.size
        return i
    }

    val prev=index(-1)
    val next=index(1)
    val zone=(width/3f).coerceAtLeast(1f)
    val visual=(drag/zone).coerceIn(-1f,1f)
    val shape=RoundedCornerShape(30.dp)

    Column(
        Modifier
            .width(156.dp)
            .height(64.dp)
            .clip(shape)
            .background(
                if(a.darkMode)Color(0xFF101412).copy(alpha=.96f)
                else Color.White.copy(alpha=.97f)
            )
            .background(p.accent.copy(alpha=if(a.darkMode).025f else .014f))
            .border(
                .55.dp,
                p.accent.copy(alpha=if(a.darkMode).18f else .25f),
                shape
            )
            .onSizeChanged{width=it.width.coerceAtLeast(1)}
            .pointerInput(safe,width,options.size){
                detectHorizontalDragGestures(
                    onDragStart={drag=0f},
                    onHorizontalDrag={change,amount->
                        change.consume()
                        drag=(drag+amount).coerceIn(-zone*1.15f,zone*1.15f)
                    },
                    onDragEnd={
                        when{
                            drag<=-zone*.55f->haptic{onSelect(next)}
                            drag>=zone*.55f->haptic{onSelect(prev)}
                        }
                        drag=0f
                    },
                    onDragCancel={drag=0f}
                )
            },
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Text(
            title,
            color=ui.text,
            fontSize=8.5.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.7.sp,
            fontFamily=a.fontFamily,
            modifier=Modifier.padding(top=5.dp)
        )

        Box(
            Modifier.fillMaxWidth().weight(1f)
        ){
            SelectorValue(
                options[prev],
                -1f+visual,
                visual.coerceIn(0f,1f),
                if(colorSelector)clockColorValues[prev] else centerColor,
                ui.muted,
                font,
                colorSelector
            )

            SelectorValue(
                options[safe],
                visual,
                (1f-abs(visual)).coerceIn(0f,1f),
                if(colorSelector)clockColorValues[safe] else centerColor,
                ui.muted,
                font,
                colorSelector
            )

            SelectorValue(
                options[next],
                1f+visual,
                (-visual).coerceIn(0f,1f),
                if(colorSelector)clockColorValues[next] else centerColor,
                ui.muted,
                font,
                colorSelector
            )
        }
    }
}

@Composable
private fun BoxScope.SelectorValue(
    text:String,
    position:Float,
    selected:Float,
    selectedColor:Color,
    muted:Color,
    font:FontFamily,
    colorSelector:Boolean
){
    val t=selected.coerceIn(0f,1f)

    Text(
        text.lowercase(),
        Modifier
            .align(Alignment.Center)
            .graphicsLayer{
                translationX=position*52f
                translationY=1f
                scaleX=.84f+t*.22f
                scaleY=.84f+t*.22f
            },
        color=if(colorSelector)
            lerpClockColor(muted.copy(alpha=.65f),selectedColor,t)
        else
            lerpClockColor(muted.copy(alpha=.65f),LocalNmixAppearance.current.uiColors().text,t),
        fontSize=(8f+t*4f).sp,
        fontWeight=if(t>.55f)FontWeight.Bold else FontWeight.Medium,
        fontFamily=font,
        maxLines=1,
        textAlign=TextAlign.Center
    )
}

/* ---------- WORLD ---------- */

@Composable
private fun BoxScope.FullscreenWorldBackground(wallpaper:Boolean){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val world=rememberNmixWorldMotion("fullscreenWorld")

    if(!a.animationEnabled)return

    world.bodies.forEachIndexed{index,body->
        val x=body.x*(900f+index*66f)
        val y=body.y*(760f+index*55f)
        val alpha=if(wallpaper).52f else 1f

        if(a.animation!=NmixAnimationName.FLOAT){
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
                        translationX=x
                        translationY=y
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to
                                    (if(index%2==0)p.accent else p.accentLight)
                                        .copy(alpha=(if(a.darkMode).29f else .20f)*alpha),
                                .25f to p.accent.copy(alpha=.15f*alpha),
                                .52f to p.accent.copy(alpha=.065f*alpha),
                                .75f to p.accent.copy(alpha=.018f*alpha),
                                .90f to p.accent.copy(alpha=.003f*alpha),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val size=when(index){
                0->560.dp
                1->485.dp
                2->420.dp
                3->455.dp
                else->375.dp
            }

            Canvas(
                Modifier
                    .size(size)
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=x
                        translationY=y
                        rotationZ=body.rotation
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
            ){
                val c=if(index%2==0)p.accent else p.accentLight
                val inset=14.dp.toPx()

                drawRoundRect(
                    c.copy(alpha=.024f*alpha),
                    cornerRadius=CornerRadius(55.dp.toPx())
                )

                drawRoundRect(
                    c.copy(alpha=(if(a.darkMode).145f else .11f)*alpha),
                    Offset(inset,inset),
                    Size(
                        (this.size.width-inset*2).coerceAtLeast(0f),
                        (this.size.height-inset*2).coerceAtLeast(0f)
                    ),
                    CornerRadius(43.dp.toPx())
                )
            }
        }
    }
}

/* ---------- 24 CLOCK STYLES ---------- */

@Composable
private fun ClockFace(
    style:Int,
    parts:ClockParts,
    date:String,
    font:FontFamily,
    landscape:Boolean,
    hours:Boolean,
    minutes:Boolean,
    seconds:Boolean,
    showDate:Boolean,
    clockColor:Color
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val main=when{
        clockColor!=Color.White->clockColor
        a.darkMode->Color.White.copy(alpha=.94f)
        else->Color(0xFF252A27)
    }

    val secondary=if(clockColor==Color.White){
        if(a.darkMode)p.accentLight else p.accentDark
    }else{
        clockColor.copy(alpha=.70f)
    }

    val numeric=buildList{
        if(hours)add(parts.hour)
        if(minutes)add(parts.minute)
        if(seconds)add(parts.second)
    }.joinToString(":").ifEmpty{"--"}

    val hm=buildList{
        if(hours)add(parts.hour)
        if(minutes)add(parts.minute)
    }.joinToString(":").ifEmpty{"--"}

    val big=if(landscape)82.sp else 57.sp
    val huge=if(landscape)101.sp else 68.sp
    val medium=if(landscape)64.sp else 47.sp

    when(style%24){
        0->ClockCentered(numeric,parts.period,date,font,main,secondary,big,showDate,"NMIX • LOCAL TIME")
        1->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,null)
        2->ClockStack(parts,date,font,main,secondary,landscape,hours,minutes,seconds,showDate)
        3->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,"FOCUS")
        4->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,"BOLD",FontWeight.Bold)
        5->ClockCentered(numeric,parts.period,date,font,main,secondary,medium,showDate,"SLIM",FontWeight.Normal)
        6->ClockWide(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        7->ClockCentered(numeric,parts.period,date,font,main,secondary,medium,showDate,"COMPACT")
        8->ClockHour(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        9->ClockMinute(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        10->ClockSplit(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        11->ClockCentered(numeric,parts.period,date,font,main,secondary,big,showDate,"CENTER")
        12->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,"STUDIO",FontWeight.Bold)
        13->ClockMono(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        14->ClockCentered(hm,parts.period,date,font,main,secondary,big,showDate,"CALM")
        15->ClockCentered(numeric,parts.period,date,font,main,secondary,big,showDate,null,FontWeight.Medium)
        16->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,null,FontWeight.Bold)
        17->ClockFine(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        18->ClockAir(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        19->ClockDense(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        20->ClockModern(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        21->ClockCentered(hm,parts.period,date,font,main,secondary,huge,showDate,"QUIET",FontWeight.Normal)
        22->ClockPrime(parts,date,font,main,secondary,hours,minutes,seconds,showDate,landscape)
        else->ClockCentered(numeric,parts.period,date,font,main,secondary,huge,showDate,"ESSENTIAL")
    }
}

@Composable
private fun ClockCentered(
    time:String,period:String,date:String,font:FontFamily,
    main:Color,secondary:Color,size:androidx.compose.ui.unit.TextUnit,
    showDate:Boolean,kicker:String?,weight:FontWeight=FontWeight.SemiBold
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        if(kicker!=null){
            Text(
                kicker,color=secondary,fontSize=9.sp,
                fontWeight=FontWeight.Bold,letterSpacing=2.3.sp,fontFamily=font
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(verticalAlignment=Alignment.Bottom){
            Text(time,color=main,fontSize=size,fontWeight=weight,fontFamily=font,maxLines=1)
            if(period.isNotEmpty()){
                Spacer(Modifier.width(7.dp))
                Text(
                    period,color=secondary,fontSize=13.sp,
                    fontWeight=FontWeight.Bold,fontFamily=font,
                    modifier=Modifier.padding(bottom=8.dp)
                )
            }
        }

        if(showDate){
            Spacer(Modifier.height(9.dp))
            Text(date,color=main.copy(alpha=.62f),fontSize=10.sp,fontFamily=font)
        }
    }
}

@Composable
private fun ClockStack(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    landscape:Boolean,h:Boolean,m:Boolean,s:Boolean,d:Boolean
){
    Row(
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(20.dp)
    ){
        Column{
            if(h)Text(p.hour,color=main,fontSize=if(landscape)70.sp else 54.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(m)Text(p.minute,color=secondary,fontSize=if(landscape)70.sp else 54.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Column{
            if(s)Text(p.second,color=main,fontSize=30.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Text(p.period,color=secondary,fontSize=13.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d){
                Spacer(Modifier.height(7.dp))
                Text(date,color=main.copy(alpha=.58f),fontSize=9.sp,fontFamily=font)
            }
        }
    }
}

@Composable
private fun ClockWide(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Row(
            verticalAlignment=Alignment.Bottom,
            horizontalArrangement=Arrangement.spacedBy(if(landscape)24.dp else 12.dp)
        ){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)91.sp else 59.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(m)Text(p.minute,color=main,fontSize=if(landscape)91.sp else 59.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(s)Text(p.second,color=secondary,fontSize=if(landscape)40.sp else 27.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Text(p.period,color=secondary,fontSize=12.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        if(d)Text(date,color=main.copy(alpha=.58f),fontSize=9.sp,fontFamily=font)
    }
}

@Composable
private fun ClockHour(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Row(verticalAlignment=Alignment.Bottom){
        if(h)Text(p.hour,color=main,fontSize=if(landscape)116.sp else 82.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        Column(Modifier.padding(bottom=12.dp)){
            if(m)Text(p.minute,color=secondary,fontSize=32.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(s)Text(p.second,color=main.copy(alpha=.65f),fontSize=15.sp,fontFamily=font)
            Text(p.period,color=secondary,fontSize=11.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d)Text(date,color=main.copy(alpha=.52f),fontSize=8.sp,fontFamily=font)
        }
    }
}

@Composable
private fun ClockMinute(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Row(verticalAlignment=Alignment.Bottom){
        Column(Modifier.padding(bottom=12.dp)){
            if(h)Text(p.hour,color=secondary,fontSize=31.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Text(p.period,color=secondary,fontSize=11.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Spacer(Modifier.width(8.dp))
        if(m)Text(p.minute,color=main,fontSize=if(landscape)116.sp else 82.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        if(s)Text(p.second,color=secondary,fontSize=18.sp,fontWeight=FontWeight.Bold,fontFamily=font,modifier=Modifier.padding(bottom=16.dp))
    }
    if(d)Text(date,color=main.copy(alpha=.54f),fontSize=8.sp,fontFamily=font)
}

@Composable
private fun ClockSplit(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Row(verticalAlignment=Alignment.CenterVertically){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)84.sp else 59.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(h&&m)Text(" : ",color=secondary,fontSize=35.sp,fontFamily=font)
            if(m)Text(p.minute,color=secondary,fontSize=if(landscape)84.sp else 59.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Row(verticalAlignment=Alignment.CenterVertically){
            if(s)Text(p.second,color=main.copy(alpha=.68f),fontSize=17.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Spacer(Modifier.width(8.dp))
            Text(p.period,color=secondary,fontSize=12.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d){
                Spacer(Modifier.width(12.dp))
                Text(date,color=main.copy(alpha=.55f),fontSize=9.sp,fontFamily=font)
            }
        }
    }
}

@Composable
private fun ClockMono(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.Start){
        Text("LOCAL / ${p.period}",color=secondary,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp,fontFamily=font)
        Row{
            if(h)Text(p.hour,color=main,fontSize=if(landscape)82.sp else 58.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(m)Text(":${p.minute}",color=main,fontSize=if(landscape)82.sp else 58.sp,fontWeight=FontWeight.Normal,fontFamily=font)
            if(s)Text(":${p.second}",color=secondary,fontSize=if(landscape)39.sp else 26.sp,fontFamily=font,modifier=Modifier.padding(top=13.dp))
        }
        if(d)Text(date,color=main.copy(alpha=.55f),fontSize=9.sp,fontFamily=font)
    }
}

@Composable
private fun ClockFine(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Text("TIME",color=secondary,fontSize=8.sp,letterSpacing=5.sp,fontFamily=font)
        Row(verticalAlignment=Alignment.Bottom){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)76.sp else 52.sp,fontWeight=FontWeight.Normal,fontFamily=font)
            if(m)Text(" ${p.minute}",color=main,fontSize=if(landscape)76.sp else 52.sp,fontWeight=FontWeight.Light,fontFamily=font)
            if(s)Text(" ${p.second}",color=secondary,fontSize=20.sp,fontFamily=font,modifier=Modifier.padding(bottom=7.dp))
        }
        Text(p.period,color=secondary,fontSize=10.sp,fontFamily=font)
        if(d)Text(date,color=main.copy(alpha=.5f),fontSize=8.sp,fontFamily=font)
    }
}

@Composable
private fun ClockAir(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Row(horizontalArrangement=Arrangement.spacedBy(if(landscape)30.dp else 19.dp)){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)78.sp else 53.sp,fontWeight=FontWeight.Medium,fontFamily=font)
            if(m)Text(p.minute,color=main,fontSize=if(landscape)78.sp else 53.sp,fontWeight=FontWeight.Medium,fontFamily=font)
            if(s)Text(p.second,color=secondary,fontSize=if(landscape)36.sp else 24.sp,fontWeight=FontWeight.Medium,fontFamily=font)
        }
        Spacer(Modifier.height(6.dp))
        Text(p.period,color=secondary,fontSize=11.sp,letterSpacing=2.sp,fontFamily=font)
        if(d)Text(date,color=main.copy(alpha=.52f),fontSize=9.sp,fontFamily=font)
    }
}

@Composable
private fun ClockDense(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Row(verticalAlignment=Alignment.Bottom){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)92.sp else 64.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(m)Text(p.minute,color=main,fontSize=if(landscape)92.sp else 64.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Row{
            if(s)Text("SEC ${p.second}",color=secondary,fontSize=10.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Spacer(Modifier.width(10.dp))
            Text(p.period,color=secondary,fontSize=10.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d){
                Spacer(Modifier.width(10.dp))
                Text(date,color=main.copy(alpha=.55f),fontSize=8.sp,fontFamily=font)
            }
        }
    }
}

@Composable
private fun ClockModern(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Column(horizontalAlignment=Alignment.Start){
        Text("NMIX TIME",color=secondary,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=2.8.sp,fontFamily=font)
        Row(verticalAlignment=Alignment.Bottom){
            if(h)Text(p.hour,color=main,fontSize=if(landscape)91.sp else 62.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(m)Text(":${p.minute}",color=main,fontSize=if(landscape)91.sp else 62.sp,fontWeight=FontWeight.Medium,fontFamily=font)
            if(s)Text(p.second,color=secondary,fontSize=20.sp,fontWeight=FontWeight.Bold,fontFamily=font,modifier=Modifier.padding(start=7.dp,bottom=9.dp))
        }
        Row{
            Text(p.period,color=secondary,fontSize=11.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d){
                Spacer(Modifier.width(12.dp))
                Text(date,color=main.copy(alpha=.56f),fontSize=9.sp,fontFamily=font)
            }
        }
    }
}

@Composable
private fun ClockPrime(
    p:ClockParts,date:String,font:FontFamily,main:Color,secondary:Color,
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,landscape:Boolean
){
    Row(verticalAlignment=Alignment.CenterVertically){
        Column(horizontalAlignment=Alignment.End){
            if(h)Text(p.hour,color=secondary,fontSize=if(landscape)46.sp else 35.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Text(p.period,color=secondary,fontSize=10.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        }
        Spacer(Modifier.width(10.dp))
        if(m)Text(p.minute,color=main,fontSize=if(landscape)112.sp else 78.sp,fontWeight=FontWeight.Bold,fontFamily=font)
        Column{
            if(s)Text(p.second,color=secondary,fontSize=21.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(d)Text(date,color=main.copy(alpha=.52f),fontSize=8.sp,fontFamily=font)
        }
    }
}

/* ---------- DISPLAY / ACTIONS ---------- */

@Composable
private fun DisplayOptions(
    h:Boolean,m:Boolean,s:Boolean,d:Boolean,
    onH:()->Unit,onM:()->Unit,onS:()->Unit,onD:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)

    Row(
        Modifier
            .clip(shape)
            .background(
                if(a.darkMode)Color(0xFF141917).copy(alpha=.96f)
                else Color.White.copy(alpha=.97f)
            )
            .border(.55.dp,p.accent.copy(alpha=if(a.darkMode).18f else .27f),shape)
            .padding(8.dp),
        horizontalArrangement=Arrangement.spacedBy(6.dp)
    ){
        DisplayChoice("H",h,onH)
        DisplayChoice("M",m,onM)
        DisplayChoice("S",s,onS)
        DisplayChoice("D",d,onD)
    }
}

@Composable
private fun DisplayChoice(text:String,selected:Boolean,onClick:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val t by animateFloatAsState(if(selected)1f else 0f,tween(170),label="displayChoice")
    val shape=RoundedCornerShape(50)

    Box(
        Modifier
            .width(44.dp).height(39.dp)
            .clip(shape)
            .background(if(a.darkMode)Color(0xFF111614) else Color.White)
            .background(p.accent.copy(alpha=.015f+t*.06f))
            .border(
                (.45f+t*.60f).dp,
                p.accent.copy(alpha=.16f+t*.36f),
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
    val fg=if(red)Color(0xFFE66E75) else ui.text

    Row(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(
                if(a.darkMode)Color(0xFF131816).copy(alpha=.95f)
                else Color.White.copy(alpha=.95f)
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
                    red->fg.copy(alpha=.28f)
                    selected->p.accent.copy(alpha=.50f)
                    else->p.accent.copy(alpha=if(a.darkMode).17f else .25f)
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
        NmixIcon(icon,Modifier.size(16.dp),if(red)fg else p.accent)
        Text(
            text,color=fg,fontSize=9.sp,fontWeight=FontWeight.SemiBold,
            fontFamily=font,maxLines=1
        )
    }
}

/* ---------- WALLPAPER ---------- */

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
            "CUSTOM WALLPAPER",color=p.accent,fontSize=9.sp,
            fontWeight=FontWeight.Bold,letterSpacing=.8.sp,fontFamily=a.fontFamily
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Choose an image for Fullscreen Clock.",
            color=ui.muted,fontSize=9.sp,lineHeight=14.sp,fontFamily=a.fontFamily
        )
        Spacer(Modifier.height(13.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(7.dp)
        ){
            DialogButton("Cancel",Modifier.weight(1f),onClick=onCancel)

            if(hasWallpaper){
                DialogButton(
                    "Remove",Modifier.weight(1f),
                    red=true,onClick=onRemove
                )
            }

            DialogButton(
                "Choose",Modifier.weight(1f),
                accent=true,onClick=onChoose
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
                if(red)Color(0xFFE66E75).copy(alpha=.26f)
                else p.accent.copy(alpha=.22f),
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

/* ---------- BRAND / PARSE / IMAGE ---------- */

@Composable
private fun ClockBrand(
    modifier:Modifier=Modifier,
    centered:Boolean=false
){
    val a=LocalNmixAppearance.current
    val c=if(a.darkMode)Color.White else a.uiColors().text

    Column(
        modifier,
        horizontalAlignment=
            if(centered)Alignment.CenterHorizontally else Alignment.Start
    ){
        Text(
            "EVERYTHING WITH NUMBERS",
            color=c.copy(alpha=.58f),
            fontSize=7.sp,
            letterSpacing=1.5.sp,
            fontFamily=a.fontFamily
        )
        Text(
            "NMIX",
            color=c,
            fontSize=24.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=2.sp,
            fontFamily=NmixLogoFont
        )
    }
}

private fun parseClockTime(time:String):ClockParts{
    val t=time.trim()

    val period=when{
        t.endsWith(" AM",true)->"AM"
        t.endsWith(" PM",true)->"PM"
        else->""
    }

    val raw=t
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

private fun lerpClockColor(start:Color,end:Color,amount:Float):Color{
    val t=amount.coerceIn(0f,1f)
    return Color(
        start.red+(end.red-start.red)*t,
        start.green+(end.green-start.green)*t,
        start.blue+(end.blue-start.blue)*t,
        start.alpha+(end.alpha-start.alpha)*t
    )
}

@Composable
private fun FullscreenWallpaper(uri:Uri){
    val context=LocalContext.current
    var bitmap by remember(uri){mutableStateOf<ImageBitmap?>(null)}

    LaunchedEffect(uri){
        bitmap=withContext(Dispatchers.IO){
            try{
                context.contentResolver.openInputStream(uri)?.use{
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
