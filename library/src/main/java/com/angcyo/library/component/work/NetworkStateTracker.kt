package com.angcyo.library.component.work

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.ConnectivityManagerCompat
import com.angcyo.library.L
import com.angcyo.library.L.d
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.havePermission

/**
 * A [ConstraintTracker] for monitoring network state.
 *
 *
 * For API 24 and up: Network state is tracked using a registered [NetworkCallback] with
 * [ConnectivityManager.registerDefaultNetworkCallback], added in API 24.
 *
 *
 * For API 23 and below: Network state is tracked using a [android.content.BroadcastReceiver].
 * Much less efficient than tracking with [NetworkCallback]s and [ConnectivityManager].
 *
 *
 * Based on [android.app.job.JobScheduler]'s ConnectivityController on API 26.
 * {@see https://android.googlesource.com/platform/frameworks/base/+/oreo-release/services/core/java/com/android/server/job/controllers/ConnectivityController.java}
 */
class NetworkStateTracker(context: Context, taskExecutor: TaskExecutor) :
    ConstraintTracker<NetworkState?>(context, taskExecutor) {

    // Synthetic Accessor
    private val connectivityManager: ConnectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: NetworkStateCallback? = null

    private var broadcastReceiver: NetworkStateBroadcastReceiver? = null

    override val initialState: NetworkState
        get() = activeNetworkState

    // Use getActiveNetworkInfo() instead of getNetworkInfo(network) because it can detect VPNs.

    /* synthetic access */
    val activeNetworkState: NetworkState
        get() {
            // Use getActiveNetworkInfo() instead of getNetworkInfo(network) because it can detect VPNs.
            if (lastContext.havePermission(listOf(Manifest.permission.ACCESS_NETWORK_STATE))) {
                val info = connectivityManager.activeNetworkInfo
                val isConnected = info != null && info.isConnected
                val isValidated = isActiveNetworkValidated
                val isMetered =
                    ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager)
                val isNotRoaming = info != null && !info.isRoaming
                return NetworkState(isConnected, isValidated, isMetered, isNotRoaming)
            } else {
                return NetworkState(false, false, false, false)
            }
        }

    // NET_CAPABILITY_VALIDATED not available until API 23. Used on API 26+.
    private val isActiveNetworkValidated: Boolean
        get() {
            if (Build.VERSION.SDK_INT < 23 || !lastContext.havePermission(listOf(Manifest.permission.ACCESS_NETWORK_STATE))) {
                return false // NET_CAPABILITY_VALIDATED not available until API 23. Used on API 26+.
            }
            val network = connectivityManager.activeNetwork
            val capabilities =
                connectivityManager.getNetworkCapabilities(network)
            return (capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        }

    /**
     * Create an instance of [NetworkStateTracker]
     *
     * @param context      the application [Context]
     * @param taskExecutor The internal [TaskExecutor] being used by WorkManager.
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = NetworkStateCallback()
        } else {
            broadcastReceiver = NetworkStateBroadcastReceiver()
        }
    }

    override fun startTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                d("Registering network callback")
                connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
            } catch (e: IllegalArgumentException) {
                // This seems to be happening on NVIDIA Shield K1 Tablets.  Catching the
                // exception since and moving on.  See b/136569342.
                L.e("Received exception while unregistering network callback", e)
            }
        } else {
            d("Registering broadcast receiver")
            appContext.registerReceiver(
                broadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun stopTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                d("Unregistering network callback")
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            } catch (e: IllegalArgumentException) {
                // This seems to be happening on NVIDIA Shield Tablets a lot.  Catching the
                // exception since it's not fatal and moving on.  See b/119484416.
                L.e("Received exception while unregistering network callback", e)
            }
        } else {
            d("Unregistering broadcast receiver")
            appContext.unregisterReceiver(broadcastReceiver)
        }
    }

    @RequiresApi(24)
    private inner class NetworkStateCallback : NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            // The Network parameter is unreliable when a VPN app is running - use active network.
            d(String.format("Network capabilities changed: %s", capabilities))
            setState(activeNetworkState)
        }

        override fun onLost(network: Network) {
            d("Network connection lost")
            setState(activeNetworkState)
        }
    }

    private inner class NetworkStateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                d("Network broadcast received")
                setState(activeNetworkState)
            }
        }
    }
}