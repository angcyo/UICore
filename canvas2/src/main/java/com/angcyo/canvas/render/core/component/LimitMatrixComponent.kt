package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import com.angcyo.canvas.render.core.BaseCanvasRenderListener
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.updateScale
import kotlin.math.max

/**
 * 限制尺寸大小
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/14
 */
class LimitMatrixComponent : BaseCanvasRenderListener(), IComponent {

    override var isEnable: Boolean = true

    override fun onApplyControlMatrix(
        control: BaseControl,
        controlRenderer: BaseRenderer,
        controlMatrix: Matrix,
        controlType: Int
    ) {
        if (isEnable) {
            if (controlType == BaseControlPoint.CONTROL_TYPE_SCALE) {
                limitScale(controlRenderer, controlMatrix)
            }
        }
    }

    override fun onApplyMatrix(
        delegate: CanvasRenderDelegate,
        renderer: BaseRenderer,
        matrix: Matrix,
        controlType: Int
    ) {
        if (isEnable) {
            if (controlType == BaseControlPoint.CONTROL_TYPE_SCALE) {
                limitScale(renderer, matrix)
            }
        }
    }

    private fun limitScale(renderer: BaseRenderer, matrix: Matrix) {
        var sx = matrix.getScaleX()
        var sy = matrix.getScaleY()
        
        val renderBounds = renderer.renderProperty?.getRenderBounds()
        if (renderBounds == null) {
            //必须是正值
            sx = sx.abs()
            sy = sy.abs()
        } else {
            //最小要保留1个像素
            val width = renderBounds.width()
            val height = renderBounds.height()

            if (width == 0f) {
                sx = 1f
            }
            if (height == 0f) {
                sy = 1f
            }

            val minSx = if (width == 0f) 1f else 1 / width
            val minSy = if (height == 0f) 1f else 1 / height

            if (sx < 0) {
                //x反向了
                sx = minSx
            }

            if (sy < 0) {
                //y反向了
                sy = minSy
            }

            sx = max(minSx, sx)
            sy = max(minSy, sy)
        }

        //调整
        matrix.updateScale(sx, sy)
    }
}