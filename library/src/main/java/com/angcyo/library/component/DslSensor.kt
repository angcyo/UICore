package com.angcyo.library.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.view.OrientationEventListener

/**
 * 传感器相关操作类
 *
 * https://developer.android.google.cn/guide/topics/sensors
 *
 * https://developer.android.google.cn/guide/topics/sensors/sensors_overview
 *
 * https://developer.android.google.cn/guide/topics/sensors/sensors_motion
 *
 * 传感器的可能架构因传感器类型而异：
 * 重力、线性加速度、旋转矢量、有效运动、计步器和步测器传感器可能基于硬件，也可能基于软件。
 * 加速度计传感器和陀螺仪传感器始终基于硬件。
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslSensor {

    companion object {

        /**获取设备支持的所有传感器*/
        fun getAllSensor(context: Context): List<Sensor> {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            return sensorManager.getSensorList(Sensor.TYPE_ALL)
        }
    }

    /**记录最后一次传感器的方向*/
    var lastOrientation = -1

    var _orientationEventListener: OrientationEventListener? = null

    /**监听传感器方向的改变*/
    fun listenerOrientation(
        context: Context,
        rate: Int = SensorManager.SENSOR_DELAY_NORMAL,
        action: (orientation: Int) -> Unit
    ) {
        release()
        _orientationEventListener =
            object : OrientationEventListener(context.applicationContext, rate) {
                override fun onOrientationChanged(orientation: Int) {
                    lastOrientation = orientation
                    action(orientation)
                }
            }
        _orientationEventListener?.enable()
    }

    /**是否资源*/
    fun release() {
        _orientationEventListener?.disable()
        _orientationEventListener = null
    }
}