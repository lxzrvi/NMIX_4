package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent{NmixApp()}
    }
}

@Composable
fun NmixApp(){
    val context=LocalContext.current
    val appearance=rememberNmixAppearance(context)

    val prefs=remember{
        context.getSharedPreferences(
            "nmix_preferences",
            Context.MODE_PRIVATE
        )
    }

    var main by remember{
        mutableStateOf(
            prefs.getString(
                "home_screen",
                "landing"
            )=="main"
        )
    }

    var loading by remember{
        mutableStateOf(true)
    }

    LaunchedEffect(Unit){
        delay(1550)
        loading=false
    }

    ProvideNmixAppearance(appearance){
        AnimatedContent(
            targetState=loading,
            transitionSpec={
                fadeIn(tween(430)) togetherWith
                    fadeOut(tween(480))
            },
            label="launch"
        ){showLoading->
            if(showLoading){
                NmixLaunchScreen()
            }else{
                AnimatedContent(
                    targetState=main,
                    transitionSpec={
                        (
                            fadeIn(tween(420))+
                            scaleIn(
                                initialScale=.988f,
                                animationSpec=tween(420)
                            )
                        ) togetherWith (
                            fadeOut(tween(280))+
                            scaleOut(
                                targetScale=.988f,
                                animationSpec=tween(280)
                            )
                        )
                    },
                    label="page"
                ){showMain->
                    if(showMain){
                        NativeMainPageV2(
                            onBack={
                                prefs.edit()
                                    .putString(
                                        "home_screen",
                                        "landing"
                                    )
                                    .apply()
                                main=false
                            }
                        )
                    }else{
                        LandingScreen(
                            onStart={
                                prefs.edit()
                                    .putString(
                                        "home_screen",
                                        "main"
                                    )
                                    .apply()
                                main=true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NmixLaunchScreen(){
    val a=LocalNmixAppearance.current
    val p=a.palette

    var entered by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        entered=true
    }

    val logoScale by animateFloatAsState(
        targetValue=if(entered)1f else .78f,
        animationSpec=spring(
            dampingRatio=.72f,
            stiffness=210f
        ),
        label="launchScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue=if(entered)1f else 0f,
        animationSpec=tween(650),
        label="launchAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue=if(entered).74f else 0f,
        animationSpec=tween(
            durationMillis=700,
            delayMillis=220
        ),
        label="tagline"
    )

    val motion=rememberInfiniteTransition(
        label="launchGlow"
    )

    val glowScale by motion.animateFloat(
        initialValue=.75f,
        targetValue=1.25f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                1500,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="glowScale"
    )

    val glowAlpha by motion.animateFloat(
        initialValue=.22f,
        targetValue=.48f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                1200,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="glowAlpha"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        p.topDark,
                        p.accentDark,
                        p.topEnd
                    )
                )
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(330.dp)
                .graphicsLayer{
                    scaleX=glowScale
                    scaleY=glowScale
                    alpha=glowAlpha
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(alpha=.72f),
                            p.accent.copy(alpha=.22f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            Modifier
                .graphicsLayer{
                    scaleX=logoScale
                    scaleY=logoScale
                    alpha=logoAlpha
                },
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                text="NMIX",
                color=Color.White,
                fontSize=50.sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=3.sp,
                fontFamily=NmixLogoFont
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text="EVERYTHING WITH NUMBERS",
                color=Color.White,
                modifier=Modifier.graphicsLayer{
                    alpha=taglineAlpha
                },
                fontSize=8.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
private fun LandingScreen(
    onStart:()->Unit
){
    val context=LocalContext.current
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val motion=rememberInfiniteTransition(
        label="landingMotion"
    )

    val move1 by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                6200,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="move1"
    )

    val move2 by motion.animateFloat(
        initialValue=1f,
        targetValue=-1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                7900,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="move2"
    )

    val move3 by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                9700,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="move3"
    )

    val pulse by motion.animateFloat(
        initialValue=.84f,
        targetValue=1.18f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                4600,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="pulse"
    )

    val bg=if(a.darkMode){
        Brush.verticalGradient(
            listOf(
                Color(0xFF030504),
                Color(0xFF080D0B),
                p.topDark.copy(alpha=.80f),
                Color(0xFF050807),
                Color(0xFF020302)
            )
        )
    }else{
        Brush.verticalGradient(
            listOf(
                p.topDark,
                p.accentDark,
                p.accent,
                p.topEnd
            )
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
    ){
        LandingGlow(
            modifier=Modifier
                .align(Alignment.TopStart)
                .offset(
                    x=(-230).dp,
                    y=(-210).dp
                )
                .graphicsLayer{
                    translationX=move1*330f
                    translationY=move2*165f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=p.accentLight,
            alpha=if(a.darkMode).22f else .42f,
            size=620
        )

        LandingGlow(
            modifier=Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x=250.dp,
                    y=230.dp
                )
                .graphicsLayer{
                    translationX=move2*350f
                    translationY=move3*195f
                    scaleX=1.12f
                    scaleY=1.12f
                },
            color=p.accent,
            alpha=if(a.darkMode).25f else .46f,
            size=690
        )

        LandingGlow(
            modifier=Modifier
                .align(Alignment.Center)
                .offset(
                    x=(-100).dp,
                    y=(-40).dp
                )
                .graphicsLayer{
                    translationX=move3*290f
                    translationY=move1*220f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=p.accentLight,
            alpha=if(a.darkMode).15f else .28f,
            size=540
        )

        Column(
            modifier=Modifier
                .align(Alignment.Center)
                .offset(y=(-58).dp)
                .padding(horizontal=22.dp),
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                text="EVERYTHING WITH NUMBERS",
                color=Color.White.copy(alpha=.74f),
                fontSize=9.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text="NMIX",
                color=Color.White,
                fontSize=52.sp,
                letterSpacing=3.5.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(Modifier.height(27.dp))

            LandingButton(
                text="Start",
                onClick=onStart
            )

            Spacer(Modifier.height(10.dp))

            LandingButton(
                text="Share",
                onClick={
                    val intent=Intent(
                        Intent.ACTION_SEND
                    ).apply{
                        type="text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                        )
                    }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Share NMIX"
                        )
                    )
                }
            )
        }

        Column(
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal=15.dp)
                .padding(bottom=53.dp),
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            LandingInfo()
        }

        Row(
            modifier=Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom=10.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Text(
                text="NMIX",
                color=Color.White.copy(alpha=.92f),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                text="  •  lxzrvi  •  © 2026",
                color=Color.White.copy(alpha=.66f),
                fontSize=11.sp,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
private fun LandingGlow(
    modifier:Modifier,
    color:Color,
    alpha:Float,
    size:Int
){
    Box(
        modifier
            .size(size.dp)
            .background(
                Brush.radialGradient(
                    colorStops=arrayOf(
                        0f to color.copy(alpha=alpha),
                        .24f to color.copy(alpha=alpha*.82f),
                        .52f to color.copy(alpha=alpha*.45f),
                        .78f to color.copy(alpha=alpha*.14f),
                        1f to Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

@Composable
private fun LandingButton(
    text:String,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).96f else 1f,
        spring(
            dampingRatio=.68f,
            stiffness=650f
        ),
        label="landingPress"
    )

    val glass=if(a.darkMode)
        Color.Black.copy(alpha=.30f)
    else
        ui.glass.copy(alpha=.38f)

    Box(
        Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(glass)
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text=text,
            color=Color.White,
            fontSize=13.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun LandingInfo(){
    val context=LocalContext.current
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val messages=remember{
        listOf(
            "NMIX brings useful number tools together in one focused native app.",
            "Calculate values, track time, count things and generate numbers from one place.",
            "Built for a fast native experience with core tools available completely offline.",
            "EVERYTHING WITH NUMBERS — calculations, counting and time tools together."
        )
    }

    var index by remember{
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit){
        while(true){
            delay(4300)
            index=(index+1)%messages.size
        }
    }

    val outerGlass=if(a.darkMode)
        Color.Black.copy(alpha=.30f)
    else
        ui.glass.copy(alpha=.34f)

    val innerGlass=if(a.darkMode)
        Color.Black.copy(alpha=.34f)
    else
        Color.White.copy(alpha=.19f)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .background(outerGlass)
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(Modifier.weight(1f)){
                Text(
                    "APP INFO",
                    color=Color.White.copy(alpha=.60f),
                    fontSize=8.sp,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily
                )

                Text(
                    "NMIX",
                    color=Color.White,
                    fontSize=20.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=NmixLogoFont
                )
            }

            MiniLink(
                text="Web",
                onClick={
                    openUrl(
                        context,
                        "https://lxzrvi.github.io/NMIX/"
                    )
                }
            )

            Spacer(Modifier.width(7.dp))

            MiniLink(
                text="GitHub",
                onClick={
                    openUrl(
                        context,
                        "https://github.com/lxzrvi"
                    )
                }
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(9.dp)
        ){
            Box(
                Modifier
                    .weight(1.2f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(innerGlass)
                    .padding(11.dp),
                contentAlignment=Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        (
                            fadeIn(tween(420))+
                            slideInVertically(
                                initialOffsetY={it/7},
                                animationSpec=tween(420)
                            )
                        ) togetherWith (
                            fadeOut(tween(280))+
                            slideOutVertically(
                                targetOffsetY={-it/7},
                                animationSpec=tween(280)
                            )
                        )
                    },
                    label="info"
                ){
                    Text(
                        text=messages[it],
                        color=Color.White.copy(alpha=.90f),
                        fontSize=10.sp,
                        lineHeight=16.sp,
                        fontFamily=a.fontFamily
                    )
                }
            }

            Column(
                Modifier
                    .weight(.8f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(innerGlass)
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=Color.White.copy(alpha=.62f),
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=a.fontFamily
                )

                Spacer(Modifier.height(8.dp))

                Tech("Kotlin",p.accent)
                Spacer(Modifier.height(4.dp))
                Tech("Jetpack Compose",p.accent)
                Spacer(Modifier.height(4.dp))
                Tech("Android SDK",p.accent)
                Spacer(Modifier.height(4.dp))
                Tech("Gradle",p.accent)
            }
        }
    }
}

@Composable
private fun Tech(
    text:String,
    accent:Color
){
    val a=LocalNmixAppearance.current

    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(
                accent.copy(alpha=.34f)
            )
            .padding(
                horizontal=8.dp,
                vertical=3.dp
            )
    ){
        Text(
            text=text,
            color=Color.White,
            fontSize=7.5.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1
        )
    }
}

@Composable
private fun MiniLink(
    text:String,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).94f else 1f,
        spring(
            dampingRatio=.68f,
            stiffness=650f
        ),
        label="miniPress"
    )

    Box(
        Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                p.accent.copy(alpha=.34f)
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
            .padding(
                horizontal=12.dp,
                vertical=7.dp
            )
    ){
        Text(
            text=text,
            color=Color.White,
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            textAlign=TextAlign.Center
        )
    }
}

private fun openUrl(
    context:Context,
    url:String
){
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
    )
}
