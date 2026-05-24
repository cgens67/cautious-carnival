/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer,
) {
    @Serializable
    data class SubscribeButtonRenderer(
        val subscribed: Boolean,
        val channelId: String,
    )
}
