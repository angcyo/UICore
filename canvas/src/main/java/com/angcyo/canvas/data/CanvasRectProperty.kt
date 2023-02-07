package com.angcyo.canvas.data

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.offsetCenterTo

/**
 * Canvas矩形相关属性
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/06
 */
class CanvasRectProperty : CanvasProperty() {

    /**矩形的左上角, 像素*/
    @Pixel
    var left: Float = 0f

    /**矩形的左上角, 像素*/
    @Pixel
    var top: Float = 0f

    /**矩形的宽度, 像素*/
    @Pixel
    var width: Float = 0f

    /**矩形的高度, 像素*/
    @Pixel
    var height: Float = 0f

    override fun copyTo(target: CanvasProperty): CanvasProperty {
        super.copyTo(target)
        if (target is CanvasRectProperty) {
            target.left = left
            target.top = top
            target.width = width
            target.height = height
        }
        return target
    }

    override fun clone(): CanvasRectProperty {
        val result = CanvasRectProperty()
        copyTo(result)
        return result
    }

    //region---基础方法---

    /**使用[rect]初始化*/
    fun initWithRect(rect: RectF) {
        left = rect.left
        top = rect.top
        width = rect.width()
        height = rect.height()
    }

    /**获取矩形属性描述的矩形, 没有进行[Matrix]映射*/
    @Pixel
    fun getRect(@Pixel result: RectF = RectF()): RectF {
        result.set(left, top, left + width, top + height)
        return result
    }

    /**获取映射后, 原始矩形变换后的矩形*/
    @Pixel
    fun getBounds(@Pixel result: RectF = RectF(), includeRotate: Boolean = false): RectF {
        val matrix = super.getMatrix(includeRotate)
        getRect(result)

        matrix.mapRect(result)

        matrix.release()
        return result
    }

    /**
     * 这里获取到的矩阵锚点是矩形的中心
     * @inheritDoc*/
    override fun getMatrix(includeRotate: Boolean): Matrix {
        val rect = getRect(acquireTempRectF())
        val originCenterX = rect.centerX()
        val originCenterY = rect.centerY()
        val matrix = super.getMatrix(includeRotate)

        matrix.mapRect(rect)
        matrix.postTranslate(originCenterX - rect.centerX(), originCenterY - rect.centerY())

        //val m2 = getMatrix(includeRotate, originCenterX, originCenterY)

        rect.release()
        return matrix
    }

    /**此方法可以指定锚点*/
    override fun getMatrix(includeRotate: Boolean, px: Float, py: Float): Matrix {
        return super.getMatrix(includeRotate, px, py)
    }

    /**将矩形的中点, 偏移到矩阵[matrix]所描述的位置*/
    fun offsetCenterTo(matrix: Matrix) {
        val rect = getRect(acquireTempRectF())
        val target = acquireTempRectF()
        target.set(rect)

        matrix.mapRect(target)
        rect.offsetCenterTo(target.centerX(), target.centerY())
        initWithRect(rect)

        rect.release()
        target.release()
    }

    //endregion---基础方法---

    //region---操作方法---

    /**直接缩放到指定比例*/
    fun scaleTo(sx: Float = scaleX, sy: Float = scaleY) {
        scaleX = sx
        scaleY = sy
    }

    /**错切/倾斜到指定的度数, 角度单位*/
    fun skewTo(kx: Float = skewX, ky: Float = skewY) {
        skewX = kx
        skewY = ky
    }

    /**围绕bounds的矩形, 缩放了多少比例
     * [sx] [sy] bounds矩形缩放了多少比例
     * [px] [py] 缩放的锚点, 事实看到的锚点
     * */
    fun wrapScale(sx: Float = 1f, sy: Float = 1f, px: Float, py: Float) {
        val matrix = getMatrix(true)
        matrix.postScale(sx, sy, px, py)

        qrDecomposition(matrix, this)
        offsetCenterTo(matrix)
    }

    /**围绕bounds的矩形, 旋转了多少度数
     * [rotate] 旋转的度数, 角度单位
     * [px] [py] 缩放的锚点, 事实看到的锚点
     * */
    fun wrapRotate(rotate: Float, px: Float, py: Float) {
        val matrix = getMatrix(true)
        matrix.postRotate(rotate, px, py)

        qrDecomposition(matrix, this)
        offsetCenterTo(matrix)
    }

    //endregion---操作方法---

}
