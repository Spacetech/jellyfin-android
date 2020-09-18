package org.jellyfin.mobile.model.dto

import androidx.compose.runtime.Immutable
import org.jellyfin.apiclient.model.entities.ImageType

@Immutable
data class SongInfo(
    val id: String,
    val albumId: String,
    val name: String,
    val artist: String,
    val imageTags: Map<ImageType, String>,
)
