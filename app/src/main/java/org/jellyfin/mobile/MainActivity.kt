package org.jellyfin.mobile

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.OrientationEventListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import org.jellyfin.mobile.bridge.ExternalPlayer
import org.jellyfin.mobile.cast.Chromecast
import org.jellyfin.mobile.utils.*
import org.jellyfin.mobile.webapp.ConnectionHelper
import org.jellyfin.mobile.webapp.RemotePlayerService
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    val appPreferences: AppPreferences by inject()
    val permissionRequestHelper: PermissionRequestHelper by inject()
    val chromecast = Chromecast()
    private val connectionHelper = ConnectionHelper(this)
    private val externalPlayer = ExternalPlayer(this@MainActivity)

    val rootView: CoordinatorLayout by lazyView(R.id.root_view)

    var serviceBinder: RemotePlayerService.ServiceBinder? = null
        private set
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            serviceBinder = binder as? RemotePlayerService.ServiceBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            serviceBinder?.run { webViewController = null }
        }
    }

    private val orientationListener: OrientationEventListener by lazy { SmartOrientationListener(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle window insets
        setStableLayoutFlags()

        // Load content
        connectionHelper.initialize()

        chromecast.initializePlugin(this)
    }

    override fun onStart() {
        super.onStart()
        orientationListener.enable()
    }

    fun updateRemoteVolumeLevel(value: Int) {
        serviceBinder?.run { remoteVolumeProvider.currentVolume = value }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.HANDLE_EXTERNAL_PLAYER) {
            externalPlayer.handleActivityResult(resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) = permissionRequestHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults)

    override fun onStop() {
        super.onStop()
        orientationListener.disable()
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        serviceBinder?.webViewController = null
        chromecast.destroy()
        super.onDestroy()
    }
}
