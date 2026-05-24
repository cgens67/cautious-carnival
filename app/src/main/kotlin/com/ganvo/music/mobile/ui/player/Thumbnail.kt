/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.ganvo.music.mobile.LocalPlayerConnection
import com.ganvo.music.mobile.R
import com.ganvo.music.mobile.constants.CropThumbnailToSquareKey
import com.ganvo.music.mobile.constants.HidePlayerThumbnailKey
import com.ganvo.music.mobile.constants.PlayerBackgroundStyle
import com.ganvo.music.mobile.constants.PlayerBackgroundStyleKey
import com.ganvo.music.mobile.constants.PlayerHorizontalPadding
import com.ganvo.music.mobile.constants.SeekExtraSeconds
import com.ganvo.music.mobile.constants.SwipeThumbnailKey
import com.ganvo.music.mobile.constants.ThumbnailCornerRadiusKey
import com.ganvo.music.mobile.extensions.metadata
import com.ganvo.music.mobile.extensions.toMediaItem
import com.ganvo.music.mobile.innertube.YouTube
import com.ganvo.music.mobile.innertube.models.YouTubeClient
import com.ganvo.music.mobile.utils.rememberEnumPreference
import com.ganvo.music.mobile.utils.rememberPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.util.Locale
import kotlin.math.abs



private object ThumbnailConstants {
    const val CANVAS_DEFAULT_MAX_SIZE = 256
    const val PERSIST_FILE = "canvas_artwork_cache.json"
    const val PERSIST_DEBOUNCE_MS = 2_000L
    const val HEADER_HORIZONTAL_PADDING = 32
    const val HEADER_VERTICAL_PADDING = 16
    const val ICON_SIZE = 120
    const val THUMBNAIL_CORNER_RADIUS_DEFAULT = 16f
    const val HORIZONTAL_ITEM_WIDTH_FACTOR = 1f
    const val BLUR_RADIUS = 60f
    const val BLUR_ALPHA = 0.6f
    const val SEEK_EFFECT_DURATION_MS = 1_000L
    const val DOUBLE_TAP_WINDOW_MS = 1_000L
    const val SEEK_INCREMENT_MS = 5_000
    const val SEEK_TEXT_ALPHA = 0.7f
    const val VELOCITY_THRESHOLD = 500f

    const val ARTWORK_SIZE_TARGET = 1440
}
@Composable
private fun createImageRequest(
    context: Context,
    uri: String?
): ImageRequest {
    return remember(uri) {
        ImageRequest.Builder(context)
            .data(uri)
            .size(ThumbnailConstants.ARTWORK_SIZE_TARGET)
            .scale(Scale.FILL)
            .crossfade(true)
            .build()
    }
}



@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Thumbnail(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    isPlayerExpanded: Boolean = true, // Add parameter to control swipe based on player state
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current
    val currentView = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    // States
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val error by playerConnection.error.collectAsState()
    val queueTitle by playerConnection.queueTitle.collectAsState()

    val swipeThumbnail by rememberPreference(SwipeThumbnailKey, true)
    val hidePlayerThumbnail by rememberPreference(HidePlayerThumbnailKey, false)
    // Canvas feature removed; use standard artwork rendering
    val (thumbnailCornerRadius, _) = rememberPreference(
        key = ThumbnailCornerRadiusKey,
        defaultValue = 16f
    )
    // note: Thumbnail corner radius is defined above; keep consistency
    val maxCanvasCacheSize = 0
    val (thumbnailCornerRadius, _) = rememberPreference(
        key = ThumbnailCornerRadiusKey,
        defaultValue = 16f
    )
    val cropThumbnailToSquare by rememberPreference(CropThumbnailToSquareKey, false)
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    // Player background style for consistent theming
    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.DEFAULT
    )

    val textBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
        PlayerBackgroundStyle.BLUR -> Color.White
        PlayerBackgroundStyle.GRADIENT -> Color.White
        PlayerBackgroundStyle.COLORING -> Color.White
        PlayerBackgroundStyle.BLUR_GRADIENT -> Color.White
        PlayerBackgroundStyle.GLOW -> Color.White
        PlayerBackgroundStyle.GLOW_ANIMATED -> Color.White
        PlayerBackgroundStyle.CUSTOM -> Color.White
    }



    // Grid state
    val thumbnailLazyGridState = rememberLazyGridState()

    // Create a playlist using correct shuffle-aware logic
    val timeline = playerConnection.player.currentTimeline
    val currentIndex = playerConnection.player.currentMediaItemIndex
    val shuffleModeEnabled = playerConnection.player.shuffleModeEnabled
    val previousMediaMetadata = if (swipeThumbnail && !timeline.isEmpty) {
        val previousIndex = timeline.getPreviousWindowIndex(
            currentIndex,
            Player.REPEAT_MODE_OFF,
            shuffleModeEnabled
        )
        if (previousIndex != C.INDEX_UNSET) {
            try {
                playerConnection.player.getMediaItemAt(previousIndex)
            } catch (e: Exception) { null }
        } else null
    } else null

    val nextMediaMetadata = if (swipeThumbnail && !timeline.isEmpty) {
        val nextIndex = timeline.getNextWindowIndex(
            currentIndex,
            Player.REPEAT_MODE_OFF,
            shuffleModeEnabled
        )
        if (nextIndex != C.INDEX_UNSET) {
            try {
                playerConnection.player.getMediaItemAt(nextIndex)
            } catch (e: Exception) { null }
        } else null
    } else null

    val currentMediaItem = remember(mediaMetadata) {
        // Fallback to player's current item if mediaMetadata is null,
        // but prefer mediaMetadata for immediate updates during crossfade.
        val metadata = mediaMetadata
        if (metadata != null) {
            // Use extension to convert metadata to a proper MediaItem with all fields (uri, artwork, tag)
            metadata.toMediaItem()
        } else {
            try {
                playerConnection.player.currentMediaItem
            } catch (e: Exception) { null }
        }
    }

    val mediaItems = listOfNotNull(previousMediaMetadata, currentMediaItem, nextMediaMetadata)
    val currentMediaIndex = mediaItems.indexOf(currentMediaItem)

    // OuterTune Snap behavior
    val horizontalLazyGridItemWidthFactor = 1f
    val thumbnailSnapLayoutInfoProvider = remember(thumbnailLazyGridState) {
        SnapLayoutInfoProvider(
            lazyGridState = thumbnailLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
                (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
            },
            velocityThreshold = 500f
        )
    }

    // Current item tracking
    val currentItem by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemIndex } }
    val itemScrollOffset by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemScrollOffset } }

    // Handle swipe to change song
    LaunchedEffect(itemScrollOffset) {
        if (!thumbnailLazyGridState.isScrollInProgress || !swipeThumbnail || itemScrollOffset != 0 || currentMediaIndex < 0) return@LaunchedEffect

        if (currentItem > currentMediaIndex && canSkipNext) {
            playerConnection.player.seekToNext()
            if (com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.isRunning()) {
                try { com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.restart() } catch (_: Exception) {}
            }
        } else if (currentItem < currentMediaIndex && canSkipPrevious) {
            playerConnection.player.seekToPreviousMediaItem()
            if (com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.isRunning()) {
                try { com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.restart() } catch (_: Exception) {}
            }
        }
    }

    // Update position when song changes
    LaunchedEffect(mediaMetadata, currentMediaItem?.mediaId, canSkipPrevious, canSkipNext) {
        val index = maxOf(0, currentMediaIndex)
        if (index >= 0 && index < mediaItems.size) {
            try {
                thumbnailLazyGridState.animateScrollToItem(index)
            } catch (e: Exception) {
                thumbnailLazyGridState.scrollToItem(index)
            }
        }
    }

    LaunchedEffect(playerConnection.player.currentMediaItemIndex, currentMediaItem?.mediaId) {
        val index = mediaItems.indexOf(currentMediaItem)
        if (index >= 0 && index != currentItem) {
            thumbnailLazyGridState.scrollToItem(index)
        }
    }

    // Seek on double tap
    var showSeekEffect by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf("") }
    val layoutDirection = LocalLayoutDirection.current

    Box(modifier = modifier) {
        // Error view
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center),
        ) {
            error?.let { playbackError ->
                PlaybackError(
                    error = playbackError,
                    retry = playerConnection.service::retryCurrentFromFreshStream,
                )
            }
        }

        // Main thumbnail view
        AnimatedVisibility(
            visible = error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Now Playing header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.now_playing),
                        style = MaterialTheme.typography.titleMedium,
                        color = textBackgroundColor
                    )
                    // Show album title or queue title
                    val playingFrom = queueTitle ?: mediaMetadata?.album?.title
                    if (!playingFrom.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playingFrom,
                            style = MaterialTheme.typography.titleMedium,
                            color = textBackgroundColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }

                // Thumbnail content
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
                    val containerMaxWidth = maxWidth

                    LazyHorizontalGrid(
                        state = thumbnailLazyGridState,
                        rows = GridCells.Fixed(1),
                        flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                        userScrollEnabled = swipeThumbnail && isPlayerExpanded, // Only allow swipe when player is expanded
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = mediaItems,
                            key = { item ->
                                // Use mediaId with stable fallback to avoid recomposition issues
                                item.mediaId.ifEmpty { "unknown_${item.hashCode()}" }
                            }
                        ) { item ->
                            val incrementalSeekSkipEnabled by rememberPreference(SeekExtraSeconds, defaultValue = false)
                            var skipMultiplier by remember { mutableStateOf(1) }
                            var lastTapTime by remember { mutableLongStateOf(0L) }
                            val itemMetadata = remember(item) { item.metadata }
                            val storefront =
                                remember {
                                    val country = Locale.getDefault().country
                                    if (country.length == 2) country.lowercase(Locale.ROOT) else "us"
                                }
                            val shouldAnimateCanvas = false
                            var canvasArtworkUrl by remember(item.mediaId) { mutableStateOf<String?>(null) }
                            var canvasFetchedAtMs by remember(item.mediaId) { mutableLongStateOf(0L) }
                            var canvasFetchInFlight by remember(item.mediaId) { mutableStateOf(false) }

                            LaunchedEffect(shouldAnimateCanvas) {
                                if (!shouldAnimateCanvas) {
                                                                    canvasArtworkUrl = null
                                                                    canvasFetchedAtMs = 0L
                                                                    canvasFetchInFlight = false
                                                                }
                            }

                            LaunchedEffect(shouldAnimateCanvas, item.mediaId) {
                                if (!shouldAnimateCanvas) return@LaunchedEffect

                                val songTitleRaw = itemMetadata?.title
                                        ?.takeIf { it.isNotBlank() }
                                        ?: item.mediaMetadata.title?.toString()
                                        ?: return@LaunchedEffect

                                val artistNameRaw =
                                    itemMetadata?.artists?.firstOrNull()?.name
                                        ?.takeIf { it.isNotBlank() }
                                        ?: item.mediaMetadata.artist?.toString()
                                        ?: item.mediaMetadata.subtitle?.toString()
                                        ?: ""

                                val now = System.currentTimeMillis()
                                if (canvasFetchInFlight) return@LaunchedEffect
                                canvasFetchInFlight = true

                                val fetched =
                                    withContext(Dispatchers.IO) {
                                        val songTitle = normalizeCanvasSongTitle(songTitleRaw)
                                        val artistName = normalizeCanvasArtistName(artistNameRaw)
                                        val candidates =
                                            linkedSetOf(
                                                songTitle to artistName,
                                                songTitleRaw to artistName,
                                                songTitle to artistNameRaw,
                                                songTitleRaw to artistNameRaw,
                                            ).filter { (song, artist) ->
                                                song.isNotBlank() && artist.isNotBlank()
                                            }

                                        candidates.firstNotNullOfOrNull { (song, artist) ->
                                            null
                                        }
                                    }
                                canvasArtworkUrl = null
                                                                canvasFetchedAtMs = now

                                canvasFetchInFlight = false
                            }

                            Box(
                                modifier = Modifier
                                    .width(horizontalLazyGridItemWidth)
                                    .fillMaxSize()
                                    .padding(horizontal = PlayerHorizontalPadding)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = { offset ->
                                                val currentPosition = playerConnection.player.currentPosition
                                                val duration = playerConnection.player.duration

                                                val now = System.currentTimeMillis()
                                                if (incrementalSeekSkipEnabled && now - lastTapTime < 1000) {
                                                    skipMultiplier++
                                                } else {
                                                    skipMultiplier = 1
                                                }
                                                lastTapTime = now

                                                val skipAmount = 5000 * skipMultiplier

                                                if ((layoutDirection == LayoutDirection.Ltr && offset.x < size.width / 2) ||
                                                    (layoutDirection == LayoutDirection.Rtl && offset.x > size.width / 2)
                                                ) {
                                                    playerConnection.player.seekTo(
                                                        (currentPosition - skipAmount).coerceAtLeast(0)
                                                    )
                                                    seekDirection =
                                                        context.getString(R.string.seek_backward_dynamic, skipAmount / 1000)
                                                } else {
                                                    playerConnection.player.seekTo(
                                                        (currentPosition + skipAmount).coerceAtMost(duration)
                                                    )
                                                    seekDirection = context.getString(R.string.seek_forward_dynamic, skipAmount / 1000)
                                                }
                                                // If a user double-tap skip lands on a new media item, restart presence manager to pick up artwork quickly
                                                if (com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.isRunning()) {
                                                    try { com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.restart() } catch (_: Exception) {}
                                                }

                                                showSeekEffect = true
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                        Box(
                                            modifier = Modifier
                                                .size(containerMaxWidth - (PlayerHorizontalPadding * 2))
                                                .clip(RoundedCornerShape(thumbnailCornerRadius.dp))
                                            ) {
                                    if (hidePlayerThumbnail) {
                                        // Show app logo when thumbnail is hidden
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ganvomusic),
                                                contentDescription = stringResource(R.string.hide_player_thumbnail),
                                                tint = textBackgroundColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(120.dp)
                                            )
                                        }
                                    } else {
                                        val primaryCanvasUrl = canvasArtworkUrl
                                                                                val fallbackCanvasUrl: String? = null

                                        AsyncImage(
                                            model = item.mediaMetadata.artworkUri?.toString(),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .let { if (cropThumbnailToSquare) it.aspectRatio(1f) else it }
                                                .graphicsLayer(
                                                    renderEffect = BlurEffect(radiusX = 60f, radiusY = 60f),
                                                    alpha = 0.6f
                                                )
                                        )

                                        AsyncImage(
                                            model = item.mediaMetadata.artworkUri?.toString(),
                                            contentDescription = null,
                                            contentScale = if (cropThumbnailToSquare) ContentScale.Crop else ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .let { if (cropThumbnailToSquare) it.aspectRatio(1f) else it }
                                        )

                                        if (shouldAnimateCanvas && (!primaryCanvasUrl.isNullOrBlank() || !fallbackCanvasUrl.isNullOrBlank())) {
                                            CanvasArtworkPlayer(
                                                primaryUrl = primaryCanvasUrl,
                                                fallbackUrl = fallbackCanvasUrl,
                                                isPlaying = isPlaying,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seek effect
        LaunchedEffect(showSeekEffect) {
            if (showSeekEffect) {
                delay(1000)
                showSeekEffect = false
            }
        }

        AnimatedVisibility(
            visible = showSeekEffect,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = seekDirection,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun CanvasArtworkPlayer(
    primaryUrl: String?,
    fallbackUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val primary = primaryUrl?.takeIf { it.isNotBlank() }
    val fallback = fallbackUrl?.takeIf { it.isNotBlank() }
    val initial = primary ?: fallback ?: return
    var currentUrl by remember(initial) { mutableStateOf(initial) }
    var isVideoReady by remember(initial) { mutableStateOf(false) }

    val okHttpClient =
        remember {
            OkHttpClient
                .Builder()
                .proxy(YouTube.proxy)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val host = request.url.host
                    val isYouTubeMediaHost =
                        host.endsWith("googlevideo.com") ||
                            host.endsWith("googleusercontent.com") ||
                            host.endsWith("youtube.com") ||
                            host.endsWith("youtube-nocookie.com") ||
                            host.endsWith("ytimg.com")

                    if (!isYouTubeMediaHost) return@addInterceptor chain.proceed(request)

                    val clientParam = request.url.queryParameter("c")?.trim().orEmpty()
                    val isWeb =
                        clientParam.startsWith("WEB", ignoreCase = true) ||
                            clientParam.startsWith("WEB_REMIX", ignoreCase = true) ||
                            request.url.toString().contains("c=WEB", ignoreCase = true)

                    val userAgent =
                        when {
                            clientParam.startsWith("WEB", ignoreCase = true) ||
                                clientParam.startsWith("WEB_REMIX", ignoreCase = true) -> YouTubeClient.USER_AGENT_WEB

                            clientParam.startsWith("IOS", ignoreCase = true) -> YouTubeClient.IOS.userAgent

                            clientParam.startsWith("ANDROID_VR", ignoreCase = true) -> YouTubeClient.ANDROID_VR_NO_AUTH.userAgent

                            clientParam.startsWith("ANDROID", ignoreCase = true) -> YouTubeClient.MOBILE.userAgent

                            else -> YouTubeClient.USER_AGENT_WEB
                        }

                    val builder = request.newBuilder().header("User-Agent", userAgent)
                    if (isWeb) {
                        builder.header("Origin", YouTubeClient.ORIGIN_YOUTUBE_MUSIC)
                        builder.header("Referer", YouTubeClient.REFERER_YOUTUBE_MUSIC)
                    }

                    chain.proceed(builder.build())
                }
                .build()
        }
    val mediaSourceFactory =
        remember(okHttpClient) {
            DefaultMediaSourceFactory(
                DefaultDataSource.Factory(
                    context,
                    OkHttpDataSource.Factory(okHttpClient),
                ),
            )
        }
    val exoPlayer =
        remember(initial) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build(),
                    false,
                )
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = isPlaying
            }
        }

    LaunchedEffect(isPlaying) {
        if (exoPlayer.playWhenReady != isPlaying) {
            exoPlayer.playWhenReady = isPlaying
        }
    }

    DisposableEffect(exoPlayer, primary, fallback) {
        val listener =
            object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    val next =
                        when (currentUrl) {
                            primary -> fallback
                            else -> null
                        }
                    if (!next.isNullOrBlank()) {
                        currentUrl = next
                        isVideoReady = false
                    }
                }

                override fun onRenderedFirstFrame() {
                    isVideoReady = true
                }
            }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(currentUrl, exoPlayer) {
        val normalized = currentUrl.trim()
        val mimeType =
            when {
                primary != null && currentUrl == primary -> MimeTypes.APPLICATION_M3U8
                fallback != null && currentUrl == fallback -> MimeTypes.VIDEO_MP4
                normalized.lowercase(Locale.ROOT).contains("m3u8") -> MimeTypes.APPLICATION_M3U8
                normalized.lowercase(Locale.ROOT).contains("mp4") -> MimeTypes.VIDEO_MP4
                else -> MimeTypes.APPLICATION_M3U8
            }

        val mediaItem =
            MediaItem.Builder()
                .setUri(normalized)
                .setMimeType(mimeType)
                .build()

        exoPlayer.stop()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "canvasAlpha"
    )

    AndroidView(
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { view ->
            if (view.player !== exoPlayer) view.player = exoPlayer
        },
        modifier = modifier.alpha(alpha),
    )
}

private fun restartDiscordPresenceIfRunning() {
    if (
        com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.isRunning()
    ) {
        try {
            com.ganvo.music.mobile.ui.screens.settings.DiscordPresenceManager.restart()
        } catch (_: Exception) {
        }
    }
}


private fun normalizeCanvasSongTitle(raw: String): String {
    return raw
        .replace(Regex("\\s*\\[[^]]*]"), "")
        .replace(
            Regex(
                "\\s*\\((?:feat\\.?|ft\\.?|featuring|with)\\b[^)]*\\)",
                RegexOption.IGNORE_CASE
            ),
            ""
        )
        .replace(
            Regex(
                "\\s*\\((?:official\\s*)?(?:music\\s*)?(?:video|mv|lyrics?|audio|visualizer|live|remaster(?:ed)?|version|edit|mix|remix)[^)]*\\)",
                RegexOption.IGNORE_CASE
            ),
            ""
        )
        .replace(
            Regex(
                "\\s*-\\s*(?:official\\s*)?(?:music\\s*)?(?:video|mv|lyrics?|audio|visualizer|live|remaster(?:ed)?|version|edit|mix|remix)\\b.*$",
                RegexOption.IGNORE_CASE
            ),
            ""
        )
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('-')
}

private fun normalizeCanvasArtistName(raw: String): String {
    val first = raw
        .split(
            Regex(
                "(?:\\s*,\\s*|\\s*&\\s*|\\s+×\\s+|\\s+x\\s+|\\bfeat\\.?\\b|\\bft\\.?\\b|\\bfeaturing\\b|\\bwith\\b)",
                RegexOption.IGNORE_CASE
            ),
            limit = 2
        )
        .firstOrNull()
        .orEmpty()

    return first
        .replace(Regex("\\s+"), " ")
        .trim()
}
/*
 * Copyright (C) OuterTune Project
 * Custom SnapLayoutInfoProvider idea belongs to OuterTune
 */

// SnapLayoutInfoProvider
@ExperimentalFoundationApi
fun SnapLayoutInfoProvider(
    lazyGridState: LazyGridState,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float = { layoutSize, itemSize ->
        (layoutSize / 2f - itemSize / 2f)
    },
    velocityThreshold: Float = 1000f,
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
    private val layoutInfo: LazyGridLayoutInfo
        get() = lazyGridState.layoutInfo

    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float = 0f
    override fun calculateSnapOffset(velocity: Float): Float {
        val bounds = calculateSnappingOffsetBounds()

        // Only snap when velocity exceeds threshold
        if (abs(velocity) < velocityThreshold) {
            if (abs(bounds.start) < abs(bounds.endInclusive))
                return bounds.start

            return bounds.endInclusive
        }

        return when {
            velocity < 0 -> bounds.start
            velocity > 0 -> bounds.endInclusive
            else -> 0f
        }
    }

    fun calculateSnappingOffsetBounds(): ClosedFloatingPointRange<Float> {
        var lowerBoundOffset = Float.NEGATIVE_INFINITY
        var upperBoundOffset = Float.POSITIVE_INFINITY

        layoutInfo.visibleItemsInfo.fastForEach { item ->
            val offset = calculateDistanceToDesiredSnapPosition(layoutInfo, item, positionInLayout)

            // Find item that is closest to the center
            if (offset <= 0 && offset > lowerBoundOffset) {
                lowerBoundOffset = offset
            }

            // Find item that is closest to center, but after it
            if (offset >= 0 && offset < upperBoundOffset) {
                upperBoundOffset = offset
            }
        }

        return lowerBoundOffset.rangeTo(upperBoundOffset)
    }
}

fun calculateDistanceToDesiredSnapPosition(
    layoutInfo: LazyGridLayoutInfo,
    item: LazyGridItemInfo,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float,
): Float {
    val containerSize =
        layoutInfo.singleAxisViewportSize - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding

    val desiredDistance = positionInLayout(containerSize.toFloat(), item.size.width.toFloat())
    val itemCurrentPosition = item.offset.x.toFloat()

    return itemCurrentPosition - desiredDistance
}

private val LazyGridLayoutInfo.singleAxisViewportSize: Int
    get() = if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width
