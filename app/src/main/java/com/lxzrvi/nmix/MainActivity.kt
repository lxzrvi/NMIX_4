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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                (fadeIn(tween(350))+
                    scaleIn(initialScale=.985f)) togetherWith
                (fadeOut(tween(250))+
                    scaleOut(targetScale=.985f))
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

@Composable
private fun LandingScreen(
    onStart:()->kotlin.Unit
){
    val context=LocalContext.current
    val appearance=LocalNmixAppearance.current
    val palette=appearance.palette
    val ui=appearance.uiColors()

    val motion=rememberInfiniteTransition(
        label="landing"
    )

    val x1 by motion.animateFloat(
        initialValue=-260f,
        targetValue=270f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                3900,
                easing=LinearEasing
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="x1"
    )

    val x2 by motion.animateFloat(
        initialValue=250f,
        targetValue=-280f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                5100,
                easing=LinearEasing
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="x2"
    )

    val y by motion.animateFloat(
        initialValue=-190f,
        targetValue=220f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                4500,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="y"
    )

    val pulse by motion.animateFloat(
        initialValue=.92f,
        targetValue=1.16f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                3200,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="pulse"
    )

    val backgroundColors=
        if(appearance.darkMode){
            listOf(
                Color(0xFF020403),
                Color(0xFF080D0B),
                palette.accent.copy(alpha=.30f),
                Color(0xFF070B09),
                Color.Black
            )
        }else{
            listOf(
                palette.topDark,
                palette.accent.copy(alpha=.72f),
                palette.accent,
                palette.accentDark,
                palette.topEnd
            )
        }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    backgroundColors
                )
            )
    ){
        GlowOrb(
            modifier=Modifier
                .offset(
                    x=(-220).dp,
                    y=(-210).dp
                )
                .graphicsLayer{
                    translationX=x1
                    translationY=y*.55f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=palette.accentLight.copy(
                alpha=
                    if(appearance.darkMode)
                        .18f
                    else .36f
            ),
            size=560
        )

        GlowOrb(
            modifier=Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x=240.dp,
                    y=230.dp
                )
                .graphicsLayer{
                    translationX=x2
                    translationY=-y*.48f
                },
            color=palette.accent.copy(
                alpha=
                    if(appearance.darkMode)
                        .20f
                    else .42f
            ),
            size=610
        )

        GlowOrb(
            modifier=Modifier
                .align(Alignment.Center)
                .graphicsLayer{
                    translationX=-x1*.65f
                    translationY=x2*.42f
                    scaleX=pulse
                    scaleY=pulse
                },
            color=palette.accentLight.copy(
                alpha=
                    if(appearance.darkMode)
                        .12f
                    else .20f
            ),
            size=490
        )

        Column(
            modifier=Modifier
                .align(Alignment.Center)
                .offset(y=(-52).dp)
                .padding(horizontal=20.dp),
            horizontalAlignment=
                Alignment.CenterHorizontally
        ){
            Text(
                text="EVERYTHING WITH NUMBERS",
                color=Color.White.copy(
                    alpha=.76f
                ),
                fontSize=10.sp,
                letterSpacing=2.4.sp,
                fontWeight=
                    FontWeight.SemiBold
            )

            Text(
                text="NMIX",
                color=Color.White,
                fontSize=55.sp,
                letterSpacing=6.sp,
                fontWeight=FontWeight.Bold
            )

            Spacer(
                Modifier.height(28.dp)
            )

            LandingButton(
                text="Start",
                accent=palette.accent,
                onClick=onStart
            )

            Spacer(
                Modifier.height(10.dp)
            )

            LandingButton(
                text="Share",
                accent=palette.accent,
                onClick={
                    val intent=
                        Intent(
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
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .padding(horizontal=15.dp)
                .padding(bottom=56.dp),
            horizontalAlignment=
                Alignment.CenterHorizontally
        ){
            LandingInfo(
                accent=palette.accent,
                glass=ui.glassStrong
            )
        }

        Row(
            modifier=Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(bottom=9.dp),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Text(
                text="NMIX",
                color=Color.White.copy(
                    alpha=.92f
                ),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=.7.sp
            )

            Text(
                text="  •  lxzrvi  •  © 2026",
                color=Color.White.copy(
                    alpha=.65f
                ),
                fontSize=12.sp
            )
        }
    }
}

@Composable
private fun GlowOrb(
    modifier:Modifier,
    color:Color,
    size:Int
){
    Box(
        modifier
            .size(size.dp)
            .blur(145.dp)
            .background(
                color,
                CircleShape
            )
    )
}

@Composable
private fun LandingButton(
    text:String,
    accent:Color,
    onClick:()->kotlin.Unit
){
    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue=
            if(pressed)
                .95f
            else 1f,
        animationSpec=spring(
            dampingRatio=.65f,
            stiffness=700f
        ),
        label="landingPress"
    )

    Box(
        modifier=Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                Color.White.copy(
                    alpha=.18f
                )
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
            fontWeight=
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun LandingInfo(
    accent:Color,
    glass:Color
){
    val context=LocalContext.current

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
            index=
                (index+1)%
                    messages.size
        }
    }

    Column(
        modifier=Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(
                Color.White.copy(
                    alpha=.18f
                )
            )
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Column(
                Modifier.weight(1f)
            ){
                Text(
                    text="APP INFO",
                    color=Color.White.copy(
                        alpha=.60f
                    ),
                    fontSize=8.sp,
                    letterSpacing=1.2.sp
                )

                Text(
                    text="NMIX",
                    color=Color.White,
                    fontSize=20.sp,
                    fontWeight=
                        FontWeight.Bold
                )
            }

            MiniLink(
                text="Web",
                accent=accent,
                onClick={
                    openUrl(
                        context,
                        "https://lxzrvi.github.io/NMIX/"
                    )
                }
            )

            Spacer(
                Modifier.width(7.dp)
            )

            MiniLink(
                text="GitHub",
                accent=accent,
                onClick={
                    openUrl(
                        context,
                        "https://github.com/lxzrvi"
                    )
                }
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Row(
            horizontalArrangement=
                Arrangement.spacedBy(9.dp)
        ){
            Box(
                modifier=Modifier
                    .weight(1.2f)
                    .height(120.dp)
                    .clip(
                        RoundedCornerShape(
                            15.dp
                        )
                    )
                    .background(glass)
                    .padding(11.dp),
                contentAlignment=
                    Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        fadeIn(
                            tween(400)
                        ) togetherWith
                        fadeOut(
                            tween(300)
                        )
                    },
                    label="info"
                ){
                    Text(
                        text=messages[it],
                        color=
                            Color.White.copy(
                                alpha=.90f
                            ),
                        fontSize=10.sp,
                        lineHeight=16.sp
                    )
                }
            }

            Column(
                modifier=Modifier
                    .weight(.8f)
                    .height(120.dp)
                    .clip(
                        RoundedCornerShape(
                            15.dp
                        )
                    )
                    .background(glass)
                    .padding(10.dp)
            ){
                Text(
                    text="BUILT WITH",
                    color=
                        Color.White.copy(
                            alpha=.62f
                        ),
                    fontSize=8.sp,
                    fontWeight=
                        FontWeight.Bold
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Tech(
                    "Kotlin",
                    accent
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Tech(
                    "Jetpack Compose",
                    accent
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Tech(
                    "Android SDK",
                    accent
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Tech(
                    "Gradle",
                    accent
                )
            }
        }
    }
}

@Composable
private fun Tech(
    text:String,
    accent:Color
){
    Box(
        Modifier
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                accent.copy(alpha=.35f)
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
            fontWeight=
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun MiniLink(
    text:String,
    accent:Color,
    onClick:()->kotlin.Unit
){
    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    Box(
        Modifier
            .scale(
                if(pressed)
                    .92f
                else 1f
            )
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                accent.copy(alpha=.35f)
            )
            .clickable(
                interactionSource=
                    interaction,
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
            fontWeight=
                FontWeight.SemiBold
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
