package org.jellyfin.mobile.fragment

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.mobile.R
import org.jellyfin.mobile.WEBAPP_FUNCTION_CHANNEL
import org.jellyfin.mobile.bridge.NativeInterface
import org.jellyfin.mobile.bridge.NativePlayer
import org.jellyfin.mobile.bridge.WebViewLoader
import org.jellyfin.mobile.databinding.FragmentWebviewBinding
import org.jellyfin.mobile.utils.*
import org.jellyfin.mobile.webapp.ConnectionHelper
import org.jellyfin.mobile.webapp.RemotePlayerService
import org.jellyfin.mobile.webapp.WebViewController
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import timber.log.Timber
import java.io.Reader
import java.util.*

class WebViewFragment : Fragment(), WebViewController {
    // DI
    private val webappFunctionChannel: Channel<String> by inject(named(WEBAPP_FUNCTION_CHANNEL))
    private val connectionHelper: ConnectionHelper by inject()
    private val apiClient: ApiClient by inject()

    // UI
    private lateinit var webViewBinding: FragmentWebviewBinding
    private val webViewRoot: View get() = webViewBinding.root
    private val webView: WebView get() = webViewBinding.webView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        webViewBinding = FragmentWebviewBinding.inflate(layoutInflater, container, false)

        return webViewRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.doOnNextLayout { webView ->
                // Maximum allowed exclusion rect height is 200dp,
                // offsetting 100dp from the center in both directions
                // uses the maximum available space
                val verticalCenter = webView.measuredHeight / 2
                val offset = requireContext().dip(100)

                // Arbitrary, currently 2x minimum touch target size
                val exclusionWidth = requireContext().dip(96)

                webView.systemGestureExclusionRects = listOf(
                    Rect(
                        0,
                        verticalCenter - offset,
                        exclusionWidth,
                        verticalCenter + offset
                    )
                )
            }
        }

        // Setup WebView
        webView.initialize()

        // Process JS functions called from other components (e.g. the PlayerActivity)
        lifecycleScope.launch {
            for (function in webappFunctionChannel) {
                loadUrl("javascript:$function")
            }
        }

        // Bind player service
//        requireContext().bindService(Intent(this, RemotePlayerService::class.java), serviceConnection, Service.BIND_AUTO_CREATE)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.initialize() {
        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.theme_background))
        webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url
                val path = url.path?.toLowerCase(Locale.ROOT) ?: return null
                return when {
                    path.endsWith(Constants.APPLOADER_PATH) -> {
                        //runOnUiThread {
                            webView.evaluateJavascript(JS_INJECTION_CODE) {
                                connectionHelper.onConnectedToWebapp()
                            }
                        //}
                        null // continue loading normally
                    }
                    path.contains("native") -> requireContext().loadAsset("native/${url.lastPathSegment}")
                    path.endsWith(Constants.SELECT_SERVER_PATH) -> {
                        //runOnUiThread {
                            connectionHelper.onSelectServer()
                        //}
                        emptyResponse
                    }
                    path.endsWith(Constants.SESSION_CAPABILITIES_PATH) -> {
                        //runOnUiThread {
                            webView.evaluateJavascript("window.localStorage.getItem('jellyfin_credentials')") { result ->
                                try {
                                    val credentials = JSONObject(result.unescapeJson())
                                    val server = credentials.getJSONArray("Servers").getJSONObject(0)
                                    val address = server.getString("ManualAddress")
                                    val user = server.getString("UserId")
                                    val token = server.getString("AccessToken")
                                    apiClient.ChangeServerLocation(address)
                                    apiClient.SetAuthenticationInfo(token, user)
                                } catch (e: JSONException) {
                                    Timber.e(e, "Failed to extract apiclient credentials")
                                }
                            }
                        //}
                        null
                    }
                    else -> null
                }
            }

            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                val errorMessage = errorResponse.data?.run { bufferedReader().use(Reader::readText) }
                Timber.e("Received WebView HTTP %d error: %s", errorResponse.statusCode, errorMessage)
                if (request.url.path?.endsWith(Constants.INDEX_PATH) != false)
                    //runOnUiThread {
                        connectionHelper.onErrorReceived()
                    //}
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat) {
                val description = if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) error.description else null

                Timber.e("Received WebView error at %s: %s", request.url.toString(), description)
            }
        }
        webChromeClient = WebChromeClient()
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
//        addJavascriptInterface(NativeInterface(this), "NativeInterface")
//        addJavascriptInterface(NativePlayer(this), "NativePlayer")
//        addJavascriptInterface(externalPlayer, "ExternalPlayer")
        addJavascriptInterface(WebViewLoader(connectionHelper), "WebViewLoader")
    }

    override fun loadUrl(url: String) = webView.loadUrl(url)
}
