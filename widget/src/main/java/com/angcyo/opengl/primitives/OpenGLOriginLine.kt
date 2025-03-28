package com.angcyo.opengl.primitives

import android.graphics.Color
import android.opengl.GLES20
import com.angcyo.library.annotation.Api
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.toOpenGLColor
import com.angcyo.opengl.core.OpenGLObject
import com.angcyo.opengl.core.OpenGLScene

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/27
 *
 * OpenGL原点横竖线
 */
class OpenGLOriginLine : OpenGLObject() {

    init {
        drawingMode = GLES20.GL_LINES
        color = Color.RED.toOpenGLColor()
    }

    var _lastScaleX: Float? = null
    var _lastScaleY: Float? = null

    @Api
    fun checkCreateVertexData(scene: OpenGLScene) {
        if (_lastScaleX != scene.sceneScaleX || _lastScaleY != scene.sceneScaleY) {
            val numVertices = 4
            val vertices = FloatArray(numVertices * 3)
            //
            val x = 1 / scene.sceneScaleX
            val y = 1 / scene.sceneScaleY

            val tx = scene.sceneTranslateX.abs() * x
            val ty = scene.sceneTranslateY.abs() * y

            vertices[0] = -x - tx
            vertices[1] = 0f
            vertices[2] = 0f
            vertices[3] = x + tx
            vertices[4] = 0f
            vertices[5] = 0f
            //
            vertices[6] = 0f
            vertices[7] = -y - ty
            vertices[8] = 0f
            vertices[9] = 0f
            vertices[10] = y + ty
            vertices[11] = 0f
            //--
            setData(vertices, null, null, null, null, true)
            //--
            _lastScaleX = scene.sceneScaleX
            _lastScaleY = scene.sceneScaleY
        }
    }

    override fun bindVertexShaderProgram(programHandle: Int) {
        super.bindVertexShaderProgram(programHandle)
    }

    override fun preRender(scene: OpenGLScene) {
        checkCreateVertexData(scene)
        super.preRender(scene)
        GLES20.glLineWidth(1f)
    }
}