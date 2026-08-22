package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NmixCustomThemeButton(
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    var open by remember{
        mutableStateOf(false)
    }

    val radius by animateDpAsState(
        targetValue=
            if(open)
                16.dp
            else
                14.dp,
        animationSpec=tween(
            320,
            easing=EaseInOutCubic
        ),
        label="customRadius"
    )

    val shape=
        RoundedCornerShape(radius)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.84f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.90f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .05f
                        else
                            .04f
                )
            )
            /*
             * No visible border normally.
             * Accent outline appears only while
             * Custom is the active color source.
             */
            .then(
                if(a.usingCustomColor){
                    Modifier.border(
                        1.dp,
                        p.accent.copy(
                            alpha=.82f
                        ),
                        shape
                    )
                }else{
                    Modifier
                }
            )
            .animateContentSize(
                animationSpec=tween(
                    340,
                    easing=EaseInOutCubic
                )
            )
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(
                    interactionSource=remember{
                        MutableInteractionSource()
                    },
                    indication=null
                ){
                    open=!open
                }
                .padding(horizontal=12.dp),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(p.accent)
            )

            Spacer(
                Modifier.width(9.dp)
            )

            Column(
                Modifier.weight(1f)
            ){
                Text(
                    "Custom",
                    color=ui.text,
                    fontSize=10.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=a.fontFamily
                )

                Text(
                    if(a.usingCustomColor)
                        "Custom color active"
                    else
                        "Create your own NMIX color",
                    color=ui.muted,
                    fontSize=7.5.sp,
                    fontFamily=a.fontFamily
                )
            }

            Text(
                if(open)
                    "CLOSE"
                else
                    "OPEN",
                color=p.accent,
                fontSize=7.sp,
                fontWeight=FontWeight.Bold,
                letterSpacing=.8.sp,
                fontFamily=a.fontFamily
            )
        }

        AnimatedVisibility(
            visible=open,
            enter=
                expandVertically(
                    animationSpec=tween(
                        320,
                        easing=EaseOutCubic
                    ),
                    expandFrom=
                        Alignment.Top
                )+
                fadeIn(
                    tween(220)
                ),
            exit=
                shrinkVertically(
                    animationSpec=tween(
                        280,
                        easing=EaseInOutCubic
                    ),
                    shrinkTowards=
                        Alignment.Top
                )+
                fadeOut(
                    tween(170)
                )
        ){
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start=10.dp,
                        end=10.dp,
                        bottom=10.dp
                    )
            ){
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(.5.dp)
                        .background(
                            p.accent.copy(
                                alpha=.14f
                            )
                        )
                )

                Spacer(
                    Modifier.height(9.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement=
                        Arrangement.spacedBy(8.dp)
                ){
                    CustomAction(
                        text="EDIT COLOR",
                        modifier=
                            Modifier.weight(1f),
                        accent=true,
                        onClick=onClick
                    )

                    CustomAction(
                        text="RESET",
                        modifier=
                            Modifier.weight(1f),
                        accent=false
                    ){
                        a.setTheme(a.theme)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomAction(
    text:String,
    modifier:Modifier,
    accent:Boolean,
    onClick:()->Unit
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val shape=
        RoundedCornerShape(11.dp)

    Box(
        modifier
            .height(38.dp)
            .clip(shape)
            .background(
                if(accent){
                    p.accent.copy(
                        alpha=.78f
                    )
                }else if(a.darkMode){
                    Color.White.copy(
                        alpha=.045f
                    )
                }else{
                    Color.White.copy(
                        alpha=.50f
                    )
                }
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(accent)
                            .48f
                        else
                            .16f
                ),
                shape
            )
            .clickable(
                interactionSource=remember{
                    MutableInteractionSource()
                },
                indication=null,
                onClick=onClick
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=
                if(accent)
                    Color.White
                else
                    ui.text,
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.5.sp,
            fontFamily=a.fontFamily
        )
    }
}
