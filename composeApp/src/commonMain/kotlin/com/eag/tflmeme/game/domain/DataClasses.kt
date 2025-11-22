package com.eag.tflmeme.game.domain

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

@Serializable
data class Oyun(
    val id: String,
    val oyunkodu: String,
    val gun : String,
    val kurucu : String,
    val memekategori: Int
)

@Serializable
data class BitenOyun(
    val oyunkodu: String
)

@Serializable
data class OyunInsert(
    val oyunkodu: String,
    val gun : String,
    val kurucu : String
)

@Serializable
data class Oyuncu(
    val id: String,
    val isim: String,
    val oyunkodu: String
)

@Serializable
data class OyuncuInsert(
    val isim: String,
    val oyunkodu: String
)

@Serializable
data class Tur(
    val id : String,
    val oyunkodu : String,
    val timestamp : Int,
    val tur : Int,
    val memekategori: Int
)

@Serializable
data class TurInsert(
    val oyunkodu: String,
    val timestamp: Int,
    val tur: Int,
    val memekategori: Int
)

@Serializable
data class Meme(
    val id : String,
    val oyunkodu : String,
    val tur : Int,
    val oyuncu: String,
    val meme: ReplicatableMemeState
)

@Serializable
data class MemeResponse(
    val id : String,
    val oyunkodu : String,
    val tur : Int,
    val oyuncu: String,
    val meme: String
)


@Serializable
data class MemeInsert(
    val oyunkodu : String,
    val tur : Int,
    val oyuncu: String,
    val meme: String
)

@Serializable
data class Oylama(
    val id : String,
    val oyunkodu : String,
    val tur : Int,
    val oyuncu: String,
    val meme: String,
    val oy: Int
)

@Serializable
data class OylamaInsert(
    val oyunkodu : String,
    val tur : Int,
    val oyuncu: String,
    val meme: String,
    val oy: Int
)


@Serializable
data class ReplicatableMemeState(
    val templateId: String,
    val texts: List<MemeTextData>
)

@Serializable
data class MemeTextData(
    val id: String,
    val text: String,
    val fontSize: Float = 36f,
    val offsetRatioX: Float = 0f,
    val offsetRatioY: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f
)