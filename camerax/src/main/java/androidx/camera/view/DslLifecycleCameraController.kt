package androidx.camera.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.camera.core.Camera
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.impl.utils.Threads
import androidx.camera.view.transform.OutputTransform
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.ex.MatrixAction

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
    var useCaseList: List<UseCase>? = null

    /**预览变换监听*/
    var updatePreviewViewTransformList = mutableListOf<MatrixAction>()

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
        useCaseList = caseList
        bindToLifecycle(lifecycleOwner)
    }

    /**添加一个用例*/
    fun addCameraUseCase(useCase: UseCase) {
        if (useCaseList == null) {
            useCaseList = mutableListOf()
        }
        useCaseList = useCaseList?.toMutableList()?.apply {
            add(useCase)
        }
    }

    private var _effects: List<CameraEffect>? = null

    override fun setEffects(effects: MutableList<CameraEffect>) {
        super.setEffects(effects)
        _effects = effects
    }

    override fun createUseCaseGroup(): UseCaseGroup? {
        //mImageAnalysis
        val originGroup = super.createUseCaseGroup()
        if (useCaseList == null || originGroup == null) {
            return originGroup
        }
        val resultGroup = UseCaseGroup.Builder().apply {
            mViewPort?.let { viewPort ->
                setViewPort(viewPort)
            }
            _effects?.forEach { effect ->
                addEffect(effect)
            }
            originGroup.useCases.forEach { case ->
                addUseCase(case)
            }
            useCaseList?.forEach { case ->
                addUseCase(case)
            }
        }.build()
        return resultGroup
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun updatePreviewViewTransform(outputTransform: OutputTransform?) {
        super.updatePreviewViewTransform(outputTransform)
        for (action in updatePreviewViewTransformList) {
            action.invoke(outputTransform?.matrix)
        }
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

    override fun setCameraSelector(cameraSelector: CameraSelector) {
        Threads.checkMainThread()
        if (mCameraSelector == cameraSelector) {
            return
        }
        useCaseList?.let {
            //必须要解绑所有
            mCameraProvider?.unbind(*it.toTypedArray())
        }
        super.setCameraSelector(cameraSelector)
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