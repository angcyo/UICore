package com.angcyo.library.component.adapt

import android.content.res.Resources
import android.util.DisplayMetrics

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2021/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

enum class Unit {
    DP, PT
}

abstract class UnitHandler(var origin: Int, var design: Int, /* 是否关闭适配*/ var isClose: Boolean) {

    abstract fun apply(dm: DisplayMetrics)

    class PT(origin: Int, design: Int, isClose: Boolean) : UnitHandler(origin, design, isClose) {
        override fun apply(dm: DisplayMetrics) {
            if (isClose)
                dm.xdpi = Resources.getSystem().displayMetrics.xdpi
            else
                dm.xdpi = origin.toFloat() / design / 72
        }
    }

    class DP(origin: Int, design: Int, isClose: Boolean) : UnitHandler(origin, design, isClose) {
        override fun apply(dm: DisplayMetrics) {
            if (isClose) {
                dm.scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
                dm.density = Resources.getSystem().displayMetrics.density
                dm.densityDpi = Resources.getSystem().displayMetrics.densityDpi
            } else {
                val targetDensity = origin.toFloat() / design
                //dm.scaledDensity = targetDensity * (dm.scaledDensity / dm.density);
                val sysDensity = Resources.getSystem().displayMetrics.density
                val sysScaledDensity = Resources.getSystem().displayMetrics.scaledDensity
                dm.density = targetDensity
                dm.scaledDensity = targetDensity * (sysScaledDensity / sysDensity)
                dm.densityDpi = targetDensity.toInt() * 160
            }
        }
    }
}