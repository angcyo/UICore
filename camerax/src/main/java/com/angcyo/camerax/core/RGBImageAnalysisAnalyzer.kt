package com.angcyo.camerax.core

import android.graphics.ImageFormat
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.angcyo.library.L
import com.angcyo.library.component._delay

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
        L.d(
            image.format.toImageFormatStr(),
            "w:${image.width} h:${image.height}",
            image.cropRect,
            image.imageInfo
        )

        //ImageFormat.YUV_420_888 转 ImageFormat.RGB_565
        //获取YUV
        //val yBuffer = image.planes[0].buffer
        //val uBuffer = image.planes[1].buffer
        //val vBuffer = image.planes[2].buffer

        _delay(3_000) {
            image.close() //关闭之后, 才有下一帧
        }
    }

    override fun getDefaultTargetResolution(): Size? {
        return super.getDefaultTargetResolution()
    }

    override fun getTargetCoordinateSystem(): Int {
        return super.getTargetCoordinateSystem()
    }

    override fun updateTransform(matrix: Matrix?) {
        super.updateTransform(matrix)
    }
}