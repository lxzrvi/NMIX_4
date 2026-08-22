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

    private var pendingNotificationAction:
        (()->Unit)?=null

    private val notificationPermissionLauncher=
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){granted->
            if(granted){
                pendingNotificationAction?.invoke()
            }

            pendingNotificationAction=null
        }

    override fun onCreate(
        savedInstanceState:Bundle?
    ){
        /*
         * Paint the Android window before Compose
         * draws its first frame.
         */
        window.statusBarColor=
            android.graphics.Color.TRANSPARENT

        window.navigationBarColor=
            android.graphics.Color.TRANSPARENT

        super.onCreate(savedInstanceState)

        setContent{
            NmixApp()
        }
    }

    fun runWithNotificationPermission(
        action:()->Unit
    ){
        if(
            Build.VERSION.SDK_INT<
            Build.VERSION_CODES.TIRAMISU
        ){
            action()
            return
        }

        if(
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )==
            PackageManager.PERMISSION_GRANTED
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

    while(
        current is
        android.content.ContextWrapper
    ){
        if(current is MainActivity){
            return current
        }

        current=current.baseContext
    }

    return current as? MainActivity
}

@Composable
fun NmixApp(){
    val context=LocalContext.current

    val appearance=
        rememberNmixAppearance(context)

    val prefs=remember(context){
        context.getSharedPreferences(
            "nmix_preferences",
            Context.MODE_PRIVATE
        )
    }

    /*
     * Keep the user's existing Start/Main preference.
     */
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
        delay(420)
        loading=false
    }

    ProvideNmixAppearance(
        appearance
    ){
        val ui=appearance.uiColors()
        val p=appearance.palette

        val appBackground=
            if(appearance.darkMode){
                Brush.verticalGradient(
                    listOf(
                        ui.page,
                        mixAppColor(
                            ui.page,
                            p.accent,
                            .075f
                        ),
                        ui.page
                    )
                )
            }else{
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF7F8F7),
                        mixAppColor(
                            Color(0xFFF7F8F7),
                            p.accent,
                            .055f
                        ),
                        Color(0xFFF7F8F7)
                    )
                )
            }

        Box(
            Modifier
                .fillMaxSize()
                .background(appBackground)
        ){
            AnimatedContent(
                targetState=loading,
                modifier=Modifier.fillMaxSize(),
                transitionSpec={
                    (
                        fadeIn(
                            tween(
                                300,
                                easing=EaseOutCubic
                            )
                        )
                    ) togetherWith (
                        fadeOut(
                            tween(
                                340,
                                easing=EaseInOutCubic
                            )
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
                        modifier=
                            Modifier.fillMaxSize(),
                        transitionSpec={
                            /*
                             * Both pages always cover the full
                             * window. No slide edge/cutout can
                             * expose the window underneath.
                             */
                            (
                                fadeIn(
                                    tween(
                                        420,
                                        easing=EaseOutCubic
                                    )
                                )+
                                scaleIn(
                                    initialScale=.992f,
                                    animationSpec=tween(
                                        440,
                                        easing=EaseOutCubic
                                    )
                                )
                            ) togetherWith (
                                fadeOut(
                                    tween(
                                        330,
                                        easing=EaseInOutCubic
                                    )
                                )+
                                scaleOut(
                                    targetScale=.996f,
                                    animationSpec=tween(
                                        350,
                                        easing=EaseInOutCubic
                                    )
                                )
                            )
                        },
                        label="startMain"
                    ){showMain->
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(appBackground)
                        ){
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

            /*
             * Main -> Start.
             * Start -> normal Activity/system exit.
             */
            BackHandler(
                enabled=
                    !loading &&
                    main
            ){
                prefs.edit()
                    .putString(
                        "home_screen",
                        "landing"
                    )
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

    var entered by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(Unit){
        entered=true
    }

    val scale by animateFloatAsState(
        targetValue=
            if(entered)
                1f
            else
                .90f,
        animationSpec=spring(
            dampingRatio=.80f,
            stiffness=180f
        ),
        label="launchScale"
    )

    val alpha by animateFloatAsState(
        targetValue=
            if(entered)
                1f
            else
                0f,
        animationSpec=tween(
            440,
            easing=EaseOutCubic
        ),
        label="launchAlpha"
    )

    val motion=
        rememberNmixMotion(
            "launchMotion"
        )

    val background=
        if(a.darkMode){
            Brush.verticalGradient(
                listOf(
                    Color(0xFF020403),
                    p.topDark,
                    p.topEnd,
                    Color(0xFF020302)
                )
            )
        }else{
            Brush.linearGradient(
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
            .clip(
                RoundedCornerShape(0.dp)
            ),
        contentAlignment=Alignment.Center
    ){
        Box(
            Modifier
                .size(820.dp)
                .graphicsLayer{
                    translationX=
                        motion.x*270f

                    translationY=
                        motion.y*170f

                    scaleX=
                        motion.pulse*1.10f

                    scaleY=
                        motion.pulse*1.10f
                }
                .background(
                    Brush.radialGradient(
                        colorStops=arrayOf(
                            0f to
                                p.accentLight.copy(
                                    alpha=.44f
                                ),

                            .30f to
                                p.accent.copy(
                                    alpha=.22f
                                ),

                            .58f to
                                p.accent.copy(
                                    alpha=.085f
                                ),

                            .80f to
                                p.accent.copy(
                                    alpha=.025f
                                ),

                            1f to
                                Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            Modifier.graphicsLayer{
                scaleX=scale
                scaleY=scale
                this.alpha=alpha
            },
            horizontalAlignment=
                Alignment.CenterHorizontally
        ){
            Text(
                "N",
                color=Color.White,
                fontSize=72.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Text(
                "EVERYTHING WITH NUMBERS",
                color=
                    Color.White.copy(
                        alpha=.76f
                    ),
                fontSize=8.sp,
                letterSpacing=2.2.sp,
                fontWeight=
                    FontWeight.SemiBold,
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

    val motion=
        rememberNmixMotion(
            "landingMotion"
        )

    val bg=
        if(a.darkMode){
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
            .background(bg)
            .clip(
                RoundedCornerShape(0.dp)
            )
    ){
        LandingGlow(
            modifier=
                Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x=(-360).dp,
                        y=(-335).dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.x*620f

                        translationY=
                            motion.y*285f

                        scaleX=
                            motion.pulse*1.13f

                        scaleY=
                            motion.pulse*1.13f
                    },
            color=p.accentLight,
            alpha=
                if(a.darkMode)
                    .35f
                else
                    .54f,
            size=980
        )

        LandingGlow(
            modifier=
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x=390.dp,
                        y=355.dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.y*600f

                        translationY=
                            motion.z*320f

                        scaleX=1.08f
                        scaleY=1.08f
                    },
            color=p.accent,
            alpha=
                if(a.darkMode)
                    .37f
                else
                    .56f,
            size=1010
        )

        LandingGlow(
            modifier=
                Modifier
                    .align(Alignment.Center)
                    .offset(
                        x=(-190).dp,
                        y=(-80).dp
                    )
                    .graphicsLayer{
                        translationX=
                            motion.z*520f

                        translationY=
                            motion.x*380f

                        scaleX=motion.pulse
                        scaleY=motion.pulse
                    },
            color=p.accentLight,
            alpha=
                if(a.darkMode)
                    .25f
                else
                    .37f,
            size=820
        )

        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y=(-58).dp)
                .padding(horizontal=22.dp),
            horizontalAlignment=
                Alignment.CenterHorizontally
        ){
            Text(
                "EVERYTHING WITH NUMBERS",
                color=
                    Color.White.copy(
                        alpha=.76f
                    ),
                fontSize=9.sp,
                letterSpacing=2.2.sp,
                fontWeight=
                    FontWeight.SemiBold,
                fontFamily=a.fontFamily
            )

            Spacer(
                Modifier.height(3.dp)
            )

            Text(
                "NMIX",
                color=Color.White,
                fontSize=52.sp,
                letterSpacing=3.5.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Spacer(
                Modifier.height(27.dp)
            )

            LandingButton(
                "Start",
                onStart
            )

            Spacer(
                Modifier.height(10.dp)
            )

            LandingButton(
                "Share"
            ){
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
        }

        Column(
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .padding(horizontal=15.dp)
                .padding(bottom=53.dp),
            horizontalAlignment=
                Alignment.CenterHorizontally
        ){
            LandingInfo()
        }

        Row(
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(bottom=10.dp),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Text(
                "NMIX",
                color=
                    Color.White.copy(
                        alpha=.92f
                    ),
                fontSize=12.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                "  •  lxzrvi  •  © 2026",
                color=
                    Color.White.copy(
                        alpha=.68f
                    ),
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
                        0f to
                            color.copy(
                                alpha=alpha
                            ),

                        .24f to
                            color.copy(
                                alpha=alpha*.88f
                            ),

                        .50f to
                            color.copy(
                                alpha=alpha*.52f
                            ),

                        .73f to
                            color.copy(
                                alpha=alpha*.18f
                            ),

                        .90f to
                            color.copy(
                                alpha=alpha*.04f
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
private fun LandingButton(
    text:String,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val haptic=rememberNmixHapticAction()

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val buttonScale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .955f
                else
                    1f,
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
            .scale(buttonScale)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .30f
                                else
                                    .24f
                        ),

                        p.accentLight.copy(
                            alpha=
                                if(a.darkMode)
                                    .17f
                                else
                                    .14f
                        ),

                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .30f
                                else
                                    .24f
                        )
                    )
                )
            )
            .border(
                .5.dp,
                p.accentLight.copy(
                    alpha=
                        if(a.darkMode)
                            .27f
                        else
                            .46f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null
            ){
                haptic(onClick)
            },
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=Color.White,
            fontSize=13.sp,
            fontWeight=
                FontWeight.SemiBold,
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

            index=
                (index+1)%
                    messages.size
        }
    }

    val outer=
        Brush.linearGradient(
            listOf(
                if(a.darkMode)
                    Color(0xFF111513)
                        .copy(alpha=.82f)
                else
                    Color.White
                        .copy(alpha=.76f),

                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .16f
                        else
                            .12f
                ),

                if(a.darkMode)
                    Color(0xFF0D110F)
                        .copy(alpha=.82f)
                else
                    Color.White
                        .copy(alpha=.74f)
            )
        )

    val inner=
        if(a.darkMode)
            Color(0xFF080B0A)
                .copy(alpha=.56f)
        else
            Color.White
                .copy(alpha=.72f)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(21.dp)
            )
            .background(outer)
            .border(
                .5.dp,
                p.accentLight.copy(
                    alpha=
                        if(a.darkMode)
                            .22f
                        else
                            .40f
                ),
                RoundedCornerShape(21.dp)
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
                    "APP INFO",
                    color=
                        if(a.darkMode)
                            Color.White.copy(
                                alpha=.62f
                            )
                        else
                            Color(0xFF43504B),
                    fontSize=8.sp,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily
                )

                Text(
                    "NMIX",
                    color=
                        if(a.darkMode)
                            Color.White
                        else
                            Color(0xFF222825),
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

            Spacer(
                Modifier.width(7.dp)
            )

            MiniLink("GitHub"){
                openUrl(
                    context,
                    "https://github.com/lxzrvi"
                )
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(9.dp)
        ){
            Box(
                Modifier
                    .weight(1.2f)
                    .height(120.dp)
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .18f
                                else
                                    .27f
                        ),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(11.dp),
                contentAlignment=
                    Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=index,
                    transitionSpec={
                        (
                            fadeIn(tween(380))+
                            slideInVertically(
                                initialOffsetY={
                                    it/6
                                },
                                animationSpec=tween(
                                    380,
                                    easing=EaseOutCubic
                                )
                            )
                        ) togetherWith (
                            fadeOut(tween(280))+
                            slideOutVertically(
                                targetOffsetY={
                                    -it/6
                                },
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
                        color=
                            if(a.darkMode)
                                Color.White.copy(
                                    alpha=.90f
                                )
                            else
                                Color(0xFF303834),
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
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(inner)
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .18f
                                else
                                    .27f
                        ),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=
                        if(a.darkMode)
                            Color.White.copy(
                                alpha=.64f
                            )
                        else
                            Color(0xFF4D5853),
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(8.dp)
                )

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
private fun Tech(
    text:String
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    Box(
        Modifier
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                p.accent.copy(alpha=.24f)
            )
            .padding(
                horizontal=8.dp,
                vertical=3.dp
            )
    ){
        Text(
            text,
            color=
                if(a.darkMode)
                    Color.White
                else
                    Color(0xFF26302C),
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

    val interaction=remember{
        MutableInteractionSource()
    }

    val pressed by
        interaction.collectIsPressedAsState()

    val linkScale by
        animateFloatAsState(
            targetValue=
                if(pressed)
                    .94f
                else
                    1f,
            animationSpec=spring(
                dampingRatio=.70f,
                stiffness=620f
            ),
            label="miniPress"
        )

    Box(
        Modifier
            .scale(linkScale)
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                p.accent.copy(alpha=.27f)
            )
            .border(
                .45.dp,
                p.accentLight.copy(
                    alpha=
                        if(a.darkMode)
                            .20f
                        else
                            .38f
                ),
                RoundedCornerShape(50)
            )
            .clickable(
                interactionSource=interaction,
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
            color=
                if(a.darkMode)
                    Color.White
                else
                    Color(0xFF25302C),
            fontSize=9.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            textAlign=TextAlign.Center
        )
    }
}

private fun mixAppColor(
    first:Color,
    second:Color,
    amount:Float
):Color{
    val t=
        amount.coerceIn(0f,1f)

    return Color(
        red=
            first.red+
                (second.red-first.red)*t,
        green=
            first.green+
                (second.green-first.green)*t,
        blue=
            first.blue+
                (second.blue-first.blue)*t,
        alpha=1f
    )
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
