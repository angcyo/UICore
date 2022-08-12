package com.angcyo.doodle.data

import android.graphics.Bitmap
import com.angcyo.doodle.DoodleDelegate

/**
 * 图片绘制的数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapElementData : BaseBoundsElementData() {

    /**绘制的图片*/
    var bitmap: Bitmap? = null

    /**更新图片数据*/
    fun updateBitmap(doodleDelegate: DoodleDelegate, bitmap: Bitmap?) {
        this.bitmap = bitmap
        bitmap?.let {
            bounds.set(doodleDelegate.getCenterRect(it.width, it.height))
        }
    }

}