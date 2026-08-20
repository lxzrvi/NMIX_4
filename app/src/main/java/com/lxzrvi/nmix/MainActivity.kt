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

    ProvideNmixAppearance(appearance){
        AnimatedContent(
            targetState=main,
            transitionSpec={
                (
                    fadeIn(tween(420))+
                    scaleIn(
                        initialScale=.992f,
                        animationSpec=tween(420)
                    )
                ) togetherWith (
                    fadeOut(tween(280))+
                    scaleOut(
                        targetScale=.992f,
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
                            .putString("home_screen","landing")
                            .apply()
                        main=false
                    }
                )
            }else{
                LandingScreen(
                    onStart={
                        prefs.edit()
                            .putString("home_screen","main")
                            .apply()
                        main=true
                    }
                )
            }
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

    val motion=rememberInfiniteTransition(label="landingMotion")

    val move1 by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                9200,
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
                11800,
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
                14700,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="move3"
    )

    val pulse by motion.animateFloat(
        initialValue=.92f,
        targetValue=1.12f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                6800,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="pulse"
    )

    val bg=if(a.darkMode){
        Brush.verticalGradient(
            listOf(
                Color(0xFF080C0B),
                p.topDark.copy(alpha=.96f),
                Color(0xFF0B1110),
                Color(0xFF070A09)
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

    val primaryText=Color.White
    val secondaryText=Color.White.copy(alpha=.70f)

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
    ){
        LandingGlow(
            modifier=Modifier
                .align(Alignment.TopStart)
                .offset(x=(-210).dp,y=(-190).dp)
                .graphicsLayer{
                    translationX=move1*210f
                    translationY=move2*95f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=p.accentLight,
            alpha=if(a.darkMode).18f else .31f,
            size=600
        )

        LandingGlow(
            modifier=Modifier
                .align(Alignment.BottomEnd)
                .offset(x=230.dp,y=210.dp)
                .graphicsLayer{
                    translationX=move2*235f
                    translationY=move3*125f
                    scaleX=1.08f
                    scaleY=1.08f
                },
            color=p.accent,
            alpha=if(a.darkMode).20f else .34f,
            size=650
        )

        LandingGlow(
            modifier=Modifier
                .align(Alignment.Center)
                .offset(x=(-80).dp,y=30.dp)
                .graphicsLayer{
                    translationX=move3*170f
                    translationY=move1*145f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=p.accentLight,
            alpha=if(a.darkMode).10f else .18f,
            size=520
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
                color=secondaryText,
                fontSize=9.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text="NMIX",
                color=primaryText,
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
                    val intent=Intent(Intent.ACTION_SEND).apply{
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
                color=primaryText.copy(alpha=.92f),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                text="  •  lxzrvi  •  © 2026",
                color=secondaryText,
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
                        .30f to color.copy(alpha=alpha*.72f),
                        .62f to color.copy(alpha=alpha*.30f),
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
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue=if(pressed).96f else 1f,
        animationSpec=spring(
            dampingRatio=.68f,
            stiffness=650f
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
                if(a.darkMode)
                    ui.glassStrong
                else
                    Color.White.copy(alpha=.20f)
            )
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

    var index by remember{mutableIntStateOf(0)}

    LaunchedEffect(Unit){
        while(true){
            delay(4300)
            index=(index+1)%messages.size
        }
    }

    val outerGlass=if(a.darkMode)
        ui.glass
    else
        Color.White.copy(alpha=.17f)

    val innerGlass=if(a.darkMode)
        ui.glassStrong
    else
        Color.White.copy(alpha=.20f)

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
                    text="APP INFO",
                    color=Color.White.copy(alpha=.58f),
                    fontSize=8.sp,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily
                )

                Text(
                    text="NMIX",
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
                                initialOffsetY={it/8},
                                animationSpec=tween(420)
                            )
                        ) togetherWith (
                            fadeOut(tween(280))+
                            slideOutVertically(
                                targetOffsetY={-it/8},
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
                    text="BUILT WITH",
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
            .background(accent.copy(alpha=.32f))
            .padding(horizontal=8.dp,vertical=3.dp)
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
    val interaction=remember{MutableInteractionSource()}
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
            .background(p.accent.copy(alpha=.30f))
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
            .padding(horizontal=12.dp,vertical=7.dp)
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
