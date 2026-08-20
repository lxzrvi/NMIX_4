package com.lxzrvi.nmix

import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    fun swText():String{val s=sw/1000;return "%02d:%02d.%02d".format(s/60,s%60,(sw%1000)/10)}
    fun timeText()=SimpleDateFormat("hh:mm:ss a",Locale.getDefault()).format(Date(now))
    fun dateText()=SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(Date(now))
    fun stop(){timerRun=false;swRun=false}
    fun open(name:String){top=true;settings=false;section=if(section==name)null else name}

    LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();delay(200)}}
    LaunchedEffect(timerRun){
        while(timerRun&&timer>0){
            delay(1000)
            if(timerRun){timer=(timer-1).coerceAtLeast(0);if(timer==0){timerRun=false;status="Time's up!"}}
        }
    }
    LaunchedEffect(swRun){
        if(swRun){
            swBase=SystemClock.elapsedRealtime()-sw
            while(swRun){sw=SystemClock.elapsedRealtime()-swBase;delay(30)}
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
        return if(i.toDouble()==v)i.toString() else String.format(Locale.US,"%.10f",v).trimEnd('0').trimEnd('.')
    }
    fun calcStatus(){status=when{n1.isEmpty()->"Enter your first number.";op.isEmpty()->"Choose an operator.";n2.isEmpty()->"Enter the second number.";else->"Ready — tap = or the large display."}}
    fun calculate(){
        val x=n1.toDoubleOrNull();val y=n2.toDoubleOrNull()
        if(x==null||y==null){display="Incomplete";status="Enter both numbers first.";return}
        val r=when(op){
            "+"->x+y;"−"->x-y;"×"->x*y
            "÷"->{if(y==0.0){display="Error";status="Division by zero is not allowed.";return};x/y}
            "%"->{if(y==0.0){display="Error";status="Remainder by zero is not allowed.";return};x%y}
            else->{display="No sign";status="Choose an operator.";return}
        }
        display=fmt(r);label="RESULT";status="Calculation complete."
    }
    fun key(k:String){
        stop();mode="calculator"
        when(k){
            "+","−","×","÷","%"->{if(n1.isEmpty()){status="Enter the first number first.";return};op=k;second=true;display=k;label="OPERATOR"}
            "="->calculate()
            "."->if(second){if(!n2.contains(".")){n2+=if(n2.isEmpty())"0." else ".";display=n2}}else if(!n1.contains(".")){n1+=if(n1.isEmpty())"0." else ".";display=n1}
            "±"->if(second){n2.toDoubleOrNull()?.let{n2=fmt(-it);display=n2}}else n1.toDoubleOrNull()?.let{n1=fmt(-it);display=n1}
            "⌫"->if(second){if(n2.isNotEmpty()){n2=n2.dropLast(1);display=n2.ifEmpty{"0"}}else{op="";second=false}}else{n1=n1.dropLast(1);display=n1.ifEmpty{"0"}}
            "AC"->{n1="";n2="";op="";second=false;display="Ready";label="CALCULATOR";status="Calculator cleared."}
            else->if(k.all(Char::isDigit)){if(second&&n2.length<18){n2+=k;display=n2;label="SECOND NUMBER"}else if(!second&&n1.length<18){n1+=k;display=n1;label="FIRST NUMBER"}}
        }
        if(k!="="&&k!="AC")calcStatus()
    }

    val calcOpen=section=="calculator"
    val headerTarget=if(top)if(calcOpen)430.dp else 355.dp else 0.dp
    val headerHeight by animateDpAsState(headerTarget,spring(dampingRatio=.88f,stiffness=260f),label="header")
    val listTop by animateDpAsState(if(top)headerTarget+16.dp else 112.dp,spring(dampingRatio=.88f,stiffness=260f),label="list")

    Box(Modifier.fillMaxSize().background(ui.page)){
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding=PaddingValues(top=listTop,bottom=14.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)
        ){
            item{
                NmixToolSection(NmixIcon.CALCULATOR,"Calculator","Numbers and operations",calcOpen,{
                    open("calculator");mode="calculator";label="CALCULATOR";calcStatus()
                }){NmixCalculator(::key)}
            }
            item{
                NmixToolSection(NmixIcon.CLOCK,"Clock","Timer, clock and stopwatch",section=="clock",{
                    open("clock");status="Choose Timer, Clock or Stopwatch."
                }){
                    NmixClockTools(
                        mode=mode,
                        onTimer={
                            swRun=false;mode="timer"
                            if(timer<=0)status="Add five seconds before starting."
                            else{timerRun=!timerRun;status=if(timerRun)"Timer running." else "Timer paused."}
                        },
                        onTimerReset={timerRun=false;timer=0;mode="timer";status="Timer reset to zero."},
                        onClock={stop();mode="clock";status="Live clock is active."},
                        onFullscreen={stop();mode="clock";fullscreen=true},
                        onStopwatch={
                            timerRun=false;mode="stopwatch";swRun=!swRun
                            status=if(swRun)"Stopwatch running." else "Stopwatch paused."
                        },
                        onStopwatchReset={swRun=false;sw=0;mode="stopwatch";status="Stopwatch reset."}
                    )
                }
            }
            item{
                NmixToolSection(NmixIcon.COUNTER,"Counters","Count and generate",section=="counter",{
                    open("counter");stop();mode="counter";status="Counter ready."
                }){
                    NmixCounters(
                        add={count++;mode="counter";status="Counter increased."},
                        reset={count=0;mode="counter";status="Counter reset to zero."},
                        random={count=Random.nextInt(1,1001);mode="counter";status="Random number generated."},
                        minus={count=(count-1).coerceAtLeast(0);mode="counter";status="Counter decreased."}
                    )
                }
            }
            item{
                NmixToolSection(NmixIcon.HELP,"How to use NMIX","Instructions and controls",section=="help",{open("help")}){NmixInstructions()}
            }
            item{Spacer(Modifier.height(70.dp))}
            item{NmixTextButton("Back to the Start",Modifier.fillMaxWidth().padding(horizontal=22.dp).height(44.dp),accent=true,onClick=onBack)}
            item{Spacer(Modifier.height(17.dp))}
            item{
                Row(Modifier.fillMaxWidth().padding(bottom=8.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){
                    Text("NMIX",color=ui.text.copy(alpha=.82f),fontSize=12.sp,fontWeight=FontWeight.Bold)
                    Text("  •  lxzrvi  •  © 2026",color=ui.text.copy(alpha=.55f),fontSize=12.sp)
                }
            }
        }

        AnimatedVisibility(
            visible=top,
            enter=slideInVertically({-it},tween(400,easing=EaseOutCubic))+fadeIn(tween(220)),
            exit=slideOutVertically({-it},tween(350,easing=EaseInCubic))+fadeOut(tween(160))
        ){
            Box(
                Modifier.fillMaxWidth().height(headerHeight)
                    .clip(RoundedCornerShape(bottomStart=23.dp,bottomEnd=23.dp))
                    .background(Brush.linearGradient(listOf(p.topDark,p.accent,p.topEnd)))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start=12.dp,end=12.dp,top=7.dp,bottom=11.dp)
            ){
                Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally){
                    Box(Modifier.fillMaxWidth().height(62.dp),contentAlignment=Alignment.Center){
                        Column(horizontalAlignment=Alignment.CenterHorizontally){
                            Text("EVERYTHING WITH NUMBERS",color=Color.White.copy(alpha=.72f),fontSize=7.5.sp,letterSpacing=1.9.sp)
                            Text("NMIX",color=Color.White,fontSize=29.sp,fontWeight=FontWeight.Bold,letterSpacing=4.sp)
                        }
                    }
                    AnimatedVisibility(
                        calcOpen,
                        enter=expandVertically(tween(300,easing=EaseOutCubic),expandFrom=Alignment.Top)+fadeIn(tween(200)),
                        exit=shrinkVertically(tween(250,easing=EaseInCubic),shrinkTowards=Alignment.Top)+fadeOut(tween(140))
                    ){
                        Row(Modifier.fillMaxWidth().padding(top=7.dp,bottom=8.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                            NmixCalcField(n1.ifEmpty{"_"},Modifier.weight(1f))
                            NmixCalcField(op.ifEmpty{"sign"},Modifier.width(58.dp))
                            NmixCalcField(n2.ifEmpty{"_"},Modifier.weight(1f))
                        }
                    }
                    NmixDisplay(
                        label,display,status,mode=="timer",
                        onMinus={timer=(timer-5).coerceAtLeast(0);if(timer==0)timerRun=false;status="Five seconds removed."},
                        onPlus={timer+=5;status="Five seconds added."},
                        onClick={if(mode=="calculator"&&n1.isNotEmpty()&&op.isNotEmpty()&&n2.isNotEmpty())calculate()},
                        modifier=Modifier.fillMaxWidth().weight(1f)
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start=14.dp,end=14.dp,top=9.dp),
            horizontalArrangement=Arrangement.SpaceBetween
        ){
            NmixCircleButton(if(top)NmixIcon.ARROW_UP else NmixIcon.ARROW_DOWN,Modifier.size(48.dp)){top=!top;if(!top)settings=false}
            NmixCircleButton(if(settings)NmixIcon.CLOSE else NmixIcon.MENU,Modifier.size(48.dp)){settings=!settings}
        }

        AnimatedVisibility(settings,Modifier.align(Alignment.TopEnd),slideInHorizontally({it},tween(360,easing=EaseOutCubic))+fadeIn(tween(180)),slideOutHorizontally({it},tween(300,easing=EaseInCubic))+fadeOut(tween(150))){
            Box(
                Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top=66.dp)
                    .clip(RoundedCornerShape(topStart=22.dp,bottomStart=22.dp))
            ){NmixSettings()}
        }

        if(fullscreen) FullClock(timeText(),dateText(),p,ui){fullscreen=false}
    }
}

@Composable
private fun FullClock(time:String,date:String,p:NmixPalette,ui:NmixUiColors,onExit:()->Unit){
    Dialog(onDismissRequest=onExit,properties=DialogProperties(usePlatformDefaultWidth=false)){
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(p.accent.copy(alpha=.32f),p.topDark,Color(0xFF070D0B))
                )
            )
        ){
            Column(Modifier.align(Alignment.TopStart).windowInsetsPadding(WindowInsets.statusBars).padding(20.dp)){
                Text("EVERYTHING WITH NUMBERS",color=Color.White.copy(alpha=.55f),fontSize=6.sp,letterSpacing=1.5.sp)
                Text("NMIX",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,letterSpacing=3.sp)
            }
            Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
                Text("NMIX • LOCAL TIME",color=Color.White.copy(alpha=.60f),fontSize=10.sp,letterSpacing=2.sp)
                Text(time,color=Color.White,fontSize=52.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(date,color=Color.White.copy(alpha=.68f),fontSize=12.sp)
            }
            NmixTextButton(
                "Exit",
                Modifier.align(Alignment.BottomEnd).windowInsetsPadding(WindowInsets.navigationBars).padding(18.dp).width(90.dp).height(42.dp),
                onClick=onExit
            )
        }
    }
}
