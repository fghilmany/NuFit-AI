package com.fghilmany.nufitai

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fghilmany.nufitai.navigation.NuFitNavGraph
import com.fghilmany.nufitai.ui.theme.NuFitTheme

@Composable
@Preview
fun App() {
    NuFitTheme {
        NuFitNavGraph()
    }
}
