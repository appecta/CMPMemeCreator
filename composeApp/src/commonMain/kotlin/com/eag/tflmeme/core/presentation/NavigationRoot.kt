package com.eag.tflmeme.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.eag.tflmeme.core.presentation.MemeTemplates.memeTemplates
import com.eag.tflmeme.game.presentation.MemeOylamaScreen
import com.eag.tflmeme.game.presentation.MemeSonucScreen
import com.eag.tflmeme.game.presentation.OyunAnasayfaScreen
import com.eag.tflmeme.game.presentation.OyunMemeEditor
import com.eag.tflmeme.game.presentation.OyunaKatilScreen
import com.eag.tflmeme.meme_editor.presentation.MemeEditorRoot
import com.eag.tflmeme.meme_gallery.presentation.MemeGalleryScreen

@Composable
fun NavigationRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.MemeGallery
    ) {
        composable<Route.MemeGallery> {
            MemeGalleryScreen(
                onMemeTemplateSelected = { memeTemplate ->
                    navController.navigate(Route.MemeEditor(memeTemplate.id))
                },
                onGoGameClicked = {
                    navController.navigate(Route.OyunaKatil)
                }
            )
        }
        composable<Route.MemeEditor> {
            val templateId = it.toRoute<Route.MemeEditor>().templateId
            val template = remember(templateId) {
                memeTemplates.first { it.id == templateId }
            }
            MemeEditorRoot(
                template = template,
                onGoBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Route.OyunaKatil> {
            OyunaKatilScreen(
                onKatil = { oyunkodu, kullaniciadi, kurucuMu ->
                    navController.navigate(
                        Route.OyunAnasayfa(oyunkodu, kullaniciadi, kurucuMu)
                    )
                }
            )
        }
        composable<Route.OyunAnasayfa> {
            val oyunkodu = it.toRoute<Route.OyunAnasayfa>().oyunkodu
            val kullaniciadi = it.toRoute<Route.OyunAnasayfa>().kullaniciadi
            val kurucumu = it.toRoute<Route.OyunAnasayfa>().kurucuMu
            OyunAnasayfaScreen(
                oyunkodu,
                kullaniciadi,
                kurucumu,
                onSonucaGit = {
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.navigate(
                        Route.MemeSonuc(oyunkodu)
                    )
                },
                onOyunaGit = { kullaniciadi, oyunkodu, tur, memekategori, timestamp ->
                    navController.navigate(
                        Route.OyunMemeEditor(
                            kullaniciadi = kullaniciadi,
                            oyunkodu = oyunkodu,
                            timestamp = timestamp,
                            tur = tur,
                            memekategori = memekategori,
                            kurucuMu = kurucumu
                        )
                    )
                }
            )
        }
        composable<Route.OyunMemeEditor> {
            val tur = it.toRoute<Route.OyunMemeEditor>().tur
            val oyunkodu = it.toRoute<Route.OyunMemeEditor>().oyunkodu
            val timestamp = it.toRoute<Route.OyunMemeEditor>().timestamp
            val memekategori = it.toRoute<Route.OyunMemeEditor>().memekategori
            val kullaniciadi = it.toRoute<Route.OyunMemeEditor>().kullaniciadi
            val kurucuMu = it.toRoute<Route.OyunMemeEditor>().kurucuMu
            OyunMemeEditor(
                kullaniciadi,
                tur,
                oyunkodu,
                timestamp,
                memekategori,
                oylamayaGit = {
                    navController.popBackStack()
                    navController.navigate(
                        Route.MemeOylama(
                            kullaniciadi = kullaniciadi,
                            tur = tur,
                            oyunkodu = oyunkodu,
                            kurucuMu = kurucuMu
                        )
                    )
                }
            )
        }
        composable<Route.MemeOylama> {
            val kullaniciadi = it.toRoute<Route.MemeOylama>().kullaniciadi
            val kurucuMu = it.toRoute<Route.MemeOylama>().kurucuMu
            val tur = it.toRoute<Route.MemeOylama>().tur
            val oyunkodu = it.toRoute<Route.MemeOylama>().oyunkodu
            MemeOylamaScreen(
                kullaniciadi,
                tur,
                oyunkodu,
                oylamaBitti = {
                    navController.popBackStack()
                    navController.navigate(Route.OyunAnasayfa(oyunkodu, kullaniciadi, kurucuMu))
                }
            )
        }
        composable<Route.MemeSonuc> {
            val oyunkodu = it.toRoute<Route.MemeSonuc>().oyunkodu
            MemeSonucScreen(
                oyunkodu,
                onBack = {
                    navController.popBackStack()
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }
    }
}