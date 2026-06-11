package com.lonx.lyrico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.lyrico.data.model.entity.SongEntity
import com.lonx.lyrico.data.model.search.LocalLyricSearchResult
import com.lonx.lyrico.data.model.search.LocalSearchUiState
import com.lonx.lyrico.data.repository.LibraryIndexRepository
import com.lonx.lyrico.data.song.search.SongSearchRepository
import com.lonx.lyrico.utils.LyricsSearchTextExtractor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LocalSearchViewModel(
    private val songSearchRepository: SongSearchRepository,
    private val libraryIndexRepository: LibraryIndexRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            songSearchRepository.rebuildMissingLyricSearchTextIndex()
        }
    }

    val searchQuery: StateFlow<String> = query

    val uiState: StateFlow<LocalSearchUiState> = query
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { keyword ->
            if (keyword.isBlank()) {
                flowOf(LocalSearchUiState(query = keyword))
            } else {
                combine(
                    songSearchRepository.searchSongsForLocalSearch(keyword),
                    libraryIndexRepository.searchAlbums(keyword),
                    libraryIndexRepository.searchArtists(keyword),
                    songSearchRepository.searchLyricsForLocalSearch(keyword)
                ) { songs, albums, artists, lyricSongs ->
                    LocalSearchUiState(
                        query = keyword,
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        lyricMatches = lyricSongs.mapNotNull { song ->
                            findLyricMatch(keyword, song)
                        }
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LocalSearchUiState()
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    private fun findLyricMatch(
        keyword: String,
        song: SongEntity
    ): LocalLyricSearchResult? {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank()) return null

        val matchedLine = searchableLyricLines(song)
            .firstOrNull { line -> line.contains(normalizedKeyword, ignoreCase = true) }
            ?: return null

        return LocalLyricSearchResult(
            song = song,
            lyricLine = matchedLine
        )
    }

    private fun searchableLyricLines(song: SongEntity): List<String> {
        return (
            song.lyricSearchText
                .orEmpty()
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList() + LyricsSearchTextExtractor.extractLines(song.lyrics)
            )
            .distinct()
    }
}
