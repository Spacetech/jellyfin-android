package org.jellyfin.mobile.ui.screen.library.music

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.apiclient.model.dto.BaseItemDto
import org.jellyfin.apiclient.model.dto.BaseItemType
import org.jellyfin.apiclient.model.entities.CollectionType
import org.jellyfin.apiclient.model.querying.ArtistsQuery
import org.jellyfin.apiclient.model.querying.ItemFields
import org.jellyfin.apiclient.model.querying.ItemQuery
import org.jellyfin.mobile.model.dto.AlbumInfo
import org.jellyfin.mobile.model.dto.ArtistInfo
import org.jellyfin.mobile.model.dto.SongInfo
import org.jellyfin.mobile.model.dto.UserViewInfo
import org.jellyfin.mobile.ui.screen.library.LibraryViewModel
import org.jellyfin.mobile.utils.getAlbumArtists
import org.jellyfin.mobile.utils.getItems
import org.koin.core.KoinComponent
import org.koin.core.inject

class MusicViewModel(viewInfo: UserViewInfo) : LibraryViewModel(viewInfo), KoinComponent {
    private val apiClient: ApiClient by inject()

    val currentTab = mutableStateOf(0)
    val albums = mutableStateListOf<AlbumInfo>()
    val artists = mutableStateListOf<ArtistInfo>()
    val songs = mutableStateListOf<SongInfo>()

    init {
        require(viewInfo.collectionType == CollectionType.Music) {
            "Invalid ViewModel for collection type ${viewInfo.collectionType}"
        }

        viewModelScope.launch {
            launch {
                apiClient.getItems(buildItemQuery(viewInfo, BaseItemType.MusicAlbum))?.run {
                    albums += items.map(BaseItemDto::toAlbumInfo)
                }
            }
            launch {
                apiClient.getAlbumArtists(buildArtistsItemQuery(viewInfo))?.run {
                    artists += items.map(BaseItemDto::toArtistInfo)
                }
            }
            launch {
                apiClient.getItems(buildItemQuery(viewInfo, BaseItemType.Audio))?.run {
                    songs += items.map(BaseItemDto::toSongInfo)
                }
            }
        }
    }

    private fun buildItemQuery(viewInfo: UserViewInfo, itemType: BaseItemType) = ItemQuery().apply {
        parentId = viewInfo.id
        includeItemTypes = arrayOf(itemType.name)
        recursive = true
        sortBy = arrayOf(ItemFields.SortName.name)
        startIndex = 0
        limit = 100
    }

    private fun buildArtistsItemQuery(viewInfo: UserViewInfo) = ArtistsQuery().apply {
        parentId = viewInfo.id
        recursive = true
        sortBy = arrayOf(ItemFields.SortName.name)
        startIndex = 0
        limit = 100
    }
}

private fun BaseItemDto.toAlbumInfo() = AlbumInfo(id, name.orEmpty(), albumArtist.orEmpty(), imageTags)
private fun BaseItemDto.toArtistInfo() = ArtistInfo(id, name.orEmpty(), imageTags)
private fun BaseItemDto.toSongInfo() = SongInfo(id, albumId, name.orEmpty(), artists?.joinToString().orEmpty(), imageTags)
