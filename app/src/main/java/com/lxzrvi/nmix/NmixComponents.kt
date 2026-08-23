package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

private fun mix(a:Float,b:Float,t:Float)=a+(b-a)*t.coerceIn(0f,1f)

@Composable
private fun screenColor()=
    if(LocalNmixAppearance.current.darkMode) Color(0xFF111614)
    else Color(0xFFF8F9F8)

@Composable
private fun screenBorder():Color{
    val a=LocalNmixAppearance.current
    return a.palette.accent.copy(alpha=if(a.darkMode).15f else .23f)
}

@Composable
private fun displayText()=
    if(LocalNmixAppearance.current.darkMode) Color.White.copy(alpha=.93f)
    else Color(0xFF202522)

@Composable
fun NmixToolSection(
    icon:NmixIcon,title:String,subtitle:String,open:Boolean,
    onClick:()->Unit,content:@Composable ()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val progress by animateFloatAsState(
        if(open)1f else 0f,tween(340,easing=EaseInOutCubic),label="toolOpen"
    )
    val shape=RoundedCornerShape(mix(17f,27f,progress).dp)

    Column(
        Modifier.padding(horizontal=12.dp).clip(shape)
            .background(if(a.darkMode)Color(0xFF121715).copy(alpha=.94f) else Color.White.copy(alpha=.92f))
            .background(p.accent.copy(alpha=if(a.darkMode).04f else .022f))
            .border(
                mix(.45f,1f,progress).dp,
                p.accent.copy(alpha=mix(if(a.darkMode).14f else .22f,if(a.darkMode).46f else .52f,progress)),
                shape
            )
    ){
        Row(
            Modifier.fillMaxWidth().clickable(
                interactionSource=remember{MutableInteractionSource()},indication=null,onClick=onClick
            ).padding(13.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            Box(Modifier.size(42.dp),contentAlignment=Alignment.Center){
                val iconShape=RoundedCornerShape(mix(9f,20f,progress).dp)
                Box(
                    Modifier.size(mix(42f,39f,progress).dp).rotate(180f*progress)
                        .clip(iconShape).background(p.accent.copy(alpha=.68f))
                        .border(.65.dp,p.accentLight.copy(alpha=.54f),iconShape)
                )
                Canvas(Modifier.size(mix(36f,34f,progress).dp).rotate(-180f*progress)){
                    val sw=.62.dp.toPx()
                    drawRoundRect(
                        Color.White.copy(alpha=.48f),Offset(sw,sw),
                        Size(size.width-sw*2,size.height-sw*2),
                        CornerRadius(mix(7f,16f,progress).dp.toPx()),style=Stroke(sw)
                    )
                }
                NmixIcon(icon,Modifier.size(19.dp),Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)){
                Text(title,color=ui.text,fontSize=14.sp,fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily)
                Text(subtitle,color=ui.muted,fontSize=9.sp,fontFamily=a.fontFamily)
            }
            NmixIcon(NmixIcon.CHEVRON_DOWN,Modifier.size(18.dp).rotate(180f*progress),ui.muted)
        }

        AnimatedVisibility(
            visible=open,
            enter=expandVertically(tween(420,easing=EaseOutCubic),expandFrom=Alignment.Top)+fadeIn(tween(300)),
            exit=shrinkVertically(tween(320,easing=EaseInOutCubic),shrinkTowards=Alignment.Top)+fadeOut(tween(200))
        ){content()}
    }
}

@Composable
fun NmixOption(
    icon:NmixIcon,title:String,selected:Boolean=false,
    modifier:Modifier=Modifier,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if(pressed).965f else 1f,spring(dampingRatio=.72f,stiffness=620f),label="optionPress"
    )
    val selection by animateFloatAsState(if(selected)1f else 0f,tween(220),label="optionSelected")
    val shape=RoundedCornerShape(13.dp)

    Row(
        modifier.scale(scale).height(58.dp).clip(shape)
            .background(if(a.darkMode)Color(0xFF141917).copy(alpha=.91f) else Color.White.copy(alpha=.92f))
            .background(p.accent.copy(alpha=if(a.darkMode).035f+selection*.05f else .02f+selection*.035f))
            .border(
                mix(.45f,1f,selection).dp,
                p.accent.copy(alpha=if(a.darkMode).14f+selection*.32f else .22f+selection*.30f),
                shape
            )
            .clickable(interactionSource=interaction,indication=null,onClick=onClick)
            .padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Box(
            Modifier.size(35.dp)
                .clip(if(selected)CircleShape else RoundedCornerShape(9.dp))
                .background(p.accent.copy(alpha=if(selected).20f else .12f)),
            contentAlignment=Alignment.Center
        ){NmixIcon(icon,Modifier.size(18.dp),p.accent)}
        Spacer(Modifier.width(12.dp))
        Text(title,color=ui.text,fontSize=13.sp,fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily)
    }
}

@Composable
fun NmixCircleButton(
    icon:NmixIcon,modifier:Modifier=Modifier,color:Color?=null,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    NmixPressBox(modifier,CircleShape,color?:a.palette.accent.copy(alpha=.80f),onClick){
        NmixIcon(icon,Modifier.size(21.dp),Color.White)
    }
}

@Composable
fun NmixSmallIconButton(
    icon:NmixIcon,modifier:Modifier=Modifier,selected:Boolean=false,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(9.dp)
    Box(
        modifier.clip(shape)
            .background(if(a.darkMode)Color(0xFF151A18).copy(alpha=.91f) else Color.White.copy(alpha=.92f))
            .background(p.accent.copy(alpha=if(selected).09f else .025f))
            .border(
                if(selected)1.dp else .45.dp,
                p.accent.copy(alpha=if(selected).50f else if(a.darkMode).14f else .22f),
                shape
            )
    ){
        NmixPressBox(Modifier.fillMaxSize(),shape,Color.Transparent,onClick){
            NmixIcon(icon,Modifier.size(19.dp),p.accent)
        }
    }
}

@Composable
fun NmixTextButton(
    text:String,modifier:Modifier=Modifier,accent:Boolean=false,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(50)
    Box(
        modifier.clip(shape)
            .background(
                when{
                    accent->p.accent.copy(alpha=.80f)
                    a.darkMode->Color(0xFF141917).copy(alpha=.92f)
                    else->Color.White.copy(alpha=.92f)
                }
            )
            .background(if(accent)Color.Transparent else p.accent.copy(alpha=if(a.darkMode).035f else .02f))
            .border(.5.dp,p.accent.copy(alpha=if(accent).42f else if(a.darkMode).15f else .25f),shape)
    ){
        NmixPressBox(Modifier.fillMaxSize(),shape,Color.Transparent,onClick){
            Text(
                text,color=if(accent)Color.White else a.uiColors().text,
                fontSize=12.sp,fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily
            )
        }
    }
}

@Composable
fun NmixKey(
    text:String,modifier:Modifier=Modifier,type:Int=0,onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val bg=when(type){
        1->p.accent.copy(alpha=.86f)
        2->Color(0xFFD83939).copy(alpha=if(a.darkMode).19f else .14f)
        else->if(a.darkMode)p.accent.copy(alpha=.10f) else Color.White.copy(alpha=.92f)
    }
    val fg=when(type){
        1->Color.White
        2->Color(0xFFE15A5A)
        else->a.uiColors().text
    }

    Box(
        modifier.clip(CircleShape).background(bg)
            .border(
                .45.dp,
                when(type){
                    1->p.accentLight.copy(alpha=.28f)
                    2->Color(0xFFE15A5A).copy(alpha=.20f)
                    else->p.accent.copy(alpha=if(a.darkMode).14f else .21f)
                },
                CircleShape
            )
    ){
        NmixPressBox(Modifier.fillMaxSize(),CircleShape,Color.Transparent,onClick){
            Text(text,color=fg,fontSize=15.sp,fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily)
        }
    }
}

/* S=0, M=.27, L=.61, EL=1. Same progress as draggable header. */
@Composable
fun NmixDisplay(
    label:String,value:String,status:String,timer:Boolean,
    calcVisible:Boolean,calcFirst:String,calcOperator:String,calcSecond:String,
    displayProgress:Float=1f,
    onMinus:()->Unit,onPlus:()->Unit,onClick:()->Unit,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val interaction=remember{MutableInteractionSource()}
    val h=displayProgress.coerceIn(0f,1f)

    val sToM=(h/.27f).coerceIn(0f,1f)
    val mToL=((h-.27f)/.34f).coerceIn(0f,1f)
    val lToEl=((h-.61f)/.39f).coerceIn(0f,1f)

    val pill=((.13f-h)/.13f).coerceIn(0f,1f)
    val displayShape=RoundedCornerShape(mix(19f,54f,pill).dp)
    val calcAlpha by animateFloatAsState(
        if(calcVisible)1f else 0f,tween(330,easing=EaseInOutCubic),label="calcVisible"
    )
    val world=rememberNmixWorldMotion("mainDisplayWorld")

    Box(
        modifier.clip(displayShape)
            .background(screenColor())
            .border(.55.dp,screenBorder(),displayShape)
            .clickable(interactionSource=interaction,indication=null,onClick=onClick)
    ){
        if(a.animationEnabled)GiantWorldLayer(world)

        if(calcVisible){
            CalculatorMorph(
                first=calcFirst.ifEmpty{"_"},
                operator=calcOperator.ifEmpty{"sign"},
                second=calcSecond.ifEmpty{"_"},
                sToM=sToM,mToL=mToL,lToEl=lToEl,
                modifier=Modifier.fillMaxSize().graphicsLayer{alpha=calcAlpha}
            )
        }

        /*
         * S/M: content remains right of the left field group.
         * M->L: fields travel to top and content continuously returns center.
         */
        if(calcVisible&&mToL<.92f){
            val enterCenter=
                ((mToL-.10f)/.82f)
                    .coerceIn(0f,1f)
        
            /*
             * S/M calculator fields own the left side.
             * Main value owns the right side.
             * Label/status disappear at the tightest height so
             * "Enter your number" cannot collide with fields/value.
             */
            val detailsAlpha=
                ((h-.10f)/.20f)
                    .coerceIn(0f,1f)
        
            Column(
                Modifier
                    .fillMaxWidth(
                        mix(.52f,.78f,enterCenter)
                    )
                    .align(Alignment.CenterEnd)
                    .padding(
                        start=8.dp,
                        end=10.dp
                    )
                    .graphicsLayer{
                        translationY=
                            -4f*enterCenter
                    },
                horizontalAlignment=
                    Alignment.CenterHorizontally,
                verticalArrangement=
                    Arrangement.Center
            ){
                Text(
                    label,
                    Modifier.graphicsLayer{
                        alpha=detailsAlpha
                    },
                    color=
                        if(a.darkMode)
                            p.accentLight
                        else
                            p.accentDark,
                    fontSize=
                        mix(
                            6.8f,
                            8.7f,
                            enterCenter
                        ).sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=1.2.sp,
                    fontFamily=a.fontFamily,
                    maxLines=1
                )
        
                if(detailsAlpha>.12f){
                    Spacer(Modifier.height(2.dp))
                }
        
                Text(
                    value,
                    color=displayText(),
                    fontSize=
                        mix(
                            22f,
                            34f,
                            enterCenter
                        ).sp,
                    fontWeight=FontWeight.SemiBold,
                    fontFamily=a.fontFamily,
                    maxLines=1,
                    textAlign=TextAlign.Center
                )
        
                if(detailsAlpha>.12f){
                    Spacer(Modifier.height(2.dp))
                }
        
                Text(
                    status,
                        Modifier.graphicsLayer{
                            alpha=detailsAlpha
                        },
                        color=
                            if(a.darkMode)
                                p.accentLight.copy(alpha=.72f)
                            else
                                p.accentDark.copy(alpha=.74f),
                        fontSize=
                            mix(
                                6.3f,
                                8.5f,
                                enterCenter
                            ).sp,
                        lineHeight=9.sp,
                        fontWeight=FontWeight.Medium,
                        fontFamily=a.fontFamily,
                        textAlign=TextAlign.Center,
                        maxLines=1
                    )
                }
            }else{
            val topPadding=if(calcVisible)mix(72f,76f,lToEl).dp else 17.dp

            Text(
                label,
                Modifier.align(Alignment.TopCenter).padding(top=topPadding)
                    .graphicsLayer{translationY=if(calcVisible)6f*(1f-mToL) else 0f},
                color=if(a.darkMode)p.accentLight else p.accentDark,
                fontSize=mix(8.5f,9f,lToEl).sp,fontWeight=FontWeight.Bold,
                letterSpacing=2.sp,fontFamily=a.fontFamily,maxLines=1
            )

            Text(
                value,
                Modifier.align(Alignment.Center)
                    .padding(horizontal=if(timer)70.dp else 16.dp)
                    .graphicsLayer{translationY=if(calcVisible)mix(9f,16f,lToEl) else 0f},
                color=displayText(),fontSize=mix(36f,40f,lToEl).sp,
                fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily,maxLines=1
            )

            Text(
                status,
                Modifier.align(Alignment.BottomCenter).padding(horizontal=18.dp,vertical=15.dp),
                color=if(a.darkMode)p.accentLight.copy(alpha=.78f) else p.accentDark.copy(alpha=.80f),
                fontSize=mix(10f,11f,lToEl).sp,lineHeight=15.sp,
                fontWeight=FontWeight.Medium,fontFamily=a.fontFamily,
                textAlign=TextAlign.Center,maxLines=2
            )
        }

        val timerSize=mix(34f,47f,h).dp
        AnimatedVisibility(
            timer,
            Modifier.align(Alignment.CenterStart).padding(start=if(h<.15f)8.dp else 13.dp),
            enter=fadeIn(tween(220))+scaleIn(),exit=fadeOut(tween(170))+scaleOut()
        ){NmixCircleButton(NmixIcon.MINUS,Modifier.size(timerSize),onClick=onMinus)}

        AnimatedVisibility(
            timer,
            Modifier.align(Alignment.CenterEnd).padding(end=if(h<.15f)8.dp else 13.dp),
            enter=fadeIn(tween(220))+scaleIn(),exit=fadeOut(tween(170))+scaleOut()
        ){NmixCircleButton(NmixIcon.PLUS,Modifier.size(timerSize),onClick=onPlus)}
    }
}

@Composable
private fun BoxScope.GiantWorldLayer(world:NmixWorldMotion){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val soft=a.animation!=NmixAnimationName.FLOAT

    world.bodies.forEachIndexed{index,body->
        val x=body.x*(650f+index*55f)
        val y=body.y*(475f+index*42f)

        if(soft){
            Box(
                Modifier
                    .size(when(index){0->1080.dp;1->950.dp;2->830.dp;3->890.dp;else->740.dp})
                    .align(Alignment.Center)
                    .graphicsLayer{
                        translationX=x;translationY=y
                        scaleX=body.pulse;scaleY=body.pulse
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops=arrayOf(
                                0f to (if(index%2==0)p.accent else p.accentLight)
                                    .copy(alpha=if(a.darkMode).28f else .20f),
                                .18f to p.accent.copy(alpha=if(a.darkMode).23f else .17f),
                                .38f to p.accent.copy(alpha=.12f),
                                .58f to p.accent.copy(alpha=.055f),
                                .75f to p.accent.copy(alpha=.018f),
                                .90f to p.accent.copy(alpha=.003f),
                                1f to Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }else{
            val elementSize=when(index){0->520.dp;1->455.dp;2->395.dp;3->425.dp;else->355.dp}
            Canvas(
                Modifier.size(elementSize).align(Alignment.Center)
                    .graphicsLayer{
                        translationX=x;translationY=y
                        rotationZ=body.rotation
                        scaleX=body.pulse;scaleY=body.pulse
                    }
            ){
                val c=if(index%2==0)p.accent else p.accentLight
                val inset=16.dp.toPx()
                drawRoundRect(c.copy(alpha=.024f),cornerRadius=CornerRadius(58.dp.toPx()))
                drawRoundRect(
                    c.copy(alpha=if(a.darkMode).17f else .13f),
                    Offset(inset,inset),
                    Size((size.width-inset*2).coerceAtLeast(0f),(size.height-inset*2).coerceAtLeast(0f)),
                    CornerRadius(46.dp.toPx())
                )
                drawRoundRect(
                    c.copy(alpha=.13f),Offset(inset,inset),
                    Size((size.width-inset*2).coerceAtLeast(0f),(size.height-inset*2).coerceAtLeast(0f)),
                    CornerRadius(46.dp.toPx()),style=Stroke(1.6.dp.toPx())
                )
            }
        }
    }
}

/* Same three Layout children are moved throughout S -> M -> L -> EL. */
@Composable
private fun CalculatorMorph(
    first:String,operator:String,second:String,
    sToM:Float,mToL:Float,lToEl:Float,
    modifier:Modifier=Modifier
){
    val density=LocalDensity.current

    fun textScale(text:String)=when{
        text.length>=15->.55f
        text.length>=12->.63f
        text.length>=9->.71f
        text.length>=7->.79f
        text.length>=5->.88f
        else->1f
    }

    val textBase=mix(mix(8.8f,10.5f,sToM),mix(13.2f,14f,lToEl),mToL)

    Layout(
        modifier=modifier,
        content={
            MorphField(first,(textBase*textScale(first)).coerceAtLeast(6.6f).sp,0,sToM,mToL)
            MorphField(operator,(textBase*.80f).coerceAtLeast(6.2f).sp,1,sToM,mToL)
            MorphField(second,(textBase*textScale(second)).coerceAtLeast(6.6f).sp,2,sToM,mToL)
        }
    ){measurables,constraints->
        val w=constraints.maxWidth
        val h=constraints.maxHeight
        fun px(v:Float)=with(density){v.dp.toPx()}

        /* S: left vertical. */
        /*
         * S + M are the same horizontal-left family.
         * S is compact; M grows smoothly without changing topology.
         */
        val sLeft=px(5f)
        val sGap=px(2f)
        val sTotal=w*.45f
        val sSignW=sTotal*.22f
        val sNumW=(sTotal-sSignW-sGap*2)/2f
        val sHeight=(h-px(10f))
            .coerceAtLeast(px(25f))
        
        val sx=floatArrayOf(
            sLeft,
            sLeft+sNumW+sGap,
            sLeft+sNumW+sGap+sSignW+sGap
        )
        
        val sy=floatArrayOf(
            (h-sHeight)/2f,
            (h-sHeight)/2f,
            (h-sHeight)/2f
        )
        
        val sw=floatArrayOf(
            sNumW,
            sSignW,
            sNumW
        )
        
        val sh=floatArrayOf(
            sHeight,
            sHeight,
            sHeight
        )
        
        val mLeft=px(5f)
        val mGap=px(2.5f)
        val mTotal=w*.47f
        val mSignW=mTotal*.23f
        val mNumW=(mTotal-mSignW-mGap*2)/2f
        val mHeight=(h-px(10f))
            .coerceAtLeast(px(25f))
        
        val mx=floatArrayOf(
            mLeft,
            mLeft+mNumW+mGap,
            mLeft+mNumW+mGap+mSignW+mGap
        )
        
        val my=floatArrayOf(
            (h-mHeight)/2f,
            (h-mHeight)/2f,
            (h-mHeight)/2f
        )
        
        val mw=floatArrayOf(
            mNumW,
            mSignW,
            mNumW
        )
        
        val mh=floatArrayOf(
            mHeight,
            mHeight,
            mHeight
        )

        /* L: full horizontal row at top. */
        val lOuter=px(12f)
        val lGap=px(7f)
        val lTop=px(12f)
        val lHeight=px(45f)
        val lSignW=px(58f)
        val lNumW=((w-lOuter*2-lGap*2-lSignW)/2f).coerceAtLeast(px(20f))
        val lx=floatArrayOf(
            lOuter,
            lOuter+lNumW+lGap,
            lOuter+lNumW+lGap+lSignW+lGap
        )
        val ly=floatArrayOf(lTop,lTop,lTop)
        val lw=floatArrayOf(lNumW,lSignW,lNumW)
        val lh=floatArrayOf(lHeight,lHeight,lHeight)

        /* EL: same top structure, only subtle size increase. */
        val eOuter=px(12f)
        val eGap=px(7f)
        val eTop=px(12f)
        val eHeight=px(46f)
        val eSignW=px(58f)
        val eNumW=((w-eOuter*2-eGap*2-eSignW)/2f).coerceAtLeast(px(20f))
        val ex=floatArrayOf(
            eOuter,
            eOuter+eNumW+eGap,
            eOuter+eNumW+eGap+eSignW+eGap
        )
        val ey=floatArrayOf(eTop,eTop,eTop)
        val ew=floatArrayOf(eNumW,eSignW,eNumW)
        val eh=floatArrayOf(eHeight,eHeight,eHeight)

        val fx=FloatArray(3)
        val fy=FloatArray(3)
        val fw=FloatArray(3)
        val fh=FloatArray(3)

        repeat(3){i->
            val smX=mix(sx[i],mx[i],sToM)
            val smY=mix(sy[i],my[i],sToM)
            val smW=mix(sw[i],mw[i],sToM)
            val smH=mix(sh[i],mh[i],sToM)

            val mlX=mix(smX,lx[i],mToL)
            val mlY=mix(smY,ly[i],mToL)
            val mlW=mix(smW,lw[i],mToL)
            val mlH=mix(smH,lh[i],mToL)

            fx[i]=mix(mlX,ex[i],lToEl)
            fy[i]=mix(mlY,ey[i],lToEl)
            fw[i]=mix(mlW,ew[i],lToEl)
            fh[i]=mix(mlH,eh[i],lToEl)
        }

        val placeables=measurables.mapIndexed{i,m->
            m.measure(
                Constraints.fixed(
                    fw[i].toInt().coerceAtLeast(1).coerceAtMost(w.coerceAtLeast(1)),
                    fh[i].toInt().coerceAtLeast(1).coerceAtMost(h.coerceAtLeast(1))
                )
            )
        }

        layout(w,h){
            placeables.forEachIndexed{i,child->
                child.placeRelative(
                    fx[i].toInt().coerceIn(0,(w-child.width).coerceAtLeast(0)),
                    fy[i].toInt().coerceIn(0,(h-child.height).coerceAtLeast(0))
                )
            }
        }
    }
}

@Composable
private fun MorphField(
    text:String,textSize:TextUnit,kind:Int,sToM:Float,mToL:Float
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val r=11f

    val shape=when(kind){
    0->{
        /*
         * S/M: strong left-side rounding.
         * L/EL: returns smoothly to top-left-only.
         */
        val leftRadius=
            mix(24f,28f,sToM)

        RoundedCornerShape(
            topStart=
                mix(leftRadius,r,mToL).dp,
            topEnd=0.dp,
            bottomEnd=0.dp,
            bottomStart=
                mix(leftRadius,0f,mToL).dp
        )
    }

    1->RoundedCornerShape(0.dp)

    else->{
        /*
         * S/M: square right field.
         * L/EL: top-right corner appears smoothly.
         */
        RoundedCornerShape(
            topStart=0.dp,
            topEnd=(r*mToL).dp,
            bottomEnd=0.dp,
            bottomStart=0.dp
        )
    }
}

    Box(
        Modifier.fillMaxSize().clip(shape)
            .background(if(a.darkMode)Color(0xFF151B18).copy(alpha=.74f) else Color.White.copy(alpha=.73f))
            .background(p.accent.copy(alpha=if(a.darkMode).045f else .027f))
            .border(.5.dp,screenBorder(),shape),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,color=displayText().copy(alpha=.87f),fontSize=textSize,
            fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily,
            maxLines=1,textAlign=TextAlign.Center
        )
    }
}

@Composable
fun NmixCalcField(
    text:String,modifier:Modifier=Modifier,
    shape:Shape=RoundedCornerShape(11.dp),
    height:Dp?=46.dp,textSize:TextUnit=15.sp
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    Box(
        modifier.then(if(height!=null)Modifier.height(height) else Modifier)
            .clip(shape)
            .background(if(a.darkMode)Color(0xFF151B18).copy(alpha=.74f) else Color.White.copy(alpha=.73f))
            .background(p.accent.copy(alpha=if(a.darkMode).045f else .027f))
            .border(.5.dp,screenBorder(),shape),
        contentAlignment=Alignment.Center
    ){
        Text(
            text,color=displayText().copy(alpha=.87f),fontSize=textSize,
            fontWeight=FontWeight.SemiBold,fontFamily=a.fontFamily,
            maxLines=1,textAlign=TextAlign.Center
        )
    }
}

@Composable
fun NmixGlassBox(
    modifier:Modifier=Modifier,accentTint:Boolean=true,
    content:@Composable BoxScope.()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val shape=RoundedCornerShape(13.dp)
    Box(
        modifier.clip(shape)
            .background(if(a.darkMode)Color(0xFF141917).copy(alpha=.92f) else Color.White.copy(alpha=.92f))
            .background(
                if(accentTint)p.accent.copy(alpha=if(a.darkMode).04f else .022f)
                else Color.Transparent
            )
            .border(.45.dp,p.accent.copy(alpha=if(a.darkMode).14f else .22f),shape),
        content=content
    )
}

@Composable
fun NmixPressBox(
    modifier:Modifier,shape:Shape,color:Color,
    onClick:()->Unit,content:@Composable ()->Unit
){
    val haptic=rememberNmixHapticAction()
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if(pressed).95f else 1f,
        spring(dampingRatio=.72f,stiffness=620f),
        label="press"
    )

    Box(
        modifier.scale(scale).clip(shape).background(color)
            .clickable(interactionSource=interaction,indication=null){haptic(onClick)},
        contentAlignment=Alignment.Center
    ){content()}
}
