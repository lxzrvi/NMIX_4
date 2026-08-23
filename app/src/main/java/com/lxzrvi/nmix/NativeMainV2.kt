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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun NativeMainPageV2(onBack:()->Unit){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val context=LocalContext.current
    val config=LocalConfiguration.current
    val density=LocalDensity.current
    val prefs=remember(context){context.getSharedPreferences("nmix_main_display",Context.MODE_PRIVATE)}

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

    BackHandler(enabled=customColorOpen){customColorOpen=false}
    BackHandler(enabled=settings&&!customColorOpen){settings=false}

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
                        op=""
                        second=false
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

    /*
     * Four anchors:
     * S  = 0.00
     * M  = 0.27
     * L  = 0.61
     * EL = 1.00
     */
    val minHeight=190.dp
    val maxHeight=(config.screenHeightDp.dp*.50f).coerceAtLeast(minHeight)
    val range=(maxHeight.value-minHeight.value).coerceAtLeast(1f)
    val small=minHeight
    val medium=(minHeight.value+range*.27f).dp
    val large=(minHeight.value+range*.61f).dp
    val extraLarge=maxHeight
    val anchors=listOf(small,medium,large,extraLarge)

    fun nearest(v:Float)=anchors.minByOrNull{abs(it.value-v)}?:medium

    var targetHeight by remember(config.screenHeightDp){
    var targetHeight by remember(config.screenHeightDp){
        mutableStateOf(
            nearest(
                prefs.getFloat("header_height",390f)
                    .coerceIn(minHeight.value,maxHeight.value)
            )
        )
    }
    
    var liveHeight by remember(config.screenHeightDp){
        mutableStateOf(targetHeight)
    }
    
    var dragging by remember{
        mutableStateOf(false)
    }
    
    val releaseHeightState=animateDpAsState(
    targetValue=targetHeight,
        animationSpec=spring<Dp>(
            dampingRatio=1f,
            stiffness=1250f
        ),
        label="displaySettle"
    )
    
    val releaseHeight=releaseHeightState.value
    
    val displayHeight=
        if(dragging) liveHeight
        else releaseHeight
    
    /*
     * Finger down = direct raw height.
     * Release only = fast nearest-anchor settle.
     */
    val displayHeight=
        if(dragging)liveHeight
        else releaseHeight
    
    /*
     * Do not animate height a second time while dragging.
     */
    val visibleHeight=
        if(top)displayHeight
        else 0.dp
    
    /*
     * List follows Display directly too.
     */
    val listTop=
        if(top)displayHeight+16.dp
        else 112.dp
    
    val normalized=(
        (displayHeight.value-minHeight.value)/range
    ).coerceIn(0f,1f)

    /*
     * S and M stay mathematically flat.
     * Radius starts late between M -> L, reaches old 23dp at L,
     * then remains unchanged through EL.
     */
    val shellProgress=when{
        normalized<=.27f->0f
        normalized>=.61f->1f
    
        else->{
            val t=(
                (normalized-.27f)/.34f
            ).coerceIn(0f,1f)
    
            t*t*(3f-2f*t)
        }
    }
    
    val shellRadius=
        (23f*shellProgress).dp

    val headerShape=RoundedCornerShape(
        bottomStart=shellRadius,
        bottomEnd=shellRadius
    )

    val controlSurface by animateColorAsState(
        if(a.darkMode)Color(0xFF171C1A) else Color(0xFFF7F8F7),
        tween(210),
        label="controlSurface"
    )

    val menuBackground by animateColorAsState(
        if(settings)p.accent else controlSurface,
        tween(250,easing=EaseInOutCubic),
        label="menuBackground"
    )

    val menuIconColor by animateColorAsState(
        if(settings)Color.White else p.accent,
        tween(210),
        label="menuIcon"
    )

    /*
     * Main page is appearance white/black. Accent no longer
     * washes the whole background green/blue/etc.
     */
    val pageBackground=if(a.darkMode)Color(0xFF0D1110) else Color(0xFFF5F7F6)

    Box(
        Modifier.fillMaxSize().background(pageBackground)
    ){
        LazyColumn(
            Modifier
                .fillMaxSize()
                .blur(if(settings||customColorOpen)3.dp else 0.dp),
            contentPadding=PaddingValues(top=listTop,bottom=22.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)
        ){
            item{
                NmixToolSection(
                    NmixIcon.CALCULATOR,"Calculator","Numbers and operations",
                    calcOpen,
                    {
                        open("calculator")
                        mode="calculator"
                        label="CALCULATOR"
                        calcStatus()
                    }
                ){NmixCalculator(::key)}
            }

            item{
                NmixToolSection(
                    NmixIcon.CLOCK,"Clock","Timer, clock and stopwatch",
                    section=="clock",
                    {
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
                                if(timerRun){
                                    startTimerService(timer)
                                    status="Timer running."
                                }else{
                                    stopTimeService()
                                    status="Timer paused."
                                }
                            }
                        },
                        onTimerReset={
                            timerRun=false
                            timer=0
                            mode="timer"
                            stopTimeService()
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
                            if(swRun){
                                startStopwatchService(sw)
                                status="Stopwatch running."
                            }else{
                                stopTimeService()
                                status="Stopwatch paused."
                            }
                        },
                        onStopwatchReset={
                            swRun=false
                            sw=0
                            mode="stopwatch"
                            stopTimeService()
                            status="Stopwatch reset."
                        }
                    )
                }
            }

            item{
                NmixToolSection(
                    NmixIcon.COUNTER,"Counters","Count and generate",
                    section=="counter",
                    {
                        open("counter")
                        stop()
                        mode="counter"
                        display=count.toString()
                        label="COUNTER"
                        status="Counter ready."
                    }
                ){
                    NmixCounters(
                        add={count++;mode="counter";status="Counter increased."},
                        reset={count=0;mode="counter";status="Counter reset to zero."},
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
                    NmixIcon.HELP,"How to use NMIX","Instructions and controls",
                    section=="help",
                    {
                        open("help")
                        stop()
                        mode="idle"
                        label="NMIX LIVE"
                        display="Ready"
                        status="NMIX instructions."
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
                        false,
                        onBack
                    )
                }
            }
            item{Spacer(Modifier.height(8.dp))}
        }

        AnimatedVisibility(
            visible=top,
            modifier=Modifier.blur(if(settings||customColorOpen)3.dp else 0.dp),
            enter=fadeIn(tween(220,easing=EaseOutCubic)),
            exit=fadeOut(tween(170,easing=EaseInCubic))
        ){
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(visibleHeight)
                    .clip(headerShape)
                    .background(Brush.linearGradient(listOf(p.topDark,p.accent,p.topEnd)))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start=12.dp,end=12.dp,top=7.dp,bottom=10.dp)
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
                                "NMIX",
                                color=Color.White,
                                fontSize=27.sp,
                                fontWeight=FontWeight.Bold,
                                letterSpacing=2.2.sp,
                                fontFamily=NmixLogoFont
                            )
                        }
                    }

                    /*
                     * Header normalized progress is passed directly.
                     * Calculator geometry no longer guesses the anchor
                     * from Display's measured pixel height.
                     */
                    NmixDisplay(
                        label=label,
                        value=display,
                        status=status,
                        timer=mode=="timer",
                        calcVisible=calcOpen,
                        calcFirst=n1,
                        calcOperator=op,
                        calcSecond=n2,
                        displayProgress=normalized,
                        onMinus={
                            timer=(timer-5).coerceAtLeast(0)
                            if(timer==0){
                                timerRun=false
                                stopTimeService()
                            }else if(timerRun)startTimerService(timer)
                            status="Five seconds removed."
                        },
                        onPlus={
                            timer+=5
                            if(timerRun)startTimerService(timer)
                            status="Five seconds added."
                        },
                        onClick={
                            if(
                                mode=="calculator"&&
                                n1.isNotEmpty()&&op.isNotEmpty()&&n2.isNotEmpty()
                            )calculate()
                        },
                        modifier=Modifier.fillMaxWidth().weight(1f)
                    )
                }

                /*
                 * Large invisible touch target, tiny visual grip.
                 * Dots are lower than before without creating a new strip.
                 */
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y=6.5.dp)
                        .width(96.dp)
                        .height(32.dp)
                        .pointerInput(minHeight,maxHeight){
                            detectVerticalDragGestures(
                                onDragStart={
                                    liveHeight=displayHeight
                                    dragging=true
                                },
                                onVerticalDrag={change,dragAmount->
                                    change.consume()
                                
                                    val delta=with(density){
                                        dragAmount.toDp()
                                    }
                                
                                    liveHeight=(liveHeight+delta)
                                        .coerceIn(
                                            minHeight,
                                            maxHeight
                                        )
                                },
                                onDragEnd={
                                    val destination=
                                        nearest(liveHeight.value)
                                
                                    targetHeight=destination
                                    dragging=false
                                
                                    prefs.edit()
                                        .putFloat(
                                            "header_height",
                                            destination.value
                                        )
                                        .apply()
                                },
                                onDragCancel={
                                    val destination=
                                        nearest(liveHeight.value)
                                
                                    targetHeight=destination
                                    dragging=false
                                },
                    contentAlignment=Alignment.BottomEnd
                ){
                    Row(
                        Modifier.padding(end=18.dp,bottom=0.dp),
                        horizontalArrangement=Arrangement.spacedBy(3.dp),
                        verticalAlignment=Alignment.CenterVertically
                    ){
                        repeat(4){
                            Box(
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
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

        if(settings){
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha=if(a.darkMode).16f else .055f)
                    )
                    .clickable(
                        interactionSource=remember{MutableInteractionSource()},
                        indication=null
                    ){settings=false}
            )
        }

        AnimatedVisibility(
            visible=settings,
            modifier=Modifier
                .align(Alignment.CenterEnd)
                .blur(if(customColorOpen)4.dp else 0.dp),
            enter=slideInHorizontally(
                initialOffsetX={it},
                animationSpec=tween(390,easing=EaseOutCubic)
            )+fadeIn(tween(210)),
            exit=slideOutHorizontally(
                targetOffsetX={it},
                animationSpec=tween(340,easing=EaseInOutCubic)
            )+fadeOut(tween(175))
        ){
            val drawerShape=RoundedCornerShape(topStart=25.dp,bottomStart=25.dp)

            Box(
                Modifier
                    .width(286.dp)
                    .fillMaxHeight()
                    .clip(drawerShape)
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
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top=10.dp)
                ){
                    NmixSettings{customColorOpen=true}
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start=14.dp,top=13.dp),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(controlSurface)
            ){
                NmixPressBox(
                    Modifier.fillMaxSize(),
                    CircleShape,
                    Color.Transparent,
                    {
                        top=!top
                        if(!top)settings=false
                    }
                ){
                    AnimatedContent(
                        targetState=when{
                            settings->"settings"
                            top->"up"
                            else->"down"
                        },
                        transitionSpec={
                            (fadeIn(tween(160))+scaleIn(initialScale=.82f)) togetherWith
                                (fadeOut(tween(120))+scaleOut(targetScale=.86f))
                        },
                        label="leftControl"
                    ){state->
                        if(state=="settings"){
                            NmixSettingsGlyph(Modifier.size(23.dp),p.accent)
                        }else{
                            NmixIcon(
                                if(state=="down")NmixIcon.ARROW_DOWN else NmixIcon.ARROW_UP,
                                Modifier.size(21.dp),
                                p.accent
                            )
                        }
                    }
                }
            }

            val menuShape=RoundedCornerShape(topStart=25.dp,bottomStart=25.dp)

            Box(
                Modifier
                    .width(66.dp)
                    .height(48.dp)
                    .clip(menuShape)
                    .background(menuBackground)
                    .clickable(
                        interactionSource=remember{MutableInteractionSource()},
                        indication=null
                    ){settings=!settings},
                contentAlignment=Alignment.Center
            ){
                AnimatedContent(
                    targetState=settings,
                    transitionSpec={
                        (fadeIn(tween(160))+scaleIn(initialScale=.82f)) togetherWith
                            (fadeOut(tween(120))+scaleOut(targetScale=.86f))
                    },
                    label="menuControl"
                ){open->
                    NmixIcon(
                        if(open)NmixIcon.CLOSE else NmixIcon.MENU,
                        Modifier.size(21.dp),
                        menuIconColor
                    )
                }
            }
        }

        NmixCustomColorPicker(
            visible=customColorOpen,
            onClose={customColorOpen=false}
        )

        AnimatedVisibility(
            visible=fullscreen,
            modifier=Modifier.fillMaxSize(),
            enter=fadeIn(tween(280)),
            exit=fadeOut(tween(230))
        ){
            NmixFullscreenClock(
                time=timeText(),
                date=dateText()
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
        val c=androidx.compose.ui.geometry.Offset(size.width/2f,size.height/2f)
        val outer=size.minDimension*.36f
        val inner=size.minDimension*.13f
        val stroke=size.minDimension*.075f

        drawCircle(
            color=color,
            radius=outer*.68f,
            center=c,
            style=androidx.compose.ui.graphics.drawscope.Stroke(width=stroke)
        )
        drawCircle(
            color=color,
            radius=inner,
            center=c,
            style=androidx.compose.ui.graphics.drawscope.Stroke(width=stroke)
        )

        repeat(8){i->
            val angle=Math.toRadians(i*45.0-90.0)
            val x1=c.x+kotlin.math.cos(angle).toFloat()*outer*.72f
            val y1=c.y+kotlin.math.sin(angle).toFloat()*outer*.72f
            val x2=c.x+kotlin.math.cos(angle).toFloat()*outer
            val y2=c.y+kotlin.math.sin(angle).toFloat()*outer
            drawLine(
                color=color,
                start=androidx.compose.ui.geometry.Offset(x1,y1),
                end=androidx.compose.ui.geometry.Offset(x2,y2),
                strokeWidth=stroke,
                cap=androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
