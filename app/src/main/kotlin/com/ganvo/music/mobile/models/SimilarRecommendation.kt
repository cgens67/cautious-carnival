/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.models

import com.ganvo.music.mobile.innertube.models.YTItem
import com.ganvo.music.mobile.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
