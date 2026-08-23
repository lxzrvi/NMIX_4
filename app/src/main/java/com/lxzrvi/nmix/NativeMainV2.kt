package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun NativeMainPageV2(onBack:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val context=LocalContext.current
    val config=LocalConfiguration.current
    val density=LocalDensity.current
    val haptic=rememberNmixHapticAction()
    val prefs=remember(context){
        context.getSharedPreferences("nmix_main_display",Context.MODE_PRIVATE)
    }

    var top by remember{mutableStateOf(true)}
    var section by remember{mutableStateOf<String?>(null)}
    var settings by remember{mutableStateOf(false)}
    var customColorOpen by remember{mutableStateOf(false)}
    var fullscreen by rememberSaveable{mutableStateOf(false)}

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
        return "%02d:%02d.%02d".format(s/60,s%60,(sw%1000)/10)
    }
    fun timeText()=SimpleDateFormat("hh:mm:ss a",Locale.getDefault()).format(Date(now))
    fun dateText()=SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(Date(now))

    fun stopTimeService(){
        runCatching{
            context.startService(Intent(context,NmixTimeService::class.java).apply{
                action=NmixTimeService.ACTION_STOP
            })
        }
    }

    fun startTimerService(seconds:Int){
        if(seconds<=0)return
        val action:()->Unit={
            ContextCompat.startForegroundService(
                context,
                Intent(context,NmixTimeService::class.java).apply{
                    action=NmixTimeService.ACTION_START_TIMER
                    putExtra(NmixTimeService.EXTRA_TIMER_SECONDS,seconds)
                }
            )
        }
        context.findNmixActivity()?.runWithNotificationPermission(action)?:action()
    }

    fun startStopwatchService(elapsed:Long){
        val action:()->Unit={
            ContextCompat.startForegroundService(
                context,
                Intent(context,NmixTimeService::class.java).apply{
                    action=NmixTimeService.ACTION_START_STOPWATCH
                    putExtra(NmixTimeService.EXTRA_STOPWATCH_ELAPSED,elapsed)
                }
            )
        }
        context.findNmixActivity()?.runWithNotificationPermission(action)?:action()
    }

    fun stop(){
        timerRun=false
        swRun=false
        stopTimeService()
    }

    fun open(name:String){
        if(section=="clock"&&name!="clock"&&(timerRun||swRun))stop()
        top=true
        settings=false
        section=if(section==name)null else name
    }

    BackHandler(customColorOpen){customColorOpen=false}
    BackHandler(settings&&!customColorOpen){settings=false}

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
            "timer"->{display=timerText();label="TIMER"}
            "clock"->{display=timeText();label="LIVE CLOCK"}
            "stopwatch"->{display=swText();label="STOPWATCH"}
            "counter"->{display=count.toString();label="COUNTER"}
        }
    }

    fun fmt(v:Double):String{
        if(!v.isFinite())return "Overflow"
        val i=v.toLong()
        return if(i.toDouble()==v)i.toString()
        else String.format(Locale.US,"%.10f",v).trimEnd('0').trimEnd('.')
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
            display="Incomplete";status="Enter both numbers first.";return
        }

        val result=when(op){
            "+"->x+y
            "−"->x-y
            "×"->x*y
            "÷"->{
                if(y==0.0){
                    display="Error";status="Division by zero is not allowed.";return
                }
                x/y
            }
            "%"->{
                if(y==0.0){
                    display="Error";status="Remainder by zero is not allowed.";return
                }
                x%y
            }
            else->{
                display="No sign";status="Choose an operator.";return
            }
        }

        display=fmt(result)
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
                op=k;second=true;display=k;label="OPERATOR"
            }

            "="->calculate()

            "."->{
                if(second){
                    if(!n2.contains(".")){
                        n2+=if(n2.isEmpty())"0." else "."
                        display=n2
                    }
                }else if(!n1.contains(".")){
                    n1+=if(n1.isEmpty())"0." else "."
                    display=n1
                }
            }

            "±"->{
                if(second)n2.toDoubleOrNull()?.let{n2=fmt(-it);display=n2}
                else n1.toDoubleOrNull()?.let{n1=fmt(-it);display=n1}
            }

            "⌫"->{
                if(second){
                    if(n2.isNotEmpty()){
                        n2=n2.dropLast(1)
                        display=n2.ifEmpty{"0"}
                    }else{
                        op="";second=false
                    }
                }else{
                    n1=n1.dropLast(1)
                    display=n1.ifEmpty{"0"}
                }
            }

            "AC"->{
                n1="";n2="";op="";second=false
                display="Ready";label="CALCULATOR";status="Calculator cleared."
            }

            else->if(k.all(Char::isDigit)){
                if(second&&n2.length<18){
                    n2+=k;display=n2;label="SECOND NUMBER"
                }else if(!second&&n1.length<18){
                    n1+=k;display=n1;label="FIRST NUMBER"
                }
            }
        }

        if(k!="="&&k!="AC")calcStatus()
    }

    val calcOpen=section=="calculator"
    val minHeight=190.dp
    val maxHeight=(config.screenHeightDp.dp*.50f).coerceAtLeast(minHeight)
    val range=(maxHeight.value-minHeight.value).coerceAtLeast(1f)

    /*
     * Exact free height. No anchors, no snap, no release spring.
     */
    var displayHeight by remember(config.screenHeightDp){
        mutableStateOf(
            prefs.getFloat("header_height",390f)
                .coerceIn(minHeight.value,maxHeight.value).dp
        )
    }

    val progress=((displayHeight.value-minHeight.value)/range).coerceIn(0f,1f)
    val visibleHeight=if(top)displayHeight else 0.dp
    val listTop=if(top)displayHeight+16.dp else 112.dp

    /*
     * Shell stays flatter low down, then becomes Display-like smoothly.
     * This never changes Display height.
     */
    val shellT=((progress-.30f)/.34f).coerceIn(0f,1f)
    val shellEase=shellT*shellT*(3f-2f*shellT)
    val shellRadius=(23f*shellEase).dp
    val headerShape=RoundedCornerShape(
        bottomStart=shellRadius,
        bottomEnd=shellRadius
    )

    val controlSurface by animateColorAsState(
        if(a.darkMode)Color(0xFF171C1A) else Color(0xFFF7F8F7),
        tween(180),label="controlSurface"
    )

    val menuBackground by animateColorAsState(
        if(settings)p.accent else controlSurface,
        tween(210),label="menuBackground"
    )

    val menuIconColor by animateColorAsState(
        if(settings)Color.White else p.accent,
        tween(180),label="menuIcon"
    )

    val page=if(a.darkMode)Color(0xFF0D1110) else Color(0xFFF5F7F6)
    val backgroundBlur=if(settings||customColorOpen)4.dp else 0.dp

    Box(Modifier.fillMaxSize().background(page)){

        /*
         * Entire Main visual layer blurs together, including shell + Display.
         * This avoids separate clipped blur boundaries.
         */
        Box(
            Modifier
                .fillMaxSize()
                .blur(backgroundBlur)
        ){
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding=PaddingValues(top=listTop,bottom=22.dp),
                verticalArrangement=Arrangement.spacedBy(12.dp)
            ){
                item{
                    NmixToolSection(
                        NmixIcon.CALCULATOR,"Calculator","Numbers and operations",
                        calcOpen,
                        {
                            haptic{
                                open("calculator")
                                mode="calculator"
                                label="CALCULATOR"
                                calcStatus()
                            }
                        }
                    ){NmixCalculator(::key)}
                }

                item{
                    NmixToolSection(
                        NmixIcon.CLOCK,"Clock","Timer, clock and stopwatch",
                        section=="clock",
                        {
                            haptic{
                                open("clock")
                                stop()
                                mode="clock"
                                label="LIVE CLOCK"
                                display=timeText()
                                status="Live clock is active."
                            }
                        }
                    ){
                        NmixClockTools(
                            mode,
                            {
                                swRun=false
                                mode="timer"
                                if(timer<=0)status="Add five seconds before starting."
                                else{
                                    timerRun=!timerRun
                                    if(timerRun){
                                        startTimerService(timer);status="Timer running."
                                    }else{
                                        stopTimeService();status="Timer paused."
                                    }
                                }
                            },
                            {
                                timerRun=false;timer=0;mode="timer"
                                stopTimeService();status="Timer reset to zero."
                            },
                            {
                                stop();mode="clock";label="LIVE CLOCK"
                                display=timeText();status="Live clock is active."
                            },
                            {
                                stop();mode="clock";settings=false;fullscreen=true
                            },
                            {
                                timerRun=false;mode="stopwatch";swRun=!swRun
                                if(swRun){
                                    startStopwatchService(sw);status="Stopwatch running."
                                }else{
                                    stopTimeService();status="Stopwatch paused."
                                }
                            },
                            {
                                swRun=false;sw=0;mode="stopwatch"
                                stopTimeService();status="Stopwatch reset."
                            }
                        )
                    }
                }

                item{
                    NmixToolSection(
                        NmixIcon.COUNTER,"Counters","Count and generate",
                        section=="counter",
                        {
                            haptic{
                                open("counter")
                                stop();mode="counter"
                                display=count.toString()
                                label="COUNTER";status="Counter ready."
                            }
                        }
                    ){
                        NmixCounters(
                            {count++;mode="counter";status="Counter increased."},
                            {count=0;mode="counter";status="Counter reset to zero."},
                            {
                                count=Random.nextInt(1,1001)
                                mode="counter";status="Random number generated."
                            },
                            {
                                count=(count-1).coerceAtLeast(0)
                                mode="counter";status="Counter decreased."
                            }
                        )
                    }
                }

                item{
                    NmixToolSection(
                        NmixIcon.HELP,"How to use NMIX","Instructions and controls",
                        section=="help",
                        {
                            haptic{
                                open("help");stop();mode="idle"
                                label="NMIX LIVE";display="Ready"
                                status="NMIX instructions."
                            }
                        }
                    ){NmixInstructions()}
                }

                item{Spacer(Modifier.height(4.dp))}
                item{NmixContribution(Modifier.fillMaxWidth())}
                item{Spacer(Modifier.height(2.dp))}
                item{
                    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){
                        NmixTextButton(
                            "Back to the Start",
                            Modifier.width(178.dp).height(42.dp),
                            false,onBack
                        )
                    }
                }
                item{Spacer(Modifier.height(8.dp))}
            }

            AnimatedVisibility(
                visible=top,
                enter=fadeIn(tween(160)),
                exit=fadeOut(tween(140))
            ){
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(visibleHeight)
                        .clip(headerShape)
                        .background(
                            Brush.linearGradient(
                                listOf(p.topDark,p.accent,p.topEnd)
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start=12.dp,end=12.dp,top=7.dp,bottom=12.dp)
                ){
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment=Alignment.CenterHorizontally
                    ){
                        Box(
                            Modifier.fillMaxWidth().height(62.dp),
                            contentAlignment=Alignment.Center
                        ){
                            Column(horizontalAlignment=Alignment.CenterHorizontally){
                                Text(
                                    "EVERYTHING WITH NUMBERS",
                                    color=Color.White.copy(alpha=.72f),
                                    fontSize=7.5.sp,
                                    letterSpacing=1.9.sp,
                                    fontFamily=a.fontFamily
                                )
                                Text(
                                    "NMIX",color=Color.White,fontSize=27.sp,
                                    fontWeight=FontWeight.Bold,
                                    letterSpacing=2.2.sp,fontFamily=NmixLogoFont
                                )
                            }
                        }

                        NmixDisplay(
                            label,display,status,
                            mode=="timer",
                            calcOpen,n1,op,n2,
                            progress,
                            {
                                timer=(timer-5).coerceAtLeast(0)
                                if(timer==0){
                                    timerRun=false;stopTimeService()
                                }else if(timerRun)startTimerService(timer)
                                status="Five seconds removed."
                            },
                            {
                                timer+=5
                                if(timerRun)startTimerService(timer)
                                status="Five seconds added."
                            },
                            {
                                if(
                                    mode=="calculator"&&n1.isNotEmpty()&&
                                    op.isNotEmpty()&&n2.isNotEmpty()
                                )calculate()
                            },
                            Modifier.fillMaxWidth().weight(1f)
                        )
                    }

                    /*
                     * Grip touch target overlaps invisibly.
                     * Visual dots sit centered in the 12dp bottom accent gap.
                     */
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y=7.5.dp)
                            .width(96.dp)
                            .height(32.dp)
                            .pointerInput(minHeight,maxHeight){
                                detectVerticalDragGestures(
                                    onDragStart={haptic{}},
                                    onVerticalDrag={change,amount->
                                        change.consume()
                                        val d=with(density){amount.toDp()}
                                        displayHeight=(displayHeight+d)
                                            .coerceIn(minHeight,maxHeight)
                                    },
                                    onDragEnd={
                                        prefs.edit()
                                            .putFloat("header_height",displayHeight.value)
                                            .apply()
                                        haptic{}
                                    },
                                    onDragCancel={
                                        prefs.edit()
                                            .putFloat("header_height",displayHeight.value)
                                            .apply()
                                    }
                                )
                            },
                        contentAlignment=Alignment.BottomEnd
                    ){
                        Row(
                            Modifier.padding(end=18.dp,bottom=0.dp),
                            horizontalArrangement=Arrangement.spacedBy(3.dp)
                        ){
                            repeat(4){
                                Box(
                                    Modifier.size(4.dp).clip(CircleShape)
                                        .background(
                                            if(a.darkMode)Color.Black.copy(alpha=.92f)
                                            else Color.White.copy(alpha=.97f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        if(settings){
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha=if(a.darkMode).14f else .045f))
                    .clickable(
                        interactionSource=remember{MutableInteractionSource()},
                        indication=null
                    ){settings=false}
            )
        }

        AnimatedVisibility(
            settings,
            Modifier.align(Alignment.CenterEnd)
                .blur(if(customColorOpen)4.dp else 0.dp),
            enter=slideInHorizontally(
                initialOffsetX={it},animationSpec=tween(330,easing=EaseOutCubic)
            )+fadeIn(tween(180)),
            exit=slideOutHorizontally(
                targetOffsetX={it},animationSpec=tween(290,easing=EaseInOutCubic)
            )+fadeOut(tween(150))
        ){
            val shape=RoundedCornerShape(topStart=25.dp,bottomStart=25.dp)

            Box(
                Modifier.width(286.dp).fillMaxHeight().clip(shape)
                    .background(
                        if(a.darkMode)Color(0xFF151917).copy(alpha=.98f)
                        else Color(0xFFF5F7F6).copy(alpha=.99f)
                    )
                    .background(p.accent.copy(alpha=if(a.darkMode).035f else .025f))
                    .clickable(
                        interactionSource=remember{MutableInteractionSource()},
                        indication=null
                    ){}
            ){
                Box(
                    Modifier.fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top=10.dp)
                ){
                    NmixSettings{customColorOpen=true}
                }
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start=14.dp,top=13.dp),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(controlSurface)
            ){
                NmixPressBox(
                    Modifier.fillMaxSize(),CircleShape,Color.Transparent,
                    {
                        top=!top
                        if(!top)settings=false
                    }
                ){
                    AnimatedContent(
                        when{
                            settings->"settings"
                            top->"up"
                            else->"down"
                        },
                        transitionSpec={
                            fadeIn(tween(110)) togetherWith fadeOut(tween(110))
                        },
                        label="leftControl"
                    ){state->
                        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                            if(state=="settings"){
                                NmixSettingsGlyph(Modifier.size(22.dp),p.accent)
                            }else{
                                NmixIcon(
                                    if(state=="down")NmixIcon.ARROW_DOWN else NmixIcon.ARROW_UP,
                                    Modifier.size(21.dp),p.accent
                                )
                            }
                        }
                    }
                }
            }

            val menuShape=RoundedCornerShape(topStart=25.dp,bottomStart=25.dp)

            Box(
                Modifier.width(66.dp).height(48.dp).clip(menuShape)
                    .background(menuBackground)
                    .clickable(
                        interactionSource=remember{MutableInteractionSource()},
                        indication=null
                    ){
                        haptic{settings=!settings}
                    },
                contentAlignment=Alignment.Center
            ){
                AnimatedContent(
                    settings,
                    transitionSpec={
                        fadeIn(tween(130)) togetherWith fadeOut(tween(110))
                    },
                    label="menu"
                ){open->
                    NmixIcon(
                        if(open)NmixIcon.CLOSE else NmixIcon.MENU,
                        Modifier.size(21.dp),menuIconColor
                    )
                }
            }
        }

        NmixCustomColorPicker(
            customColorOpen,
            {customColorOpen=false}
        )

        AnimatedVisibility(
            fullscreen,
            Modifier.fillMaxSize(),
            enter=fadeIn(tween(240)),
            exit=fadeOut(tween(200))
        ){
            NmixFullscreenClock(
                timeText(),
                dateText()
            ){fullscreen=false}
        }
    }
}

@Composable
private fun NmixSettingsGlyph(
    modifier:Modifier=Modifier,
    color:Color
){
    androidx.compose.foundation.Canvas(modifier){
        val c=androidx.compose.ui.geometry.Offset(size.width/2,size.height/2)
        val outer=size.minDimension*.36f
        val stroke=size.minDimension*.075f

        drawCircle(
            color,outer*.68f,c,
            style=androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        )
        drawCircle(
            color,size.minDimension*.13f,c,
            style=androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        )

        repeat(8){i->
            val angle=Math.toRadians(i*45.0-90.0)
            val cos=kotlin.math.cos(angle).toFloat()
            val sin=kotlin.math.sin(angle).toFloat()

            drawLine(
                color,
                androidx.compose.ui.geometry.Offset(
                    c.x+cos*outer*.72f,
                    c.y+sin*outer*.72f
                ),
                androidx.compose.ui.geometry.Offset(
                    c.x+cos*outer,
                    c.y+sin*outer
                ),
                stroke,
                androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
