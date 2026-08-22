package com.lxzrvi.nmix

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

    val interaction=remember{
        MutableInteractionSource()
    }

    val shape=
        RoundedCornerShape(14.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(
                if(a.darkMode)
                    Color(0xFF151A18)
                        .copy(alpha=.82f)
                else
                    Color(0xFFE8ECEA)
                        .copy(alpha=.88f)
            )
            .background(
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .045f
                        else
                            .035f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.usingCustomColor)
                            .72f
                        else if(a.darkMode)
                            .18f
                        else
                            .25f
                ),
                shape
            )
            .clickable(
                interactionSource=interaction,
                indication=null,
                onClick=onClick
            )
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
            if(a.usingCustomColor)
                "ACTIVE"
            else
                "OPEN",
            color=p.accent,
            fontSize=7.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.8.sp,
            fontFamily=a.fontFamily
        )
    }
}
