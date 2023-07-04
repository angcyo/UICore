package com.angcyo.camerax.core

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import com.angcyo.library.L

/**
 * 图片分析
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/04
 */
open class ImageAnalysisAnalyzer : ImageAnalysis.Analyzer {

    /**需要使用的坐标系统
     * [androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]
     * [androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL] 默认
     * */
    var analyzerTargetCoordinateSystem: Int = CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED

    override fun analyze(imageProxy: ImageProxy) {

    }

    @SuppressLint("UnsafeOptInUsageError")
    private val transformFactory = ImageProxyTransformFactory()

    /**在数据中的坐标, 偏移到预览界面上的坐标, 需要进行的转换矩阵
     * [isUsingRotationDegrees] [androidx.camera.view.transform.ImageProxyTransformFactory.setUsingRotationDegrees]*/
    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    fun getCoordinateTransform(
        imageProxy: ImageProxy,
        isUsingRotationDegrees: Boolean = true
    ): Matrix? {
        // By default, the matrix is identity for COORDINATE_SYSTEM_ORIGINAL.
        if (targetCoordinateSystem != ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL) {
            // Calculate the transform if not COORDINATE_SYSTEM_ORIGINAL.
            val sensorToTarget: Matrix? = sensorToTarget
            if (sensorToTarget == null) {
                // If the app set a target coordinate system, do not perform detection until the
                // transform is ready.
                L.d("Transform is null.")
                return null
            }
            val transform = Matrix()
            transformFactory.isUsingRotationDegrees = isUsingRotationDegrees
            val analysisTransform: OutputTransform = transformFactory.getOutputTransform(imageProxy)
            val cropRectSize = Size(imageProxy.cropRect.width(), imageProxy.cropRect.height())
            val coordinateTransform = CoordinateTransform(
                analysisTransform,
                OutputTransform(sensorToTarget, cropRectSize)
            )
            coordinateTransform.transform(transform)
            return transform
        }
        return null
    }

    //---

    /**缺省的图片大小*/
    override fun getDefaultTargetResolution(): Size? {
        return super.getDefaultTargetResolution()
    }

    /**坐标系统
     * [androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]
     * [androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL] 默认
     * */
    override fun getTargetCoordinateSystem(): Int {
        return analyzerTargetCoordinateSystem
    }

    private var sensorToTarget: Matrix? = null

    /**只有在[androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]时才会被调用
     * [getTargetCoordinateSystem]*/
    override fun updateTransform(matrix: Matrix?) {
        sensorToTarget = if (matrix == null) {
            null
        } else {
            if (sensorToTarget == null) {
                Matrix(matrix)
            } else {
                sensorToTarget!!.apply {
                    set(matrix)
                }
            }
        }
    }
}