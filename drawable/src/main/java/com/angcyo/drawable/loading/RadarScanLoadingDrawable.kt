package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.dpi
import kotlin.math.max

/**
 * 雷达扫描动画
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class RadarScanLoadingDrawable : AbsDslDrawable() {

    /**是否需要动画*/
    var loading: Boolean = false
        set(value) {
            field = value
            invalidateSelf()
        }

    /**雷达开始的半径*/
    var radarRadius: Int = 40 * dpi

    /**雷达线的宽度*/
    var radarWidth: Int = 1 * dpi

    /**雷达的颜色*/
    var radarColor: Int = Color.RED

    /**雷达半径增长比例*/
    var radarRadiusIncrease: Float = 0.4f

    /**雷达扫描线, 渐变开始的颜色*/
    var radarScanColor: Int = Color.RED
        set(value) {
            field = value
            updateShader()
        }

    /**扫描的步长*/
    var radarScanStep = -2

    /**扫描的当前角度*/
    var radarScanDegrees: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    init {

    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
    }

    val cx: Float
        get() = bounds.centerX().toFloat()

    val cy: Float
        get() = bounds.centerY().toFloat()

    var _scanShader: Shader? = null

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updateShader()
    }

    fun updateShader() {
        _scanShader = createSweepShader()
    }

    override fun draw(canvas: Canvas) {
        //发光背景

        //绘制雷达背景
        textPaint.strokeWidth = radarWidth.toFloat()
        textPaint.style = Paint.Style.STROKE
        textPaint.color = radarColor
        textPaint.shader = null

        //最大半径
        val maxR = max(bounds.width() / 2, bounds.height() / 2)
        //当前的半径
        var r = radarRadius.toFloat()
        var lastR = r
        while (r <= maxR) {
            lastR = r
            canvas.drawCircle(cx, cy, r, textPaint)
            r += r * radarRadiusIncrease
        }

        //绘制扫描
        canvas.withScale(1f, -1f, cx, cy) {
            canvas.withRotation(radarScanDegrees, cx, cy) {
                textPaint.style = Paint.Style.FILL
                textPaint.shader = _scanShader
                canvas.drawCircle(cx, cy, lastR, textPaint)
            }
        }

        //动画
        if (loading) {
            radarScanDegrees += radarScanStep
            if (radarScanDegrees < 0) {
                radarScanDegrees = 360f
            } else if (radarScanDegrees > 360) {
                radarScanDegrees = 0f
            }
        }
    }

    fun createSweepShader(): SweepGradient {
        return SweepGradient(
            cx,
            cy,
            intArrayOf(radarScanColor, radarScanColor.alphaRatio(0.5f), Color.TRANSPARENT),
            floatArrayOf(0f, 0.3f, 1f)
        )
    }

    /*fun createRadialShader(): RadialGradient {
        return RadialGradient(
            cx, cy, cx,
            Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP
        )
    }*/

}