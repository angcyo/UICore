package com.angcyo.opengl.primitives

import android.graphics.Color
import android.opengl.GLES20
import com.angcyo.opengl.core.Matrix4
import com.angcyo.opengl.core.OpenGLObject
import com.angcyo.opengl.core.Vector3
import java.util.Stack

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 */
class OpenGLLine(
    val points: Stack<Vector3>,
    val colors: IntArray? = null,
    val lineThickness: Float = 1f,
) : OpenGLObject() {

    init {
        initialize(true)
    }

    fun initialize(createVBOs: Boolean) {
        drawingMode = GLES20.GL_LINE_STRIP

        val numVertices: Int = points.size

        val vertices = FloatArray(numVertices * 3)
        val indices = IntArray(numVertices)
        var colors: FloatArray? = null

        if (this.colors != null) colors = FloatArray(this.colors.size * 4)

        for (i in 0..<numVertices) {
            val point: Vector3 = points[i]
            val index = i * 3
            vertices[index] = point.x
            vertices[index + 1] = point.y
            vertices[index + 2] = point.z
            indices[i] = i.toShort().toInt()

            if (this.colors != null) {
                val color: Int = this.colors[i]
                val colorIndex = i * 4
                colors!![colorIndex] = Color.red(color) / 255f
                colors[colorIndex + 1] = Color.green(color) / 255f
                colors[colorIndex + 2] = Color.blue(color) / 255f
                colors[colorIndex + 3] = Color.alpha(color) / 255f
            }
        }

        setData(vertices, null, null, colors, indices, createVBOs)
    }

    override fun preRender() {
        super.preRender()
        GLES20.glLineWidth(lineThickness)
    }

    override fun render(vpMatrix: Matrix4?, projMatrix: Matrix4?, vMatrix: Matrix4?) {
        super.render(vpMatrix, projMatrix, vMatrix)
    }
}