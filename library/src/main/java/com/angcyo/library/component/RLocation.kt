package com.angcyo.library.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.angcyo.library.BuildConfig
import com.angcyo.library.component.RNetwork.isWifi
import com.angcyo.library.component.RNetwork.registerObserver
import com.angcyo.library.component.RNetwork.unregisterObserver

/**
 * 系统位置回调, 获取处理类
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019-8-31
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

@SuppressLint("MissingPermission")
class RLocation(private val context: Context) {

    companion object {
        private const val REFRESH_TIME = 5000L
        private const val METER_POSITION = 0.0f
        private var mLocationListener: ILocationListener? = null
        private var listener: LocationListener? = MyLocationListener()

        /**
         * GPS获取定位方式
         */
        fun getGPSLocation(context: Context): Location? {
            var location: Location? = null
            val manager = getLocationManager(context)
            //高版本的权限检查
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //是否支持GPS定位
                //获取最后的GPS定位信息，如果是第一次打开，一般会拿不到定位信息，一般可以请求监听，在有效的时间范围可以获取定位信息
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            return location
        }

        /**
         * network获取定位方式
         */
        fun getNetWorkLocation(context: Context): Location? {
            var location: Location? = null
            val manager = getLocationManager(context)
            //高版本的权限检查
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { //是否支持Network定位
                //获取最后的network定位信息
                location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            return location
        }

        /**
         * 获取最好的定位方式
         */
        fun getBestLocation(
            context: Context,
            criteria: Criteria?
        ): Location? {
            var criteria = criteria
            val location: Location?
            val manager = getLocationManager(context)
            if (criteria == null) {
                criteria = Criteria()
            }
            val provider = chooseProvider(context, criteria)
            location = if (TextUtils.isEmpty(provider)) {
                //如果找不到最适合的定位，使用network定位
                getNetWorkLocation(context)
            } else {
                //高版本的权限检查
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
                //获取最适合的定位方式的最后的定位权限
                manager.getLastKnownLocation(provider)
            }
            return location
        }

        /**
         * 智能选择 位置服务商
         */
        fun chooseProvider(
            context: Context,
            criteria: Criteria?
        ): String {
            var criteria = criteria
            if (criteria == null) {
                criteria = Criteria()
            }
            var provider: String
            provider = if (isWifi(context)) {
                LocationManager.NETWORK_PROVIDER
            } else {
                //智能选择 位置服务商 best is GPS
                getLocationManager(context).getBestProvider(criteria, true)
            }
            if (TextUtils.isEmpty(provider)) {
                //如果找不到最适合的定位
                provider = LocationManager.GPS_PROVIDER
            }
            return provider
        }

        /**
         * 定位监听
         */
        fun addLocationListener(
            context: Context,
            provider: String?,
            locationListener: ILocationListener?
        ) {
            addLocationListener(
                context,
                provider,
                REFRESH_TIME,
                METER_POSITION,
                locationListener
            )
        }

        /**
         * 定位监听
         */
        fun addLocationListener(
            context: Context,
            provider: String?,
            time: Long,
            meter: Float,
            locationListener: ILocationListener?
        ) {
            if (locationListener != null) {
                mLocationListener = locationListener
            }
            if (listener == null) {
                listener = MyLocationListener()
            }
            val manager = getLocationManager(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager.requestLocationUpdates(provider, time, meter, listener)
        }

        /**
         * 取消定位监听
         */
        fun unRegisterListener(context: Context) {
            if (listener != null) {
                val manager = getLocationManager(context)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                //移除定位监听
                manager.removeUpdates(listener)
            }
        }

        private fun getLocationManager(context: Context): LocationManager {
            return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    private var locationListener: ILocationListener? = null
    private val locationManager: LocationManager

    //两次定位, 最小间隔时间 毫秒
    private val minTime: Long = 1000

    //两次定位, 最小距离 米
    private val minDistance = 1f

    //位置监听回调
    private val internalLocationListener: LocationListener? = object : LocationListener {
        /**
         * 数据结构
         * Location[gps 22.570636,114.060309 hAcc=24 et=+10d20h11m57s460ms alt=154.0521240234375 vel=0.29 bear=119.8 vAcc=48 sAcc=2 bAcc=96 {Bundle[mParcelledData.dataSize=96]}]
         */
        override fun onLocationChanged(location: Location) { //定位改变监听
            if (locationListener != null) {
                locationListener!!.onSuccessLocation(location)
            }
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) { //定位状态监听
            log("onStatusChanged==$provider   status==$status")
        }

        override fun onProviderEnabled(provider: String) { //定位状态可用监听
            log("onProviderEnabled==$provider")
        }

        override fun onProviderDisabled(provider: String) { //定位状态不可用监听
            log("onProviderDisabled==$provider")
        }
    }

    //监听网络状态
    private val netStateChangeObserver: NetStateChangeObserver = object : NetStateChangeObserver {
        override fun onNetDisconnected() {
            switchLocationProvider(LocationManager.GPS_PROVIDER)
        }

        override fun onNetConnected(networkType: NetworkType) {
            if (networkType === NetworkType.NETWORK_AVAILABLE) {
                switchLocationProvider(LocationManager.NETWORK_PROVIDER)
            } else {
                switchLocationProvider(LocationManager.GPS_PROVIDER)
            }
        }
    }

    init {
        locationManager = getLocationManager(context)
        registerObserver(netStateChangeObserver)
    }

    fun release() {
        stopLocationListener()
        unregisterObserver(netStateChangeObserver)
    }

    /**
     * 停止监听
     */
    fun stopLocationListener() {
        if (internalLocationListener != null) {
            locationManager.removeUpdates(internalLocationListener)
        }
    }

    /**
     * 开始监听位置信息改变
     */
    fun startLocationListener(listener: ILocationListener) {
        locationListener = listener
        startLocationListenerInternal(chooseProvider(context, null))
        val bestLocation =
            getBestLocation(context, null)
        if (bestLocation != null) {
            listener.onSuccessLocation(bestLocation)
        }
    }

    /**
     * @see LocationManager.requestLocationUpdates
     */
    private fun startLocationListenerInternal(provider: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (locationListener != null) {
            locationListener!!.onLocationStart(provider)
        }
        //获取所有可用的位置提供器
        val providers = locationManager.allProviders
        if (providers.contains(provider)) {
            locationManager.requestLocationUpdates(
                provider,
                minTime,
                minDistance,
                internalLocationListener
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                internalLocationListener
            )
        }
    }

    //切换定位方式
    private fun switchLocationProvider(provider: String) {
        stopLocationListener()
        startLocationListenerInternal(provider)
    }

    private fun log(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(javaClass.simpleName, msg)
        }
    }

    /**
     * 自定义接口
     */
    interface ILocationListener {
        fun onLocationStart(provider: String)
        fun onSuccessLocation(location: Location)
    }

    private class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) { //定位改变监听
            if (mLocationListener != null) {
                mLocationListener!!.onSuccessLocation(location)
            }
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) { //定位状态监听
        }

        override fun onProviderEnabled(provider: String) { //定位状态可用监听
        }

        override fun onProviderDisabled(provider: String) { //定位状态不可用监听
        }
    }

}