package com.lxzrvi.nmix

import androidx.compose.runtime.*
import kotlinx.coroutines.isActive
import kotlin.math.sin

@Stable
data class NmixMotionValues(
    val x:Float,
    val y:Float,
    val z:Float,
    val pulse:Float
)

@Stable
data class NmixWorldBody(
    val x:Float,
    val y:Float,
    val rotation:Float,
    val pulse:Float
)

@Stable
data class NmixWorldMotion(
    val bodies:List<NmixWorldBody>
)

/*
 * Reflects a continuously increasing value between
 * -1 and +1.
 *
 * These boundaries belong to the huge imaginary
 * world. They are NOT visible screen boundaries.
 */
private fun nmixWorldReflect(
    value:Float
):Float{
    var p=value%4f

    if(p<0f){
        p+=4f
    }

    return when{
        p<1f->
            p

        p<3f->
            2f-p

        else->
            p-4f
    }
}

@Composable
fun rememberNmixWorldMotion(
    label:String="nmixWorld",
    quantity:Int=
        LocalNmixAppearance.current
            .animationQuantity
):NmixWorldMotion{
    val a=LocalNmixAppearance.current

    val count=
        quantity.coerceIn(
            1,
            5
        )

    if(!a.animationEnabled){
        val staticBodies=
            listOf(
                NmixWorldBody(
                    x=-.52f,
                    y=-.33f,
                    rotation=-12f,
                    pulse=1f
                ),
                NmixWorldBody(
                    x=.49f,
                    y=.36f,
                    rotation=17f,
                    pulse=.92f
                ),
                NmixWorldBody(
                    x=.40f,
                    y=-.43f,
                    rotation=-23f,
                    pulse=.84f
                ),
                NmixWorldBody(
                    x=-.44f,
                    y=.42f,
                    rotation=27f,
                    pulse=.88f
                ),
                NmixWorldBody(
                    x=.03f,
                    y=.04f,
                    rotation=4f,
                    pulse=.79f
                )
            )

        return NmixWorldMotion(
            staticBodies.take(count)
        )
    }

    /*
     * Frame-driven phase means changing the speed
     * slider changes motion immediately.
     */
    var phase by remember(label){
        mutableFloatStateOf(0f)
    }

    var previousFrame by remember(label){
        mutableLongStateOf(0L)
    }

    val liveSpeed by
        rememberUpdatedState(
            a.animationSpeed
                .coerceIn(
                    .45f,
                    2.20f
                )
        )

    val liveAnimation by
        rememberUpdatedState(
            a.animation
        )

    LaunchedEffect(label){
        while(isActive){
            withFrameNanos{
                frame->

                if(previousFrame!=0L){
                    val delta=
                        (
                            frame-
                                previousFrame
                        )
                            .coerceAtMost(
                                50_000_000L
                            )
                            .toFloat()/
                            1_000_000_000f

                    /*
                     * Heavy, calm world movement.
                     */
                    phase+=
                        delta*
                            liveSpeed*
                            .32f
                }

                previousFrame=frame
            }
        }
    }

    /*
     * Independent trajectories.
     *
     * No object-object collision logic exists here.
     * Every body only reacts to its own imaginary
     * world boundaries.
     *
     * Different X/Y velocities mean wall hits happen
     * at different moments, making paths repeatedly
     * return at different visible angles.
     */
    val velocityX=
        floatArrayOf(
            1.00f,
            -.81f,
            .69f,
            -.93f,
            .61f
        )

    val velocityY=
        floatArrayOf(
            .63f,
            .89f,
            -.76f,
            -.57f,
            1.04f
        )

    val startX=
        floatArrayOf(
            .16f,
            1.48f,
            2.31f,
            3.09f,
            .82f
        )

    val startY=
        floatArrayOf(
            1.37f,
            .24f,
            2.83f,
            1.94f,
            3.42f
        )

    val rotationSeed=
        floatArrayOf(
            -14f,
            19f,
            -27f,
            31f,
            8f
        )

    val pulseSeed=
        floatArrayOf(
            .12f,
            1.39f,
            2.61f,
            3.78f,
            4.94f
        )

    val bodies=
        List(count){
            index->

            val x=
                nmixWorldReflect(
                    startX[index]+
                        phase*
                            velocityX[index]
                )

            val y=
                nmixWorldReflect(
                    startY[index]+
                        phase*
                            velocityY[index]
                )

            val direction=
                if(index%2==0)
                    1f
                else
                    -1f

            val rotation=
                rotationSeed[index]+
                    phase*
                        (
                            28f+
                                index*6f
                        )*
                        direction

            val pulseAmount=
                when(liveAnimation){
                    NmixAnimationName.DRIFT->
                        .105f

                    NmixAnimationName.ORBIT->
                        .075f

                    NmixAnimationName.FLOW->
                        .09f

                    NmixAnimationName.FLOAT->
                        .042f

                    NmixAnimationName.PULSE->
                        .16f

                    NmixAnimationName.CROSS->
                        .055f
                }

            val pulse=
                1f+
                    sin(
                        (
                            phase*
                                (
                                    2.0f+
                                        index*.19f
                                )+
                                pulseSeed[index]
                        ).toDouble()
                    ).toFloat()*
                    pulseAmount

            NmixWorldBody(
                x=x,
                y=y,
                rotation=rotation,
                pulse=pulse
            )
        }

    return NmixWorldMotion(
        bodies=bodies
    )
}

/*
 * Compatibility for existing callers.
 */
@Composable
fun rememberNmixMotion(
    label:String="nmixMotion"
):NmixMotionValues{
    val a=LocalNmixAppearance.current

    if(!a.animationEnabled){
        return NmixMotionValues(
            x=0f,
            y=0f,
            z=0f,
            pulse=1f
        )
    }

    val world=
        rememberNmixWorldMotion(
            label="${label}_compat",
            quantity=3
        )

    return NmixMotionValues(
        x=world.bodies[0].x,
        y=world.bodies[1].y,
        z=world.bodies[2].x,
        pulse=world.bodies[0].pulse
    )
}
