package com.angcyo.camerax.core

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import com.angcyo.library.L
import com.angcyo.library.component._delay
import com.angcyo.library.ex.logInfo
import com.angcyo.library.ex.rotate
import com.angcyo.library.ex.transform

/**
 *  将[ImageFormat.YUV_420_888]转换成[ImageFormat.RGB_565]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/18
 */
class RGBImageAnalysisAnalyzer : ImageAnalysis.Analyzer {

    /**需要使用的坐标系统
     * [androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]
     * [androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL] 默认
     * */
    var analyzerTargetCoordinateSystem: Int =
        COORDINATE_SYSTEM_VIEW_REFERENCED //COORDINATE_SYSTEM_ORIGINAL

    /**当前使用的摄像头*/
    var analyzerCameraSelector: CameraSelector? = null

    /**测试, 绘制一个矩形在预览界面中心*/
    var onRectTestAction: (RectF) -> Unit = {}

    /**图片格式YUV:
     * https://baike.baidu.com/item/YUV/3430784
     * https://zh.wikipedia.org/wiki/YUV
     * “Y”表示明亮度（Luminance、Luma），
     * “U”和“V”则是色度、浓度（Chrominance、Chroma），
     * [android.graphics.ImageFormat.YUV_420_888]
     *
     * ![](https://wikimedia.org/api/rest_v1/media/math/render/svg/50ffda2265cc0b057d327e3d042110d2080953d7)
     * <p>
     * <img src="https://wikimedia.org/api/rest_v1/media/math/render/svg/50ffda2265cc0b057d327e3d042110d2080953d7" alt="">
     *
     * */
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        //Pixel 6
        //1:前后摄像头出来的数据大小一致
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
        //w:1280 h:720 -> 宽:1280 高:666

        //2:
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
        //w:640 h:480 -> 宽:640 高:332

        //OnePlus Ace2 Pro
        //1:
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
        //w:864 h:480 -> 宽:864 高:442

        //2:
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
        //w:640 h:480 -> 宽:640 高:328

        //realme 11 Pro+
        //1:
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
        //w:960 h:544 -> 宽:960 高:490

        //2:
        //imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
        //w:640 h:480 -> 宽:640 高:326

        L.d(
            imageProxy.format.toImageFormatStr(),
            "w:${imageProxy.width} h:${imageProxy.height} r:${imageProxy.imageInfo.rotationDegrees}",
            imageProxy.cropRect,
            imageProxy.imageInfo
        )//w:640 h:480

        val centerRect = RectF()
        val cx = imageProxy.width / 2f
        val cy = imageProxy.height / 2f
        val size = Size(100, 200) //100像素的点
        centerRect.set(
            cx - size.width / 2,
            cy - size.height / 2,
            cx + size.width / 2,
            cy + size.height / 2
        )
        val matrix = getCoordinateTransform(imageProxy, false)
        val result = RectF()
        matrix.mapRect(result, centerRect)
        result.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())//这个时候再旋转
        onRectTestAction.invoke(result)

        //YUV420 转成 RGB
        val bitmap =
            imageProxy.toBitmap()?.transform(getBitmapTransform(imageProxy.imageInfo)) //w:640 h:332
        val bitmap2 = imageProxy.toBitmapConverter()
        L.d(bitmap?.logInfo())
        L.d(bitmap2.logInfo())

        _delay(5_000) {
            imageProxy.close() //关闭之后, 才有下一帧
        }
    }

    /**拍出来的照片, 需要进行的矩阵变换
     * 默认拍出来的照片都是带旋转的.
     * 前摄拍出来的照片水平是翻转的*/
    fun getBitmapTransform(imageInfo: ImageInfo?): Matrix? {
        val cameraSelector = analyzerCameraSelector ?: return null
        val degrees = (imageInfo?.rotationDegrees ?: 0).toFloat()
        return when (cameraSelector) {
            CameraSelector.DEFAULT_BACK_CAMERA -> Matrix().apply {
                postRotate(degrees)
            }

            CameraSelector.DEFAULT_FRONT_CAMERA -> Matrix().apply {
                postRotate(degrees)
                postScale(-1f, 1f)
            }

            else -> {
                null
            }
        }
    }

    /**在数据中的坐标, 偏移到预览界面上的坐标, 需要进行的转换矩阵
     * [isUsingRotationDegrees] [androidx.camera.view.transform.ImageProxyTransformFactory.setUsingRotationDegrees]*/
    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    fun getCoordinateTransform(
        imageProxy: ImageProxy,
        isUsingRotationDegrees: Boolean = true
    ): Matrix {
        // By default, the matrix is identity for COORDINATE_SYSTEM_ORIGINAL.
        val transform = Matrix()
        if (targetCoordinateSystem != ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL) {
            // Calculate the transform if not COORDINATE_SYSTEM_ORIGINAL.
            val sensorToTarget: Matrix? = sensorToTarget
            if (sensorToTarget == null) {
                // If the app set a target coordinate system, do not perform detection until the
                // transform is ready.
                L.d("Transform is null.")
                return transform
            }
            val transformFactory = ImageProxyTransformFactory()
            transformFactory.isUsingRotationDegrees = isUsingRotationDegrees
            val analysisTransform: OutputTransform = transformFactory.getOutputTransform(imageProxy)
            val cropRectSize = Size(imageProxy.cropRect.width(), imageProxy.cropRect.height())
            val coordinateTransform = CoordinateTransform(
                analysisTransform,
                OutputTransform(sensorToTarget, cropRectSize)
            )
            coordinateTransform.transform(transform)
        }
        return transform
    }

    /**缺省的图片大小*/
    override fun getDefaultTargetResolution(): Size? {
        return super.getDefaultTargetResolution()
    }

    /**
     * [androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]
     * [androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL] 默认
     * */
    override fun getTargetCoordinateSystem(): Int {
        return analyzerTargetCoordinateSystem
    }

    private var sensorToTarget: Matrix? = null

    /**只有在[androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]时才有效
     * [getTargetCoordinateSystem]*/
    override fun updateTransform(matrix: Matrix?) {
        sensorToTarget = if (matrix == null) {
            null
        } else {
            Matrix(matrix)
        }
    }
}