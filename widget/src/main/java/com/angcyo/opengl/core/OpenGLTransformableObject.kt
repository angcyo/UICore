package com.angcyo.opengl.core

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * [android.opengl.Matrix]
 * [android.opengl.Matrix.setIdentityM] 设置为单位矩阵
 * [android.opengl.Matrix.multiplyMV] 向量作用一次矩阵[lhsMat]*[rhsVec]
 *
 */
open class OpenGLTransformableObject {
}

data class Vector3(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
) {

    //--------------------------------------------------
    // Modification methods
    //--------------------------------------------------
    /**
     * Sets all components of this [Vector3] to the specified values.
     *
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     *
     * @return A reference to this [Vector3] to facilitate chaining.
     */
    fun setAll(x: Double, y: Double, z: Double): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

}