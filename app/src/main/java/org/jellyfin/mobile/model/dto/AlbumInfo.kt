package org.jellyfin.mobile.model.dto

import androidx.compose.runtime.Immutable
import org.jellyfin.apiclient.model.entities.ImageType

@Immutable
data class AlbumInfo(
    val id: String,
    val name: String,
    val artist: String,
    val imageTags: Map<ImageType, String>,
)
