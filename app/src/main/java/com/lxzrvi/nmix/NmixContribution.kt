package com.lxzrvi.nmix

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun NmixContribution(modifier:Modifier=Modifier){
    val a=LocalNmixAppearance.current
    val p=a.palette
    val ui=a.uiColors()

    val stories=remember{
        listOf(
            "NMIX started as an idea to bring useful number tools together in one focused place.",
            "The project evolved from a web reference into a fully native Android app built with Kotlin and Jetpack Compose.",
            "NMIX focuses on useful offline tools, fluid interaction and one idea — EVERYTHING WITH NUMBERS.",
            "Calculator, time tools, counters, themes and the interface were shaped into one consistent native experience."
        )
    }

    var story by remember{mutableIntStateOf(0)}

    LaunchedEffect(Unit){
        while(true){
            delay(4200)
            story=(story+1)%stories.size
        }
    }

    val outer=RoundedCornerShape(20.dp)
    val inner=RoundedCornerShape(14.dp)

    Column(
        modifier
            .padding(horizontal=12.dp)
            .clip(outer)
            .background(
                if(a.darkMode)
                    Color(0xFF121715).copy(alpha=.94f)
                else Color.White.copy(alpha=.93f)
            )
            .background(
                p.accent.copy(
                    alpha=if(a.darkMode).045f else .025f
                )
            )
            .border(
                .5.dp,
                p.accent.copy(
                    alpha=if(a.darkMode).18f else .28f
                ),
                outer
            )
            .padding(14.dp)
    ){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically
        ){
            Column(Modifier.weight(1f)){
                Text(
                    "CONTRIBUTION",
                    color=p.accent,
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=1.3.sp,
                    fontFamily=a.fontFamily
                )
                Spacer(Modifier.height(2.dp))
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
                    .clip(RoundedCornerShape(50))
                    .background(
                        p.accent.copy(
                            alpha=if(a.darkMode).15f else .10f
                        )
                    )
                    .border(
                        .4.dp,
                        p.accent.copy(alpha=.20f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal=10.dp,vertical=6.dp)
            ){
                Text(
                    "CONTRIBUTOR",
                    color=p.accent,
                    fontSize=8.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=.6.sp,
                    fontFamily=a.fontFamily
                )
            }
        }

        Spacer(Modifier.height(11.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(9.dp)
        ){
            Box(
                Modifier
                    .weight(1.15f)
                    .height(145.dp)
                    .clip(inner)
                    .background(
                        p.accent.copy(
                            alpha=if(a.darkMode).075f else .055f
                        )
                    )
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=if(a.darkMode).18f else .25f
                        ),
                        inner
                    )
                    .padding(12.dp),
                contentAlignment=Alignment.CenterStart
            ){
                AnimatedContent(
                    targetState=story,
                    transitionSpec={
                        (
                            fadeIn(
                                tween(
                                    320,
                                    easing=EaseOutCubic
                                )
                            )+
                            slideInVertically(
                                initialOffsetY={it/10},
                                animationSpec=tween(
                                    320,
                                    easing=EaseOutCubic
                                )
                            )
                        ) togetherWith (
                            fadeOut(
                                tween(210)
                            )+
                            slideOutVertically(
                                targetOffsetY={-it/10},
                                animationSpec=tween(
                                    260,
                                    easing=EaseInCubic
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
                            fontWeight=FontWeight.Bold,
                            letterSpacing=.7.sp,
                            fontFamily=a.fontFamily
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            stories[index],
                            color=ui.text.copy(alpha=.84f),
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
                    .height(145.dp)
                    .clip(inner)
                    .background(
                        p.accent.copy(
                            alpha=if(a.darkMode).075f else .055f
                        )
                    )
                    .border(
                        .45.dp,
                        p.accent.copy(
                            alpha=if(a.darkMode).18f else .25f
                        ),
                        inner
                    )
                    .padding(10.dp)
            ){
                Text(
                    "BUILT WITH",
                    color=p.accent,
                    fontSize=7.5.sp,
                    fontWeight=FontWeight.Bold,
                    letterSpacing=.5.sp,
                    fontFamily=a.fontFamily
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ){
                    SkillRow("GitHub","Kotlin")
                    Spacer(Modifier.height(5.dp))
                    SkillPill(
                        "Compose",
                        Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(5.dp))
                    SkillPill(
                        "Android SDK",
                        Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(5.dp))
                    SkillRow("Gradle","Actions")
                }
            }
        }

        Spacer(Modifier.height(13.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.Center,
            verticalAlignment=Alignment.CenterVertically
        ){
            Text(
                "NMIX",
                color=ui.text.copy(alpha=.82f),
                fontSize=11.sp,
                fontWeight=FontWeight.Bold,
                fontFamily=NmixLogoFont
            )

            Text(
                "  •  lxzrvi  •  © 2026",
                color=ui.muted,
                fontSize=10.sp,
                fontFamily=a.fontFamily
            )
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
    val shape=RoundedCornerShape(50)

    Box(
        modifier
            .height(21.dp)
            .clip(shape)
            .background(
                p.accent.copy(
                    alpha=if(a.darkMode).17f else .11f
                )
            )
            .border(
                .35.dp,
                p.accent.copy(alpha=.18f),
                shape
            ),
        contentAlignment=Alignment.Center
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
