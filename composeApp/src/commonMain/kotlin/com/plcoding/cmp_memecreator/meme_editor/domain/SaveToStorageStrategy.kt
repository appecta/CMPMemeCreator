package com.plcoding.cmp_memecreator.meme_editor.domain

interface SaveToStorageStrategy {
    fun getFilePath(fileName: String): String
}