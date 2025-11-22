package com.eag.tflmeme.game.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eag.tflmeme.game.domain.BitenOyun
import com.eag.tflmeme.game.domain.Oyun
import com.eag.tflmeme.game.domain.Oyuncu
import com.eag.tflmeme.game.domain.StateHolder
import com.eag.tflmeme.game.domain.SupabaseRepository
import com.eag.tflmeme.game.domain.Tur
import com.eag.tflmeme.game.domain.TurInsert
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


// Renk Paleti (Önceki ekranla uyumlu)
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF121212), Color(0xFF1E232C), Color(0xFF0F172A))
)
private val CardColor = Color(0xFF2A2F3A)
private val ActiveTabColor = Color(0xFF6366F1) // İndigo/Mor
private val InactiveTabColor = Color.Transparent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OyunAnasayfaScreen(
    oyunkodu: String,
    kullaniciadi: String,
    kurucuMu: Boolean,
    onSonucaGit: (String) -> Unit,
    onOyunaGit: (String, String, Int, Int, Int) -> Unit
) {
    val viewModel: OyunAnasayfaViewModel = koinInject()
    val state by viewModel.stateHolder.collectAsState()
    val info by viewModel.info.collectAsState()

    // Takip ve Yönlendirme Mantığı
    LaunchedEffect(Unit) {
        viewModel.oyunculariTakipEt(oyunkodu)
    }

    LaunchedEffect(state) {
        if (state is StateHolder.SuccessWithData) {
            if ((state as StateHolder.SuccessWithData).data != null) {
                val tur = (state as StateHolder.SuccessWithData).data as Tur
                onOyunaGit(kullaniciadi, oyunkodu, tur.tur, tur.memekategori, tur.timestamp)
            }
        } else if (state is StateHolder.Success) {
            onSonucaGit(oyunkodu)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            // Sadece Kurucu için Alt Kontrol Paneli
            if (kurucuMu) {
                AdminControlPanel(
                    info = info,
                    onStartRound = { viewModel.turBaslat() },
                    onEndGame = { viewModel.oyunuBitir(oyunkodu) }
                )
            } else {
                // Oyuncular için bekleme mesajı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(60.dp)
                        .background(Color(0xFF1E232C).copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(16.dp))
                        Text("Kurucunun oyunu başlatması bekleniyor...", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(20.dp))

                // --- HEADER: OYUN KODU ---
                GameCodeHeader(oyunkodu, info?.oyun?.kurucu)

                Spacer(Modifier.height(24.dp))

                // --- AYARLAR: KAYNAK SEÇİMİ ---
                // Sadece kurucu değiştirebilir, diğerleri sadece görür
                MemeSourceSelector(
                    selectedCategory = info?.oyun?.memekategori ?: 0,
                    isEditable = kurucuMu,
                    onToggle = { viewModel.memeKaynakDegistir(oyunkodu) }
                )

                Spacer(Modifier.height(32.dp))

                // --- OYUNCU LİSTESİ ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lobi (${info?.oyuncular?.size ?: 0})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.LightGray
                    )
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // BottomBar için boşluk
                ) {
                    // Kurucu kendini listede görmek isterse buraya eklenebilir,
                    // şimdilik filtreyi korudum.
                    items(items = info?.oyuncular?.filter { it.isim != info?.oyun?.kurucu } ?: emptyList()) { oyuncu ->
                        OyuncuRowModern(
                            oyuncu = oyuncu,
                            kurucuMu = kurucuMu,
                            onDelete = { viewModel.deleteOyuncu(oyuncu) }
                        )
                    }

                    if ((info?.oyuncular?.size ?: 0) <= 1) {
                        item {
                            Text(text = "Henüz başka oyuncu katılmadı.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

// --- Alt Bileşenler (UI Components) ---

@Composable
fun GameCodeHeader(code: String, kurucu: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2F3A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("OYUN KODU", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                // Görsel amaçlı kopyalama ikonu
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Kurucu: $kurucu", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MemeSourceSelector(selectedCategory: Int, isEditable: Boolean, onToggle: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if(isEditable) {
            Text("Meme Kaynağı Seçimi", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = isEditable) { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seçenek 1: Meme Arşivi
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (selectedCategory == 0) ActiveTabColor else InactiveTabColor, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Meme Arşivi",
                    color = Color.White,
                    fontWeight = if (selectedCategory == 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Seçenek 2: TFL Memeler
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (selectedCategory == 1) ActiveTabColor else InactiveTabColor, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "TFL Memeler",
                    color = Color.White,
                    fontWeight = if (selectedCategory == 1) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun OyuncuRowModern(oyuncu: Oyuncu, kurucuMu: Boolean, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar İkonu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text = oyuncu.isim,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            if (kurucuMu) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "Oyuncuyu At"
                    )
                }
            }
        }
    }
}

@Composable
fun AdminControlPanel(info: OyunAnasayfaInfo?, onStartRound: () -> Unit, onEndGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.95f), Color.Black)
                )
            )
            .padding(24.dp)
    ) {
        val turSayisi = info?.turlar?.maxByOrNull { it.tur }?.tur ?: 0
        val buttonText = if (info?.turlar.isNullOrEmpty()) "İlk Turu Başlat" else "${turSayisi + 1}. Turu Başlat"

        Button(
            onClick = onStartRound,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActiveTabColor), // Primary Color
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (info?.turlar?.isNotEmpty() == true) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onEndGame,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.Red, Color.Red))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Oyunu Bitir")
            }
        }
    }
}

data class OyunAnasayfaInfo(

    val oyun: Oyun,

    val oyuncular : List<Oyuncu>,

    val turlar : List<Tur>

)




class OyunAnasayfaViewModel (
    val supabase: SupabaseRepository
) : ViewModel() {

    private val _stateHolder = MutableStateFlow<StateHolder>(StateHolder.Idle)
    val stateHolder = _stateHolder.asStateFlow()


    private val _info = MutableStateFlow<OyunAnasayfaInfo?>(null)
    val info = _info.asStateFlow()


    @OptIn(SupabaseExperimental::class)
    fun oyunculariTakipEt(oyunkodu : String){
        viewModelScope.launch {

            val oyun = supabase.supabaseClient.from("oyunlar")
                .select {
                    filter {
                        eq("oyunkodu", oyunkodu)
                    }
                }.decodeSingle<Oyun>()

            val oyuncular = supabase.supabaseClient.from("oyuncular")
                .select {
                    filter {
                        eq("oyunkodu", oyunkodu)
                    }
                }.decodeList<Oyuncu>()

            val turlar = supabase.supabaseClient.from("turlar")
                .select {
                    filter {
                        eq("oyunkodu", oyunkodu)
                    }
                }.decodeList<Tur>()

            _info.value = OyunAnasayfaInfo(oyun, oyuncular, turlar)

            launch {
                val oyunFlow: Flow<Oyun> = supabase.supabaseClient.from("oyunlar")
                    .selectSingleValueAsFlow(
                        Oyun::oyunkodu,
                    ) {
                        eq("oyunkodu", oyunkodu)
                    }
                oyunFlow.collect { response ->
                    _info.value = _info.value?.copy(oyun = response)
                }
            }

            launch {
                val oyuncuFlow: Flow<List<Oyuncu>> = supabase.supabaseClient.from("oyuncular")
                    .selectAsFlow(
                        Oyuncu::oyunkodu,
                        filter = FilterOperation("oyunkodu", FilterOperator.EQ, oyunkodu)
                    )
                oyuncuFlow.collect { response ->
                    _info.value = _info.value?.copy(oyuncular = response)
                }
            }

            launch {
                val turFlow: Flow<List<Tur>> = supabase.supabaseClient.from("turlar")
                    .selectAsFlow(
                        Tur::oyunkodu,
                        filter = FilterOperation("oyunkodu", FilterOperator.EQ, oyunkodu)
                    )
                turFlow.collect { response ->
                    _info.value = _info.value?.copy(turlar = response)

                    val guncelTur = response.find { it.timestamp <= currentClock() && abs(currentClock() - it.timestamp) < 5}
                    if (guncelTur != null) {
                        _stateHolder.value = StateHolder.SuccessWithData(guncelTur)
                    }
                }
            }

            launch {
                val bitenOyunFlow: Flow<List<BitenOyun>> = supabase.supabaseClient.from("bitenoyunlar")
                    .selectAsFlow(
                        BitenOyun::oyunkodu,
                    )
                bitenOyunFlow.collect { response ->
                    if (response.any { it.oyunkodu == oyunkodu }) {
                        _stateHolder.value = StateHolder.Success
                    }
                }
            }
        }

    }


    fun memeKaynakDegistir( oyunkodu: String ) {
        viewModelScope.launch {
            _info.value = info.value?.copy(oyun = info.value!!.oyun.copy(memekategori = 1 - info.value?.oyun!!.memekategori))

            supabase.supabaseClient.from("oyunlar").update({
                set("memekategori", info.value?.oyun?.memekategori ?: 0)
            }) {
                filter {
                    eq("oyunkodu", oyunkodu)
                }
            }
        }
    }

    fun deleteOyuncu (oyuncu: Oyuncu) {
        viewModelScope.launch {
            supabase.supabaseClient.from("oyuncular").delete {
                filter {
                    eq("id", oyuncu.id)
                }
            }
        }
    }

    fun turBaslat() {
        viewModelScope.launch {
            val tur = if (info.value?.turlar.isNullOrEmpty()){
                1
            } else {
                info.value?.turlar?.maxBy{ it.tur }?.tur?.plus(1) ?: 1
            }

            supabase.supabaseClient.from("turlar").insert(
                TurInsert(
                    tur = tur,
                    timestamp = currentClock(),
                    oyunkodu = info.value?.oyun?.oyunkodu ?: "",
                    memekategori = info.value?.oyun?.memekategori ?: 0
                )
            )
        }
    }


    @OptIn(ExperimentalTime::class)
    fun currentClock(): Int {
        val currentMoment = Clock.System.now()
        val datetime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

        val hour = datetime.hour.toString().padStart(2, '0')
        val minute = datetime.minute.toString().padStart(2, '0')
        val second = datetime.second.toString().padStart(2, '0')

        return "$hour$minute$second".toInt()
    }


    fun oyunuBitir(oyunkodu: String) {
        viewModelScope.launch {
            supabase.supabaseClient.from("bitenoyunlar").insert(
                BitenOyun(oyunkodu)
            )
        }
    }


}