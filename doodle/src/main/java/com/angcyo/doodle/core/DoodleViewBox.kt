package com.angcyo.doodle.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import androidx.annotation.UiThread
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.matrixAnimator

/**
 * 视口
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleViewBox(val doodleDelegate: DoodleDelegate) {

    /**内容绘制区域*/
    val contentRect = Rect()

    @CallPoint
    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        contentRect.set(0, 0, w, h)
        updateCoordinateSystemOriginPoint(0f, 0f)
    }

    /**坐标系的原点像素坐标*/
    val coordinateSystemOriginPoint: PointF = PointF(0f, 0f)

    /**更新坐标系原点*/
    fun updateCoordinateSystemOriginPoint(x: Float, y: Float) {
        coordinateSystemOriginPoint.set(x, y)
        //doodleDelegate.dispatchCoordinateSystemOriginChanged(coordinateSystemOriginPoint)
        doodleDelegate.refresh()
    }

    //region ---Matrix---

    /**触摸带来的视图矩阵变化
     * 用来将相对于[View]左上角的点, 转换成相对于坐标系原点的点*/
    val matrix: Matrix = Matrix()
    val oldMatrix: Matrix = Matrix()

    /**用来将相对于坐标系原点的点, 转换成相对于[View]左上角的点*/
    val invertMatrix: Matrix = Matrix()

    /**刷新*/
    @UiThread
    fun refresh(newMatrix: Matrix) {
        oldMatrix.set(matrix)
        //doodleDelegate.dispatchCanvasBoxMatrixChangeBefore(matrix, newMatrix)

        matrix.set(newMatrix)
        //limitTranslateAndScale(matrix)

        //反转矩阵后的值
        matrix.invert(invertMatrix)

        //doodleDelegate.dispatchCanvasBoxMatrixChanged(matrix, oldMatrix)

        doodleDelegate.refresh()
    }

    /**重置坐标系*/
    fun updateTo(endMatrix: Matrix = Matrix(), anim: Boolean = true) {
        if (anim) {
            matrixAnimator(matrix, endMatrix) {
                //adjustScaleOutToLimit(it)
                refresh(it)
            }
        } else {
            //adjustScaleOutToLimit(endMatrix)
            refresh(endMatrix)
        }
    }

    //endregion ---Matrix---

}