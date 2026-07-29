package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res

/**
 * ADR-007 -- renders a bundled `composeResources/files/...` image/GIF (e.g. exercise photos)
 * via Coil. [resourcePath] is resource-relative (e.g. "files/images/0001-xxx.jpg"); bytes are
 * read once via `Res.readBytes()` then handed to Coil as a `ByteArray` model -- Coil3's default
 * component registry (plus `coil-gif`, added in this module) picks the right decoder (static
 * bitmap vs animated GIF) from the byte content itself, no URI/Fetcher plumbing needed since
 * these are local bundled bytes, not network requests.
 */
@Composable
fun BundledMediaImage(resourcePath: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    if (resourcePath == null) {
        Box(modifier = modifier.fillMaxSize().background(NuFitColors.SurfaceContainerHigh))
        return
    }

    val bytes by produceState<ByteArray?>(initialValue = null, resourcePath) {
        value = Res.readBytes(resourcePath)
    }

    val loaded = bytes
    if (loaded == null) {
        Box(modifier = modifier.fillMaxSize().background(NuFitColors.SurfaceContainerHigh))
    } else {
        AsyncImage(
            model = loaded,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
