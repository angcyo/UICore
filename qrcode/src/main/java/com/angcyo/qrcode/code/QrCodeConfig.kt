package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * 二维码类型, 支持的配置参数, 对字符没有限制
 * [com.google.zxing.BarcodeFormat.QR_CODE]
 * [com.google.zxing.qrcode.QRCodeWriter]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class QrCodeConfig(
    /**字符编码
     * [com.google.zxing.qrcode.encoder.Encoder.DEFAULT_BYTE_MODE_ENCODING]
     * [com.google.zxing.EncodeHintType.CHARACTER_SET]*/
    val character: String? = "utf-8",
    /**错误级别
     * [com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L]
     * [com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M]
     * [com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q]
     * [com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H]
     * [com.google.zxing.qrcode.decoder.ErrorCorrectionLevel]*/
    //val errorCorrection: ErrorCorrectionLevel? = ErrorCorrectionLevel.H,
    val errorLevel: String? = null,
    /**指定生成条形码时要使用的边距（以像素为单位）。
     * [com.google.zxing.qrcode.QRCodeWriter.QUIET_ZONE_SIZE]
     * [com.google.zxing.EncodeHintType.MARGIN]
     * */
    val margin: Int? = 4,
    /**指定是否应将数据编码为 GS1 标准
     * [com.google.zxing.EncodeHintType.GS1_FORMAT]
     * */
    val gs1Format: Boolean = false,
    /**指定要编码的 QR 码的确切版本。
     * [com.google.zxing.EncodeHintType.QR_VERSION]*/
    val qrVersion: Int? = null,
    /**指定要使用的 QR 码掩码图案。 [0~8)
     *  -1:自动
     * [com.google.zxing.qrcode.encoder.QRCode.NUM_MASK_PATTERNS]
     * [com.google.zxing.EncodeHintType.QR_MASK_PATTERN]
     * */
    val qrMaskPattern: Int? = -1,
) : BaseCodeConfig(BarcodeFormat.QR_CODE, DslCode.DEFAULT_CODE_SIZE, DslCode.DEFAULT_CODE_SIZE) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        character?.let { put(EncodeHintType.CHARACTER_SET, it) }
        //errorCorrection?.let { put(EncodeHintType.ERROR_CORRECTION, it) }
        errorLevel?.let { put(EncodeHintType.ERROR_CORRECTION, it) }
        margin?.let { put(EncodeHintType.MARGIN, it) }
        put(EncodeHintType.GS1_FORMAT, gs1Format)
        qrVersion?.let { put(EncodeHintType.QR_VERSION, it) }
        qrMaskPattern?.let { put(EncodeHintType.QR_MASK_PATTERN, it) }
    }
}
