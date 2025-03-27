package com.angcyo.opengl.primitives

import android.graphics.Color
import android.opengl.GLES20
import com.angcyo.library.ex.toOpenGLColor
import com.angcyo.opengl.core.OpenGLObject

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/27
 *
 * OpenGL原点横竖线
 */
class OpenGLOriginLine : OpenGLObject() {

    init {
        drawingMode = GLES20.GL_LINES

        val numVertices = 4
        val vertices = FloatArray(numVertices * 3)
        //
        vertices[0] = -1f
        vertices[1] = 0f
        vertices[2] = 0f
        vertices[3] = 1f
        vertices[4] = 0f
        vertices[5] = 0f
        //
        vertices[6] = 0f
        vertices[7] = -1f
        vertices[8] = 0f
        vertices[9] = 0f
        vertices[10] = 1f
        vertices[11] = 0f
        //--
        color = Color.RED.toOpenGLColor()
        setData(vertices, null, null, null, null, true)
    }
}