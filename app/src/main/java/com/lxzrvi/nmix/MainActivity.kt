package com.lxzrvi.nmix

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
    var c=this

    while(c is android.content.ContextWrapper){
        if(c is MainActivity)return c
        c=c.baseContext
    }

    return c as? MainActivity
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
        delay(3000)
        loading=false
    }

    ProvideNmixAppearance(appearance){
        val a=LocalNmixAppearance.current
        val root=
            if(a.darkMode)Color(0xFF050807)
            else Color(0xFFF5F7F6)

        Box(
            Modifier
                .fillMaxSize()
                .background(root)
        ){
            AnimatedContent(
                targetState=loading,
                modifier=Modifier.fillMaxSize(),
                transitionSpec={
                    fadeIn(
                        tween(
                            220,
                            easing=EaseOutCubic
                        )
                    ) togetherWith
                    fadeOut(
                        tween(
                            300,
                            easing=EaseInOutCubic
                        )
                    )
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
                                    tween(
                                        330,
                                        easing=EaseOutCubic
                                    )
                                )+
                                scaleIn(
                                    initialScale=.994f,
                                    animationSpec=tween(350)
                                )
                            ) togetherWith (
                                fadeOut(
                                    tween(250)
                                )+
                                scaleOut(
                                    targetScale=1.004f,
                                    animationSpec=tween(280)
                                )
                            )
                        },
                        label="startMain"
                    ){showMain->
                        if(showMain){
                            NativeMainPageV2{
                                /*
                                 * Only NMIX's own Back to Start
                                 * changes the persisted home.
                                 */
                                prefs.edit()
                                    .putString(
                                        "home_screen",
                                        "landing"
                                    )
                                    .apply()

                                main=false
                            }
                        }else{
                            LandingScreen{
                                prefs.edit()
                                    .putString(
                                        "home_screen",
                                        "main"
                                    )
                                    .apply()

                                main=true
                            }
                        }
                    }
                }
            }

            /*
             * Intentionally no root BackHandler.
             *
             * Main + Android Back = app closes, "main" stays saved.
             * Start + Android Back = app closes, "landing" stays saved.
             * Settings and Fullscreen handle their own Back first.
             */
        }
    }
}

@Composable
private fun NmixLaunchScreen(){
    val a=LocalNmixAppearance.current
    val world=rememberNmixWorldMotion(
        "launchWorld"
    )

    var entered by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        entered=true
    }

    val logoScale by animateFloatAsState(
        targetValue=if(entered)1f else .90f,
        animationSpec=spring(
            dampingRatio=.82f,
            stiffness=185f
        ),
        label="launchScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue=if(entered)1f else 0f,
        animationSpec=tween(
            430,
            easing=EaseOutCubic
        ),
        label="launchAlpha"
    )

    val base=
        if(a.darkMode)Color(0xFF050807)
        else Color(0xFFF5F7F6)

    val fg=
        if(a.darkMode)Color.White
        else Color(0xFF202522)

    Box(
        Modifier
            .fillMaxSize()
            .background(base),
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
                color=fg,
                fontSize=72.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(Modifier.height(14.dp))

            Text(
                "EVERYTHING WITH NUMBERS",
                color=fg.copy(alpha=.72f),
                fontSize=8.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
private fun BoxScope.LaunchWorld(
    world:NmixWorldMotion
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=
        a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{
        index,
        body->

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
                                    (
                                        if(index%2==0)
                                            p.accentLight
                                        else p.accent
                                    ).copy(
                                        alpha=
                                            if(a.darkMode).27f
                                            else .20f
                                    ),

                                .24f to
                                    p.accent.copy(
                                        alpha=
                                            if(a.darkMode).17f
                                            else .14f
                                    ),

                                .50f to
                                    p.accent.copy(
                                        alpha=.075f
                                    ),

                                .72f to
                                    p.accent.copy(
                                        alpha=.022f
                                    ),

                                .88f to
                                    p.accent.copy(
                                        alpha=.005f
                                    ),

                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val shape=
                RoundedCornerShape(58.dp)

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
                    .clip(shape)
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).12f
                                else .085f
                        )
                    )
                    .border(
                        .8.dp,
                        p.accentLight.copy(
                            alpha=.11f
                        ),
                        shape
                    )
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
    val world=rememberNmixWorldMotion(
        "landingWorld"
    )

    val base=
        if(a.darkMode)Color(0xFF040706)
        else Color(0xFFF5F7F6)

    val fg=
        if(a.darkMode)Color.White
        else Color(0xFF202522)

    Box(
        Modifier
            .fillMaxSize()
            .background(base)
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
                color=fg.copy(alpha=.70f),
                fontSize=9.sp,
                letterSpacing=2.2.sp,
                fontWeight=FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(3.dp))

            Text(
                "NMIX",
                color=fg,
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

            LandingButton("Share"){
                context.startActivity(
                    Intent.createChooser(
                        Intent(
                            Intent.ACTION_SEND
                        ).apply{
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
                color=fg.copy(alpha=.92f),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                "  •  lxzrvi  •  © 2026",
                color=fg.copy(alpha=.62f),
                fontSize=11.sp,
                fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
private fun BoxScope.LandingWorld(
    world:NmixWorldMotion
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=
        a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{
        index,
        body->

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
                                    (
                                        if(index%2==0)
                                            p.accentLight
                                        else p.accent
                                    ).copy(
                                        alpha=
                                            if(a.darkMode).29f
                                            else .20f
                                    ),

                                .25f to
                                    p.accent.copy(
                                        alpha=
                                            if(a.darkMode).17f
                                            else .13f
                                    ),

                                .52f to
                                    p.accent.copy(
                                        alpha=.070f
                                    ),

                                .75f to
                                    p.accent.copy(
                                        alpha=.019f
                                    ),

                                .90f to
                                    p.accent.copy(
                                        alpha=.004f
                                    ),

                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val shape=
                RoundedCornerShape(60.dp)

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
                    .clip(shape)
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).13f
                                else .085f
                        )
                    )
                    .border(
                        .8.dp,
                        p.accentLight.copy(
                            alpha=.12f
                        ),
                        shape
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
    val ui=a.uiColors()
    val haptic=rememberNmixHapticAction()

    val interaction=
        remember{
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue=
            if(pressed).955f else 1f,
        animationSpec=spring(
            dampingRatio=.70f,
            stiffness=620f
        ),
        label="landingPress"
    )

    val shape=
        RoundedCornerShape(50)

    Box(
        Modifier
            .width(278.dp)
            .height(44.dp)
            .scale(scale)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF121715)
                        .copy(alpha=.92f)
                else
                    Color.White
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode).08f
                        else .045f
                )
            )
            .border(
                .55.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode).30f
                        else .38f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                haptic(onClick)
            },
        contentAlignment=Alignment.Center
    ){
        Text(
            text,
            color=ui.text,
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

    val messages=
        remember{
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

    val outerShape=
        RoundedCornerShape(21.dp)

    val innerShape=
        RoundedCornerShape(15.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(
                if(a.darkMode)
                    Color(0xFF121715)
                        .copy(alpha=.90f)
                else
                    Color.White
                        .copy(alpha=.91f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode).045f
                        else .025f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode).20f
                        else .29f
                ),
                outerShape
            )
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(
                Modifier.weight(1f)
            ){
                Text(
                    "APP INFO",
                    color=ui.muted,
                    fontSize=8.sp,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily
                )

                Text(
                    "NMIX",
                    color=ui.text,
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
            horizontalArrangement=
                Arrangement.spacedBy(9.dp)
        ){
            Box(
                Modifier
                    .weight(1.2f)
                    .height(120.dp)
                    .clip(innerShape)
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).075f
                                else .055f
                        )
                    )
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).19f
                                else .27f
                        ),
                        innerShape
                    )
                    .padding(11.dp),
                contentAlignment=Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        fadeIn(
                            tween(300)
                        ) togetherWith
                        fadeOut(
                            tween(220)
                        )
                    },
                    label="info"
                ){
                    Text(
                        messages[it],
                        color=ui.text,
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
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).075f
                                else .055f
                        )
                    )
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode).19f
                                else .27f
                        ),
                        innerShape
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=ui.muted,
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
                p.accent.copy(
                    alpha=
                        if(a.darkMode).20f
                        else .13f
                )
            )
            .padding(
                horizontal=8.dp,
                vertical=3.dp
            )
    ){
        Text(
            text,
            color=a.uiColors().text,
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
    val haptic=rememberNmixHapticAction()
    val shape=RoundedCornerShape(50)

    Box(
        Modifier
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode).12f
                        else .075f
                )
            )
            .border(
                .45.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode).24f
                        else .32f
                ),
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
            }
            .padding(
                horizontal=12.dp,
                vertical=7.dp
            )
    ){
        Text(
            text,
            color=a.uiColors().text,
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
