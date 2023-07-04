package com.angcyo.camerax.core

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import com.angcyo.library.L
import com.angcyo.library.LTime
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
class RGBImageAnalysisAnalyzer : ImageAnalysisAnalyzer() {

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
        if (matrix == null) {
            result.set(centerRect)
        }
        matrix?.mapRect(result, centerRect)
        result.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())//这个时候再旋转
        onRectTestAction.invoke(result)

        //YUV420 转成 RGB
        LTime.tick()
        val bitmap = imageProxy.toBitmap()
            ?.transform(imageProxy.getBitmapTransform(analyzerCameraSelector)) //w:640 h:332
        L.d(bitmap?.logInfo() + " 耗时:${LTime.time()}")
        /*val bitmap2 =
            imageProxy.toBitmapConverter().transform(getBitmapTransform(imageProxy.imageInfo))
        L.d(bitmap2.logInfo() + " 耗时:${LTime.time()}")//w:640 h:332*/

        _delay(5_000) {
            imageProxy.close() //关闭之后, 才有下一帧
        }
    }
}