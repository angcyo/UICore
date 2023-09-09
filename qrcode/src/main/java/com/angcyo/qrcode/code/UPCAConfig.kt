package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 条码类型, 支持的配置参数, 只能是数字
 * 参考[com.google.zxing.oned.EAN13Writer]
 *
 * [com.google.zxing.BarcodeFormat.UPC_A]
 * [com.google.zxing.oned.UPCAWriter]
 *
 * 11个数字, 应为会在前面自动补上0
 * [Ean13Config]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class UPCAConfig(
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.EncodeHintType.MARGIN]
     * [com.google.zxing.oned.UPCEANWriter.getDefaultMargin]*/
    val margin: Int = 9
) : BaseCodeConfig(BarcodeFormat.UPC_A, DslCode.DEFAULT_CODE_WIDTH, DslCode.DEFAULT_CODE_HEIGHT) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.MARGIN, margin)
    }
}
