package com.eag.tflmeme.meme_editor.domain

interface SaveToStorageStrategy {
    fun getFilePath(fileName: String): String
}