package com.angcyo.opengl

import android.graphics.Color
import com.angcyo.opengl.core.Matrix4
import com.angcyo.opengl.core.OpenGLObject

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 */
class TestOpenGLObject : OpenGLObject() {

    /**渲染的背景颜色*/
    var backgroundColor: Int = Color.MAGENTA

    override fun render(vpMatrix: Matrix4?, projMatrix: Matrix4?, vMatrix: Matrix4?) {
        super.render(vpMatrix, projMatrix, vMatrix)
        //L.d("")
        drawColor(backgroundColor)
        //drawColor(Color.TRANSPARENT)
    }
}