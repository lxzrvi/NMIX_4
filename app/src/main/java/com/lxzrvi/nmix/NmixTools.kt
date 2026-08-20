package com.lxzrvi.nmix

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NmixCalculator(onKey:(String)->Unit){
    val keys=listOf("1","2","3","4","5","6","7","8","9","0","+","−","×","÷","%",".","±","⌫","AC","=")
    Column(Modifier.fillMaxWidth().padding(10.dp)){
        keys.chunked(5).forEach{row->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
                row.forEach{k->NmixKey(k,Modifier.size(55.dp),when{ k in listOf("+","−","×","÷","%","=")->1;k=="AC"->2;else->0}){onKey(k)}}
            }
            Spacer(Modifier.height(9.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NmixClockTools(mode:String,onTimer:()->Unit,onTimerReset:()->Unit,onClock:()->Unit,onFullscreen:()->Unit,onStopwatch:()->Unit,onStopwatchReset:()->Unit){
    Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        ModeRow(NmixIcon.TIMER,"Timer",mode=="timer",onTimer,onTimerReset)
        Box(Modifier.fillMaxWidth()){
            ModeRow(NmixIcon.CLOCK,"Clock",mode=="clock",onClock,null)
            NmixSmallIconButton(NmixIcon.FULLSCREEN,Modifier.align(Alignment.CenterEnd).padding(end=10.dp).size(38.dp),mode=="clock",onFullscreen)
        }
        ModeRow(NmixIcon.STOPWATCH,"Stopwatch",mode=="stopwatch",onStopwatch,onStopwatchReset)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModeRow(icon:NmixIcon,title:String,selected:Boolean,onClick:()->Unit,onLong:(()->Unit)?){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if(pressed).965f else 1f,label="mode")
    Row(
        Modifier.fillMaxWidth().height(58.dp).scale(scale).then(
            Modifier.combinedClickable(
                interactionSource=interaction,indication=null,
                onClick=onClick,onLongClick={onLong?.invoke()}
            )
        ).padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        NmixOption(icon,title,selected,Modifier.fillMaxSize(),onClick={})
    }
}

@Composable
fun NmixCounters(add:()->Unit,reset:()->Unit,random:()->Unit,minus:()->Unit){
    val ui=LocalNmixAppearance.current.uiColors()
    Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            CounterButton(NmixIcon.PLUS,"Add",Modifier.weight(1f),add)
            CounterButton(NmixIcon.RESET,"Reset",Modifier.weight(1f),reset)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            CounterButton(NmixIcon.RANDOM,"Random",Modifier.weight(1f),random)
            CounterButton(NmixIcon.MINUS,"Minus",Modifier.weight(1f),minus)
        }
    }
}

@Composable
private fun CounterButton(icon:NmixIcon,title:String,modifier:Modifier,onClick:()->Unit){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    NmixPressBox(modifier.height(64.dp),RoundedCornerShape(12.dp),ui.accentGlassStrong,onClick){
        Row(verticalAlignment=Alignment.CenterVertically){
            NmixIcon(icon,Modifier.size(18.dp),a.palette.accent)
            Spacer(Modifier.width(8.dp))
            Text(title,color=ui.text,fontSize=12.sp)
        }
    }
}

@Composable
fun NmixInstructions(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val p=a.palette
    val data=listOf(
        "Calculator" to "Enter numbers with the NMIX keypad. Use +, −, ×, ÷ or %. Tap = or the large display to calculate.",
        "Editing" to "Use decimal, ±, backspace and AC to edit or clear calculations.",
        "Timer" to "Tap Timer to start or pause. Hold Timer to reset to zero. Use − / + on the main display.",
        "Clock" to "Tap Clock for local time. Use the fullscreen icon for the full-screen clock.",
        "Stopwatch" to "Tap to start or pause. Hold Stopwatch to reset.",
        "Counters" to "Add and Minus change the value. Reset returns to zero. Random generates 1–1000.",
        "Top Screen" to "Use the top-left vector arrow to hide or restore the NMIX display.",
        "Settings" to "Use the top-right menu for dark mode and color themes."
    )
    Column(Modifier.padding(11.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
        data.forEach{(title,body)->
            NmixGlassBox(Modifier.fillMaxWidth()){
                Column(Modifier.padding(11.dp)){
                    Text(title,color=p.accent,fontSize=11.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Text(body,color=ui.muted,fontSize=9.sp,lineHeight=14.sp)
                }
            }
        }
    }
}

@Composable
fun NmixSettings(){
    val a=LocalNmixAppearance.current
    val ui=a.uiColors()
    val p=a.palette
    Column(
        Modifier.width(330.dp)
            .backgroundCompat(ui.page)
            .padding(17.dp)
    ){
        Text("NMIX Settings",color=ui.text,fontSize=15.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold)
        Text("Personalize your interface",color=ui.muted,fontSize=9.sp)
        Spacer(Modifier.height(20.dp))
        val interaction=remember{MutableInteractionSource()}
        Row(
            Modifier.fillMaxWidth().combinedClickable(
                interactionSource=interaction,indication=null,
                onClick={a.toggleDarkMode()},onLongClick={}
            ).padding(vertical=7.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(Modifier.weight(1f)){
                Text("Appearance",color=ui.text,fontSize=12.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text(if(a.darkMode)"Dark mode" else "Light mode",color=ui.muted,fontSize=9.sp)
            }
            NmixSwitch(a.darkMode,p.accent){a.toggleDarkMode()}
        }
        Spacer(Modifier.height(20.dp))
        Text("Color Theme",color=ui.text,fontSize=12.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
        Text("Choose your NMIX color",color=ui.muted,fontSize=9.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
            NmixThemeName.entries.forEach{t->
                val selected=a.theme==t
                val c=t.palette().accent
                NmixPressBox(Modifier.size(if(selected)42.dp else 38.dp),androidx.compose.foundation.shape.CircleShape,c,{a.setTheme(t)}){
                    if(selected) NmixIcon(NmixIcon.CHECK,Modifier.size(18.dp),androidx.compose.ui.graphics.Color.White)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Theme and appearance are saved on this device.",color=ui.muted,fontSize=8.sp)
    }
}

@Composable
private fun NmixSwitch(on:Boolean,accent:androidx.compose.ui.graphics.Color,onClick:()->Unit){
    Box(
        Modifier.width(49.dp).height(28.dp)
            .backgroundCompat(if(on)accent else androidx.compose.ui.graphics.Color(0xFFD0D5D2))
            .padding(4.dp)
            .then(Modifier),
        contentAlignment=if(on)Alignment.CenterEnd else Alignment.CenterStart
    ){
        NmixPressBox(Modifier.size(20.dp),androidx.compose.foundation.shape.CircleShape,androidx.compose.ui.graphics.Color.White,onClick){}
    }
}

private fun Modifier.backgroundCompat(color:androidx.compose.ui.graphics.Color):Modifier=
    this.then(androidx.compose.ui.Modifier.background(color, RoundedCornerShape(topStart=20.dp,bottomStart=20.dp)))
