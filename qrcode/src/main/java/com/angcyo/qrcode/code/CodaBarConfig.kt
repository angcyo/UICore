package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 条码类型, 支持的配置参数
 *
 * 至少要2个字符,不足2个字符会自动在前后拼上[com.google.zxing.oned.CodaBarWriter.DEFAULT_GUARD]
 * 只能是数字或者[com.google.zxing.oned.CodaBarWriter.CHARS_WHICH_ARE_TEN_LENGTH_EACH_AFTER_DECODED]中的字符
 *
 * [com.google.zxing.BarcodeFormat.CODABAR]
 * [com.google.zxing.oned.CodaBarWriter]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class CodaBarConfig(
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.EncodeHintType.MARGIN]
     * [com.google.zxing.oned.OneDimensionalCodeWriter.getDefaultMargin]*/
    val margin: Int = 10,
) : BaseCodeConfig(BarcodeFormat.CODABAR, DslCode.DEFAULT_CODE_WIDTH, DslCode.DEFAULT_CODE_HEIGHT) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.MARGIN, margin)
    }
}
