package com.eag.tflmeme.core.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cmpmemecreator.composeapp.generated.resources.Res
import cmpmemecreator.composeapp.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource

data class MemeTemplate(
    val id: String,
    val drawable: DrawableResource
)

object MemeTemplates {
    var memeTemplates by mutableStateOf(
        Res
            .allDrawableResources
            .filterKeys { it.startsWith("meme_template") }
            .map { (key, value) ->
                MemeTemplate(
                    id = key,
                    drawable = value
                )
            }
    )

    fun shuffle() {
        memeTemplates = memeTemplates.shuffled()
    }
}
