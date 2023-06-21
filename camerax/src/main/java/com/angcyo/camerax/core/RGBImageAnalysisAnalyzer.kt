package com.angcyo.camerax.core

import android.graphics.ImageFormat
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.angcyo.library.L
import com.angcyo.library.component._delay
import com.angcyo.library.ex.logInfo

/**
 *  将[ImageFormat.YUV_420_888]转换成[ImageFormat.RGB_565]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/18
 */
class RGBImageAnalysisAnalyzer : ImageAnalysis.Analyzer {

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
    override fun analyze(image: ImageProxy) {
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
            image.format.toImageFormatStr(),
            "w:${image.width} h:${image.height}",
            image.cropRect,
            image.imageInfo
        )//w:640 h:480

        //YUV420 转成 RGB
        val bitmap = image.toBitmap() //w:640 h:332
        L.d(bitmap?.logInfo())

        _delay(5_000) {
            image.close() //关闭之后, 才有下一帧
        }
    }

    override fun getDefaultTargetResolution(): Size? {
        return super.getDefaultTargetResolution()
    }

    /**
     * [androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED]
     * [androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL] 默认
     * */
    override fun getTargetCoordinateSystem(): Int {
        return super.getTargetCoordinateSystem()
    }

    override fun updateTransform(matrix: Matrix?) {
        super.updateTransform(matrix)
    }
}