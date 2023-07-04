package com.angcyo.camerax.core

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * [Bitmap]分析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/04
 */

/**图片有效*/
typealias BitmapAnalysisAvailableAction = (imageProxy: ImageProxy, bitmap: Bitmap, coordinateMatrix: Matrix?) -> Unit

/**[Bitmap]图片分析器*/
open class BitmapImageAnalysisAnalyzer(
    coordinateSystem: Int = ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
    var onBitmapAvailable: BitmapAnalysisAvailableAction
) : ImageAnalysisAnalyzer() {

    init {
        analyzerTargetCoordinateSystem = coordinateSystem
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
        /*val matrix = imageProxy.getBitmapTransform(cameraItemConfig.itemCameraSelector)
        bitmap = bitmap.transform(matrix)*/
        onBitmapAvailable(imageProxy, bitmap, getCoordinateTransform(imageProxy))
    }

}