package com.angcyo.core.component.model

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.ViewModel
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.runOnMainThread
import com.angcyo.viewmodel.updateThis
import com.angcyo.viewmodel.updateValue
import com.angcyo.viewmodel.vmData
import com.angcyo.viewmodel.vmDataNull
import com.angcyo.viewmodel.vmDataOnce

/** 网络服务发现
 * https://developer.android.com/training/connect-devices-wirelessly/nsd?hl=zh-cn
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/12/25
 */
class NsdModel : ViewModel() {

    val nsdManager: NsdManager by lazy {
        app().getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    /**服务是否注册成功*/
    val registerStateData = vmData(false)

    /**是否正在发现服务*/
    val discoveryInfoData = vmDataNull<DiscoverServicesInfo>(null)

    /**发送服务通知*/
    val serviceFoundOnceData = vmDataOnce<NsdServiceInfo>()

    val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            L.d("解析服务失败:$errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            L.d("解析服务成功:$serviceInfo")
        }

        override fun onResolutionStopped(serviceInfo: NsdServiceInfo) {
            L.d("停止解析:$serviceInfo")
        }

        override fun onStopResolutionFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            L.d("停止解析失败:$errorCode")
        }
    }

    val registrationListener = object : NsdManager.RegistrationListener {
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            L.d("注册服务失败:$serviceInfo :$errorCode")
            registerStateData.updateValue(false)
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            L.d("取消注册服务失败:$serviceInfo :$errorCode")
        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            L.d("注册服务成功:$serviceInfo")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            L.d("取消注册服务成功:$serviceInfo")
            registerStateData.updateValue(false)
        }
    }

    //Added in API level 34ServiceInfoCallback
    /*val serviceInfoCallback = object : NsdManager.ServiceInfoCallback {
        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
            L.d("服务信息注册失败:$errorCode")
        }

        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
            L.d("服务信息更新:$serviceInfo")
        }

        override fun onServiceLost() {
            L.d("服务信息丢失.")
        }

        override fun onServiceInfoCallbackUnregistered() {
            L.d("服务信息取消注册.")
        }
    }*/

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            L.d("开始发现服务:${regType}")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            L.d("发现服务:$service")
            runOnMainThread {
                serviceFoundOnceData.updateValue(service)
            }

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                nsdManager.registerServiceInfoCallback(service, ThreadExecutor, serviceInfoCallback)
            }*/
            //nsdManager.resolveService(service, resolveListener)

            /*when {
                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    L.d("Unknown Service Type: ${service.serviceType}")

                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    L.d("Same machine: $mServiceName")

                service.serviceName.contains("NsdChat") -> nsdManager.resolveService(
                    service,
                    resolveListener
                )
            }*/

        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            L.e("服务丢失:$service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            L.i("发现服务停止:$serviceType")
            discoveryInfoData.value?.start = false
            discoveryInfoData.value?.errorCode = null
            discoveryInfoData.updateThis()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            L.e("发现服务失败:$serviceType :$errorCode")
            discoveryInfoData.value?.start = false
            discoveryInfoData.value?.errorCode = errorCode
            discoveryInfoData.updateThis()
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            L.e("停止发现服务失败:${serviceType} :$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    /**开始发现服务
     * ```
     * "_http._tcp", NsdManager.PROTOCOL_DNS_SD
     * ```
     * */
    fun startDiscovery(serviceType: String, protocolType: Int = NsdManager.PROTOCOL_DNS_SD) {
        if (discoveryInfoData.value?.start == true) {
            L.w("正在发现服务")
            return
        }
        discoveryInfoData.updateValue(DiscoverServicesInfo(serviceType, protocolType, true))
        nsdManager.discoverServices(serviceType, protocolType, discoveryListener)
    }

    /**停止探测*/
    fun stopDiscovery() {
        if (discoveryInfoData.value?.start == true) {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }

    /**注册一个服务
     * ```
     * val serviceInfo = NsdServiceInfo().apply {
     *     serviceName = "NsdChat"
     *     serviceType = "_http._tcp."
     *     port = 8080
     * }
     * ```
     * */
    fun registerService(serviceInfo: NsdServiceInfo) {
        if (registerStateData.value == true) {
            L.w("服务已经注册")
            return
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        registerStateData.updateValue(true)
    }

    /**取消注册服务*/
    fun unregisterService() {
        if (registerStateData.value == true) {
            nsdManager.unregisterService(registrationListener)
        }
    }

}

/**正在发现服务的信息*/
data class DiscoverServicesInfo(
    val serviceType: String,
    val protocolType: Int,
    /**是否开始了*/
    var start: Boolean,
    /**错误代码*/
    var errorCode: Int? = null,
)