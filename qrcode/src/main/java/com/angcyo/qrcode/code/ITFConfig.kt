package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 条码类型, 支持的配置参数, 只能是数字, 必须是偶数个数字, 不能超过80个数字
 *
 * [com.google.zxing.BarcodeFormat.ITF]
 * [com.google.zxing.oned.ITFWriter]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class ITFConfig(
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.EncodeHintType.MARGIN]
     * [com.google.zxing.oned.OneDimensionalCodeWriter.getDefaultMargin]*/
    val margin: Int? = 10,
) : BaseCodeConfig(BarcodeFormat.ITF, DslCode.DEFAULT_CODE_WIDTH, DslCode.DEFAULT_CODE_HEIGHT) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        margin?.let { put(EncodeHintType.MARGIN, it) }
    }
}
