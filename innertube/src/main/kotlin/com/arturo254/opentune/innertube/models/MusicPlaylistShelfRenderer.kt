/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicPlaylistShelfRenderer(
    val playlistId: String?,
    val contents: List<MusicShelfRenderer.Content> = emptyList(),
    val collapsedItemCount: Int? = null,
    val continuations: List<Continuation>? = null,
)
