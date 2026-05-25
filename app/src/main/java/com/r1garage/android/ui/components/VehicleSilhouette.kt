package com.r1garage.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Stylized side-profile silhouette of a battery-electric SUV / pickup,
 * drawn with Compose primitives. Intentionally generic — boxy roofline,
 * short overhangs, raised stance — so it evokes the truck without copying
 * any manufacturer's industrial design.
 *
 * Tinted with [MaterialTheme.colorScheme.onSurface] so it reads against
 * the dark Home background as a low-key accent above the battery readout.
 */
@Composable
fun VehicleSilhouette(
    modifier: Modifier = Modifier,
) {
    val stroke = MaterialTheme.colorScheme.onSurface
    val fill = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .aspectRatio(3.2f, matchHeightConstraintsFirst = false),
    ) {
        val w = size.width
        val h = size.height

        // Body coordinates as fractions of the canvas — roof slopes back
        // from a vertical windshield, short rear overhang, raised ride
        // height with squared wheel arches.
        val body = Path().apply {
            // start at front bumper bottom
            moveTo(0.04f * w, 0.72f * h)
            // bumper up the nose
            lineTo(0.04f * w, 0.58f * h)
            quadraticBezierTo(0.06f * w, 0.52f * h, 0.12f * w, 0.50f * h)
            // hood
            lineTo(0.26f * w, 0.46f * h)
            // a-pillar / windshield
            lineTo(0.34f * w, 0.18f * h)
            // roof
            lineTo(0.72f * w, 0.18f * h)
            // d-pillar / rear glass straight down (boxy SUV roofline)
            lineTo(0.80f * w, 0.46f * h)
            // tailgate top edge
            lineTo(0.96f * w, 0.48f * h)
            // rear bumper
            quadraticBezierTo(0.99f * w, 0.52f * h, 0.98f * w, 0.62f * h)
            lineTo(0.98f * w, 0.72f * h)
            close()
        }
        drawPath(body, color = fill)
        drawPath(body, color = stroke, style = Stroke(width = 1.5.dp.toPx()))

        // Beltline (window strip)
        drawLine(
            color = stroke.copy(alpha = 0.6f),
            start = Offset(0.30f * w, 0.40f * h),
            end = Offset(0.82f * w, 0.40f * h),
            strokeWidth = 1.dp.toPx(),
        )

        // Two round wheels with a contrast inner cap.
        val wheelR = 0.10f * h * 1.2f
        val frontCx = 0.24f * w
        val rearCx = 0.76f * w
        val wheelY = 0.78f * h
        listOf(frontCx, rearCx).forEach { cx ->
            drawCircle(
                color = stroke,
                radius = wheelR,
                center = Offset(cx, wheelY),
            )
            drawCircle(
                color = fill,
                radius = wheelR * 0.55f,
                center = Offset(cx, wheelY),
            )
        }

        // Ground line — subtle.
        drawLine(
            color = stroke.copy(alpha = 0.25f),
            start = Offset(0f, 0.92f * h),
            end = Offset(w, 0.92f * h),
            strokeWidth = 1.dp.toPx(),
        )
    }
}
