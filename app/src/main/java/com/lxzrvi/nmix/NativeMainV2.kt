package com.lxzrvi.nmix

import android.content.Context
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
        ).format(Date(now))
    }

    fun dateText():String{
        return SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.getDefault()
        ).format(Date(now))
    }

    fun stop(){
        timerRun=false
        swRun=false
    }

    fun open(name:String){
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
            now=System.currentTimeMillis()
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
                SystemClock.elapsedRealtime()-
                    sw

            while(swRun){
                sw=
                    SystemClock.elapsedRealtime()-
                        swBase

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

            "="->calculate()

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
                    n2.toDoubleOrNull()
                        ?.let{
                            n2=fmt(-it)
                            display=n2
                        }
                }else{
                    n1.toDoubleOrNull()
                        ?.let{
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
                .screenHeightDp
                .dp*
                .50f
        ).coerceAtLeast(
            minimumHeaderHeight
        )

    var openHeaderHeight by remember(
        configuration.screenHeightDp
    ){
        mutableStateOf(
            headerPrefs
                .getFloat(
                    "header_height",
                    390f
                )
                .dp
                .coerceIn(
                    minimumHeaderHeight,
                    maximumHeaderHeight
                )
        )
    }

    val animatedHeaderHeight by
        animateDpAsState(
            targetValue=
                if(top)
                    openHeaderHeight
                else
                    0.dp,
            animationSpec=tween(
                380,
                easing=EaseInOutCubic
            ),
            label="headerCollapse"
        )

    val headerHeight=
        if(top)
            openHeaderHeight
        else
            animatedHeaderHeight

    val animatedListTop by
        animateDpAsState(
            targetValue=
                if(top)
                    openHeaderHeight+16.dp
                else
                    112.dp,
            animationSpec=tween(
                380,
                easing=EaseInOutCubic
            ),
            label="listCollapse"
        )

    val listTop=
        if(top)
            openHeaderHeight+16.dp
        else
            animatedListTop
        Box(
        Modifier
            .fillMaxSize()
            .background(ui.page)
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
                    open=section=="clock",
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

                                status=
                                    if(timerRun)
                                        "Timer running."
                                    else
                                        "Timer paused."
                            }
                        },

                        onTimerReset={
                            timerRun=false
                            timer=0
                            mode="timer"

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
                            swRun=!swRun

                            status=
                                if(swRun)
                                    "Stopwatch running."
                                else
                                    "Stopwatch paused."
                        },

                        onStopwatchReset={
                            swRun=false
                            sw=0
                            mode="stopwatch"

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
                    title="How to use NMIX",
                    subtitle=
                        "Instructions and controls",
                    open=section=="help",
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
                        text="Back to the Start",
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

        /*
         * ==================================================
         * TOP ACCENT HEADER + DISPLAY
         * ==================================================
         */

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
                        280,
                        easing=EaseOutCubic
                    )
                ),
            exit=
                fadeOut(
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
                        /*
                         * Accent strip remains visible
                         * underneath the Display for
                         * the resize handle.
                         */
                        bottom=13.dp
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
                                fontFamily=NmixLogoFont
                            )
                        }
                    }

                    NmixDisplay(
                        label=label,
                        value=display,
                        status=status,
                        timer=
                            mode=="timer",

                        calcVisible=calcOpen,
                        calcFirst=n1,
                        calcOperator=op,
                        calcSecond=n2,

                        onMinus={
                            timer=
                                (timer-5)
                                    .coerceAtLeast(0)

                            if(timer==0){
                                timerRun=false
                            }

                            status=
                                "Five seconds removed."
                        },

                        onPlus={
                            timer+=5

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
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                    )
                }

                /*
                 * ==================================================
                 * RESIZE GRIP
                 * ==================================================
                 *
                 * Grip belongs to the accent strip,
                 * not the Display surface.
                 *
                 * Invisible touch target is larger
                 * than the visible four dots.
                 */
                Box(
                    Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .width(66.dp)
                        .height(30.dp)
                        .padding(
                            end=10.dp,
                            bottom=2.dp
                        )
                        .pointerInput(
                            minimumHeaderHeight,
                            maximumHeaderHeight
                        ){
                            detectVerticalDragGestures(
                                onVerticalDrag={
                                    change,
                                    dragAmount->

                                    change.consume()

                                    val deltaDp=
                                        with(density){
                                            dragAmount.toDp()
                                        }

                                    openHeaderHeight=
                                        (
                                            openHeaderHeight+
                                                deltaDp
                                        )
                                            .coerceIn(
                                                minimumHeaderHeight,
                                                maximumHeaderHeight
                                            )
                                },

                                onDragEnd={
                                    headerPrefs
                                        .edit()
                                        .putFloat(
                                            "header_height",
                                            openHeaderHeight.value
                                        )
                                        .apply()
                                }
                            )
                        },
                    contentAlignment=
                        Alignment.BottomCenter
                ){
                    Row(
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
                                    .clip(CircleShape)
                                    .background(
                                        Color.White.copy(
                                            alpha=.90f
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        /*
         * ==================================================
         * SETTINGS OUTSIDE SCRIM
         * ==================================================
         */

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

        /*
         * ==================================================
         * SETTINGS DRAWER
         * ==================================================
         */

        AnimatedVisibility(
            visible=settings,
            modifier=
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    /*
                     * Custom picker sits above the
                     * drawer. Drawer blurs behind it.
                     */
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
                        460,
                        easing=EaseOutCubic
                    )
                )+
                fadeIn(
                    tween(250)
                ),
            exit=
                slideOutHorizontally(
                    targetOffsetX={
                        it
                    },
                    animationSpec=tween(
                        420,
                        easing=EaseInOutCubic
                    )
                )+
                fadeOut(
                    tween(220)
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
                                .copy(alpha=.94f)
                        }else{
                            Color(0xFFF0F4F2)
                                .copy(alpha=.94f)
                        }
                    )
                    .background(
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .055f
                                else
                                    .075f
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
                /*
         * ==================================================
         * TOP CONTROLS
         * ==================================================
         */

        /*
         * Arrow blurs whenever Settings is open.
         * Custom picker increases the same blur.
         */
        val arrowBlur by animateDpAsState(
            targetValue=
                when{
                    customColorOpen->
                        5.dp

                    settings->
                        3.dp

                    else->
                        0.dp
                },
            animationSpec=tween(
                260,
                easing=EaseInOutCubic
            ),
            label="arrowBlur"
        )

        /*
         * Hamburger background:
         *
         * closed = theme-aware neutral surface
         * open   = accent
         *
         * animateColorAsState gives a smooth
         * surface transition instead of a snap.
         */
        val menuBackground by
            animateColorAsState(
                targetValue=
                    if(settings){
                        p.accent
                    }else if(a.darkMode){
                        Color(0xFF171C1A)
                    }else{
                        Color(0xFFE7EBE9)
                    },
                animationSpec=tween(
                    300,
                    easing=EaseInOutCubic
                ),
                label="menuBackground"
            )

        /*
         * Closed menu icon = accent.
         * Open X = readable color over accent.
         */
        val menuIconColor by
            animateColorAsState(
                targetValue=
                    if(settings){
                        Color.White
                    }else{
                        p.accent
                    },
                animationSpec=tween(
                    260,
                    easing=EaseInOutCubic
                ),
                label="menuIconColor"
            )

        val arrowSurface by
            animateColorAsState(
                targetValue=
                    if(a.darkMode){
                        Color(0xFF171C1A)
                    }else{
                        Color(0xFFE7EBE9)
                    },
                animationSpec=tween(
                    260,
                    easing=EaseInOutCubic
                ),
                label="arrowSurface"
            )

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
            horizontalArrangement=
                Arrangement.SpaceBetween,
            verticalAlignment=
                Alignment.CenterVertically
        ){
            /*
             * LEFT ARROW
             *
             * No border.
             * No glass wrapper.
             * Theme surface + accent icon.
             */
            Box(
                Modifier
                    .size(48.dp)
                    .blur(arrowBlur)
                    .clip(CircleShape)
                    .background(
                        arrowSurface
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
                    NmixIcon(
                        if(top)
                            NmixIcon.ARROW_UP
                        else
                            NmixIcon.ARROW_DOWN,
                        Modifier.size(21.dp),
                        p.accent
                    )
                }
            }

            /*
             * RIGHT MENU / X
             */
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
                        settings=!settings
                    },
                contentAlignment=
                    Alignment.Center
            ){
                AnimatedContent(
                    targetState=settings,
                    transitionSpec={
                        (
                            fadeIn(
                                animationSpec=tween(
                                    190,
                                    easing=EaseOutCubic
                                )
                            )+
                            scaleIn(
                                initialScale=.78f,
                                animationSpec=tween(
                                    240,
                                    easing=EaseOutCubic
                                )
                            )
                        ) togetherWith (
                            fadeOut(
                                animationSpec=tween(
                                    150,
                                    easing=EaseInCubic
                                )
                            )+
                            scaleOut(
                                targetScale=.82f,
                                animationSpec=tween(
                                    190,
                                    easing=EaseInOutCubic
                                )
                            )
                        )
                    },
                    label="menuIcon"
                ){open:Boolean->
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

        /*
         * ==================================================
         * CUSTOM COLOR PICKER
         * ==================================================
         */

        NmixCustomColorPicker(
            visible=customColorOpen,
            onClose={
                customColorOpen=false
            }
        )

        /*
         * ==================================================
         * FULLSCREEN CLOCK
         * ==================================================
         */

        AnimatedVisibility(
            visible=fullscreen,
            modifier=
                Modifier.fillMaxSize(),
            enter=
                fadeIn(
                    tween(420)
                ),
            exit=
                fadeOut(
                    tween(340)
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
