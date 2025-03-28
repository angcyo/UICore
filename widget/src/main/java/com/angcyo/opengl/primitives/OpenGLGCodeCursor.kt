package com.angcyo.opengl.primitives

import android.graphics.Color
import android.graphics.PointF
import android.opengl.GLES20
import com.angcyo.library.annotation.Api
import com.angcyo.library.annotation.ConfigProperty
import com.angcyo.library.ex.toOpenGLColor
import com.angcyo.opengl.core.OpenGLObject
import com.angcyo.opengl.core.OpenGLScene

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/28
 *
 * GCode当前位置, 十字光标提示
 *
 * [OpenGLGCodeLine]
 * [OpenGLOriginLine]
 */
class OpenGLGCodeCursor : OpenGLObject() {

    init {
        color = Color.RED.toOpenGLColor()
        drawingMode = GLES20.GL_LINES
    }

    /**宽度/高度的大小*/
    @ConfigProperty
    var cursorSize = 10f

    /**光标的位置*/
    @ConfigProperty
    var cursorPoint: PointF? = null
        set(value) {
            field = value
            isChanged = true
        }

    /**是否发生了改变, 需要重新创建顶点数据*/
    @ConfigProperty
    var isChanged = true

    //--

    var _lastScaleX: Float? = null
    var _lastScaleY: Float? = null

    @Api
    fun checkCreateVertexData(scene: OpenGLScene) {
        if (isChanged || _lastScaleX != scene.sceneScaleX || _lastScaleY != scene.sceneScaleY) {
            if (cursorPoint == null) {
                isVisible = false
            } else {
                isVisible = true
                val numVertices = 4
                val vertices = FloatArray(numVertices * 3)
                //
                val x = 1 / scene.sceneScaleX
                val y = 1 / scene.sceneScaleY

                val tx = cursorSize * x / 2 * (cursorSize / scene.lastWidth)
                val ty = cursorSize * y / 2 * (cursorSize / scene.lastHeight)

                vertices[0] = cursorPoint!!.x - tx
                vertices[1] = cursorPoint!!.y
                vertices[2] = 0f
                vertices[3] = cursorPoint!!.x + tx
                vertices[4] = cursorPoint!!.y
                vertices[5] = 0f
                //
                vertices[6] = cursorPoint!!.x
                vertices[7] = cursorPoint!!.y - ty
                vertices[8] = 0f
                vertices[9] = cursorPoint!!.x
                vertices[10] = cursorPoint!!.y + ty
                vertices[11] = 0f
                //--
                setData(vertices, null, null, null, null, true)
            }
            //--
            _lastScaleX = scene.sceneScaleX
            _lastScaleY = scene.sceneScaleY
            isChanged = false
        }
    }

    override fun preRender(scene: OpenGLScene) {
        checkCreateVertexData(scene)
        super.preRender(scene)
        GLES20.glLineWidth(1f)
    }

}