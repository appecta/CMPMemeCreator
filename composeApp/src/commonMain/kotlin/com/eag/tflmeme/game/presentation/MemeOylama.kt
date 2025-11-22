package com.eag.tflmeme.game.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmpmemecreator.composeapp.generated.resources.Res
import cmpmemecreator.composeapp.generated.resources.meme_template_1
import coil3.Image
import coil3.compose.AsyncImage
import com.eag.tflmeme.game.data.MemeResources
import com.eag.tflmeme.game.domain.Meme
import com.eag.tflmeme.game.domain.MemeResponse
import com.eag.tflmeme.game.domain.Oylama
import com.eag.tflmeme.game.domain.OylamaInsert
import com.eag.tflmeme.game.domain.ReplicatableMemeState
import com.eag.tflmeme.game.domain.StateHolder
import com.eag.tflmeme.game.domain.SupabaseRepository
import com.eag.tflmeme.meme_editor.presentation.MemeText
import com.eag.tflmeme.meme_editor.presentation.TextBoxInteractionState
import com.eag.tflmeme.meme_editor.presentation.components.DraggableContainer
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.readResourceBytes
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.koinInject

private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF000000))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemeOylamaScreen(
    kullaniciadi: String,
    tur: Int,
    oyunkodu: String,
    oylamaBitti: () -> Unit
) {
    val viewModel: MemeOylamaViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val oylar by viewModel.oylar.collectAsState()
    val stateHolder by viewModel.stateHolder.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getTurunMemeleri(oyunkodu, tur, kullaniciadi)
    }

    LaunchedEffect(stateHolder) {
        if (stateHolder is StateHolder.Success) {
            oylamaBitti()
        }
    }

    val oyVerilmisMemeSayisi = oylar.filter { it.value != null }.size
    val toplamMemeSayisi = state.memeler.size
    val progress = if (toplamMemeSayisi > 0) oyVerilmisMemeSayisi.toFloat() / toplamMemeSayisi else 0f

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            // İlerleme Çubuğu Header'ı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Oylama Zamanı",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$oyVerilmisMemeSayisi / $toplamMemeSayisi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.DarkGray
                )
            }
        },
        floatingActionButton = {
            // Tüm oylar verildiğinde çıkan buton
            AnimatedVisibility(
                visible = state.memeler.isNotEmpty() && oyVerilmisMemeSayisi == toplamMemeSayisi,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.oylamayiKaydet(kullaniciadi, oyunkodu, tur) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("Oylamayı Bitir") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.memeler) { meme ->
                    OylamaCard(
                        oyGuncelle = { oy -> viewModel.oyVerildi(meme.id, oy) },
                        memeyiYapan = meme.oyuncu,
                        meme = meme.meme,
                    )
                }
                // FAB'ın altında içerik kalmasın diye boşluk
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun OylamaCard(
    oyGuncelle: (Int) -> Unit,
    memeyiYapan: String,
    meme: ReplicatableMemeState,
) {
    // Yerel state: Hızlı UI tepkisi için.
    // Not: Gerçek uygulamada bu state ViewModel'den "oylar" listesinden beslenirse daha sağlam olur
    // ancak LazyColumn performansını bozmamak için basit tutuyoruz.
    var currentVote by remember { mutableStateOf<Int?>(null) }

    val texts = remember(meme) {
        meme.texts.map {
            MemeText(
                id = it.id,
                text = it.text,
                fontSize = it.fontSize.sp,
                offsetRatioX = it.offsetRatioX,
                offsetRatioY = it.offsetRatioY,
                rotation = it.rotation
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232C)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- KART BAŞLIĞI (YAPAN KİŞİ) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        memeyiYapan.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = memeyiYapan,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // --- MEME İÇERİĞİ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    // Görselin çok uzun olup ekranı kaplamasını engellemek için max height verebiliriz
                    .heightIn(min = 200.dp, max = 400.dp)
            ) {
                val painter = painterResource(MemeResources.map[meme.templateId.drop(14)] ?: Res.drawable.meme_template_1)

                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sadece Görüntüleme Modu (InteractionState.None)
                DraggableContainer(
                    children = texts,
                    textBoxInteractionState = TextBoxInteractionState.None,
                    onChildTransformChanged = { _, _, _, _ -> },
                    onChildClick = {},
                    onChildDoubleClick = {},
                    onChildTextChange = { _, _ -> },
                    onChildDeleteClick = {},
                    modifier = Modifier.matchParentSize()
                )
            }

            // --- OYLAMA BUTONLARI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DOWNVOTE (-1)
                VoteButton(
                    isSelected = currentVote == -1,
                    icon = if (currentVote == -1) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    selectedColor = Color(0xFFEF4444), // Red
                    defaultColor = Color.Gray,
                    onClick = {
                        currentVote = -1
                        oyGuncelle(-1)
                    }
                )

                // NEUTRAL (0)
                VoteButton(
                    isSelected = currentVote == 0,
                    icon = Icons.Outlined.HorizontalRule,
                    selectedColor = Color(0xFF9CA3AF), // Light Gray
                    defaultColor = Color.Gray,
                    onClick = {
                        currentVote = 0
                        oyGuncelle(0)
                    }
                )

                // UPVOTE (1)
                VoteButton(
                    isSelected = currentVote == 1,
                    icon = if (currentVote == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    selectedColor = Color(0xFF22C55E), // Green
                    defaultColor = Color.Gray,
                    onClick = {
                        currentVote = 1
                        oyGuncelle(1)
                    }
                )
            }
        }
    }
}

@Composable
fun VoteButton(
    isSelected: Boolean,
    icon: ImageVector,
    selectedColor: Color,
    defaultColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
                CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else defaultColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

data class MemeOylamaState(
    val memeler : List<Meme>
)

class MemeOylamaViewModel(
    val supabase: SupabaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MemeOylamaState(emptyList()))
    val state = _state.asStateFlow()

    private val _stateHolder = MutableStateFlow<StateHolder>(StateHolder.Idle)
    val stateHolder = _stateHolder.asStateFlow()


    private val _oylar = MutableStateFlow(mutableMapOf<String, Int?>())
    val oylar = _oylar.asStateFlow()


    fun getTurunMemeleri (oyunkodu: String, tur: Int, kullaniciadi: String) {
        viewModelScope.launch {
            delay(2000L)

            val memeResponse = supabase.supabaseClient.from("memeler").select{
                filter {
                    eq("oyunkodu", oyunkodu)
                    eq("tur", tur)
                }
            }.decodeList<MemeResponse>()

            val memeler = mutableListOf<Meme>()
            for (response in memeResponse) {
                //if (response.oyuncu == kullaniciadi) continue
                memeler.add(
                    Meme(
                        id = response.id,
                        oyunkodu = response.oyunkodu,
                        tur = response.tur,
                        oyuncu = response.oyuncu,
                        meme = Json.decodeFromString<ReplicatableMemeState>(response.meme)
                    )
                )
            }
            println(memeler)
            _state.value = _state.value.copy(
                memeler = memeler
            )
            _oylar.value = memeler.associate { it.id to null }.toMutableMap()

        }
    }

    fun oyVerildi (memeId : String, oy : Int) {
        val newOylar = _oylar.value.toMutableMap()
        newOylar[memeId] = oy
        _oylar.value = newOylar

        println("OY VERILDI: Meme ID: $memeId, Oy: $oy")
        println("GUNCEL OYLAR DURUMU: ${_oylar.value}")
        val oyVerilenMemeSayisi = _oylar.value.filter { it.value != null }.size
        println("OY VERILEN MEME SAYISI: $oyVerilenMemeSayisi")
    }

    fun oylamayiKaydet (kullaniciadi: String, oyunkodu: String, tur: Int) {
        viewModelScope.launch {
            for (memeId in oylar.value.keys){
                supabase.supabaseClient.from("oylamalar").insert(
                    OylamaInsert(
                        oyunkodu = oyunkodu,
                        tur = tur,
                        oyuncu = kullaniciadi,
                        meme = memeId,
                        oy = oylar.value[memeId]!!
                    )
                )
            }
            _stateHolder.value = StateHolder.Success
        }
    }



}
