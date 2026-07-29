package com.fghilmany.nufitai

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.gif.GifDecoder
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.fghilmany.nufitai.core.navigation.NuFitNavGraph
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme

@Composable
@Preview
fun App() {
    // ADR-007 -- registers GifDecoder so BundledMediaImage's ByteArray models animate GIFs
    // instead of only rendering a static first frame. KtorNetworkFetcherFactory added for P-15's
    // Google profile photo (URL-based model) -- reuses the same Ktor engine already on each
    // platform's classpath, no separate HTTP client dependency.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    NuFitTheme {
        NuFitNavGraph()
    }
}
