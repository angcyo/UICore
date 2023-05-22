package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.canvas.render.core.BaseCanvasRenderListener
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.getTranslateX
import com.angcyo.library.ex.getTranslateY
import com.angcyo.library.ex.updateScale
import com.angcyo.library.ex.updateTranslate
import com.angcyo.library.unit.toPixel
import kotlin.math.max

/**
 * 限制尺寸大小
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/14
 */
class LimitMatrixComponent : BaseCanvasRenderListener(), IComponent {

    companion object {

        private val _tempBoundsLimit = RectF()

        /**元素的范围限制, +-1000mm*/
        val BOUNDS_LIMIT: RectF?
            get() {
                val limit = LibHawkKeys.canvasItemBoundsLimit
                return if (limit.isBlank()) {
                    null
                } else {
                    val list = limit.split(",")
                    list.forEachIndexed { index, string ->
                        when (index) {
                            0 -> _tempBoundsLimit.left = string.toFloatOrNull()?.toPixel() ?: 0f //l
                            1 -> _tempBoundsLimit.top = string.toFloatOrNull()?.toPixel() ?: 0f  //t
                            2 -> _tempBoundsLimit.right =
                                string.toFloatOrNull()?.toPixel() ?: 0f //r
                            3 -> _tempBoundsLimit.bottom =
                                string.toFloatOrNull()?.toPixel() ?: 0f //b
                        }
                    }
                    _tempBoundsLimit
                }
            }

        /**元素的宽高限制, +-1000mm*/
        val SIZE_LIMIT: RectF?
            get() {
                val limit = LibHawkKeys.canvasItemSizeLimit
                return if (limit.isBlank()) {
                    null
                } else {
                    val list = limit.split(",")
                    _tempBoundsLimit.left = 0f
                    _tempBoundsLimit.top = 0f
                    list.forEachIndexed { index, string ->
                        when (index) {
                            0 -> _tempBoundsLimit.left = string.toFloatOrNull() ?: 0f //l
                            1 -> _tempBoundsLimit.top = string.toFloatOrNull() ?: 0f  //t
                            2 -> _tempBoundsLimit.right = string.toFloatOrNull() ?: 0f //r
                            3 -> _tempBoundsLimit.bottom = string.toFloatOrNull() ?: 0f //b
                        }
                    }
                    _tempBoundsLimit
                }
            }
    }

    /**限制元素只能在此范围内移动*/
    @Pixel
    var limitLeft: Float? = BOUNDS_LIMIT?.left

    @Pixel
    var limitTop: Float? = BOUNDS_LIMIT?.top

    @Pixel
    var limitRight: Float? = BOUNDS_LIMIT?.right

    @Pixel
    var limitBottom: Float? = BOUNDS_LIMIT?.bottom

    /**限制元素的最小/最大宽高*/
    @Pixel
    var limitMinWidth: Float? = BOUNDS_LIMIT?.left

    @Pixel
    var limitMinHeight: Float? = BOUNDS_LIMIT?.top

    @Pixel
    var limitMaxWidth: Float? = BOUNDS_LIMIT?.right

    @Pixel
    var limitMaxHeight: Float? = BOUNDS_LIMIT?.bottom

    override var isEnableComponent: Boolean = true

    override fun onLimitControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    ) {
        if (isEnableComponent) {
            if (controlType == BaseControlPoint.CONTROL_TYPE_SCALE) {
                limitScale(controlRenderer, controlMatrix)
            } else if (controlType == BaseControlPoint.CONTROL_TYPE_TRANSLATE) {
                limitTranslate(controlRenderer, controlMatrix)
            }
        }
    }

    override fun onApplyMatrix(
        delegate: CanvasRenderDelegate,
        renderer: BaseRenderer,
        matrix: Matrix,
        controlType: Int
    ) {
        if (isEnableComponent) {
            if (controlType == BaseControlPoint.CONTROL_TYPE_SCALE) {
                limitScale(renderer, matrix)
            } else if (controlType == BaseControlPoint.CONTROL_TYPE_TRANSLATE) {
                limitTranslate(renderer, matrix)
            }
        }
    }

    /**限制缩放操作, 也就是限制宽高*/
    private fun limitScale(renderer: BaseRenderer, matrix: Matrix) {
        var sx = matrix.getScaleX()
        var sy = matrix.getScaleY()

        val renderBounds = renderer.getRendererBounds()

        val width = renderBounds?.width() ?: 0f
        val height = renderBounds?.height() ?: 0f

        if (renderBounds == null) {
            //必须是正值
            sx = sx.abs()
            sy = sy.abs()
        } else {
            //最小要保留1个像素

            if (width <= 0f || height <= 0) {
                //no op
            } else {
                val minSx = 1 / width
                val minSy = 1 / height

                sx = max(minSx, sx)
                sy = max(minSy, sy)
            }
        }

        //限制
        limitMinWidth?.let {
            //需要限制最小宽度
            if (sx * width < it) {
                sx = it / width
            }
        }
        limitMinHeight?.let {
            //需要限制最小宽度
            if (sy * height < it) {
                sy = it / height
            }
        }

        limitMaxWidth?.let {
            //需要限制最大宽度
            if (sx * width > it) {
                sx = it / width
            }
        }
        limitMaxHeight?.let {
            //需要限制最大宽度
            if (sy * height > it) {
                sy = it / height
            }
        }

        //调整
        matrix.updateScale(sx.ensure(1f), sy.ensure(1f))
    }

    /**限制平移操作, 也就是限制xy*/
    private fun limitTranslate(renderer: BaseRenderer, matrix: Matrix) {
        val renderBounds = renderer.getRendererBounds() ?: return
        var tx = matrix.getTranslateX()
        var ty = matrix.getTranslateY()

        //限制
        limitRight?.let {
            if (renderBounds.right + tx > it) {
                tx = it - renderBounds.right
            }
        }
        limitBottom?.let {
            if (renderBounds.bottom + ty > it) {
                ty = it - renderBounds.bottom
            }
        }

        limitLeft?.let {
            if (renderBounds.left + tx < it) {
                tx = it - renderBounds.left
            }
        }
        limitTop?.let {
            if (renderBounds.top + ty < it) {
                ty = it - renderBounds.top
            }
        }

        //调整
        matrix.updateTranslate(tx, ty)
    }
}