package org.jellyfin.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.mobile.AppPreferences
import org.jellyfin.mobile.model.sql.dao.ServerDao
import org.jellyfin.mobile.model.sql.dao.UserDao
import org.jellyfin.mobile.model.sql.entity.ServerEntity

class MainViewModel(
    app: Application,
    private val appPreferences: AppPreferences,
    private val apiClient: ApiClient,
    private val serverDao: ServerDao,
    private val userDao: UserDao,
) : AndroidViewModel(app) {
    private val _serverState: MutableStateFlow<ServerState> = MutableStateFlow(ServerState.Pending)
    val serverState: StateFlow<ServerState> get() = _serverState

    init {
        viewModelScope.launch {
            serverState.collect { state ->
                apiClient.ChangeServerLocation(state.server?.hostname?.trimEnd('/'))
            }
        }

        viewModelScope.launch {
            // Migrate from preferences if necessary
            appPreferences.instanceUrl?.let { url ->
                setupServer(url)
                appPreferences.instanceUrl = null
            }

            refreshServer()
        }
    }

    suspend fun setupServer(hostname: String) {
        appPreferences.currentServerId = withContext(Dispatchers.IO) {
            serverDao.insert(hostname)
        }
    }

    suspend fun refreshServer() {
        val server = withContext(Dispatchers.IO) {
            val serverId = appPreferences.currentServerId ?: return@withContext null
            serverDao.getServer(serverId)
        }

        _serverState.value = server?.let { ServerState.Available(it) } ?: ServerState.Unset
    }

    suspend fun setupUser(serverId: Long, userId: String, accessToken: String) {
        appPreferences.currentUserId = withContext(Dispatchers.IO) {
            userDao.insert(serverId, userId, accessToken)
        }
        apiClient.SetAuthenticationInfo(accessToken, userId)
    }
}

sealed class ServerState {
    open val server: ServerEntity? = null

    object Pending : ServerState()
    object Unset : ServerState()
    class Available(override val server: ServerEntity) : ServerState()
}
