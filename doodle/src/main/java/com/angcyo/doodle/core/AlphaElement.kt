package com.angcyo.doodle.core

import android.graphics.*
import android.text.TextPaint
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dpi

/**
 * 透明颜色绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class AlphaElement : IDoodleItem {

    /**画笔*/
    val paint: Paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.BLACK
        this.style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**每个块的大小, 4个块组成一个盘, N个盘组成透明背景*/
    val size = 10 * dpi

    //2个块的颜色
    var colorOdd = -0x3d3d3e

    //2个块的颜色
    var colorEven = -0xc0c0d
    var cacheBitmap: Bitmap? = null
    val _tempRect = Rect()

    @CallPoint
    fun onDraw(canvas: Canvas) {
        if (cacheBitmap == null) {
            initAlphaBitmap()
        }
        canvas.drawPaint(paint)
    }

    //init
    fun initAlphaBitmap() {
        val bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888)
        cacheBitmap = bitmap
        val canvas = Canvas(bitmap)
        _tempRect.set(0, 0, size, size)
        paint.color = colorOdd
        canvas.drawRect(_tempRect, paint)
        _tempRect.offset(size, size)
        canvas.drawRect(_tempRect, paint)
        paint.color = colorEven
        _tempRect.offset(-size, 0)
        canvas.drawRect(_tempRect, paint)
        _tempRect.offset(size, -size)
        canvas.drawRect(_tempRect, paint)
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        paint.shader = shader
    }
}