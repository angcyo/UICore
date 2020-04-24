package com.angcyo.library.component

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.annotation.Px
import com.angcyo.library.L

/**
 * density适配到指定的设计宽高
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslDensityAdapter {

    companion object {
        
        //<editor-fold desc="内部属性用于恢复">
        var _oldSysDp: Float = -1f
        var _oldSysDpi: Int = -1
        var _oldSysSp: Float = -1f

        var _oldAppDp: Float = -1f
        var _oldAppDpi: Int = -1
        var _oldAppSp: Float = -1f

        var _oldCtxDp: Float = -1f
        var _oldCtxDpi: Int = -1
        var _oldCtxSp: Float = -1f
        //</editor-fold desc="内部属性用于恢复">

        /**恢复默认值*/
        fun restore(context: Context) {
            val sysDisplayMetrics = Resources.getSystem()?.displayMetrics
            val appDisplayMetrics = context.applicationContext.resources.displayMetrics
            val contextDisplayMetrics = context.resources.displayMetrics

            if (_oldSysDp > 0) {
                sysDisplayMetrics?.apply {
                    density = _oldSysDp
                    densityDpi = _oldSysDpi
                    scaledDensity = _oldSysSp
                }
            }

            if (_oldAppDp > 0) {
                appDisplayMetrics.apply {
                    density = _oldAppDp
                    densityDpi = _oldAppDpi
                    scaledDensity = _oldAppSp
                }
            }

            if (_oldCtxDp > 0) {
                contextDisplayMetrics.apply {
                    density = _oldCtxDp
                    densityDpi = _oldCtxDpi
                    scaledDensity = _oldCtxSp
                }
            }
        }
    }

    /**指定原始的宽度. 小于0默认是屏幕宽度*/
    @Px
    var originWidth: Int = -1

    /**原始的Density, 小于0使用默认*/
    var originDensity: Float = -1f

    /**需要适配的目标设计宽度,宽度适配维度. 小于0默认是屏幕宽度*/
    @Px
    var adapterWidth: Int = -1

    /**需要适配的目标设计dp*/
    var adapterDensity: Float = 1f

    var _context: Context? = null

    /**开始适配*/
    fun adapter(context: Context) {
        _context = context

        val sysDisplayMetrics = Resources.getSystem()?.displayMetrics
        val appDisplayMetrics = context.applicationContext.resources.displayMetrics
        val contextDisplayMetrics = context.resources.displayMetrics

        //保存默认值
        if (_oldSysDp < 0) {
            sysDisplayMetrics?.apply {
                _oldSysDp = density
                _oldSysDpi = densityDpi
                _oldSysSp = scaledDensity
            }
        }

        if (_oldAppDp < 0) {
            appDisplayMetrics.apply {
                _oldAppDp = density
                _oldAppDpi = densityDpi
                _oldAppSp = scaledDensity
            }
        }

        if (_oldCtxDp < 0) {
            contextDisplayMetrics.apply {
                _oldCtxDp = density
                _oldCtxDpi = densityDpi
                _oldCtxSp = scaledDensity
            }
        }

        if (originDensity < 0) {
            originDensity = _oldAppDp
        }

        sysDisplayMetrics?.adapter(originDensity, _oldSysSp)
        appDisplayMetrics.adapter(originDensity, _oldAppSp)
        contextDisplayMetrics.adapter(originDensity, _oldCtxSp)

        val adapterDp = appDisplayMetrics.density
        val adapterDpi: Int = appDisplayMetrics.densityDpi
        val adapterSp = appDisplayMetrics.scaledDensity

        L.w("density:$_oldAppDp $adapterDp densityDpi:$_oldAppDpi $adapterDpi scaledDensity:$_oldAppSp $adapterSp")
    }

    fun DisplayMetrics.adapter(defDp: Float, defSp: Float) {
        val _originWidth = if (originWidth > 0) originWidth else widthPixels
        val _adapterWidth = if (adapterWidth > 0) adapterWidth else widthPixels

        //都用dp进行计算
        val _originWidthDp = _originWidth * 1f / defDp
        val _adapterWidthDp = _adapterWidth * 1f / adapterDensity

        //适配之后, 需要的dp值
        val adapterDp = _originWidthDp / _adapterWidthDp * defDp
        //dpi值
        val adapterDpi: Int = (adapterDp * 160).toInt()
        //sp
        val adapterSp = adapterDp * (defSp / defDp)

        density = adapterDp
        densityDpi = adapterDpi
        scaledDensity = adapterSp
    }

    fun restore() {
        _context?.apply { Companion.restore(this) }
    }
}

/**恢复*/
fun Context.densityRestore() {
    DslDensityAdapter.restore(this)
}

/**将设备的宽度, 适配到指定的宽度*/
fun Context.densityAdapter(targetWidth: Int, targetDensity: Float = 1f) {
    DslDensityAdapter().apply {
        adapterWidth = targetWidth
        adapterDensity = targetDensity
        adapter(this@densityAdapter)
    }
}

/**将目标的宽度, 适配到设备的宽度*/
fun Context.densityAdapterFrom(width: Int) {
    DslDensityAdapter().apply {
        originWidth = width
        adapter(this@densityAdapterFrom)
    }
}