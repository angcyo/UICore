package com.angcyo.opengl.core

import android.opengl.GLES20
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.angcyo.library.L
import com.angcyo.library.annotation.Api
import com.angcyo.opengl.core.BaseOpenGLRenderer.Companion.supportsUIntBuffers
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * OpenGL具体渲染的对象
 */
open class OpenGLObject : OpenGLTransformableObject() {

    companion object {
        const val FLOAT_SIZE_BYTES: Int = 4
        const val INT_SIZE_BYTES: Int = 4
        const val SHORT_SIZE_BYTES: Int = 2
        const val BYTE_SIZE_BYTES: Int = 1

        /**使用[FloatArray]创建一个[FloatBuffer]*/
        fun createFloatBuffer(array: FloatArray): FloatBuffer {
            val floatBuffer = ByteBuffer
                .allocateDirect(array.size * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
            floatBuffer.put(array)
            floatBuffer.position(0)
            return floatBuffer
        }
    }

    /**模型矩阵
     * [getModelMatrix]*/
    protected val mMMatrix = Matrix4()

    /**
     * Retrieves this [ATransformable3D] objects model matrix.
     *
     * @return [Matrix4] The internal model matrix. Modification of this will directly affect this object.
     */
    fun getModelMatrix(): Matrix4 {
        return mMMatrix
    }

    /**模型视图矩阵*/
    protected val mMVMatrix = Matrix4()

    /**模型视图投影矩阵*/
    protected val mMVPMatrix = Matrix4()

    /**绘制类型, 也是图元类型*/
    protected var drawingMode: Int = GLES20.GL_TRIANGLES

    //region --override--

    /**
     * Renders the object with no parent matrix.
     *
     * @param camera The camera
     * @param vpMatrix [Matrix4] The view-projection matrix 视图矩阵
     * @param projMatrix [Matrix4] The projection matrix 投影矩阵
     * @param vMatrix [Matrix4] The view matrix 模型矩阵
     * @param sceneMaterial The scene-wide Material to use, if any.
     *
     * [OpenGLScene.render]
     */
    open fun render(
        scene: OpenGLScene,
        vpMatrix: Matrix4?,
        projMatrix: Matrix4?,
        vMatrix: Matrix4?
    ) {
        preRender(scene)

        // -- calculate model view matrix;
        if (vMatrix != null) {
            mMVMatrix.setAll(vMatrix).multiply(mMMatrix)
        }

        //Create MVP Matrix from View-Projection Matrix
        if (vpMatrix != null) {
            //mMVPMatrix.setAll(vpMatrix).multiply(mMMatrix)
            mMVPMatrix.setAll(vpMatrix)
        }

        //
        onRender()
    }

    /**[render]*/
    open fun onRender() {
        //drawColor(Color.YELLOW)

        if (mIsDirty) {
            mProgramHandle = createProgram(buildVertexShader(), buildFragmentShader())
            if (mProgramHandle == 0) {
                mIsDirty = false
                return
            }
        }
        GLES20.glUseProgram(mProgramHandle)

        bindVertexShaderProgram(mProgramHandle)
        bindFragmentShaderProgram(mProgramHandle)

        //--
        //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        //GLES20.glDrawElements(mDrawingMode, mGeometry.getNumIndices(), bufferType, 0)
        //GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glDrawArrays(drawingMode, 0, mNumVertices)

        mIsDirty = false
    }

    protected var mHaveCreatedBuffers: Boolean = false

    /**
     * 每次渲染之前的回调
     * Executed before the rendering process starts
     *
     * [render]
     */
    protected open fun preRender(scene: OpenGLScene) {
        if (!mHaveCreatedBuffers) {
            createBuffers()
        }
        /*if (mOriginalGeometry != null) {
            mOriginalGeometry.validateBuffers()
            return
        }
        if (mVertexBufferInfo != null && mVertexBufferInfo.bufferHandle === 0) {
            createBuffer(mVertexBufferInfo)
        }
        if (mIndexBufferInfo != null && mIndexBufferInfo.bufferHandle === 0) {
            createBuffer(mIndexBufferInfo)
        }
        if (mTexCoordBufferInfo != null && mTexCoordBufferInfo.bufferHandle === 0) {
            createBuffer(mTexCoordBufferInfo)
        }
        if (mColorBufferInfo != null && mColorBufferInfo.bufferHandle === 0) {
            createBuffer(mColorBufferInfo)
        }
        if (mNormalBufferInfo != null && mNormalBufferInfo.bufferHandle === 0) {
            createBuffer(mNormalBufferInfo)
        }*/
    }

    //endregion --override--

    //region --core--

    /**对象的颜色, R G B A*/
    var color = floatArrayOf(1f, 0f, 1f, 1f)

    /**
     * 顶点数据
     * FloatBuffer containing vertex data (x, y, z)
     *
     * [setVertices]
     */
    protected var mVertices: FloatBuffer? = null

    /**
     * [mVertices]在OpenGL中绑定的索引位置
     * [createBuffers]
     * */
    protected var verticesBufferIndex: Int? = null

    /**顶点的数量
     * [setVertices]*/
    protected var mNumVertices: Int = 0

    /**
     * 顶点颜色数据
     * FloatBuffer containing color data (r, g, b, a)
     *
     * [setColors]
     */
    protected var mColors: FloatBuffer? = null

    /**
     * [mColors]在OpenGL中绑定的索引位置
     * [createBuffers]
     * */
    protected var colorsBufferIndex: Int? = null

    /**
     * Passes the data to the Geometry3D instance. Vertex Buffer Objects (VBOs) will be created.
     *
     * @param vertices
     * A float array containing vertex data
     * @param normals
     * A float array containing normal data
     * @param textureCoords
     * A float array containing texture coordinates
     * @param colors
     * A float array containing color values (rgba)
     * @param indices
     * An integer array containing face indices
     * @param createVBOs
     * A boolean controlling if the VBOs are create immediately.
     */
    open fun setData(
        vertices: FloatArray,
        normals: FloatArray?,
        textureCoords: FloatArray?,
        colors: FloatArray?,
        indices: IntArray?,
        createVBOs: Boolean
    ) {
        setData(
            vertices,
            GLES20.GL_STATIC_DRAW,
            normals,
            GLES20.GL_STATIC_DRAW,
            textureCoords,
            GLES20.GL_STATIC_DRAW,
            colors,
            GLES20.GL_STATIC_DRAW,
            indices,
            GLES20.GL_STATIC_DRAW,
            createVBOs
        )
    }

    open fun setData(
        vertices: FloatArray,
        verticesUsage: Int,
        normals: FloatArray?,
        normalsUsage: Int,
        textureCoords: FloatArray?,
        textureCoordsUsage: Int,
        colors: FloatArray?,
        colorsUsage: Int,
        indices: IntArray?,
        indicesUsage: Int,
        createVBOs: Boolean
    ) {

        //--
        setVertices(vertices)

        //--
        if (colors != null && colors.isNotEmpty()) setColors(colors)

        //--
        if (createVBOs) {
            createBuffers()
        }

        /*mGeometry.setData(
            vertices,
            verticesUsage,
            normals,
            normalsUsage,
            textureCoords,
            textureCoordsUsage,
            colors,
            colorsUsage,
            indices,
            indicesUsage,
            createVBOs
        )
        mIsContainerOnly = false
        mElementsBufferType = if (mGeometry.areOnlyShortBuffersSupported())
            GLES20.GL_UNSIGNED_SHORT
        else
            GLES20.GL_UNSIGNED_INT*/
    }

    /**设置顶点数据*/
    fun setVertices(vertices: FloatArray, override: Boolean = false) {
        if (mVertices == null || override) {
            mVertices?.clear()
            mVertices = createFloatBuffer(vertices)
            mNumVertices = vertices.size / 3
        } else {
            mVertices?.put(vertices)
        }
    }

    /**设置顶点颜色*/
    fun setColors(colors: FloatArray, override: Boolean = false) {
        if (mColors == null || override) {
            mColors?.clear()
            mColors = createFloatBuffer(colors)
        } else {
            mColors?.put(colors)
            mColors?.position(0)
        }
    }

    /**
     * Creates the actual Buffer objects.
     */
    open fun createBuffers() {
        val supportsUIntBuffers: Boolean = supportsUIntBuffers

        if (mVertices != null) {
            mVertices!!.compact().position(0)
            verticesBufferIndex = createBuffer(
                mVertices,
                GLES20.GL_ARRAY_BUFFER
            )
        }
        /*if (mNormals != null) {
            mNormals.compact().position(0)
            createBuffer(
                mNormalBufferInfo,
                BufferType.FLOAT_BUFFER,
                mNormals,
                GLES20.GL_ARRAY_BUFFER
            )
        }
        if (mTextureCoords != null) {
            mTextureCoords.compact().position(0)
            createBuffer(
                mTexCoordBufferInfo,
                BufferType.FLOAT_BUFFER,
                mTextureCoords,
                GLES20.GL_ARRAY_BUFFER
            )
        }*/
        if (mColors != null) {
            mColors!!.compact().position(0)
            colorsBufferIndex = createBuffer(mColors, GLES20.GL_ARRAY_BUFFER)
        }
        /*if (mIndicesInt != null && !mOnlyShortBufferSupported && supportsUIntBuffers) {
            mIndicesInt.compact().position(0)
            createBuffer(
                mIndexBufferInfo,
                BufferType.INT_BUFFER,
                mIndicesInt,
                GLES20.GL_ELEMENT_ARRAY_BUFFER
            )
        }*/

        /*if (mOnlyShortBufferSupported || !supportsUIntBuffers) {
            mOnlyShortBufferSupported = true

            if (mIndicesShort == null && mIndicesInt != null) {
                mIndicesInt.position(0)
                mIndicesShort = ByteBuffer
                    .allocateDirect(mNumIndices * org.rajawali3d.Geometry3D.SHORT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asShortBuffer()

                try {
                    for (i in 0..<mNumIndices) {
                        mIndicesShort.put(mIndicesInt.get(i).toShort())
                    }
                } catch (e: BufferOverflowException) {
                    RajLog.e("Buffer overflow. Unfortunately your device doesn't supported int type index buffers. The mesh is too big.")
                    throw (e)
                }

                mIndicesInt.clear()
                mIndicesInt.limit()
                mIndicesInt = null
            }
            if (mIndicesShort != null) {
                mIndicesShort.compact().position(0)
                createBuffer(
                    mIndexBufferInfo,
                    BufferType.SHORT_BUFFER,
                    mIndicesShort,
                    GLES20.GL_ELEMENT_ARRAY_BUFFER
                )
            }
        }*/

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        mHaveCreatedBuffers = true
    }

    /**
     * Creates a buffer and assumes the buffer will be used for static drawing only.
     *
     * @param bufferInfo
     * @param type
     * @param buffer
     * @param target 目标缓存区的类型
     * @param usage 缓存区的用途提示 [GLES20.GL_STATIC_DRAW] [GLES20.GL_DYNAMIC_DRAW]
     */
    fun createBuffer(buffer: Buffer?, target: Int, usage: Int = GLES20.GL_STATIC_DRAW): Int? {
        val byteSize: Int = FLOAT_SIZE_BYTES
        /*if (type == BufferType.SHORT_BUFFER) byteSize = org.rajawali3d.Geometry3D.SHORT_SIZE_BYTES
        else if (type == BufferType.BYTE_BUFFER) byteSize =
            org.rajawali3d.Geometry3D.BYTE_SIZE_BYTES
        else if (type == BufferType.INT_BUFFER) byteSize = org.rajawali3d.Geometry3D.INT_SIZE_BYTES
        bufferInfo.byteSize = byteSize*/

        val buff = IntArray(1)
        GLES20.glGenBuffers(1, buff, 0)

        val handle = buff[0]

        if (buffer != null) {
            buffer.rewind()
            GLES20.glBindBuffer(target, handle)
            GLES20.glBufferData(target, buffer.capacity() * byteSize, buffer, usage)
            GLES20.glBindBuffer(target, 0)
            return handle
        }
        return null

        /*bufferInfo.buffer = buffer
        bufferInfo.bufferHandle = handle
        bufferInfo.bufferType = type
        bufferInfo.target = target

        bufferInfo.usage = usage*/
    }

    /**[reload]*/
    open fun destroy() {
        deleteBuffers(verticesBufferIndex)
        deleteBuffers(colorsBufferIndex)
    }

    /**重新加载, [Buffer] 要重新创建*/
    fun reload() {
        mIsDirty = true
        //createShaders()
        destroy()
        createBuffers()
    }

    //endregion --core--

    //region --shader--

    /**顶点着色器 GLSL
     * [buildVertexShader]*/
    protected var vertexShaderBuilder: StringBuilder? = null

    /**片段着色器 GLSL
     * [buildFragmentShader]*/
    protected var fragmentShaderBuilder: StringBuilder? = null

    /**[vertexShaderBuilder]*/
    open fun buildVertexShader(): String {
        return buildString {
            vertexShaderBuilder = this
            //--着色器声明--
            appendLine("precision mediump float;")//精度声明
            appendLine("attribute vec4 aPosition;")//顶点坐标
            if (colorsBufferIndex == null) {
                appendLine("uniform vec4 uColor;")//顶点颜色
            } else {
                appendLine("attribute vec4 aVertexColor;")//顶点矢量颜色
            }
            appendLine("uniform mat4 uModelViewMatrix;")//模型视图矩阵
            appendLine("uniform mat4 uMVPMatrix;")//模型视图投影矩阵
            appendLine("varying vec4 vColor;")//输出的顶点颜色
            //--着色器函数体--
            appendLine("void main() {")
            appendLine("  gl_Position = uMVPMatrix * uModelViewMatrix * aPosition;")
            if (colorsBufferIndex == null) {
                appendLine("  vColor = uColor;")
            } else {
                appendLine("  vColor = aVertexColor;")
            }
            appendLine("}")
        }
    }

    /**将着色器绑定到程序*/
    open fun bindVertexShaderProgram(programHandle: Int) {
        if (verticesBufferIndex != null) {
            val aPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesBufferIndex!!)
            GLES20.glEnableVertexAttribArray(aPositionHandle)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0)
        }

        if (colorsBufferIndex == null) {
            val uColorHandle = GLES20.glGetUniformLocation(programHandle, "uColor")
            GLES20.glUniform4fv(uColorHandle, 1, color, 0)
        } else {
            val aVertexColorHandle = GLES20.glGetAttribLocation(programHandle, "aVertexColor")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorsBufferIndex!!)
            GLES20.glEnableVertexAttribArray(aVertexColorHandle)
            GLES20.glVertexAttribPointer(
                aVertexColorHandle,
                4,
                GLES20.GL_FLOAT,
                false,
                0,
                0,
            )
        }

        val uModelViewMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uModelViewMatrix")
        GLES20.glUniformMatrix4fv(uModelViewMatrixHandle, 1, false, mMVMatrix.getFloatValues(), 0)

        val uMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mMVPMatrix.getFloatValues(), 0)
    }

    /**[fragmentShaderBuilder]*/
    open fun buildFragmentShader(): String {
        return buildString {
            fragmentShaderBuilder = this
            //--着色器声明--
            appendLine("precision mediump float;")
            appendLine("varying vec4 vColor;")//顶点颜色
            //--着色器函数体--
            appendLine("void main() {")
            appendLine("  gl_FragColor = vColor;")
            appendLine("}")
        }
    }

    fun bindFragmentShaderProgram(programHandle: Int) {

    }

    protected var mIsDirty = true

    /**
     * Holds a reference to the shader program
     */
    protected var mProgramHandle = -1

    /**
     * Holds a reference to the vertex shader
     */
    protected var mVShaderHandle = 0

    /**
     * Holds a reference to the fragment shader
     */
    protected var mFShaderHandle = 0

    /**
     * Creates a shader program by compiling the vertex and fragment shaders
     * from a string.
     *
     * @param vertexSource
     * @param fragmentSource
     *
     * @return
     */
    protected fun createProgram(vertexSource: String, fragmentSource: String): Int {
        mVShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (mVShaderHandle == 0) {
            return 0
        }

        mFShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (mFShaderHandle == 0) {
            return 0
        }

        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, mVShaderHandle)
            GLES20.glAttachShader(program, mFShaderHandle)
            GLES20.glLinkProgram(program)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                L.e("Could not link program in " + javaClass.canonicalName + ": ")
                L.e(GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    /**
     * Loads the shader from a text string and then compiles it.
     *
     * @param shaderType
     * @param source
     *
     * @return
     */
    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                L.e(
                    ("[" + javaClass.name + "] Could not compile "
                            + (if (shaderType == GLES20.GL_FRAGMENT_SHADER) "fragment" else "vertex") + " shader:")
                )
                L.e("Shader log: " + GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    //endregion --shader--

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

    @Api
    fun deleteBuffers(index: Int?) {
        if (index != null) {
            GLES20.glDeleteBuffers(1, intArrayOf(index), 0)
        }
    }

    //endregion --api--

}