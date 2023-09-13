package com.angcyo.canvas.render.element

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.library.ex.ADJUST_TYPE_CENTER
import com.angcyo.library.ex.adjustSize
import com.angcyo.library.ex.limitMaxWidthHeight

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/07/29
 */

/**将元素缩放到指定大小, 并保持中心点一致
 * [com.angcyo.canvas.render.element.IElement.updateElementFromBean]*/
fun IElement.scaleElementTo(
    width: Float,
    height: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
) {
    val renderProperty = requestElementRenderProperty()
    val bounds = renderProperty.getRenderBounds()
    val bounds2 = RectF(bounds)
    bounds2.adjustSize(width, height, adjustType)

    val matrix = Matrix()
    matrix.setTranslate(bounds2.left - bounds.left, bounds2.top - bounds.top)
    renderProperty.applyTranslateMatrix(matrix)
    matrix.setScale(bounds2.width() / bounds.width(), bounds2.height() / bounds.height())
    renderProperty.applyScaleMatrixWithValue(matrix)
}

/**限制元素最大的宽高, 并保持中心点一致
 * [com.angcyo.canvas.render.element.IElement.updateElementFromBean]*/
fun IElement.limitElementMaxSize(
    width: Float,
    height: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
) {
    val renderProperty = requestElementRenderProperty()
    val bounds = renderProperty.getRenderBounds()
    val bounds2 = RectF(bounds)
    bounds2.limitMaxWidthHeight(width, height, adjustType)

    val matrix = Matrix()
    matrix.setTranslate(bounds2.left - bounds.left, bounds2.top - bounds.top)
    renderProperty.applyTranslateMatrix(matrix)
    matrix.setScale(bounds2.width() / bounds.width(), bounds2.height() / bounds.height())
    renderProperty.applyScaleMatrixWithValue(matrix)
}

/**返回的是一个矩阵
 * [limitElementMaxSize]*/
fun RectF.limitElementMaxSizeMatrix(
    width: Float,
    height: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
): Matrix {
    val matrix = Matrix()

    val temp = RectF(this)
    temp.limitMaxWidthHeight(width, height, adjustType)

    matrix.setScale(temp.width() / width(), temp.height() / height())
    matrix.postTranslate(temp.left - left, temp.top - top)

    return matrix
}

/**[com.angcyo.canvas.render.element.IElement.requestElementBitmap]*/
fun IElement.rendererToBitmap(): Bitmap? = requestElementBitmap(null, null)
/*
fun RectF.limitElementMaxRectMatrix(rect: RectF, adjustType: Int = ADJUST_TYPE_CENTER): Matrix {
    val matrix = Matrix()

    val temp = RectF(this)
    temp.limitMaxWidthHeight(rect.width(), rect.height(), adjustType)

    matrix.setScale(temp.width() / width(), temp.height() / height())
    matrix.postTranslate(temp.left - left, temp.top - top)

    return matrix
}*/
