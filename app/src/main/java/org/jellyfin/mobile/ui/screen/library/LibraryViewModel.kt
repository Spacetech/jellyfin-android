package org.jellyfin.mobile.ui.screen.library

import androidx.lifecycle.ViewModel
import org.jellyfin.mobile.model.dto.UserViewInfo

abstract class LibraryViewModel(protected val viewInfo: UserViewInfo) : ViewModel()
