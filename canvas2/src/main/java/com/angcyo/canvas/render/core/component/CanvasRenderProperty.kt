package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.component.CanvasRenderProperty.Companion.ANCHOR_X_RIGHT
import com.angcyo.canvas.render.core.component.CanvasRenderProperty.Companion.ANCHOR_Y_CENTER
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.getRotateDegrees
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.getSkewX
import com.angcyo.library.ex.getSkewY
import com.angcyo.library.ex.getTranslateX
import com.angcyo.library.ex.getTranslateY
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.toDegrees
import com.angcyo.library.ex.toRadians
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * 渲染的核心矩阵数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
data class CanvasRenderProperty(

    //region ---矩形描述---

    /**锚点[anchorX]的位置*/
    var originX: String = "left",

    /**锚点[anchorY]的位置*/
    var originY: String = "top",

    /**矩形的横向锚点位置, 像素.
     *
     * 假设[originX] = [ANCHOR_X_RIGHT] 则表示矩形最终渲染的右边距离为[anchorX]
     *
     * 如果有旋转[angle], 那么这个值也要是旋转后的值*/
    @Pixel
    var anchorX: Float = 0f,

    /**矩形的纵向锚点位置, 像素.
     *
     * 假设[originY] = [ANCHOR_Y_CENTER] 则表示矩形最终渲染的垂直中心位置是[anchorY]
     *
     * 如果有旋转[angle], 那么这个值也要是旋转后的值*/
    @Pixel
    var anchorY: Float = 0f,

    /**矩形的宽度, 像素*/
    @Pixel
    var width: Float = 0f,

    /**矩形的高度, 像素*/
    @Pixel
    var height: Float = 0f,

    //endregion ---矩形描述---

    //region ---渲染描述---

    /**旋转的度数, 角度单位.*/
    var angle: Float = 0f,

    /**绘制时的缩放比例*/
    var scaleX: Float = 1f,

    var scaleY: Float = 1f,

    /**是否水平翻转, 如果是, 则就是[-scaleX]*/
    var flipX: Boolean = false,

    /**是否垂直翻转
     * [-scaleX]*/
    var flipY: Boolean = false,

    /**绘制时的倾斜度数, 角度单位.
     * 先倾斜, 再缩放 最后旋转绘制
     *
     * ```
     * setSkew(tan(skewX.toRadians()).toFloat(), tan(skewY.toRadians()).toFloat(), rect.left, rect.top)
     * ```
     * */
    var skewX: Float = 0f,

    /**这是值始终为0*/
    var skewY: Float = 0f,

    //endregion ---渲染描述---
) : Cloneable {

    companion object {

        /**锚点对应在矩形中的位置*/

        /**[anchorX]描述矩形的左边*/
        const val ANCHOR_X_LEFT = "left"

        /**[anchorX]描述矩形的右边*/
        const val ANCHOR_X_RIGHT = "right"

        /**[anchorX]描述矩形的中心*/
        const val ANCHOR_X_CENTER = "center"

        /**[anchorY]描述矩形的上边*/
        const val ANCHOR_Y_TOP = "top"

        /**[anchorY]描述矩形的下边*/
        const val ANCHOR_Y_BOTTOM = "bottom"

        /**[anchorY]描述矩形的中心*/
        const val ANCHOR_Y_CENTER = "center"
    }

    /**使用一个未旋转的矩形[rect], 和一个即将旋转的角度[angle]初始化*/
    fun initWithRect(@Pixel rect: RectF, angle: Float) {
        val point = acquireTempPointF()
        point.set(rect.left, rect.top)

        val matrix = acquireTempMatrix()
        matrix.reset()
        matrix.setRotate(angle, rect.centerX(), rect.centerY())
        matrix.mapPoint(point)

        this.originX = ANCHOR_X_LEFT
        this.originY = ANCHOR_Y_TOP
        this.anchorX = point.x
        this.anchorY = point.y
        this.angle = angle
        this.width = rect.width()
        this.height = rect.height()

        point.release()
        matrix.release()
    }

    /**重置属性*/
    fun reset() {
        originX = ANCHOR_X_LEFT
        originY = ANCHOR_Y_TOP
        anchorX = 0f
        anchorY = anchorX
        width = 0f
        height = width
        angle = 0f
        scaleX = 1f
        scaleY = scaleX
        flipX = false
        flipY = false
        skewX = 0f
        skewY = skewX
    }

    /**复制属性到[target]对象*/
    fun copyTo(target: CanvasRenderProperty? = CanvasRenderProperty()): CanvasRenderProperty? {
        target ?: return null
        target.originX = originX
        target.originY = originY
        target.anchorX = anchorX
        target.anchorY = anchorY
        target.width = width
        target.height = height
        target.angle = angle
        target.scaleX = scaleX
        target.scaleY = scaleY
        target.flipX = flipX
        target.flipY = flipY
        target.skewX = skewX
        target.skewY = skewY
        return target
    }

    override fun clone(): Any {
        val result = CanvasRenderProperty()
        copyTo(result)
        return result
    }

    /**根据[originX]的描述返回[anchorX]应该对应矩形x方向的偏移量*/
    @Pixel
    fun getOffsetX(): Float {
        val rect = getScaleRect()
        return when (originX) {
            ANCHOR_X_RIGHT -> anchorX - rect.right
            ANCHOR_X_CENTER -> anchorX - rect.centerX()
            else -> anchorX - rect.left
        }
    }

    /**根据[originY]的描述返回[anchorY]应该对应矩形y方向的偏移量*/
    @Pixel
    fun getOffsetY(): Float {
        val rect = getScaleRect()
        return when (originY) {
            ANCHOR_Y_BOTTOM -> anchorY - rect.bottom
            ANCHOR_Y_CENTER -> anchorY - rect.centerY()
            else -> anchorY - rect.top
        }
    }

    //region---方法---

    /**基础矩形*/
    private fun getBaseRect(result: RectF = _baseRect): RectF {
        result.set(0f, 0f, width, height)
        return result
    }

    /**基础矩阵, 无旋转
     * [includeFlip] 是否需要翻转*/
    private fun getBaseMatrix(result: Matrix = _baseMatrix, includeFlip: Boolean): Matrix {
        result.setSkew(tan(skewX.toRadians()), tan(skewY.toRadians()))
        if (includeFlip) {
            result.postScale(if (flipX) -scaleX else scaleX, if (flipY) -scaleY else scaleY)
        } else {
            result.postScale(scaleX, scaleY)
        }
        return result
    }

    /**仅获取缩放倾斜后的矩形*/
    private fun getScaleRect(result: RectF = _baseRect): RectF {
        getBaseRect(result)
        val matrix = getBaseMatrix(_baseMatrix, false)
        matrix.mapRect(result) //先计算出目标的宽高
        return result
    }

    private val _baseRect = RectF()
    private val _baseMatrix = Matrix()
    private val _centerPoint = PointF()

    /**获取渲染目标时的中点坐标*/
    @CanvasInsideCoordinate
    fun getRenderCenter(result: PointF = _centerPoint): PointF {
        val rect = getScaleRect()
        rect.offset(getOffsetX(), getOffsetY())

        result.set(rect.centerX(), rect.centerY())

        val matrix = acquireTempMatrix()
        matrix.reset()
        matrix.setRotate(angle, anchorX, anchorY)

        //将中点绕着left,top旋转之后, 就是原始的中点
        matrix.mapPoint(result)

        matrix.release()
        return result
    }

    /**获取渲染的矩形位置, 包含了缩放和倾斜, 但是不包含旋转
     * [includeRotate] 是否要包含旋转矩阵, 包含后就等于[getRenderBounds]了*/
    @Pixel
    @CanvasInsideCoordinate
    fun getRenderRect(result: RectF, includeRotate: Boolean = false): RectF {
        val matrix = acquireTempMatrix()
        getRenderMatrix(matrix, includeRotate)
        getBaseRect(result)
        matrix.mapRect(result)
        matrix.release()
        return result
    }

    /**缓存*/
    private val _renderBounds = RectF()

    /**获取能够包裹元素, 在坐标系统中的矩形坐标位置.
     * 通常用于群组选中时, 边框的确定.
     * 和用来决定[Drawable]绘制的x,y值.
     * 这个矩形的宽高通常会和Drawable的宽高一致
     *
     * [afterRotate] 如果为true, 则获取到的是[getRenderRect]旋转后的bounds
     *               如果为false, 则是原始矩形变换后的最佳bounds, 默认false
     * 此方法可能有性能消耗
     * //todo 在非矢量的数据情况下, bounds计算是否会有问题
     * */
    @Pixel
    @CanvasInsideCoordinate
    fun getRenderBounds(result: RectF = _renderBounds, afterRotate: Boolean = false): RectF {
        val matrix = acquireTempMatrix()
        if (afterRotate) {
            getRenderRect(result)//先获取到未旋转的矩形
            matrix.reset()
            matrix.setRotate(angle, result.centerX(), result.centerY())
            matrix.mapRect(result) //然后再旋转
        } else {
            getRenderMatrix(matrix, true)
            getBaseRect(result)
            matrix.mapRect(result)
        }
        matrix.release()
        return result
    }

    private val _renderMatrix = Matrix()

    /**获取对应的的矩阵, 偏移到了[anchorX] [anchorY]
     * [includeRotate] 是否需要包含旋转信息, 否则就是缩放和倾斜信息描述的矩阵*/
    fun getRenderMatrix(
        result: Matrix = _renderMatrix,
        includeRotate: Boolean = true,
        includeFlip: Boolean = true
    ): Matrix {
        getBaseMatrix(result, includeFlip)
        val centerPoint = getRenderCenter(_centerPoint)
        if (includeRotate) {
            result.postRotate(angle)
        }
        //关键, 平移到指定位置
        translateMatrixCenterTo(result, centerPoint.x, centerPoint.y)
        return result
    }

    /**移动[matrix]矩阵作用矩形后的中心坐标到[anchorX] [anchorY]
     * 相当于给[matrix]加了偏移量, 使其和描述的[anchorX] [anchorY]对齐
     * */
    private fun translateMatrixCenterTo(matrix: Matrix, centerX: Float, centerY: Float) {
        val rect = getBaseRect()
        matrix.mapRect(rect)

        val dx = centerX - rect.centerX()
        val dy = centerY - rect.centerY()
        matrix.postTranslate(dx, dy)
    }

    private val _boundsRect = RectF()
    private val _drawMatrix = Matrix()

    /**获取在[0,0]位置可以直接渲染的矩阵
     * [getRenderMatrix]*/
    fun getDrawMatrix(result: Matrix = _drawMatrix, includeRotate: Boolean = true): Matrix {
        getRenderBounds(_boundsRect, false)
        getRenderMatrix(result, includeRotate)
        result.postTranslate(-_boundsRect.left, -_boundsRect.top)
        return result
    }

    //endregion---方法---

    //region---core---

    /**应用一个平移矩阵*/
    fun applyTranslateMatrix(matrix: Matrix) {
        this.anchorX += matrix.getTranslateX()
        this.anchorY += matrix.getTranslateY()
    }

    /**直接将缩放值, 应用到属性*/
    fun applyScaleMatrixWithValue(matrix: Matrix) {
        this.scaleX *= matrix.getScaleX()
        this.scaleY *= matrix.getScaleY()
    }

    /**应用一个缩放矩阵[matrix], 并调整对应的属性
     * 保持原有的锚点不变
     *
     * 这种方式, 在群组使用旋转的rect取范围时有效, 旋转完成后不需要归位旋转角度
     *
     * 这里需要使用QR分解算法
     * [applyScaleMatrixWithAnchor]
     * [applyScaleMatrixWithCenter]
     * */
    private fun applyScaleMatrixWithAnchor(matrix: Matrix, useQr: Boolean) {
        if (useQr) {
            val target = acquireTempMatrix()
            getRenderMatrix(target, true, false)
            target.postConcat(matrix)

            val anchor = acquireTempPointF()
            anchor.set(anchorX, anchorY)
            matrix.mapPoint(anchor)

            this.anchorX = anchor.x
            this.anchorY = anchor.y

            anchor.release()

            qrDecomposition(target)

            target.release()
        } else {
            applyScaleMatrixWithValue(matrix)
        }
    }

    /**应用一个缩放矩阵[matrix], 并调整对应的属性
     * 保持原有的中点不变
     *
     * 这种方式, 在群组使用贴合的bounds取范围时有效, 并且旋转完成后需要归位旋转角度
     *
     * 这里需要使用QR分解算法
     * [applyScaleMatrixWithAnchor]
     * [applyScaleMatrixWithCenter]
     *
     * [matrix] 当前缩放的控制矩阵
     * [useQr] 是否要使用qr算法, 一般在群组操作时, 内部元素才需要使用qr
     * */
    fun applyScaleMatrixWithCenter(matrix: Matrix, useQr: Boolean) {
        val point = acquireTempPointF()

        getRenderCenter(point)
        matrix.mapPoint(point)

        //目标中点位置
        val targetCenterX = point.x
        val targetCenterY = point.y

        if (useQr) {
            val target = acquireTempMatrix()
            getRenderMatrix(target, true, true)
            target.postConcat(matrix)
            qrDecomposition(target)
            target.release()
        } else {
            applyScaleMatrixWithValue(matrix)
        }

        //qr后的元素中点
        getRenderCenter(point)

        //将中点偏移
        this.anchorX += targetCenterX - point.x
        this.anchorY += targetCenterY - point.y

        point.release()
    }

    /**应用一个旋转矩阵*/
    fun applyRotateMatrix(matrix: Matrix) {
        val anchor = PointF(anchorX, anchorY)
        matrix.mapPoint(anchor)
        this.anchorX = anchor.x
        this.anchorY = anchor.y

        updateAngle(this.angle + matrix.getRotateDegrees())
    }

    /**将[matrix]拆解成[CanvasRenderProperty]
     * QR分解
     * https://stackoverflow.com/questions/5107134/find-the-rotation-and-skew-of-a-matrix-transformation
     *
     * https://ristohinno.medium.com/qr-decomposition-903e8c61eaab
     *
     * https://zh.wikipedia.org/zh-hans/QR%E5%88%86%E8%A7%A3
     *
     * https://rosettacode.org/wiki/QR_decomposition#Java
     *
     * */
    private fun qrDecomposition(matrix: Matrix) {
        val sx = matrix.getScaleX()
        val sy = matrix.getScaleY()
        val angle = atan2(matrix.getSkewY(), sx).toDegrees()
        val denom = sx.pow(2f) + matrix.getSkewY().pow(2f)

        val scaleX = sqrt(denom)
        val scaleY = (sx * sy - matrix.getSkewX() * matrix.getSkewY()) / scaleX

        val skewX = atan2((sx * matrix.getSkewX() + matrix.getSkewY() * sy), denom) //x倾斜的角度, 弧度单位
        val skewY = 0.0f//y倾斜的角度, 弧度单位

        updateAngle(angle)
        flipX = scaleX < 0
        flipY = scaleY < 0
        this.scaleX = scaleX.absoluteValue  //flip单独控制
        this.scaleY = scaleY.absoluteValue  //flip单独控制
        /*flipX = false
        flipY = false
        this.scaleX = scaleX
        this.scaleY = scaleY*/
        this.skewX = skewX.toDegrees()
        this.skewY = skewY
    }

    /**更新旋转角度, 强制正数*/
    private fun updateAngle(angle: Float) {
        this.angle = (angle + 360) % 360
        //this.angle = angle
    }

    /**应用一个翻转参数*/
    fun applyFlip(flipX: Boolean, flipY: Boolean) {
        this.flipX = flipX
        this.flipY = flipY
    }

    //endregion---core---

    //region---other---

    fun toShortString(): String {
        return buildString {
            append("anchorX:$anchorX ")
            append("anchorY:$anchorY ")
            append("width:$width ")
            append("height:$height ")
            append("angle:$angle ")
            append("scaleX:$scaleX ")
            append("scaleY:$scaleY ")
            append("flipX:$flipX ")
            append("flipY:$flipY ")
            append("skewX:$skewX ")
            append("skewY:$skewY ")
        }
    }

    //endregion---other---

}