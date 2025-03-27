package com.angcyo.opengl.core

import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.Matrix
import com.angcyo.library.annotation.Api
import com.angcyo.library.ex.expand
import java.util.Collections
import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * [BaseOpenGLRenderer]需要绘制的场景
 *
 */
open class OpenGLScene(val renderer: BaseOpenGLRenderer) {

    private var mChildren: MutableList<OpenGLObject> =
        Collections.synchronizedList(CopyOnWriteArrayList())
    protected var mAntiAliasingConfig: ANTI_ALIASING_CONFIG? = null

    /**场景的背景颜色 R G B A [0~1]*/
    var sceneBackgroundColor = floatArrayOf(1f, 1f, 1f, 1f)

    //region --override--

    /**
     * Called by the renderer after [Renderer.initScene].
     */
    fun initScene() {
    }

    /**
     * Reloads this scene.
     */
    fun reload() {
        synchronized(mChildren) {
            var i = 0
            val j: Int = mChildren.size
            while (i < j) {
                // Model matrix updates are deferred to the render method due to parent matrix needs
                mChildren[i].reload()
                ++i
            }
        }
    }

    /**
     * Clears any references the scene is holding for its contents. This does
     * not clear the items themselves as they may be held by some other scene.
     */
    fun destroyScene() {
        /*clearAnimations()
        clearCameras()
        clearLights()
        clearPlugins()
        clearChildren()
        clearFrameCallbacks()*/
    }

    protected var mVMatrix = Matrix4()
    protected var mPMatrix = Matrix4()

    /**[setProjectionMatrix]*/
    protected var mVPMatrix = Matrix4()

    /**
     * [BaseOpenGLRenderer.onRender]
     * [BaseOpenGLRenderer.render]
     *
     * [setProjectionMatrix]
     * */
    open fun render(ellapsedTime: Long, deltaTime: Double) {
        performFrameTasks() //Handle the task queue

        GLES20.glClearColor(
            sceneBackgroundColor[0],
            sceneBackgroundColor[1],
            sceneBackgroundColor[2],
            sceneBackgroundColor[3]
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        synchronized(mChildren) {
            var i = 0
            val j: Int = mChildren.size
            while (i < j) {
                // Model matrix updates are deferred to the render method due to parent matrix needs
                mChildren[i].render(this, mVPMatrix, mPMatrix, mVMatrix)
                ++i
            }
        }
    }

    //endregion --override--


    /**
     * Updates the projection matrix of the current camera for new view port dimensions.
     *
     * @param width int the new viewport width in pixels.
     * @param height in the new viewport height in pixes.
     *
     * [setProjectionMatrix]
     */
    fun updateProjectionMatrix(width: Int, height: Int) {
        setProjectionMatrix(width, height)
    }

    fun setAntiAliasingConfig(config: ANTI_ALIASING_CONFIG) {
        mAntiAliasingConfig = config
    }

    /**
     * Applies the Rajawali default GL state to the driver. Developers who wish
     * to change this default behavior can override this method.
     */
    fun resetGLState() {
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    /**
     * Requests the addition of a child to the scene. The child
     * will be added to the end of the list.
     *
     * @param child [Object3D] child to be added.
     * @return True if the child was successfully queued for addition.
     */
    fun addChild(child: OpenGLObject): Boolean {
        val task: OpenGLFrameTask = object : OpenGLFrameTask() {
            protected override fun doTask() {
                mChildren.add(child)
                /*if (mSceneGraph != null) {
                    //mSceneGraph.addObject(child); //TODO: Uncomment
                }
                addShadowMapMaterialPlugin(
                    child,
                    if (mShadowMapMaterial == null) null else mShadowMapMaterial.getMaterialPlugin()
                )*/
            }
        }
        return internalOfferTask(task)
    }

    private val mFrameTaskQueue: LinkedList<OpenGLFrameTask> = LinkedList<OpenGLFrameTask>()


    /**
     * Adds a task to the frame task queue.
     *
     * @param task AFrameTask to be added.
     * @return boolean True on successful addition to queue.
     */
    private fun internalOfferTask(task: OpenGLFrameTask): Boolean {
        synchronized(mFrameTaskQueue) {
            return mFrameTaskQueue.offer(task)
        }
    }

    /**
     * Internal method for performing frame tasks. Should be called at the
     * start of onDrawFrame() prior to render().
     */
    private fun performFrameTasks() {
        synchronized(mFrameTaskQueue) {
            //Fetch the first task
            var task: OpenGLFrameTask? = mFrameTaskQueue.poll()
            while (task != null) {
                task.run()
                //Retrieve the next task
                task = mFrameTaskQueue.poll()
            }
        }
    }
    //--

    /**最后一次的视口宽度
     * [setProjectionMatrix]*/
    var lastWidth: Int = 0

    /**最后一次的视口宽度
     * [setProjectionMatrix]*/
    var lastHeight: Int = 0

    //--
    protected var mNearPlane = 1.0f
    protected var mFarPlane = 2f //120.0f
    protected var mFieldOfView = 45.0f

    /**[render]
     *
     * [updateProjectionMatrix]
     * */
    fun setProjectionMatrix(width: Int, height: Int) {
        /*if (mLastWidth != width || mLastHeight != height) mCameraDirty = true*/
        lastWidth = width
        lastHeight = height

        //mVPMatrix.setToPerspective(mNearPlane, mFarPlane, mFieldOfView, ratio)

        mVPMatrix.identity()
        if (width > height) {
            val ratio = (height.toFloat()) / (width.toFloat())
            Matrix.scaleM(mVPMatrix.getFloatValues(), 0, ratio, -1f, 1f)
        } else {
            val ratio = (width.toFloat()) / (height.toFloat())
            Matrix.scaleM(mVPMatrix.getFloatValues(), 0, 1f, -ratio, 1f)
        }

        // Create a camera view matrix
        /*Matrix.setLookAtM(
            mVPMatrix.getFloatValues(), 0,
            0f, 0f, -3f,
            0f, 0f, 0f,
            0f, 1.0f, 0.0f
        )*/

        // create a projection matrix from device screen geometry
        /*Matrix.frustumM(
            mVPMatrix.getFloatValues(), 0,
            -ratio, ratio, -1f, 1f,
            3f, 7f
        )*/
    }

    //region --api--

    val sceneScaleX: Float
        get() = mVMatrix.sx

    val sceneScaleY: Float
        get() = mVMatrix.sy

    val sceneScaleZ: Float
        get() = mVMatrix.sz

    val sceneTranslateX: Float
        get() = mVMatrix.tx

    val sceneTranslateY: Float
        get() = mVMatrix.ty

    val sceneTranslateZ: Float
        get() = mVMatrix.tz

    /**缩放场景
     * [scaleSceneTo]*/
    @Api
    fun scaleSceneBy(sx: Float = 1f, sy: Float = 1f, sz: Float = 1f) {
        mVMatrix.scaleBy(sx, sy, sz)
    }

    /**[scaleSceneBy]*/
    @Api
    fun scaleSceneTo(sx: Float = 1f, sy: Float = 1f, sz: Float = 1f) {
        mVMatrix.scaleTo(sx, sy, sz)
    }

    /**移动场景
     * [translateSceneTo]*/
    @Api
    fun translateSceneBy(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        mVMatrix.translateBy(x, y, z)
    }

    /**[translateSceneBy]*/
    @Api
    fun translateSceneTo(x: Float? = null, y: Float? = null, z: Float? = null) {
        mVMatrix.translateTo(x, y, z)
    }

    /**将一个矩形, 完全显示在场景中
     * [rect] 完全显示的矩形
     * [padding] 边界需要留的距离, 相对于视图的大小*/
    @Api
    fun fitScene(rect: RectF?, padding: RectF? = null) {
        if (rect == null) {
            return
        }
        val width = rect.width()
        val height = rect.height()

        val refWidth = width / 2
        val refHeight = height / 2

        val scaleX = 1 / refWidth
        val scaleY = 1 / refHeight

        //等比缩放到完全显示
        val scale = min(scaleX, scaleY)
        if (padding != null) {
            fitScene(
                /*RectF(rect).expand(
                    padding.left / scale,
                    padding.top / scale,
                    padding.right / scale,
                    padding.bottom / scale
                )*/
                RectF(rect).expand(
                    padding.left,
                    padding.top,
                    padding.right,
                    padding.bottom
                )
            )
            return
        }
        scaleSceneTo(scale, scale)

        //将矩形中心移动至屏幕中心, OpenGL的坐标系的坐标值范围是[-1f~1f]
        //移动时的量, 也是OpenGL坐标系中的量
        //OpenGL标准化设备坐标(Normalized Device Coordinates, NDC)
        val refSize = max(refWidth, refHeight)
        translateSceneTo(
            -rect.centerX() / refSize,
            -rect.centerY() / refSize,
        )

        //这种平移只能将矩形的左上角移动到屏幕左上角
        //translateSceneTo(-1f - rect.left / refSize, -1f - rect.top / refSize)
        //translateSceneTo(-1f, -1f)
    }

    fun testMatrix() {
        //Matrix.setLookAtM(mVMatrix.m,0, )
        Matrix.translateM(mVMatrix.m, 0, -2f, -2f, 0f)
    }

    //endregion --api--
}