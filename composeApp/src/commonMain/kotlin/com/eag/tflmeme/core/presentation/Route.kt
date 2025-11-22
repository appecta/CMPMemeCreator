package com.eag.tflmeme.core.presentation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object MemeGallery: Route

    @Serializable
    data class MemeEditor(
        val templateId: String
    ): Route

    @Serializable
    data object OyunaKatil: Route

    @Serializable
    data class OyunAnasayfa(
        val oyunkodu: String,
        val kullaniciadi: String,
        val kurucuMu: Boolean,
    ): Route

    @Serializable
    data class OyunMemeEditor(
        val kullaniciadi: String,
        val oyunkodu: String,
        val timestamp: Int,
        val tur: Int,
        val memekategori: Int,
        val kurucuMu: Boolean
    ): Route

    @Serializable
    data class MemeOylama(
        val oyunkodu: String,
        val tur: Int,
        val kullaniciadi: String,
        val kurucuMu: Boolean
    )

    @Serializable
    data class MemeSonuc(
        val oyunkodu: String
    )
}