/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.pages

import com.ganvo.music.mobile.innertube.models.Album
import com.ganvo.music.mobile.innertube.models.AlbumItem
import com.ganvo.music.mobile.innertube.models.Artist
import com.ganvo.music.mobile.innertube.models.ArtistItem
import com.ganvo.music.mobile.innertube.models.MusicResponsiveListItemRenderer
import com.ganvo.music.mobile.innertube.models.MusicTwoRowItemRenderer
import com.ganvo.music.mobile.innertube.models.PlaylistItem
import com.ganvo.music.mobile.innertube.models.SongItem
import com.ganvo.music.mobile.innertube.models.YTItem
import com.ganvo.music.mobile.innertube.models.oddElements
import com.ganvo.music.mobile.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            val browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null
            val playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                ?.musicPlayButtonRenderer?.playNavigationEndpoint
                ?.watchPlaylistEndpoint?.playlistId
                ?: renderer.menu?.menuRenderer?.items?.firstOrNull()
                    ?.menuNavigationItemRenderer?.navigationEndpoint
                    ?.watchPlaylistEndpoint?.playlistId
                ?: browseId.removePrefix("MPREb_").let { "OLAK5uy_$it" }

            return AlbumItem(
                browseId = browseId,
                playlistId = playlistId,
                title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                artists = null,
                year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit = renderer.subtitleBadges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null
            )
        }
    }
}
