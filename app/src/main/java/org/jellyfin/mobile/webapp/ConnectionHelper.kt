package org.jellyfin.mobile.webapp

import androidx.fragment.app.Fragment
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jellyfin.mobile.AppPreferences
import org.jellyfin.mobile.MainActivity
import org.jellyfin.mobile.R
import org.jellyfin.mobile.fragment.ConnectFragment
import org.jellyfin.mobile.fragment.WebViewFragment
import org.jellyfin.mobile.utils.requestNoBatteryOptimizations
import org.koin.core.KoinComponent

class ConnectionHelper(private val activity: MainActivity) : KoinComponent {
    private val appPreferences: AppPreferences get() = activity.appPreferences

    var connected = false
        private set

    fun initialize() {
        appPreferences.instanceUrl?.toHttpUrlOrNull().also { url ->
            if (url != null) {
                setFragment(WebViewFragment()) {
                    loadUrl(url.toString())
                }
            } else {
                setFragment(ConnectFragment())
            }
        }
    }

    private fun <T : Fragment> setFragment(fragment: T, init: (T.() -> Unit)? = null) {
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_overlay, fragment)
            .commit()

        init?.let { fragment.it() }
    }

    fun onConnectedToWebapp() {
        connected = true
        activity.requestNoBatteryOptimizations()
    }

    fun onSelectServer() {
        setFragment(ConnectFragment())
    }

    fun onErrorReceived() {
        connected = false
        // TODO
        //showConnectionError()
        onSelectServer()
    }
}
