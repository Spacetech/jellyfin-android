package org.jellyfin.mobile.controller

import org.koin.dsl.module

val controllerModule = module {
    single { ServerController(get(), get(), get()) }
    single { LibraryController(get()) }
}
