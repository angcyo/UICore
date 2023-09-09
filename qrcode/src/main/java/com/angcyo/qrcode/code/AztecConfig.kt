package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.aztec.encoder.Encoder

/**
 * 二维码类型, 支持的配置参数, 对字符没有限制
 * [com.google.zxing.BarcodeFormat.AZTEC]
 * [com.google.zxing.aztec.AztecWriter]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class AztecConfig(
    /**字符编码
     * [com.google.zxing.EncodeHintType.CHARACTER_SET]*/
    val character: String? = "utf-8",
    /**表示纠错字的最小百分比[0~100]
     * [com.google.zxing.EncodeHintType.ERROR_CORRECTION]*/
    val errorCorrection: Int? = Encoder.DEFAULT_EC_PERCENT,
    /**指定 Aztec 代码所需的层数。负数（-1、-2、-3、-4）指定紧凑的 Aztec 代码。
     * 0 表示使用最小层数（默认值）。
     * 正数 (1, 2, .. 32) 指定正常（非紧凑）Aztec 代码。
     * （类型Integer或整数值的String表示形式）。
     * [com.google.zxing.EncodeHintType.AZTEC_LAYERS]*/
    val aztecLayers: Int? = Encoder.DEFAULT_AZTEC_LAYERS,
) : BaseCodeConfig(BarcodeFormat.AZTEC, DslCode.DEFAULT_CODE_SIZE, DslCode.DEFAULT_CODE_SIZE) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        character?.let { put(EncodeHintType.CHARACTER_SET, it) }
        errorCorrection?.let { put(EncodeHintType.ERROR_CORRECTION, it) }
        aztecLayers?.let { put(EncodeHintType.AZTEC_LAYERS, it) }
    }
}
