package org.jellyfin.mobile.ui.utils

import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

interface GridScope {
    fun Modifier.fillItemMaxWidth(): Modifier
}

@Composable
fun <T> GridListFor(
    items: List<T>,
    modifier: Modifier = Modifier,
    numberOfColumns: Int = 2,
    contentPadding: InnerPadding = InnerPadding(0.dp),
    horizontalGravity: Alignment.Horizontal = Alignment.CenterHorizontally,
    itemContent: @Composable GridScope.(T) -> Unit
) {
    WithConstraints {
        val maxItemWidth = with(DensityAmbient.current) {
            constraints.maxWidth.toDp() / numberOfColumns
        }
        val gridScope = GridScopeImpl(maxItemWidth)

        // TODO: we really want an actual grid composable here
        LazyColumnFor(
            items = items.chunked(numberOfColumns),
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalGravity = horizontalGravity,
        ) { row ->
            Row(Modifier.fillParentMaxWidth()) {
                row.fastForEach { info ->
                    gridScope.itemContent(info)
                }
            }
        }
    }
}

private data class GridScopeImpl(val maxWidth: Dp) : GridScope {
    override fun Modifier.fillItemMaxWidth() = width(maxWidth)
}
