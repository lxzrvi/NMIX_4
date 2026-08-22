package com.lxzrvi.nmix

import android.content.Context
import android.content.Intent
import android.os.SystemClock
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun NativeMainPageV2(
    onBack:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val context=LocalContext.current
    val configuration=LocalConfiguration.current
    val density=LocalDensity.current

    val headerPrefs=remember(context){
        context.getSharedPreferences(
            "nmix_main_display",
            Context.MODE_PRIVATE
        )
    }

    var top by remember{
        mutableStateOf(true)
    }

    var section by remember{
        mutableStateOf<String?>(null)
    }

    var settings by remember{
        mutableStateOf(false)
    }

    var customColorOpen by remember{
        mutableStateOf(false)
    }

    var fullscreen by rememberSaveable{
        mutableStateOf(false)
    }

    var mode by remember{
        mutableStateOf("idle")
    }

    var display by remember{
        mutableStateOf("Ready")
    }

    var label by remember{
        mutableStateOf("NMIX LIVE")
    }

    var status by remember{
        mutableStateOf(
            "Choose a tool below."
        )
    }

    var n1 by remember{
        mutableStateOf("")
    }

    var n2 by remember{
        mutableStateOf("")
    }

    var op by remember{
        mutableStateOf("")
    }

    var second by remember{
        mutableStateOf(false)
    }

    var timer by remember{
        mutableIntStateOf(0)
    }

    var timerRun by remember{
        mutableStateOf(false)
    }

    var sw by remember{
        mutableLongStateOf(0L)
    }

    var swRun by remember{
        mutableStateOf(false)
    }

    var swBase by remember{
        mutableLongStateOf(0L)
    }

    var count by remember{
        mutableIntStateOf(0)
    }

    var now by remember{
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    fun timerText():String{
        return "%02d:%02d".format(
            timer/60,
            timer%60
        )
    }

    fun swText():String{
        val s=sw/1000

        return "%02d:%02d.%02d".format(
            s/60,
            s%60,
            (sw%1000)/10
        )
    }

    fun timeText():String{
        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(
            Date(now)
        )
    }

    fun dateText():String{
        return SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        ).format(
            Date(now)
        )
    }

    fun stopTimeService(){
        runCatching{
            context.startService(
                Intent(
                    context,
                    NmixTimeService::class.java
                ).apply{
                    action=
                        NmixTimeService.ACTION_STOP
                }
            )
        }
    }

    fun startTimerService(
        seconds:Int
    ){
        if(seconds<=0){
            return
        }

        val action:()->Unit={
            val intent=
                Intent(
                    context,
                    NmixTimeService::class.java
                ).apply{
                    action=
                        NmixTimeService
                            .ACTION_START_TIMER

                    putExtra(
                        NmixTimeService
                            .EXTRA_TIMER_SECONDS,
                        seconds
                    )
                }

            ContextCompat.startForegroundService(
                context,
                intent
            )
        }

        val activity=
            context.findNmixActivity()

        if(activity!=null){
            activity.runWithNotificationPermission(
                action
            )
        }else{
            action()
        }
    }

    fun startStopwatchService(
        elapsed:Long
    ){
        val action:()->Unit={
            val intent=
                Intent(
                    context,
                    NmixTimeService::class.java
                ).apply{
                    action=
                        NmixTimeService
                            .ACTION_START_STOPWATCH

                    putExtra(
                        NmixTimeService
                            .EXTRA_STOPWATCH_ELAPSED,
                        elapsed
                    )
                }

            ContextCompat.startForegroundService(
                context,
                intent
            )
        }

        val activity=
            context.findNmixActivity()

        if(activity!=null){
            activity.runWithNotificationPermission(
                action
            )
        }else{
            action()
        }
    }

    fun stop(){
        timerRun=false
        swRun=false
        stopTimeService()
    }

    fun open(name:String){
        /*
         * Leaving Timer / Stopwatch tool while they
         * are active closes their service notification.
         * Closing the app itself does not execute this.
         */
        if(
            section=="clock" &&
            name!="clock" &&
            (timerRun || swRun)
        ){
            stop()
        }

        top=true
        settings=false

        section=
            if(section==name)
                null
            else
                name
    }

    LaunchedEffect(Unit){
        while(true){
            now=
                System.currentTimeMillis()

            delay(200)
        }
    }

    LaunchedEffect(timerRun){
        while(
            timerRun &&
            timer>0
        ){
            delay(1000)

            if(timerRun){
                timer=
                    (timer-1)
                        .coerceAtLeast(0)

                if(timer==0){
                    timerRun=false
                    status="Time's up!"
                }
            }
        }
    }

    LaunchedEffect(swRun){
        if(swRun){
            swBase=
                SystemClock.elapsedRealtime()-sw

            while(swRun){
                sw=
                    SystemClock.elapsedRealtime()-swBase

                delay(30)
            }
        }
    }

    LaunchedEffect(
        mode,
        timer,
        sw,
        now,
        count
    ){
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
        if(!v.isFinite()){
            return "Overflow"
        }

        val integer=v.toLong()

        return if(
            integer.toDouble()==v
        ){
            integer.toString()
        }else{
            String.format(
                Locale.US,
                "%.10f",
                v
            )
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    fun calcStatus(){
        status=when{
            n1.isEmpty()->
                "Enter your first number."

            op.isEmpty()->
                "Choose an operator."

            n2.isEmpty()->
                "Enter the second number."

            else->
                "Ready — tap = or the large display."
        }
    }

    fun calculate(){
        val x=n1.toDoubleOrNull()
        val y=n2.toDoubleOrNull()

        if(
            x==null ||
            y==null
        ){
            display="Incomplete"
            status="Enter both numbers first."
            return
        }

        val result=when(op){
            "+"->x+y
            "−"->x-y
            "×"->x*y

            "÷"->{
                if(y==0.0){
                    display="Error"
                    status=
                        "Division by zero is not allowed."
                    return
                }

                x/y
            }

            "%"->{
                if(y==0.0){
                    display="Error"
                    status=
                        "Remainder by zero is not allowed."
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
                    status=
                        "Enter the first number first."
                    return
                }

                op=k
                second=true
                display=k
                label="OPERATOR"
            }

            "="->{
                calculate()
            }

            "."->{
                if(second){
                    if(!n2.contains(".")){
                        n2+=
                            if(n2.isEmpty())
                                "0."
                            else
                                "."

                        display=n2
                    }
                }else if(!n1.contains(".")){
                    n1+=
                        if(n1.isEmpty())
                            "0."
                        else
                            "."

                    display=n1
                }
            }

            "±"->{
                if(second){
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
            }

            "⌫"->{
                if(second){
                    if(n2.isNotEmpty()){
                        n2=n2.dropLast(1)

                        display=
                            n2.ifEmpty{
                                "0"
                            }
                    }else{
                        op=""
                        second=false
                    }
                }else{
                    n1=n1.dropLast(1)

                    display=
                        n1.ifEmpty{
                            "0"
                        }
                }
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

            else->{
                if(k.all(Char::isDigit)){
                    if(
                        second &&
                        n2.length<18
                    ){
                        n2+=k
                        display=n2
                        label="SECOND NUMBER"
                    }else if(
                        !second &&
                        n1.length<18
                    ){
                        n1+=k
                        display=n1
                        label="FIRST NUMBER"
                    }
                }
            }
        }

        if(
            k!="=" &&
            k!="AC"
        ){
            calcStatus()
        }
    }

    val calcOpen=
        section=="calculator"

    val minimumHeaderHeight=
        190.dp

    val maximumHeaderHeight=
        (
            configuration
                .screenHeightDp.dp*
                .50f
        ).coerceAtLeast(
            minimumHeaderHeight
        )

    val heightRange=
        (
            maximumHeaderHeight.value-
                minimumHeaderHeight.value
        ).coerceAtLeast(1f)

    /*
     * Four stable heights.
     */
    val smallHeight=
        minimumHeaderHeight

    val compactHeight=
        (
            minimumHeaderHeight.value+
                heightRange*.25f
        ).dp

    val mediumHeight=
        (
            minimumHeaderHeight.value+
                heightRange*.58f
        ).dp

    val largeHeight=
        maximumHeaderHeight

    fun nearestHeight(
        value:Float
    )=
        listOf(
            smallHeight,
            compactHeight,
            mediumHeight,
            largeHeight
        ).minByOrNull{
            kotlin.math.abs(
                it.value-value
            )
        } ?: mediumHeight

    var targetHeaderHeight by remember(
        configuration.screenHeightDp
    ){
        mutableStateOf(
            nearestHeight(
                headerPrefs
                    .getFloat(
                        "header_height",
                        390f
                    )
                    .coerceIn(
                        minimumHeaderHeight.value,
                        maximumHeaderHeight.value
                    )
            )
        )
    }

    var dragHeight by remember(
        configuration.screenHeightDp
    ){
        mutableStateOf(
            targetHeaderHeight
        )
    }

    var draggingHeader by remember{
        mutableStateOf(false)
    }

    val settledHeaderHeight by
        animateDpAsState(
            targetValue=
                if(draggingHeader)
                    dragHeight
                else
                    targetHeaderHeight,
            animationSpec=
                if(draggingHeader){
                    tween(
                        durationMillis=50,
                        easing=LinearEasing
                    )
                }else{
                    spring(
                        dampingRatio=.90f,
                        stiffness=430f
                    )
                },
            label="settledHeader"
        )

    val visibleHeaderHeight by
        animateDpAsState(
            targetValue=
                if(top)
                    settledHeaderHeight
                else
                    0.dp,
            animationSpec=tween(
                320,
                easing=EaseInOutCubic
            ),
            label="headerCollapse"
        )

    val listTop by
        animateDpAsState(
            targetValue=
                if(top)
                    settledHeaderHeight+
                        16.dp
                else
                    112.dp,
            animationSpec=tween(
                300,
                easing=EaseInOutCubic
            ),
            label="listPosition"
        )

    val normalizedHeight=
        (
            (
                settledHeaderHeight.value-
                    minimumHeaderHeight.value
            )/
                heightRange
        ).coerceIn(
            0f,
            1f
        )

    /*
     * Small + Compact:
     * straight accent shell.
     *
     * Compact -> Medium:
     * radius returns very quickly and smoothly.
     *
     * Medium + Large:
     * normal old rounded shell.
     */
    val shellProgress=
        (
            (normalizedHeight-.28f)/
                .18f
        ).coerceIn(
            0f,
            1f
        )

    val shellRadius=
        (
            23f*
                FastOutSlowInEasing
                    .transform(
                        shellProgress
                    )
        ).dp

    val headerShape=
        RoundedCornerShape(
            bottomStart=shellRadius,
            bottomEnd=shellRadius
        )

    val controlSurface by
        animateColorAsState(
            targetValue=
                if(a.darkMode)
                    Color(0xFF171C1A)
                else
                    Color(0xFFF7F8F7),
            animationSpec=tween(230),
            label="controlSurface"
        )

    val menuBackground by
        animateColorAsState(
            targetValue=
                if(settings)
                    p.accent
                else
                    controlSurface,
            animationSpec=tween(
                280,
                easing=EaseInOutCubic
            ),
            label="menuBackground"
        )

    val menuIconColor by
        animateColorAsState(
            targetValue=
                if(settings)
                    Color.White
                else
                    p.accent,
            animationSpec=tween(230),
            label="menuIcon"
        )

    val pageBackground=
        Brush.verticalGradient(
            colorStops=
                if(a.darkMode){
                    arrayOf(
                        0f to ui.page,

                        .18f to
                            p.accent.copy(
                                alpha=.075f
                            ),

                        .48f to ui.page,

                        .78f to
                            p.accent.copy(
                                alpha=.032f
                            ),

                        1f to ui.page
                    )
                }else{
                    arrayOf(
                        0f to
                            Color(0xFFF7F8F7),

                        .20f to
                            p.accent.copy(
                                alpha=.065f
                            ),

                        .48f to
                            Color(0xFFF5F7F6),

                        .80f to
                            p.accent.copy(
                                alpha=.028f
                            ),

                        1f to
                            Color(0xFFF7F8F7)
                    )
                }
        )

    Box(
        Modifier
            .fillMaxSize()
            .background(pageBackground)
    ){
        LazyColumn(
            Modifier
                .fillMaxSize()
                .blur(
                    if(
                        settings ||
                        customColorOpen
                    )
                        3.dp
                    else
                        0.dp
                ),
            contentPadding=
                PaddingValues(
                    top=listTop,
                    bottom=22.dp
                ),
            verticalArrangement=
                Arrangement.spacedBy(12.dp)
        ){
            item{
                NmixToolSection(
                    icon=NmixIcon.CALCULATOR,
                    title="Calculator",
                    subtitle=
                        "Numbers and operations",
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
                    subtitle=
                        "Timer, clock and stopwatch",
                    open=
                        section=="clock",
                    onClick={
                        open("clock")
                        stop()

                        mode="clock"
                        label="LIVE CLOCK"
                        display=timeText()
                        status=
                            "Live clock is active."
                    }
                ){
                    NmixClockTools(
                        mode=mode,

                        onTimer={
                            swRun=false
                            mode="timer"

                            if(timer<=0){
                                status=
                                    "Add five seconds before starting."
                            }else{
                                timerRun=
                                    !timerRun

                                if(timerRun){
                                    startTimerService(
                                        timer
                                    )

                                    status=
                                        "Timer running."
                                }else{
                                    stopTimeService()

                                    status=
                                        "Timer paused."
                                }
                            }
                        },

                        onTimerReset={
                            timerRun=false
                            timer=0
                            mode="timer"

                            stopTimeService()

                            status=
                                "Timer reset to zero."
                        },

                        onClock={
                            stop()

                            mode="clock"
                            label="LIVE CLOCK"
                            display=timeText()

                            status=
                                "Live clock is active."
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

                            swRun=
                                !swRun

                            if(swRun){
                                startStopwatchService(
                                    sw
                                )

                                status=
                                    "Stopwatch running."
                            }else{
                                stopTimeService()

                                status=
                                    "Stopwatch paused."
                            }
                        },

                        onStopwatchReset={
                            swRun=false
                            sw=0
                            mode="stopwatch"

                            stopTimeService()

                            status=
                                "Stopwatch reset."
                        }
                    )
                }
            }

            item{
                NmixToolSection(
                    icon=NmixIcon.COUNTER,
                    title="Counters",
                    subtitle=
                        "Count and generate",
                    open=
                        section=="counter",
                    onClick={
                        open("counter")
                        stop()

                        mode="counter"

                        display=
                            count.toString()

                        label="COUNTER"
                        status="Counter ready."
                    }
                ){
                    NmixCounters(
                        add={
                            count++
                            mode="counter"

                            status=
                                "Counter increased."
                        },

                        reset={
                            count=0
                            mode="counter"

                            status=
                                "Counter reset to zero."
                        },

                        random={
                            count=
                                Random.nextInt(
                                    1,
                                    1001
                                )

                            mode="counter"

                            status=
                                "Random number generated."
                        },

                        minus={
                            count=
                                (count-1)
                                    .coerceAtLeast(0)

                            mode="counter"

                            status=
                                "Counter decreased."
                        }
                    )
                }
            }

            item{
                NmixToolSection(
                    icon=NmixIcon.HELP,
                    title=
                        "How to use NMIX",
                    subtitle=
                        "Instructions and controls",
                    open=
                        section=="help",
                    onClick={
                        open("help")
                        stop()

                        mode="idle"
                        label="NMIX LIVE"
                        display="Ready"

                        status=
                            "NMIX instructions."
                    }
                ){
                    NmixInstructions()
                }
            }

            item{
                Spacer(
                    Modifier.height(4.dp)
                )
            }

            item{
                NmixContribution(
                    Modifier.fillMaxWidth()
                )
            }

            item{
                Spacer(
                    Modifier.height(2.dp)
                )
            }

            item{
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment=
                        Alignment.Center
                ){
                    NmixTextButton(
                        text=
                            "Back to the Start",
                        modifier=
                            Modifier
                                .width(178.dp)
                                .height(42.dp),
                        accent=false,
                        onClick=onBack
                    )
                }
            }

            item{
                Spacer(
                    Modifier.height(8.dp)
                )
            }
        }

        AnimatedVisibility(
            visible=top,
            modifier=
                Modifier.blur(
                    if(
                        settings ||
                        customColorOpen
                    )
                        3.dp
                    else
                        0.dp
                ),
            enter=
                fadeIn(
                    tween(
                        240,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(
                    tween(
                        190,
                        easing=EaseInCubic
                    )
                )
        ){
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(
                        visibleHeaderHeight
                    )
                    .clip(headerShape)
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
                        bottom=7.dp
                    )
            ){
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment=
                        Alignment.CenterHorizontally
                ){
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        contentAlignment=
                            Alignment.Center
                    ){
                        Column(
                            horizontalAlignment=
                                Alignment.CenterHorizontally
                        ){
                            Text(
                                "EVERYTHING WITH NUMBERS",
                                color=
                                    Color.White.copy(
                                        alpha=.72f
                                    ),
                                fontSize=7.5.sp,
                                letterSpacing=1.9.sp,
                                fontFamily=a.fontFamily
                            )

                            Text(
                                "NMIX",
                                color=Color.White,
                                fontSize=27.sp,
                                fontWeight=
                                    FontWeight.Bold,
                                letterSpacing=2.2.sp,
                                fontFamily=
                                    NmixLogoFont
                            )
                        }
                    }

                    /*
                     * No dedicated 25dp strip anymore.
                     *
                     * Display keeps the same ~7dp edge
                     * gap as its side spacing.
                     */
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ){
                        NmixDisplay(
                            label=label,
                            value=display,
                            status=status,
                            timer=
                                mode=="timer",

                            calcVisible=
                                calcOpen,

                            calcFirst=n1,
                            calcOperator=op,
                            calcSecond=n2,

                            onMinus={
                                timer=
                                    (timer-5)
                                        .coerceAtLeast(
                                            0
                                        )

                                if(timer==0){
                                    timerRun=false
                                    stopTimeService()
                                }else if(timerRun){
                                    startTimerService(
                                        timer
                                    )
                                }

                                status=
                                    "Five seconds removed."
                            },

                            onPlus={
                                timer+=5

                                if(timerRun){
                                    startTimerService(
                                        timer
                                    )
                                }

                                status=
                                    "Five seconds added."
                            },

                            onClick={
                                if(
                                    mode=="calculator" &&
                                    n1.isNotEmpty() &&
                                    op.isNotEmpty() &&
                                    n2.isNotEmpty()
                                ){
                                    calculate()
                                }
                            },

                            modifier=
                                Modifier.fillMaxSize()
                        )

                        /*
                         * Large invisible touch target,
                         * but the four dots are drawn
                         * outside the Display surface in
                         * the existing bottom accent gap.
                         *
                         * Negative translation changes
                         * visuals only; it does not add
                         * layout height.
                         */
                        Box(
                            Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .offset(
                                    y=7.dp
                                )
                                .width(86.dp)
                                .height(24.dp)
                                .pointerInput(
                                    minimumHeaderHeight,
                                    maximumHeaderHeight
                                ){
                                    detectVerticalDragGestures(
                                        onDragStart={
                                            draggingHeader=true
                                            dragHeight=
                                                settledHeaderHeight
                                        },

                                        onVerticalDrag={
                                            change,
                                            dragAmount->

                                            change.consume()

                                            val delta=
                                                with(density){
                                                    dragAmount
                                                        .toDp()
                                                }

                                            dragHeight=
                                                (
                                                    dragHeight+
                                                        delta
                                                ).coerceIn(
                                                    minimumHeaderHeight,
                                                    maximumHeaderHeight
                                                )
                                        },

                                        onDragEnd={
                                            draggingHeader=false

                                            targetHeaderHeight=
                                                nearestHeight(
                                                    dragHeight.value
                                                )

                                            headerPrefs
                                                .edit()
                                                .putFloat(
                                                    "header_height",
                                                    targetHeaderHeight
                                                        .value
                                                )
                                                .apply()
                                        },

                                        onDragCancel={
                                            draggingHeader=false

                                            targetHeaderHeight=
                                                nearestHeight(
                                                    dragHeight.value
                                                )
                                        }
                                    )
                                },
                            contentAlignment=
                                Alignment.CenterEnd
                        ){
                            Row(
                                Modifier.padding(
                                    end=18.dp
                                ),
                                horizontalArrangement=
                                    Arrangement.spacedBy(
                                        3.dp
                                    ),
                                verticalAlignment=
                                    Alignment.CenterVertically
                            ){
                                repeat(4){
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(
                                                CircleShape
                                            )
                                            .background(
                                                if(a.darkMode)
                                                    Color.Black.copy(
                                                        alpha=.92f
                                                    )
                                                else
                                                    Color.White.copy(
                                                        alpha=.97f
                                                    )
                                            )
                                    )
                                }
                            }
                        }
                    }
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
                                    .18f
                                else
                                    .07f
                        )
                    )
                    .clickable(
                        interactionSource=
                            remember{
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
            modifier=
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .blur(
                        if(customColorOpen)
                            4.dp
                        else
                            0.dp
                    ),
            enter=
                slideInHorizontally(
                    initialOffsetX={
                        it
                    },
                    animationSpec=tween(
                        410,
                        easing=EaseOutCubic
                    )
                )+
                fadeIn(
                    tween(220)
                ),
            exit=
                slideOutHorizontally(
                    targetOffsetX={
                        it
                    },
                    animationSpec=tween(
                        360,
                        easing=EaseInOutCubic
                    )
                )+
                fadeOut(
                    tween(190)
                )
        ){
            val drawerShape=
                RoundedCornerShape(
                    topStart=25.dp,
                    bottomStart=25.dp
                )

            Box(
                Modifier
                    .width(286.dp)
                    .fillMaxHeight()
                    .clip(drawerShape)
                    .background(
                        if(a.darkMode){
                            Color(0xFF151917)
                                .copy(alpha=.95f)
                        }else{
                            Color(0xFFF4F6F5)
                                .copy(alpha=.96f)
                        }
                    )
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .055f
                                else
                                    .065f
                        )
                    )
                    .clickable(
                        interactionSource=
                            remember{
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
                        .padding(
                            top=10.dp
                        )
                ){
                    NmixSettings(
                        onCustomColor={
                            customColorOpen=true
                        }
                    )
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
                    top=13.dp
                ),
            horizontalArrangement=
                Arrangement.SpaceBetween,
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        controlSurface
                    )
            ){
                NmixPressBox(
                    modifier=
                        Modifier.fillMaxSize(),
                    shape=CircleShape,
                    color=Color.Transparent,
                    onClick={
                        top=!top

                        if(!top){
                            settings=false
                        }
                    }
                ){
                    AnimatedContent(
                        targetState=
                            when{
                                settings->
                                    "settings"

                                top->
                                    "up"

                                else->
                                    "down"
                            },
                        transitionSpec={
                            (
                                fadeIn(
                                    tween(160)
                                )+
                                scaleIn(
                                    initialScale=.82f
                                )
                            ) togetherWith (
                                fadeOut(
                                    tween(130)
                                )+
                                scaleOut(
                                    targetScale=.84f
                                )
                            )
                        },
                        label="leftControl"
                    ){state->
                        NmixIcon(
                            when(state){
                                "settings"->
                                    NmixIcon.SETTINGS

                                "down"->
                                    NmixIcon.ARROW_DOWN

                                else->
                                    NmixIcon.ARROW_UP
                            },
                            Modifier.size(21.dp),
                            p.accent
                        )
                    }
                }
            }

            val menuShape=
                RoundedCornerShape(
                    topStart=25.dp,
                    bottomStart=25.dp
                )

            Box(
                Modifier
                    .width(66.dp)
                    .height(48.dp)
                    .clip(menuShape)
                    .background(
                        menuBackground
                    )
                    .clickable(
                        interactionSource=
                            remember{
                                MutableInteractionSource()
                            },
                        indication=null
                    ){
                        settings=
                            !settings
                    },
                contentAlignment=
                    Alignment.Center
            ){
                AnimatedContent(
                    targetState=settings,
                    transitionSpec={
                        (
                            fadeIn(
                                tween(170)
                            )+
                            scaleIn(
                                initialScale=.80f
                            )
                        ) togetherWith (
                            fadeOut(
                                tween(130)
                            )+
                            scaleOut(
                                targetScale=.84f
                            )
                        )
                    },
                    label="menuControl"
                ){open->
                    NmixIcon(
                        if(open)
                            NmixIcon.CLOSE
                        else
                            NmixIcon.MENU,
                        Modifier.size(21.dp),
                        menuIconColor
                    )
                }
            }
        }

        NmixCustomColorPicker(
            visible=
                customColorOpen,
            onClose={
                customColorOpen=false
            }
        )

        AnimatedVisibility(
            visible=fullscreen,
            modifier=
                Modifier.fillMaxSize(),
            enter=
                fadeIn(
                    tween(300)
                ),
            exit=
                fadeOut(
                    tween(260)
                )
        ){
            NmixFullscreenClock(
                time=timeText(),
                date=dateText(),
                onExit={
                    fullscreen=false
                }
            )
        }
    }
}
