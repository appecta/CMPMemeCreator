@file:OptIn(ExperimentalComposeUiApi::class)

package com.eag.tflmeme.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eag.tflmeme.core.presentation.MemeTemplate
import com.eag.tflmeme.core.presentation.MemeTemplates.memeTemplates
import com.eag.tflmeme.game.domain.MemeInsert
import com.eag.tflmeme.game.domain.MemeTextData
import com.eag.tflmeme.game.domain.ReplicatableMemeState
import com.eag.tflmeme.game.domain.StateHolder
import com.eag.tflmeme.game.domain.SupabaseRepository
import com.eag.tflmeme.meme_editor.presentation.MemeEditorState
import com.eag.tflmeme.meme_editor.presentation.MemeText
import com.eag.tflmeme.meme_editor.presentation.TextBoxInteractionState
import com.eag.tflmeme.meme_editor.presentation.components.DraggableContainer
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val EditorBackground = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF000000))
)

@Composable
fun OyunMemeEditor(
    kullaniciadi: String,
    tur: Int,
    oyunkodu: String,
    timestamp: Int,
    memekategori: Int,
    oylamayaGit: () -> Unit
) {
    val viewModel: OyunMemeEditorViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stateHolder by viewModel.stateHolder.collectAsStateWithLifecycle()
    val clockState by viewModel.uiState.collectAsState()

    val template = remember { memeTemplates.random() }

    // Sayaç Başlatma
    LaunchedEffect(Unit) {
        viewModel.startCountdownFrom(timestamp.toString())
    }

    // Navigasyon ve Upload Logic
    LaunchedEffect(stateHolder) {
        if (stateHolder is StateHolder.SuccessWithData) {
            // Yükleme tetikleniyor...
            viewModel.uploadMeme(
                memeTemplate = template,
                oyunkodu = oyunkodu,
                tur = tur,
                kullaniciadi = kullaniciadi,
            )
        } else if (stateHolder is StateHolder.Success) {
            // Yükleme bitti, oylamaya git
            oylamayaGit()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Modern "Yazı Ekle" Butonu
            ExtendedFloatingActionButton(
                onClick = { viewModel.addText() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                text = { Text("Yazı Ekle") }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EditorBackground)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    // Boşluğa tıklayınca seçimi kaldır
                    detectTapGestures {
                        viewModel.unselectMemeText()
                    }
                }
        ) {
            // --- KATMAN 1: MEME EDİTÖR ALANI (ORTA) ---
            // Resmi ekranın ortasına, sınırlarına dikkat ederek yerleştiriyoruz
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 20.dp, start = 10.dp, end = 10.dp), // Üstte sayaç için yer bırak
                contentAlignment = Alignment.Center
            ) {
                // Resim Container
                Box(
                    modifier = Modifier
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val windowSize = currentWindowSize()

                    // Meme Resmi
                    Image(
                        painter = painterResource(template.drawable),
                        contentDescription = null,
                        modifier = Modifier
                            .then(
                                // Ekran yönüne göre resmi sığdır
                                if (windowSize.width > windowSize.height) {
                                    Modifier.fillMaxHeight()
                                } else Modifier.fillMaxWidth()
                            )
                            .onSizeChanged {
                                viewModel.updateContainerSize(it)
                            },
                        contentScale = if (windowSize.width > windowSize.height) {
                            ContentScale.FillHeight
                        } else ContentScale.FillWidth
                    )

                    // Sürüklenebilir Yazılar (Draggable Text Overlay)
                    DraggableContainer(
                        children = state.memeTexts,
                        textBoxInteractionState = state.textBoxInteractionState,
                        onChildTransformChanged = { id, offset, rotation, scale ->
                            viewModel.transformMemeText(id, offset, rotation, scale)
                        },
                        onChildClick = { viewModel.selectMemeText(it) },
                        onChildDoubleClick = { viewModel.editMemeText(it) },
                        onChildTextChange = { id, text -> viewModel.updateMemeText(id, text) },
                        onChildDeleteClick = { viewModel.deleteMemeText(it) },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            // --- KATMAN 2: ÜST BİLGİ PANELİ (HUD) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center, // Ortaya sabitle
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactTimer(
                    remainingSeconds = clockState.remainingSeconds,
                    progress = clockState.progress
                )
            }

            // --- KATMAN 3: YÜKLENİYOR EKRANI (Loading Overlay) ---
            // Oyun süresi bittiğinde ve upload başladığında ekranı kilitler
            if (stateHolder is StateHolder.SuccessWithData || stateHolder is StateHolder.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Meme Gönderiliyor...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// --- ALT BİLEŞENLER ---

@Composable
fun CompactTimer(
    remainingSeconds: Long,
    progress: Float
) {
    // Şeffaf arka planlı, hap şeklinde (pill-shaped) modern sayaç
    Surface(
        color = Color(0xFF2A2F3A).copy(alpha = 0.9f),
        shape = CircleShape, // Tam yuvarlak kenarlar
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.height(56.dp).width(140.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Dairesel İlerleme
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray.copy(alpha = 0.2f),
                    strokeWidth = 4.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (progress < 0.25f) Color(0xFFEF4444) else Color(0xFF3B82F6), // Kırmızı (Tehlike) veya Mavi
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Metin
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = formatSecondsToMMSS(remainingSeconds),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "KALAN SÜRE",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatSecondsToMMSS(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}


class OyunMemeEditorViewModel(
    val supabase: SupabaseRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(MemeEditorState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MemeEditorState()
        )

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState = _uiState.asStateFlow()


    private val _stateHolder = MutableStateFlow<StateHolder>(StateHolder.Idle)
    val stateHolder = _stateHolder.asStateFlow()


    fun uploadMeme(memeTemplate: MemeTemplate, oyunkodu: String, tur: Int, kullaniciadi: String) {
        println("UPLOAD MEME CALLED") // EKLENECEK SATIR
        viewModelScope.launch {
            try {
                val currentState = state.value

                val replicatableTexts = currentState.memeTexts.map { memeText ->
                    MemeTextData(
                        id = memeText.id,
                        fontSize = memeText.fontSize.value,
                        text = memeText.text,
                        offsetRatioX = memeText.offsetRatioX,
                        offsetRatioY = memeText.offsetRatioY,
                        scale = memeText.scale,
                        rotation = memeText.rotation
                    )
                }

                val memeToUpload = ReplicatableMemeState(
                    templateId = memeTemplate.id,
                    texts = replicatableTexts
                )

                val jsonString = Json.encodeToString(ReplicatableMemeState.serializer(), memeToUpload)

                println("Inserting to Supabase with data: $jsonString")

                supabase.supabaseClient.from("memeler").insert(
                    MemeInsert(
                        meme = jsonString,
                        oyunkodu = oyunkodu,
                        tur = tur,
                        oyuncu = kullaniciadi
                    )
                )

                println("Supabase insert successful!")
                _stateHolder.value = StateHolder.Success

            } catch (e: Exception) {
                println("!!! SUPABASE INSERT FAILED: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun transformMemeText(
        id: String,
        offset: Offset,
        rotation: Float,
        scale: Float
    ) {
        _state.update {
            val (width, height) = it.templateSize
            it.copy(
                memeTexts = it.memeTexts.map { memeText ->
                    if (memeText.id == id) {
                        memeText.copy(
                            offsetRatioX = offset.x / width,
                            offsetRatioY = offset.y / height,
                            rotation = rotation,
                            scale = scale
                        )
                    } else memeText
                }
            )
        }
    }

    fun unselectMemeText() {
        _state.update {
            it.copy(
                textBoxInteractionState = TextBoxInteractionState.None
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addText() {
        val id = Uuid.random().toString()

        val memeText = MemeText(
            id = id,
            text = "MEME İÇİN TIKLA",
            offsetRatioX = 0.25f,
            offsetRatioY = 0.25f,
        )

        _state.update {
            it.copy(
                memeTexts = it.memeTexts + memeText,
                textBoxInteractionState = TextBoxInteractionState.Selected(id)
            )
        }
    }

    fun deleteMemeText(id: String) {
        _state.update {
            it.copy(
                memeTexts = it.memeTexts.filter { memeText ->
                    memeText.id != id
                }
            )
        }
    }

    fun selectMemeText(id: String) {
        _state.update {
            it.copy(
                textBoxInteractionState = TextBoxInteractionState.Selected(id)
            )
        }
    }

    fun updateMemeText(id: String, text: String) {
        _state.update {
            it.copy(
                memeTexts = it.memeTexts.map { memeText ->
                    if (memeText.id == id) {
                        memeText.copy(text = text)
                    } else memeText
                }
            )
        }
    }

    fun editMemeText(id: String) {
        _state.update {
            it.copy(
                textBoxInteractionState = TextBoxInteractionState.Editing(id)
            )
        }
    }

    fun updateContainerSize(size: IntSize) {
        _state.update {
            it.copy(
                templateSize = size
            )
        }
    }

    private var timerJob: Job? = null

    @OptIn(ExperimentalTime::class)
    fun startCountdownFrom(timeString: String) {
        timerJob?.cancel()

        try {
            val hour = if(timeString.length == 5) timeString.take(1).toInt() else timeString.take(2).toInt()
            val minute = if(timeString.length == 5) timeString.substring(1, 3).toInt() else timeString.substring(2, 4).toInt()
            val second = if(timeString.length == 5) timeString.substring(3, 5).toInt() else timeString.substring(4, 6).toInt()

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val apiTime = LocalDateTime(now.year, now.month, now.day, hour, minute, second)

            val apiInstant = apiTime.toInstant(TimeZone.currentSystemDefault())
            val targetInstant = apiInstant.plus(120, DateTimeUnit.SECOND)

            startTicker(targetInstant)

        } catch (e: Exception) {
            println("Saat parse hatası: ${e.message}")
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startTicker(targetInstant: Instant) {
        timerJob = viewModelScope.launch {
            val totalDuration = 120f

            while (true) {
                val currentInstant = Clock.System.now()
                val remaining = targetInstant.minus(currentInstant).inWholeSeconds

                if (remaining <= 0) {
                    println("TIMER FINISHED: Setting state to SuccessWithData")
                    _uiState.value = TimerUiState(remainingSeconds = 0, progress = 0f)
                    _stateHolder.value = StateHolder.SuccessWithData(state.value)
                    break
                } else {
                    val progress = remaining.toFloat() / totalDuration
                    _uiState.value = TimerUiState(
                        remainingSeconds = remaining,
                        progress = progress
                    )
                }
                delay(1000L)
            }
        }
    }

}


data class TimerUiState(
    val remainingSeconds: Long = 0,
    val progress: Float = 1f
)