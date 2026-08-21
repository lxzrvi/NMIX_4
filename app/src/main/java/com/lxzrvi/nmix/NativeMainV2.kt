package com.lxzrvi.nmix

import android.os.SystemClock
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun NativeMainPageV2(onBack:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    var top by remember{mutableStateOf(true)}
    var section by remember{mutableStateOf<String?>(null)}
    var settings by remember{mutableStateOf(false)}
    var fullscreen by remember{mutableStateOf(false)}
    var mode by remember{mutableStateOf("idle")}
    var display by remember{mutableStateOf("Ready")}
    var label by remember{mutableStateOf("NMIX LIVE")}
    var status by remember{mutableStateOf("Choose a tool below.")}

    var n1 by remember{mutableStateOf("")}
    var n2 by remember{mutableStateOf("")}
    var op by remember{mutableStateOf("")}
    var second by remember{mutableStateOf(false)}

    var timer by remember{mutableIntStateOf(0)}
    var timerRun by remember{mutableStateOf(false)}
    var sw by remember{mutableLongStateOf(0L)}
    var swRun by remember{mutableStateOf(false)}
    var swBase by remember{mutableLongStateOf(0L)}
    var count by remember{mutableIntStateOf(0)}
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}

    fun timerText()="%02d:%02d".format(timer/60,timer%60)

    fun swText():String{
        val s=sw/1000
        return "%02d:%02d.%02d".format(
            s/60,s%60,(sw%1000)/10
        )
    }

    fun timeText()=SimpleDateFormat(
        "hh:mm:ss a",
        Locale.getDefault()
    ).format(Date(now))

    fun dateText()=SimpleDateFormat(
        "EEEE, d MMMM yyyy",
        Locale.getDefault()
    ).format(Date(now))

    fun stop(){
        timerRun=false
        swRun=false
    }

    fun open(name:String){
        top=true
        settings=false
        section=if(section==name)null else name
    }

    LaunchedEffect(Unit){
        while(true){
            now=System.currentTimeMillis()
            delay(200)
        }
    }

    LaunchedEffect(timerRun){
        while(timerRun&&timer>0){
            delay(1000)

            if(timerRun){
                timer=(timer-1).coerceAtLeast(0)

                if(timer==0){
                    timerRun=false
                    status="Time's up!"
                }
            }
        }
    }

    LaunchedEffect(swRun){
        if(swRun){
            swBase=SystemClock.elapsedRealtime()-sw

            while(swRun){
                sw=SystemClock.elapsedRealtime()-swBase
                delay(30)
            }
        }
    }

    LaunchedEffect(mode,timer,sw,now,count){
        when(mode){
            "timer"->{
                display=timerText()
                label="TIMER"
            }

            "clock"->{
                display=timeText()
                label="LIVE CLOCK"
            }

            "stopwatch"->{
                display=swText()
                label="STOPWATCH"
            }

            "counter"->{
                display=count.toString()
                label="COUNTER"
            }
        }
    }

    fun fmt(v:Double):String{
        if(!v.isFinite())return "Overflow"

        val i=v.toLong()

        return if(i.toDouble()==v){
            i.toString()
        }else{
            String.format(
                Locale.US,
                "%.10f",
                v
            ).trimEnd('0').trimEnd('.')
        }
    }

    fun calcStatus(){
        status=when{
            n1.isEmpty()->"Enter your first number."
            op.isEmpty()->"Choose an operator."
            n2.isEmpty()->"Enter the second number."
            else->"Ready — tap = or the large display."
        }
    }

    fun calculate(){
        val x=n1.toDoubleOrNull()
        val y=n2.toDoubleOrNull()

        if(x==null||y==null){
            display="Incomplete"
            status="Enter both numbers first."
            return
        }

        val r=when(op){
            "+"->x+y
            "−"->x-y
            "×"->x*y

            "÷"->{
                if(y==0.0){
                    display="Error"
                    status="Division by zero is not allowed."
                    return
                }
                x/y
            }

            "%"->{
                if(y==0.0){
                    display="Error"
                    status="Remainder by zero is not allowed."
                    return
                }
                x%y
            }

            else->{
                display="No sign"
                status="Choose an operator."
                return
            }
        }

        display=fmt(r)
        label="RESULT"
        status="Calculation complete."
    }

    fun key(k:String){
        stop()
        mode="calculator"

        when(k){
            "+","−","×","÷","%"->{
                if(n1.isEmpty()){
                    status="Enter the first number first."
                    return
                }

                op=k
                second=true
                display=k
                label="OPERATOR"
            }

            "="->calculate()

            "."->if(second){
                if(!n2.contains(".")){
                    n2+=if(n2.isEmpty())"0." else "."
                    display=n2
                }
            }else if(!n1.contains(".")){
                n1+=if(n1.isEmpty())"0." else "."
                display=n1
            }

            "±"->if(second){
                n2.toDoubleOrNull()?.let{
                    n2=fmt(-it)
                    display=n2
                }
            }else{
                n1.toDoubleOrNull()?.let{
                    n1=fmt(-it)
                    display=n1
                }
            }

            "⌫"->if(second){
                if(n2.isNotEmpty()){
                    n2=n2.dropLast(1)
                    display=n2.ifEmpty{"0"}
                }else{
                    op=""
                    second=false
                }
            }else{
                n1=n1.dropLast(1)
                display=n1.ifEmpty{"0"}
            }

            "AC"->{
                n1=""
                n2=""
                op=""
                second=false
                display="Ready"
                label="CALCULATOR"
                status="Calculator cleared."
            }

            else->if(k.all(Char::isDigit)){
                if(second&&n2.length<18){
                    n2+=k
                    display=n2
                    label="SECOND NUMBER"
                }else if(!second&&n1.length<18){
                    n1+=k
                    display=n1
                    label="FIRST NUMBER"
                }
            }
        }

        if(k!="="&&k!="AC"){
            calcStatus()
        }
    }

    val calcOpen=section=="calculator"
    val sectionOpen=section!=null

    val headerTarget=when{
        !top->0.dp
        calcOpen->430.dp
        else->355.dp
    }

    val headerHeight by animateDpAsState(
        targetValue=headerTarget,
        animationSpec=tween(
            420,
            easing=EaseInOutCubic
        ),
        label="header"
    )

    val listTop by animateDpAsState(
        targetValue=
            if(top)
                headerTarget+16.dp
            else
                112.dp,
        animationSpec=tween(
            420,
            easing=EaseInOutCubic
        ),
        label="listTop"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(ui.page)
    ){
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding=PaddingValues(
                top=listTop,
                bottom=if(sectionOpen)14.dp else 135.dp
            ),
            verticalArrangement=Arrangement.spacedBy(12.dp)
        ){
            item{
                NmixToolSection(
                    icon=NmixIcon.CALCULATOR,
                    title="Calculator",
                    subtitle="Numbers and operations",
                    open=calcOpen,
                    onClick={
                        open("calculator")
                        mode="calculator"
                        label="CALCULATOR"
                        calcStatus()
                    }
                ){
                    NmixCalculator(::key)
                }
            }

            item{
                NmixToolSection(
                    icon=NmixIcon.CLOCK,
                    title="Clock",
                    subtitle="Timer, clock and stopwatch",
                    open=section=="clock",
                    onClick={
                        open("clock")
                        stop()
                        mode="clock"
                        label="LIVE CLOCK"
                        display=timeText()
                        status="Live clock is active."
                    }
                ){
                    NmixClockTools(
                        mode=mode,

                        onTimer={
                            swRun=false
                            mode="timer"

                            if(timer<=0){
                                status="Add five seconds before starting."
                            }else{
                                timerRun=!timerRun
                                status=if(timerRun)
                                    "Timer running."
                                else
                                    "Timer paused."
                            }
                        },

                        onTimerReset={
                            timerRun=false
                            timer=0
                            mode="timer"
                            status="Timer reset to zero."
                        },

                        onClock={
                            stop()
                            mode="clock"
                            label="LIVE CLOCK"
                            display=timeText()
                            status="Live clock is active."
                        },

                        onFullscreen={
                            stop()
                            mode="clock"
                            settings=false
                            fullscreen=true
                        },

                        onStopwatch={
                            timerRun=false
                            mode="stopwatch"
                            swRun=!swRun

                            status=if(swRun)
                                "Stopwatch running."
                            else
                                "Stopwatch paused."
                        },

                        onStopwatchReset={
                            swRun=false
                            sw=0
                            mode="stopwatch"
                            status="Stopwatch reset."
                        }
                    )
                }
            }

            item{
                NmixToolSection(
                    icon=NmixIcon.COUNTER,
                    title="Counters",
                    subtitle="Count and generate",
                    open=section=="counter",
                    onClick={
                        open("counter")
                        stop()
                        mode="counter"
                        display=count.toString()
                        label="COUNTER"
                        status="Counter ready."
                    }
                ){
                    NmixCounters(
                        add={
                            count++
                            mode="counter"
                            status="Counter increased."
                        },

                        reset={
                            count=0
                            mode="counter"
                            status="Counter reset to zero."
                        },

                        random={
                            count=Random.nextInt(1,1001)
                            mode="counter"
                            status="Random number generated."
                        },

                        minus={
                            count=(count-1).coerceAtLeast(0)
                            mode="counter"
                            status="Counter decreased."
                        }
                    )
                }
            }

            item{
                NmixToolSection(
                    icon=NmixIcon.HELP,
                    title="How to use NMIX",
                    subtitle="Instructions and controls",
                    open=section=="help",
                    onClick={
                        open("help")
                        stop()
                        mode="idle"
                        label="NMIX LIVE"
                        display="Ready"
                        status="NMIX instructions."
                    }
                ){
                    NmixInstructions()
                }
            }

            if(sectionOpen){
                item{
                    Spacer(
                        Modifier.height(38.dp)
                    )
                }

                item{
                    PageBackButton(onBack)
                }

                item{
                    Spacer(
                        Modifier.height(17.dp)
                    )
                }

                item{
                    PageFooter()
                }
            }
        }

        /*
         * Closed state gets a real bottom layout slot.
         * It does not depend on header/listTop, so collapsing
         * the display cannot move this footer.
         */
        AnimatedVisibility(
            visible=!sectionOpen,
            modifier=Modifier.align(Alignment.BottomCenter),
            enter=
                fadeIn(tween(280))+
                slideInVertically(
                    initialOffsetY={it/5},
                    animationSpec=tween(
                        360,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(tween(180))+
                slideOutVertically(
                    targetOffsetY={it/5},
                    animationSpec=tween(
                        250,
                        easing=EaseInCubic
                    )
                )
        ){
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(ui.page)
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
                    .padding(bottom=8.dp),
                horizontalAlignment=Alignment.CenterHorizontally
            ){
                PageBackButton(onBack)

                Spacer(
                    Modifier.height(17.dp)
                )

                PageFooter()
            }
        }

        AnimatedVisibility(
            visible=top,
            enter=fadeIn(
                tween(
                    280,
                    easing=EaseOutCubic
                )
            ),
            exit=fadeOut(
                tween(
                    220,
                    easing=EaseInCubic
                )
            )
        ){
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .clip(
                        RoundedCornerShape(
                            bottomStart=23.dp,
                            bottomEnd=23.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                p.topDark,
                                p.accent,
                                p.topEnd
                            )
                        )
                    )
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
                    .padding(
                        start=12.dp,
                        end=12.dp,
                        top=7.dp,
                        bottom=11.dp
                    )
            ){
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment=Alignment.CenterHorizontally
                ){
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        contentAlignment=Alignment.Center
                    ){
                        Column(
                            horizontalAlignment=Alignment.CenterHorizontally
                        ){
                            Text(
                                "EVERYTHING WITH NUMBERS",
                                color=Color.White.copy(alpha=.72f),
                                fontSize=7.5.sp,
                                letterSpacing=1.9.sp,
                                fontFamily=a.fontFamily
                            )

                            Text(
                                "NMIX",
                                color=Color.White,
                                fontSize=27.sp,
                                fontWeight=FontWeight.Bold,
                                letterSpacing=2.2.sp,
                                fontFamily=NmixLogoFont
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible=calcOpen,
                        enter=
                            expandVertically(
                                animationSpec=tween(
                                    420,
                                    easing=EaseInOutCubic
                                ),
                                expandFrom=Alignment.Top
                            )+
                            fadeIn(tween(250)),
                        exit=
                            shrinkVertically(
                                animationSpec=tween(
                                    420,
                                    easing=EaseInOutCubic
                                ),
                                shrinkTowards=Alignment.Top
                            )+
                            fadeOut(tween(200))
                    ){
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top=7.dp,
                                    bottom=8.dp
                                ),
                            horizontalArrangement=Arrangement.spacedBy(7.dp)
                        ){
                            NmixCalcField(
                                n1.ifEmpty{"_"},
                                Modifier.weight(1f)
                            )

                            NmixCalcField(
                                op.ifEmpty{"sign"},
                                Modifier.width(58.dp)
                            )

                            NmixCalcField(
                                n2.ifEmpty{"_"},
                                Modifier.weight(1f)
                            )
                        }
                    }

                    NmixDisplay(
                        label=label,
                        value=display,
                        status=status,
                        timer=mode=="timer",

                        onMinus={
                            timer=(timer-5).coerceAtLeast(0)

                            if(timer==0){
                                timerRun=false
                            }

                            status="Five seconds removed."
                        },

                        onPlus={
                            timer+=5
                            status="Five seconds added."
                        },

                        onClick={
                            if(
                                mode=="calculator"&&
                                n1.isNotEmpty()&&
                                op.isNotEmpty()&&
                                n2.isNotEmpty()
                            ){
                                calculate()
                            }
                        },

                        modifier=Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        if(settings){
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha=
                                if(a.darkMode)
                                    .15f
                                else
                                    .05f
                        )
                    )
                    .clickable(
                        interactionSource=remember{
                            MutableInteractionSource()
                        },
                        indication=null
                    ){
                        settings=false
                    }
            )
        }

        AnimatedVisibility(
            visible=settings,
            modifier=Modifier.align(Alignment.CenterEnd),
            enter=
                slideInHorizontally(
                    initialOffsetX={it},
                    animationSpec=tween(
                        460,
                        easing=EaseOutCubic
                    )
                )+
                fadeIn(tween(250)),
            exit=
                slideOutHorizontally(
                    targetOffsetX={it},
                    animationSpec=tween(
                        420,
                        easing=EaseInOutCubic
                    )
                )+
                fadeOut(tween(220))
        ){
            Box(
                Modifier
                    .width(286.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart=25.dp,
                            bottomStart=25.dp
                        )
                    )
                    .background(
                        if(a.darkMode)
                            Color(0xFF151917)
                        else
                            Color(0xFFF0F3F1)
                    )
                    .clickable(
                        interactionSource=remember{
                            MutableInteractionSource()
                        },
                        indication=null
                    ){}
            ){
                Box(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.statusBars
                        )
                        .padding(top=10.dp)
                ){
                    NmixSettings()
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    start=14.dp,
                    top=9.dp
                ),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically
        ){
            NmixCircleButton(
                icon=
                    if(top)
                        NmixIcon.ARROW_UP
                    else
                        NmixIcon.ARROW_DOWN,
                modifier=Modifier.size(48.dp),
                onClick={
                    top=!top

                    if(!top){
                        settings=false
                    }
                }
            )

            Box(
                Modifier
                    .width(66.dp)
                    .height(48.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart=25.dp,
                            bottomStart=25.dp
                        )
                    )
                    .background(p.accent)
                    .clickable(
                        interactionSource=remember{
                            MutableInteractionSource()
                        },
                        indication=null
                    ){
                        settings=!settings
                    },
                contentAlignment=Alignment.Center
            ){
                AnimatedContent(
                    targetState=settings,
                    transitionSpec={
                        (
                            fadeIn(tween(180))+
                            scaleIn(
                                initialScale=.82f,
                                animationSpec=tween(220)
                            )
                        ) togetherWith (
                            fadeOut(tween(140))+
                            scaleOut(
                                targetScale=.82f,
                                animationSpec=tween(180)
                            )
                        )
                    },
                    label="menuIcon"
                ){open->
                    NmixIcon(
                        if(open)
                            NmixIcon.CLOSE
                        else
                            NmixIcon.MENU,
                        Modifier.size(21.dp),
                        Color.White
                    )
                }
            }
        }

        AnimatedVisibility(
            visible=fullscreen,
            modifier=Modifier.fillMaxSize(),
            enter=fadeIn(tween(420)),
            exit=fadeOut(tween(340))
        ){
            FullClock(
                time=timeText(),
                date=dateText(),
                onExit={fullscreen=false}
            )
        }
    }
}

@Composable
private fun PageBackButton(
    onBack:()->Unit
){
    NmixTextButton(
        text="Back to the Start",
        modifier=Modifier
            .fillMaxWidth()
            .padding(horizontal=22.dp)
            .height(44.dp),
        accent=true,
        onClick=onBack
    )
}

@Composable
private fun PageFooter(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom=8.dp),
        horizontalArrangement=Arrangement.Center,
        verticalAlignment=Alignment.CenterVertically
    ){
        Text(
            "NMIX",
            color=ui.text.copy(alpha=.82f),
            fontSize=12.sp,
            fontWeight=FontWeight.Bold,
            fontFamily=NmixLogoFont
        )

        Text(
            "  •  lxzrvi  •  © 2026",
            color=ui.text.copy(alpha=.55f),
            fontSize=12.sp,
            fontFamily=a.fontFamily
        )
    }
}

@Composable
private fun FullClock(
    time:String,
    date:String,
    onExit:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val activity=LocalActivity.current

    DisposableEffect(activity){
        val window=activity?.window

        if(window!=null){
            WindowCompat.setDecorFitsSystemWindows(
                window,
                false
            )

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply{
                hide(
                    WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
                )

                systemBarsBehavior=
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose{
            if(window!=null){
                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).show(
                    WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
                )
            }
        }
    }

    val motion=rememberInfiniteTransition(
        label="fullClockMotion"
    )

    val x by motion.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(
            animation=tween(
                3400,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="clockX"
    )

    val y by motion.animateFloat(
        1f,
        -1f,
        infiniteRepeatable(
            animation=tween(
                4300,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="clockY"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020403),
                        p.topDark,
                        Color(0xFF07100D),
                        Color(0xFF010201)
                    )
                )
            )
    ){
        Box(
            Modifier
                .size(680.dp)
                .align(Alignment.TopStart)
                .offset(
                    x=(-300).dp,
                    y=(-290).dp
                )
                .graphicsLayer{
                    translationX=x*340f
                    translationY=y*160f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accent.copy(alpha=.38f),
                            p.accent.copy(alpha=.12f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            Modifier
                .size(600.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x=260.dp,
                    y=250.dp
                )
                .graphicsLayer{
                    translationX=-x*280f
                    translationY=-y*140f
                }
                .background(
                    Brush.radialGradient(
                        listOf(
                            p.accentLight.copy(alpha=.24f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            Modifier
                .align(Alignment.TopStart)
                .padding(22.dp)
        ){
            Text(
                "EVERYTHING WITH NUMBERS",
                color=Color.White.copy(alpha=.54f),
                fontSize=7.sp,
                letterSpacing=1.5.sp,
                fontFamily=a.fontFamily
            )

            Text(
                "NMIX",
                color=Color.White,
                fontSize=24.sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=2.sp,
                fontFamily=NmixLogoFont
            )
        }

        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment=Alignment.CenterHorizontally
        ){
            Text(
                "NMIX • LOCAL TIME",
                color=p.accentLight.copy(alpha=.92f),
                fontSize=10.sp,
                letterSpacing=2.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(13.dp))

            Text(
                time,
                color=Color.White,
                fontSize=50.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=a.fontFamily
            )

            Spacer(Modifier.height(8.dp))

            Text(
                date,
                color=Color.White.copy(alpha=.68f),
                fontSize=12.sp,
                fontFamily=a.fontFamily
            )
        }

        NmixTextButton(
            text="Exit",
            modifier=Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .width(94.dp)
                .height(42.dp),
            onClick=onExit
        )
    }
}
