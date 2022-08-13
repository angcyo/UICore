package com.angcyo.crop

import android.graphics.*
import android.view.MotionEvent
import com.angcyo.library.annotation.CallPoint

/**
 * 覆盖层
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class CropOverlay(val cropDelegate: CropDelegate) {

    /**剪切的矩形*/
    var clipRect: Rect = Rect()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    //region ---core---

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {

        return true
    }

    @CallPoint
    fun onDraw(canvas: Canvas) {
        paint.color = Color.WHITE
        canvas.drawRect(clipRect, paint)
    }

    //endregion ---core---

    fun updateWithBitmap(bitmap: Bitmap) {
        /*clipRect.set(
            -bitmap.width / 2, -bitmap.height / 2,
            bitmap.width / 2, bitmap.height / 2
        )*/
        clipRect.set(cropDelegate._bestRect)
    }

}