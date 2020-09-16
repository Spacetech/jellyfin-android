package org.jellyfin.mobile.ui

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.jellyfin.mobile.R

val DefaultCornerRounding = RoundedCornerShape(8.dp)

@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String,
    titleFont: FontFamily? = null,
    canGoBack: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (InnerPadding) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (canGoBack) {
                TopAppBar(
                    title = {
                        Text(text = title, fontFamily = titleFont)
                    },
                    navigationIcon = {
                        ToolbarBackButton()
                    },
                    actions = actions,
                    backgroundColor = MaterialTheme.colors.primary,
                )
            } else {
                TopAppBar(
                    title = {
                        Text(text = title, fontFamily = titleFont)
                    },
                    actions = actions,
                    backgroundColor = MaterialTheme.colors.primary,
                )
            }
        },
        bodyContent = content,
    )
}

@Composable
fun ToolbarBackButton() {
    val backStack = BackStackAmbient.current
    IconButton(
        onClick = {
            backStack.pop()
        },
    ) {
        Icon(
            asset = vectorResource(R.drawable.ic_arrow_back_white_24dp),
        )
    }
}

@Composable
inline fun CenterRow(
    children: @Composable RowScope.() -> Unit
) = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalGravity = Alignment.CenterVertically,
    children = children,
)

@Composable
fun ChipletButton(
    text: String,
    onClick: () -> Unit
) = OutlinedButton(
    onClick = onClick,
    modifier = Modifier.padding(8.dp),
    shape = CircleShape,
) {
    Text(text = text, color = MaterialTheme.colors.onSurface)
}
