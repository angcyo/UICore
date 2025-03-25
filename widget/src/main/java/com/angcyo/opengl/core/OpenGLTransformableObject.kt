package com.angcyo.opengl.core

import android.opengl.Matrix

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

data class Matrix4(
    val m: FloatArray = FloatArray(16),
    private val mTmp: FloatArray = FloatArray(16) //A scratch matrix
) {
    init {
        Matrix.setIdentityM(m, 0)
    }

    //--------------------------------------------------
    // Modification methods
    //--------------------------------------------------
    /**
     * Sets the elements of this [Matrix4] based on the elements of the provided [Matrix4].
     *
     * @param matrix [Matrix4] to copy.
     * @return A reference to this [Matrix4] to facilitate chaining.
     */
    fun setAll(matrix: Matrix4): Matrix4 {
        matrix.toArray(m)
        return this
    }

    /**
     * Multiplies this [Matrix4] with the given one, storing the result in this [Matrix].
     * <pre>
     * A.multiply(B) results in A = AB.
    </pre> *
     *
     * @param matrix [Matrix4] The RHS [Matrix4].
     * @return A reference to this [Matrix4] to facilitate chaining.
     */
    fun multiply(matrix: Matrix4): Matrix4 {
        System.arraycopy(m, 0, mTmp, 0, 16)
        Matrix.multiplyMM(m, 0, mTmp, 0, matrix.getDoubleValues(), 0)
        return this
    }

    fun zero(): Matrix4 {
        for (i in 0..15) {
            m[i] = 0.0f
        }
        return this
    }

    //--

    //--------------------------------------------------
    // Utility methods
    //--------------------------------------------------

    /**
     * Copies the backing array of this [Matrix4] into a float array and returns it.
     *
     * @return float array containing a copy of the backing array. The returned array is owned
     * by this [Matrix4] and is subject to change as the implementation sees fit.
     */
    fun getFloatValues(): FloatArray {
        return m
    }

    /**
     * Returns the backing array of this [Matrix4].
     *
     * @return double array containing the backing array. The returned array is owned
     * by this [Matrix4] and is subject to change as the implementation sees fit.
     */
    fun getDoubleValues(): FloatArray {
        return m
    }

    /**
     * Copies the backing array of this [Matrix4] into the provided double array.
     *
     * @param doubleArray double array to store the copy in. Must be at least 16 elements long.
     * Entries will be placed starting at the 0 index.
     */
    fun toArray(doubleArray: FloatArray) {
        System.arraycopy(m, 0, doubleArray, 0, 16)
    }
}

data class Vector3(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f,
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
    fun setAll(x: Float, y: Float, z: Float): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Scales each component of this [Vector3] by the corresponding components
     * of the provided [Vector3].
     *
     * @param v [Vector3] containing the values to scale by.
     *
     * @return A reference to this [Vector3] to facilitate chaining.
     */
    fun multiply(v: Vector3): Vector3 {
        x *= v.x
        y *= v.y
        z *= v.z
        return this
    }

    /**
     * Multiplies this [Vector3] and the provided 4x4 matrix.
     *
     * @param matrix double[16] representation of a 4x4 matrix.
     *
     * @return A reference to this [Vector3] to facilitate chaining.
     */
    fun multiply(matrix: Matrix4): Vector3 {
        val obj = floatArrayOf(x, y, z, 1.0f)
        Matrix.multiplyMV(
            obj, 0,
            matrix.m, 0,
            obj, 0
        )
        x = obj[0]
        y = obj[1]
        z = obj[2]
        return this
    }

}