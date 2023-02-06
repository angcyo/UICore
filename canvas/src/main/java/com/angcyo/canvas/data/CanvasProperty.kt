package com.angcyo.canvas.data

import android.graphics.Matrix
import com.angcyo.library.ex.*
import java.lang.Math.pow
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Canvas绘制时的一些属性
 *
 * [CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/02
 */
open class CanvasProperty : Cloneable {

    /**旋转的度数, 角度单位. 按照中心点旋转绘制*/
    var angle: Float = 0f

    /**绘制时的缩放比例*/
    var scaleX: Float = 1f

    var scaleY: Float = 1f

    /**是否水平翻转, 如果是, 则就是[-scaleX]*/
    var flipX: Boolean = false

    /**是否垂直翻转
     * [-scaleX]*/
    var flipY: Boolean = false

    /**绘制时的倾斜度数, 角度单位.
     * 先缩放, 再倾斜. 然后旋转绘制
     *
     * ```
     * preSkew(tan(skewX.toRadians()).toFloat(), tan(skewY.toRadians()).toFloat(), rect.left, rect.top)
     * ```
     * */
    var skewX: Float = 0f

    /**这是值始终为0*/
    var skewY: Float = 0f

    open fun copyTo(target: CanvasProperty): CanvasProperty {
        target.angle = angle
        target.scaleX = scaleX
        target.scaleY = scaleY
        target.flipX = flipX
        target.flipY = flipY
        target.skewX = skewX
        target.skewY = skewY
        return target
    }

    override fun clone(): CanvasProperty {
        val result = CanvasProperty()
        copyTo(result)
        return result
    }

    //region---关键方法---

    /**将[matrix]拆解成[CanvasProperty]
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
    fun qrDecomposition(
        matrix: Matrix,
        property: CanvasProperty = CanvasProperty()
    ): CanvasProperty {
        val angle = atan2(matrix.getSkewY(), matrix.getScaleX()).toDegrees()
        val denom = pow(matrix.getScaleX().toDouble(), 2.0) +
                pow(matrix.getSkewY().toDouble(), 2.0)

        val scaleX = sqrt(denom)
        val scaleY = (matrix.getScaleX() * matrix.getScaleY() -
                matrix.getSkewX() * matrix.getSkewY()) / scaleX

        val skewX = atan2(
            (matrix.getScaleX() * matrix.getSkewX() +
                    matrix.getSkewY() * matrix.getScaleY()).toDouble(),
            denom
        ) //x倾斜的角度, 弧度单位
        val skewY = 0.0f//y倾斜的角度, 弧度单位

        property.angle = angle
        property.scaleX = scaleX.toFloat()
        property.scaleY = scaleY.toFloat()
        property.skewX = skewX.toDegrees().toFloat()
        property.skewY = skewY

        return property
    }

    //endregion---关键方法---

    /**获取矩阵属性对应的矩阵[Matrix]
     * [includeRotate] 是否需要旋转属性, 在Group中计算包含元素Bounds时, 推荐true, 在单元素绘制时推荐false*/
    open fun getMatrix(includeRotate: Boolean = true): Matrix {
        //这里使用默认的锚点, 如果需要指定锚点, 可以在返回后使用preConcat锚点信息
        return getMatrix(includeRotate, 0f, 0f)
    }

    protected fun getMatrix(includeRotate: Boolean, px: Float, py: Float): Matrix {
        val matrix = Matrix()
        matrix.preSkew(tan(skewX.toRadians()), tan(skewY.toRadians()), px, py)
        //这里的翻转要在中心点效果才对
        matrix.postScale(if (flipX) -scaleX else scaleX, if (flipY) -scaleY else scaleY, px, py)
        if (includeRotate) {
            matrix.postRotate(angle, px, py)
        }
        return matrix
    }
}
