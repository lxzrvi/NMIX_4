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
        delay(1450)
        loading=false
    }

    ProvideNmixAppearance(appearance){
        AnimatedContent(
            targetState=loading,
            transitionSpec={
                fadeIn(
                    tween(
                        420,
                        easing=EaseOutCubic
                    )
                ) togetherWith
                fadeOut(
                    tween(
                        500,
                        easing=EaseInOutCubic
                    )
                )
            },
            label="launch"
        ){showLoading->
            if(showLoading){
                NmixLaunchScreen()
            }else{
                AnimatedContent(
                    targetState=main,
                    transitionSpec={
                        if(targetState){
                            (
                                slideInHorizontally(
                                    initialOffsetX={it/7},
                                    animationSpec=tween(
                                        520,
                                        easing=EaseOutCubic
                                    )
                                )+
                                fadeIn(tween(420))
                            ) togetherWith (
                                slideOutHorizontally(
                                    targetOffsetX={-it/9},
                                    animationSpec=tween(
                                        470,
                                        easing=EaseInOutCubic
                                    )
                                )+
                                fadeOut(tween(350))
                            )
                        }else{
                            (
                                slideInHorizontally(
                                    initialOffsetX={-it/7},
                                    animationSpec=tween(
                                        520,
                                        easing=EaseOutCubic
                                    )
                                )+
                                fadeIn(tween(420))
                            ) togetherWith (
                                slideOutHorizontally(
                                    targetOffsetX={it/9},
                                    animationSpec=tween(
                                        470,
                                        easing=EaseInOutCubic
                                    )
                                )+
                                fadeOut(tween(350))
                            )
                        }
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
    val motion=rememberInfiniteTransition(
        label="launchMotion"
    )

    var entered by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        entered=true
    }

    val logoScale by animateFloatAsState(
        targetValue=if(entered)1f else .84f,
        animationSpec=spring(
            dampingRatio=.72f,
            stiffness=190f
        ),
        label="logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue=if(entered)1f else 0f,
        animationSpec=tween(
            560,
            easing=EaseOutCubic
        ),
        label="logoAlpha"
    )

    val pulse by motion.animateFloat(
        initialValue=.88f,
        targetValue=1.14f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                1800,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="launchPulse"
    )

    val shift by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                2600,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="launchShift"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        p.topDark,
                        p.accentDark,
                        p.accent,
                        p.topEnd
                    )
                )
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(420.dp)
                .graphicsLayer{
                    translationX=shift*110f
                    translationY=-shift*55f
                    scaleX=pulse
                    scaleY=pulse
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(alpha=.38f),
                            p.accent.copy(alpha=.14f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Box(
                Modifier
                    .size(126.dp)
                    .graphicsLayer{
                        scaleX=logoScale
                        scaleY=logoScale
                        alpha=logoAlpha
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                p.accentLight.copy(alpha=.30f),
                                p.accent.copy(alpha=.24f),
                                Color.White.copy(alpha=.08f)
                            )
                        )
                    )
                    .border(
                        .7.dp,
                        p.accentLight.copy(alpha=.62f),
                        CircleShape
                    ),
                contentAlignment=Alignment.Center
            ){
                Text(
                    text="N",
                    color=Color.White,
                    fontSize=70.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=NmixLogoFont
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text="EVERYTHING WITH NUMBERS",
                color=Color.White.copy(alpha=.76f),
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

    val motion=rememberInfiniteTransition(
        label="landingMotion"
    )

    val x by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                3300,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="x"
    )

    val y by motion.animateFloat(
        initialValue=1f,
        targetValue=-1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                4100,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="y"
    )

    val z by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                4900,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="z"
    )

    val pulse by motion.animateFloat(
        initialValue=.83f,
        targetValue=1.18f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                3000,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="pulse"
    )

    val background=if(a.darkMode){
        Brush.verticalGradient(
            listOf(
                Color(0xFF020403),
                Color(0xFF07100D),
                p.topDark.copy(alpha=.92f),
                Color(0xFF050A08),
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
            .background(background)
    ){
        LandingGlow(
            Modifier
                .align(Alignment.TopStart)
                .offset(
                    x=(-245).dp,
                    y=(-220).dp
                )
                .graphicsLayer{
                    translationX=x*430f
                    translationY=y*180f
                    scaleX=pulse
                    scaleY=pulse
                },
            p.accentLight,
            if(a.darkMode).27f else .48f,
            650
        )

        LandingGlow(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x=265.dp,
                    y=245.dp
                )
                .graphicsLayer{
                    translationX=y*410f
                    translationY=z*210f
                    scaleX=1.10f
                    scaleY=1.10f
                },
            p.accent,
            if(a.darkMode).30f else .50f,
            700
        )

        LandingGlow(
            Modifier
                .align(Alignment.Center)
                .offset(
                    x=(-120).dp,
                    y=(-40).dp
                )
                .graphicsLayer{
                    translationX=z*350f
                    translationY=x*260f
                    scaleX=pulse
                    scaleY=pulse
                },
            p.accentLight,
            if(a.darkMode).18f else .31f,
            560
        )

        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y=(-58).dp)
                .padding(horizontal=22.dp),
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                "EVERYTHING WITH NUMBERS",
                color=Color.White.copy(alpha=.76f),
                fontSize=9.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(3.dp))

            Text(
                "NMIX",
                color=Color.White,
                fontSize=52.sp,
                letterSpacing=3.5.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(Modifier.height(27.dp))

            LandingButton(
                "Start",
                onStart
            )

            Spacer(Modifier.height(10.dp))

            LandingButton(
                "Share"
            ){
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
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal=15.dp)
                .padding(bottom=53.dp),
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            LandingInfo()
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom=10.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Text(
                "NMIX",
                color=Color.White.copy(alpha=.92f),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                "  •  lxzrvi  •  © 2026",
                color=Color.White.copy(alpha=.68f),
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
                        .25f to color.copy(alpha=alpha*.85f),
                        .52f to color.copy(alpha=alpha*.48f),
                        .77f to color.copy(alpha=alpha*.15f),
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
    val p=a.palette
    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).955f else 1f,
        spring(
            dampingRatio=.68f,
            stiffness=620f
        ),
        label="landingPress"
    )

    Box(
        Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        p.accent.copy(
                            alpha=if(a.darkMode).24f else .20f
                        ),
                        p.accentLight.copy(
                            alpha=if(a.darkMode).13f else .11f
                        ),
                        p.accent.copy(
                            alpha=if(a.darkMode).24f else .20f
                        )
                    )
                )
            )
            .border(
                .55.dp,
                p.accentLight.copy(alpha=.48f),
                RoundedCornerShape(50)
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            ),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
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
            delay(4000)
            index=(index+1)%messages.size
        }
    }

    val outer=Brush.linearGradient(
        listOf(
            if(a.darkMode)
                Color.Black.copy(alpha=.48f)
            else
                Color.White.copy(alpha=.20f),

            p.accent.copy(
                alpha=if(a.darkMode).18f else .13f
            ),

            if(a.darkMode)
                Color.Black.copy(alpha=.42f)
            else
                Color.White.copy(alpha=.17f)
        )
    )

    val inner=if(a.darkMode)
        Color.Black.copy(alpha=.38f)
    else
        Color.White.copy(alpha=.18f)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .background(outer)
            .border(
                .55.dp,
                p.accentLight.copy(alpha=.42f),
                RoundedCornerShape(21.dp)
            )
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(Modifier.weight(1f)){
                Text(
                    "APP INFO",
                    color=Color.White.copy(alpha=.64f),
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

            MiniLink("Web"){
                openUrl(
                    context,
                    "https://lxzrvi.github.io/NMIX/"
                )
            }

            Spacer(Modifier.width(7.dp))

            MiniLink("GitHub"){
                openUrl(
                    context,
                    "https://github.com/lxzrvi"
                )
            }
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
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(alpha=.27f),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(11.dp),
                contentAlignment=Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        (
                            fadeIn(tween(380))+
                            slideInVertically(
                                initialOffsetY={it/6},
                                animationSpec=tween(
                                    380,
                                    easing=EaseOutCubic
                                )
                            )
                        ) togetherWith (
                            fadeOut(tween(280))+
                            slideOutVertically(
                                targetOffsetY={-it/6},
                                animationSpec=tween(
                                    320,
                                    easing=EaseInCubic
                                )
                            )
                        )
                    },
                    label="info"
                ){
                    Text(
                        messages[it],
                        color=Color.White.copy(alpha=.92f),
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
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(alpha=.27f),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=Color.White.copy(alpha=.66f),
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=a.fontFamily
                )

                Spacer(Modifier.height(8.dp))
                Tech("Kotlin")
                Spacer(Modifier.height(4.dp))
                Tech("Jetpack Compose")
                Spacer(Modifier.height(4.dp))
                Tech("Android SDK")
                Spacer(Modifier.height(4.dp))
                Tech("Gradle")
            }
        }
    }
}

@Composable
private fun Tech(text:String){
    val a=LocalNmixAppearance.current
    val p=a.palette

    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(
                p.accent.copy(alpha=.34f)
            )
            .padding(
                horizontal=8.dp,
                vertical=3.dp
            )
    ){
        Text(
            text,
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
            stiffness=620f
        ),
        label="miniPress"
    )

    Box(
        Modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                p.accent.copy(alpha=.32f)
            )
            .border(
                .45.dp,
                p.accentLight.copy(alpha=.42f),
                RoundedCornerShape(50)
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
            text,
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
