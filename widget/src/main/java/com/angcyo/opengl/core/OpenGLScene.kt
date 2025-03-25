package com.angcyo.opengl.core

import android.opengl.GLES20
import com.angcyo.library.L
import java.util.Collections
import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArrayList

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
    protected var mVPMatrix = Matrix4()

    /**
     * [BaseOpenGLRenderer.onRender]
     * [BaseOpenGLRenderer.render]
     * */
    fun render(ellapsedTime: Long, deltaTime: Double) {
        performFrameTasks() //Handle the task queue
        synchronized(mChildren) {
            var i = 0
            val j: Int = mChildren.size
            while (i < j) {
                // Model matrix updates are deferred to the render method due to parent matrix needs
                mChildren[i].render(mVPMatrix, mPMatrix, mVMatrix)
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
        //mCamera.setProjectionMatrix(width, height)
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

    protected var mLastWidth: Int = 0
    protected var mLastHeight: Int = 0

    fun setProjectionMatrix(width: Int, height: Int) {
        /*synchronized(mFrustumLock) {
            if (mLastWidth != width || mLastHeight != height) mCameraDirty = true
            mLastWidth = width
            mLastHeight = height
            val ratio = (width.toDouble()) / (height.toDouble())
            mProjMatrix.setToPerspective(mNearPlane, mFarPlane, mFieldOfView, ratio)
            mIsInitialized = true
        }*/
    }
}