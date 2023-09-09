package com.angcyo.qrcode.code

import com.angcyo.qrcode.DslCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.Dimension
import com.google.zxing.EncodeHintType
import com.google.zxing.datamatrix.encoder.SymbolShapeHint

/**
 * 二维码类型, 支持的配置参数, 不支持emoji表情, 不支持中文
 * [com.google.zxing.BarcodeFormat.DATA_MATRIX]
 * [com.google.zxing.datamatrix.DataMatrixWriter]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/09
 */
data class DataMatrixConfig(
    /**指定数据矩阵的矩阵形状
     * [com.google.zxing.EncodeHintType.DATA_MATRIX_SHAPE]*/
    val shape: SymbolShapeHint = SymbolShapeHint.FORCE_NONE,

    /**指定条形码尺寸
     * [com.google.zxing.EncodeHintType.MIN_SIZE]
     * */
    val minSize: Dimension? = null,

    /**
     * [com.google.zxing.EncodeHintType.MAX_SIZE]
     * */
    val maxSize: Dimension? = null,
) : BaseCodeConfig(
    BarcodeFormat.DATA_MATRIX,
    DslCode.DEFAULT_CODE_SIZE,
    DslCode.DEFAULT_CODE_SIZE
) {
    override fun getHints(): Map<EncodeHintType, Any> = mutableMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.DATA_MATRIX_SHAPE, shape)
        minSize?.let { put(EncodeHintType.MIN_SIZE, it) }
        maxSize?.let { put(EncodeHintType.MAX_SIZE, it) }
    }
}
