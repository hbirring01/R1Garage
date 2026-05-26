package com.r1garage.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/** Fixed hero height; matches the visual weight of [VehicleSilhouette]. */
private val HERO_HEIGHT_DP = 120.dp

/**
 * Hero image at the top of Home. Shows the owner's actual Rivian (paint +
 * wheels matched to their config) via Rivian's CDN when [imageUrl] is
 * available; falls back to the generic [VehicleSilhouette] while loading or
 * on any error so the layout never collapses.
 */
@Suppress("FunctionNaming") // Composable, PascalCase by Compose convention.
@Composable
fun VehicleImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isNullOrBlank()) {
        VehicleSilhouette(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val request = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request)

    if (painter.state is AsyncImagePainter.State.Success) {
        Image(
            painter = painter,
            contentDescription = "Vehicle",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(HERO_HEIGHT_DP),
        )
    } else {
        VehicleSilhouette(modifier = modifier)
    }
}

