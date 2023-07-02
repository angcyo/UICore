package com.angcyo.camerax.control

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.angcyo.camerax.core.Camera
import com.angcyo.camerax.core.RGBImageAnalysisAnalyzer
import com.angcyo.fragment.requestPermissions
import com.angcyo.library.BuildConfig
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.TestPoint
import com.angcyo.library.component.ThreadExecutor
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.havePermission
import com.angcyo.library.ex.toFileUri
import com.angcyo.library.libCacheFile
import com.angcyo.library.utils.fileNameUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

/**
 * 摄像机预览控制
 * [androidx.camera.view.PreviewView]
 *
 * [android.app.Application]实现[CameraXConfig.Provider],用于实例化CameraX
 *
 * 预览模式: https://developer.android.com/training/camerax/preview?hl=zh-cn#implementation-mode
 * [androidx.camera.view.PreviewView.setImplementationMode]
 * [androidx.camera.view.PreviewView.ImplementationMode.PERFORMANCE] //默认, 性能模式SurfaceView, 自动降级到兼容模式
 * [androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE] //兼容模式, TextureView
 *
 * 缩放类型: https://developer.android.com/training/camerax/preview?hl=zh-cn#scale-type
 * [androidx.camera.view.PreviewView.setScaleType]
 * [androidx.camera.view.PreviewTransformation.DEFAULT_SCALE_TYPE]
 * [androidx.camera.view.PreviewView.ScaleType.FILL_CENTER] //默认, 填充中心
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/17
 */
class CameraXPreviewControl {

    /**需要的权限*/
    var previewPermissionList = listOf(Manifest.permission.CAMERA)

    /**核心[CameraView]*/
    var cameraView: PreviewView? = null

    /**控制器*/
    var _lifecycleCameraController: LifecycleCameraController? = null

    /**RGB图片分析*/
    var rgbImageAnalysisAnalyzer = RGBImageAnalysisAnalyzer()

    /**当前使用的摄像头*/
    var _cameraSelector: CameraSelector? = null

    init {
        //CameraXConfig.Provider
    }

    /**绑定到声明周期, 并且开始预览
     * 需要权限[Manifest.permission.CAMERA]
     * [requestPermission]*/
    @SuppressLint("UnsafeOptInUsageError")
    @CallPoint
    @MainThread
    fun bindToLifecycle(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ): CameraController {
        cameraView = previewView
        if (previewView.controller == null) {
            val lifecycleCameraController = LifecycleCameraController(previewView.context).apply {
                //setEnabledUseCases(itemEnabledUseCases)
                isPinchToZoomEnabled //是否支持缩放
                isTapToFocusEnabled //是否支持点击对焦
                //isVideoCaptureEnabled
                isImageCaptureEnabled
                isImageAnalysisEnabled

                //设置需要使用的功能, 默认IMAGE_CRGBImageAnalysisAnalyzer()APTURE | IMAGE_ANALYSIS
                //IMAGE_CAPTURE | IMAGE_ANALYSIS | VIDEO_CAPTURE

                //前置摄像头
                if (BuildConfig.DEBUG) {
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }
                //后置摄像头, 默认
                //cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                //开始分析图像
                //setEnabledUseCases(CameraController.IMAGE_ANALYSIS) //仅分析图像
                setImageAnalysisAnalyzer(
                    ThreadExecutor /*previewView.context.mainExecutor()*/,
                    rgbImageAnalysisAnalyzer.apply {
                        analyzerCameraSelector = cameraSelector
                    }
                )
                //clearImageAnalysisAnalyzer()

                //图片捕获模式, 质量优先or速度优先
                imageCaptureMode //CAPTURE_MODE_MINIMIZE_LATENCY 1

                //闪光灯模式
                imageCaptureFlashMode //FLASH_MODE_OFF 2

                //图片捕获目标大小
                previewTargetSize
                imageAnalysisTargetSize
                imageCaptureTargetSize
                videoCaptureTargetSize

                //16:9
                imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
                //imageAnalysisTargetSize = CameraController.OutputSize(Size(3280, 2464))

                //缩放状态发生更改回调
                zoomState.observe(lifecycleOwner) {
                    L.d("摄像头缩放:$it")
                }

                tapToFocusState.observe(lifecycleOwner) {
                    L.d("摄像头对焦:$it")
                }

                _cameraSelector = cameraSelector
                bindToLifecycle(lifecycleOwner)
            }
            _lifecycleCameraController = lifecycleCameraController

            //设置预览控制器
            previewView.controller = lifecycleCameraController

            previewView.post {
                //获取当前连接的摄像头信息
                L.d("摄像头信息:${lifecycleCameraController.cameraInfo}")

                //Camera相关操作需要等待
                //[androidx.camera.view.CameraController.startCameraAndTrackStates(java.lang.Runnable)]
                //方法执行完毕后, 才能获取到正确的信息

                lifecycleCameraController.cameraControl?.cancelFocusAndMetering()
                //lifecycleCameraController.cameraControl?.startFocusAndMetering()
            }
        }
        return previewView.controller!!
    }

    /**请求权限*/
    fun requestPermission(result: (granted: Boolean) -> Unit): Boolean =
        lastContext.requestPermissions(previewPermissionList, result)

    /**是否有权限*/
    fun havePermission(): Boolean = lastContext.havePermission(previewPermissionList)

    fun hasCamera() = hasBackCamera() || hasFrontCamera()

    fun hasBackCamera() =
        _lifecycleCameraController?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true

    fun hasFrontCamera() =
        _lifecycleCameraController?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true

    /**解绑, 并停止相机*/
    @CallPoint
    @MainThread
    fun unbind() {
        cameraView?.controller?.apply {
            if (this is LifecycleCameraController) {
                unbind()
            } else {
                L.w("不支持的解绑操作")
            }
        }
        _lifecycleCameraController = null
    }

    /**切换摄像头*/
    fun switchCamera() {
        cameraView?.controller?.apply {
            if (this is LifecycleCameraController) {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                _cameraSelector = cameraSelector
                rgbImageAnalysisAnalyzer.analyzerCameraSelector = cameraSelector
            } else {
                L.w("不支持的切换操作")
            }
        }
    }

    /**对焦*/
    fun focus(x: Float, y: Float) {
        cameraView?.let {
            val meteringPointFactory = it.meteringPointFactory
            val focusPoint = meteringPointFactory.createPoint(x, y)
            focus(focusPoint)
        }
    }

    /**对焦*/
    fun focus(meteringPoint: MeteringPoint) {
        cameraView?.controller?.cameraControl?.apply {
            val meteringAction = FocusMeteringAction.Builder(meteringPoint).build()
            startFocusAndMetering(meteringAction)
        }
    }

    /**缩放
     * [scaleFactor] 在现有基础上缩放的比例*/
    fun scale(scaleFactor: Float) {
        cameraView?.controller?.apply {
            cameraControl?.apply {
                val currentZoomRatio: Float = cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                setZoomRatio(scaleFactor * currentZoomRatio)
            }
        }
    }

    /**更新分析图片目标大小*/
    @TestPoint
    fun updateImageAnalysisTargetSize() {
        _lifecycleCameraController?.apply {
            Camera.getStreamConfigurationMap(cameraInfo)
                ?.getOutputSizes(ImageFormat.YUV_420_888)?.apply {
                    val size = get(0)
                    imageAnalysisTargetSize = CameraController.OutputSize(size)
                    L.i("更新分析目标大小:$size")
                }
        }
    }

    //---

    /**拍照
     * [action] 回调*/
    fun capturePhoto(@WorkerThread action: (uri: Uri?, exception: Exception?) -> Unit) {
        val photoFile = libCacheFile(fileNameUUID(".jpg"))
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = _cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        }
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        if (_lifecycleCameraController == null) {
            action(null, IllegalStateException("未绑定相机"))
            return
        }

        try {
            _lifecycleCameraController?.takePicture(
                outputFileOptions,
                Dispatchers.Default.asExecutor(),
                @WorkerThread
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        action(outputFileResults.savedUri ?: photoFile.toFileUri(), null)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        action(null, exception)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            action(null, e)
        }
    }
}