package com.angcyo.opengl.primitives

import android.graphics.Color
import android.graphics.PointF
import android.opengl.GLES20
import com.angcyo.library.ex.toOpenGLColor
import com.angcyo.opengl.core.OpenGLObject
import java.util.Stack

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/26
 */
class OpenGLGCodeLine(
    val points: Stack<PointF>,
    val lineThickness: Float = 1f,
) : OpenGLObject() {

    init {
        drawingMode = GLES20.GL_LINES
        color = Color.GREEN.toOpenGLColor()

        val numVertices: Int = points.size

        val vertices = FloatArray(numVertices * 3)

        for (i in 0..<numVertices) {
            val point = points[i]
            val index = i * 3
            vertices[index] = point.x
            vertices[index + 1] = point.y
            vertices[index + 2] = 0f
        }

        setData(vertices, null, null, null, null, true)
    }

    override fun preRender() {
        super.preRender()
        GLES20.glLineWidth(lineThickness)
    }
}

/**GCode线段的信息
 * [OpenGLGCodeLine]*/
/*
data class GCodeLineData(

)*/
