package com.lxzrvi.nmix

import android.app.Activity
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
    val hour:String,val minute:String,val second:String,val period:String
)

private val clockStyles=listOf("Digital","Minimal","Stack","Focus")
private val clockFonts=listOf("Inter","Nunito","Outfit","Poppins","Quicksand")
private val clockColors=listOf("White","Green","Blue","Purple","Orange","Rose","Cyan")
private val clockColorValues=listOf(
    Color.White,Color(0xFF6CBBA1),Color(0xFF71B4D8),
    Color(0xFFB295DD),Color(0xFFE4A16B),Color(0xFFDC8AA2),Color(0xFF68C6D0)
)

@Composable
fun NmixFullscreenClock(time:String,date:String,onExit:()->Unit){
    val context=LocalContext.current
    val activity=LocalActivity.current
    val config=LocalConfiguration.current
    val a=LocalNmixAppearance.current
    val haptic=rememberNmixHapticAction()
    val prefs=remember(context){context.getSharedPreferences(CLOCK_PREFS,android.content.Context.MODE_PRIVATE)}
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

    var fontIndex by remember{mutableIntStateOf(prefs.getInt("font",initialFont).coerceIn(0,clockFonts.lastIndex))}
    var styleIndex by remember{mutableIntStateOf(prefs.getInt("style",0).coerceIn(0,clockStyles.lastIndex))}
    var colorIndex by remember{mutableIntStateOf(prefs.getInt("text_color",0).coerceIn(0,clockColors.lastIndex))}
    var showHours by remember{mutableStateOf(prefs.getBoolean("hours",true))}
    var showMinutes by remember{mutableStateOf(prefs.getBoolean("minutes",true))}
    var showSeconds by remember{mutableStateOf(prefs.getBoolean("seconds",true))}
    var showDate by remember{mutableStateOf(prefs.getBoolean("date",true))}
    var clean by remember{mutableStateOf(false)}
    var displayOptions by remember{mutableStateOf(false)}
    var wallpaperConsent by remember{mutableStateOf(false)}
    var customWallpaperString by remember{mutableStateOf(prefs.getString("custom_wallpaper",null))}

    val wallpaper=customWallpaperString?.let(Uri::parse)
    val font=when(fontIndex){
        1->NmixNunito
        2->NmixOutfit
        3->NmixPoppins
        4->NmixQuicksand
        else->NmixInter
    }
    val parts=parseFullscreenTime(time)
    val clockColor=clockColorValues[colorIndex]

    fun save(key:String,value:Boolean)=prefs.edit().putBoolean(key,value).apply()

    fun exit(){
        activity?.requestedOrientation=originalOrientation
        onExit()
    }

    /*
     * Rotate before Compose sees the new Configuration.
     * Manifest configChanges keeps this Activity alive, so fullscreen state
     * does not disappear during rotation.
     */
    fun rotate(){
        val target=if(config.orientation==Configuration.ORIENTATION_LANDSCAPE)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.requestedOrientation=target
    }

    val imagePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
        if(uri!=null){
            customWallpaperString=uri.toString()
            prefs.edit().putString("custom_wallpaper",uri.toString()).apply()
        }
    }

    BackHandler{exit()}

    DisposableEffect(activity){
        val window=activity?.window
        if(window!=null){
            WindowCompat.setDecorFitsSystemWindows(window,false)
            WindowInsetsControllerCompat(window,window.decorView).apply{
                hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        onDispose{
            activity?.requestedOrientation=originalOrientation
            if(window!=null){
                WindowCompat.setDecorFitsSystemWindows(window,true)
                WindowInsetsControllerCompat(window,window.decorView)
                    .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(if(a.darkMode)Color(0xFF050807) else Color(0xFFF8FAF9))
            .clickable(
                interactionSource=remember{MutableInteractionSource()},indication=null
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
            !clean,
            Modifier.align(Alignment.TopStart).windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            ClockBrand(Modifier.padding(start=19.dp,top=17.dp))
        }

        AnimatedVisibility(
            !clean,
            Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.safeDrawing)
        ){
            if(landscape){
                Row(
                    Modifier.padding(top=14.dp,end=10.dp),
                    horizontalArrangement=Arrangement.spacedBy(8.dp)
                ){
                    DragSelector("FONT",clockFonts,fontIndex,a.palette.accent,font){i->
                        fontIndex=i;prefs.edit().putInt("font",i).apply()
                    }
                    DragSelector("STYLE",clockStyles,styleIndex,a.palette.accent,font){i->
                        styleIndex=i;prefs.edit().putInt("style",i).apply()
                    }
                    DragSelector("COLOR",clockColors,colorIndex,clockColor,font,true){i->
                        colorIndex=i;prefs.edit().putInt("text_color",i).apply()
                    }
                }
            }else{
                Column(
                    Modifier.padding(top=14.dp,end=10.dp),
                    verticalArrangement=Arrangement.spacedBy(8.dp)
                ){
                    DragSelector("FONT",clockFonts,fontIndex,a.palette.accent,font){i->
                        fontIndex=i;prefs.edit().putInt("font",i).apply()
                    }
                    DragSelector("STYLE",clockStyles,styleIndex,a.palette.accent,font){i->
                        styleIndex=i;prefs.edit().putInt("style",i).apply()
                    }
                    DragSelector("COLOR",clockColors,colorIndex,clockColor,font,true){i->
                        colorIndex=i;prefs.edit().putInt("text_color",i).apply()
                    }
                }
            }
        }

        Box(
            Modifier.align(Alignment.Center)
                .fillMaxWidth(if(landscape).90f else .95f)
                .height(if(landscape)330.dp else 370.dp),
            contentAlignment=Alignment.Center
        ){
            AnimatedContent(
                styleIndex,
                transitionSpec={
                    (fadeIn(tween(240,easing=EaseOutCubic))+scaleIn(initialScale=.985f)) togetherWith
                        (fadeOut(tween(170))+scaleOut(targetScale=1.008f))
                },
                label="clockStyle"
            ){style->
                ClockFace(
                    style,parts,date,font,landscape,
                    showHours,showMinutes,showSeconds,showDate,clockColor
                )
            }

            AnimatedVisibility(clean,Modifier.align(Alignment.BottomCenter)){
                ClockBrand(Modifier.padding(bottom=7.dp),true)
            }
        }

        AnimatedVisibility(
            !clean&&displayOptions,
            Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom=77.dp),
            enter=fadeIn(tween(180))+scaleIn(initialScale=.97f),
            exit=fadeOut(tween(150))
        ){
            DisplayOptions(
                showHours,showMinutes,showSeconds,showDate,
                {
                    haptic{showHours=!showHours;save("hours",showHours)}
                },{
                    haptic{showMinutes=!showMinutes;save("minutes",showMinutes)}
                },{
                    haptic{showSeconds=!showSeconds;save("seconds",showSeconds)}
                },{
                    haptic{showDate=!showDate;save("date",showDate)}
                }
            )
        }

        AnimatedVisibility(
            !clean,
            Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing)
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
                ClockAction("Display",NmixIcon.CLOCK,font,selected=displayOptions){
                    haptic{displayOptions=!displayOptions}
                }
                ClockAction("Clean",NmixIcon.FULLSCREEN,font){
                    haptic{displayOptions=false;clean=true}
                }
                ClockAction("Exit",NmixIcon.CLOSE,font,red=true){
                    haptic{exit()}
                }
            }
        }

        AnimatedVisibility(
            wallpaperConsent,
            Modifier.align(Alignment.Center).padding(20.dp),
            enter=fadeIn(tween(180))+scaleIn(initialScale=.97f),
            exit=fadeOut(tween(140))
        ){
            WallpaperDialog(
                wallpaper!=null,
                {wallpaperConsent=false},
                {
                    customWallpaperString=null
                    prefs.edit().remove("custom_wallpaper").apply()
                    wallpaperConsent=false
                },
                {
                    wallpaperConsent=false
                    imagePicker.launch("image/*")
                }
            )
        }
    }
}

@Composable
private fun DragSelector(
    title:String,options:List<String>,selected:Int,
    centerColor:Color,font:FontFamily,colorSelector:Boolean=false,
    onSelect:(Int)->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()
    var widthPx by remember{mutableIntStateOf(1)}
    var drag by remember{mutableFloatStateOf(0f)}
    val safe=selected.coerceIn(0,options.lastIndex)

    fun index(delta:Int):Int{
        var i=(safe+delta)%options.size
        if(i<0)i+=options.size
        return i
    }

    val prev=index(-1)
    val next=index(1)
    val travel=(widthPx*.24f).coerceAtLeast(1f)
    val visual=(drag/travel).coerceIn(-1f,1f)
    val shape=RoundedCornerShape(50)

    Column(
        Modifier.width(150.dp).height(56.dp).clip(shape)
            .background(
                if(a.darkMode)Color(0xFF131816).copy(alpha=.94f)
                else Color.White.copy(alpha=.95f)
            )
            .background(p.accent.copy(alpha=if(a.darkMode).027f else .018f))
            .border(.55.dp,p.accent.copy(alpha=if(a.darkMode).18f else .27f),shape)
            .onSizeChanged{widthPx=it.width.coerceAtLeast(1)}
            .pointerInput(safe,widthPx,options.size){
                detectHorizontalDragGestures(
                    onDragStart={drag=0f},
                    onHorizontalDrag={change,amount->
                        change.consume()
                        drag=(drag+amount).coerceIn(-travel*1.15f,travel*1.15f)
                    },
                    onDragEnd={
                        when{
                            drag<=-travel*.58f->haptic{onSelect(next)}
                            drag>=travel*.58f->haptic{onSelect(prev)}
                        }
                        drag=0f
                    },
                    onDragCancel={drag=0f}
                )
            },
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Text(
            title,color=p.accent,fontSize=7.3.sp,fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,fontFamily=a.fontFamily,
            modifier=Modifier.padding(top=6.dp,bottom=2.dp)
        )

        Box(Modifier.fillMaxWidth().weight(1f)){
            SelectorText(
                options[prev],-1f+visual,visual.coerceIn(0f,1f),
                if(colorSelector)clockColorValues[prev] else centerColor,ui.muted,font
            )
            SelectorText(
                options[safe],visual,(1f-abs(visual)).coerceIn(0f,1f),
                if(colorSelector)clockColorValues[safe] else centerColor,ui.muted,font
            )
            SelectorText(
                options[next],1f+visual,(-visual).coerceIn(0f,1f),
                if(colorSelector)clockColorValues[next] else centerColor,ui.muted,font
            )
        }
    }
}

@Composable
private fun BoxScope.SelectorText(
    text:String,position:Float,amount:Float,
    selectedColor:Color,muted:Color,font:FontFamily
){
    val t=amount.coerceIn(0f,1f)
    Text(
        text,
        Modifier.align(Alignment.Center).graphicsLayer{
            translationX=position*54f
            scaleX=.88f+t*.14f
            scaleY=.88f+t*.14f
        },
        color=lerpColor(muted.copy(alpha=.70f),selectedColor,t),
        fontSize=(8.3f+t*2.2f).sp,
        fontWeight=if(t>.52f)FontWeight.Bold else FontWeight.Medium,
        fontFamily=font,maxLines=1,textAlign=TextAlign.Center
    )
}

@Composable
private fun BoxScope.FullscreenWorldBackground(wallpaper:Boolean){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val world=rememberNmixWorldMotion("fullscreenWorld")

    if(!a.animationEnabled)return

    world.bodies.forEachIndexed{index,body->
        val x=body.x*(900f+index*66f)
        val y=body.y*(760f+index*55f)
        val alpha=if(wallpaper).55f else 1f

        if(a.animation!=NmixAnimationName.FLOAT){
            Box(
                Modifier.size(
                    when(index){0->1480.dp;1->1280.dp;2->1110.dp;3->1190.dp;else->980.dp}
                ).align(Alignment.Center).graphicsLayer{
                    translationX=x;translationY=y
                    scaleX=body.pulse;scaleY=body.pulse
                }.background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to (if(index%2==0)p.accent else p.accentLight)
                                .copy(alpha=(if(a.darkMode).29f else .20f)*alpha),
                            .25f to p.accent.copy(alpha=.15f*alpha),
                            .52f to p.accent.copy(alpha=.065f*alpha),
                            .75f to p.accent.copy(alpha=.018f*alpha),
                            .90f to p.accent.copy(alpha=.003f*alpha),
                            1f to Color.Transparent
                        )
                    ),CircleShape
                )
            )
        }else{
            val s=when(index){0->560.dp;1->485.dp;2->420.dp;3->455.dp;else->375.dp}
            Canvas(
                Modifier.size(s).align(Alignment.Center).graphicsLayer{
                    translationX=x;translationY=y;rotationZ=body.rotation
                    scaleX=body.pulse;scaleY=body.pulse
                }
            ){
                val c=if(index%2==0)p.accent else p.accentLight
                val inset=14.dp.toPx()
                drawRoundRect(c.copy(alpha=.025f*alpha),cornerRadius=CornerRadius(55.dp.toPx()))
                drawRoundRect(
                    c.copy(alpha=(if(a.darkMode).145f else .11f)*alpha),
                    Offset(inset,inset),
                    Size((size.width-inset*2).coerceAtLeast(0f),(size.height-inset*2).coerceAtLeast(0f)),
                    CornerRadius(43.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun ClockFace(
    style:Int,parts:ClockParts,date:String,font:FontFamily,landscape:Boolean,
    hours:Boolean,minutes:Boolean,seconds:Boolean,showDate:Boolean,clockColor:Color
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
    }else clockColor.copy(alpha=.74f)

    val numeric=buildList{
        if(hours)add(parts.hour)
        if(minutes)add(parts.minute)
        if(seconds)add(parts.second)
    }.joinToString(":").ifEmpty{"--"}

    when(style){
        1->Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text(numeric,color=main,fontSize=if(landscape)78.sp else 55.sp,fontWeight=FontWeight.Bold,fontFamily=font,maxLines=1)
            Text(parts.period,color=secondary,fontSize=14.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            if(showDate){
                Spacer(Modifier.height(10.dp))
                Text(date,color=main.copy(alpha=.64f),fontSize=11.sp,fontFamily=font)
            }
        }

        2->Row(
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(20.dp)
        ){
            Column{
                if(hours)Text(parts.hour,color=main,fontSize=if(landscape)68.sp else 52.sp,fontWeight=FontWeight.Bold,fontFamily=font)
                if(minutes)Text(parts.minute,color=secondary,fontSize=if(landscape)68.sp else 52.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            }
            Column{
                if(seconds)Text(parts.second,color=main,fontSize=31.sp,fontWeight=FontWeight.Bold,fontFamily=font)
                Text(parts.period,color=secondary,fontSize=13.sp,fontWeight=FontWeight.Bold,fontFamily=font)
                if(showDate){
                    Spacer(Modifier.height(8.dp))
                    Text(date,color=main.copy(alpha=.60f),fontSize=9.sp,fontFamily=font)
                }
            }
        }

        3->Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text("FOCUS",color=secondary,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=3.5.sp,fontFamily=font)
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment=Alignment.Bottom){
                Text(numeric,color=main,fontSize=if(landscape)82.sp else 57.sp,fontWeight=FontWeight.Bold,fontFamily=font,maxLines=1)
                Spacer(Modifier.width(7.dp))
                Text(parts.period,color=secondary,fontSize=14.sp,fontWeight=FontWeight.Bold,fontFamily=font,modifier=Modifier.padding(bottom=8.dp))
            }
            if(showDate){
                Spacer(Modifier.height(9.dp))
                Text(date,color=main.copy(alpha=.60f),fontSize=10.sp,fontFamily=font)
            }
        }

        else->Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text("NMIX • LOCAL TIME",color=secondary,fontSize=10.sp,letterSpacing=1.9.sp,fontWeight=FontWeight.Bold,fontFamily=font)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment=Alignment.Bottom){
                Text(numeric,color=main,fontSize=if(landscape)78.sp else 55.sp,fontWeight=FontWeight.Bold,fontFamily=font,maxLines=1)
                Spacer(Modifier.width(8.dp))
                Text(parts.period,color=secondary,fontSize=14.sp,fontWeight=FontWeight.Bold,fontFamily=font,modifier=Modifier.padding(bottom=8.dp))
            }
            if(showDate){
                Spacer(Modifier.height(10.dp))
                Text(date,color=main.copy(alpha=.67f),fontSize=12.sp,fontFamily=font)
            }
        }
    }
}

@Composable
private fun DisplayOptions(
    hours:Boolean,minutes:Boolean,seconds:Boolean,date:Boolean,
    onHours:()->Unit,onMinutes:()->Unit,onSeconds:()->Unit,onDate:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)
    Row(
        Modifier.clip(shape)
            .background(if(a.darkMode)Color(0xFF141917).copy(alpha=.95f) else Color.White.copy(alpha=.96f))
            .background(p.accent.copy(alpha=.025f))
            .border(.55.dp,p.accent.copy(alpha=if(a.darkMode).18f else .27f),shape)
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
private fun DisplayChoice(text:String,selected:Boolean,onClick:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val progress by animateFloatAsState(if(selected)1f else 0f,tween(190),label="displayChoice")
    val shape=RoundedCornerShape(50)

    Box(
        Modifier.width(44.dp).height(39.dp).clip(shape)
            .background(if(a.darkMode)Color(0xFF111614) else Color.White)
            .background(p.accent.copy(alpha=.018f+progress*.055f))
            .border((.45f+progress*.6f).dp,p.accent.copy(alpha=.16f+progress*.36f),shape)
            .clickable(
                interactionSource=remember{MutableInteractionSource()},indication=null,onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,color=if(selected)p.accent else ui.text,fontSize=10.5.sp,
            fontWeight=FontWeight.Bold,fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun ClockAction(
    text:String,icon:NmixIcon,font:FontFamily,
    red:Boolean=false,selected:Boolean=false,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(50)
    val fg=if(red)Color(0xFFE66E75) else ui.text

    Row(
        Modifier.height(44.dp).clip(shape)
            .background(if(a.darkMode)Color(0xFF131816).copy(alpha=.94f) else Color.White.copy(alpha=.94f))
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
                },shape
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},indication=null,onClick=onClick
            ).padding(horizontal=10.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(6.dp)
    ){
        NmixIcon(icon,Modifier.size(16.dp),if(red)fg else p.accent)
        Text(text,color=fg,fontSize=9.sp,fontWeight=FontWeight.SemiBold,fontFamily=font,maxLines=1)
    }
}

@Composable
private fun WallpaperDialog(
    hasWallpaper:Boolean,onCancel:()->Unit,onRemove:()->Unit,onChoose:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val shape=RoundedCornerShape(22.dp)

    Column(
        Modifier.width(286.dp).clip(shape)
            .background(if(a.darkMode)Color(0xFF151A18) else Color(0xFFF8FAF9))
            .background(p.accent.copy(alpha=.022f))
            .border(.55.dp,p.accent.copy(alpha=.27f),shape)
            .padding(15.dp)
    ){
        Text("CUSTOM WALLPAPER",color=p.accent,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=.8.sp,fontFamily=a.fontFamily)
        Spacer(Modifier.height(6.dp))
        Text("Choose an image for Fullscreen Clock.",color=ui.muted,fontSize=9.sp,lineHeight=14.sp,fontFamily=a.fontFamily)
        Spacer(Modifier.height(13.dp))

        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            DialogButton("Cancel",Modifier.weight(1f),onClick=onCancel)
            if(hasWallpaper)DialogButton("Remove",Modifier.weight(1f),red=true,onClick=onRemove)
            DialogButton("Choose",Modifier.weight(1f),accent=true,onClick=onChoose)
        }
    }
}

@Composable
private fun DialogButton(
    text:String,modifier:Modifier,accent:Boolean=false,red:Boolean=false,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()
    val shape=RoundedCornerShape(50)

    Box(
        modifier.height(40.dp).clip(shape)
            .background(
                when{
                    accent->p.accent.copy(alpha=.82f)
                    red->Color(0xFFD94F57).copy(alpha=.11f)
                    a.darkMode->Color(0xFF111614)
                    else->Color.White
                }
            )
            .border(.5.dp,if(red)Color(0xFFE66E75).copy(alpha=.26f) else p.accent.copy(alpha=.22f),shape)
            .clickable(
                interactionSource=remember{MutableInteractionSource()},indication=null
            ){haptic(onClick)},
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=when{accent->Color.White;red->Color(0xFFE66E75);else->ui.text},
            fontSize=9.sp,fontWeight=FontWeight.Bold,fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun ClockBrand(modifier:Modifier=Modifier,centered:Boolean=false){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val c=if(a.darkMode)Color.White else ui.text
    Column(
        modifier,
        horizontalAlignment=if(centered)Alignment.CenterHorizontally else Alignment.Start
    ){
        Text("EVERYTHING WITH NUMBERS",color=c.copy(alpha=.58f),fontSize=7.sp,letterSpacing=1.5.sp,fontFamily=a.fontFamily)
        Text("NMIX",color=c,fontSize=24.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp,fontFamily=NmixLogoFont)
    }
}

private fun parseFullscreenTime(time:String):ClockParts{
    val t=time.trim()
    val period=when{
        t.endsWith(" AM",true)->"AM"
        t.endsWith(" PM",true)->"PM"
        else->""
    }
    val raw=t.removeSuffix(" AM").removeSuffix(" PM").removeSuffix(" am").removeSuffix(" pm")
    val p=raw.split(":")
    return ClockParts(p.getOrElse(0){"00"},p.getOrElse(1){"00"},p.getOrElse(2){"00"},period)
}

private fun lerpColor(a:Color,b:Color,t0:Float):Color{
    val t=t0.coerceIn(0f,1f)
    return Color(
        a.red+(b.red-a.red)*t,
        a.green+(b.green-a.green)*t,
        a.blue+(b.blue-a.blue)*t,
        a.alpha+(b.alpha-a.alpha)*t
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
            }catch(_:Exception){null}
        }
    }

    bitmap?.let{
        Image(
            it,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop
        )
    }
}
