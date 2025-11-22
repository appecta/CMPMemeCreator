package com.eag.tflmeme

import androidx.compose.runtime.Composable
import com.eag.tflmeme.core.presentation.NavigationRoot
import com.eag.tflmeme.core.theme.MemeCreatorTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MemeCreatorTheme {
        NavigationRoot()
    }
}