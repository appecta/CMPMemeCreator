package com.eag.tflmeme.meme_editor.data

import android.content.Context
import com.eag.tflmeme.meme_editor.domain.SaveToStorageStrategy
import java.io.File

actual class CacheStorageStrategy(
    private val context: Context
) : SaveToStorageStrategy {

    actual override fun getFilePath(fileName: String): String {
        return File(context.cacheDir, fileName).absolutePath
    }
}