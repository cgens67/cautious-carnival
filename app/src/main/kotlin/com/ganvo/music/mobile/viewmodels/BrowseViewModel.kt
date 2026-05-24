/*
 * OpenTune Project Original (2026)
 * ganvo (github.com/ganvo)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.ganvo.music.mobile.viewmodels
 
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganvo.music.mobile.db.MusicDatabase
import com.ganvo.music.mobile.utils.reportException
import com.ganvo.music.mobile.innertube.YouTube
import com.ganvo.music.mobile.innertube.models.AlbumItem
import com.ganvo.music.mobile.innertube.models.PlaylistItem
import com.ganvo.music.mobile.innertube.models.YTItem
import com.ganvo.music.mobile.innertube.utils.completed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
 
@HiltViewModel
class BrowseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val browseId: String? = savedStateHandle.get<String>("browseId")
 
    val items = MutableStateFlow<List<YTItem>?>(emptyList())
    val title = MutableStateFlow<String?>("")
 
    init {
        viewModelScope.launch {
            browseId?.let {
                YouTube.browse(browseId, null).onSuccess { result ->
                    // Store the title
                    title.value = result.title
 
                    // Flatten the nested structure to get all YTItems
                    val allItems = result.items.flatMap { it.items }
                    items.value = allItems
                }.onFailure {
                    reportException(it)
                }
            }
        }
    }
}
