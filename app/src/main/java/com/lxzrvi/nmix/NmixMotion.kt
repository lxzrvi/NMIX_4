package com.lxzrvi.nmix

import androidx.compose.animation.core.*
import androidx.compose.runtime.*

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

    val speed1=when(a.animation){
        NmixAnimationName.DRIFT->3600
        NmixAnimationName.ORBIT->3000
        NmixAnimationName.FLOW->2700

        NmixAnimationName.FLOAT->2200
        NmixAnimationName.PULSE->1900
        NmixAnimationName.CROSS->2100
    }

    val speed2=when(a.animation){
        NmixAnimationName.DRIFT->4400
        NmixAnimationName.ORBIT->3500
        NmixAnimationName.FLOW->3200

        NmixAnimationName.FLOAT->2600
        NmixAnimationName.PULSE->2300
        NmixAnimationName.CROSS->2500
    }

    val speed3=when(a.animation){
        NmixAnimationName.DRIFT->5200
        NmixAnimationName.ORBIT->4100
        NmixAnimationName.FLOW->3800

        NmixAnimationName.FLOAT->3100
        NmixAnimationName.PULSE->2800
        NmixAnimationName.CROSS->2900
    }

    val t by motion.animateFloat(
        initialValue=-1f,
        targetValue=1f,
        animationSpec=infiniteRepeatable(
            animation=tween(
                speed1,
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
                speed2,
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
                speed3,
                easing=EaseInOutSine
            ),
            repeatMode=RepeatMode.Reverse
        ),
        label="${label}V"
    )

    return when(a.animation){
        /*
         * SOFT
         *
         * Slow, flowing motion intended for
         * large feathered glow shapes.
         */
        NmixAnimationName.DRIFT->
            NmixMotionValues(
                x=t,
                y=u*.72f,
                z=v*.82f,
                pulse=
                    .91f+
                    ((u+1f)/2f)*.18f
            )

        NmixAnimationName.ORBIT->
            NmixMotionValues(
                x=t*.88f,
                y=v*.88f,
                z=-u*.88f,
                pulse=
                    .95f+
                    ((v+1f)/2f)*.10f
            )

        NmixAnimationName.FLOW->
            NmixMotionValues(
                x=t*1.12f,
                y=t*.52f,
                z=u*1.05f,
                pulse=
                    .93f+
                    ((u+1f)/2f)*.14f
            )

        /*
         * HARD
         *
         * More deliberate geometry-oriented
         * movement. Landing/Display still keep
         * feathered rendering, while Settings
         * previews show sharper shapes.
         */
        NmixAnimationName.FLOAT->
            NmixMotionValues(
                x=t*.70f,
                y=u*1.16f,
                z=v*.72f,
                pulse=
                    .97f+
                    ((t+1f)/2f)*.06f
            )

        NmixAnimationName.PULSE->
            NmixMotionValues(
                x=t*.26f,
                y=u*.22f,
                z=v*.20f,
                pulse=
                    .78f+
                    ((t+1f)/2f)*.42f
            )

        NmixAnimationName.CROSS->
            NmixMotionValues(
                x=t*1.20f,
                y=u*.38f,
                z=-t*1.20f,
                pulse=
                    .96f+
                    ((v+1f)/2f)*.08f
            )
    }
}
