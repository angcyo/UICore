package com.angcyo.crop.ui

import android.graphics.Bitmap

/**
 * 数据提供器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/25
 */
interface ICropProvide {

    /**获取需要裁剪的图片*/
    fun getOriginCropBitmap(): Bitmap?

}