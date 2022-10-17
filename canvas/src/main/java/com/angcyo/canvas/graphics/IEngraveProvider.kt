package com.angcyo.canvas.graphics

import android.graphics.Bitmap

/**
 * 雕刻数据提供者
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/17
 */
interface IEngraveProvider {

    /**获取用于直接雕刻的图片, 返回的图片应该已经进行了缩放和旋转
     *
     * 获取一个用于雕刻的图片数据,
     * 请注意, 这个图片是原始数据,
     * 可能需要再次缩放处理后发给机器
     * */
    fun getEngraveBitmap(): Bitmap?

}