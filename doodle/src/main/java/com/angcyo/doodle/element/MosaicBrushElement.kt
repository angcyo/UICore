package com.angcyo.doodle.element

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint

/**
 * 马赛克
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class MosaicBrushElement(brushElementData: BrushElementData) : PenBrushElement(brushElementData) {

    /**马赛克缩放的级别, 值越大马赛克越小*/
    var mosaicLevel: Float = 50f

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)
        val bitmap = manager.doodleDelegate.getLayerPreviewBitmap()

        val matrix = Matrix()
        matrix.setScale(1f / mosaicLevel, 1f / mosaicLevel)
        //缩放原始图片
        val mosaicBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        matrix.reset()
        matrix.setScale(mosaicLevel, mosaicLevel)

        //在放大图片, 这样就相当于把原图指定位置的像素点放大了
        val shader = BitmapShader(mosaicBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        shader.setLocalMatrix(matrix)
        paint.shader = shader
    }
}