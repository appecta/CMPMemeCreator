package com.plcoding.cmp_memecreator.meme_editor.data

import com.plcoding.cmp_memecreator.meme_editor.domain.SaveToStorageStrategy

expect class CacheStorageStrategy: SaveToStorageStrategy {
    override fun getFilePath(fileName: String): String
}