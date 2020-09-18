package org.jellyfin.mobile.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jellyfin.apiclient.Jellyfin
import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.apiclient.model.dto.UserDto
import org.jellyfin.apiclient.model.system.PublicSystemInfo
import org.jellyfin.mobile.R
import org.jellyfin.mobile.model.dto.UserInfo
import org.jellyfin.mobile.model.sql.dao.ServerDao
import org.jellyfin.mobile.model.state.CheckUrlState
import org.jellyfin.mobile.model.state.LoginState
import org.jellyfin.mobile.utils.PRODUCT_NAME_SUPPORTED_SINCE
import org.jellyfin.mobile.utils.authenticateUser
import org.jellyfin.mobile.utils.getPublicSystemInfo
import org.jellyfin.mobile.utils.getUserInfo

class ServerController(
    private val jellyfin: Jellyfin,
    private val apiClient: ApiClient,
    private val serverDao: ServerDao,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var loginState by mutableStateOf(LoginState.PENDING)
    var userInfo by mutableStateOf<UserInfo?>(null)

    init {
        scope.launch {
            val serverInfo = withContext(Dispatchers.IO) {
                serverDao.queryLastUsedSever()
            }
            loginState = if (serverInfo != null) {
                apiClient.ChangeServerLocation(serverInfo.hostname)
                apiClient.SetAuthenticationInfo(serverInfo.accessToken, serverInfo.userId)
                userInfo = apiClient.getUserInfo(serverInfo.userId)?.toUserInfo()
                LoginState.LOGGED_IN
            } else {
                LoginState.NOT_LOGGED_IN
            }
        }
    }

    suspend fun checkServerUrl(hostname: String): CheckUrlState {
        val urls = jellyfin.discovery.addressCandidates(hostname)
        var httpUrl: HttpUrl? = null
        var serverInfo: PublicSystemInfo? = null
        loop@ for (url in urls) {
            httpUrl = url.toHttpUrlOrNull()

            if (httpUrl == null) {
                return CheckUrlState.Error(R.string.connection_error_invalid_format)
            }

            // Set API client address
            apiClient.ChangeServerLocation(httpUrl.toString())

            serverInfo = apiClient.getPublicSystemInfo()
            if (serverInfo != null)
                break@loop
        }

        if (httpUrl == null || serverInfo == null) {
            return CheckUrlState.Error()
        }

        val version = serverInfo.version
            .split('.')
            .mapNotNull(String::toIntOrNull)

        val isValidInstance = when {
            version.size != 3 -> false
            version[0] == PRODUCT_NAME_SUPPORTED_SINCE.first && version[1] < PRODUCT_NAME_SUPPORTED_SINCE.second -> true // Valid old version
            else -> true // FIXME: check ProductName once API client supports it
        }

        return if (isValidInstance) CheckUrlState.Success else CheckUrlState.Error()
    }

    suspend fun authenticate(username: String, password: String): Boolean {
        requireNotNull(apiClient.serverAddress) { "Server address not set" }
        val authResult = apiClient.authenticateUser(username, password)
        if (authResult != null) {
            val user = authResult.user
            withContext(Dispatchers.IO) {
                serverDao.insert(apiClient.serverAddress, user.id, authResult.accessToken)
            }
            userInfo = user?.toUserInfo()
            loginState = LoginState.LOGGED_IN
            return true
        }
        return false
    }

    fun tryLogout() {
        scope.launch { logout() }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            serverDao.logout(apiClient.serverAddress)
        }
        apiClient.ChangeServerLocation(null)
        loginState = LoginState.NOT_LOGGED_IN
        userInfo = null
    }
}

private fun UserDto.toUserInfo() = UserInfo(id, name, primaryImageTag)