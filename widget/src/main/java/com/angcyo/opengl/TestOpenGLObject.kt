package com.angcyo.opengl

import android.graphics.Color
import android.opengl.GLES20
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 */
class TestOpenGLObject : OpenGLObject() {

    /**渲染的背景颜色*/
    var backgroundColor: Int = Color.YELLOW

    override fun render(vpMatrix: FloatArray, projMatrix: FloatArray?, vMatrix: FloatArray?) {
        super.render(vpMatrix, projMatrix, vMatrix)
        //L.d("")
        GLES20.glClearColor(
            backgroundColor.red / 255f,
            backgroundColor.green / 255f,
            backgroundColor.blue / 255f,
            backgroundColor.alpha / 255f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}