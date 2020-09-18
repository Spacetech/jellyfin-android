package org.jellyfin.mobile.ui.screen.library.music

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jellyfin.apiclient.model.entities.ImageType
import org.jellyfin.mobile.R
import org.jellyfin.mobile.model.dto.ArtistInfo
import org.jellyfin.mobile.ui.DefaultCornerRounding
import org.jellyfin.mobile.ui.utils.ApiImage
import org.jellyfin.mobile.ui.utils.GridListFor
import timber.log.Timber

@Composable
fun ArtistList(viewModel: MusicViewModel) {
    GridListFor(
        items = viewModel.artists,
        numberOfColumns = 3,
        contentPadding = InnerPadding(top = 8.dp, bottom = 8.dp),
    ) { artist ->
        Artist(artistInfo = artist, modifier = Modifier.fillItemMaxWidth(), onClick = {
            Timber.d("Clicked ${artist.name}")
        })
    }
}

@Stable
@Composable
fun Artist(artistInfo: ArtistInfo, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .clip(DefaultCornerRounding)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        WithConstraints {
            val imageSize = with(DensityAmbient.current) { constraints.maxWidth.toDp() }
            ApiImage(
                id = artistInfo.id,
                modifier = Modifier.size(imageSize).clip(DefaultCornerRounding),
                imageType = ImageType.Primary,
                imageTags = artistInfo.imageTags,
                fallback = {
                    VectorPainter(asset = vectorResource(R.drawable.fallback_image_person))
                },
            )
        }
        Text(
            text = artistInfo.name,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}
