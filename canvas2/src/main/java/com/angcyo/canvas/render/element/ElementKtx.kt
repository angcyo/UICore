package com.angcyo.canvas.render.element

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