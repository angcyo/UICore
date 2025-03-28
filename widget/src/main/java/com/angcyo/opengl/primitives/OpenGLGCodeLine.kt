package com.angcyo.opengl.primitives

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLES20
import com.angcyo.library.animation.AnimationStateEnum
import com.angcyo.library.annotation.Api
import com.angcyo.library.annotation.AutoConfigProperty
import com.angcyo.library.annotation.ConfigProperty
import com.angcyo.library.annotation.OutputProperty
import com.angcyo.library.annotation.ThreadDes
import com.angcyo.library.ex.distance
import com.angcyo.library.ex.toOpenGLColor
import com.angcyo.library.ex.toOpenGLColorList
import com.angcyo.opengl.core.OpenGLObject
import com.angcyo.opengl.core.OpenGLScene
import java.nio.FloatBuffer
import java.util.Stack
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/26
 */
class OpenGLGCodeLine(
    val points: Stack<OpenGLGCodeLineData>,
    val lineThickness: Float = 1f,
) : OpenGLObject() {

    companion object {
        /**仿真速度列表*/
        val simulationSpeedList = arrayOf(
            "0.1X",
            "0.2X",
            "0.5X",
            "1X",
            "2X",
            "5X",
            "10X",
            "20X",
            "40X",
        )
    }

    /**绘制到的距离, 距离0,0的距离*/
    @ConfigProperty
    var renderEndDistance = 0f

    /**总距离*/
    @OutputProperty
    var sumDistance = 0f

    /**所有点对应的边界*/
    @OutputProperty
    var outputBounds: RectF? = null

    /**当前的渲染进度[0~1]
     * 同时也可以设置渲染进度
     * */
    @Api
    var renderProgress: Float
        get() = renderEndDistance / sumDistance
        set(value) {
            stopAnimation()
            val old = renderProgress
            renderEndDistance = if (value <= 0f) {
                0f
            } else if (value >= 1f) {
                sumDistance
            } else {
                sumDistance * value
            }
            notifyRenderProgressChanged(old, true)
        }

    /**事件监听者集合*/
    @ConfigProperty
    val listeners = mutableListOf<OpenGLGCodeLineListener>()

    init {
        drawingMode = GLES20.GL_LINES
        color = Color.BLACK.toOpenGLColor()
        isDynamicColorBuffer = true
        setTransparent(true)

        val numVertices: Int = points.size

        //顶点坐标数据
        val vertices = mutableListOf<Float>()
        //顶点颜色数据
        val colors = mutableListOf<Float>()
        //--
        val startPoints = mutableListOf<Float>()
        val startDistances = mutableListOf<Float>()

        //
        sumDistance = 0f
        var minX: Float? = null
        var maxX: Float? = null
        var minY: Float? = null
        var maxY: Float? = null

        for (i in 0..<numVertices) {
            val point = points[i]

            //
            point.lineStartDistance = sumDistance

            vertices.add(point.startPoint.x)
            vertices.add(point.startPoint.y)
            vertices.add(0f)
            vertices.add(point.endPoint.x)
            vertices.add(point.endPoint.y)
            vertices.add(0f)

            //--
            val rgbaList = point.color.toOpenGLColorList()
            colors.addAll(rgbaList)
            colors.addAll(rgbaList)

            //--
            startPoints.add(point.startPoint.x)
            startPoints.add(point.startPoint.y)
            startPoints.add(0f)
            startPoints.add(point.startPoint.x)
            startPoints.add(point.startPoint.y)
            startPoints.add(0f)

            //--
            startDistances.add(point.lineStartDistance)
            startDistances.add(point.lineStartDistance)

            //
            sumDistance += point.distance
            //--
            if (minX == null) {
                minX = min(point.startPoint.x, point.endPoint.x)
            } else {
                minX = min(minX, point.startPoint.x)
                minX = min(minX, point.endPoint.x)
            }
            if (maxX == null) {
                maxX = max(point.startPoint.x, point.endPoint.x)
            } else {
                maxX = max(maxX, point.startPoint.x)
                maxX = max(maxX, point.endPoint.x)
            }
            //--
            if (minY == null) {
                minY = min(point.startPoint.y, point.endPoint.y)
            } else {
                minY = min(minY, point.startPoint.y)
                minY = min(minY, point.endPoint.y)
            }
            if (maxY == null) {
                maxY = max(point.startPoint.y, point.endPoint.y)
            } else {
                maxY = max(maxY, point.startPoint.y)
                maxY = max(maxY, point.endPoint.y)
            }
        }

        //--
        renderEndDistance = sumDistance
        if (minX != null && maxX != null && minY != null && maxY != null) {
            outputBounds = RectF(minX, minY, maxX, maxY)
        }

        setStartPositions(startPoints.toFloatArray())
        setStartDistance(startDistances.toFloatArray(), true)
        setData(vertices.toFloatArray(), null, null, colors.toFloatArray(), null, true)
    }

    //--

    /**每一帧动画需要增加的距离*/
    @ConfigProperty
    var animationDistanceStep = 1f

    /**动画每一帧的倍速
     * [simulationSpeedList]*/
    @ConfigProperty
    var animationSpeed = 1f

    /**是否开始了动画*/
    @OutputProperty
    var animationState: AnimationStateEnum = AnimationStateEnum.DEFAULT

    /**开关动画的状态*/
    fun toggleAnimation(pause: Boolean = true) {
        if (animationState.isPlaying) {
            stopAnimation(if (pause) AnimationStateEnum.PAUSED else AnimationStateEnum.STOPPED)
        } else {
            startAnimation()
        }
    }

    /**开始动画*/
    fun startAnimation() {
        if (animationState.isPlaying) {
            return
        }
        if (animationState.isPaused) {
            //
        } else {
            renderEndDistance = 0f
        }
        val old = animationState
        animationState = AnimationStateEnum.PLAYING
        notifyAnimationState(old)
    }

    /**停止动画*/
    fun stopAnimation(state: AnimationStateEnum = AnimationStateEnum.STOPPED) {
        if (animationState.isStarted) {
            val old = animationState
            animationState = state
            notifyAnimationState(old)
            return
        }
    }

    /**通知动画状态改变*/
    @ThreadDes("GLThread")
    fun notifyAnimationState(old: AnimationStateEnum) {
        for (listener in listeners) {
            listener.onAnimationStateChanged(old, animationState)
        }
    }

    /**通知绘制进度改变*/
    @ThreadDes("GLThread")
    fun notifyRenderProgressChanged(old: Float, fromUser: Boolean) {
        for (listener in listeners) {
            listener.onRenderProgressChanged(old, renderProgress, fromUser)
        }
    }

    //--

    override fun preRender(scene: OpenGLScene) {
        super.preRender(scene)
        //GLES20.glLineWidth(lineThickness / scene.sceneScaleX)
        GLES20.glLineWidth(lineThickness)
    }

    override fun onRender() {
        super.onRender()

        if (animationState.isPlaying) {
            val old = renderProgress
            renderEndDistance += animationDistanceStep * animationSpeed
            if (renderEndDistance > sumDistance) {
                renderEndDistance = sumDistance
                stopAnimation()
            }
            notifyRenderProgressChanged(old, false)
        }
    }

    //--

    protected var startPositionBuffer: FloatBuffer? = null
    protected var startPositionBufferHandle: Int? = null
    protected var startDistanceBuffer: FloatBuffer? = null
    protected var startDistanceBufferHandle: Int? = null

    fun setStartPositions(array: FloatArray, override: Boolean = false) {
        if (startPositionBuffer == null || override) {
            startPositionBuffer?.clear()
            startPositionBuffer = createFloatBuffer(array)
        } else {
            startPositionBuffer?.put(array)
        }
    }

    fun setStartDistance(array: FloatArray, override: Boolean = false) {
        if (startDistanceBuffer == null || override) {
            startDistanceBuffer?.clear()
            startDistanceBuffer = createFloatBuffer(array)
        } else {
            startDistanceBuffer?.put(array)
        }
    }

    override fun createObjectBuffers() {
        super.createObjectBuffers()

        if (startPositionBuffer != null) {
            startPositionBuffer!!.compact().position(0)
            startPositionBufferHandle = createOpenGLBuffer(
                startPositionBuffer,
                GLES20.GL_ARRAY_BUFFER
            )
        }

        if (startDistanceBuffer != null) {
            startDistanceBuffer!!.compact().position(0)
            startDistanceBufferHandle = createOpenGLBuffer(
                startDistanceBuffer,
                GLES20.GL_ARRAY_BUFFER
            )
        }
    }

    override fun destroy() {
        super.destroy()
        deleteOpenGLBuffers(startPositionBufferHandle)
        startPositionBufferHandle = null
        deleteOpenGLBuffers(startDistanceBufferHandle)
        startDistanceBufferHandle = null
    }

    override fun bindVertexShaderProgram(programHandle: Int) {
        super.bindVertexShaderProgram(programHandle)

        if (startPositionBufferHandle != null) {
            val handle = GLES20.glGetAttribLocation(programHandle, "aStartPosition")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, startPositionBufferHandle!!)
            GLES20.glEnableVertexAttribArray(handle)
            GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FLOAT, false, 0, 0)
        }

        if (startDistanceBufferHandle != null) {
            val handle = GLES20.glGetAttribLocation(programHandle, "aStartDistance")
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, startDistanceBufferHandle!!)
            GLES20.glEnableVertexAttribArray(handle)
            GLES20.glVertexAttribPointer(handle, 1, GLES20.GL_FLOAT, false, 0, 0)
        }

        val uRenderEndDistanceHandle =
            GLES20.glGetUniformLocation(programHandle, "uRenderEndDistance")
        GLES20.glUniform1f(uRenderEndDistanceHandle, renderEndDistance)
    }

    //--

    override fun buildVertexShader(): String {
        return buildString {
            vertexShaderBuilder = this
            //--着色器声明--
            appendLine("precision highp float;")//精度声明
            appendLine("attribute vec4 aPosition;")//顶点坐标
            if (colorsBufferHandle == null) {
                appendLine("uniform vec4 uColor;")//顶点颜色
            } else {
                appendLine("attribute vec4 aVertexColor;")//顶点矢量颜色
            }
            appendLine("attribute vec4 aStartPosition;")//--开始的点位置
            appendLine("attribute float aStartDistance;")//--开始的距离
            appendLine("uniform mat4 uModelViewMatrix;")//模型视图矩阵
            appendLine("uniform mat4 uMVPMatrix;")//模型视图投影矩阵
            appendLine("varying vec4 vColor;")//输出的顶点颜色
            appendLine("varying float vDistance;")//输出当前点距离0,0的位置距离
            //--着色器函数体--
            appendLine("void main() {")
            appendLine("  gl_Position = uMVPMatrix * uModelViewMatrix * aPosition;")
            if (colorsBufferHandle == null) {
                appendLine("  vColor = uColor;")
            } else {
                appendLine("  vColor = aVertexColor;")
            }
            appendLine("  float dis = abs(distance(aPosition.xy, aStartPosition.xy));")
            appendLine("  vDistance = dis + aStartDistance;")
            appendLine("}")
        }
    }

    override fun buildFragmentShader(): String {
        return buildString {
            fragmentShaderBuilder = this
            //--着色器声明--
            append(
                """
                precision highp float;
                varying vec4 vColor;
                varying float vDistance;
                uniform float uRenderEndDistance;
                void main() {
                  if (vDistance > uRenderEndDistance) {
                    discard;
                  } else {
                    gl_FragColor = vColor;
                  }
                }
            """.trimIndent()
            )
        }
    }
}

interface OpenGLGCodeLineListener {

    /**动画状态改变通知*/
    @ThreadDes("GLThread")
    fun onAnimationStateChanged(from: AnimationStateEnum, to: AnimationStateEnum) {
    }

    /**绘制进度改变通知*/
    @ThreadDes("GLThread")
    fun onRenderProgressChanged(from: Float, to: Float, fromUser: Boolean) {
    }

}

/**GCode线段的信息
 * [OpenGLGCodeLine]*/
data class OpenGLGCodeLineData(
    /**线开始的点*/
    val startPoint: PointF,
    /**线结束的点*/
    val endPoint: PointF,
    /**线的类型
     * 0: G0
     * 1: G1*/
    val type: Int = 0,
    /**线的颜色*/
    val color: Int = Color.GREEN,
    /**线开始的距离, 距离0,0的距离*/
    @AutoConfigProperty
    var lineStartDistance: Float = 0f,
    /**线的宽度*/
    val lineThickness: Float = 1f,
) {
    val distance: Float
        get() = startPoint.distance(endPoint)
}
