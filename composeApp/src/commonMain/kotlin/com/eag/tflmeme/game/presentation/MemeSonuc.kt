package com.eag.tflmeme.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmpmemecreator.composeapp.generated.resources.Res
import cmpmemecreator.composeapp.generated.resources.meme_template_1
import com.eag.tflmeme.game.data.MemeResources
import com.eag.tflmeme.game.domain.Meme
import com.eag.tflmeme.game.domain.MemeResponse
import com.eag.tflmeme.game.domain.Oylama
import com.eag.tflmeme.game.domain.ReplicatableMemeState
import com.eag.tflmeme.game.domain.SupabaseRepository
import com.eag.tflmeme.meme_editor.presentation.MemeText
import com.eag.tflmeme.meme_editor.presentation.TextBoxInteractionState
import com.eag.tflmeme.meme_editor.presentation.components.DraggableContainer
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

// Tema Renkleri & Derece Renkleri
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF000000))
)
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemeSonucScreen(
    oyunKodu: String,
    onBack: () -> Unit
) {
    val viewModel: MemeSonucViewModel = koinInject()
    val siraliMemeler by viewModel.siraliMemeList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMemeler(oyunKodu)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Sonuçlar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)
                )
            )
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // itemsIndexed kullanarak sıralamayı (index) alıyoruz
                itemsIndexed(siraliMemeler) { index, item ->
                    RankedMemeCard(
                        memeData = item,
                        rank = index + 1 // 0-based index olduğu için +1 ekliyoruz
                    )
                }
            }
        }
    }
}

@Composable
fun RankedMemeCard(
    memeData: MemeSonucData,
    rank: Int
) {
    // Sıralamaya göre renk ve boyut belirleme
    val rankColor = when (rank) {
        1 -> GoldColor
        2 -> SilverColor
        3 -> BronzeColor
        else -> Color.Gray
    }

    val isWinner = rank == 1

    // Meme üzerindeki yazıları hazırla
    val texts = remember(memeData) {
        memeData.meme.texts.map {
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
        elevation = CardDefaults.cardElevation(if (isWinner) 12.dp else 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            // Kazanan kartını biraz daha büyük vurgulayabiliriz istersen
            .then(if(isWinner) Modifier.padding(vertical = 8.dp) else Modifier)
    ) {
        Column {
            // --- HEADER: SIRA, İSİM ve PUAN ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2F3A))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sıralama Rozeti (Badge)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(rankColor.copy(alpha = 0.2f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (rank <= 3) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = rankColor,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "#$rank",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Oyuncu İsmi
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = memeData.yapanKisi,
                        color = if (isWinner) rankColor else Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isWinner) {
                        Text(
                            text = "Turun Kazananı!",
                            color = rankColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Puan Kısmı
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${memeData.oy}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Amber
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // --- MEME GÖRSELİ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .heightIn(min = 200.dp, max = 400.dp)
            ) {
                val painter = painterResource(MemeResources.map[memeData.meme.templateId.drop(14)] ?: Res.drawable.meme_template_1)


                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sadece Görüntüleme (InteractionState.None)
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
        }
    }
}


data class MemeSonucData(
    val meme: ReplicatableMemeState,
    val oy: Int,
    val yapanKisi : String
)


class MemeSonucViewModel(
    val supabase: SupabaseRepository
) : ViewModel() {

    private val _siraliMemeList = MutableStateFlow<List<MemeSonucData>>(mutableListOf())
    val siraliMemeList = _siraliMemeList.asStateFlow()

    fun getMemeler (oyunKodu: String) {
        viewModelScope.launch {
            val memeresponses = supabase.supabaseClient.from("memeler").select {
                filter {
                    eq("oyunkodu", oyunKodu)
                }
            }.decodeList<MemeResponse>()

            println("memeresponses $memeresponses")

            val memeler = memeresponses.map {
                Meme(
                    id = it.id,
                    oyunkodu = it.oyunkodu,
                    tur = it.tur,
                    oyuncu = it.oyuncu,
                    meme = Json.decodeFromString<ReplicatableMemeState>(it.meme)
                )
            }

            println("memeler $memeler")

            val oylamalar = supabase.supabaseClient.from("oylamalar").select {
                filter {
                    eq("oyunkodu", oyunKodu)
                }
            }.decodeList<Oylama>()

            println("oylamalar $oylamalar")

            val memeoy = mutableMapOf<String, Int>()

            for (oy in oylamalar){
                memeoy[oy.meme] = (memeoy[oy.meme] ?: 0) + oy.oy
            }

            println("memeoy $memeoy")

            val siraliListe = memeoy.toList().sortedByDescending { it.second }
            println(siraliListe)

            val mutableSiraliListe = mutableListOf<MemeSonucData>()
            for (eleman in siraliListe){
                mutableSiraliListe.add(
                    MemeSonucData(
                        meme = memeler.first { it.id == eleman.first }.meme,
                        oy = eleman.second,
                        yapanKisi = memeler.first { it.id == eleman.first }.oyuncu
                    )
                )
            }
            _siraliMemeList.value = mutableSiraliListe
        }
    }


}