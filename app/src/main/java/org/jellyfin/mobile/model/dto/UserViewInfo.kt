package org.jellyfin.mobile.model.dto

import androidx.compose.runtime.Immutable
import org.jellyfin.apiclient.model.entities.ImageType

@Immutable
data class UserViewInfo(
    val id: String,
    val name: String,
    val collectionType: String,
    val imageTags: Map<ImageType, String>,
)
