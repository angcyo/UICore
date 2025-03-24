package com.angcyo.opengl

import android.opengl.GLES20
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.angcyo.library.annotation.Api

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * OpenGL具体渲染的对象
 */
open class OpenGLObject : OpenGLTransformableObject() {

    protected val mMMatrix = FloatArray(16)
    protected val mMVMatrix = FloatArray(16)
    protected val mMVPMatrix = FloatArray(16)

    /**
     * Renders the object with no parent matrix.
     *
     * @param camera The camera
     * @param vpMatrix [Matrix4] The view-projection matrix
     * @param projMatrix [Matrix4] The projection matrix
     * @param vMatrix [Matrix4] The view matrix
     * @param sceneMaterial The scene-wide Material to use, if any.
     */
    open fun render(
        vpMatrix: FloatArray,
        projMatrix: FloatArray?,
        vMatrix: FloatArray?
    ) {

    }

    //region --api--

    /**绘制一个颜色*/
    @Api
    fun drawColor(color: Int) {
        GLES20.glClearColor(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    //endregion --api--

}