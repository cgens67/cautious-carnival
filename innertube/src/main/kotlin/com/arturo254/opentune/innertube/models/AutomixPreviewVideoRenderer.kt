/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class AutomixPreviewVideoRenderer(
    val content: Content,
) {
    @Serializable
    data class Content(
        val automixPlaylistVideoRenderer: AutomixPlaylistVideoRenderer,
    ) {
        @Serializable
        data class AutomixPlaylistVideoRenderer(
            val navigationEndpoint: NavigationEndpoint,
        )
    }
}
