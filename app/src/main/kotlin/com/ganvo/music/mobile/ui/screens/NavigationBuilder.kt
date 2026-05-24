/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */

package com.ganvo.music.mobile.ui.screens

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganvo.music.mobile.ui.screens.artist.ArtistAlbumsScreen
import com.ganvo.music.mobile.ui.screens.artist.ArtistItemsScreen
import com.ganvo.music.mobile.ui.screens.artist.ArtistScreen
import com.ganvo.music.mobile.ui.screens.artist.ArtistSongsScreen
import com.ganvo.music.mobile.ui.screens.library.LibraryScreen
import com.ganvo.music.mobile.ui.screens.musicrecognition.MusicRecognitionRoute
import com.ganvo.music.mobile.ui.screens.musicrecognition.MusicRecognitionScreen
import com.ganvo.music.mobile.ui.screens.playlist.AutoPlaylistScreen
import com.ganvo.music.mobile.ui.screens.playlist.CachePlaylistScreen
import com.ganvo.music.mobile.ui.screens.playlist.LocalPlaylistScreen
import com.ganvo.music.mobile.ui.screens.playlist.OnlinePlaylistScreen
import com.ganvo.music.mobile.ui.screens.playlist.TopPlaylistScreen
import com.ganvo.music.mobile.ui.screens.search.OnlineSearchResult
import com.ganvo.music.mobile.ui.screens.settings.AboutScreen
import com.ganvo.music.mobile.ui.screens.settings.AppearanceSettings
import com.ganvo.music.mobile.ui.screens.settings.BackupAndRestore
import com.ganvo.music.mobile.ui.screens.settings.ChangelogScreen
import com.ganvo.music.mobile.ui.screens.settings.ContentSettings
import com.ganvo.music.mobile.ui.screens.settings.CustomizeBackground
import com.ganvo.music.mobile.ui.screens.settings.DebugSettings
import com.ganvo.music.mobile.ui.screens.settings.DiscordLoginScreen
import com.ganvo.music.mobile.ui.screens.settings.DiscordSettings
import com.ganvo.music.mobile.ui.screens.settings.IntegrationScreen
import com.ganvo.music.mobile.ui.screens.settings.LastFMSettings

import com.ganvo.music.mobile.ui.screens.settings.PalettePickerScreen
import com.ganvo.music.mobile.ui.screens.settings.PlayerSettings
import com.ganvo.music.mobile.ui.screens.settings.PoTokenScreen
import com.ganvo.music.mobile.ui.screens.settings.PrivacySettings
import com.ganvo.music.mobile.ui.screens.settings.SettingsScreen
import com.ganvo.music.mobile.ui.screens.settings.StorageSettings
import com.ganvo.music.mobile.ui.screens.settings.ThemeCreatorScreen
import com.ganvo.music.mobile.ui.screens.settings.UpdateScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
) {
    composable(Screens.Home.route) {
        HomeScreen(navController)
    }
    composable(
        Screens.Library.route,
    ) {
        LibraryScreen(navController)
    }
    composable("history") {
        HistoryScreen(navController)
    }
    composable("stats") {
        StatsScreen(navController)
    }
    composable("year_in_music") {
        YearInMusicScreen(navController)
    }
    composable(MusicRecognitionRoute) {
        MusicRecognitionScreen(navController)
    }
    composable(Screens.MoodAndGenres.route) {
        MoodAndGenresScreen(navController)
    }
    composable("account") {
        AccountScreen(navController, scrollBehavior)
    }
    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }
    composable("charts_screen") {
        ChartsScreen(navController)
    }
    composable(
        route = "browse/{browseId}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
            }
        )
    ) {
        BrowseScreen(
            navController,
            scrollBehavior,
            it.arguments?.getString("browseId")
        )
    }
    composable(
        route = "search/{query}",
        arguments =
            listOf(
                navArgument("query") {
                    type = NavType.StringType
                },
            ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }
    composable(
        route = "album/{albumId}",
        arguments =
            listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                },
            ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/songs",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/albums",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            }
        )
    ) {
        ArtistAlbumsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/items?browseId={browseId}&params={params}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }
    composable(
        route = "online_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "local_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "auto_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "cache_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "top_playlist/{top}",
        arguments =
            listOf(
                navArgument("top") {
                    type = NavType.StringType
                },
            ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments =
            listOf(
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        YouTubeBrowseScreen(navController)
    }
    composable("settings") {
        SettingsScreen(navController, scrollBehavior, latestVersionName)
    }
    composable("settings/appearance") {
        AppearanceSettings(navController, scrollBehavior)
    }
    composable("settings/appearance/palette_picker") {
        PalettePickerScreen(navController)
    }
    composable("settings/appearance/theme_creator") {
        ThemeCreatorScreen(navController)
    }
    composable("settings/content") {
        ContentSettings(navController, scrollBehavior)
    }
    composable("settings/player") {
        PlayerSettings(navController, scrollBehavior)
    }
    composable("settings/storage") {
        StorageSettings(navController, scrollBehavior)
    }
    composable("settings/privacy") {
        PrivacySettings(navController, scrollBehavior)
    }
    composable("settings/backup_restore") {
        BackupAndRestore(navController, scrollBehavior)
    }
    composable("settings/discord") {
        DiscordSettings(navController, scrollBehavior)
    }
    composable("settings/integration") {
        IntegrationScreen(navController, scrollBehavior)
    }

    composable("settings/lastfm") {
        LastFMSettings(navController, scrollBehavior)
    }
    composable("settings/discord/experimental") {
        com.ganvo.music.mobile.ui.screens.settings.DiscordExperimental(navController)
    }
    composable("settings/misc") {
        DebugSettings(navController)
    }
    composable("settings/update") {
        UpdateScreen(navController, scrollBehavior)
    }
    composable("settings/changelog") {
        ChangelogScreen(navController, scrollBehavior)
    }
    composable("settings/discord/login") {
        DiscordLoginScreen(navController)
    }
    composable("settings/about") {
        AboutScreen(navController, scrollBehavior)
    }
    composable("settings/po_token") {
        PoTokenScreen(navController, scrollBehavior)
    }
    composable("customize_background") {
        CustomizeBackground(navController)
    }
    composable(
        route = "$LOGIN_ROUTE?$LOGIN_URL_ARGUMENT={$LOGIN_URL_ARGUMENT}",
        arguments = listOf(
            navArgument(LOGIN_URL_ARGUMENT) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        LoginScreen(
            navController,
            startUrl = backStackEntry.arguments?.getString(LOGIN_URL_ARGUMENT)?.let(Uri::decode)
        )
    }

// ─────────────────────────────────────────────────────────────────────────
// Always On Display — como diálogo que cubre completamente
// ─────────────────────────────────────────────────────────────────────────
    composable(
        route = "always_on_display",
        // Esto la hace un diálogo que se superpone
    ) { backStackEntry ->
        Dialog(
            onDismissRequest = { navController.navigateUp() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AlwaysOnDisplayScreen(navController)
        }
    }

}
