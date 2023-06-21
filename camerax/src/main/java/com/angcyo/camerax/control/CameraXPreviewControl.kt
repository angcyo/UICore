package com.angcyo.camerax.control

import android.Manifest
import androidx.annotation.MainThread
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.angcyo.camerax.core.RGBImageAnalysisAnalyzer
import com.angcyo.fragment.requestPermissions
import com.angcyo.library.BuildConfig
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.ThreadExecutor
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.havePermission

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

    init {
        //CameraXConfig.Provider
    }

    /**绑定到声明周期, 并且开始预览
     * 需要权限[Manifest.permission.CAMERA]
     * [requestPermission]*/
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
                isPinchToZoomEnabled
                isTapToFocusEnabled
                //isVideoCaptureEnabled
                isImageCaptureEnabled
                isImageAnalysisEnabled

                //设置需要使用的功能, 默认IMAGE_CAPTURE | IMAGE_ANALYSIS
                //IMAGE_CAPTURE | IMAGE_ANALYSIS | VIDEO_CAPTURE

                //前置摄像头
                if (BuildConfig.DEBUG) {
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }
                //后置摄像头, 默认
                //cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                //开始分析图像
                setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                setImageAnalysisAnalyzer(
                    ThreadExecutor /*previewView.context.mainExecutor()*/,
                    RGBImageAnalysisAnalyzer()
                )
                //clearImageAnalysisAnalyzer()

                //图片捕获模式, 质量优先or速度优先
                imageCaptureMode

                //闪光灯模式
                imageCaptureFlashMode

                //缩放状态发生更改回调
                zoomState.observe(lifecycleOwner) {
                    L.d("摄像头缩放:$it")
                }

                tapToFocusState.observe(lifecycleOwner) {
                    L.d("摄像头对焦:$it")
                }

                bindToLifecycle(lifecycleOwner)
            }

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
    }
}