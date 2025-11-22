@file:OptIn(ExperimentalMaterial3Api::class)

package com.eag.tflmeme.meme_gallery.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmpmemecreator.composeapp.generated.resources.Res
import cmpmemecreator.composeapp.generated.resources.meme_templates
import com.eag.tflmeme.core.presentation.MemeTemplate
import com.eag.tflmeme.core.presentation.MemeTemplates
import com.eag.tflmeme.core.presentation.MemeTemplates.memeTemplates
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Tema Renkleri (Diğer ekranlarla uyumlu)
private val GalleryBackground = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF000000))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemeGalleryScreen(
    onMemeTemplateSelected: (MemeTemplate) -> Unit,
    onGoGameClicked: () -> Unit
) {
    // Listeyi state içinde tutuyoruz ki shuffle yapıldığında ekran yenilensin
    var currentList by remember { mutableStateOf(memeTemplates) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Gradient için transparent
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.meme_templates),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Listeyi karıştır ve state'i güncelle
                            currentList = currentList.shuffled()
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Karıştır"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onGoGameClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Gamepad, contentDescription = null) },
                text = { Text("Oyuna Gir", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                expanded = true
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GalleryBackground)
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp), // Biraz daha geniş kartlar
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = 100.dp, // FAB altında içerik kalmaması için boşluk
                    start = 12.dp,
                    end = 12.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = currentList) { memeTemplate ->
                    MemeGalleryItem(
                        memeTemplate = memeTemplate,
                        onClick = { onMemeTemplateSelected(memeTemplate) }
                    )
                }
            }
        }
    }
}


@Composable
fun MemeGalleryItem(
    memeTemplate: MemeTemplate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E232C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Image(
                painter = painterResource(memeTemplate.drawable),
                contentDescription = null,
                contentScale = ContentScale.Crop, // Crop veya FillWidth tasarım tercihine göre
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Resmin orijinal oranını korumaya çalışır
            )

            // Opsiyonel: Resim üzerine hafif bir gradient veya efekt eklenebilir
        }
    }
}