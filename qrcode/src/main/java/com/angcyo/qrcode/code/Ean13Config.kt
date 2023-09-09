package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 条码类型, 支持的配置参数, 只能是数字
 * 必须是13个数字[0~9]
 * 如果是12个数字, 会自动在面补上一个校验位
 * [com.google.zxing.oned.UPCEANReader.getStandardUPCEANChecksum]
 *
 * [com.google.zxing.BarcodeFormat.EAN_13]
 * [com.google.zxing.oned.EAN13Writer]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class Ean13Config(
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.EncodeHintType.MARGIN]
     * [com.google.zxing.oned.UPCEANWriter.getDefaultMargin]*/
    val margin: Int = 9,
) : BaseCodeConfig(BarcodeFormat.EAN_13, DslCode.DEFAULT_CODE_WIDTH, DslCode.DEFAULT_CODE_HEIGHT) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.MARGIN, margin)
    }
}
