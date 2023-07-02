package androidx.camera.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.camera.core.Camera
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.impl.utils.Threads
import androidx.lifecycle.LifecycleOwner

/**
 *
 * [androidx.camera.view.LifecycleCameraController]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/22
 */
@SuppressLint("RestrictedApi")
class DslLifecycleCameraController(context: Context) : CameraController(context) {

    val TAG = "CamLifecycleController"

    /**自定义的Case, 额外需要开启的功能
     * [androidx.camera.core.UseCase]*/
    var useCaseGroup: UseCaseGroup? = null

    private var lifecycleOwner: LifecycleOwner? = null

    init {
        //关闭所有的默认功能
        //setEnabledUseCases(0)
        setEnabledUseCases(IMAGE_CAPTURE/*or IMAGE_ANALYSIS or VIDEO_CAPTURE*/) //仅开启预览功能
        //选择摄像头
        cameraSelector
    }

    /**
     * Sets the [LifecycleOwner] to be bound with the controller.
     *
     *
     *  The state of the lifecycle will determine when the cameras are open, started, stopped
     * and closed. When the [LifecycleOwner]'s state is start or greater, the controller
     * receives camera data. It stops once the [LifecycleOwner] is destroyed.
     *
     * @throws IllegalStateException If the provided camera selector is unable to resolve a
     * camera to be used for the given use cases.
     * @see ProcessCameraProvider.bindToLifecycle
     */
    @SuppressLint("MissingPermission")
    @MainThread
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        Threads.checkMainThread()
        this.lifecycleOwner = lifecycleOwner
        startCameraAndTrackStates()
    }

    /**[bindToLifecycle]
     * [caseList] 额外需要开启的功能*/
    @SuppressLint("MissingPermission")
    @MainThread
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, caseList: List<UseCase>?) {
        if (caseList.isNullOrEmpty()) {
            bindToLifecycle(lifecycleOwner)
        } else {
            bindToLifecycle(lifecycleOwner, UseCaseGroup.Builder().apply {
                caseList.forEach {
                    addUseCase(it)
                }
            }.build())
        }
    }

    /**[bindToLifecycle]*/
    @SuppressLint("MissingPermission")
    @MainThread
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, useCaseGroup: UseCaseGroup) {
        this.useCaseGroup = useCaseGroup
        bindToLifecycle(lifecycleOwner)
    }

    override fun createUseCaseGroup(): UseCaseGroup? {
        //mImageAnalysis
        val originGroup = super.createUseCaseGroup()
        if (originGroup == null && useCaseGroup == null) {
            return null
        }
        val resultGroup = UseCaseGroup.Builder().apply {
            originGroup?.useCases?.forEach {
                addUseCase(it)
            }
            useCaseGroup?.useCases?.forEach {
                addUseCase(it)
            }
        }.build()
        return resultGroup
    }

    /**
     * Clears the previously set [LifecycleOwner] and stops the camera.
     *
     * @see ProcessCameraProvider.unbindAll
     */
    @MainThread
    fun unbind() {
        Threads.checkMainThread()
        lifecycleOwner = null
        mCamera = null
        mCameraProvider?.unbindAll()
    }

    /**
     * Unbind and rebind all use cases to [LifecycleOwner].
     *
     * @return null if failed to start camera.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    override fun startCamera(): Camera? {
        val owner = lifecycleOwner
        if (owner == null) {
            Log.d(TAG, "Lifecycle is not set.")
            return null
        }
        val cameraProvider = mCameraProvider
        if (cameraProvider == null) {
            Log.d(TAG, "CameraProvider is not ready.")
            return null
        }
        // Use cases can't be created.
        val useCaseGroup: UseCaseGroup = createUseCaseGroup() ?: return null
        return cameraProvider.bindToLifecycle(owner, mCameraSelector, useCaseGroup)
    }

    /**
     * 关闭所有的相机, 并且释放资源
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    fun shutDownForTests() {
        mCameraProvider?.unbindAll()
        mCameraProvider?.shutdown()
    }

    //region ---

    //endregion
}