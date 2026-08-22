package com.lxzrvi.nmix

import androidx.compose.runtime.*
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.floor
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
 * Reflecting line:
 *
 * Input keeps travelling forward forever.
 * Output reflects between -1 and +1.
 *
 * The wall therefore exists in a much larger
 * imaginary world, not at the visible viewport.
 */
private fun nmixReflect(
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

private fun nmixFrac(
    value:Float
):Float{
    return value-floor(value)
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
        quantity.coerceIn(1,5)

    if(!a.animationEnabled){
        return NmixWorldMotion(
            List(count){index->
                val homes=
                    listOf(
                        NmixWorldBody(
                            -.48f,
                            -.28f,
                            -12f,
                            1f
                        ),
                        NmixWorldBody(
                            .44f,
                            .31f,
                            15f,
                            .91f
                        ),
                        NmixWorldBody(
                            .36f,
                            -.39f,
                            -21f,
                            .83f
                        ),
                        NmixWorldBody(
                            -.39f,
                            .38f,
                            24f,
                            .87f
                        ),
                        NmixWorldBody(
                            .04f,
                            .03f,
                            0f,
                            .78f
                        )
                    )

                homes[index]
            }
        )
    }

    /*
     * phase is accumulated frame-by-frame instead of
     * being tied to a remembered tween duration.
     *
     * Changing Animation Speed therefore changes the
     * actual movement immediately without leaving and
     * reopening the screen.
     */
    var phase by remember(label){
        mutableFloatStateOf(0f)
    }

    var lastFrame by remember(label){
        mutableLongStateOf(0L)
    }

    val speed by rememberUpdatedState(
        a.animationSpeed.coerceIn(
            .45f,
            2.20f
        )
    )

    val animation by rememberUpdatedState(
        a.animation
    )

    LaunchedEffect(label){
        while(isActive){
            withFrameNanos{frame->
                if(lastFrame!=0L){
                    val delta=
                        (
                            frame-
                                lastFrame
                        )
                            .coerceAtMost(
                                50_000_000L
                            )/
                            1_000_000_000f

                    /*
                     * One world crossing is deliberately
                     * slow enough that the giant objects
                     * feel heavy instead of vibrating.
                     */
                    phase+=
                        delta*
                            speed*
                            .34f
                }

                lastFrame=frame
            }
        }
    }

    /*
     * Each body owns a different straight trajectory.
     * X and Y frequencies are intentionally unequal,
     * so reflected wall hits send the visible path back
     * at changing angles rather than retracing one line.
     *
     * The world itself is several viewports larger.
     * Rendering code applies the final world scale.
     */
    val seeds=
        listOf(
            floatArrayOf(
                .17f,
                .43f,
                .71f,
                .28f,
                .92f
            ),
            floatArrayOf(
                .63f,
                .12f,
                .38f,
                .84f,
                .51f
            ),
            floatArrayOf(
                .36f,
                .79f,
                .08f,
                .57f,
                .24f
            ),
            floatArrayOf(
                .88f,
                .31f,
                .61f,
                .05f,
                .76f
            ),
            floatArrayOf(
                .47f,
                .94f,
                .22f,
                .68f,
                .15f
            )
        )

    val bodies=
        List(count){index->
            val seed=
                seeds[index]

            /*
             * Large invisible-world velocity.
             * Different irrational-looking ratios mean
             * repeated wall reflection does not look
             * like a simple left/right loop.
             */
            val vx=
                when(index){
                    0->1.00f
                    1->-.83f
                    2->.71f
                    3->-.94f
                    else->.62f
                }

            val vy=
                when(index){
                    0->.67f
                    1->.91f
                    2->-.78f
                    3->-.59f
                    else->1.07f
                }

            var worldX=
                nmixReflect(
                    (
                        phase*vx+
                            seed[0]*3.7f
                    )
                )

            var worldY=
                nmixReflect(
                    (
                        phase*vy+
                            seed[1]*3.7f
                    )
                )

            /*
             * Visual collision response.
             *
             * Bodies share the same large world. When
             * two projected paths come very close, each
             * receives an opposite separation bend.
             * It is deliberately visual rather than a
             * heavyweight physics simulation.
             */
            if(count>1){
                for(other in 0 until index){
                    val otherSeed=
                        seeds[other]

                    val ovx=
                        when(other){
                            0->1.00f
                            1->-.83f
                            2->.71f
                            3->-.94f
                            else->.62f
                        }

                    val ovy=
                        when(other){
                            0->.67f
                            1->.91f
                            2->-.78f
                            3->-.59f
                            else->1.07f
                        }

                    val otherX=
                        nmixReflect(
                            phase*ovx+
                                otherSeed[0]*3.7f
                        )

                    val otherY=
                        nmixReflect(
                            phase*ovy+
                                otherSeed[1]*3.7f
                        )

                    val dx=
                        worldX-otherX

                    val dy=
                        worldY-otherY

                    val near=
                        abs(dx)<.24f &&
                            abs(dy)<.24f

                    if(near){
                        val push=
                            (
                                .24f-
                                    maxOf(
                                        abs(dx),
                                        abs(dy)
                                    )
                            )
                                .coerceAtLeast(0f)*
                                .58f

                        worldX+=
                            if(dx>=0f)
                                push
                            else
                                -push

                        worldY+=
                            if(dy>=0f)
                                push*.72f
                            else
                                -push*.72f
                    }
                }
            }

            /*
             * Do not clamp to viewport.
             * Values slightly beyond the nominal world
             * edge are useful during collision bends.
             */
            val spinDirection=
                if(index%2==0)
                    1f
                else
                    -1f

            val rotation=
                (
                    phase*
                        (
                            34f+
                                index*7f
                        )*
                        spinDirection+
                        seed[2]*120f
                )%360f

            val pulseBase=
                when(animation){
                    NmixAnimationName.DRIFT->
                        .10f

                    NmixAnimationName.ORBIT->
                        .07f

                    NmixAnimationName.FLOW->
                        .09f

                    NmixAnimationName.FLOAT->
                        .045f

                    NmixAnimationName.PULSE->
                        .18f

                    NmixAnimationName.CROSS->
                        .06f
                }

            val pulse=
                1f+
                    sin(
                        (
                            phase*
                                (
                                    2.1f+
                                        index*.23f
                                )+
                                seed[3]*6.28f
                        ).toDouble()
                    ).toFloat()*
                    pulseBase

            NmixWorldBody(
                x=worldX,
                y=worldY,
                rotation=rotation,
                pulse=pulse
            )
        }

    return NmixWorldMotion(
        bodies=bodies
    )
}

/*
 * Compatibility engine.
 *
 * Older/current callers continue to compile while the
 * four visual surfaces use the richer world bodies.
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

    val first=
        world.bodies[0]

    val second=
        world.bodies[1]

    val third=
        world.bodies[2]

    return NmixMotionValues(
        x=first.x,
        y=second.y,
        z=third.x,
        pulse=first.pulse
    )
}
