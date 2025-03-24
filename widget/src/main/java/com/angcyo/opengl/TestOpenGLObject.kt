package com.angcyo.opengl

import android.graphics.Color

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 */
class TestOpenGLObject : OpenGLObject() {

    /**渲染的背景颜色*/
    var backgroundColor: Int = Color.MAGENTA

    override fun render(vpMatrix: FloatArray, projMatrix: FloatArray?, vMatrix: FloatArray?) {
        super.render(vpMatrix, projMatrix, vMatrix)
        //L.d("")
        drawColor(backgroundColor)
        //drawColor(Color.TRANSPARENT)
    }
}