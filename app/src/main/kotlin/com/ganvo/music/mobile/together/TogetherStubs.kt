package com.ganvo.music.mobile.together

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Minimal stubs to keep "Together" references compiling while disabling functionality.

// Domain models
data class TogetherTrack(
    val id: String,
    val title: String,
    val artists: List<String> = emptyList(),
    val durationSec: Int = -1,
    val thumbnailUrl: String? = null,
)

enum class AddTrackMode {
    PLAY_NEXT,
    ADD_TO_QUEUE,
}

sealed class TogetherRole {
    object Host : TogetherRole()
    object Guest : TogetherRole()
}

data class TogetherRoomSettings(
    val allowGuestsToAddTracks: Boolean = false,
    val allowGuestsToControlPlayback: Boolean = false,
)

data class TogetherParticipant(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val isPending: Boolean = false,
    val isConnected: Boolean = false,
)

data class TogetherRoomState(
    val sessionId: String,
    val hostId: String,
    val participants: List<TogetherParticipant> = emptyList(),
    val settings: TogetherRoomSettings = TogetherRoomSettings(),
    val queue: List<TogetherTrack> = emptyList(),
    val queueHash: String = "",
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val repeatMode: Int = 0,
    val shuffleEnabled: Boolean = false,
    val sentAtElapsedRealtimeMs: Long = 0L,
)

// Session states (kept minimal)
sealed class TogetherSessionState {
    object Idle : TogetherSessionState()
    data class Joining(val deepLink: Any?) : TogetherSessionState()
    data class JoiningOnline(val code: String) : TogetherSessionState()
    data class Joined(
        val role: TogetherRole,
        val sessionId: String,
        val selfParticipantId: String? = null,
        val roomState: TogetherRoomState? = null,
    ) : TogetherSessionState()
    data class Hosting(val sessionId: String) : TogetherSessionState()
    data class HostingOnline(val sessionId: String, val code: String) : TogetherSessionState()
    data class Error(val message: String, val recoverable: Boolean = true) : TogetherSessionState()
}

// Events produced by a client (kept minimal)
sealed class TogetherClientEvent {
    data class Welcome(val welcome: WelcomeInfo) : TogetherClientEvent()
    data class RoomState(val state: TogetherRoomState) : TogetherClientEvent()
    data class JoinDecision(val decision: JoinDecisionInfo) : TogetherClientEvent()
    data class ServerIssue(val code: String?, val message: String) : TogetherClientEvent()
    data class HeartbeatPong(val pong: HeartbeatPongInfo, val receivedAtElapsedRealtimeMs: Long) : TogetherClientEvent()
    data class Error(val message: String) : TogetherClientEvent()
    object Disconnected : TogetherClientEvent()

    data class WelcomeInfo(val participantId: String, val isPending: Boolean, val settings: TogetherRoomSettings)
    data class JoinDecisionInfo(val approved: Boolean)
    data class HeartbeatPongInfo(val clientElapsedRealtimeMs: Long, val serverElapsedRealtimeMs: Long)
}

// Planner op
sealed class TogetherGuestOp {
    data class Control(val action: Any) : TogetherGuestOp()
    data class AddTrack(val track: TogetherTrack, val mode: AddTrackMode) : TogetherGuestOp()
}

object TogetherGuestPlaybackPlanner {
    fun planPlayTrackNow(roomState: TogetherRoomState, track: TogetherTrack, positionMs: Long, playWhenReady: Boolean): List<TogetherGuestOp> = emptyList()
}

// Simple client stub that exposes an events flow and a no-op connect
class TogetherClient(private val scope: CoroutineScope, val clientId: String = "") {
    private val _events = MutableSharedFlow<TogetherClientEvent>(replay = 0)
    val events: Flow<TogetherClientEvent> = _events.asSharedFlow()

    suspend fun connect(joinInfo: Any, displayName: String) {
        // no-op: feature disabled
    }

    suspend fun disconnect() {
        // no-op
    }
}

// Server / host stubs
class TogetherServer
class TogetherOnlineHost
class TogetherClock

// Online API stubs
data class TogetherOnlineCreateSessionResponse(
    val sessionId: String,
    val code: String,
    val hostKey: String,
    val guestKey: String,
    val wsUrl: String,
    val settings: TogetherRoomSettings,
)

class TogetherOnlineApi(private val baseUrl: String, private val bearerToken: String? = null) {
    suspend fun createSession(hostDisplayName: String, settings: TogetherRoomSettings): TogetherOnlineCreateSessionResponse {
        throw UnsupportedOperationException("Together online hosting is disabled in this build")
    }

    suspend fun resolveCode(code: String): Any {
        throw UnsupportedOperationException("Together online hosting is disabled in this build")
    }
}

object TogetherOnlineEndpoint {
    suspend fun baseUrlOrNull(dataStore: Any?): String? = null
    fun onlineWebSocketUrlOrNull(rawWsUrl: String, baseUrl: String): String? = null
}

// Link helper stub
object TogetherLink {
    fun decode(raw: String): Any? = null
    fun encode(info: Any): String = ""
}
