/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistResponse(
    val playlistId: String
)
