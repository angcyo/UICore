package com.angcyo.drawable.loading

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.angcyo.drawable.base.BaseProgressDrawable
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColorInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseTGLoadingDrawable : BaseProgressDrawable() {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**加载的背景颜色*/
    var loadingBgColor: Int = "#80000000".toColorInt()

    /**加载的颜色*/
    var loadingColor: Int = Color.WHITE

    /**加载的宽度*/
    var loadingWidth: Float = 3 * dp

    /**不明确进度时的绘制扫描的角度*/
    var indeterminateSweepAngle = 3f

    /**角度绘制动画步进的进度*/
    var angleStep = 4f

    //
    val _loadingRectF = RectF()

    //当前的角度
    var _angle = 0f

    fun doAngle() {
        //动画
        _angle += angleStep
        _angle %= 360
        invalidateSelf()
    }

}