package com.lxzrvi.nmix

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

enum class NmixIcon {
    CALCULATOR,
    CLOCK,
    COUNTER,
    HELP,
    TIMER,
    STOPWATCH,

    ARROW_UP,
    ARROW_DOWN,
    CHEVRON_DOWN,

    MENU,
    CLOSE,

    FULLSCREEN,
    BACK,

    PLUS,
    MINUS,
    RESET,
    RANDOM,

    DECIMAL,
    PLUS_MINUS,
    BACKSPACE,

    CHECK
}

@Composable
fun NmixIcon(
    icon: NmixIcon,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = size.minDimension

        val strokeWidth = unit * 0.095f

        val stroke = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        fun point(
            x: Float,
            y: Float
        ) = Offset(
            x = w * x,
            y = h * y
        )

        fun line(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float
        ) {
            drawLine(
                color = color,
                start = point(x1, y1),
                end = point(x2, y2),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        fun circle(
            x: Float,
            y: Float,
            radius: Float
        ) {
            drawCircle(
                color = color,
                radius = unit * radius,
                center = point(x, y)
            )
        }

        when (icon) {
            NmixIcon.CALCULATOR -> {
                circle(
                    x = 0.50f,
                    y = 0.23f,
                    radius = 0.055f
                )

                line(
                    0.22f,
                    0.50f,
                    0.78f,
                    0.50f
                )

                circle(
                    x = 0.50f,
                    y = 0.77f,
                    radius = 0.055f
                )
            }

            NmixIcon.CLOCK -> {
                drawCircle(
                    color = color,
                    radius = unit * 0.34f,
                    center = point(
                        0.50f,
                        0.50f
                    ),
                    style = stroke
                )

                line(
                    0.50f,
                    0.50f,
                    0.50f,
                    0.29f
                )

                line(
                    0.50f,
                    0.50f,
                    0.68f,
                    0.59f
                )
            }

            NmixIcon.COUNTER,
            NmixIcon.PLUS -> {
                line(
                    0.50f,
                    0.20f,
                    0.50f,
                    0.80f
                )

                line(
                    0.20f,
                    0.50f,
                    0.80f,
                    0.50f
                )
            }

            NmixIcon.MINUS -> {
                line(
                    0.20f,
                    0.50f,
                    0.80f,
                    0.50f
                )
            }

            NmixIcon.HELP -> {
                val path = Path().apply {
                    moveTo(
                        w * 0.29f,
                        h * 0.31f
                    )

                    cubicTo(
                        w * 0.34f,
                        h * 0.12f,
                        w * 0.72f,
                        h * 0.10f,
                        w * 0.72f,
                        h * 0.35f
                    )

                    cubicTo(
                        w * 0.72f,
                        h * 0.53f,
                        w * 0.50f,
                        h * 0.56f,
                        w * 0.50f,
                        h * 0.67f
                    )
                }

                drawPath(
                    path = path,
                    color = color,
                    style = stroke
                )

                circle(
                    x = 0.50f,
                    y = 0.84f,
                    radius = 0.055f
                )
            }

            NmixIcon.TIMER -> {
                drawCircle(
                    color = color,
                    radius = unit * 0.31f,
                    center = point(
                        0.50f,
                        0.55f
                    ),
                    style = stroke
                )

                line(
                    0.41f,
                    0.16f,
                    0.59f,
                    0.16f
                )

                line(
                    0.50f,
                    0.16f,
                    0.50f,
                    0.24f
                )

                line(
                    0.50f,
                    0.55f,
                    0.63f,
                    0.41f
                )
            }

            NmixIcon.STOPWATCH -> {
                drawCircle(
                    color = color,
                    radius = unit * 0.31f,
                    center = point(
                        0.50f,
                        0.53f
                    ),
                    style = stroke
                )

                line(
                    0.42f,
                    0.14f,
                    0.58f,
                    0.14f
                )

                line(
                    0.50f,
                    0.14f,
                    0.50f,
                    0.22f
                )

                circle(
                    x = 0.50f,
                    y = 0.53f,
                    radius = 0.075f
                )
            }

            NmixIcon.ARROW_UP -> {
                line(
                    0.50f,
                    0.78f,
                    0.50f,
                    0.24f
                )

                line(
                    0.50f,
                    0.24f,
                    0.30f,
                    0.44f
                )

                line(
                    0.50f,
                    0.24f,
                    0.70f,
                    0.44f
                )
            }

            NmixIcon.ARROW_DOWN -> {
                line(
                    0.50f,
                    0.22f,
                    0.50f,
                    0.76f
                )

                line(
                    0.50f,
                    0.76f,
                    0.30f,
                    0.56f
                )

                line(
                    0.50f,
                    0.76f,
                    0.70f,
                    0.56f
                )
            }

            NmixIcon.CHEVRON_DOWN -> {
                line(
                    0.27f,
                    0.38f,
                    0.50f,
                    0.62f
                )

                line(
                    0.50f,
                    0.62f,
                    0.73f,
                    0.38f
                )
            }

            NmixIcon.MENU -> {
                line(
                    0.20f,
                    0.28f,
                    0.80f,
                    0.28f
                )

                line(
                    0.20f,
                    0.50f,
                    0.80f,
                    0.50f
                )

                line(
                    0.20f,
                    0.72f,
                    0.80f,
                    0.72f
                )
            }

            NmixIcon.CLOSE -> {
                line(
                    0.27f,
                    0.27f,
                    0.73f,
                    0.73f
                )

                line(
                    0.73f,
                    0.27f,
                    0.27f,
                    0.73f
                )
            }

            NmixIcon.FULLSCREEN -> {
                line(
                    0.18f,
                    0.38f,
                    0.18f,
                    0.18f
                )

                line(
                    0.18f,
                    0.18f,
                    0.38f,
                    0.18f
                )

                line(
                    0.62f,
                    0.18f,
                    0.82f,
                    0.18f
                )

                line(
                    0.82f,
                    0.18f,
                    0.82f,
                    0.38f
                )

                line(
                    0.18f,
                    0.62f,
                    0.18f,
                    0.82f
                )

                line(
                    0.18f,
                    0.82f,
                    0.38f,
                    0.82f
                )

                line(
                    0.62f,
                    0.82f,
                    0.82f,
                    0.82f
                )

                line(
                    0.82f,
                    0.82f,
                    0.82f,
                    0.62f
                )
            }

            NmixIcon.BACK -> {
                line(
                    0.78f,
                    0.50f,
                    0.24f,
                    0.50f
                )

                line(
                    0.24f,
                    0.50f,
                    0.44f,
                    0.30f
                )

                line(
                    0.24f,
                    0.50f,
                    0.44f,
                    0.70f
                )
            }

            NmixIcon.RESET -> {
                val path = Path().apply {
                    moveTo(
                        w * 0.73f,
                        h * 0.35f
                    )

                    cubicTo(
                        w * 0.59f,
                        h * 0.16f,
                        w * 0.27f,
                        h * 0.20f,
                        w * 0.22f,
                        h * 0.48f
                    )

                    cubicTo(
                        w * 0.17f,
                        h * 0.75f,
                        w * 0.48f,
                        h * 0.88f,
                        w * 0.68f,
                        h * 0.69f
                    )
                }

                drawPath(
                    path,
                    color,
                    style = stroke
                )

                line(
                    0.73f,
                    0.35f,
                    0.73f,
                    0.17f
                )

                line(
                    0.73f,
                    0.35f,
                    0.55f,
                    0.34f
                )
            }

            NmixIcon.RANDOM -> {
                line(
                    0.18f,
                    0.30f,
                    0.34f,
                    0.30f
                )

                line(
                    0.34f,
                    0.30f,
                    0.67f,
                    0.69f
                )

                line(
                    0.67f,
                    0.69f,
                    0.82f,
                    0.69f
                )

                line(
                    0.70f,
                    0.59f,
                    0.82f,
                    0.69f
                )

                line(
                    0.70f,
                    0.79f,
                    0.82f,
                    0.69f
                )

                line(
                    0.18f,
                    0.70f,
                    0.34f,
                    0.70f
                )

                line(
                    0.34f,
                    0.70f,
                    0.67f,
                    0.31f
                )

                line(
                    0.67f,
                    0.31f,
                    0.82f,
                    0.31f
                )

                line(
                    0.70f,
                    0.21f,
                    0.82f,
                    0.31f
                )

                line(
                    0.70f,
                    0.41f,
                    0.82f,
                    0.31f
                )
            }

            NmixIcon.DECIMAL -> {
                circle(
                    0.50f,
                    0.68f,
                    0.07f
                )
            }

            NmixIcon.PLUS_MINUS -> {
                line(
                    0.23f,
                    0.31f,
                    0.57f,
                    0.31f
                )

                line(
                    0.40f,
                    0.14f,
                    0.40f,
                    0.48f
                )

                line(
                    0.27f,
                    0.73f,
                    0.73f,
                    0.73f
                )
            }

            NmixIcon.BACKSPACE -> {
                val path = Path().apply {
                    moveTo(
                        w * 0.20f,
                        h * 0.50f
                    )

                    lineTo(
                        w * 0.36f,
                        h * 0.29f
                    )

                    lineTo(
                        w * 0.80f,
                        h * 0.29f
                    )

                    lineTo(
                        w * 0.80f,
                        h * 0.71f
                    )

                    lineTo(
                        w * 0.36f,
                        h * 0.71f
                    )

                    close()
                }

                drawPath(
                    path,
                    color,
                    style = stroke
                )

                line(
                    0.47f,
                    0.40f,
                    0.65f,
                    0.60f
                )

                line(
                    0.65f,
                    0.40f,
                    0.47f,
                    0.60f
                )
            }

            NmixIcon.CHECK -> {
                line(
                    0.24f,
                    0.52f,
                    0.43f,
                    0.70f
                )

                line(
                    0.43f,
                    0.70f,
                    0.77f,
                    0.31f
                )
            }
        }
    }
}
