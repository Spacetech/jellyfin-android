package org.jellyfin.mobile.model.dto

import androidx.compose.runtime.Immutable
import org.jellyfin.apiclient.model.entities.ImageType

@Immutable
data class ArtistInfo(
    val id: String,
    val name: String,
    val imageTags: Map<ImageType, String>,
)
