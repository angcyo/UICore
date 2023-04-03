package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import com.angcyo.canvas.render.core.BaseCanvasRenderListener
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*
import com.angcyo.library.unit.toPixel
import kotlin.math.max

/**
 * 限制尺寸大小
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/14
 */
class LimitMatrixComponent : BaseCanvasRenderListener(), IComponent {

    companion object {

        /**限制坐标*/
        @Pixel
        var DEFAULT_MIN_LEFT: Float? = (-1000f).toPixel()

        @Pixel
        var DEFAULT_MIN_TOP: Float? = DEFAULT_MIN_LEFT

        @Pixel
        var DEFAULT_MAX_RIGHT: Float? = 1000f.toPixel()

        @Pixel
        var DEFAULT_MAX_BOTTOM: Float? = DEFAULT_MAX_RIGHT

        /**限制大小*/
        @Pixel
        var DEFAULT_MIN_WIDTH: Float? = 1f

        @Pixel
        var DEFAULT_MIN_HEIGHT: Float? = DEFAULT_MIN_WIDTH

        @Pixel
        var DEFAULT_MAX_WIDTH: Float? = 1000f.toPixel()

        @Pixel
        var DEFAULT_MAX_HEIGHT: Float? = DEFAULT_MAX_WIDTH
    }

    /**限制元素只能在此范围内移动*/
    @Pixel
    var limitLeft: Float? = DEFAULT_MIN_LEFT

    @Pixel
    var limitTop: Float? = DEFAULT_MIN_TOP

    @Pixel
    var limitRight: Float? = DEFAULT_MAX_RIGHT

    @Pixel
    var limitBottom: Float? = DEFAULT_MAX_BOTTOM

    /**限制元素的最小/最大宽高*/
    @Pixel
    var limitMinWidth: Float? = DEFAULT_MIN_WIDTH

    @Pixel
    var limitMinHeight: Float? = DEFAULT_MIN_HEIGHT

    @Pixel
    var limitMaxWidth: Float? = DEFAULT_MAX_WIDTH

    @Pixel
    var limitMaxHeight: Float? = DEFAULT_MAX_HEIGHT

    override var isEnableComponent: Boolean = true

    override fun onApplyControlMatrix(
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

            if (width == 0f) {
                sx = 1f
            }
            if (height == 0f) {
                sy = 1f
            }

            val minSx = if (width == 0f) 1f else 1 / width
            val minSy = if (height == 0f) 1f else 1 / height

            sx = max(minSx, sx)
            sy = max(minSy, sy)
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
        matrix.updateScale(sx, sy)
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