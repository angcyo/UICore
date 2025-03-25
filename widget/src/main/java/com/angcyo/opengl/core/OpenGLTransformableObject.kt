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
open class OpenGLTransformableObject

data class Matrix4(
    val m: FloatArray = FloatArray(16),
    private val mTmp: FloatArray = FloatArray(16) //A scratch matrix
) {

    companion object {
        //Matrix indices as column major notation (Row x Column)
        const val M00: Int = 0 // 0;
        const val M01: Int = 4 // 1;
        const val M02: Int = 8 // 2;
        const val M03: Int = 12 // 3;
        const val M10: Int = 1 // 4;
        const val M11: Int = 5 // 5;
        const val M12: Int = 9 // 6;
        const val M13: Int = 13 // 7;
        const val M20: Int = 2 // 8;
        const val M21: Int = 6 // 9;
        const val M22: Int = 10 // 10;
        const val M23: Int = 14 // 11;
        const val M30: Int = 3 // 12;
        const val M31: Int = 7 // 13;
        const val M32: Int = 11 // 14;
        const val M33: Int = 15 // 15;
    }

    init {
        identity()
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


    /**
     * Sets this [Matrix4] to an identity matrix.
     *
     * @return A reference to this [Matrix4] to facilitate chaining.
     */
    fun identity(): Matrix4 {
        Matrix.setIdentityM(m, 0)
        return this
    }

    /**
     * 透视矩阵
     *
     * Sets this [Matrix4] to a perspective projection matrix.
     *
     * @param near double The near plane.
     * @param far double The far plane.
     * @param fov double The field of view in degrees.
     * @param aspect double The aspect ratio. Defined as width/height.
     * @return A reference to this [Matrix4] to facilitate chaining.
     */
    fun setToPerspective(near: Float, far: Float, fov: Float, aspect: Float): Matrix4 {
        identity()
        Matrix.perspectiveM(m, 0, fov, aspect, near, far)
        return this
    }

    /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return ("""
     [${m[M00]}|${m[M01]}|${m[M02]}|${m[M03]}]
     [${m[M10]}|${m[M11]}|${m[M12]}|${m[M13]}]
     [${m[M20]}|${m[M21]}|${m[M22]}|${m[M23]}]
     [${m[M30]}|${m[M31]}|${m[M32]}|${m[M33]}]
     """.trimIndent())
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