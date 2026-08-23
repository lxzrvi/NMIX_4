package com.lxzrvi.nmix

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

class MainActivity:ComponentActivity(){

    private var pendingNotificationAction:(()->Unit)?=null

    private val notificationPermissionLauncher=
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){granted->
            if(granted)pendingNotificationAction?.invoke()
            pendingNotificationAction=null
        }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)

        /*
         * System preview and Compose meet on the same
         * opaque launch color. No separate black frame.
         */
        window.statusBarColor=0xFF19493A.toInt()
        window.navigationBarColor=0xFF19493A.toInt()

        setContent{NmixApp()}
    }

    fun runWithNotificationPermission(action:()->Unit){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.TIRAMISU){
            action()
            return
        }

        if(
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )==PackageManager.PERMISSION_GRANTED
        ){
            action()
            return
        }

        pendingNotificationAction=action
        notificationPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
}

fun Context.findNmixActivity():MainActivity?{
    var current=this

    while(current is android.content.ContextWrapper){
        if(current is MainActivity)return current
        current=current.baseContext
    }

    return current as? MainActivity
}

@Composable
fun NmixApp(){
    val context=LocalContext.current
    val appearance=rememberNmixAppearance(context)

    val prefs=remember(context){
        context.getSharedPreferences(
            "nmix_preferences",
            Context.MODE_PRIVATE
        )
    }

    var main by remember{
        mutableStateOf(
            prefs.getString("home_screen","landing")=="main"
        )
    }

    var loading by remember{mutableStateOf(true)}

    /*
     * NMIX loading surface itself stays for 3 sec.
     */
    LaunchedEffect(Unit){
        delay(3000)
        loading=false
    }

    ProvideNmixAppearance(appearance){
        val a=LocalNmixAppearance.current
        val launchBase=Color(0xFF19493A)

        /*
         * Root is launch color, never white/black.
         * It exists before all page transitions.
         */
        Box(
            Modifier
                .fillMaxSize()
                .background(launchBase)
        ){
            AnimatedContent(
                targetState=loading,
                modifier=Modifier.fillMaxSize(),
                transitionSpec={
                    fadeIn(tween(250)) togetherWith
                        fadeOut(tween(360,easing=EaseInOutCubic))
                },
                label="launch"
            ){launching->
                if(launching){
                    NmixLaunchScreen()
                }else{
                    AnimatedContent(
                        targetState=main,
                        modifier=Modifier.fillMaxSize(),
                        transitionSpec={
                            (
                                fadeIn(
                                    tween(390,easing=EaseOutCubic)
                                )+
                                scaleIn(
                                    initialScale=.993f,
                                    animationSpec=tween(
                                        410,
                                        easing=EaseOutCubic
                                    )
                                )
                            ) togetherWith (
                                fadeOut(
                                    tween(300,easing=EaseInOutCubic)
                                )+
                                scaleOut(
                                    targetScale=.997f,
                                    animationSpec=tween(
                                        330,
                                        easing=EaseInOutCubic
                                    )
                                )
                            )
                        },
                        label="startMain"
                    ){showMain->
                        if(showMain){
                            NativeMainPageV2{
                                prefs.edit()
                                    .putString("home_screen","landing")
                                    .apply()
                                main=false
                            }
                        }else{
                            LandingScreen{
                                prefs.edit()
                                    .putString("home_screen","main")
                                    .apply()
                                main=true
                            }
                        }
                    }
                }
            }

            BackHandler(enabled=!loading && main){
                prefs.edit()
                    .putString("home_screen","landing")
                    .apply()
                main=false
            }
        }
    }
}

@Composable
private fun NmixLaunchScreen(){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val world=rememberNmixWorldMotion("launchWorld")

    var entered by remember{mutableStateOf(false)}

    LaunchedEffect(Unit){entered=true}

    val logoScale by animateFloatAsState(
        if(entered)1f else .90f,
        spring(dampingRatio=.82f,stiffness=185f),
        label="launchLogo"
    )

    val logoAlpha by animateFloatAsState(
        if(entered)1f else 0f,
        tween(450,easing=EaseOutCubic),
        label="launchAlpha"
    )

    /*
     * Always same base as Android window preview.
     * Theme motion is painted above it immediately.
     */
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF19493A))
            .clip(RoundedCornerShape(0.dp)),
        contentAlignment=Alignment.Center
    ){
        LaunchWorld(world)

        Column(
            Modifier.graphicsLayer{
                scaleX=logoScale
                scaleY=logoScale
                alpha=logoAlpha
            },
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                "N",
                color=Color.White,
                fontSize=72.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(Modifier.height(14.dp))

            Text(
                "EVERYTHING WITH NUMBERS",
                color=Color.White.copy(alpha=.78f),
                fontSize=8.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
private fun BoxScope.LaunchWorld(world:NmixWorldMotion){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{index,body->
        val x=body.x*(820f+index*62f)
        val y=body.y*(690f+index*50f)

        if(soft){
            Box(
                Modifier
                    .size(
                        when(index){
                            0->1400.dp
                            1->1210.dp
                            2->1050.dp
                            3->1130.dp
                            else->930.dp
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
                                    (if(index%2==0)p.accentLight else p.accent)
                                        .copy(alpha=.38f),
                                .28f to p.accent.copy(alpha=.21f),
                                .56f to p.accent.copy(alpha=.085f),
                                .78f to p.accent.copy(alpha=.022f),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            Box(
                Modifier
                    .size(
                        when(index){
                            0->520.dp
                            1->455.dp
                            2->395.dp
                            3->425.dp
                            else->350.dp
                        }
                    )
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=x
                        translationY=y
                        rotationZ=body.rotation
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
                    .clip(RoundedCornerShape(58.dp))
                    .background(p.accent.copy(alpha=.15f))
                    .border(
                        1.dp,
                        p.accentLight.copy(alpha=.13f),
                        RoundedCornerShape(58.dp)
                    )
            )
        }
    }
}

@Composable
private fun LandingScreen(onStart:()->Unit){
    val context=LocalContext.current
    val a=LocalNmixAppearance.current
    val p=a.palette
    val world=rememberNmixWorldMotion("landingWorld")

    Box(
        Modifier
            .fillMaxSize()
            .background(
                if(a.darkMode)Color(0xFF040706)
                else p.topDark
            )
            .clip(RoundedCornerShape(0.dp))
    ){
        LandingWorld(world)

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

            LandingButton("Start",onStart)

            Spacer(Modifier.height(10.dp))

            LandingButton("Share"){
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply{
                            type="text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "NMIX — EVERYTHING WITH NUMBERS\nhttps://lxzrvi.github.io/NMIX/"
                            )
                        },
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
private fun BoxScope.LandingWorld(world:NmixWorldMotion){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{index,body->
        val x=body.x*(900f+index*66f)
        val y=body.y*(760f+index*55f)

        if(soft){
            Box(
                Modifier
                    .size(
                        when(index){
                            0->1500.dp
                            1->1300.dp
                            2->1120.dp
                            3->1210.dp
                            else->1000.dp
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
                                    (if(index%2==0)p.accentLight else p.accent)
                                        .copy(
                                            alpha=if(a.darkMode).35f else .47f
                                        ),
                                .27f to p.accent.copy(
                                    alpha=if(a.darkMode).20f else .30f
                                ),
                                .55f to p.accent.copy(alpha=.10f),
                                .78f to p.accent.copy(alpha=.025f),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            Box(
                Modifier
                    .size(
                        when(index){
                            0->570.dp
                            1->495.dp
                            2->430.dp
                            3->465.dp
                            else->385.dp
                        }
                    )
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=x
                        translationY=y
                        rotationZ=body.rotation
                        scaleX=body.pulse
                        scaleY=body.pulse
                    }
                    .clip(RoundedCornerShape(60.dp))
                    .background(
                        p.accent.copy(
                            alpha=if(a.darkMode).14f else .18f
                        )
                    )
                    .border(
                        1.dp,
                        p.accentLight.copy(alpha=.13f),
                        RoundedCornerShape(60.dp)
                    )
            )
        }
    }
}

@Composable
private fun LandingButton(
    text:String,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val haptic=rememberNmixHapticAction()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if(pressed).955f else 1f,
        spring(dampingRatio=.70f,stiffness=620f),
        label="landingPress"
    )

    val shape=RoundedCornerShape(50)

    Box(
        Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        p.accent.copy(alpha=if(a.darkMode).30f else .24f),
                        p.accentLight.copy(alpha=if(a.darkMode).17f else .14f),
                        p.accent.copy(alpha=if(a.darkMode).30f else .24f)
                    )
                )
            )
            .border(
                .5.dp,
                p.accentLight.copy(alpha=if(a.darkMode).27f else .46f),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){haptic(onClick)},
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

    var index by remember{mutableIntStateOf(0)}

    LaunchedEffect(Unit){
        while(true){
            delay(4000)
            index=(index+1)%messages.size
        }
    }

    val outer=Brush.linearGradient(
        listOf(
            if(a.darkMode)
                Color(0xFF111513).copy(alpha=.82f)
            else Color.White.copy(alpha=.76f),

            p.accent.copy(alpha=if(a.darkMode).16f else .12f),

            if(a.darkMode)
                Color(0xFF0D110F).copy(alpha=.82f)
            else Color.White.copy(alpha=.74f)
        )
    )

    val inner=
        if(a.darkMode)
            Color(0xFF080B0A).copy(alpha=.58f)
        else
            Color.White.copy(alpha=.72f)

    val outerShape=RoundedCornerShape(21.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(outer)
            .border(
                .5.dp,
                p.accentLight.copy(alpha=if(a.darkMode).22f else .40f),
                outerShape
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
                    color=if(a.darkMode)
                        Color.White.copy(alpha=.62f)
                    else Color(0xFF43504B),
                    fontSize=8.sp,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily
                )

                Text(
                    "NMIX",
                    color=if(a.darkMode)Color.White else Color(0xFF222825),
                    fontSize=20.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=NmixLogoFont
                )
            }

            MiniLink("Web"){
                openUrl(context,"https://lxzrvi.github.io/NMIX/")
            }

            Spacer(Modifier.width(7.dp))

            MiniLink("GitHub"){
                openUrl(context,"https://github.com/lxzrvi")
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(9.dp)
        ){
            val innerShape=RoundedCornerShape(15.dp)

            Box(
                Modifier
                    .weight(1.2f)
                    .height(120.dp)
                    .clip(innerShape)
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(alpha=if(a.darkMode).18f else .27f),
                        innerShape
                    )
                    .padding(11.dp),
                contentAlignment=Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        fadeIn(tween(330)) togetherWith fadeOut(tween(240))
                    },
                    label="landingInfo"
                ){
                    Text(
                        messages[it],
                        color=if(a.darkMode)
                            Color.White.copy(alpha=.90f)
                        else Color(0xFF303834),
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
                    .clip(innerShape)
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(alpha=if(a.darkMode).18f else .27f),
                        innerShape
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=if(a.darkMode)
                        Color.White.copy(alpha=.64f)
                    else Color(0xFF4D5853),
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
            .background(p.accent.copy(alpha=.24f))
            .padding(horizontal=8.dp,vertical=3.dp)
    ){
        Text(
            text,
            color=if(a.darkMode)Color.White else Color(0xFF26302C),
            fontSize=7.5.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1
        )
    }
}

@Composable
private fun MiniLink(text:String,onClick:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val haptic=rememberNmixHapticAction()
    val shape=RoundedCornerShape(50)

    Box(
        Modifier
            .clip(shape)
            .background(p.accent.copy(alpha=.27f))
            .border(
                .45.dp,
                p.accentLight.copy(alpha=if(a.darkMode).20f else .38f),
                shape
            )
            .clickable(
                interactionSource=remember{MutableInteractionSource()},
                indication=null
            ){haptic(onClick)}
            .padding(horizontal=12.dp,vertical=7.dp)
    ){
        Text(
            text,
            color=if(a.darkMode)Color.White else Color(0xFF25302C),
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            textAlign=TextAlign.Center
        )
    }
}

private fun openUrl(context:Context,url:String){
    context.startActivity(
        Intent(Intent.ACTION_VIEW,Uri.parse(url))
    )
}
