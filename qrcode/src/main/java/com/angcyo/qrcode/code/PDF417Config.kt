package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.pdf417.encoder.Compaction
import com.google.zxing.pdf417.encoder.Dimensions

/**
 * 二维码类型, 支持的配置参数, 不支持emoji表情, 不支持中文
 * [com.google.zxing.BarcodeFormat.PDF_417]
 * [com.google.zxing.pdf417.PDF417Writer]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class PDF417Config(

    /**是否使用紧凑模式
     * [com.google.zxing.EncodeHintType.PDF417_COMPACT]
     * */
    val compact: Boolean = false,

    /**
     * 压缩模式
     * [Compaction.AUTO]
     * [Compaction.TEXT]
     * [Compaction.BYTE]
     * [Compaction.NUMERIC]
     * [com.google.zxing.EncodeHintType.PDF417_COMPACTION]*/
    val compaction: Compaction = Compaction.AUTO,

    /**
     * 指定 PDF417 的最小和最大行数和列数
     * [com.google.zxing.EncodeHintType.PDF417_DIMENSIONS]
     * */
    val dimensions: Dimensions? = null,

    /**边界
     * [com.google.zxing.pdf417.PDF417Writer.WHITE_SPACE]
     * [com.google.zxing.EncodeHintType.MARGIN]
     * */
    val margin: Int? = 30,

    /**错误级别, [0~8]
     * [com.google.zxing.pdf417.PDF417Writer.DEFAULT_ERROR_CORRECTION_LEVEL]
     * [com.google.zxing.EncodeHintType.ERROR_CORRECTION]
     * */
    val errorCorrection: Int? = 2,
    /**字符编码
     * [com.google.zxing.EncodeHintType.CHARACTER_SET]*/
    val character: String? = "utf-8",
) : BaseCodeConfig(BarcodeFormat.PDF_417, DslCode.DEFAULT_CODE_SIZE, DslCode.DEFAULT_CODE_SIZE) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.PDF417_COMPACT, compact)
        put(EncodeHintType.PDF417_COMPACTION, compaction)
        dimensions?.let { put(EncodeHintType.PDF417_DIMENSIONS, it) }
        margin?.let { put(EncodeHintType.MARGIN, it) }
        errorCorrection?.let { put(EncodeHintType.ERROR_CORRECTION, it) }
        character?.let { put(EncodeHintType.CHARACTER_SET, it) }
    }
}
