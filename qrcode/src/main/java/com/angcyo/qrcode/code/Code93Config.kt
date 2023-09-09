package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 二维码类型, 支持的配置参数, 最大字符长度为80
 * [com.google.zxing.BarcodeFormat.CODE_93]
 * [com.google.zxing.oned.Code93Writer]
 *
 * 仅支持"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*"字符
 * [com.google.zxing.oned.Code93Reader.ALPHABET_STRING]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class Code93Config(
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.EncodeHintType.MARGIN]
     * [com.google.zxing.oned.OneDimensionalCodeWriter.getDefaultMargin]*/
    val margin: Int = 10,
) : BaseCodeConfig(BarcodeFormat.CODE_93, DslCode.DEFAULT_CODE_WIDTH, DslCode.DEFAULT_CODE_HEIGHT) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.MARGIN, margin)
    }
}
