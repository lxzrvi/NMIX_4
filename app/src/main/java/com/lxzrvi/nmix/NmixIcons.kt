package com.lxzrvi.nmix

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

enum class NmixIcon{
    CALCULATOR,CLOCK,COUNTER,HELP,TIMER,STOPWATCH,
    ARROW_UP,ARROW_DOWN,CHEVRON_DOWN,MENU,CLOSE,
    FULLSCREEN,BACK,PLUS,MINUS,RESET,RANDOM,
    DECIMAL,PLUS_MINUS,BACKSPACE,CHECK,
    ROTATE,WALLPAPER
}

@Composable
fun NmixIcon(
    icon:NmixIcon,
    modifier:Modifier=Modifier,
    color:Color=Color.White
){
    Canvas(modifier){
        val w=size.width
        val h=size.height
        val u=size.minDimension
        val sw=u*.075f

        val stroke=Stroke(
            width=sw,
            cap=StrokeCap.Round,
            join=StrokeJoin.Round
        )

        fun p(x:Float,y:Float)=Offset(w*x,h*y)

        fun line(
            x1:Float,y1:Float,
            x2:Float,y2:Float
        ){
            drawLine(
                color,
                p(x1,y1),
                p(x2,y2),
                sw,
                StrokeCap.Round
            )
        }

        fun dot(x:Float,y:Float,r:Float){
            drawCircle(
                color,
                u*r,
                p(x,y)
            )
        }

        fun ring(
            x:Float,
            y:Float,
            r:Float
        ){
            drawCircle(
                color,
                u*r,
                p(x,y),
                style=stroke
            )
        }

        when(icon){
            NmixIcon.CALCULATOR->{
                dot(.5f,.23f,.045f)
                line(.24f,.5f,.76f,.5f)
                dot(.5f,.77f,.045f)
            }

            NmixIcon.CLOCK->{
                ring(.5f,.5f,.34f)
                line(.5f,.5f,.5f,.30f)
                line(.5f,.5f,.66f,.59f)
            }

            NmixIcon.COUNTER,
            NmixIcon.PLUS->{
                line(.5f,.22f,.5f,.78f)
                line(.22f,.5f,.78f,.5f)
            }

            NmixIcon.MINUS->{
                line(.22f,.5f,.78f,.5f)
            }

            NmixIcon.HELP->{
                val path=Path().apply{
                    moveTo(w*.31f,h*.31f)
                    cubicTo(
                        w*.35f,h*.16f,
                        w*.48f,h*.12f,
                        w*.59f,h*.16f
                    )
                    cubicTo(
                        w*.73f,h*.21f,
                        w*.75f,h*.36f,
                        w*.69f,h*.46f
                    )
                    cubicTo(
                        w*.64f,h*.54f,
                        w*.51f,h*.55f,
                        w*.51f,h*.67f
                    )
                }
                drawPath(path,color,style=stroke)
                dot(.51f,.82f,.045f)
            }

            NmixIcon.TIMER->{
                ring(.5f,.56f,.30f)
                line(.41f,.17f,.59f,.17f)
                line(.5f,.17f,.5f,.25f)
                line(.70f,.31f,.77f,.38f)
                line(.5f,.56f,.5f,.39f)
                line(.5f,.56f,.63f,.47f)
            }

            NmixIcon.STOPWATCH->{
                ring(.5f,.56f,.30f)
                line(.41f,.17f,.59f,.17f)
                line(.5f,.17f,.5f,.25f)
                line(.70f,.31f,.77f,.38f)
                line(.5f,.56f,.5f,.38f)
                dot(.5f,.56f,.035f)
            }

            NmixIcon.ARROW_UP->{
                line(.5f,.77f,.5f,.25f)
                line(.5f,.25f,.31f,.44f)
                line(.5f,.25f,.69f,.44f)
            }

            NmixIcon.ARROW_DOWN->{
                line(.5f,.23f,.5f,.75f)
                line(.5f,.75f,.31f,.56f)
                line(.5f,.75f,.69f,.56f)
            }

            NmixIcon.CHEVRON_DOWN->{
                line(.28f,.39f,.5f,.61f)
                line(.5f,.61f,.72f,.39f)
            }

            NmixIcon.MENU->{
                line(.22f,.30f,.78f,.30f)
                line(.22f,.50f,.78f,.50f)
                line(.22f,.70f,.78f,.70f)
            }

            NmixIcon.CLOSE->{
                line(.29f,.29f,.71f,.71f)
                line(.71f,.29f,.29f,.71f)
            }

            NmixIcon.FULLSCREEN->{
                line(.20f,.39f,.20f,.20f)
                line(.20f,.20f,.39f,.20f)

                line(.61f,.20f,.80f,.20f)
                line(.80f,.20f,.80f,.39f)

                line(.20f,.61f,.20f,.80f)
                line(.20f,.80f,.39f,.80f)

                line(.61f,.80f,.80f,.80f)
                line(.80f,.80f,.80f,.61f)
            }

            NmixIcon.BACK->{
                line(.77f,.5f,.25f,.5f)
                line(.25f,.5f,.43f,.32f)
                line(.25f,.5f,.43f,.68f)
            }

            NmixIcon.RESET->{
                val path=Path().apply{
                    moveTo(w*.72f,h*.34f)

                    cubicTo(
                        w*.61f,h*.20f,
                        w*.41f,h*.17f,
                        w*.28f,h*.29f
                    )

                    cubicTo(
                        w*.12f,h*.43f,
                        w*.18f,h*.70f,
                        w*.36f,h*.79f
                    )

                    cubicTo(
                        w*.50f,h*.86f,
                        w*.67f,h*.79f,
                        w*.73f,h*.67f
                    )
                }

                drawPath(path,color,style=stroke)

                line(.72f,.34f,.72f,.18f)
                line(.72f,.34f,.56f,.34f)
            }

            NmixIcon.RANDOM->{
                val top=Path().apply{
                    moveTo(w*.18f,h*.31f)
                    lineTo(w*.31f,h*.31f)

                    cubicTo(
                        w*.42f,h*.31f,
                        w*.55f,h*.69f,
                        w*.69f,h*.69f
                    )

                    lineTo(w*.81f,h*.69f)
                }

                val bottom=Path().apply{
                    moveTo(w*.18f,h*.69f)
                    lineTo(w*.31f,h*.69f)

                    cubicTo(
                        w*.42f,h*.69f,
                        w*.55f,h*.31f,
                        w*.69f,h*.31f
                    )

                    lineTo(w*.81f,h*.31f)
                }

                drawPath(top,color,style=stroke)
                drawPath(bottom,color,style=stroke)

                line(.70f,.59f,.81f,.69f)
                line(.70f,.79f,.81f,.69f)

                line(.70f,.21f,.81f,.31f)
                line(.70f,.41f,.81f,.31f)
            }

            NmixIcon.DECIMAL->{
                dot(.5f,.67f,.065f)
            }

            NmixIcon.PLUS_MINUS->{
                line(.24f,.31f,.56f,.31f)
                line(.40f,.15f,.40f,.47f)
                line(.27f,.72f,.73f,.72f)
            }

            NmixIcon.BACKSPACE->{
                val path=Path().apply{
                    moveTo(w*.19f,h*.5f)
                    lineTo(w*.35f,h*.30f)
                    lineTo(w*.79f,h*.30f)
                    lineTo(w*.79f,h*.70f)
                    lineTo(w*.35f,h*.70f)
                    close()
                }

                drawPath(path,color,style=stroke)

                line(.48f,.41f,.64f,.59f)
                line(.64f,.41f,.48f,.59f)
            }

            NmixIcon.CHECK->{
                line(.25f,.52f,.43f,.69f)
                line(.43f,.69f,.76f,.32f)
            }

            NmixIcon.ROTATE->{
                val top=Path().apply{
                    moveTo(w*.25f,h*.43f)

                    cubicTo(
                        w*.29f,h*.25f,
                        w*.44f,h*.18f,
                        w*.58f,h*.21f
                    )

                    cubicTo(
                        w*.66f,h*.22f,
                        w*.72f,h*.27f,
                        w*.77f,h*.33f
                    )
                }

                drawPath(top,color,style=stroke)

                line(.77f,.33f,.77f,.18f)
                line(.77f,.33f,.62f,.33f)

                val bottom=Path().apply{
                    moveTo(w*.75f,h*.57f)

                    cubicTo(
                        w*.71f,h*.75f,
                        w*.56f,h*.82f,
                        w*.42f,h*.79f
                    )

                    cubicTo(
                        w*.34f,h*.78f,
                        w*.28f,h*.73f,
                        w*.23f,h*.67f
                    )
                }

                drawPath(bottom,color,style=stroke)

                line(.23f,.67f,.23f,.82f)
                line(.23f,.67f,.38f,.67f)
            }

            NmixIcon.WALLPAPER->{
                val frame=Path().apply{
                    moveTo(w*.20f,h*.22f)
                    lineTo(w*.80f,h*.22f)
                    lineTo(w*.80f,h*.78f)
                    lineTo(w*.20f,h*.78f)
                    close()
                }

                drawPath(
                    frame,
                    color,
                    style=stroke
                )

                dot(.63f,.38f,.055f)

                val mountains=Path().apply{
                    moveTo(w*.25f,h*.68f)
                    lineTo(w*.40f,h*.50f)
                    lineTo(w*.51f,h*.61f)
                    lineTo(w*.59f,h*.52f)
                    lineTo(w*.75f,h*.68f)
                }

                drawPath(
                    mountains,
                    color,
                    style=stroke
                )
            }
        }
    }
}
