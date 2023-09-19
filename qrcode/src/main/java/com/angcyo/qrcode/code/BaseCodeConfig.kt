package com.angcyo.qrcode.code

import android.graphics.Bitmap
import android.graphics.Color
import com.angcyo.qrcode.createBarcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType

/**
 * https://github.com/zxing/zxing
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
abstract class BaseCodeConfig(
    var format: BarcodeFormat,
    var width: Int,
    var height: Int,
    var foregroundColor: Int = Color.BLACK,
    var backgroundColor: Int = Color.WHITE,
) : IBarcodeEncode {

    companion object {

        /**使用[format]创建[BaseCodeConfig]*/
//        fun codeConfigOf(format: BarcodeFormat): BaseCodeConfig? = when (format) {
//            BarcodeFormat.QR_CODE -> QrCodeConfig()
//            BarcodeFormat.CODE_128 -> Code128Config()
//            BarcodeFormat.CODE_39 -> Code39Config()
//            BarcodeFormat.CODE_93 -> Code93Config()
//            BarcodeFormat.CODABAR -> CodaBarConfig()
//            BarcodeFormat.EAN_13 -> Ean13Config()
//            BarcodeFormat.EAN_8 -> Ean8Config()
//            BarcodeFormat.ITF -> ITFConfig()
//            BarcodeFormat.UPC_A -> UPCAConfig()
//            BarcodeFormat.UPC_E -> UPCEConfig()
//            BarcodeFormat.PDF_417 -> PDF417Config()
//            BarcodeFormat.AZTEC -> AztecConfig()
//            BarcodeFormat.DATA_MATRIX -> DataMatrixConfig()
//            //BarcodeFormat.MAXICODE -> MaxiCodeConfig()
//            //BarcodeFormat.RSS_14 -> RSS14Config()
//            //BarcodeFormat.RSS_EXPANDED -> RSSExpandedConfig()
//            //BarcodeFormat.UPC_EAN_EXTENSION -> UPC_EANExtensionConfig()
//            else -> null
//        }
    }

    /**获取对应的配置信息*/
    abstract fun getHints(): Map<EncodeHintType, Any>?

    override fun encode(content: String?): Bitmap? = createBarcode(
        content,
        width, height,
        format,
        foregroundColor, backgroundColor,
        getHints()
    )
}

/**https://github.com/zxing/zxing#supported-formats
 * 二维码类型
 * */
fun BarcodeFormat.is2DCodeType(): Boolean = when (this) {
    BarcodeFormat.QR_CODE,
    BarcodeFormat.DATA_MATRIX,
    BarcodeFormat.PDF_417,
    BarcodeFormat.AZTEC,
    BarcodeFormat.MAXICODE,
    BarcodeFormat.RSS_14,
    BarcodeFormat.RSS_EXPANDED,
    -> true

    else -> false
}

/**一维码类型*/
fun BarcodeFormat.is1DCodeType(): Boolean = when (this) {
    BarcodeFormat.CODE_128,
    BarcodeFormat.CODE_39,
    BarcodeFormat.CODE_93,
    BarcodeFormat.CODABAR,
    BarcodeFormat.EAN_13,
    BarcodeFormat.EAN_8,
    BarcodeFormat.ITF,
    BarcodeFormat.UPC_A,
    BarcodeFormat.UPC_E,
    BarcodeFormat.UPC_EAN_EXTENSION,
    -> true

    else -> false
}

/**当前的条形码格式, 是否有纠错等级设置*/
fun BarcodeFormat.haveErrorCorrection(): Boolean = when (this) {
    BarcodeFormat.QR_CODE,
    BarcodeFormat.PDF_417,
    BarcodeFormat.AZTEC,
    -> true

    else -> false
}

/**类型转换[BarcodeFormat]*/
fun String.toBarcodeFormat(): BarcodeFormat? {
    return try {
        BarcodeFormat.valueOf(this)
    } catch (_: Exception) {
        null
    }
}

/**类型转换[BaseCodeConfig]
 * [com.angcyo.qrcode.code.BaseCodeConfig.encode]
 * */
fun String.toBarcodeConfig(action: BaseCodeConfig.() -> Unit): BaseCodeConfig? {
    val format = toBarcodeFormat()
    return when (format) {
        BarcodeFormat.AZTEC -> AztecConfig().apply(action)
        BarcodeFormat.DATA_MATRIX -> DataMatrixConfig().apply(action)
        BarcodeFormat.PDF_417 -> PDF417Config().apply(action)
        BarcodeFormat.QR_CODE -> QrCodeConfig().apply(action)
        BarcodeFormat.CODE_128 -> Code128Config().apply(action)
        BarcodeFormat.CODE_39 -> Code39Config().apply(action)
        BarcodeFormat.CODE_93 -> Code93Config().apply(action)
        BarcodeFormat.CODABAR -> CodaBarConfig().apply(action)
        BarcodeFormat.EAN_13 -> Ean13Config().apply(action)
        BarcodeFormat.EAN_8 -> Ean8Config().apply(action)
        BarcodeFormat.ITF -> ITFConfig().apply(action)
        BarcodeFormat.UPC_A -> UPCAConfig().apply(action)
        BarcodeFormat.UPC_E -> UPCEConfig().apply(action)
        //BarcodeFormat.MAXICODE -> MaxiCodeConfig()
        //BarcodeFormat.RSS_14 -> RSS14Config()
        //BarcodeFormat.RSS_EXPANDED -> RSSExpandedConfig()
        //BarcodeFormat.UPC_EAN_EXTENSION -> UPC_EANExtensionConfig()
        else -> null
    }
}
