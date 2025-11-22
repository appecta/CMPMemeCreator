package com.eag.tflmeme.game.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eag.tflmeme.game.domain.Oyun
import com.eag.tflmeme.game.domain.OyunInsert
import com.eag.tflmeme.game.domain.OyuncuInsert
import com.eag.tflmeme.game.domain.StateHolder
import com.eag.tflmeme.game.domain.SupabaseRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OyunaKatilScreen(
    onKatil: (String, String, Boolean) -> Unit
) {
    val viewModel: OyunaKatilViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    var kullaniciadi by rememberSaveable { mutableStateOf("") }
    var oyunkodu by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Başarılı giriş durumunu dinle
    LaunchedEffect(key1 = state) {
        if (state is StateHolder.SuccessWithData) {
            val pair = ((state as StateHolder.SuccessWithData).data as Pair<String, Boolean>)
            onKatil(pair.first, kullaniciadi, pair.second)
        }
    }

    // Modern Koyu Tema Arka Planı (Gaming Havası)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF121212), // Koyu Gri/Siyah
            Color(0xFF1E232C), // Hafif Mavimsi Gri
            Color(0xFF0F172A)  // Derin Lacivert
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent // Brush kullanmak için transparent yaptık
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // Küçük ekranlar için scroll
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // --- Başlık ve İkon ---
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Oyuna Hoş Geldin",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Başlamak için bilgilerinizi girin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- Bölüm 1: Kimlik ---
                OutlinedTextField(
                    value = kullaniciadi,
                    onValueChange = { kullaniciadi = it },
                    label = { Text("Kullanıcı Adı") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Bölüm 2: Aksiyonlar ---
                // Kart görünümü ile aksiyonları grupluyoruz
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2F3A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Yeni Oyun Oluştur Butonu
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (kullaniciadi.isNotBlank()) viewModel.oyunOlustur(kullaniciadi)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = kullaniciadi.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Yeni Oyun Oluştur", fontSize = 16.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
                            Text(
                                text = "VEYA",
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f))
                        }

                        // Oyun Kodu Girişi
                        OutlinedTextField(
                            value = oyunkodu,
                            onValueChange = { oyunkodu = it },
                            label = { Text("Oyun Kodu") },
                            placeholder = { Text("Örn: X799") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color.Gray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (oyunkodu.isNotBlank() && kullaniciadi.isNotBlank()) {
                                        viewModel.oyunKatil(oyunkodu, kullaniciadi)
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Oyuna Katıl Butonu
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.oyunKatil(oyunkodu, kullaniciadi)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = oyunkodu.isNotBlank() && kullaniciadi.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Oyuna Katıl", fontSize = 16.sp)
                        }
                    }
                }

                // --- Hata Mesajı ---
                AnimatedVisibility(visible = state is StateHolder.Error) {
                    if (state is StateHolder.Error) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = (state as StateHolder.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


class OyunaKatilViewModel(
    private val supabase: SupabaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow<StateHolder>(StateHolder.Idle)
    val state = _state.asStateFlow()

    fun oyunOlustur( kurucu : String ) {
        if (kurucu.isNotEmpty()){
            viewModelScope.launch {
                _state.value = StateHolder.Loading

                val gecmisOyunlar = supabase.supabaseClient.postgrest.from("oyunlar")
                    .select(Columns.ALL).decodeList<Oyun>()

                val oyunkodlari = gecmisOyunlar.map { it.oyunkodu }
                val yeniOyunKodu = oyunKoduOlustur(oyunkodlari)

                val insert = supabase.supabaseClient.postgrest.from("oyunlar").insert(
                    OyunInsert(
                        oyunkodu = yeniOyunKodu,
                        gun = getTodayYYYYMMDD(),
                        kurucu = kurucu
                    )
                ) {
                    select()
                }.decodeSingle<Oyun>()

                supabase.supabaseClient.from("oyuncular")
                    .insert(
                        OyuncuInsert(
                            isim = kurucu,
                            oyunkodu = yeniOyunKodu
                        )
                    )

                _state.value = StateHolder.SuccessWithData(Pair(insert.oyunkodu, true))

            }
        } else {
            _state.value = StateHolder.Error("Kullanıcı adı boş olamaz")
        }
    }

    fun oyunKatil(oyunkodu : String, kullaniciadi : String) {
        if (oyunkodu.isNotEmpty() && kullaniciadi.isNotEmpty()){
            viewModelScope.launch {
                _state.value = StateHolder.Loading

                val oyunlar = supabase.supabaseClient.from("oyunlar")
                    .select(Columns.ALL).decodeList<Oyun>()

                if (oyunkodu in oyunlar.map { it.oyunkodu }) {
                    supabase.supabaseClient.from("oyuncular")
                        .insert(
                            OyuncuInsert(
                                isim = kullaniciadi,
                                oyunkodu = oyunkodu
                            )
                        )

                    _state.value = StateHolder.SuccessWithData(Pair(oyunkodu, false))
                } else {
                    _state.value = StateHolder.Error("Oyun kodu bulunamadı")
                }
            }
        } else {
            _state.value = StateHolder.Error("Oyun kodu veya kullanıcı adı boş olamaz")
        }
    }

    fun oyunKoduOlustur(gecmisOyunKodlari: List<String>): String {
        val karakterHavuzu = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var yeniKod: String

        do {
            yeniKod = (1..4)
                .map { karakterHavuzu.random() }
                .joinToString("")
        } while (gecmisOyunKodlari.contains(yeniKod))

        return yeniKod
    }

    @OptIn(ExperimentalTime::class)
    fun getTodayYYYYMMDD(): String {
        val currentMoment = Clock.System.now()
        val datetimeInSystemZone = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

        val year = datetimeInSystemZone.year
        val month = datetimeInSystemZone.month.number.toString().padStart(2, '0')
        val day = datetimeInSystemZone.day.toString().padStart(2, '0')

        return "$year$month$day"
    }

}