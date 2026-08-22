package com.lxzrvi.nmix

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Stable
data class NmixMotionValues(
    val x:Float,
    val y:Float,
    val z:Float,
    val pulse:Float
)

@Composable
fun rememberNmixMotion(
    label:String="nmixMotion"
):NmixMotionValues{
    val a=LocalNmixAppearance.current

    val motion=
        rememberInfiniteTransition(
            label=label
        )

    val t by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                durationMillis=
                    when(a.animation){
                        NmixAnimationName.DRIFT->3200
                        NmixAnimationName.ORBIT->2700
                        NmixAnimationName.FLOW->2300
                        NmixAnimationName.FLOAT->3600
                        NmixAnimationName.PULSE->2800
                        NmixAnimationName.CROSS->2500
                    },
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="${label}T"
    )

    val u by motion.animateFloat(
        initialValue=1f,
        targetValue=-1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                durationMillis=
                    when(a.animation){
                        NmixAnimationName.DRIFT->3900
                        NmixAnimationName.ORBIT->3100
                        NmixAnimationName.FLOW->2800
                        NmixAnimationName.FLOAT->4300
                        NmixAnimationName.PULSE->3400
                        NmixAnimationName.CROSS->3000
                    },
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="${label}U"
    )

    val v by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                durationMillis=
                    when(a.animation){
                        NmixAnimationName.DRIFT->4700
                        NmixAnimationName.ORBIT->3600
                        NmixAnimationName.FLOW->3200
                        NmixAnimationName.FLOAT->5100
                        NmixAnimationName.PULSE->4100
                        NmixAnimationName.CROSS->3500
                    },
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="${label}V"
    )

    val basePulse=
        .90f+
            ((u+1f)/2f)*.20f

    return when(a.animation){
        NmixAnimationName.DRIFT->
            NmixMotionValues(
                x=t,
                y=u,
                z=v,
                pulse=basePulse
            )

        NmixAnimationName.ORBIT->
            NmixMotionValues(
                x=t,
                y=v,
                z=-u,
                pulse=.97f+
                    ((v+1f)/2f)*.08f
            )

        NmixAnimationName.FLOW->
            NmixMotionValues(
                x=t*1.20f,
                y=t*.58f,
                z=u*1.12f,
                pulse=.94f+
                    ((u+1f)/2f)*.12f
            )

        NmixAnimationName.FLOAT->
            NmixMotionValues(
                x=t*.58f,
                y=u*1.18f,
                z=v*.65f,
                pulse=.96f+
                    ((t+1f)/2f)*.09f
            )

        NmixAnimationName.PULSE->
            NmixMotionValues(
                x=t*.30f,
                y=u*.24f,
                z=v*.22f,
                pulse=.82f+
                    ((t+1f)/2f)*.34f
            )

        NmixAnimationName.CROSS->
            NmixMotionValues(
                x=t*1.18f,
                y=u*.42f,
                z=-t*1.18f,
                pulse=.95f+
                    ((v+1f)/2f)*.10f
            )
    }
}
