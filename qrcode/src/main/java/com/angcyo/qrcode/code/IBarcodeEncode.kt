package com.angcyo.qrcode.code

import android.graphics.Bitmap

/**
 * 用来生成条码的接口
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
interface IBarcodeEncode {

    /**使用指定的内容, 创建对应的条形码/二维码*/
    fun encode(content: String?): Bitmap?
}