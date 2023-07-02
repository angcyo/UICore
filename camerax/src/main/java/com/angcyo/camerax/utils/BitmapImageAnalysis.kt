package com.angcyo.camerax.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.Surface
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import com.angcyo.camerax.core.getBitmapTransform
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.component._delay
import com.angcyo.library.ex.logInfo
import com.angcyo.library.ex.transform

/**
 * 直接获取Bitmap, 从Camera中
 * https://stackoverflow.com/questions/62201227/android-camera-x-imageanalyzer-image-format-for-tflite
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/07/02
 */
class BitmapImageAnalysis {

    @SuppressLint("RestrictedApi")
    var executor = CameraXExecutors.directExecutor()

    /**创建*/
    fun build(viewFinder: View?): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(viewFinder?.display?.rotation ?: Surface.ROTATION_0)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            LTime.tick()
            // The image rotation and RGB image buffer are initialized only once
            // the analyzer has started running
            val imageRotationDegrees = imageProxy.imageInfo.rotationDegrees
            var bitmapBuffer =
                Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
            bitmapBuffer =
                bitmapBuffer.transform(imageProxy.getBitmapTransform(CameraSelector.DEFAULT_BACK_CAMERA))
            L.d(bitmapBuffer?.logInfo() + " 耗时:${LTime.time()}") //耗时: 88 142 251 139 218 134 ms

            _delay(5_000) {
                imageProxy.close() //关闭之后, 才有下一帧
            }
        }

        // Copy out RGB bits to our shared buffer
        /*image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)  }
        image.close()*/

        return imageAnalysis
    }
}