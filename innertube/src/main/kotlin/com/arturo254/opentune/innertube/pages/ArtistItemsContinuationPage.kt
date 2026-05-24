/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.pages

import com.ganvo.music.mobile.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
