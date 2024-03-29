package com.angcyo.camerax.dslitem

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.net.Uri
import android.util.Size
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.FocusMeteringResult
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.DslLifecycleCameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.angcyo.camerax.R
import com.angcyo.camerax.core.getBitmapTransform
import com.angcyo.camerax.ui.FocusPointDrawable
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.fragment.requestPermissions
import com.angcyo.http.rx.doMain
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.BooleanAction
import com.angcyo.library.ex.UriAction
import com.angcyo.library.ex._string
import com.angcyo.library.ex.havePermission
import com.angcyo.library.ex.load
import com.angcyo.library.ex.saveToDCIM
import com.angcyo.library.ex.toFileUri
import com.angcyo.library.ex.toListOf
import com.angcyo.library.ex.transform
import com.angcyo.library.libCacheFile
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.widget.DslViewHolder
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/07/02
 */
interface ICameraXItem : IDslItem, ICameraTouchListener {

    /**统一样式配置*/
    var cameraItemConfig: CameraItemConfig

    /**初始化*/
    @CallPoint
    fun initCameraXItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        cameraItemConfig._itemHolder = itemHolder
        val cameraView = cameraItemConfig._previewView
        cameraItemConfig.itemCameraTouchListener = this
        cameraView?.apply {
            requestCameraPermission(itemHolder.context) {
                if (it) {
                    //初始化
                    if (cameraItemConfig.itemCameraPriorityUseController) {
                        initCameraXWithController(itemHolder, this, adapterItem)
                    } else {
                        initCameraXWithProvider(itemHolder, this, adapterItem)
                    }
                    //初始化手势
                    initCameraXTouchListener(itemHolder, this, adapterItem)
                } else {
                    onCameraPermissionDenied(itemHolder)
                }
            }
        }

        //切换摄像头
        itemHolder.click(R.id.lib_camera_switch_view) {
            it.isEnabled = hasCamera()
            switchCamera()
        }

        //闪光灯
        itemHolder.click(R.id.lib_camera_torch_view) {
            it.isEnabled = hasCamera()
            enableCameraTorch(!isTorchEnable())
        }

        //拍照, 预览/显示
        itemHolder.click(R.id.lib_camera_shutter_view) {
            captureCameraPhoto { uri, exception ->
                val action = cameraItemConfig.itemCaptureCameraPhotoAction
                if (action == null) {
                    exception?.let {
                        toastQQ(it.message)
                    }
                    uri?.let {
                        doMain {
                            showCameraPreviewPhoto(it)
                        }
                    }
                } else {
                    //自定义处理
                    action(uri, exception)
                }
            }
        }
        itemHolder.click(R.id.lib_camera_close_photo_preview) {
            hideCameraPreviewPhoto()
        }
    }

    /**初始化一个控制器
     * [com.angcyo.camerax.dslitem.CameraItemConfig.itemCameraPriorityUseController]
     * [initCameraXWithController]
     * */
    fun initItemCameraControllerIfNeed(action: () -> CameraController) {
        if (cameraItemConfig.itemCameraController == null) {
            cameraItemConfig.itemCameraPriorityUseController = true
            cameraItemConfig.itemCameraController = action()
        }
    }

    /**添加一个用例, 只在使用默认的控制器时, 才会自动添加[com.angcyo.camerax.dslitem.ICameraXItem.initCameraXWithController]
     * [buildImageCaptureUseCase]
     * [buildYUVImageAnalysisUseCase]
     * [buildBitmapAnalysisUseCase]
     * */
    fun addCameraItemUseCase(useCase: UseCase) {
        cameraItemConfig.itemCameraUseCaseList.add(useCase)
    }

    //region ---初始化---

    /**入口*/
    @CallPoint
    fun initCameraXTouchListener(
        itemHolder: DslViewHolder,
        previewView: PreviewView,
        adapterItem: DslAdapterItem
    ) {
        if (cameraItemConfig.itemEnableCameraTouch) {
            cameraItemConfig.initCameraTouch()
        }
    }

    /**入口*/
    @CallPoint
    fun initCameraXWithController(
        itemHolder: DslViewHolder,
        previewView: PreviewView,
        adapterItem: DslAdapterItem
    ) {
        val lifecycleOwner = cameraItemConfig.itemCameraLifecycleOwner ?: adapterItem
        var cameraController = cameraItemConfig.itemCameraController
        if (cameraController == null) {
            L.w("正在使用默认的[CameraController]")
            cameraItemConfig.itemCameraController =
                DslLifecycleCameraController(itemHolder.context).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                    cameraSelector = cameraItemConfig.itemCameraSelector
                    if (cameraItemConfig.itemCameraUseCaseList.isNotEmpty()) {
                        useCaseList = cameraItemConfig.itemCameraUseCaseList
                    }
                }
            cameraController = cameraItemConfig.itemCameraController
        }
        if (previewView.controller != cameraController) {
            previewView.controller?.let {
                if (it is LifecycleCameraController) {
                    it.unbind()
                } else if (it is DslLifecycleCameraController) {
                    it.unbind()
                }
            }
            if (cameraController is LifecycleCameraController) {
                cameraController.bindToLifecycle(lifecycleOwner)
            } else if (cameraController is DslLifecycleCameraController) {
                cameraController.bindToLifecycle(lifecycleOwner)
            }
            //set
            previewView.controller = cameraController
        }
    }

    /**入口*/
    @CallPoint
    fun initCameraXWithProvider(
        itemHolder: DslViewHolder,
        previewView: PreviewView,
        adapterItem: DslAdapterItem
    ) {
        wrapCameraProvider {
            // Set up the view finder use case to display camera preview
            val preview = buildPreviewUseCase(previewView.display?.rotation ?: Surface.ROTATION_0)

            // Apply declared configs to CameraX using the same lifecycle owner
            unbindAll()

            val useCaseGroup = UseCaseGroup.Builder().apply {
                addUseCase(preview)
                cameraItemConfig.itemCameraUseCaseList.forEach {
                    addUseCase(it)
                }
            }.build()

            cameraItemConfig._itemCamera = bindToLifecycle(
                cameraItemConfig.itemCameraLifecycleOwner ?: adapterItem,
                cameraItemConfig.itemCameraSelector,
                useCaseGroup
            )


            // Use the camera object to link our preview use case with the view
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    }

    /**
     * 获取一个摄像头提供者
     * [ProcessCameraProvider]*/
    fun wrapCameraProvider(action: ProcessCameraProvider.() -> Unit): ListenableFuture<ProcessCameraProvider> {
        val context = cameraItemConfig._itemHolder?.context ?: lastContext
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // Camera provider is now guaranteed to be available
            val cameraProvider = cameraProviderFuture.get()
            cameraItemConfig._itemCameraProvider = cameraProvider
            action(cameraProvider)
        }, ContextCompat.getMainExecutor(context))
        return cameraProviderFuture
    }

    //endregion ---初始化---

    //region ---Touch---

    override fun onCameraSingleTapUp(event: MotionEvent) {
        if (cameraItemConfig.itemEnableCameraTouch) {
            focusCamera(event.x, event.y)
            showCameraFocusPoint(event.x, event.y)
        }
    }

    override fun onCameraDoubleTap(event: MotionEvent) {
        if (cameraItemConfig.itemEnableCameraDoubleTapSwitchTouch) {
            switchCamera()
        }
    }

    override fun onCameraScale(scaleFactor: Float) {
        if (cameraItemConfig.itemEnableCameraScaleTouch) {
            scaleCamera(scaleFactor)
        }
    }

    //endregion ---Touch---

    //region ---操作---

    /**闪光灯是否已打开*/
    @UiThread
    fun isTorchEnable(): Boolean {
        val cameraInfo = cameraItemConfig._cameraInfo
        return cameraInfo?.torchState?.value == TorchState.ON
    }

    /**启用手电筒或禁用手电筒*/
    @UiThread
    fun enableCameraTorch(enable: Boolean = true): Boolean {
        val cameraInfo = cameraItemConfig._cameraInfo
        return if (cameraInfo?.hasFlashUnit() == true) {
            if (isTorchEnable()) {
                if (enable) {
                    return true
                }
            } else {
                if (!enable) {
                    return true
                }
            }
            cameraItemConfig._cameraControl?.let {
                it.enableTorch(enable)
            }
            true
        } else {
            L.w("当前摄像头不支持手电筒")
            false
        }
    }

    /**对焦*/
    @UiThread
    fun focusCamera(x: Float, y: Float): ListenableFuture<FocusMeteringResult>? {
        val cameraView = cameraItemConfig._previewView
        return cameraView?.run {
            val meteringPointFactory = meteringPointFactory
            val focusPoint = meteringPointFactory.createPoint(x, y)
            focusCamera(focusPoint)
        }
    }

    /**对焦*/
    @UiThread
    fun focusCamera(meteringPoint: MeteringPoint): ListenableFuture<FocusMeteringResult>? {
        return cameraItemConfig._cameraControl?.run {
            val meteringAction = FocusMeteringAction.Builder(meteringPoint).build()
            startFocusAndMetering(meteringAction)
        }
    }

    /**缩放
     * [scaleFactor] 在现有基础上缩放的比例*/
    @UiThread
    fun scaleCamera(scaleFactor: Float): ListenableFuture<Void>? {
        val currentZoomRatio = cameraItemConfig._cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
        return cameraItemConfig._cameraControl?.setZoomRatio(scaleFactor * currentZoomRatio)
    }

    /**线性缩放, 默认可能在0.3左右
     * [linearZoom] [0~1f]*/
    @UiThread
    fun setLinerZoomCamera(linearZoom: Float): ListenableFuture<Void>? {
        return cameraItemConfig._cameraControl?.setLinearZoom(linearZoom)
    }

    /**切换摄像头*/
    @UiThread
    fun switchCamera() {
        val itemHolder = cameraItemConfig._itemHolder ?: return
        val cameraView = cameraItemConfig._previewView ?: return
        val targetSelector =
            if (cameraItemConfig.itemCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        cameraItemConfig.itemCameraSelector = targetSelector
        if (cameraItemConfig.itemCameraPriorityUseController) {
            cameraView.controller?.apply {
                cameraSelector = targetSelector
            }
        } else {
            if (this is DslAdapterItem) {
                initCameraXWithProvider(itemHolder, cameraView, this)
            } else {
                L.w("不支持的操作[switchCamera]请使用 DslAdapterItem")
            }
        }
    }

    @UiThread
    fun hasCamera() = hasBackCamera() || hasFrontCamera()

    @UiThread
    fun hasBackCamera(): Boolean {
        if (cameraItemConfig.itemCameraPriorityUseController) {
            val cameraView = cameraItemConfig._previewView ?: return false
            return cameraView.controller?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true
        } else {
            val cameraProvider = cameraItemConfig._itemCameraProvider ?: return false
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }
    }

    @UiThread
    fun hasFrontCamera(): Boolean {
        if (cameraItemConfig.itemCameraPriorityUseController) {
            val cameraView = cameraItemConfig._previewView ?: return false
            return cameraView.controller?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true
        } else {
            val cameraProvider = cameraItemConfig._itemCameraProvider ?: return false
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        }
    }

    /**显示对焦动画*/
    fun showCameraFocusPoint(x: Float, y: Float) {
        val itemHolder = cameraItemConfig._itemHolder ?: return
        val view = itemHolder.view(R.id.lib_camera_focus_point)
        view ?: return
        val drawable = FocusPointDrawable()
        val strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            itemHolder.context.resources.displayMetrics
        )
        drawable.setStrokeWidth(strokeWidth)

        val alphaAnimation = SpringAnimation(view, DynamicAnimation.ALPHA, 1f).apply {
            spring.stiffness = CameraItemConfig.SPRING_STIFFNESS
            spring.dampingRatio = CameraItemConfig.SPRING_DAMPING_RATIO

            addEndListener { _, _, _, _ ->
                SpringAnimation(view, DynamicAnimation.ALPHA, 0f)
                    .apply {
                        spring.stiffness = CameraItemConfig.SPRING_STIFFNESS_ALPHA_OUT
                        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                    }
                    .start()
            }
        }
        val scaleAnimationX = SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
            spring.stiffness = CameraItemConfig.SPRING_STIFFNESS
            spring.dampingRatio = CameraItemConfig.SPRING_DAMPING_RATIO
        }
        val scaleAnimationY = SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = CameraItemConfig.SPRING_STIFFNESS
            spring.dampingRatio = CameraItemConfig.SPRING_DAMPING_RATIO
        }

        view.apply {
            background = drawable
            isVisible = true
            translationX = x - width / 2f
            translationY = y - height / 2f
            alpha = 0f
            scaleX = 1.5f
            scaleY = 1.5f
        }

        alphaAnimation.start()
        scaleAnimationX.start()
        scaleAnimationY.start()
    }

    /**显示预览图*/
    fun showCameraPreviewPhoto(uri: Uri?) {
        if (uri == null) return
        //后摄: bitmap:图片宽:3072 高:1574 :18.45MB
        //前摄: bitmap:图片宽:2304 高:1180 :10.37MB
        //val bitmap = uri.readBitmap()
        //L.i("bitmap:${bitmap?.logInfo()}")
        cameraItemConfig._itemHolder?.img(R.id.lib_camera_photo_preview)?.isVisible = true
        cameraItemConfig._itemHolder?.img(R.id.lib_camera_photo_preview)?.load(uri)
        cameraItemConfig._itemHolder?.img(R.id.lib_camera_close_photo_preview)?.isVisible = true
    }

    /**关闭预览图*/
    fun hideCameraPreviewPhoto() {
        cameraItemConfig._itemHolder?.img(R.id.lib_camera_photo_preview)?.isVisible = false
        cameraItemConfig._itemHolder?.img(R.id.lib_camera_close_photo_preview)?.isVisible = false
    }

    /**拍照,前摄拍出来的是水平镜像的图片
     * 如果使用的是[itemCameraController], 则需要开启[CameraController.IMAGE_CAPTURE]用例
     * 如果使用的是[androidx.camera.lifecycle.ProcessCameraProvider], 则需要主动调用[ImageCapture.takePicture]方法
     * [buildImageCaptureUseCase]
     * [com.angcyo.camerax.dslitem.CameraItemConfig.itemCameraPriorityUseController]
     * [action] 回调
     *
     * [androidx.camera.core.ImageProxy.getBitmapTransform]
     * */
    @MainThread
    fun captureCameraPhoto(
        photoFile: File = libCacheFile(fileNameUUID(".jpg")),
        @WorkerThread action: UriAction
    ): Boolean {
        if (cameraItemConfig.itemCameraPriorityUseController) {
            val controller = cameraItemConfig.itemCameraController
            if (controller == null) {
                action(null, IllegalStateException("未绑定相机"))
                return false
            }
            if (!controller.isImageCaptureEnabled) {
                action(null, IllegalStateException("未开启拍照功能"))
                return false
            }
            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal =
                    cameraItemConfig.itemCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            }
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            try {
                controller.takePicture(
                    outputFileOptions,
                    Dispatchers.Default.asExecutor(),
                    @WorkerThread
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            if (cameraItemConfig.itemCaptureToDCIM) {
                                saveFileToDCIM(photoFile)
                            }
                            action(outputFileResults.savedUri ?: photoFile.toFileUri(), null)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            action(null, exception)
                        }
                    })
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                action(null, e)
            }
        } else {
            action(null, IllegalStateException("请主动调用[ImageCapture.takePicture]"))
        }
        return false
    }

    /**录像, 需要主动请求权限
     * 如果使用的是[itemCameraController], 则需要开启[CameraController.VIDEO_CAPTURE]用例
     * 如果使用的是[androidx.camera.lifecycle.ProcessCameraProvider], 则需要主动调用[ImageCapture.takePicture]方法
     *
     * [buildVideoCaptureUseCase]
     * */
    @SuppressLint("UnsafeOptInUsageError")
    @MainThread
    fun startRecordingCamera(
        videoFile: File = libCacheFile(fileNameUUID(".mp4")),
        @WorkerThread action: (uri: Uri?, exception: Exception?) -> Unit
    ): Boolean {
        if (cameraItemConfig.itemCameraPriorityUseController) {
            val controller = cameraItemConfig.itemCameraController
            if (controller == null) {
                action(null, IllegalStateException("未绑定相机"))
                return false
            }
            if (!controller.isVideoCaptureEnabled) {
                action(null, IllegalStateException("未开启录像功能"))
                return false
            }

            val outputFileOptions = OutputFileOptions.builder(videoFile)
                .build()

            try {
                controller.startRecording(
                    outputFileOptions,
                    Dispatchers.Default.asExecutor(),
                    @WorkerThread
                    object : OnVideoSavedCallback {

                        override fun onVideoSaved(outputFileResults: OutputFileResults) {
                            if (cameraItemConfig.itemCaptureToDCIM) {
                                saveFileToDCIM(videoFile)
                            }
                            action(outputFileResults.savedUri ?: videoFile.toFileUri(), null)
                        }

                        override fun onError(
                            videoCaptureError: Int,
                            message: String,
                            cause: Throwable?
                        ) {
                            L.w("$videoCaptureError $message $cause")
                            action(null, Exception(message, cause))
                        }
                    })
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                action(null, e)
            }
        } else {
            action(null, IllegalStateException("请主动调用[VideoCapture.startRecording]"))
        }
        return false
    }

    /**将文件保存至DCIM*/
    fun saveFileToDCIM(file: File) {
        (cameraItemConfig._itemHolder?.context ?: lastContext).saveToDCIM(file)
    }

    /**停止录制, 请在[startRecordingCamera]后调用*/
    @SuppressLint("UnsafeOptInUsageError")
    @MainThread
    fun stopRecordingCamera() {
        if (cameraItemConfig.itemCameraPriorityUseController) {
            val controller = cameraItemConfig.itemCameraController
            controller?.stopRecording()
        } else {
            L.w("请主动调用[VideoCapture.startRecording]")
        }
    }

    /**获取摄像头支持的输出分辨率
     * ## ImageFormat.YUV_420_888
     * ```
     * ImageFormat.YUV_420_888:4096x3072, 4000x3000, 4096x2304, 3072x3072, 3840x2160, 3280x2464,
     * 3264x2448, 4096x1834, 3840x1644, 3264x1836, 2448x2448, 2752x2064, 2592x1944,
     * 3264x1472, 3216x1440, 2304x1728, 2560x1440, 2304x1296, 1728x1728, 1920x1440,
     * 2520x1080, 2412x1080, 2304x1036, 2160x1080, 1920x1080, 1600x1200, 1440x1080,
     * 1200x1200, 1600x800, 1280x960, 1080x1080, 1608x720, 1280x720, 1024x768, 1206x540,
     * 864x480, 720x540, 720x480, 800x400, 640x480, 804x360, 640x360, 352x288, 320x240, 176x144
     * ```
     * ## ImageFormat.RGB_565
     * ```
     * null
     * ```
     * */
    fun getCameraSupportOutputSizes(format: Int = ImageFormat.YUV_420_888): Array<Size>? {
        return cameraItemConfig._cameraInfo?.run {
            com.angcyo.camerax.core.Camera.getStreamConfigurationMap(this)
                ?.getOutputSizes(format)
        }
    }

    //endregion ---操作---

    //region ---UseCase---

    /**创建一个预览的用例*/
    fun buildPreviewUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: Preview.Builder.() -> Unit = {}
    ): Preview {
        return Preview.Builder()
            //setTargetAspectRatio 和 setTargetResolution 不能同时使用
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .apply(action)
            .build()
    }

    /**创建一个截图的用例*/
    fun buildImageCaptureUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: ImageCapture.Builder.() -> Unit = {},
    ): ImageCapture {
        return ImageCapture.Builder()
            //setTargetAspectRatio 和 setTargetResolution 不能同时使用
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            //.setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) //最大化质量
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) //最小化延迟
            .apply(action)
            .build()
    }

    /**创建一个录像的用例*/
    @SuppressLint("RestrictedApi")
    fun buildVideoCaptureUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: VideoCapture.Builder.() -> Unit = {},
    ): VideoCapture {
        return VideoCapture.Builder()
            //setTargetAspectRatio 和 setTargetResolution 不能同时使用
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .setVideoFrameRate(30)
            .setBitRate(3 * 1024 * 1024)
            .setAudioBitRate(128 * 1024)
            .setAudioSampleRate(44100)
            .build()
    }

    /**创建一个图片分析的用例*/
    fun buildImageAnalysisUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: ImageAnalysis.Builder.() -> Unit = {},
    ): ImageAnalysis {
        return ImageAnalysis.Builder()
            //setTargetAspectRatio 和 setTargetResolution 不能同时使用
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
            //.setTargetResolution()
            .setTargetRotation(rotation)
            //.setOutputImageRotationEnabled()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .apply(action)
            .build()
    }

    /**[buildImageAnalysisUseCase]*/
    @SuppressLint("RestrictedApi")
    fun buildYUVImageAnalysisUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: ImageAnalysis.Builder.() -> Unit = {},
        onImageAvailable: (imageProxy: ImageProxy, matrix: Matrix?) -> Unit
    ): ImageAnalysis {
        return buildImageAnalysisUseCase(rotation) {
            setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            action()
        }.apply {
            setAnalyzer(CameraXExecutors.directExecutor()) { imageProxy ->
                val matrix = imageProxy.getBitmapTransform(cameraItemConfig.itemCameraSelector)
                onImageAvailable(imageProxy, matrix)
                //关闭之后, 才有下一帧
                //imageProxy.close()
            }
        }
    }

    /**创建一个图片分析的用例
     * [buildImageAnalysisUseCase]*/
    @SuppressLint("RestrictedApi")
    fun buildBitmapAnalysisUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: ImageAnalysis.Builder.() -> Unit = {},
        onImageAvailable: (imageProxy: ImageProxy, bitmap: Bitmap) -> Unit
    ): ImageAnalysis {
        return buildImageAnalysisUseCase(rotation, action).apply {

            setAnalyzer(CameraXExecutors.directExecutor()) { imageProxy ->
                var bitmap = Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
                val matrix = imageProxy.getBitmapTransform(cameraItemConfig.itemCameraSelector)
                bitmap = bitmap.transform(matrix)
                onImageAvailable(imageProxy, bitmap)
                //关闭之后, 才有下一帧
                //imageProxy.close()
            }
        }
    }

    /**[buildBitmapAnalysisUseCase]*/
    @SuppressLint("RestrictedApi")
    fun buildBitmapAnalysisUseCase(
        rotation: Int = Surface.ROTATION_0,
        action: ImageAnalysis.Builder.() -> Unit = {},
        analyzer: ImageAnalysis.Analyzer
    ): ImageAnalysis {
        return buildImageAnalysisUseCase(rotation, action).apply {
            setAnalyzer(CameraXExecutors.directExecutor(), analyzer)
        }
    }

    //endregion ---UseCase---

    //region ---权限---

    /**请求摄像头权限*/
    fun requestCameraPermission(context: Context, action: BooleanAction) {
        val list = android.Manifest.permission.CAMERA.toListOf()
        requestPermission(context, list, action)
    }

    /**[requestCameraPermission]*/
    fun onCameraPermissionDenied(itemHolder: DslViewHolder) {
        toastQQ(_string(R.string.permission_disabled))
    }

    /**请求权限*/
    fun requestPermission(context: Context, list: List<String>, action: BooleanAction) {
        if (context.havePermission(list)) {
            action.invoke(true)
        } else {
            //请求权限
            if (context is FragmentActivity) {
                context.requestPermissions(list, action)
            } else if (lastContext is FragmentActivity) {
                (lastContext as FragmentActivity).requestPermissions(list, action)
            } else {
                context.requestPermissions(list, action)
            }
        }
    }

    //endregion ---权限---
}

/**声明周期控制*/
var ICameraXItem.itemCameraLifecycleOwner: LifecycleOwner?
    get() = cameraItemConfig.itemCameraLifecycleOwner
    set(value) {
        cameraItemConfig.itemCameraLifecycleOwner = value
    }

/**相机控制/用例控制*/
var ICameraXItem.itemCameraController: CameraController?
    get() = cameraItemConfig.itemCameraController
    set(value) {
        cameraItemConfig.itemCameraController = value
    }

var ICameraXItem.itemCameraPriorityUseController: Boolean
    get() = cameraItemConfig.itemCameraPriorityUseController
    set(value) {
        cameraItemConfig.itemCameraPriorityUseController = value
    }

var ICameraXItem.itemEnableCameraTouch: Boolean
    get() = cameraItemConfig.itemEnableCameraTouch
    set(value) {
        cameraItemConfig.itemEnableCameraTouch = value
    }

var ICameraXItem.itemCaptureToDCIM: Boolean
    get() = cameraItemConfig.itemCaptureToDCIM
    set(value) {
        cameraItemConfig.itemCaptureToDCIM = value
    }

var ICameraXItem.itemCaptureCameraPhotoAction: UriAction?
    get() = cameraItemConfig.itemCaptureCameraPhotoAction
    set(value) {
        cameraItemConfig.itemCaptureCameraPhotoAction = value
    }

/**Camera手势监听*/
interface ICameraTouchListener {
    /**简单的点击, 需要手动对焦*/
    fun onCameraSingleTapUp(event: MotionEvent)

    /**双击, 需要切换摄像头*/
    fun onCameraDoubleTap(event: MotionEvent)

    /**缩放*/
    fun onCameraScale(scaleFactor: Float)
}

class CameraItemConfig : IDslItemConfig {

    /**关键控件id: 预览*/
    var itemCameraViewId = R.id.lib_camera_view

    /**需要绑定的生命周期, 不指定则使用[DslAdapterItem]的生命周期*/
    var itemCameraLifecycleOwner: LifecycleOwner? = null

    /**是否优先使用[itemCameraController]控制摄像头*/
    var itemCameraPriorityUseController: Boolean = true

    /**摄像头控制器, 自动预览时, 必须赋值
     * [androidx.camera.view.LifecycleCameraController]*/
    var itemCameraController: CameraController? = null

    /**额外的特性, 默认一定会有预览*/
    var itemCameraUseCaseList = mutableListOf<UseCase>()

    /**默认选择的摄像头*/
    var itemCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    /**激活的用例
     * [CameraController.IMAGE_CAPTURE]
     * [CameraController.IMAGE_ANALYSIS]
     * [CameraController.VIDEO_CAPTURE]
     * */
    //var itemEnableUseCases: Int = CameraController.IMAGE_CAPTURE

    /**拍照捕捉回调, 不设置会使用默认的处理*/
    var itemCaptureCameraPhotoAction: UriAction? = null

    /**捕捉的图片/视频是否保存一份到DCIM*/
    var itemCaptureToDCIM: Boolean = false

    //--

    var _itemHolder: DslViewHolder? = null

    /**在不使用[itemCameraPriorityUseController]的情况下, 才有值*/
    var _itemCameraProvider: ProcessCameraProvider? = null

    /**在不使用[itemCameraPriorityUseController]的情况下, 才有值
     *
     * [ProcessCameraProvider]*/
    var _itemCamera: Camera? = null

    val _previewView: PreviewView?
        get() = _itemHolder?.v(itemCameraViewId)

    /**[CameraControl]*/
    val _cameraControl: CameraControl?
        get() = _previewView?.controller?.cameraControl ?: _itemCamera?.cameraControl

    /**[CameraInfo]*/
    val _cameraInfo: CameraInfo?
        get() = _previewView?.controller?.cameraInfo ?: _itemCamera?.cameraInfo

    //region ---touch---

    /**激活手势总开关*/
    var itemEnableCameraTouch: Boolean = true

    /**是否激活双击切换摄像头手势*/
    var itemEnableCameraDoubleTapSwitchTouch: Boolean = false

    /**是否激活缩放摄像头手势*/
    var itemEnableCameraScaleTouch: Boolean = false

    /**手势后的实现操作监听*/
    var itemCameraTouchListener: ICameraTouchListener? = null

    /**点击对焦手势处理*/
    var itemCameraGestureDetector =
        GestureDetectorCompat(lastContext, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                itemCameraTouchListener?.onCameraSingleTapUp(e)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                itemCameraTouchListener?.onCameraDoubleTap(e)
                return true
            }
        })

    /**缩放手势处理*/
    var itemCameraScaleGestureDetector = ScaleGestureDetector(
        lastContext,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                itemCameraTouchListener?.onCameraScale(detector.scaleFactor)
                return true
            }
        })

    /**手势入口*/
    @SuppressLint("ClickableViewAccessibility")
    var itemCameraViewTouchListener = View.OnTouchListener { view, event ->
        var didConsume = itemCameraScaleGestureDetector.onTouchEvent(event)
        if (!itemCameraScaleGestureDetector.isInProgress) {
            didConsume = itemCameraGestureDetector.onTouchEvent(event)
        }
        didConsume
    }

    @CallPoint
    fun initCameraTouch() {
        _previewView?.setOnTouchListener(itemCameraViewTouchListener)
    }

    //endregion ---touch---

    companion object {
        // animation constants for focus point
        internal const val SPRING_STIFFNESS_ALPHA_OUT = 100f
        internal const val SPRING_STIFFNESS = 800f
        internal const val SPRING_DAMPING_RATIO = 0.35f
    }

}