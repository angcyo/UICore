package com.angcyo.opengl

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

}