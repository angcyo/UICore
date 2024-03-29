package com.angcyo.doodle.core

import android.graphics.Bitmap
import android.graphics.Color
import com.angcyo.doodle.data.BaseElementData

/**
 * 涂鸦配置存储类, 统一的配置入口
 *
 * [com.angcyo.doodle.data.BaseElementData]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleConfig {

    /**笔的颜色*/
    var paintColor: Int = Color.BLACK

    /**笔的宽度, 理论上会等于[android.graphics.Paint.setStrokeWidth], px*/
    var paintWidth: Float = 20f

    /**更新数据到[BaseElementData]*/
    fun updateToElementData(data: BaseElementData) {
        data.paintWidth = paintWidth
        data.paintColor = paintColor
        data.brushBitmap = brushBitmap
    }

    /**定义图片画刷的图片*/
    var brushBitmap: Bitmap? = null

}