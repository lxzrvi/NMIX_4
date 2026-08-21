package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@Composable
fun NmixContribution(
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val stories=remember{
        listOf(
            "NMIX started as an idea to bring useful number tools together in one focused place.",
            "Designed and developed by lxzrvi using a phone-first workflow with GitHub and Codespaces.",
            "The project moved from a web reference into a fully native Android experience built with Kotlin and Jetpack Compose.",
            "NMIX is designed around offline tools, fluid interaction and one simple idea — EVERYTHING WITH NUMBERS.",
            "From interface design and themes to calculator, timer, clock, stopwatch and counters, the app was shaped as one consistent experience."
        )
    }

    var story by remember{
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit){
        while(true){
            delay(4300)
            story=(story+1)%stories.size
        }
    }

    val outerShape=
        RoundedCornerShape(20.dp)

    val innerShape=
        RoundedCornerShape(14.dp)

    val outerBg=
        if(a.darkMode)
            p.accent.copy(alpha=.07f)
        else
            Color.White.copy(alpha=.74f)

    val innerBg=
        if(a.darkMode)
            Color.Black.copy(alpha=.20f)
        else
            p.accent.copy(alpha=.055f)

    Column(
        modifier
            .padding(horizontal=12.dp)
            .clip(outerShape)
            .background(outerBg)
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=
                        if(a.darkMode)
                            .13f
                        else
                            .23f
                ),
                outerShape
            )
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=
                Alignment.CenterVertically
        ){
            Column(
                Modifier.weight(1f)
            ){
                Text(
                    "CONTRIBUTION",
                    color=p.accent,
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=1.3.sp,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(2.dp)
                )

                Text(
                    "lxzrvi",
                    color=ui.text,
                    fontSize=22.sp,
                    fontWeight=FontWeight.Bold,
                    fontFamily=NmixLogoFont
                )
            }

            Box(
                Modifier
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        p.accent.copy(
                            alpha=.12f
                        )
                    )
                    .padding(
                        horizontal=11.dp,
                        vertical=6.dp
                    )
            ){
                Text(
                    "CONTRIBUTOR",
                    color=p.accent,
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=.7.sp,
                    fontFamily=a.fontFamily
                )
            }
        }

        Spacer(
            Modifier.height(12.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=
                Arrangement.spacedBy(9.dp)
        ){
            Box(
                Modifier
                    .weight(1.15f)
                    .height(150.dp)
                    .clip(innerShape)
                    .background(innerBg)
                    .border(
                        .4.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .10f
                                else
                                    .18f
                        ),
                        innerShape
                    )
                    .padding(12.dp),
                contentAlignment=
                    Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=story,
                    transitionSpec={
                        (
                            fadeIn(
                                tween(
                                    380,
                                    easing=
                                        EaseOutCubic
                                )
                            )+
                            slideInVertically(
                                initialOffsetY={
                                    it/8
                                },
                                animationSpec=
                                    tween(
                                        380,
                                        easing=
                                            EaseOutCubic
                                    )
                            )
                        ) togetherWith (
                            fadeOut(
                                tween(250)
                            )+
                            slideOutVertically(
                                targetOffsetY={
                                    -it/8
                                },
                                animationSpec=
                                    tween(
                                        300,
                                        easing=
                                            EaseInCubic
                                    )
                            )
                        )
                    },
                    label="contributionStory"
                ){index->
                    Column{
                        Text(
                            "ABOUT THE PROJECT",
                            color=p.accent,
                            fontSize=8.sp,
                            fontWeight=
                                FontWeight.Bold,
                            letterSpacing=.8.sp,
                            fontFamily=a.fontFamily
                        )

                        Spacer(
                            Modifier.height(7.dp)
                        )

                        Text(
                            stories[index],
                            color=ui.text.copy(
                                alpha=.82f
                            ),
                            fontSize=9.sp,
                            lineHeight=14.sp,
                            fontFamily=a.fontFamily
                        )
                    }
                }
            }

            Column(
                Modifier
                    .weight(.85f)
                    .height(150.dp)
                    .clip(innerShape)
                    .background(innerBg)
                    .border(
                        .4.dp,
                        p.accent.copy(
                            alpha=
                                if(a.darkMode)
                                    .10f
                                else
                                    .18f
                        ),
                        innerShape
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT / WORKED WITH",
                    color=p.accent,
                    fontSize=7.5.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=.5.sp,
                    fontFamily=a.fontFamily
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                SkillRow(
                    "GitHub",
                    "Kotlin"
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                SkillRow(
                    "HTML",
                    "CSS"
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                SkillRow(
                    "JavaScript",
                    "Compose"
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                SkillPill(
                    "Android SDK",
                    Modifier.fillMaxWidth()
                )

                Spacer(
                    Modifier.height(5.dp)
                )

                SkillRow(
                    "Gradle",
                    "Actions"
                )
            }
        }
    }
}

@Composable
private fun SkillRow(
    left:String,
    right:String
){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement=
            Arrangement.spacedBy(5.dp)
    ){
        SkillPill(
            left,
            Modifier.weight(1f)
        )

        SkillPill(
            right,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun SkillPill(
    text:String,
    modifier:Modifier=Modifier
){
    val a=LocalNmixAppearance.current
    val p=a.palette

    Box(
        modifier
            .height(21.dp)
            .clip(
                RoundedCornerShape(50)
            )
            .background(
                p.accent.copy(alpha=.12f)
            ),
        contentAlignment=
            Alignment.Center
    ){
        Text(
            text,
            color=p.accent,
            fontSize=6.5.sp,
            fontWeight=FontWeight.SemiBold,
            fontFamily=a.fontFamily,
            maxLines=1
        )
    }
}
