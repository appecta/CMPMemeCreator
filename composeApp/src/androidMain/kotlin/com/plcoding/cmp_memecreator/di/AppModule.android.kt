package com.plcoding.cmp_memecreator.di

import com.plcoding.cmp_memecreator.meme_editor.data.CacheStorageStrategy
import com.plcoding.cmp_memecreator.meme_editor.data.PlatformMemeExporter
import com.plcoding.cmp_memecreator.meme_editor.domain.MemeExporter
import com.plcoding.cmp_memecreator.meme_editor.domain.SaveToStorageStrategy
import com.plcoding.cmp_memecreator.meme_editor.presentation.util.PlatformShareSheet
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformAppModule = module {
    factoryOf(::PlatformMemeExporter) bind MemeExporter::class
    factoryOf(::CacheStorageStrategy) bind SaveToStorageStrategy::class
    factoryOf(::PlatformShareSheet)
}