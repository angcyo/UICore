package com.angcyo.canvas.render.core

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.Gravity
import androidx.annotation.AnyThread
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*
import kotlin.math.max

/**
 * 渲染器的坐标转换, 和可视范围的配置/限制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderViewBox(val delegate: CanvasRenderDelegate) {

    /**是否初始化了*/
    val isCanvasInit: Boolean
        get() = !renderBounds.isEmpty

    //region---限制---

    /**这个矩形表示当前的绘制, 需要在View的这个位置进行.
     * 超过这个位置的像素都应该被clip
     * 在[View]的这个位置绘制
     * [updateRenderBounds] 请使用此方法进行数据更新*/
    @Pixel
    @CanvasOutsideCoordinate
    val renderBounds: RectF = RectF(0f, 0f, 0f, 0f)

    /**当前[renderBounds]范围, 在坐标系中的范围*/
    @CanvasInsideCoordinate
    val visibleBoundsInside: RectF
        get() {
            _tempRect.set(0f, 0f, renderBounds.width(), renderBounds.height())
            transformToInside(_tempRect)
            return _tempRect
        }

    /**更新可以用来渲染的区域[renderBounds]
     * [com.angcyo.canvas.render.core.CanvasRenderDelegate.onSizeChanged]
     * */
    fun updateRenderBounds(newBounds: RectF) {
        renderBounds.set(newBounds)

        delegate.dispatchRenderBoxBoundsUpdate(newBounds)

        delegate.refresh()
    }

    /**坐标原点在什么位置, 默认在左上
     * [Gravity.CENTER] 中心点*/
    var originGravity = Gravity.LEFT or Gravity.TOP

    /**更新坐标系所在的位置*/
    fun updateOriginGravity(gravity: Int) {
        originGravity = gravity

        delegate.dispatchRenderBoxOriginGravityUpdate(gravity)

        delegate.refresh()
    }

    val _originPoint = PointF(0f, 0f)

    /**获取[originGravity]描述的坐标系原点坐标*/
    fun getOriginPoint(result: PointF = _originPoint): PointF {
        when (originGravity) {
            Gravity.CENTER -> result.set(renderBounds.width() / 2, renderBounds.height() / 2)
            else -> result.set(0f, 0f)
        }
        return result
    }

    /**最小和最大的缩放比例
     * [10%~1000%]*/
    var minScaleX: Float = 0.1f
    var maxScaleX: Float = 10f //5f

    var minScaleY: Float = minScaleX
    var maxScaleY: Float = maxScaleX //5f

    /**最小和最大的平移距离*/
    var minTranslateX: Float = -Float.MAX_VALUE
    var maxTranslateX: Float = Float.MAX_VALUE //0f

    var minTranslateY: Float = minTranslateX
    var maxTranslateY: Float = maxTranslateX //0f

    val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val _tempPoint = PointF()
    val _tempRect: RectF = emptyRectF()

    /**限制[matrix]画布矩阵的平移和缩放大小
     * [matrix] 输入和输出的 对象*/
    fun limitRenderMatrix(matrix: Matrix) {
        matrix.getValues(_tempValues)

        val curScaleX: Float = _tempValues[Matrix.MSCALE_X]
        val curScaleY: Float = _tempValues[Matrix.MSCALE_Y]

        val curTransX: Float = _tempValues[Matrix.MTRANS_X]
        val curTransY: Float = _tempValues[Matrix.MTRANS_Y]

        val scaleX = clamp(curScaleX, minScaleX, maxScaleX)
        val scaleY = clamp(curScaleY, minScaleY, maxScaleY)

        val translateX = clamp(curTransX, minTranslateX, maxTranslateX)
        val translateY = clamp(curTransY, minTranslateY, maxTranslateY)

        _tempValues[Matrix.MTRANS_X] = translateX
        _tempValues[Matrix.MTRANS_Y] = translateY

        _tempValues[Matrix.MSCALE_X] = scaleX
        _tempValues[Matrix.MSCALE_Y] = scaleY

        matrix.setValues(_tempValues)
    }

    /**指定的[matrix]是否超出了限制范围*/
    fun isOutOfLimit(matrix: Matrix): Boolean {
        val sx = matrix.getScaleX()
        if (sx in minScaleX..maxScaleX) {

        } else {
            return true
        }

        val sy = matrix.getScaleY()
        if (sy in minScaleY..maxScaleY) {

        } else {
            return true
        }

        val tx = matrix.getTranslateX()
        if (tx in minTranslateX..maxTranslateX) {

        } else {
            return true
        }

        val ty = matrix.getTranslateY()
        if (ty in minTranslateY..maxTranslateY) {

        } else {
            return true
        }

        return false
    }

    //endregion---限制---

    //region---绘制---

    /**用来控制画板的缩放和平移*/
    val renderMatrix = Matrix()

    /**[renderMatrix]的逆矩阵*/
    val renderInvertMatrix = Matrix()

    /**更新box的渲染矩阵[renderMatrix]
     * [anim] 是否需要动画*/
    @AnyThread
    fun updateRenderMatrix(target: Matrix, finish: Boolean, reason: Reason) {
        limitRenderMatrix(target)

        renderMatrix.set(target)
        renderMatrix.invert(renderInvertMatrix)

        delegate.dispatchRenderBoxMatrixUpdate(target, reason, finish)

        delegate.refresh()
    }

    var _changeAnimator: ValueAnimator? = null

    /**内部会调用[updateRenderMatrix]*/
    @AnyThread
    fun changeRenderMatrix(
        target: Matrix,
        anim: Boolean,
        reason: Reason,
        finish: ((isCancel: Boolean) -> Unit)? = null
    ) {
        _changeAnimator?.cancel()
        _changeAnimator = null
        val from = Matrix(renderMatrix)
        if (anim) {
            _changeAnimator = matrixAnimator(from, target, finish = { isCancel ->
                _changeAnimator = null
                updateRenderMatrix(target, true, reason)
                finish?.invoke(isCancel)//

                delegate.dispatchRenderBoxMatrixChange(from, target, reason)
            }) {
                updateRenderMatrix(it, false, reason)
            }
        } else {
            updateRenderMatrix(target, true, reason)
            finish?.invoke(false)//

            delegate.dispatchRenderBoxMatrixChange(from, target, reason)
        }
    }

    /**恢复到默认状态*/
    fun reset(
        anim: Boolean = true,
        reason: Reason = Reason.user,
        finish: ((isCancel: Boolean) -> Unit)? = null
    ) {
        val newMatrix = Matrix()
        changeRenderMatrix(newMatrix, anim, reason, finish)
    }

    /**当前缩放的参考值*/
    fun getScale() = max(getScaleX(), getScaleY())

    /**当前x轴方向的缩放*/
    fun getScaleX(): Float {
        return renderMatrix.getScaleX()
    }

    /**当前y轴方向的缩放*/
    fun getScaleY(): Float {
        return renderMatrix.getScaleY()
    }

    fun getTranslateX(): Float {
        return renderMatrix.getTranslateX()
    }

    fun getTranslateY(): Float {
        return renderMatrix.getTranslateY()
    }

    //endregion---绘制---

    //region---转换---

    /**将相对于画板左上角的点[point], 偏移到相对于View的左上角的点*/
    fun offsetToView(point: PointF, result: PointF = point): PointF {
        result.set(point)
        result.offset(renderBounds.left, renderBounds.top)
        return result
    }

    /**将相对于画板左上角的点[point], 偏移到相对于画板原点的点,
     * 可能还需要[transformToInside]*/
    fun offsetToOrigin(point: PointF, result: PointF = point): PointF {
        val originPoint = getOriginPoint()
        result.set(point)
        result.offset(-originPoint.x, -originPoint.y)
        return result
    }

    /**将相对于画板左上角的点, 转换成画板内部相对于画板原点的坐标*/
    fun transformToInside(point: PointF, result: PointF = point): PointF {
        val originPoint = getOriginPoint()
        result.set(point)
        result.offset(-originPoint.x, -originPoint.y)

        renderInvertMatrix.mapPoint(result)

        return result
    }

    /**[transformToInside]*/
    fun transformToInside(rect: RectF, result: RectF = rect): RectF {
        val originPoint = getOriginPoint()
        result.set(rect)
        result.offset(-originPoint.x, -originPoint.y)

        renderInvertMatrix.mapRect(result)

        return result
    }

    /**将画板内部的点, 转换成相对于画板左上角的坐标*/
    fun transformToOutside(point: PointF, result: PointF = point): PointF {
        val originPoint = getOriginPoint()

        result.set(point)
        renderMatrix.mapPoint(result)

        result.offset(originPoint.x, originPoint.y)

        return result
    }

    /**[transformToOutside]*/
    fun transformToOutside(rect: RectF, result: RectF = rect): RectF {
        val originPoint = getOriginPoint()

        result.set(rect)
        renderMatrix.mapRect(result)

        result.offset(originPoint.x, originPoint.y)

        return result
    }

    //endregion---转换---

    //region---操作---

    /**获取当前相对于画板左上角的中点坐标, 映射到画板内部后的坐标*/
    @CanvasInsideCoordinate
    fun getRenderCenterInside(result: PointF = _tempPoint): PointF {
        result.set(renderBounds.width() / 2, renderBounds.height() / 2)
        transformToInside(result)
        return result
    }

    /**获取当前画板原点, 相对于画板左上角的坐标*/
    @CanvasOutsideCoordinate
    fun getOriginPointOutside(result: PointF = _tempPoint): PointF {
        result.set(0f, 0f)
        transformToOutside(result)
        return result
    }

    /**是否在当前的可视坐标范围内可见
     * [fullIn] 是否要全部可见, 否则露出一部分也视为可见*/
    fun isVisibleInRenderBox(bounds: RectF?, fullIn: Boolean = false): Boolean {
        bounds ?: return false
        val visibleBoundsInside = visibleBoundsInside
        return if (fullIn) {
            //需要全部可见
            visibleBoundsInside.contains(bounds) //全包含
        } else {
            visibleBoundsInside.intersect(bounds) //相交即可
        }
    }

    //---

    /**增量平移画布*/
    fun translateBy(dx: Float, dy: Float, anim: Boolean = true, reason: Reason = Reason.user) {
        val matrix = Matrix(renderMatrix)
        matrix.postTranslate(dx, dy)
        changeRenderMatrix(matrix, anim, reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
        })
    }

    /**直接平移画布*/
    fun translateTo(tx: Float, ty: Float, anim: Boolean = true, reason: Reason = Reason.user) {
        val matrix = Matrix(renderMatrix)
        matrix.updateTranslate(tx, ty)
        changeRenderMatrix(matrix, anim, reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_TRANSLATE)
        })
    }

    //---

    /**增量缩放画布*/
    fun scaleBy(sx: Float, sy: Float, anim: Boolean = true, reason: Reason = Reason.user) {
        //默认用画板的中点进行缩放
        val center = getRenderCenterInside()
        scaleBy(sx, sy, center.x, center.y, anim, reason)
    }

    /**增量缩放画布, 已经达到了最大或/最小还想要继续操作, 则忽略.
     * 如果不忽略, 则会有[translate]的效果
     * */
    fun scaleBy(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
        anim: Boolean = true,
        reason: Reason = Reason.user
    ) {
        val matrix = Matrix(renderMatrix)
        matrix.postScale(sx, sy, px, py)

        if ((sx < 1f && getScaleX() <= minScaleX) ||
            (sx > 1f && getScaleX() >= maxScaleX)
        ) {
            //已经达到了最小/最大, 还想缩放/放大
            return
        }

        if ((sy < 1f && getScaleY() <= minScaleY) ||
            (sy > 1f && getScaleY() >= maxScaleY)
        ) {
            return
        }

        changeRenderMatrix(matrix, anim, reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_SCALE)
        })
    }

    /**直接缩放画布*/
    fun scaleTo(sx: Float, sy: Float, anim: Boolean = true, reason: Reason = Reason.user) {
        val matrix = Matrix(renderMatrix)
        matrix.updateScale(sx, sy)
        changeRenderMatrix(matrix, anim, reason.apply {
            controlType = (controlType ?: 0).add(BaseControlPoint.CONTROL_TYPE_SCALE)
        })
    }

    //endregion---操作---

}