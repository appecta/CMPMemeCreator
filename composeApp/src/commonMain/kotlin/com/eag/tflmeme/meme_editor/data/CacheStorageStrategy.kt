package com.eag.tflmeme.meme_editor.data

import com.eag.tflmeme.meme_editor.domain.SaveToStorageStrategy

expect class CacheStorageStrategy: SaveToStorageStrategy {
    override fun getFilePath(fileName: String): String
}