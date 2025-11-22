package com.eag.tflmeme.di

import com.eag.tflmeme.BuildKonfig
import com.eag.tflmeme.game.domain.SupabaseRepository
import com.eag.tflmeme.game.presentation.MemeOylamaViewModel
import com.eag.tflmeme.game.presentation.MemeSonucScreen
import com.eag.tflmeme.game.presentation.MemeSonucViewModel
import com.eag.tflmeme.game.presentation.OyunAnasayfaViewModel
import com.eag.tflmeme.game.presentation.OyunMemeEditorViewModel
import com.eag.tflmeme.game.presentation.OyunaKatilViewModel
import com.eag.tflmeme.meme_editor.presentation.MemeEditorViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformAppModule: Module

val appModule = module {

    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.API_URL,
            supabaseKey = BuildKonfig.API_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)

            useHTTPS = true
        }
    }

    factoryOf(::SupabaseRepository)

    viewModelOf(::MemeSonucViewModel)
    viewModelOf(::MemeOylamaViewModel)
    viewModelOf(::OyunMemeEditorViewModel)
    viewModelOf(::MemeEditorViewModel)
    viewModelOf(::OyunaKatilViewModel)
    viewModelOf(::OyunAnasayfaViewModel)
    includes(platformAppModule)
}