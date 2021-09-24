package com.angcyo.library.component.adapt

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import com.angcyo.library.app
import java.lang.reflect.Field
import java.util.*

/**
 * https://github.com/Blankj/AndroidUtilCode
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/11/15
 * desc  : utils about adapt screen
</pre> *
 */
class AdaptScreenUtils private constructor() {

    companion object {

        private var sMetricsFields: MutableList<Field>? = null

        /**
         * Adapt for the horizontal screen, and call it in [android.app.Activity.getResources].
         */
        fun adaptWidth(resources: Resources, designWidth: Int): Resources {
            val newXdpi: Float = (resources.displayMetrics.widthPixels * 72f) / designWidth
            applyDisplayMetrics(resources, newXdpi)
            return resources
        }

        /**
         * Adapt for the vertical screen, and call it in [android.app.Activity.getResources].
         */
        /**
         * Adapt for the vertical screen, and call it in [android.app.Activity.getResources].
         */
        @JvmOverloads
        fun adaptHeight(
            resources: Resources,
            designHeight: Int,
            includeNavBar: Boolean = false
        ): Resources {
            val screenHeight: Float = ((resources.displayMetrics.heightPixels
                    + (if (includeNavBar) getNavBarHeight(resources) else 0))) * 72f
            val newXdpi: Float = screenHeight / designHeight
            applyDisplayMetrics(resources, newXdpi)
            return resources
        }

        private fun getNavBarHeight(resources: Resources): Int {
            val resourceId: Int =
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId != 0) {
                resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        }

        /**
         * @param resources The resources.
         * @return the resource
         */
        fun closeAdapt(resources: Resources): Resources {
            val newXdpi: Float = Resources.getSystem().displayMetrics.density * 72f
            applyDisplayMetrics(resources, newXdpi)
            return resources
        }

        /**
         * Value of pt to value of px.
         *
         * @param ptValue The value of pt.
         * @return value of px
         */
        fun pt2Px(ptValue: Float): Int {
            val metrics: DisplayMetrics = app().resources.displayMetrics
            return (ptValue * metrics.xdpi / 72f + 0.5).toInt()
        }

        /**
         * Value of px to value of pt.
         *
         * @param pxValue The value of px.
         * @return value of pt
         */
        fun px2Pt(pxValue: Float): Int {
            val metrics: DisplayMetrics = app().resources.displayMetrics
            return (pxValue * 72 / metrics.xdpi + 0.5).toInt()
        }

        private fun applyDisplayMetrics(resources: Resources, newXdpi: Float) {
            resources.displayMetrics.xdpi = newXdpi
            app().resources.displayMetrics.xdpi = newXdpi
            applyOtherDisplayMetrics(resources, newXdpi)
        }

        val preLoadRunnable: Runnable
            get() = Runnable { preLoad() }

        private fun preLoad() {
            applyDisplayMetrics(Resources.getSystem(), Resources.getSystem().displayMetrics.xdpi)
        }

        private fun applyOtherDisplayMetrics(resources: Resources, newXdpi: Float) {
            if (sMetricsFields == null) {
                sMetricsFields = ArrayList()
                var resCls: Class<*>? = resources.javaClass
                var declaredFields: Array<Field>? = resCls!!.declaredFields
                while (declaredFields != null && declaredFields.isNotEmpty()) {
                    for (field: Field in declaredFields) {
                        if (field.type.isAssignableFrom(DisplayMetrics::class.java)) {
                            field.isAccessible = true
                            val tmpDm: DisplayMetrics? = getMetricsFromField(resources, field)
                            if (tmpDm != null) {
                                sMetricsFields?.add(field)
                                tmpDm.xdpi = newXdpi
                            }
                        }
                    }
                    resCls = resCls!!.superclass
                    if (resCls != null) {
                        declaredFields = resCls.declaredFields
                    } else {
                        break
                    }
                }
            } else {
                applyMetricsFields(resources, newXdpi)
            }
        }

        private fun applyMetricsFields(resources: Resources, newXdpi: Float) {
            for (metricsField: Field in sMetricsFields!!) {
                try {
                    val dm: DisplayMetrics? = metricsField.get(resources) as? DisplayMetrics
                    if (dm != null) dm.xdpi = newXdpi
                } catch (e: Exception) {
                    Log.e("AdaptScreenUtils", "applyMetricsFields: $e")
                }
            }
        }

        private fun getMetricsFromField(resources: Resources, field: Field): DisplayMetrics? {
            try {
                return field.get(resources) as DisplayMetrics
            } catch (ignore: Exception) {
                return null
            }
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
